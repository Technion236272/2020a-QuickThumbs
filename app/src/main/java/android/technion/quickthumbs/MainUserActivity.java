package android.technion.quickthumbs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

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
}
