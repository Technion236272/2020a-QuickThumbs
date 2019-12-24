package android.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.technion.quickthumbs.game.GameActivity;
import android.technion.quickthumbs.personalArea.ProfileActivity;
import android.technion.quickthumbs.settings.UserSettingActivity;
import android.technion.quickthumbs.theme.ThemeSelectorActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainUserActivity extends AppCompatActivity {
    private static final String TAG = MainUserActivity.class.getSimpleName();
    private FirebaseAuth fireBaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        fireBaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        Button gameBtn = findViewById(R.id.startGameButton);
        gameBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Intent i = new Intent(getApplicationContext(), GameActivity.class);
                                           startActivityForResult(i, 2);
                                       }
                                   }
        );

        Button addTxtBtn = findViewById(R.id.textAdderButton);
        addTxtBtn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             Intent i = new Intent(getApplicationContext(), AddTextActivity.class);
                                             startActivityForResult(i, 3);
                                         }
                                     }
        );

        setButtonListener((Button) findViewById(R.id.startGameButton), GameActivity.class);

        setButtonListener((Button) findViewById(R.id.textAdderButton), AddTextActivity.class);

        setButtonListener((Button) findViewById(R.id.themeSelectorButton), ThemeSelectorActivity.class);

        setActionBar();

        closeKeyboard();
    }

    private void setButtonListener(Button button, final Class<? extends AppCompatActivity> moveToActivityClass) {
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          switch (v.getId()) {
                                              case R.id.themeSelectorButton:
                                              case R.id.startGameButton:
                                              case R.id.textAdderButton:
                                                  startActivity(new Intent(getApplicationContext(), moveToActivityClass));

                                                  break;

                                          }
                                      }
                                  }
        );
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.MainUserToolbar));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Map<String, Object> changedUser = new HashMap<>();
        changedUser.put("uid", fireBaseAuth.getUid());
        changedUser.put("email", fireBaseAuth.getCurrentUser().getEmail());
        db.collection("users").document(fireBaseAuth.getUid())
                .set(changedUser, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User was inserted to to DB!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
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

        if (id == R.id.settingsButton) {
            Intent intent = new Intent(MainUserActivity.this, UserSettingActivity.class);
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
}
