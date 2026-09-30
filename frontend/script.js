const API_URL = "http://localhost:8080/students";

const studentForm = document.getElementById("studentForm");
const studentTableBody = document.getElementById("studentTableBody");

let editingStudentId = null;


// ==========================================
// LOAD STUDENTS - READ
// ==========================================

async function loadStudents() {

    try {

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to load students");
        }

        const students = await response.json();

        displayStudents(students);

    } catch (error) {

        console.error(error);

        alert("Unable to connect to backend server.");
    }
}


// ==========================================
// DISPLAY STUDENTS
// ==========================================

function displayStudents(students) {

    studentTableBody.innerHTML = "";

    students.forEach(student => {

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${student.studentId}</td>
            <td>${student.name}</td>
            <td>${student.age}</td>
            <td>${student.department}</td>
            <td>${student.email}</td>
            <td>${student.cgpa}</td>
            <td>${student.skills.join(", ")}</td>

            <td>

                <button
                    class="action-btn edit-btn"
                    onclick='editStudent(${JSON.stringify(student)})'>
                    Edit
                </button>

                <button
                    class="action-btn delete-btn"
                    onclick="deleteStudent('${student.studentId}')">
                    Delete
                </button>

            </td>
        `;

        studentTableBody.appendChild(row);
    });
}


// ==========================================
// CREATE / UPDATE
// ==========================================

studentForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    const student = {

        studentId:
            document.getElementById("studentId").value.trim(),

        name:
            document.getElementById("name").value.trim(),

        age:
            Number(document.getElementById("age").value),

        department:
            document.getElementById("department").value.trim(),

        email:
            document.getElementById("email").value.trim(),

        cgpa:
            Number(document.getElementById("cgpa").value),

        skills:
            document.getElementById("skills").value
                .split(",")
                .map(skill => skill.trim())
                .filter(skill => skill !== "")
    };


    try {

        let response;

        // UPDATE
        if (editingStudentId) {

            response = await fetch(
                `${API_URL}/${editingStudentId}`,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        name: student.name,
                        age: student.age,
                        department: student.department,
                        email: student.email,
                        cgpa: student.cgpa,
                        skills: student.skills
                    })
                }
            );

        }

        // CREATE
        else {

            response = await fetch(
                API_URL,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(student)
                }
            );
        }


        const result = await response.json();


        if (!response.ok) {

            throw new Error(
                result.error || "Operation failed"
            );
        }


        alert(result.message);


        studentForm.reset();

        editingStudentId = null;

        document.querySelector(
            "#studentForm button"
        ).textContent = "Add Student";


        await loadStudents();


    } catch (error) {

        console.error(error);

        alert(error.message);
    }

});


// ==========================================
// EDIT STUDENT
// ==========================================

function editStudent(student) {

    editingStudentId = student.studentId;

    document.getElementById("studentId").value =
        student.studentId;

    document.getElementById("studentId").disabled = true;

    document.getElementById("name").value =
        student.name;

    document.getElementById("age").value =
        student.age;

    document.getElementById("department").value =
        student.department;

    document.getElementById("email").value =
        student.email;

    document.getElementById("cgpa").value =
        student.cgpa;

    document.getElementById("skills").value =
        student.skills.join(", ");

    document.querySelector(
        "#studentForm button"
    ).textContent = "Update Student";
}


// ==========================================
// DELETE STUDENT
// ==========================================

async function deleteStudent(studentId) {

    if (!confirm(
        `Delete student ${studentId}?`
    )) {
        return;
    }


    try {

        const response = await fetch(
            `${API_URL}/${studentId}`,
            {
                method: "DELETE"
            }
        );


        const result = await response.json();


        if (!response.ok) {

            throw new Error(
                result.error || "Delete failed"
            );
        }


        alert(result.message);

        await loadStudents();


    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}


// ==========================================
// INITIAL LOAD
// ==========================================

loadStudents();