package androidCourse.technion.quickthumbs.game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidCourse.technion.quickthumbs.GameLoadingSplashScreenActivity;

import androidCourse.technion.quickthumbs.R;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableList;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidCourse.technion.quickthumbs.FirestoreConstants;

import static com.google.firebase.firestore.SetOptions.merge;

public class GameActivity extends AppCompatActivity {
    private EditText currentWordEditor;
    private Boolean needClearance;
    private TextView gameTextView;
    private TextView pointTextView;
    private TextView wpmTextView;
    private TextView cpmTextView;
    private SpannableString ss;
    private SpannableStringBuilder editorMutableSpannable; //TODO get rid of this and change location of lock...
    private RelativeLayout gameLoadingLayout;
    private RelativeLayout gamePlayingLayout;
    private RelativeLayout gameReportLayout;
    private TextView gameLoadingText;
    private TextView gameReportText;
    private TextView correctOutOfTotalTextView;
    private TextView correctOutOfTotalPercentageTextView;
    private TextView comboDisplayer;
    private TextView pointsChangeIndicator;

    private boolean forwardCommand;

    private int gameTextWordStart;

    private List<Pair<String, Integer>> wordsMapper;
    private int currentWordIndex;

    private GameWordStatus[] wordFlags;
    private List<ForegroundColorSpan> wordColorSpans;
    private int gameTextWordOffset;

    private final BackgroundColorSpan colorBackGround = new BackgroundColorSpan(Color.rgb(255, 102, 0));

    private long gameStartTimeStamp;    //changing, don't trust this value if you wish to get real starting time.
    private long gameStopTimeStamp;
    private Timer gameTimer;

    private int correctKeysAmount;

    List<Integer> catNoises = ImmutableList.of(R.raw.annoying_cat_0, R.raw.annoying_cat_1, R.raw.annoying_cat_2);
    private int collectedPoints;
    private Integer[] wordPoints;
    private List<Integer> comboOptions;
    private int currentComboIndex;
    private int comboCounter;
    private boolean isPreviousActionIsCorrectOrGameJustStarted;
    private boolean isVibrateOnMistakeOn =true;

    private boolean shouldStartTimer;
    private final int comboThreshold = 4;
    private Timestamp gameTimeStamp;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = GameActivity.class.getName();

    public static TextDataRow selectedTextItem; //is assigned in TextPoll
    public boolean changed=false;

    MediaPlayer positiveMediaPlayer;
    MediaPlayer negativeMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializeFields();

        setGameTextAndLogicAndEnding(gameTextView);


        currentWordEditor.setActivated(false);
        gameLoadingLayout.setVisibility(View.VISIBLE);
        gamePlayingLayout.setVisibility(View.INVISIBLE);

        closeKeyboard();

        setUpSounds();  //any future sound features should be added here;

