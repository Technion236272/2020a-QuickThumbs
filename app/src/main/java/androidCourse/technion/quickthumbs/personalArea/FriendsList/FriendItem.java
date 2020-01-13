package androidCourse.technion.quickthumbs.personalArea.FriendsList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Timestamp;

public class FriendItem {
    public FriendItem(String id, String name, String email, Bitmap profilePicture, Long textAdded, Long totalScore, double avgAccuracy, double avgCPM, double avgWPM, Timestamp lastUpdated, Long numOfGames) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
        this.totalScore = totalScore;
//        this.textAdded = textAdded;
//        this.avgAccuracy = avgAccuracy;
//        this.avgCPM = avgCPM;
//        this.avgWPM = avgWPM;
//        this.lastUpdated = lastUpdated;
//        this.numOfGames = numOfGames;
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

    public void setProfilePicture(String uid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("users");
        StorageReference userStorage = storageRef.child(uid);
        StorageReference profilePictureRef = userStorage.child("/profilePicture.JPEG");

        final long ONE_MEGABYTE = 1024 * 1024;
        profilePictureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                profilePicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                showMessage("picture was loaded from storage");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                profilePicture = null;
            }
        });
    }

    public Long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Long totalScore) {
        this.totalScore = totalScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FriendItem FriendItemFromUserCollection(DocumentSnapshot document, String status) {
        FriendItem friendItem = new FriendItem(document, status);
        friendItem.name = document.get("name").toString();
        friendItem.email = document.get("email").toString();
        setProfilePicture(this.id);
//        friendItem.profilePicture = null;
        friendItem.totalScore = document.getLong("totalScore").longValue();
//        friendItem.textAdded = document.getLong("textAdded").longValue();
//        friendItem.avgAccuracy = document.getDouble("avgAccuracy").doubleValue();
//        friendItem.avgCPM = document.getDouble("avgCPM").doubleValue();
//        friendItem.avgWPM = document.getDouble("avgWPM").doubleValue();
//        friendItem.lastUpdated = new java.sql.Timestamp(document.getTimestamp("lastUpdated"));
//        friendItem.numOfGames = document.getLong("numOfGames").longValue();
        return friendItem;
    }

//    public Long getTextAdded() {
//        return textAdded;
//    }
//
//    public void setTextAdded(Long textAdded) {
//        this.textAdded = textAdded;
//    }
//    public double getAvgAccuracy() {
//        return avgAccuracy;
//    }
//
//    public void setAvgAccuracy(double avgAccuracy) {
//        this.avgAccuracy = avgAccuracy;
//    }
//
//    public double getAvgCPM() {
//        return avgCPM;
//    }
//
//    public void setAvgCPM(double avgCPM) {
//        this.avgCPM = avgCPM;
//    }
//
//    public double getAvgWPM() {
//        return avgWPM;
//    }
//
//    public void setAvgWPM(double avgWPM) {
//        this.avgWPM = avgWPM;
//    }
//
//    public Timestamp getLastUpdated() {
//        return lastUpdated;
//    }
//
//    public void setLastUpdated(Timestamp lastUpdated) {
//        this.lastUpdated = lastUpdated;
//    }
//
//    public Long getNumOfGames() {
//        return numOfGames;
//    }
//
//    public void setNumOfGames(Long numOfGames) {
//        this.numOfGames = numOfGames;
//    }

    private String id;
    private String name;
    private String email;
    private Bitmap profilePicture;
    private Long totalScore;
    private String status;
//    private Long textAdded;
//    private double avgAccuracy;
//    private double avgCPM;
//    private double avgWPM;
//    private Timestamp lastUpdated;
//    private Long numOfGames;

    public FriendItem(DocumentSnapshot document, String status) {
        this.id = document.getId();
        this.status = status;
        this.name = document.get("name").toString();
        this.email = document.get("email").toString();
        setProfilePicture(this.id);
//        this.profilePicture = null;
//        this.totalScore = document.getLong("totalScore").longValue();
        this.totalScore = Long.valueOf(0);
    }


}
