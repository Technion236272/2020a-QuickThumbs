package android.technion.quickthumbs;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.technion.quickthumbs.FirestoreConstants.emailField;

public class AddTextActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = AddTextActivity.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<String> themesNames = ImmutableList.of("Select Theme", "Movies", "Music", "Science", "Games", "Comedy", "Literature");
    private EditText currentWordEditor;
    private EditText textTitle;
    private TextView addedTextSoFar;
    private ImageButton uploadText;
    private Spinner spin;
    private boolean needClearance;
    private boolean backwardsCommand;
    private int textMinimalLength = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);

        initializeFields();

        moveWordToText();

        uploadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleText = textTitle.getText().toString();
                String mainText = addedTextSoFar.getText().toString();
                String themesSelect = spin.getSelectedItem().toString();
                if (titleText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "The title is empty", Toast.LENGTH_SHORT).show();
                    textTitle.requestFocus();
                    return;
                } else if (mainText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "The text is empty", Toast.LENGTH_SHORT).show();
                    addedTextSoFar.requestFocus();
                    return;
                } else if (themesSelect.equals("Select Theme")) {
                    Toast.makeText(getApplicationContext(), "Please select a theme", Toast.LENGTH_SHORT).show();
                    spin.requestFocus();
                    return;
                } else if (mainText.length() < textMinimalLength){
                    Toast.makeText(getApplicationContext(), "The text is too short!\n please insert more words", Toast.LENGTH_SHORT).show();
                    currentWordEditor.requestFocus();
                    return;
                }

                uploadText.setActivated(false);
//                String textAddedId = AddTextToCollection(titleText, mainText, themesSelect);
                getCurrentThemeCount(titleText, mainText, themesSelect);



                finish();
                uploadText.setActivated(true);
            }
        });
    }

    private void updateUserTexts(final String textAddedId) {
        db.collection("users").document(getUid()).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        int userTextsAmount= 0 ;
                        if (document.getLong("textsAdded") !=  null){
                            userTextsAmount = document.getLong("textsAdded").intValue();
                        }
                        changeUserData(userTextsAmount,textAddedId);
                    } else {
                        Log.d(TAG, "No such document");
                        changeUserData(0,textAddedId);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    changeUserData(0,textAddedId);
                }
            }
        });
    }

    private void changeUserData(int value,final String textAddedId) {
        Map<String, Object> changedUser = new HashMap<>();
        changedUser.put("uid", getUid());
        changedUser.put("textsAdded", value + 1);
        db.collection("users").document(getUid())
                .set(changedUser, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        Map<String, Object> currentText = new HashMap<>();
        currentText.put("name", textAddedId);
        db.collection("users/" + getUid() + "/texts").document(textAddedId).set(currentText, SetOptions.merge());
    }

    private void getCurrentThemeCount(final String titleText, final String mainText, final String themesSelect) {
        db.collection("themes").document(themesSelect).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        int currentThemeCount= 0 ;
                        if (document.getLong("textsCount") !=  null){
                            currentThemeCount = document.getLong("textsCount").intValue();
                        }
                        changeThemeData(titleText, mainText, themesSelect, currentThemeCount);
                    } else {
                        Log.d(TAG, "No such document");
                        changeThemeData(titleText, mainText, themesSelect, 0);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    changeThemeData(titleText, mainText, themesSelect, 0);
                }
            }
        });
    }

    private void changeThemeData(final String titleText, final String mainText, final String themesSelect, final int currentThemeCount) {
        Map<String, Object> currentTheme = new HashMap<>();
        currentTheme.put("themeName", themesSelect);
        currentTheme.put("textsCount", currentThemeCount+1);
        db.collection("themes").document(themesSelect).set(currentTheme, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        AddTextToCollection(titleText,mainText,themesSelect,currentThemeCount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void AddTextToCollection(String titleText, String mainText, String themesSelect, int currentThemeTextsCount) {
        Map<String, Object> newText = new HashMap<>();
        newText.put("title", titleText);
        newText.put("theme", themesSelect);
        newText.put("mainThemeID", currentThemeTextsCount + 1);
        newText.put("text", mainText);
        newText.put("composer", getUid());
        newText.put("playCount", 0);
        newText.put("rating", 0);
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        newText.put("date", formattedDate);
        newText.put("best", 0);
        newText.put("fastestSpeed", 0);
        final String textDocumentName = db.collection("themes").document(themesSelect).collection("texts").document().getId();
        db.collection("themes").document(themesSelect).collection("texts").document(textDocumentName)
                .set(newText)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        updateUserTexts(textDocumentName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        db.collection("texts/").document(textDocumentName).set(newText, SetOptions.merge());
        db.collection("users/").document(getUid()).collection("texts/").document(textDocumentName).set(newText, SetOptions.merge());
//        return textDocumentName;
    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (account != null && currentUser == null){
            return account.getId();
        }else if (currentUser!=null){
            return mAuth.getUid();
        }else{
            return accessToken.getUserId();
        }
    }

    private void moveWordToText() {
        currentWordEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isAddedKey(before, count)) {
                    backwardsCommand = true;
                    needClearance = false;
                    return;
                } else {
                    backwardsCommand = false;
                    needClearance = false;
                }
                if (checkIfSpaceInserted(s, start)) {
                    needClearance = true;
                } else {
                    needClearance = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentWordEditor.removeTextChangedListener(this);
                String text = addedTextSoFar.getText().toString();
                if (backwardsCommand) {
                    if (text.isEmpty()) {
                        currentWordEditor.addTextChangedListener(this);
                        return;
                    }
                } else if (needClearance) {
                    if (s.toString().equals(" ")) {
                        Toast.makeText(getApplicationContext(), "Please insert a non empty word!", Toast.LENGTH_LONG).show();
                        currentWordEditor.getText().clear();
                        currentWordEditor.setSelection(0);
                    } else {
                        text += s;
                        if (text.length() >= 300) {
                            Toast.makeText(getApplicationContext(), "The text length can't be more than 300 characters!", Toast.LENGTH_LONG).show();
                        } else {
                            addedTextSoFar.setText(text);
                            currentWordEditor.getText().clear();
                            currentWordEditor.setSelection(0);
                        }
                    }
                }
                currentWordEditor.addTextChangedListener(this);
            }
        });
    }

    private void setKeyboardSettings(TextView titleValue) {
        titleValue.requestFocus();
        // showing the soft keyboard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            titleValue.setShowSoftInputOnFocus(true);
        }
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        spin = (Spinner) findViewById(R.id.themeSelectorspinner);
        needClearance = false;
        backwardsCommand = false;
        currentWordEditor = findViewById(R.id.addedWord);
        addedTextSoFar = findViewById(R.id.addedText);
        uploadText = findViewById(R.id.addButton);
        textTitle = findViewById(R.id.titleValue);
        setKeyboardSettings(textTitle);
        setActionBar();
        setSpinnerValues();
    }

    private boolean isAddedKey(int before, int count) {
        return before == 0 && count == 1;
    }

    private boolean checkIfSpaceInserted(CharSequence s, int start) {
        char key = s.charAt(start);
        String pressedKey = String.valueOf(key);
        if (pressedKey.equals(" ")) {
            return true;
        }
        return false;
    }

    private void setSpinnerValues() {
        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        spin.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, themesNames.toArray());
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(), themesNames.get(position), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.AddTextToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
