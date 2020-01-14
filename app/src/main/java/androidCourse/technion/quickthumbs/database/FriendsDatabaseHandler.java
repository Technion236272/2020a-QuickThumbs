package androidCourse.technion.quickthumbs.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.firestore.SetOptions.merge;

public class FriendsDatabaseHandler {

    public FriendsDatabaseHandler(){}

    public void addFriend(String friendUid, final Context context){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore  db = FirebaseFirestore.getInstance();
        Map<String, Object> friend = new HashMap<>();
        friend.put("uid", friendUid);
        db.collection("users").document(getUid(context)).collection("friends")
                .document(friendUid).set(friend, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"is now your friend!", Toast.LENGTH_LONG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"friend add failed. please go to requests and try again", Toast.LENGTH_LONG);
                    }
                });

    }

    public void removeRequest(String friendUid, final Context context){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore  db = FirebaseFirestore.getInstance();
        Map<String, Object> friend = new HashMap<>();
        friend.put("uid", friendUid);
        db.collection("users").document(getUid(context)).collection("requests")
                .document(friendUid).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"request to be your friend was removed!", Toast.LENGTH_LONG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"request remove failed. please go to requests and try again", Toast.LENGTH_LONG);
                    }
                });
    }

    private static String getUid( Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (currentUser != null) {
            return mAuth.getUid();
        } else if (account != null) {
            return account.getId();
        } else {
            return accessToken.getUserId();
        }
    }
}
