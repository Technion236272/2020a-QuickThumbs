package android.technion.quickthumbs.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.technion.quickthumbs.AddTextActivity;
import android.technion.quickthumbs.R;
import android.technion.quickthumbs.game.GameActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

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

public class ThemeSelectorActivity extends AppCompatActivity {
    private static final String TAG = AddTextActivity.class.getSimpleName();
    String[] themesNames={"Comedy","Music","Movies","Science","Games","Literature"};
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Map<String,Boolean> selectedThemes ;
    RecyclerView recyclerView;
    private RelativeLayout themeLoadingLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_selector);
        themeLoadingLayout = findViewById(R.id.themeLoadingLayout);
        recyclerView = findViewById(R.id.themeRecycleView);

        themeLoadingLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        setActionBar();

        final List<ThemeDataRow> data = fillWithData();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        selectedThemes = new HashMap<>();
        for (int i=0 ; i<themesNames.length ; i++){
            //this is for the layout show
            selectedThemes.put(themesNames[i],false);
            //this is for the db
        }

        getPersonalThemesData(data);

    }

    private void getPersonalThemesData(final List<ThemeDataRow> data) {
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
                            themeAdaptorSet(data);
                            themeLoadingLayout.setVisibility(View.INVISIBLE);
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
                            themeAdaptorSet(data);
                            themeLoadingLayout.setVisibility(View.INVISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (account != null && currentUser == null){
            return account.getId();
        }else if (currentUser!=null){
            return mAuth.getUid();
        }else{
            return accessToken.getUserId();
        }
    }

    private void themeAdaptorSet(List<ThemeDataRow> data) {
        ThemeAdaptor adapter = new ThemeAdaptor(data, getApplication(), selectedThemes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.themeSelectorToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    public List<ThemeDataRow> fillWithData() {

        List<ThemeDataRow> data = new ArrayList<>();

        data.add(new ThemeDataRow("Comedy", "funny times with your keyboard", R.drawable.funny_trump_icon));
        data.add(new ThemeDataRow("Music", "start singing once you recognized the song", R.drawable.music_icon));
        data.add(new ThemeDataRow("Movies", "so exiting that you will forget typing", R.drawable.movie_icon));
        data.add(new ThemeDataRow("Science", "full of science", R.drawable.stupid_science_icon));
        data.add(new ThemeDataRow("Games", "are you a gamer? your place is here", R.drawable.games_icon));
        data.add(new ThemeDataRow("Literature", "book warm? don't forget to type", R.drawable.literature_icon));

        return data;
    }
}
