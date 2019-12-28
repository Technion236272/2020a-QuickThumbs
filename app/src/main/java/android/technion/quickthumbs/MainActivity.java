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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int google_SIGN_IN = 1;
    private static final int facebook_SIGN_IN = 2;
    private FirebaseAuth fireBaseAuth;
    private final String TAG = MainActivity.class.getName();
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleSignInButton;
    private LoginButton facebookLogIn;
    private CallbackManager callbackManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        fireBaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        moveToMainUserActivityIfAlreadyLoggedIn();

        setGoogleSignInConfigurations();

        setFacebookSignInConfigurations();

        setButtonsListeners();
    }


    private void setFacebookSignInConfigurations() {
        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create();
        facebookLogIn = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLogIn.setPermissions("email", "public_profile");
        facebookLogIn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        fireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = fireBaseAuth.getCurrentUser();
                            String email = fireBaseAuth.getCurrentUser().getEmail();
                            String uid = fireBaseAuth.getUid();
//                            getFacebookUserData();
                            Map<String, Object> changedUser = new HashMap<>();
                            changedUser.put("uid", uid);
                            changedUser.put("email", email);
                            addUserDataToCollection(changedUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void setButtonsListeners() {
        (findViewById(R.id.sign_in_button)).setOnClickListener(this);

        (findViewById(R.id.create_account_button)).setOnClickListener(this);

        googleSignInButton.setOnClickListener(this);

    }

    private void setGoogleSignInConfigurations() {
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient =  GoogleSignIn.getClient(this, gso);

        // Set the dimensions of the sign-in button.
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setSize(googleSignInButton.SIZE_STANDARD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == google_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }else{
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    // START auth_with_google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = fireBaseAuth.getCurrentUser();
                            Map<String, Object> changedUser = new HashMap<>();
                            changedUser.put("uid", fireBaseAuth.getUid());
                            changedUser.put("email", fireBaseAuth.getCurrentUser().getEmail());
                            addUserDataToCollection(changedUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }

    public void moveToCreateAccount(View view){
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    private void moveToMainUserActivityIfAlreadyLoggedIn() {
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            Intent intent = new Intent(MainActivity.this, MainUserActivity.class);

            finish();
            startActivity(intent);
            Log.d(TAG, "already signed in user: " + uid);
            return;
        }

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && !account.isExpired()) {
            String uid = account.getId();
            Intent intent = new Intent(MainActivity.this, MainUserActivity.class);

            finish();
            startActivity(intent);

            Log.d(TAG, "already signed in user: " + uid);
            return;
        }

        // Check for existing Facebook Sign In account, if the user is already signed in
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            String uid = accessToken.getUserId();
            Intent intent = new Intent(MainActivity.this, MainUserActivity.class);

            finish();
            startActivity(intent);

            Log.d(TAG, "already signed in user: " + uid);
        }
    }

    private void addUserDataToCollection(Map<String, Object> changedUser) {
        db.collection("users").document(changedUser.get("uid").toString())
                .set(changedUser, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        moveToMainUserActivityIfAlreadyLoggedIn();
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
                            FirebaseUser user = fireBaseAuth.getCurrentUser();
                            String uid = user.getUid();
                            if(user.isEmailVerified()){
                                Map<String, Object> changedUser = new HashMap<>();
                                changedUser.put("uid", fireBaseAuth.getUid());
                                changedUser.put("email", fireBaseAuth.getCurrentUser().getEmail());
                                addUserDataToCollection(changedUser);
                                Log.d(TAG, "successfully signed in user: " + uid);
                            } else{
                                Toast.makeText(MainActivity.this, "Authentication failed. Please verify your email.", Toast.LENGTH_LONG)
                                        .show();
                                sendUserEmailVerification(user);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_LONG)
                                    .show();
                            Log.w(TAG, "failed to sign in user", task.getException());
                        }
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
                            Toast.makeText(MainActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn(MainUserActivity.class);

                break;

            case R.id.create_account_button:
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);

                break;

            case R.id.google_sign_in_button:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, google_SIGN_IN);

                break;
        }
    }
}
