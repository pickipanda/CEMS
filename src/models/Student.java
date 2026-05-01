package models;

public class Student {
    private String studentId;
    private String name;
    private String password;
    private String phone;   // WhatsApp-compatible number e.g. 919876543210
    private String email;

    public Student(String studentId, String name, String password) {
        this(studentId, name, password, "", "");
    }

    public Student(String studentId, String name, String password,
                   String phone, String email) {
        this.studentId = studentId;
        this.name      = name;
        this.password  = password;
        this.phone     = phone  != null ? phone  : "";
        this.email     = email  != null ? email  : "";
    }

    public String getStudentId() { return studentId; }
    public String getName()      { return name; }
    public String getPassword()  { return password; }
    public String getPhone()     { return phone; }
    public String getEmail()     { return email; }
}
