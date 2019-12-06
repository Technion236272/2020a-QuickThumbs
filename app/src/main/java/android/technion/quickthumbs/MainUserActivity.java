package android.technion.quickthumbs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class MainUserActivity extends AppCompatActivity {
    private FirebaseAuth fireBaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        fireBaseAuth = FirebaseAuth.getInstance();
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

        return super.onOptionsItemSelected(item);
    }
}
