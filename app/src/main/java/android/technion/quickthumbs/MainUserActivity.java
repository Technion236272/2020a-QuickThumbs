package android.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.technion.quickthumbs.game.GameActivity;
import android.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import android.technion.quickthumbs.personalArea.ProfileActivity;
import android.technion.quickthumbs.theme.ThemeSelectPopUp;
import android.technion.quickthumbs.personalArea.TextsActivity;
import android.technion.quickthumbs.settings.UserSettingActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.technion.quickthumbs.TextPoll.fetchRandomTextSpecifiedForUsers;

public class MainUserActivity extends AppCompatActivity {
    private static final String TAG = MainUserActivity.class.getSimpleName();
    private FirebaseAuth fireBaseAuth;
    private FirebaseFirestore db;
    public static Button gameBtn;
    private GestureDetectorCompat gestureDetectorCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        fireBaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();



        gameBtn = findViewById(R.id.startGameButton);
        gameBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
//                                           Intent i = new Intent(getApplicationContext(), GameActivity.class);
//                                           startActivityForResult(i, 2);
                                           ThemeSelectPopUp popUpWindow = new ThemeSelectPopUp();
                                           popUpWindow.showPopupWindow(v,findViewById(R.id.RelativeLayout1));
                                       }
                                   }
        );
        //setButtonListener((Button) findViewById(R.id.startGameButton), GameActivity.class);
        /*
        ((Button) findViewById(R.id.startGameButton)).setOnClickListener(new View.OnClickListener() {
        (findViewById(R.id.startGameButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThemeSelectPopUp popUpWindow = new ThemeSelectPopUp();
                popUpWindow.showPopupWindow(view,findViewById(R.id.RelativeLayout1));
            }
        });*/
        setActionBar();

        closeKeyboard();

        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (checkIfUserLoggedIn(currentUser,account,isLoggedIn)) return;

        //the part that belong to the play again settings
        RelativeLayout userLoadingLayout= findViewById(R.id.userLoadingLayout);
        RelativeLayout mainLayout= findViewById(R.id.RelativeLayout1);
        Intent i = getIntent();
        if (i.hasExtra("playAgain") && i.getExtras().getBoolean("playAgain")) {
            userLoadingLayout.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.INVISIBLE);
            TextPoll.fetchRandomTextSpecifiedForUsers();
        }else{
            userLoadingLayout.setVisibility(View.INVISIBLE);
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonListener(Button button, final Class<? extends AppCompatActivity> moveToActivityClass) {
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          if (v.getId() == R.id.startGameButton) {
                                              startActivity(new Intent(getApplicationContext(), moveToActivityClass));
                                          }
                                      }
                                  }
        );
    }

    private void setActionBar() {
        Toolbar ab = findViewById(R.id.MainUserToolbar);
        setSupportActionBar(ab);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (checkIfUserLoggedIn(currentUser,account,isLoggedIn)) return;
        //the part where i insert the user to the db just ot make sure he's there in case no user has been made
    }

    private boolean checkIfUserLoggedIn(FirebaseUser currentUser, GoogleSignInAccount account, boolean isLoggedIn) {
        if(currentUser!=null || account !=null && !account.isExpired() || isLoggedIn){
            return false;
        }
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(i);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.goToPersonalAreaButton) {
            Intent intent = new Intent(MainUserActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
        if (id == R.id.addTextButton) {
            Intent intent = new Intent(MainUserActivity.this, AddTextActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        //handle 'swipe left' action only

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            if(event2.getX() < event1.getX()){
//                Toast.makeText(getBaseContext(),"Swipe left - startActivity()",Toast.LENGTH_SHORT).show();
                //switch another activity
                Intent intent = new Intent(
                        MainUserActivity.this, TextsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            else if (event2.getX() > event1.getX()){
//                Toast.makeText(getBaseContext(), "Swipe right - startActivity()", Toast.LENGTH_SHORT).show();
                //switch another activity
                Intent intent = new Intent(
                        MainUserActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
            return true;
        }
    }

}
