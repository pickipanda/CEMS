package models;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String eventId;
    private String eventName;
    private String clubId;
    private String date;
    private String description;
    private int    maxParticipants;   // 0 = unlimited
    private boolean registrationClosed;

    private List<String> registeredStudents = new ArrayList<>();
    private List<String> attendedStudents   = new ArrayList<>();
    private List<String> savedByStudents    = new ArrayList<>();
    private List<String> comments           = new ArrayList<>();  // "StudentId|||comment"

    public Event(String eventId, String eventName, String clubId, String date) {
        this(eventId, eventName, clubId, date, "", 0, false);
    }

    public Event(String eventId, String eventName, String clubId, String date, String description) {
        this(eventId, eventName, clubId, date, description, 0, false);
    }

    public Event(String eventId, String eventName, String clubId, String date,
                 String description, int maxParticipants, boolean registrationClosed) {
        this.eventId             = eventId;
        this.eventName           = eventName;
        this.clubId              = clubId;
        this.date                = date;
        this.description         = description != null ? description : "";
        this.maxParticipants     = maxParticipants;
        this.registrationClosed  = registrationClosed;
    }

    public String  getEventId()            { return eventId; }
    public String  getEventName()          { return eventName; }
    public String  getClubId()             { return clubId; }
    public String  getDate()               { return date; }
    public String  getDescription()        { return description; }
    public int     getMaxParticipants()    { return maxParticipants; }
    public boolean isRegistrationClosed()  { return registrationClosed; }

    public void setEventName(String n)             { this.eventName = n; }
    public void setClubId(String c)                { this.clubId = c; }
    public void setDate(String d)                  { this.date = d; }
    public void setDescription(String d)           { this.description = d; }
    public void setMaxParticipants(int m)          { this.maxParticipants = m; }
    public void setRegistrationClosed(boolean b)   { this.registrationClosed = b; }

    public List<String> getRegisteredStudents() { return registeredStudents; }
    public List<String> getAttendedStudents()   { return attendedStudents; }
    public List<String> getSavedByStudents()    { return savedByStudents; }
    public List<String> getComments()           { return comments; }

    public boolean isFull() {
        return maxParticipants > 0 && registeredStudents.size() >= maxParticipants;
    }

    public boolean canRegister() {
        return !registrationClosed && !isFull();
    }

    @Override
    public String toString() { return eventName + " (" + date + ")"; }
}
