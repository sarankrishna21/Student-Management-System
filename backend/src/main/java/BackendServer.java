import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class BackendServer {

    private static final int PORT = 8080;

    private static MongoClient mongoClient;
    private static MongoCollection<Document> collection;

    public static void main(String[] args) throws Exception {

        // Connect to MongoDB
        mongoClient = MongoClients.create("mongodb://127.0.0.1:27017");

        MongoDatabase database = mongoClient.getDatabase("StudentManagement");

        collection = database.getCollection("students");

        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/students", BackendServer::handleStudents);

        server.setExecutor(null);
        server.start();

        System.out.println("======================================");
        System.out.println("Student Management Backend Started");
        System.out.println("MongoDB: StudentManagement.students");
        System.out.println("Server: http://localhost:" + PORT);
        System.out.println("======================================");
    }

    private static void handleStudents(HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        String method = exchange.getRequestMethod();

        // Handle browser CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {

            // =========================
            // READ - GET /students
            // =========================
            if ("GET".equalsIgnoreCase(method)) {

                StringBuilder json = new StringBuilder("[");
                boolean first = true;

                for (Document student : collection.find()) {

                    if (!first) {
                        json.append(",");
                    }

                    json.append(student.toJson());
                    first = false;
                }

                json.append("]");

                sendResponse(exchange, 200, json.toString());
            }

            // =========================
            // CREATE - POST /students
            // =========================
            else if ("POST".equalsIgnoreCase(method)) {

                String body = readRequestBody(exchange);

                Document student = Document.parse(body);

                collection.insertOne(student);

                sendResponse(
                        exchange,
                        201,
                        new Document()
                                .append("message", "Student inserted successfully")
                                .append("student", student)
                                .toJson());
            }

            // =========================
            // UPDATE - PUT /students/{studentId}
            // =========================
            else if ("PUT".equalsIgnoreCase(method)) {

                String studentId = getStudentId(exchange);

                if (studentId == null || studentId.isEmpty()) {
                    sendResponse(exchange, 400,
                            "{\"error\":\"Student ID is required\"}");
                    return;
                }

                String body = readRequestBody(exchange);

                Document updates = Document.parse(body);

                updates.remove("_id");
                updates.remove("studentId");

                Document setDocument = new Document();

                for (String key : updates.keySet()) {
                    setDocument.append(key, updates.get(key));
                }

                collection.updateOne(
                        Filters.eq("studentId", studentId),
                        new Document("$set", setDocument));

                Document updatedStudent = collection.find(
                        Filters.eq("studentId", studentId)).first();

                if (updatedStudent == null) {
                    sendResponse(exchange, 404,
                            "{\"error\":\"Student not found\"}");
                } else {
                    sendResponse(
                            exchange,
                            200,
                            new Document()
                                    .append("message", "Student updated successfully")
                                    .append("student", updatedStudent)
                                    .toJson());
                }
            }

            // =========================
            // DELETE - DELETE /students/{studentId}
            // =========================
            else if ("DELETE".equalsIgnoreCase(method)) {

                String studentId = getStudentId(exchange);

                if (studentId == null || studentId.isEmpty()) {
                    sendResponse(exchange, 400,
                            "{\"error\":\"Student ID is required\"}");
                    return;
                }

                long deletedCount = collection.deleteOne(
                        Filters.eq("studentId", studentId)).getDeletedCount();

                if (deletedCount == 0) {
                    sendResponse(exchange, 404,
                            "{\"error\":\"Student not found\"}");
                } else {
                    sendResponse(
                            exchange,
                            200,
                            "{\"message\":\"Student deleted successfully\"}");
                }
            }

            else {
                sendResponse(exchange, 405,
                        "{\"error\":\"Method not allowed\"}");
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    new Document()
                            .append("error", e.getMessage())
                            .toJson());
        }
    }

    // Get studentId from URL
    private static String getStudentId(HttpExchange exchange) {

        String path = exchange.getRequestURI().getPath();

        String prefix = "/students/";

        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }

        return null;
    }

    // Read request body
    private static String readRequestBody(HttpExchange exchange)
            throws IOException {

        InputStream inputStream = exchange.getRequestBody();

        return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8);
    }

    // Add CORS headers
    private static void addCorsHeaders(HttpExchange exchange) {

        Headers headers = exchange.getResponseHeaders();

        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers",
                "Content-Type");
        headers.set("Content-Type",
                "application/json; charset=UTF-8");
    }

    // Send HTTP response
    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {

            outputStream.write(bytes);
        }
    }
}
