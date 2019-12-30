package androidCourse.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {
    private FirebaseAuth fireBaseAuth;
    private final String TAG = CreateAccountActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_accout);

        initializeDbSingletons();

        setSignUpButton();
        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.createAccountToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
    }

    private void initializeDbSingletons() {
        fireBaseAuth = FirebaseAuth.getInstance();
    }

    private void setSignUpButton() {
        final Button singUpButton = findViewById(R.id.signOutButton);

        singUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    signUp(singUpButton);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void signUp(final Button singUpButton) throws InterruptedException {
        singUpButton.setEnabled(false);

        int emailMainActivity = R.id.emailCreateAccount;
        int passwordMainActivity = R.id.passwordCreateAccount;

        String emailText = ((EditText) findViewById(emailMainActivity)).getText().toString();
        String passwordText = ((EditText) findViewById(passwordMainActivity)).getText().toString();

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(CreateAccountActivity.this, "Creation failed.", Toast.LENGTH_LONG)
                    .show();
            singUpButton.setEnabled(true);

            return;
        }

        fireBaseAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = fireBaseAuth.getCurrentUser();
                            String uid = user.getUid();
                            sendUserEmailVerification(user);
                            Log.d(TAG, "successfully created user for uid: " + uid);
                        } else {
                            Toast.makeText(CreateAccountActivity.this, "Creation failed.", Toast.LENGTH_LONG)
                                    .show();

                            Log.w(TAG, "failed to sign up user", task.getException());
                        }

                        singUpButton.setEnabled(true);
                    }
                });
    }

    void sendUserEmailVerification(final FirebaseUser user){
        user.sendEmailVerification()
                .addOnCompleteListener(this,new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification mail sent successfully");
                            Toast.makeText(CreateAccountActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(CreateAccountActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
