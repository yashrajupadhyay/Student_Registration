import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Servlet mapped to /student
@WebServlet("/student")
public class StudentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/student_registration";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public StudentServlet() {
        super();
    }

    // Handles GET requests for listing, editing, and displaying students
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("action");

        try {
            // Ensure that the MySQL JDBC driver is loaded
            Class.forName("com.mysql.jdbc.Driver");

            if ("select".equalsIgnoreCase(action)) {
                // Select and display all students
                List<Student> students = getAllStudents();
                out.println("<html>");
                out.println("<head>");
                out.println("<style>");
                out.println("body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }");
                out.println("h2 { color: #333; }");
                out.println("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
                out.println("table, th, td { border: 1px solid #ddd; }");
                out.println("th, td { padding: 12px; text-align: left; }");
                out.println("th { background-color: #f2f2f2; color: #333; }");
                out.println("tr:nth-child(even) { background-color: #f9f9f9; }");
                out.println("tr:hover { background-color: #f1f1f1; }");
                out.println("a { color: #4CAF50; text-decoration: none; }");
                out.println("a:hover { text-decoration: underline; }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h2>Students List</h2>");
                out.println("<table>");
                out.println("<tr><th>ID</th><th>Name</th><th>Email</th><th>Age</th><th>Actions</th></tr>");
                for (Student student : students) {
                    out.println("<tr>");
                    out.println("<td>" + student.getId() + "</td>");
                    out.println("<td>" + student.getName() + "</td>");
                    out.println("<td>" + student.getEmail() + "</td>");
                    out.println("<td>" + student.getAge() + "</td>");
                    out.println("<td><a href='student?action=edit&id=" + student.getId() + "'>Edit</a> | <a href='student?action=delete&id=" + student.getId() + "'>Delete</a></td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                out.println("</body>");
                out.println("</html>");
            } 
            else if ("edit".equalsIgnoreCase(action)) 
            {
                // Edit form for updating a student
                int id = Integer.parseInt(request.getParameter("id"));
                Student student = getStudentById(id);
                if (student != null) {
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<style>");
                    out.println("body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }");
                    out.println("h2 { color: #333; }");
                    out.println("form { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); max-width: 400px; margin: auto; }");
                    out.println("label { display: block; margin-bottom: 8px; color: #555; }");
                    out.println("input[type='text'], input[type='email'], input[type='number'] { width: calc(100% - 22px); padding: 10px; margin-bottom: 10px; border: 1px solid #ddd; border-radius: 4px; }");
                    out.println("input[type='submit'] { background-color: #4CAF50; color: white; border: none; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block; font-size: 16px; margin-top: 10px; border-radius: 4px; cursor: pointer; }");
                    out.println("input[type='submit']:hover { background-color: #45a049; }");
                    out.println("</style>");
                    out.println("</head>");
                    out.println("<body>");
                    out.println("<h2>Edit Student</h2>");
                    out.println("<form action='student' method='post'>");
                    out.println("<input type='hidden' name='action' value='update'>");
                    out.println("<input type='hidden' name='id' value='" + student.getId() + "'>");
                    out.println("<label for='name'>Name:</label>");
                    out.println("<input type='text' id='name' name='name' value='" + student.getName() + "'><br>");
                    out.println("<label for='email'>Email:</label>");
                    out.println("<input type='email' id='email' name='email' value='" + student.getEmail() + "'><br>");
                    out.println("<label for='age'>Age:</label>");
                    out.println("<input type='number' id='age' name='age' value='" + student.getAge() + "'><br>");
                    out.println("<input type='submit' value='Update'>");
                    out.println("</form>");
                    out.println("</body>");
                    out.println("</html>");
                }
                else 
                {
                    out.println("Student not found!");
                }
            } 
            else if ("delete".equalsIgnoreCase(action)) 
            {
                // Delete a student
                int id = Integer.parseInt(request.getParameter("id"));
                deleteStudent(id);
                response.sendRedirect("student?action=select");
                return;
            }
        } 
        catch (Exception e) 
        {
            out.println("Error: " + e.getMessage());
        }
    }

    // Handles POST requests for insert, update, and delete operations
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        try
        {
            if ("insert".equalsIgnoreCase(action)) {
                // Insert a new student
                insertStudent(request);
            } else if ("update".equalsIgnoreCase(action)) {
                // Update an existing student
                updateStudent(request);
            } else if ("delete".equalsIgnoreCase(action)) {
                // Delete a student
                int id = Integer.parseInt(request.getParameter("id"));
                deleteStudent(id);
            }
        } catch (Exception e) {
            response.getWriter().println("Error: " + e.getMessage());
        }
        response.sendRedirect("student?action=select"); // Refresh the list after the operation
    }

    // Insert a new student into the database
    private void insertStudent(HttpServletRequest request) throws SQLException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        int age = Integer.parseInt(request.getParameter("age"));

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement("INSERT INTO students (name, email, age) VALUES (?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, age);
            ps.executeUpdate();
        }
    }

    // Update an existing student in the database
    private void updateStudent(HttpServletRequest request) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        int age = Integer.parseInt(request.getParameter("age"));

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement("UPDATE students SET name = ?, email = ?, age = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, age);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    // Delete a student by ID
    private void deleteStudent(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Retrieve all students from the database
    private List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {

            while (rs.next()) {
                students.add(new Student(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getInt("age")));
            }
        }
        return students;
    }

    // Retrieve a student by ID
    private Student getStudentById(int id) throws SQLException {
        Student student = null;
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    student = new Student(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getInt("age"));
                }
            }
        }
        return student;
    }

    // Simple Student class
    static class Student {
        private int id;
        private String name;
        private String email;
        private int age;

        // Constructor
        public Student(int id, String name, String email, int age) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.age = age;
        }

        // Getters and setters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getAge() { return age; }
    }
}
