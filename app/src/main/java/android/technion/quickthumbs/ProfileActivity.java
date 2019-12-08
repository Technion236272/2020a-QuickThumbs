package android.technion.quickthumbs;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setActionBar();

    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.profileToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
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

    public void moveToTextsActivity(View view){
        Intent intent = new Intent(this,TextsActivity.class);
        startActivity(intent);
    }


}
