import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Task A3 & Task 04: Generic Data-Access Object (DAO) Pattern Demo
 * Demonstrates clean separation of infrastructure from domain entities,
 * object-relational mapping, and resilience to schema changes.
 */
public class GenericDaoDemo {

    public interface Dao<T, K> {
        void insert(T entity) throws SQLException;
        Optional<T> findById(K id) throws SQLException;
        List<T> findAll() throws SQLException;
        void update(T entity) throws SQLException;
        void delete(K id) throws SQLException;
    }

    public static class Student {
        private String regNo;
        private String name;
        private String email;
        private String department;
        private double cgpa;

        public Student(String regNo, String name, String email, String department, double cgpa) {
            this.regNo = regNo;
            this.name = name;
            this.email = email;
            this.department = department;
            this.cgpa = cgpa;
        }

        public String getRegNo() { return regNo; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getDepartment() { return department; }
        public double getCgpa() { return cgpa; }

        @Override
        public String toString() {
            return String.format("Student[regNo=%s, name=%s, email=%s, dept=%s, cgpa=%.2f]",
                    regNo, name, email, department, cgpa);
        }
    }

    public static class StudentDao implements Dao<Student, String> {
        private final Map<String, Student> simulatedDb = new HashMap<>();

        // Object-to-Table Column Mapping:
        // regNo      <-> reg_no      VARCHAR(20) PRIMARY KEY
        // name       <-> name        VARCHAR(100) NOT NULL
        // email      <-> email       VARCHAR(100) UNIQUE
        // department <-> department  VARCHAR(50) NOT NULL
        // cgpa       <-> cgpa        DECIMAL(3,2) CHECK (cgpa >= 0.0 AND cgpa <= 10.0)

        @Override
        public void insert(Student s) throws SQLException {
            simulatedDb.put(s.getRegNo(), s);
        }

        @Override
        public Optional<Student> findById(String id) throws SQLException {
            return Optional.ofNullable(simulatedDb.get(id));
        }

        @Override
        public List<Student> findAll() throws SQLException {
            return new ArrayList<>(simulatedDb.values());
        }

        @Override
        public void update(Student s) throws SQLException {
            simulatedDb.put(s.getRegNo(), s);
        }

        @Override
        public void delete(String id) throws SQLException {
            simulatedDb.remove(id);
        }
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("==========================================================================");
        System.out.println("  TASK A3: GENERIC DAO PATTERN AND OBJECT-TABLE MAPPING");
        System.out.println("==========================================================================");

        StudentDao dao = new StudentDao();
        dao.insert(new Student("192311001", "Arun Kumar", "arun@simats.edu", "CSE", 8.85));
        dao.insert(new Student("192311002", "Bala Chandran", "bala@simats.edu", "ECE", 9.10));
        dao.insert(new Student("192311003", "Divya Murugan", "divya@simats.edu", "CSE", 9.45));

        System.out.println("\n[Find Student by ID '192311003']");
        Optional<Student> student = dao.findById("192311003");
        student.ifPresent(s -> System.out.println("Retrieved: " + s));

        System.out.println("\n[Find All Students]");
        for (Student s : dao.findAll()) {
            System.out.println(" - " + s);
        }

        System.out.println("\n--- Alternative Analysis: Direct SQL vs Generic DAO ---");
        System.out.println("1. Schema Change Impact: If 'reg_no' is renamed to 'student_id':");
        System.out.println("   - Direct SQL in UI/Service: 20-40 scattered SQL queries must be manually modified.");
        System.out.println("   - Generic DAO: Exactly ONE file (StudentDao.java) requires modification.");
        System.out.println("2. Runtime Overhead: 1 virtual method call invocation (~2ns), negligible compared to 2ms DB round-trip.");
        System.out.println("==========================================================================");
    }
}
