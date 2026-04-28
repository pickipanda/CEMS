package data;

import models.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore {

    // Singleton
    private static DataStore instance;
    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private List<Admin>    admins    = new ArrayList<>();
    private List<Student>  students  = new ArrayList<>();
    private List<Club>     clubs     = new ArrayList<>();
    private List<Event>    events    = new ArrayList<>();
    private List<Feedback> feedbacks = new ArrayList<>();

    private int clubCounter    = 1;
    private int eventCounter   = 1;
    private int studentCounter = 1;

    private DataStore() {
        // Seed data
        admins.add(new Admin("admin", "admin123"));
        students.add(new Student("S001", "Alice", "alice123"));
        students.add(new Student("S002", "Bob",   "bob123"));
        Club c = new Club("C001", "Tech Club");
        clubs.add(c);
        events.add(new Event("E001", "Hackathon 2025", "C001", "2025-05-10"));
    }

    // ── Admin ─────────────────────────────────────────────
    public Admin findAdmin(String username, String password) {
        return admins.stream()
            .filter(a -> a.getUsername().equals(username) && a.getPassword().equals(password))
            .findFirst().orElse(null);
    }

    // ── Student ───────────────────────────────────────────
    public Student findStudent(String id, String password) {
        return students.stream()
            .filter(s -> s.getStudentId().equals(id) && s.getPassword().equals(password))
            .findFirst().orElse(null);
    }

    public void addStudent(String name, String password) {
        String id = String.format("S%03d", ++studentCounter + 2);
        students.add(new Student(id, name, password));
    }

    public List<Student> getStudents() { return students; }

    // ── Club ──────────────────────────────────────────────
    public void addClub(String name) {
        String id = String.format("C%03d", ++clubCounter);
        clubs.add(new Club(id, name));
    }

    public List<Club> getClubs() { return clubs; }

    public Club findClub(String clubId) {
        return clubs.stream().filter(c -> c.getClubId().equals(clubId)).findFirst().orElse(null);
    }

    // ── Event ─────────────────────────────────────────────
    public void addEvent(String name, String clubId, String date) {
        String id = String.format("E%03d", ++eventCounter);
        events.add(new Event(id, name, clubId, date));
    }

    public List<Event> getEvents() { return events; }

    public Event findEvent(String eventId) {
        return events.stream().filter(e -> e.getEventId().equals(eventId)).findFirst().orElse(null);
    }

    // ── Feedback ──────────────────────────────────────────
    public void addFeedback(Feedback fb) { feedbacks.add(fb); }

    public List<Feedback> getFeedbacksForEvent(String eventId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback fb : feedbacks)
            if (fb.getEventId().equals(eventId)) result.add(fb);
        return result;
    }

    public boolean hasFeedback(String studentId, String eventId) {
        return feedbacks.stream()
            .anyMatch(f -> f.getStudentId().equals(studentId) && f.getEventId().equals(eventId));
    }
}
