package android.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth fireBaseAuth;
    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        fireBaseAuth = FirebaseAuth.getInstance();

        moveToMainUserActivityIfAlreadyLoggedIn();

        setButtonListener((Button) findViewById(R.id.sign_in_button), MainUserActivity.class);

        setButtonListener((Button) findViewById(R.id.create_account_button), CreateAccountActivity.class);

        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.signInToolbar));
    }

    private void moveToMainUserActivityIfAlreadyLoggedIn() {
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            Intent intent = new Intent(MainActivity.this, MainUserActivity.class);

            finish();
            startActivity(intent);

            Log.d(TAG, "already signed in user: " + uid);
        }
    }

    private void setButtonListener(Button button, final Class<? extends AppCompatActivity> moveToActivityClass) {
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          switch (v.getId()) {
                                              case R.id.sign_in_button:
                                                  signIn(moveToActivityClass);

                                                  break;

                                              case R.id.create_account_button:
                                                  Intent intent = new Intent(MainActivity.this, moveToActivityClass);
                                                  startActivity(intent);

                                                  break;
                                          }
                                      }
                                  }
        );
    }

    private void signIn(final Class<?> moveToActivityClass) {
        int emailMainActivity = R.id.emailMainActivity;
        int passwordMainActivity = R.id.passwordMainActivity;

        String emailText = ((EditText) findViewById(emailMainActivity)).getText().toString();
        String passwordText = ((EditText) findViewById(passwordMainActivity)).getText().toString();

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(MainActivity.this, "Sign in failed.", Toast.LENGTH_LONG)
                    .show();

            return;
        }

        fireBaseAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MainActivity.this, moveToActivityClass);
                            String uid = fireBaseAuth.getCurrentUser().getUid();

                            finish();
                            startActivity(intent);

                            Log.d(TAG, "successfully signed in user: " + uid);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_LONG)
                                    .show();
                            Log.w(TAG, "failed to sign in user", task.getException());
                        }
                    }
                });
    }
}
