package models;

public class Feedback {
    private String studentId;
    private String eventId;
    private int    rating;   // 1–5
    private String comment;

    public Feedback(String studentId, String eventId, int rating, String comment) {
        this.studentId = studentId;
        this.eventId   = eventId;
        this.rating    = rating;
        this.comment   = comment;
    }

    public String getStudentId() { return studentId; }
    public String getEventId()   { return eventId; }
    public int    getRating()    { return rating; }
    public String getComment()   { return comment; }
}
