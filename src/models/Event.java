package models;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String eventId;
    private String eventName;
    private String clubId;
    private String date;

    private List<String> registeredStudents = new ArrayList<>();
    private List<String> attendedStudents   = new ArrayList<>();

    public Event(String eventId, String eventName, String clubId, String date) {
        this.eventId   = eventId;
        this.eventName = eventName;
        this.clubId    = clubId;
        this.date      = date;
    }

    public String getEventId()   { return eventId; }
    public String getEventName() { return eventName; }
    public String getClubId()    { return clubId; }
    public String getDate()      { return date; }

    public List<String> getRegisteredStudents() { return registeredStudents; }
    public List<String> getAttendedStudents()   { return attendedStudents; }

    public boolean registerStudent(String studentId) {
        if (registeredStudents.contains(studentId)) return false;
        registeredStudents.add(studentId);
        return true;
    }

    public boolean markAttendance(String studentId) {
        if (!registeredStudents.contains(studentId)) return false;
        if (attendedStudents.contains(studentId))    return false;
        attendedStudents.add(studentId);
        return true;
    }

    @Override
    public String toString() { return eventName + " (" + date + ")"; }
}
