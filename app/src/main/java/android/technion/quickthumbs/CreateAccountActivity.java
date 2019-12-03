package android.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccountActivity extends AppCompatActivity {
    private FirebaseAuth fireBaseAuth;
    private final String TAG = CreateAccountActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_accout);

        initializeDbSingletons();

        setSignUpButton();
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
                            String uid = fireBaseAuth.getCurrentUser().getUid();

                            finish();

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
}
