package androidCourse.technion.quickthumbs.personalArea.PersonalTexts;

import com.google.firebase.firestore.DocumentSnapshot;

public class TextDataRow {
    private String textId;
    private String title;
    private String themeName;
    private String text;
    private String date;
    private String composer;
    private double rating;
    private String numberOfTimesPlayed;
    private String bestScore;
    private String fastestSpeed;
    public boolean isClicked;
    public boolean isExpanded;
    private String roomKey;
    private Long startingTimeStamp;
    private int indexInRoom;

    public TextDataRow(String textId, String title, String themeName, String text, String date, String composer, double rating, String numberOfTimesPlayed, String bestScore, String fastestSpeed, boolean isClicked, boolean isExpanded, String roomKey, Long startingTimeStamp, int indexInRoom) {
        this.textId = textId;
        this.title = title;
        this.themeName = themeName;
        this.text = text;
        this.date = date;
        this.composer = composer;
        this.rating = rating;
        this.numberOfTimesPlayed = numberOfTimesPlayed;
        this.bestScore = bestScore;
        this.fastestSpeed = fastestSpeed;
        this.isClicked = isClicked;
        this.isExpanded = isExpanded;
        this.roomKey = roomKey;
        this.startingTimeStamp = startingTimeStamp;
        this.indexInRoom = indexInRoom;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getNumberOfTimesPlayed() {
        return numberOfTimesPlayed;
    }

    public void setNumberOfTimesPlayed(String numberOfTimesPlayed) {
        this.numberOfTimesPlayed = numberOfTimesPlayed;
    }

    public String getBestScore() {
        return bestScore;
    }

    public void setBestScore(String bestScore) {
        this.bestScore = bestScore;
    }

    public String getFastestSpeed() {
        return fastestSpeed;
    }

    public void setFastestSpeed(String fastestSpeed) {
        this.fastestSpeed = fastestSpeed;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public void setClicked(boolean clicked) {
        isClicked = clicked;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public Long getStartingTimeStamp() {
        return startingTimeStamp;
    }

    public void setStartingTimeStamp(Long startingTimeStamp) {
        this.startingTimeStamp = startingTimeStamp;
    }

    public int getIndexInRoom() {
        return indexInRoom;
    }

    public void setIndexInRoom(int indexInRoom) {
        this.indexInRoom = indexInRoom;
    }

    public static TextDataRow createTextCardItem(DocumentSnapshot document, String roomKey, int positionInRoom, Long startingTimeStamp) {
        String id = document.getId();
        String title = document.get("title").toString();
        String theme = document.get("theme").toString();
        String text = document.get("text").toString();
        String date = document.get("date").toString();
        String composer = document.get("composer").toString();
        double rating = document.getDouble("rating");
        String numberOfPlays = "0";
        if (document.getLong("playCount") != null) {
            numberOfPlays = document.getLong("playCount").toString();
        }
        String fastestSpeed = "0";
        if (document.getDouble("bestWpm") != null) {
            fastestSpeed = document.getDouble("bestWpm").toString();
        }
        String bestScore = "0";
        if (document.getLong("bestScore") != null) {
            bestScore = document.getLong("bestScore").toString();
        }
        TextDataRow item = new TextDataRow(id, title, theme, text, date, composer, rating,
                numberOfPlays, bestScore, fastestSpeed, false, false, roomKey, startingTimeStamp, positionInRoom);

        return item;
    }

}
