package androidCourse.technion.quickthumbs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidCourse.technion.quickthumbs.R;

import androidCourse.technion.quickthumbs.theme.ThemeSelectPopUp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


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
    }

    @Override
    public void onViewCreated (View view,
                               Bundle savedInstanceState){
        final View fragmentView = view;
        fireBaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        gameBtn = view.findViewById(R.id.startGameButton);
        gameBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           ThemeSelectPopUp popUpWindow = new ThemeSelectPopUp();
                                           popUpWindow.showPopupWindow(v,fragmentView.findViewById(R.id.RelativeLayout1));
                                       }
                                   }
        );

        closeKeyboard();

    }


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


    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
