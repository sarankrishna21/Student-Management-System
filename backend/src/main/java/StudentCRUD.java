import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class StudentCRUD {

    public static void main(String[] args) {

        // Connect to MongoDB
        MongoClient mongoClient =
                MongoClients.create("mongodb://localhost:27017");

        // Select Database
        MongoDatabase database =
                mongoClient.getDatabase("StudentManagement");

        // Select Collection
        MongoCollection<Document> collection =
                database.getCollection("students");

        System.out.println("Connected to MongoDB");
        System.out.println();

        // =====================================================
        // CREATE - Insert Student
        // =====================================================

        Document student = new Document()
                .append("studentId", "STU101")
                .append("name", "Sarankrishna")
                .append("age", 21)
                .append("department", "AI&DS")
                .append("email", "saran@gmail.com")
                .append("cgpa", 8.5)
                .append("skills",
                        java.util.Arrays.asList("Java", "MongoDB"));

        collection.insertOne(student);

        System.out.println("CREATE:");
        System.out.println("Student inserted successfully");
        System.out.println();

        // =====================================================
        // READ - Display Students
        // =====================================================

        System.out.println("READ:");
        System.out.println("Student Records:");

        FindIterable<Document> students = collection.find();

        for (Document s : students) {
            System.out.println(s.toJson());
        }

        System.out.println();

        // =====================================================
        // UPDATE - Update Student
        // =====================================================

        collection.updateOne(
                Filters.eq("studentId", "STU101"),
                Updates.combine(
                        Updates.set("age", 22),
                        Updates.set("cgpa", 9.0)
                )
        );

        System.out.println("UPDATE:");
        System.out.println("Student age and CGPA updated successfully");
        System.out.println();

        // =====================================================
        // READ UPDATED RECORD
        // =====================================================

        Document updatedStudent = collection.find(
                Filters.eq("studentId", "STU101")
        ).first();

        System.out.println("UPDATED RECORD:");

        if (updatedStudent != null) {
            System.out.println(updatedStudent.toJson());
        }

        System.out.println();

        // =====================================================
        // DELETE - Delete Student
        // =====================================================

        collection.deleteOne(
                Filters.eq("studentId", "STU101")
        );

        System.out.println("DELETE:");
        System.out.println("Student deleted successfully");
        System.out.println();

        // Close MongoDB Connection
        mongoClient.close();

        System.out.println("MongoDB connection closed.");
    }
}