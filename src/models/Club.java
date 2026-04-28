package models;

public class Club {
    private String clubId;
    private String clubName;

    public Club(String clubId, String clubName) {
        this.clubId   = clubId;
        this.clubName = clubName;
    }

    public String getClubId()   { return clubId; }
    public String getClubName() { return clubName; }

    @Override
    public String toString() { return clubName; }
}
