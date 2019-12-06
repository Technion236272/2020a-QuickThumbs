package android.technion.quickthumbs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

        Button gameBtn = ((Button) findViewById(R.id.startGameButton));
        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent i = new Intent(getApplicationContext(), GameActivity.class);
              startActivityForResult(i, 2);
            }
        }
        );

        Button addTxtBtn = ((Button) findViewById(R.id.textAdderButton));
        addTxtBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           Intent i = new Intent(getApplicationContext(), AddTextActivity.class);
                                           startActivityForResult(i, 2);
                                       }
                                   }
        );

        setButtonListener((Button) findViewById(R.id.themeSelectorButton), ThemeSelectorActivity.class);
    }

    @Override
    protected void onStop() {
        fireBaseAuth.signOut();
        super.onStop();
    }

    /* Menu Overrider methods */

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.goToPersonalAreaButton) {
            // TODO: implement me!
        }

        if (id == R.id.settingsButton) {
            Intent intent = new Intent(MainUserActivity.this, UserSettingActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setButtonListener(Button button, final Class<? extends AppCompatActivity> moveToActivityClass) {
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          switch (v.getId()) {
                                              case R.id.themeSelectorButton:
                                                  Intent intent = new Intent(MainUserActivity.this, moveToActivityClass);
                                                  startActivity(intent);

                                                  break;
                                          }
                                      }
                                  }
        );
    }
}
