package androidCourse.technion.quickthumbs.multiplayerSearch;

public class Room {
    public String user1;
    public Integer location1;

    public String user2;
    public Integer location2;

    public String textId;

    public Room() {
    }

    public Room(String user1, Integer location1, String user2, Integer location2, String textId) {
        this.user1 = user1;
        this.location1 = location1;
        this.user2 = user2;
        this.location2 = location2;
        this.textId = textId;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public Integer getLocation1() {
        return location1;
    }

    public void setLocation1(Integer location1) {
        this.location1 = location1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public Integer getLocation2() {
        return location2;
    }

    public void setLocation2(Integer location2) {
        this.location2 = location2;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }
}
