package android.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.technion.quickthumbs.game.GameActivity;
import android.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import android.technion.quickthumbs.personalArea.ProfileActivity;
import android.technion.quickthumbs.theme.ThemeSelectPopUp;
import android.technion.quickthumbs.personalArea.TextsActivity;
import android.technion.quickthumbs.settings.UserSettingActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

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
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

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


public class MainUserActivity extends Fragment {
    private static final String TAG = MainUserActivity.class.getSimpleName();
    private FirebaseAuth fireBaseAuth;
    private FirebaseFirestore db;
    public static Button gameBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.activity_main_user, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fireBaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        gameBtn = getView().findViewById(R.id.startGameButton);
        gameBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           ThemeSelectPopUp popUpWindow = new ThemeSelectPopUp();
                                           popUpWindow.showPopupWindow(v,getView().findViewById(R.id.RelativeLayout1));
                                       }
                                   }
        );

        closeKeyboard();

        //gestureDetectorCompat = new GestureDetectorCompat(getActivity(), new MyGestureListener());


        // Check if we're running on Android 5.0 or higher
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            SlidrConfig config = new SlidrConfig.Builder().position(SlidrPosition.HORIZONTAL).build();
//            Slidr.attach(this, config);        } else {
//            // Swap without transition
//        }

    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        this.gestureDetectorCompat.onTouchEvent(event);
//        return super.onTouchEvent(event);
//    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (checkIfUserLoggedIn(currentUser,account,isLoggedIn)) return;
    }

    private void setButtonListener(Button button, final Class<? extends AppCompatActivity> moveToActivityClass) {
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          if (v.getId() == R.id.startGameButton) {
                                              startActivity(new Intent(getActivity(), moveToActivityClass));
                                          }
                                      }
                                  }
        );
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (checkIfUserLoggedIn(currentUser,account,isLoggedIn)) return;
        //the part where i insert the user to the db just ot make sure he's there in case no user has been made
    }

    private boolean checkIfUserLoggedIn(FirebaseUser currentUser, GoogleSignInAccount account, boolean isLoggedIn) {
        if(currentUser!=null || account !=null && !account.isExpired() || isLoggedIn){
            return false;
        }
        Intent i = new Intent(getActivity(), MainActivity.class);
        getActivity().finish();
        startActivity(i);
        return true;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.user_main_menu, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.goToPersonalAreaButton) {
//            Intent intent = new Intent(getActivity(), ProfileActivity.class);
//            startActivity(intent);
//            //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//        }
//        if (id == R.id.TextAreaButton) {
//            Intent intent = new Intent(getActivity(), TextsActivity.class);
//            startActivity(intent);
//            //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

//    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//        //handle 'swipe left' action only
//
//        @Override
//        public boolean onFling(MotionEvent event1, MotionEvent event2,
//                               float velocityX, float velocityY) {
//
//            if(event2.getX() < event1.getX()){
////                Toast.makeText(getBaseContext(),"Swipe left - startActivity()",Toast.LENGTH_SHORT).show();
//                //switch another activity
//                Intent intent = new Intent(
//                        getActivity(), TextsActivity.class);
//                startActivity(intent);
//               // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//            }
//            else if (event2.getX() > event1.getX()){
////                Toast.makeText(getBaseContext(), "Swipe right - startActivity()", Toast.LENGTH_SHORT).show();
//                //switch another activity
//                Intent intent = new Intent(
//                        getActivity(), ProfileActivity.class);
//                startActivity(intent);
//               // overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//            }
//            return true;
//        }
//    }

}
