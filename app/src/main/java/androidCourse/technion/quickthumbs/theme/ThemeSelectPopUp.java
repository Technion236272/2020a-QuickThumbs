package androidCourse.technion.quickthumbs.theme;

import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import androidCourse.technion.quickthumbs.AddTextActivity;
import androidCourse.technion.quickthumbs.GameLoadingSplashScreenActivity;
import androidCourse.technion.quickthumbs.R;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ThemeSelectPopUp {
    private static final String TAG = AddTextActivity.class.getSimpleName();
    private String[] themesNames={"Comedy","Music","Movies","Science","Games","Literature"};
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Map<String,Boolean> selectedThemes ;
    private RecyclerView recyclerView;
    private CountDownTimer timer;
    private TextView timerTextView;
    private View contextView;
    public void showPopupWindow(final View view, View callingLayout) {
        this.contextView = view;
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.theme_selecor, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, //popup width
                ViewGroup.LayoutParams.WRAP_CONTENT, //popup height
                true
        );

        if(Build.VERSION.SDK_INT>=21){
            popupWindow.setElevation(5.0f);
        }

        initializeFields(popupView);

        setStartGameButton(popupView, popupWindow);

        setBackToMainButton(popupView,popupWindow);

        setOnDismissListener(popupWindow, popupWindow);

        popupWindow.showAtLocation(callingLayout, Gravity.CENTER,0,0);

        getPersonalThemesData(view,popupWindow);
    }

    private void setCountDownTimer(final View popupView,final PopupWindow mPopupWindow) {
        timerTextView = popupView.findViewById(R.id.timer);
        timer  = new CountDownTimer(5000, 1000){
            public void onTick(long millisUntilFinished){
                timerTextView.setText(String.valueOf(millisUntilFinished/1000));
            }
            public  void onFinish(){
                timerTextView.setText("");
                Intent i = new Intent(popupView.getContext(), GameLoadingSplashScreenActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                popupView.getContext().startActivity(i);
                mPopupWindow.dismiss();

            }
        };
        timer.start();
    }

    private void setOnDismissListener(PopupWindow popupView, final PopupWindow popupWindow) {
        popupView.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                timer.cancel();
                timerTextView.setText("");
            }
        });
    }

    private void getPersonalThemesData(final View view, final PopupWindow popupView) {
        final List<ThemeDataRow> data = fillWithData();
        db.collection("users").document(getUid()).collection("themes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Boolean currentText = document.getBoolean("isChosen");
                                selectedThemes.put(document.getId(),currentText);
                            }
                            setCountDownTimer(popupView.getContentView(),popupView);
                            themeAdaptorSet(data, view);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            for (int i=0 ; i<themesNames.length ; i++){
                                //this is for the layout show
                                selectedThemes.put(themesNames[i],true);
                                //this is for the db
                                Map<String, Object> currentTheme = new HashMap<>();
                                currentTheme.put("isChosen", true);
                                db.collection("users/" + getUid() + "/themes").document(themesNames[i]).set(currentTheme, SetOptions.merge());
                            }
                            setCountDownTimer(popupView.getContentView(),popupView);
                            themeAdaptorSet(data,view);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void themeAdaptorSet(List<ThemeDataRow> data, View view) {
        ThemeAdaptor adapter = new ThemeAdaptor(data, view.getContext(), selectedThemes, timer, timerTextView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }
    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(contextView.getContext());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (account != null && currentUser == null){
            return account.getId();
        }else if (currentUser!=null){
            return mAuth.getUid();
        }else{
            return accessToken.getUserId();
        }
    }


    private void setStartGameButton(final View popupView, final PopupWindow mPopupWindow) {
        TextView closeButton = popupView.findViewById(R.id.startGamePopUpButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
                timer.cancel();
                timerTextView.setText("");
                Intent i = new Intent(popupView.getContext(), GameLoadingSplashScreenActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                popupView.getContext().startActivity(i);
            }
        });
    }

    private void setBackToMainButton(View popupView, final PopupWindow mPopupWindow) {
        TextView closeButton = popupView.findViewById(R.id.backToMainPopUpButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
                timer.cancel();
                timerTextView.setText("");
            }
        });
    }

    private void initializeFields(View popupView) {
        recyclerView = popupView.findViewById(R.id.themeRecycleView);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        selectedThemes = new HashMap<>();
        for (String themesName : themesNames) {
            //this is for the layout show
            selectedThemes.put(themesName, false);
        }
    }

    List<ThemeDataRow> fillWithData() {

        List<ThemeDataRow> data = new ArrayList<>();

        data.add(new ThemeDataRow("Comedy", "funny times with your keyboard", R.drawable.comedy));
        data.add(new ThemeDataRow("Music", "start singing once you recognized the song", R.drawable.music));
        data.add(new ThemeDataRow("Movies", "so exiting that you will forget typing", R.drawable.movies));
        data.add(new ThemeDataRow("Science", "full of science", R.drawable.science));
        data.add(new ThemeDataRow("Games", "are you a gamer? your place is here", R.drawable.games));
        data.add(new ThemeDataRow("Literature", "book warm? don't forget to type", R.drawable.literature));

        return data;
    }
}
