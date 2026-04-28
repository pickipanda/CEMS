package data;

import models.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * DataStore – in-memory store with optional Firebase sync.
 * Extended with: event edit/delete, comment support, save-event, student management.
 */
public class DataStore {

    private static final String FIREBASE_URL =
        "https://cems-c3dfd-default-rtdb.asia-southeast1.firebasedatabase.app/";

    // ── Credentials (runtime-mutable for reset-password) ──────────────────
    private static final List<String[]> ADMIN_CREDS_LIST = new ArrayList<>();

static {
    ADMIN_CREDS_LIST.add(new String[]{"admin", "123"});
    ADMIN_CREDS_LIST.add(new String[]{"rish", "123"});

    
}
    
    
    private static final List<String[]> STUDENT_CREDS_LIST = new ArrayList<>(Arrays.asList(
        new String[]{ "S1", "123", "Rishabh" },
        new String[]{ "S2", "123",   "Asmit"   },
        new String[]{ "S3", "123", "Adi" },
        new String[]{ "S4", "123",   "Kush"   },
        new String[]{ "S5", "123", "Shreya" },
        new String[]{ "S6", "123",   "Aryan"   }
    ));

    // ── Singleton ──────────────────────────────────────────────────────────
    private static DataStore instance;
    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    // ── In-memory cache ────────────────────────────────────────────────────
    private final List<Club>     clubs     = new ArrayList<>();
    private final List<Event>    events    = new ArrayList<>();
    private final List<Feedback> feedbacks = new ArrayList<>();

    private final boolean firebaseEnabled;

    private DataStore() {
        firebaseEnabled = !FIREBASE_URL.contains("YOUR-PROJECT");
        if (firebaseEnabled) {
            loadAllFromFirebase();
        } else {
            System.out.println("[DataStore] Firebase not configured – running locally only.");
            clubs.add(new Club("C001", "Tech Club"));
            clubs.add(new Club("C002", "Arts Club"));
            events.add(new Event("E001", "Hackathon 2025", "C001", "2025-05-10",
                                 "Annual coding competition open to all students.", 50, false));
            events.add(new Event("E002", "Sketch Workshop", "C002", "2025-06-15",
                                 "Learn pencil sketching from professionals.", 30, false));
        }
    }

    /** Re-load everything from Firebase (used by the refresh button). */
    public void reload() {
        clubs.clear();
        events.clear();
        feedbacks.clear();
        instance = null;
        instance = new DataStore();
    }

    // ── Firebase load ──────────────────────────────────────────────────────
    private void loadAllFromFirebase() {
        String clubsJson = fbGet("clubs.json");
        if (clubsJson != null && !clubsJson.equals("null")) {
            for (String obj : jsonObjects(clubsJson)) {
                String cid  = jsonString(obj, "clubId");
                String name = jsonString(obj, "clubName");
                if (cid != null) clubs.add(new Club(cid, name));
            }
            clubs.sort(Comparator.comparing(Club::getClubId));
        }
        if (clubs.isEmpty()) clubs.add(new Club("C001", "Tech Club"));

        String eventsJson = fbGet("events.json");
        if (eventsJson != null && !eventsJson.equals("null")) {
            for (String obj : jsonObjects(eventsJson)) {
                Event ev = parseEvent(obj);
                if (ev != null) events.add(ev);
            }
            events.sort(Comparator.comparing(Event::getEventId));
        }

        for (Event ev : events) {
            String evRegJson = fbGet("registrations/" + ev.getEventId() + ".json");
            if (evRegJson != null && !evRegJson.equals("null"))
                for (String sid : jsonKeys(evRegJson))
                    ev.getRegisteredStudents().add(sid);

            String attJson = fbGet("attendance/" + ev.getEventId() + ".json");
            if (attJson != null && !attJson.equals("null"))
                for (String sid : jsonKeys(attJson))
                    ev.getAttendedStudents().add(sid);

            String fbJson = fbGet("feedbacks/" + ev.getEventId() + ".json");
            if (fbJson != null && !fbJson.equals("null"))
                for (String obj : jsonObjects(fbJson)) {
                    String sid     = jsonString(obj, "studentId");
                    String eid     = jsonString(obj, "eventId");
                    String rStr    = jsonRaw(obj, "rating");
                    String comment = jsonString(obj, "comment");
                    if (sid != null)
                        feedbacks.add(new Feedback(sid, eid,
                            rStr != null ? safeInt(rStr) : 0,
                            comment != null ? comment : ""));
                }

            String cmtJson = fbGet("comments/" + ev.getEventId() + ".json");
            if (cmtJson != null && !cmtJson.equals("null"))
                for (String obj : jsonObjects(cmtJson)) {
                    String sid = jsonString(obj, "studentId");
                    String txt = jsonString(obj, "text");
                    if (sid != null) ev.getComments().add(sid + "|||" + orEmpty(txt));
                }

            String savedJson = fbGet("saved/" + ev.getEventId() + ".json");
            if (savedJson != null && !savedJson.equals("null"))
                for (String sid : jsonKeys(savedJson))
                    ev.getSavedByStudents().add(sid);
        }

        String studJson = fbGet("students.json");
        if (studJson != null && !studJson.equals("null")) {
            for (String obj : jsonObjects(studJson)) {
                String sid  = jsonString(obj, "studentId");
                String name = jsonString(obj, "name");
                String pass = jsonString(obj, "password");
                if (sid != null && !hasStudentCred(sid))
                    STUDENT_CREDS_LIST.add(new String[]{sid, orEmpty(pass), orEmpty(name)});
            }
        }
    }

