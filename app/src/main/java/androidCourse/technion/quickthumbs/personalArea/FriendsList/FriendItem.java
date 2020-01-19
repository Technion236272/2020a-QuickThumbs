package androidCourse.technion.quickthumbs.personalArea.FriendsList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Timestamp;

import androidCourse.technion.quickthumbs.R;

import static androidCourse.technion.quickthumbs.FirestoreConstants.statisticsDocument;
import static androidCourse.technion.quickthumbs.FirestoreConstants.statsCollection;
import static androidCourse.technion.quickthumbs.FirestoreConstants.themeField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.usersCollection;

public class FriendItem {
    private String id;
    private String name;
    private String email;
    private Bitmap friendProfilePicture;
    private long totalScore;

    public FriendItem() {
    }

    public FriendItem(String id, String name, String email, Bitmap friendProfilePicture, long totalScore) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.friendProfilePicture = friendProfilePicture;
        this.totalScore = totalScore;
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

    public Bitmap getFriendProfilePicture() {
        return friendProfilePicture;
    }

    public void setFriendProfilePicture(final String uid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("users");
        StorageReference userStorage = storageRef.child(uid);
        StorageReference profilePictureRef = userStorage.child("/profilePicture.JPEG");
        final long ONE_MEGABYTE = 1024 * 1024;
        try {
            profilePictureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    friendProfilePicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                showMessage("picture was loaded from storage");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    friendProfilePicture = null;
                }
            });
        } catch (Exception e) {
            //no such picture exist
        }
    }

    public long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String uid) {
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        db.collection(usersCollection)
                .document(uid).collection(statsCollection).document(statisticsDocument).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long score = task.getResult().getLong("TotalScore");
                            totalScore = score;

                        } else {
                        }
                    }
                });
    }


    public FriendItem(final DocumentSnapshot document, boolean isApproved) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("users");
        StorageReference userStorage = storageRef.child(document.getId());
        StorageReference profilePictureRef = userStorage.child("/profilePicture.JPEG");
        final long ONE_MEGABYTE = 1024 * 1024;
        try {
            profilePictureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    friendProfilePicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    setFriendAttributes(document);
//                showMessage("picture was loaded from storage");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    friendProfilePicture = null;
                    setFriendAttributes(document);
                }
            });
        } catch (Exception e) {
            friendProfilePicture = null;
            setFriendAttributes(document);
            //no such picture exist
        }
    }

    private void setFriendAttributes(DocumentSnapshot document) {
        this.id = document.getId();
//        setEmailAndName(document.getId());
        if (document.get("email") == null) {
            email = "";
        } else {
            email = document.get("email").toString();
        }
        if (document.get("name") == null) {
            name = email;
        } else {
            name = document.get("name").toString();
        }
        if (document.getLong("TotalScore") == null) {
            this.totalScore = 0;
        } else {
            this.totalScore = document.getLong("TotalScore");
        }
    }


}