        gameCreationSequence();
        setRatingBarListener();
    }

    public void gameCreationSequence() {

        String gameText = gameTextView.getText().toString();
        ss = new SpannableString(gameText);
        gameTextView.setText(ss, TextView.BufferType.SPANNABLE);

        gamePlayingLayout.setVisibility(View.VISIBLE);
        gameLoadingLayout.setVisibility(View.INVISIBLE);

        keyboardConfiguration(currentWordEditor);

        setEditorLogic();

        comboDisplayChange();

        String[] words = gameText.split(" ");

        wordsMapper = setWordsMapper(words);

        currentWordIndex = -1;
        moveMarkerToNextWord(colorBackGround);
        currentWordIndex = 0;

        initializeWordFlagsAndPointsDefaultValue(words[0]);
        gameTextWordOffset = 0;

        setUpSounds();  //any future sound features should be added here;

    }

    private void comboDisplayChange() {
        String comboString = String.valueOf(comboOptions.get(currentComboIndex));

        comboDisplayer.setText("X" + comboString);

        YoYo.with(Techniques.BounceIn)
                .duration(500)
                .playOn(comboDisplayer);
    }

    private void setUpSounds() {
        supplyFreshMediaPlayers();

        setUpSoundsOnComplete();
    }

    private void supplyFreshMediaPlayers() {
        double randomDouble = Math.random();
        randomDouble = randomDouble * 3;
        int randomInt = (int) randomDouble;

        assert (randomInt >= 0 && randomInt <= 2);

        positiveMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.typing_sound);
        negativeMediaPlayer = MediaPlayer.create(getApplicationContext(), catNoises.get(randomInt));
    }

    private void setUpSoundsOnComplete() {
        setUpSoundOnComplete(positiveMediaPlayer);
        setUpSoundOnComplete(negativeMediaPlayer);
    }

    private void setTimerUpdateGameStatsPresentation() {
        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timePassedFromStartGame = System.currentTimeMillis() - gameStartTimeStamp;

                        int cpm = (int) (((double) correctKeysAmount / (double) timePassedFromStartGame) * 1000d * 60d);
                        String cpmString = String.valueOf(cpm);

                        pointTextView.setText(String.valueOf(collectedPoints));
                        wpmTextView.setText(String.valueOf(cpm / 5));
                        cpmTextView.setText(cpmString);
                    }
                });
            }
        }, 1000, 700);
    }

    @Override
    protected void onStart() {
        super.onStart();

        long inactiveDeltaTime = System.currentTimeMillis() - gameStopTimeStamp;
        gameStartTimeStamp += inactiveDeltaTime;

        setUpSounds();
    }

    @Override
    protected void onStop() {
        super.onStop();

        gameStopTimeStamp = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();

        gameStopTimeStamp = System.currentTimeMillis();
    }

    private List<Pair<String, Integer>> setWordsMapper(String[] words) {
        List<Pair<String, Integer>> wordsMapper = new ArrayList<>();

        int current = 0;

        for (int i = 0; i < words.length; i++) {
            String currentWord = words[i];
            wordsMapper.add(new Pair<>(currentWord, current));
            current += currentWord.length() + 1;
        }

        return wordsMapper;
    }

    private void setGameTextAndLogicAndEnding(TextView gameTextView) {
        Intent i = getIntent();
        if (i.hasExtra("text") && !changed) {
            Log.d(TAG,"being set");
            gameTextView.setText(i.getExtras().getString("text"));
            String id=i.getExtras().getString("id");
            String title=i.getExtras().getString("title");
            String text=i.getExtras().getString("text");
            String composer=i.getExtras().getString("composer");
            String theme=i.getExtras().getString("theme");
            String date=i.getExtras().getString("date");
            Double rating=i.getExtras().getDouble("rating");
            String numberOfTimesPlayed=i.getExtras().getString("playCount");
            String bestScore=i.getExtras().getString("bestScore");
            String fastestSpeed=i.getExtras().getString("fastestSpeed");
            selectedTextItem= new TextDataRow(id, title,theme,text, date,composer,
                    rating,numberOfTimesPlayed, bestScore, fastestSpeed);
            changed =true;
        }
        if(changed==true){
            gameTextView.setText(selectedTextItem.getText());
            changed=false;
        }
    }

    private void initializeFields() {
        comboCounter = 0;
        collectedPoints = 0;
        comboOptions = ImmutableList.of(1, 2, 4, 8, 10);
        currentComboIndex = 0;
        shouldStartTimer = true;
        correctKeysAmount = 0;
        gameTextWordStart = 0;
        gameStartTimeStamp = 0;
        gameStopTimeStamp = 0;
        needClearance = false;
        forwardCommand = true;
        isPreviousActionIsCorrectOrGameJustStarted = true;

        currentWordEditor = findViewById(R.id.currentWord);
        editorMutableSpannable = new SpannableStringBuilder();
        currentWordEditor.setText(editorMutableSpannable);
        wordColorSpans = new ArrayList<>();

        gameTextView = findViewById(R.id.displayText);
        pointTextView = findViewById(R.id.pointsValue);
        wpmTextView = findViewById(R.id.WPMValue);
        cpmTextView = findViewById(R.id.CPMValue);
        gameLoadingLayout = findViewById(R.id.gameLoadingLayout);
        gamePlayingLayout = findViewById(R.id.gameTextLayout);
        gameReportLayout = findViewById(R.id.gameReportLayout);
        gameLoadingText = findViewById(R.id.gameLoadingText);
        gameReportText = findViewById(R.id.gameReportTextLayout);
        correctOutOfTotalTextView = findViewById(R.id.correctOutOfTotalTextView);
        correctOutOfTotalPercentageTextView = findViewById(R.id.correctOutOfTotalPercentageTextView);
        comboDisplayer = findViewById(R.id.comboDisplayer);
        pointsChangeIndicator = findViewById(R.id.changeIndicator);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        gameTimeStamp = new Timestamp(new Date());
    }

    private void setEditorLogic() {
        currentWordEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged start ...");

                if (shouldStartTimer) {
                    setTimerUpdateGameStatsPresentation();
                    shouldStartTimer = false;
                }

                Log.d(TAG, "beforeTextChanged finish ...");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged start ...");

                if (isAddedKey(before, count)) {
                    logicOnAddedKey(s, start);
                } else {    //removed key
                    forwardCommand = false;
                    logicOnRemovingKey();
                }

                Log.d(TAG, "onTextChanged finish ...");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged start ...");

                if (forwardCommand) {
                    gameTextWordOffset++;

                    if (!needClearance) {
                        paintEditorTextBasedOnLastAction(true);
                    } else {
                        currentWordEditor.removeTextChangedListener(this);

                        currentWordEditor.getText().clear();
                        currentWordEditor.setSelection(0);
                        currentWordEditor.setText(editorMutableSpannable);
                        needClearance = false;

                        currentWordEditor.addTextChangedListener(this);

                        if (currentWordIndex + 1 != wordsMapper.size()) {
                            moveMarkerToNextWord(colorBackGround);
                            spaceKeyIncreaseCorrectKeysWhenFullyCorrectWordTyped();
                        }

                        editorMutableSpannable = new SpannableStringBuilder();
                        wordColorSpans = new ArrayList<>();


                        paintGameTextBasedOnWordFlags();

                        gameTextWordOffset = 0;
                        gameTextWordStart = getNextStartWordIndex();

                        if (gameTextWordStart != -1) {
                            String currentExpectedWord = wordsMapper.get(currentWordIndex).first;
                            initializeWordFlagsAndPointsDefaultValue(currentExpectedWord);

                        } else {
                            finishGame();
                        }
                    }

                } else {
                    if (gameTextWordOffset != 0) {
                        gameTextWordOffset--;
                    }

                    paintEditorTextBasedOnLastAction(false);

                    forwardCommand = true;
                }

                if (needClearance) {

                }

                Log.d(TAG, "afterTextChanged finish ...");
            }

        });
    }

    private void spaceKeyIncreaseCorrectKeysWhenFullyCorrectWordTyped() {
        boolean giveExtraCorrectCharacterForSpacePress = true;

        for (GameWordStatus status : wordFlags) {
            if (!status.equals(GameWordStatus.CORRECT) && !status.equals(GameWordStatus.CORRECT_BUT_BEEN_HERE_BEFORE)) {
                giveExtraCorrectCharacterForSpacePress = false;
            }
        }

        if (giveExtraCorrectCharacterForSpacePress) {
            positiveLogicOnSpaceKeyForCorrectWord();
        } else {
            resetCombo(false);
        }
    }

    private void paintGameTextBasedOnWordFlags() {
        int textStartingIndex = wordsMapper.get(currentWordIndex).second;

        int maxIndexWord = textStartingIndex + gameTextWordOffset;

        int colorUntilIndex = Math.min(maxIndexWord, textStartingIndex + wordFlags.length);

        Spannable gameTextSpannable = (Spannable) gameTextView.getText();

        for (int i = textStartingIndex; i < colorUntilIndex; i++) {
            GameWordStatus status = wordFlags[i - textStartingIndex];

            switch (status) {
                case CORRECT:
                    ForegroundColorSpan green = new ForegroundColorSpan(Color.GREEN);
                    gameTextSpannable.setSpan(green, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;

                case ALREADY_SEEN:
                case NO_STATUS:
                case WRONG:
                    ForegroundColorSpan red = new ForegroundColorSpan(Color.RED);
                    gameTextSpannable.setSpan(red, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;

                case CORRECT_BUT_BEEN_HERE_BEFORE:
                    ForegroundColorSpan yellow = new ForegroundColorSpan(Color.YELLOW);
                    gameTextSpannable.setSpan(yellow, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;
            }
        }

        if (maxIndexWord < textStartingIndex + wordFlags.length) {
            ForegroundColorSpan red = new ForegroundColorSpan(Color.RED);
            gameTextSpannable.setSpan(red, maxIndexWord, textStartingIndex + wordFlags.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }


//        gameTextView.setText(ss);
    }

    private void logicOnRemovingKey() {
        resetCombo(true);

        if (gameTextWordOffset <= wordFlags.length) { //will not!! get here if position in EditText is 0 and tries to remove.
            reducePointsBasedOnPreviousEarnings();

            if (gameTextWordOffset != wordFlags.length) {
                GameWordStatus wordFlag = wordFlags[gameTextWordOffset - 1];

                if (wordFlag == GameWordStatus.CORRECT || wordFlag == GameWordStatus.CORRECT_BUT_BEEN_HERE_BEFORE) {
                    correctKeysAmount--;
                }

                wordFlags[gameTextWordOffset - 1] = GameWordStatus.ALREADY_SEEN;
            }
        }
    }

    private void reducePointsBasedOnPreviousEarnings() {
        int pointsEarnedForThisCharacterPreviously = wordPoints[gameTextWordOffset - 1];
        collectedPoints -= pointsEarnedForThisCharacterPreviously;
        wordPoints[gameTextWordOffset - 1] = 0;

        changePointsIndication(pointsEarnedForThisCharacterPreviously, false);
    }

    private void logicOnAddedKey(CharSequence s, int start) {
        char key = s.charAt(start);
        String pressedKey = String.valueOf(key);
        String currentExpectedWord = wordsMapper.get(currentWordIndex).first;

        if (pressedKey.equals(" ")) {
            if (currentWordIndex + 1 != wordsMapper.size()) {
//                moveMarkerToNextWord(colorBackGround);
            } else {
                Spannable gameTextSpannable = (Spannable) gameTextView.getText();
                gameTextSpannable.removeSpan(colorBackGround);
            }

            needClearance = true;
        } else {
            String expectedKey;

            if (gameTextWordOffset < currentExpectedWord.length()) {
                expectedKey = String.valueOf(currentExpectedWord.charAt(gameTextWordOffset));
            } else {
                resetCombo(false);
                expectedKey = null;
            }

            logicOnCurrentWordAddedKey(pressedKey, expectedKey);
        }
    }

    private void finishGame() {
        gameTimer.cancel();

        closeKeyboard();

        gameReportLayout.setVisibility(View.VISIBLE);
        gamePlayingLayout.setVisibility(View.INVISIBLE);

        gameReportText.setText(gameTextView.getText());

        int totalCharacters = gameTextView.getText().toString().length();

        correctOutOfTotalTextView.setText(String.format("%s/%s", correctKeysAmount, totalCharacters));

        int correctPercentage = (int) (((float) correctKeysAmount / (float) totalCharacters) * 100f);

        correctOutOfTotalPercentageTextView.setText(correctPercentage + "%");

        ((TextView)(findViewById(R.id.reportWPMValue))).setText(wpmTextView.getText());
        ((TextView)(findViewById(R.id.reportCPMValue))).setText(cpmTextView.getText());
        ((TextView)(findViewById(R.id.reportPointsValue))).setText(pointTextView.getText());

        Double wpm = Double.valueOf(wpmTextView.getText().toString());
        Double cpm = Double.valueOf(cpmTextView.getText().toString());
        Double points = Double.valueOf(pointTextView.getText().toString());
        updateUserStats(Double.valueOf(correctPercentage),wpm,cpm,points);
        updateComposerTextBestScoreAndWPM(wpm, points);
    }

    private Double calcNewAvgAfterAddingElement(Double oldAvg, Long oldCount, Double element) {
        return (oldAvg * oldCount + element) / (oldCount + 1.0);
    }

    private void updateUserStats(final Double accuracy, final Double wpm, final Double cpm, final Double points) {
        getUserStatsCollection().document("statistics").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                Long numOfGames = document.getLong(FirestoreConstants.numOfGamesField);
                                Double newAvgAccuracy =
                                        calcNewAvgAfterAddingElement(document.getDouble(FirestoreConstants.accuracyField), numOfGames, accuracy);
                                Double newAvgWPM =
                                        calcNewAvgAfterAddingElement(document.getDouble(FirestoreConstants.WPMField), numOfGames, wpm);
                                Double newAvgCPM =
                                        calcNewAvgAfterAddingElement(document.getDouble(FirestoreConstants.CPMField), numOfGames, cpm);
                                Double newTotalScore =
                                        document.getDouble(FirestoreConstants.totalScoreField) + points;
                                writeToUserStatistics(numOfGames + 1, newAvgAccuracy, newAvgWPM, newAvgCPM, newTotalScore);
                                writeGameResult(wpm, cpm, accuracy, points);
                            } else {
                                Log.d(TAG, "No such document");
                                writeToUserStatistics(1, accuracy, wpm, cpm, points);
                                writeGameResult(wpm, cpm, accuracy, points);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            writeToUserStatistics(1, accuracy, wpm, cpm, points);
                            writeGameResult(wpm, cpm, accuracy, points);
                        }
                    }
                });
    }

    private void writeGameResult(Double WPM, Double CPM, Double accuracy, Double points) {
        String theme = selectedTextItem.getThemeName();
        String index = selectedTextItem.getID();
        String timestamp = selectedTextItem.getDate();

        Map<String, Object> updatedStatistics = new HashMap<>();
        updatedStatistics.put(FirestoreConstants.uidField, getUid());
        if(mAuth.getCurrentUser() !=null){
            updatedStatistics.put(FirestoreConstants.emailField, mAuth.getCurrentUser().getEmail());
        }
        updatedStatistics.put(FirestoreConstants.dateField, gameTimeStamp);
        updatedStatistics.put(FirestoreConstants.CPMField, CPM);
        updatedStatistics.put(FirestoreConstants.accuracyField, accuracy);
        updatedStatistics.put(FirestoreConstants.WPMField, WPM);
        updatedStatistics.put(FirestoreConstants.totalScoreField, points);
        updatedStatistics.put(FirestoreConstants.themeField, theme);
        updatedStatistics.put(FirestoreConstants.textIndexField, index);
        updatedStatistics.put(FirestoreConstants.textDateField, timestamp);

        getUserStatsCollection().document(FirestoreConstants.statisticsDocument).collection(FirestoreConstants.gameResultsCollection)
                .document(theme + "-" + index.toString() + "-" + gameTimeStamp.toString())
                .set(updatedStatistics, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user game result successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing user game result", e);
                    }
                });
    }

    private void writeToUserStatistics(long numOfGames, Double newAvgAccuracy, Double newAvgWPM, Double newAvgCPM, Double newTotalScore) {
        Map<String, Object> updatedStatistics = new HashMap<>();
        updatedStatistics.put(FirestoreConstants.uidField, getUid());
        if(mAuth.getCurrentUser() !=null){
            updatedStatistics.put(FirestoreConstants.emailField, mAuth.getCurrentUser().getEmail());
        }
        updatedStatistics.put(FirestoreConstants.dateField, gameTimeStamp);
        updatedStatistics.put(FirestoreConstants.numOfGamesField, numOfGames);
        updatedStatistics.put(FirestoreConstants.CPMField, newAvgCPM);
        updatedStatistics.put(FirestoreConstants.accuracyField, newAvgAccuracy);
        updatedStatistics.put(FirestoreConstants.WPMField, newAvgWPM);
        updatedStatistics.put(FirestoreConstants.totalScoreField, newTotalScore);

        getUserStatsCollection().document(FirestoreConstants.statisticsDocument).set(updatedStatistics)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user average statistics successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error writing average statistics", e);
            }
        });
    }

    private CollectionReference getUserStatsCollection() {
        if (mAuth.getCurrentUser() != null) {
            return db.collection(FirestoreConstants.usersCollection)
                    .document(getUid()).collection(FirestoreConstants.statsCollection);
        }else{
            return db.collection(FirestoreConstants.usersCollection)
                    .document(getUid()).collection(FirestoreConstants.statsCollection);
        }
//        return null;
    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (currentUser != null) {
            return mAuth.getUid();
        } else if (account != null){
            return account.getId();
        }else{
            return accessToken.getUserId();
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void paintEditorTextBasedOnLastAction(boolean isForwardTyping) {
        paintGameTextBasedOnWordFlags();

//        int index;

//        if (isForwardTyping) {
//            index = gameTextWordOffset + 1;
//
//        } else {
//            index = gameTextWordOffset;
//        }
//
//        currentWordEditor.setSelection(index);
    }

    private void paintEditorTextBasedOnWordFlags(boolean isForwardTyping) {
        Editable text = currentWordEditor.getText();

        String wordText = text.toString();

        for (int i = 0; i < wordText.length(); i++) {
            SpannableString immutableSpannable;

            if (i >= wordFlags.length) {
                ForegroundColorSpan red = new ForegroundColorSpan(Color.RED);

                String suffix = wordText.substring(i);
                immutableSpannable = new SpannableString(suffix);
                immutableSpannable.setSpan(red, 0, suffix.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                editorMutableSpannable.append(immutableSpannable);

                break;
            }

            immutableSpannable = new SpannableString(String.valueOf(wordText.charAt(i)));

            GameWordStatus currentKeyFlag = wordFlags[i];

            switch (currentKeyFlag) {
                case CORRECT:
                    ForegroundColorSpan green = new ForegroundColorSpan(Color.GREEN);
                    immutableSpannable.setSpan(green, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;

                case WRONG:
                    ForegroundColorSpan red = new ForegroundColorSpan(Color.RED);
                    immutableSpannable.setSpan(red, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;

                case NO_STATUS:
                case ALREADY_SEEN:
                    ForegroundColorSpan black = new ForegroundColorSpan(Color.BLACK);
                    immutableSpannable.setSpan(black, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;

                case CORRECT_BUT_BEEN_HERE_BEFORE:
                    ForegroundColorSpan yellow = new ForegroundColorSpan(Color.YELLOW);
                    immutableSpannable.setSpan(yellow, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;
            }

            editorMutableSpannable.append(immutableSpannable);
        }

        currentWordEditor.setText(editorMutableSpannable);

        int index;

        if (isForwardTyping) {
            index = gameTextWordOffset + 1;

        } else {
            index = gameTextWordOffset;
        }

        currentWordEditor.setSelection(index);
    }

    private void logicOnCurrentWordAddedKey(String pressedKey, String expectedKey) {
        if (expectedKey == null) {
            vibrateOnKeyPressedMistake();
            return;
        }

        if (pressedKey.equals(expectedKey)) {
            positiveLogicOnAddedCorrectKeyToWord();
        } else {
            resetCombo(false);
            wordFlags[gameTextWordOffset] = GameWordStatus.WRONG;
            vibrateOnKeyPressedMistake();
        }
    }

    private void vibrateOnKeyPressedMistake() {
        if(isVibrateOnMistakeOn){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(300);
            }
        }
    }

    private void positiveLogicOnSpaceKeyForCorrectWord() {
        correctKeysAmount++;
        increaseCombo();
        increasePoints(gameTextWordOffset - 1, false);
        comboMakePositiveSound();
    }

    private void positiveLogicOnAddedCorrectKeyToWord() {
        increaseCombo();

        if (wordFlags[gameTextWordOffset].equals(GameWordStatus.NO_STATUS)) {
            wordFlags[gameTextWordOffset] = GameWordStatus.CORRECT;
            correctKeysAmount++;
        } else {
            wordFlags[gameTextWordOffset] = GameWordStatus.CORRECT_BUT_BEEN_HERE_BEFORE;
            correctKeysAmount++;
        }

        increasePoints(gameTextWordOffset, true);

        comboMakePositiveSound();
    }

    private void increasePoints(int gameTextWordOffsetLocal, boolean shouldRememberPoints) {
        Integer combo = comboOptions.get(currentComboIndex);
        int pointsToAdd = combo * 10;

        if (shouldRememberPoints) {
            wordPoints[gameTextWordOffsetLocal] = pointsToAdd;

            changePointsIndication(pointsToAdd, true);
        }

        collectedPoints += pointsToAdd;
    }

    private void changePointsIndication(int amountOfChangedPoints, boolean isAddition) {
        String previousAmountWithSign = pointsChangeIndicator.getText().toString();

        String sign = isAddition ? "+" : "-";
        String newIndication = sign + amountOfChangedPoints;

        if (amountOfChangedPoints == 0 || newIndication.equals(previousAmountWithSign)) {   //there is no change...
            return;
        }

        changePointsAmountAndColor(isAddition, newIndication);

        animateChange(isAddition);
    }

    private void animateChange(boolean isAddition) {
        Techniques animation;

        if (isAddition) {
            animation = Techniques.Flash;
        } else {
            animation = Techniques.FadeIn;
        }

        YoYo.with(animation)
                .duration(250)
                .playOn(pointsChangeIndicator);
    }

    private void changePointsAmountAndColor(boolean isAddition, String displayedIndication) {
        int color = isAddition ? Color.GREEN : Color.RED;

        ;
        SpannableString coloredIndication = new SpannableString(displayedIndication);
        coloredIndication.setSpan(new ForegroundColorSpan(color), 0, displayedIndication.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        pointsChangeIndicator.setText(coloredIndication);
    }

    private void increaseCombo() {
        isPreviousActionIsCorrectOrGameJustStarted = true;

        if (comboCounter++ == comboThreshold) {
            comboCounter = 0;

            if (currentComboIndex < comboOptions.size() - 1) {
                currentComboIndex++;
                comboDisplayChange();
            }
        }
    }

    private void resetCombo(boolean isRemoveKey) {
        comboCounter = 0;
        currentComboIndex = 0;
        comboDisplayChange();

        if (!isRemoveKey && isPreviousActionIsCorrectOrGameJustStarted) {
            comboMakeNegativeSound();
            isPreviousActionIsCorrectOrGameJustStarted = false;
        }
    }

    private void comboMakePositiveSound() {
        comboMakeSound(positiveMediaPlayer);
    }

    private void comboMakeNegativeSound() {
        comboMakeSound(negativeMediaPlayer);
    }

    private void comboMakeSound(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        setUpSounds();
    }

    private void setUpSoundOnComplete(MediaPlayer positiveMediaPlayer) {
        positiveMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });
    }

    //........................................................../
    // http://soundbible.com/tags-meow.html for more cat voices

    //........................................................../

    private void initializeWordFlagsAndPointsDefaultValue(String currentWord) {
        int length = currentWord.length();
        wordFlags = new GameWordStatus[length];
        wordPoints = new Integer[length];
        Arrays.fill(wordFlags, GameWordStatus.NO_STATUS);
        Arrays.fill(wordPoints, 0);
    }

    private void moveMarkerToNextWord(BackgroundColorSpan color) {
        Pair<String, Integer> pair = wordsMapper.get(currentWordIndex + 1);
        Integer nextWordStartIndex = pair.second;
        String nextWord = pair.first;

        int lastIndex = nextWordStartIndex + nextWord.length();

        Spannable gameTextSpannable = (Spannable) gameTextView.getText();
        gameTextSpannable.setSpan(color, nextWordStartIndex, lastIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

//        gameTextView.setText(ss);
    }

    private int getNextStartWordIndex() {
        if (++currentWordIndex == wordsMapper.size()) {
            return -1;
        }

        return wordsMapper.get(currentWordIndex).second;
    }

    private boolean isAddedKey(int before, int count) {
        return before == 0 && count == 1;
    }

    private void keyboardConfiguration(EditText editText) {
        editText.requestFocus();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setShowSoftInputOnFocus(true);
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.GameToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
    }

    public void playAgain(View view) {
        Intent intent = new Intent(GameActivity.this, GameLoadingSplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    private void setRatingBarListener() {
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        //if rating value is changed,
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, final float rating,
                                        boolean fromUser) {
                String theme = selectedTextItem.getThemeName();
                String index = selectedTextItem.getID();
                String timestamp = selectedTextItem.getDate();
                getUserStatsCollection().document(FirestoreConstants.statisticsDocument).collection(FirestoreConstants.gameResultsCollection)
                        .whereEqualTo(FirestoreConstants.themeField, theme)
                        .whereEqualTo(FirestoreConstants.textIndexField, index)
                        .whereEqualTo(FirestoreConstants.textDateField, timestamp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    boolean ratedTextPreviously = false;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Double textRating = document.getDouble(FirestoreConstants.textRatingField);
                                        if (textRating != null)
                                            ratedTextPreviously = true;
                                    }
                                    if (!ratedTextPreviously) {
                                        writeRatingIntoUserGameResult(rating);
                                        updateTextRatingInDatabase(rating);

                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });
    }

    private void writeRatingIntoUserGameResult(float rating) {
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put(FirestoreConstants.textRatingField, rating);
        String theme = selectedTextItem.getThemeName();
        String index = selectedTextItem.getID();
        getUserStatsCollection().document(FirestoreConstants.statisticsDocument).collection(FirestoreConstants.gameResultsCollection)
                .document(theme + "-" + index.toString() + "-" + gameTimeStamp.toString())
                .set(ratingMap, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "text rating successfully written into user game result!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text rating into user game result", e);
                    }
                });
    }


    private void updateTextRatingInDatabase(final float rating) {
        String theme = selectedTextItem.getThemeName();
        String index = selectedTextItem.getID();
        String timestamp = selectedTextItem.getDate();
        final String composerUid = selectedTextItem.getComposer();
        db.collection("themes").document(theme).collection("texts")
                .whereEqualTo("mainThemeID", index)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Double textRating = document.getDouble(FirestoreConstants.textRatingField);
                                Long numOfRatings = document.getLong(FirestoreConstants.numOfRatingsField);
                                if (textRating != null && numOfRatings != null) {
                                    Double newAvgRating = (numOfRatings * textRating + rating) / (numOfRatings + 1);
                                    writeTextRatingInThemesCollection(numOfRatings + 1, newAvgRating, document.getId());
                                    writeTextRatingInTextsCollection(numOfRatings + 1, newAvgRating, document.getId());
                                    writeTextRatingInComposerTextsCollection(numOfRatings + 1, newAvgRating, document.getId(), composerUid);
                                } else {
                                    writeTextRatingInThemesCollection((long) 1, (double) rating, document.getId());
                                    writeTextRatingInTextsCollection((long) 1, (double) rating, document.getId());
                                    writeTextRatingInComposerTextsCollection((long) 1, (double) rating, document.getId(), composerUid);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting text document to write rating: ", task.getException());
                        }
                    }
                });
    }

    private void updateComposerTextBestScoreAndWPM(final Double wpm, final Double score){
        db.collection("themes").document(selectedTextItem.getThemeName()).collection("texts")
                .whereEqualTo("mainThemeID", selectedTextItem.getID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Double bestScore = document.getDouble("best");
                                Double bestWPM = document.getDouble("fastestSpeed");
                                if (bestScore != null && bestWPM != null) {
                                    if(bestScore < score)
                                        writeBestScoreInComposerTextsCollection(score, document.getId());
                                    if(bestWPM < wpm)
                                        writeBestWPMInComposerTextsCollection(wpm,document.getId());
                                } else {
                                    writeBestScoreInComposerTextsCollection(score, document.getId());
                                    writeBestWPMInComposerTextsCollection(wpm,document.getId());
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting text document to write best score and wpm: ", task.getException());
                        }
                    }
                });
    }

    private void writeBestScoreInComposerTextsCollection(Double currentScore, String documentId) {
        Map<String, Object> temp = new HashMap<>();
        temp.put("best", currentScore);
        db.collection("users").document(selectedTextItem.getComposer())
                .collection("texts")
                .document(documentId)
                .set(temp, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "best score successfully updated into composer collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text best score into composer collection", e);
                    }
                });
    }
    private void writeBestWPMInComposerTextsCollection(Double currentWPM, String documentId){
        Map<String, Object> temp = new HashMap<>();
        temp.put("fastestSpeed", currentWPM);
        db.collection("users").document(selectedTextItem.getComposer())
                .collection("texts")
                .document(documentId)
                .set(temp, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "best wpm successfully updated into composer collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text best wpm into composer collection", e);
                    }
                });
    }

    private void writeTextRatingInComposerTextsCollection(Long numOfRatings, Double newAvgRating, String documentId, String composerUid) {
        db.collection("users").document(composerUid)
                .collection("texts")
                .document(documentId)
                .set(getRatingMap(numOfRatings, newAvgRating), merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "text rating successfully written into composer collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text rating into composer collection", e);
                    }
                });
    }

    private Map<String, Object> getRatingMap(Long numOfRatings, Double newAvgRating) {
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put(FirestoreConstants.textRatingField, newAvgRating);
        ratingMap.put(FirestoreConstants.numOfRatingsField, numOfRatings);
        return ratingMap;
    }

    private void writeTextRatingInThemesCollection(Long numOfRatings, Double newAvgRating, String documentId) {
        String theme = selectedTextItem.getThemeName();
        db.collection("themes").document(theme).collection("texts")
                .document(documentId)
                .set(getRatingMap(numOfRatings, newAvgRating), merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "text rating successfully written into themes collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text rating into themes collection", e);
                    }
                });
    }

    private void writeTextRatingInTextsCollection(Long numOfRatings, Double newAvgRating, String documentId) {
        db.collection("texts/").document(documentId)
                .set(getRatingMap(numOfRatings, newAvgRating), merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "text rating successfully written into texts collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text rating into texts collection", e);
                    }
                });
    }
}


