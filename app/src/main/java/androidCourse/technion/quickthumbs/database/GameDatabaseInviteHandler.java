package androidCourse.technion.quickthumbs.database;

import android.content.Context;
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

import androidCourse.technion.quickthumbs.personalArea.FriendsList.FriendItem;

import static com.google.firebase.firestore.SetOptions.merge;

public class GameDatabaseInviteHandler {

    public GameDatabaseInviteHandler(){}

    public void inviteFriendToAGame(final FriendItem friendItem, String roomKey, final Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> friend = new HashMap<>();
        friend.put("uid", getUid(context));
        friend.put("roomKey", roomKey);
        db.collection("users").document(friendItem.getuid()).collection("game_requests")
                .document(getUid(context)).set(friend, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "a game invite has been sent to " + friendItem.getName(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"invite has failed", Toast.LENGTH_LONG).show();
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