    private boolean hasStudentCred(String id) {
        return STUDENT_CREDS_LIST.stream().anyMatch(r -> r[0].equals(id));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN
    // ══════════════════════════════════════════════════════════════════════
    public Admin findAdmin(String username, String password) {
        for (String[] row : ADMIN_CREDS_LIST)
            if (row[0].equals(username) && row[1].equals(password))
                return new Admin(row[0], row[1]);
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STUDENT
    // ══════════════════════════════════════════════════════════════════════
    public Student findStudent(String id, String password) {
        for (String[] row : STUDENT_CREDS_LIST)
            if (row[0].equals(id) && row[1].equals(password))
                return new Student(row[0], row[2], row[1]);
        return null;
    }

    public List<Student> getStudents() {
        List<Student> list = new ArrayList<>();
        for (String[] row : STUDENT_CREDS_LIST)
            list.add(new Student(row[0], row[2], row[1]));
        list.sort(Comparator.comparing(Student::getStudentId));
        return list;
    }

    public void addStudent(String name, String password) {
        String id = "S" + String.format("%03d", STUDENT_CREDS_LIST.size() + 1);
        STUDENT_CREDS_LIST.add(new String[]{id, password, name});
        String json = String.format(
            "{\"studentId\":\"%s\",\"name\":\"%s\",\"password\":\"%s\"}",
            id, escJson(name), escJson(password));
        fbPutAsync("students/" + id + ".json", json);
    }

    public boolean updateStudent(String studentId, String newName, String newPassword) {
        for (String[] row : STUDENT_CREDS_LIST) {
            if (row[0].equals(studentId)) {
                row[2] = newName;
                row[1] = newPassword;
                String json = String.format(
                    "{\"studentId\":\"%s\",\"name\":\"%s\",\"password\":\"%s\"}",
                    studentId, escJson(newName), escJson(newPassword));
                fbPutAsync("students/" + studentId + ".json", json);
                return true;
            }
        }
        return false;
    }

    public boolean deleteStudent(String studentId) {
        boolean removed = STUDENT_CREDS_LIST.removeIf(r -> r[0].equals(studentId));
        if (removed) fbDeleteAsync("students/" + studentId + ".json");
        return removed;
    }

    public boolean resetPassword(String studentId, String newPassword) {
        return updateStudent(studentId,
            getStudents().stream().filter(s->s.getStudentId().equals(studentId))
                .map(Student::getName).findFirst().orElse(""), newPassword);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CLUB
    // ══════════════════════════════════════════════════════════════════════
    public void addClub(String name) {
        String id = nextLocalId(clubs, "C");
        clubs.add(new Club(id, name));
        clubs.sort(Comparator.comparing(Club::getClubId));
        fbPutAsync("clubs/" + id + ".json",
            String.format("{\"clubId\":\"%s\",\"clubName\":\"%s\"}", id, escJson(name)));
    }

    public List<Club> getClubs() { return new ArrayList<>(clubs); }

    public Club findClub(String clubId) {
        return clubs.stream().filter(c->c.getClubId().equals(clubId)).findFirst().orElse(null);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EVENT
    // ══════════════════════════════════════════════════════════════════════
    public void addEvent(String name, String clubId, String date, String description) {
        addEvent(name, clubId, date, description, 0);
    }

    public void addEvent(String name, String clubId, String date, String description, int maxP) {
        String id = nextLocalId(events, "E");
        events.add(new Event(id, name, clubId, date, description, maxP, false));
        events.sort(Comparator.comparing(Event::getEventId));
        pushEvent(findEvent(id));
    }

    public void addEvent(String name, String clubId, String date) {
        addEvent(name, clubId, date, "");
    }

    public boolean updateEvent(String eventId, String name, String clubId,
                                String date, String description, int maxP) {
        Event ev = findEvent(eventId);
        if (ev == null) return false;
        ev.setEventName(name);
        ev.setClubId(clubId);
        ev.setDate(date);
        ev.setDescription(description);
        ev.setMaxParticipants(maxP);
        pushEvent(ev);
        return true;
    }

    public boolean deleteEvent(String eventId) {
        boolean removed = events.removeIf(e -> e.getEventId().equals(eventId));
        if (removed) fbDeleteAsync("events/" + eventId + ".json");
        return removed;
    }

    public boolean closeRegistration(String eventId) {
        Event ev = findEvent(eventId);
        if (ev == null) return false;
        ev.setRegistrationClosed(true);
        pushEvent(ev);
        return true;
    }

    public boolean openRegistration(String eventId) {
        Event ev = findEvent(eventId);
        if (ev == null) return false;
        ev.setRegistrationClosed(false);
        pushEvent(ev);
        return true;
    }

    private void pushEvent(Event ev) {
        if (ev == null) return;
        String json = String.format(
            "{\"eventId\":\"%s\",\"eventName\":\"%s\",\"clubId\":\"%s\"," +
            "\"date\":\"%s\",\"description\":\"%s\",\"maxParticipants\":%d,\"registrationClosed\":%b}",
            ev.getEventId(), escJson(ev.getEventName()), escJson(ev.getClubId()),
            escJson(ev.getDate()), escJson(ev.getDescription()),
            ev.getMaxParticipants(), ev.isRegistrationClosed());
        fbPutAsync("events/" + ev.getEventId() + ".json", json);
    }

    public List<Event> getEvents() { return new ArrayList<>(events); }

    public Event findEvent(String eventId) {
        return events.stream().filter(e->e.getEventId().equals(eventId)).findFirst().orElse(null);
    }

    private Event parseEvent(String json) {
        String eid = jsonString(json, "eventId");
        if (eid == null) return null;
        String maxStr = jsonRaw(json, "maxParticipants");
        String closedStr = jsonRaw(json, "registrationClosed");
        return new Event(
            eid,
            jsonString(json, "eventName"),
            jsonString(json, "clubId"),
            jsonString(json, "date"),
            orEmpty(jsonString(json, "description")),
            maxStr != null ? safeInt(maxStr) : 0,
            "true".equals(closedStr));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REGISTRATION
    // ══════════════════════════════════════════════════════════════════════
    public boolean registerStudent(String eventId, String studentId) {
        Event ev = findEvent(eventId);
        if (ev == null) return false;
        if (ev.getRegisteredStudents().contains(studentId)) return false;
        if (!ev.canRegister()) return false;
        ev.getRegisteredStudents().add(studentId);
        fbPutAsync("registrations/" + eventId + "/" + studentId + ".json", "true");
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ATTENDANCE
    // ══════════════════════════════════════════════════════════════════════
    public boolean markAttendance(String eventId, String studentId) {
        Event ev = findEvent(eventId);
        if (ev == null) return false;
        if (!ev.getRegisteredStudents().contains(studentId)) return false;
        if (ev.getAttendedStudents().contains(studentId))    return false;
        ev.getAttendedStudents().add(studentId);
        fbPutAsync("attendance/" + eventId + "/" + studentId + ".json", "true");
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FEEDBACK
    // ══════════════════════════════════════════════════════════════════════
    public void addFeedback(Feedback fb) {
        feedbacks.add(fb);
        String json = String.format(
            "{\"studentId\":\"%s\",\"eventId\":\"%s\",\"rating\":%d,\"comment\":\"%s\"}",
            escJson(fb.getStudentId()), escJson(fb.getEventId()),
            fb.getRating(), escJson(fb.getComment()));
        fbPutAsync("feedbacks/" + fb.getEventId() + "/" + fb.getStudentId() + ".json", json);
    }

    public List<Feedback> getFeedbacksForEvent(String eventId) {
        List<Feedback> list = new ArrayList<>();
        for (Feedback fb : feedbacks)
            if (fb.getEventId().equals(eventId)) list.add(fb);
        return list;
    }

    public boolean hasFeedback(String studentId, String eventId) {
        return feedbacks.stream().anyMatch(
            f -> f.getStudentId().equals(studentId) && f.getEventId().equals(eventId));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  COMMENTS
    // ══════════════════════════════════════════════════════════════════════
    public void addComment(String eventId, String studentId, String text) {
        Event ev = findEvent(eventId);
        if (ev == null) return;
        ev.getComments().add(studentId + "|||" + text);
        String json = String.format(
            "{\"studentId\":\"%s\",\"text\":\"%s\"}",
            escJson(studentId), escJson(text));
        fbPutAsync("comments/" + eventId + "/" + studentId + "_" + System.currentTimeMillis() + ".json", json);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SAVE / FAVOURITES
    // ══════════════════════════════════════════════════════════════════════
    public boolean saveEvent(String eventId, String studentId) {
        Event ev = findEvent(eventId);
        if (ev == null) return false;
        if (ev.getSavedByStudents().contains(studentId)) return false;
        ev.getSavedByStudents().add(studentId);
        fbPutAsync("saved/" + eventId + "/" + studentId + ".json", "true");
        return true;
    }

    public boolean unsaveEvent(String eventId, String studentId) {
        Event ev = findEvent(eventId);
        if (ev == null) return false;
        boolean removed = ev.getSavedByStudents().remove(studentId);
        if (removed) fbDeleteAsync("saved/" + eventId + "/" + studentId + ".json");
        return removed;
    }

    public boolean isEventSaved(String eventId, String studentId) {
        Event ev = findEvent(eventId);
        return ev != null && ev.getSavedByStudents().contains(studentId);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ANALYTICS HELPERS
    // ══════════════════════════════════════════════════════════════════════
    public int getTotalStudents()  { return STUDENT_CREDS_LIST.size(); }
    public int getTotalEvents()    { return events.size(); }

    public double getOverallAttendanceRate() {
        int reg = 0, att = 0;
        for (Event ev : events) {
            reg += ev.getRegisteredStudents().size();
            att += ev.getAttendedStudents().size();
        }
        return reg == 0 ? 0.0 : (att * 100.0 / reg);
    }

    public Event getMostPopularEvent() {
        return events.stream()
            .max(Comparator.comparingInt(e -> e.getRegisteredStudents().size()))
            .orElse(null);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ID GENERATION
    // ══════════════════════════════════════════════════════════════════════
    private <T> String nextLocalId(List<T> list, String prefix) {
        int max = 0;
        for (T item : list) {
            String id = null;
            if (item instanceof Club)  id = ((Club)  item).getClubId();
            if (item instanceof Event) id = ((Event) item).getEventId();
            if (id != null && id.startsWith(prefix)) {
                try { max = Math.max(max, Integer.parseInt(id.substring(1))); }
                catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HTTP – Firebase REST API
    // ══════════════════════════════════════════════════════════════════════
    private String fbGet(String path) {
        if (!firebaseEnabled) return null;
        try {
            HttpURLConnection c = openConn(FIREBASE_URL + path, "GET");
            return readResponse(c);
        } catch (Exception e) {
            System.err.println("[Firebase GET ] " + path + " → " + e.getMessage());
            return null;
        }
    }

    private void fbPutAsync(String path, String body) {
        if (!firebaseEnabled) return;
        Thread t = new Thread(() -> {
            try {
                HttpURLConnection c = openConn(FIREBASE_URL + path, "PUT");
                c.setRequestProperty("Content-Type", "application/json");
                c.setDoOutput(true);
                try (OutputStream os = c.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
                readResponse(c);
            } catch (Exception e) {
                System.err.println("[Firebase PUT ] " + path + " → " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void fbDeleteAsync(String path) {
        if (!firebaseEnabled) return;
        Thread t = new Thread(() -> {
            try {
                HttpURLConnection c = openConn(FIREBASE_URL + path, "DELETE");
                readResponse(c);
            } catch (Exception e) {
                System.err.println("[Firebase DEL ] " + path + " → " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private HttpURLConnection openConn(String url, String method) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        return c;
    }

    private String readResponse(HttpURLConnection c) throws IOException {
        int code = c.getResponseCode();
        InputStream is = (code >= 400) ? c.getErrorStream() : c.getInputStream();
        if (is == null) return null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString().trim();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MINIMAL JSON PARSER
    // ══════════════════════════════════════════════════════════════════════
    static String jsonString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int ki = json.indexOf(pattern);
        if (ki < 0) return null;
        int colon = json.indexOf(':', ki + pattern.length());
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = start + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '\\' && i + 1 < json.length()) {
                char nx = json.charAt(++i);
                switch (nx) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    default:   sb.append(nx);
                }
            } else if (ch == '"') {
                break;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    static String jsonRaw(String json, String key) {
        String pattern = "\"" + key + "\"";
        int ki = json.indexOf(pattern);
        if (ki < 0) return null;
        int colon = json.indexOf(':', ki + pattern.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length() || json.charAt(start) == '"') return jsonString(json, key);
        int end = start;
        while (end < json.length() && ",}]".indexOf(json.charAt(end)) < 0) end++;
        return json.substring(start, end).trim();
    }

    static List<String> jsonObjects(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.equals("null")) return result;
        json = json.trim();
        if (!json.startsWith("{")) return result;
        int i = 1;
        while (i < json.length()) {
            int ks = json.indexOf('"', i);      if (ks < 0) break;
            int ke = json.indexOf('"', ks + 1); if (ke < 0) break;
            int cl = json.indexOf(':', ke + 1); if (cl < 0) break;
            int vs = cl + 1;
            while (vs < json.length() && json.charAt(vs) == ' ') vs++;
            if (vs >= json.length()) break;
            char first = json.charAt(vs);
            if (first == '{') {
                int end = matchingBrace(json, vs);
                result.add(json.substring(vs, end + 1));
                i = end + 1;
            } else if (first == '"') {
                int end = vs + 1;
                while (end < json.length()) {
                    if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                    end++;
                }
                i = end + 1;
            } else {
                int end = vs;
                while (end < json.length() && ",}".indexOf(json.charAt(end)) < 0) end++;
                i = end + 1;
            }
        }
        return result;
    }

    static List<String> jsonKeys(String json) {
        List<String> keys = new ArrayList<>();
        if (json == null || json.equals("null")) return keys;
        json = json.trim();
        if (!json.startsWith("{")) return keys;
        int i = 1;
        while (i < json.length()) {
            int ks = json.indexOf('"', i);      if (ks < 0) break;
            int ke = json.indexOf('"', ks + 1); if (ke < 0) break;
            keys.add(json.substring(ks + 1, ke));
            int cl = json.indexOf(':', ke + 1); if (cl < 0) break;
            int vs = cl + 1;
            while (vs < json.length() && json.charAt(vs) == ' ') vs++;
            if (vs >= json.length()) break;
            char first = json.charAt(vs);
            if (first == '{') {
                int end = matchingBrace(json, vs);
                i = end + 1;
            } else if (first == '"') {
                int end = vs + 1;
                while (end < json.length()) {
                    if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                    end++;
                }
                i = end + 1;
            } else {
                int end = vs;
                while (end < json.length() && ",}".indexOf(json.charAt(end)) < 0) end++;
                i = end + 1;
            }
        }
        return keys;
    }

    private static int matchingBrace(String s, int start) {
        int depth = 0; boolean inStr = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                if (c == '\\') { i++; continue; }
                if (c == '"')  inStr = false;
            } else {
                if (c == '"')  inStr = true;
                else if (c == '{') depth++;
                else if (c == '}') { if (--depth == 0) return i; }
            }
        }
        return s.length() - 1;
    }

    private static String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private int safeInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
    private String orEmpty(String s) { return s != null ? s : ""; }
}
