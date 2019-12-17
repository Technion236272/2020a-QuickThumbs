package android.technion.quickthumbs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.technion.quickthumbs.game.GameActivity;
import android.technion.quickthumbs.personalArea.ProfileActivity;
import android.technion.quickthumbs.settings.UserSettingActivity;
import android.technion.quickthumbs.theme.ThemeSelectorActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainUserActivity extends AppCompatActivity {
    private FirebaseAuth fireBaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        fireBaseAuth = FirebaseAuth.getInstance();


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
}
