package androidCourse.technion.quickthumbs.personalArea.FriendsList;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentSnapshot;

import java.sql.Timestamp;

public class FriendItem {
    public FriendItem(String id, String name, String email, Bitmap profilePicture, Long textAdded, Long totalScore, double avgAccuracy, double avgCPM, double avgWPM, Timestamp lastUpdated, Long numOfGames) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
        this.textAdded = textAdded;
        this.totalScore = totalScore;
        this.avgAccuracy = avgAccuracy;
        this.avgCPM = avgCPM;
        this.avgWPM = avgWPM;
        this.lastUpdated = lastUpdated;
        this.numOfGames = numOfGames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Long getTextAdded() {
        return textAdded;
    }

    public void setTextAdded(Long textAdded) {
        this.textAdded = textAdded;
    }

    public Long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Long totalScore) {
        this.totalScore = totalScore;
    }

    public double getAvgAccuracy() {
        return avgAccuracy;
    }

    public void setAvgAccuracy(double avgAccuracy) {
        this.avgAccuracy = avgAccuracy;
    }

    public double getAvgCPM() {
        return avgCPM;
    }

    public void setAvgCPM(double avgCPM) {
        this.avgCPM = avgCPM;
    }

    public double getAvgWPM() {
        return avgWPM;
    }

    public void setAvgWPM(double avgWPM) {
        this.avgWPM = avgWPM;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getNumOfGames() {
        return numOfGames;
    }

    public void setNumOfGames(Long numOfGames) {
        this.numOfGames = numOfGames;
    }

    public FriendItem(DocumentSnapshot document) {
        this.id = document.getId();
        this.name =document.get("name").toString();
        this.email = document.get("email").toString();
//        this.profilePicture = document.get("profilePicture").toString();
        this.profilePicture = null;
        this.textAdded = document.getLong("textAdded").longValue();
        this.totalScore = document.getLong("totalScore").longValue();
        this.avgAccuracy = document.getDouble("avgAccuracy").doubleValue();
        this.avgCPM = document.getDouble("avgCPM").doubleValue();
        this.avgWPM = document.getDouble("avgWPM").doubleValue();
//        this.lastUpdated = new java.sql.Timestamp(document.getTimestamp("lastUpdated"));
        this.numOfGames = document.getLong("numOfGames").longValue();
    }

    private String id;
    private String name;
    private String email;
    private Bitmap profilePicture;
    private Long textAdded;
    private Long totalScore;
    private double avgAccuracy;
    private double avgCPM;
    private double avgWPM;
    private Timestamp lastUpdated;
    private Long numOfGames;




}
