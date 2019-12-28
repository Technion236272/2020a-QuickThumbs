package android.technion.quickthumbs.personalArea;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.technion.quickthumbs.MainUserActivity;
import android.technion.quickthumbs.R;
import android.technion.quickthumbs.game.GameActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.text.DecimalFormat;
import java.util.Locale;

import static android.technion.quickthumbs.FirestoreConstants.emailField;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = GameActivity.class.getSimpleName();
    private GestureDetectorCompat gestureDetectorCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        displayStatistics();
        setActionBar();

        gestureDetectorCompat = new GestureDetectorCompat(this, new SlideRightToMainScreen());

        // Check if we're running on Android 5.0 or higher
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            SlidrConfig config = new SlidrConfig.Builder().position(SlidrPosition.HORIZONTAL).build();
//            Slidr.attach(this, config);        } else {
//            // Swap without transition
//        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private CollectionReference getUserStatsCollection() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(mAuth.getCurrentUser() !=null){
            return db.collection("users")
                    .document(mAuth.getUid()).collection("stats");
        }else if (googleAccount != null) {
                return db.collection("users")
                        .document(googleAccount.getId()).collection("stats");
        }else{
            return db.collection("users")
                    .document(accessToken.getUserId()).collection("stats");
        }
//        return null;
    }

    private void displayStatistics() {
        getUserStatsCollection().document("statistics").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                Double avgAccuracy = document.getDouble("avgAccuracy");
                                Double avgWPM = document.getDouble("avgWPM");
                                Double avgCPM = document.getDouble("avgCPM");
                                Double totalScore = document.getDouble("TotalScore");
                                setStatisticsTextViews(avgAccuracy,avgWPM,avgCPM,totalScore);
                            } else {
                                Log.d(TAG, "No such document - reading statistics");
                            }
                        } else {
                            Log.d(TAG, "reading statistics failed with ", task.getException());
                        }
                    }
                });
    }

    private void setStatisticsTextViews(Double avgAccuracy, Double avgWPM, Double avgCPM, Double totalScore) {
        DecimalFormat df = new DecimalFormat("#.##");
        TextView avgAccuracyText = findViewById(R.id.AccuracyValue);
        avgAccuracyText.setText(String.valueOf(df.format(avgAccuracy)));
        TextView avgWPMText = findViewById(R.id.WPMValue);
        avgWPMText.setText(String.valueOf(df.format(avgWPM)));
        TextView avgCPMText = findViewById(R.id.CPMValue);
        avgCPMText.setText(String.valueOf(df.format(avgCPM)));
        TextView totalScoreText = findViewById(R.id.ScoreValue);
        totalScoreText.setText(String.valueOf(df.format(totalScore)));
    }


    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.profileToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void moveToFriendsActivity(View view){
        Intent intent = new Intent(this,FriendsActivity.class);
        startActivity(intent);
    }

    public void logOut(View view){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedInOnFacebook = accessToken != null && !accessToken.isExpired();
        if(currentUser != null){
            mAuth.signOut();
            finish();
        }else if (isLoggedInOnFacebook){
            LoginManager.getInstance().logOut();
            finish();
        }else{
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(this,gso);
            client.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
        }
    }

    class SlideRightToMainScreen extends GestureDetector.SimpleOnGestureListener {
        //handle 'swipe right' action only

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            if(event2.getX() < event1.getX()){
//                Toast.makeText(getBaseContext(),"Swipe right - finish()",Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            return true;
        }
    }

}
