package androidCourse.technion.quickthumbs.multiplayerSearch;

public class GameRoom {
    public String user1;
    public Integer location1;

    public String user2;
    public Integer location2;

    public String textId;

    public boolean started;

    public boolean usr1Online;
    public boolean usr2Online;

    public int usr1Points;
    public int usr2Points;

    public GameRoom() {
    }

    public GameRoom(String user1, Integer location1, String user2, Integer location2, String textId, boolean started, boolean usr1Online, boolean usr2Online, int usr1Points, int usr2Points) {
        this.user1 = user1;
        this.location1 = location1;
        this.user2 = user2;
        this.location2 = location2;
        this.textId = textId;
        this.started = started;
        this.usr1Online = usr1Online;
        this.usr2Online = usr2Online;
        this.usr1Points = usr1Points;
        this.usr2Points = usr2Points;
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

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isUsr1Online() {
        return usr1Online;
    }

    public void setUsr1Online(boolean usr1Online) {
        this.usr1Online = usr1Online;
    }

    public boolean isUsr2Online() {
        return usr2Online;
    }

    public void setUsr2Online(boolean usr2Online) {
        this.usr2Online = usr2Online;
    }

    public int getUsr1Points() {
        return usr1Points;
    }

    public void setUsr1Points(int usr1Points) {
        this.usr1Points = usr1Points;
    }

    public int getUsr2Points() {
        return usr2Points;
    }

    public void setUsr2Points(int usr2Points) {
        this.usr2Points = usr2Points;
    }
}
