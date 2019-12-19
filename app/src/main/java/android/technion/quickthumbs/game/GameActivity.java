package android.technion.quickthumbs.game;

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
import android.technion.quickthumbs.R;
import android.technion.quickthumbs.TextPoll;
import android.text.Editable;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableList;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.technion.quickthumbs.FirestoreConstants.accuracyField;
import static android.technion.quickthumbs.FirestoreConstants.CPMField;
import static android.technion.quickthumbs.FirestoreConstants.WPMField;
import static android.technion.quickthumbs.FirestoreConstants.emailField;
import static android.technion.quickthumbs.FirestoreConstants.gameResultsCollection;
import static android.technion.quickthumbs.FirestoreConstants.dateField;
import static android.technion.quickthumbs.FirestoreConstants.numOfGamesField;
import static android.technion.quickthumbs.FirestoreConstants.numOfRatingsField;
import static android.technion.quickthumbs.FirestoreConstants.statisticsDocument;
import static android.technion.quickthumbs.FirestoreConstants.statsCollection;
import static android.technion.quickthumbs.FirestoreConstants.textDateField;
import static android.technion.quickthumbs.FirestoreConstants.textIndexField;
import static android.technion.quickthumbs.FirestoreConstants.textRatingField;
import static android.technion.quickthumbs.FirestoreConstants.themeField;
import static android.technion.quickthumbs.FirestoreConstants.totalScoreField;
import static android.technion.quickthumbs.FirestoreConstants.uidField;
import static android.technion.quickthumbs.FirestoreConstants.usersCollection;
import static com.google.firebase.firestore.SetOptions.merge;

public class GameActivity extends AppCompatActivity {
    private EditText currentWordEditor;
    private Boolean needClearance;
    private TextView gameTextView;
    private TextView pointTextView;
    private TextView wpmTextView;
    private TextView cpmTextView;
    private SpannableString ss;
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

    private boolean shouldStartTimer;
    private final int comboThreshold = 4;
    private Timestamp gameTimeStamp;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = GameActivity.class.getName();

    private List<Pair<Pair<String, Integer>, QueryDocumentSnapshot>> selectedThemeAndTextIndex; //is assigned in TextPoll

    MediaPlayer positiveMediaPlayer;
    MediaPlayer negativeMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializeFields();
        currentWordEditor.setActivated(false);
        gameLoadingLayout.setVisibility(View.VISIBLE);
        gamePlayingLayout.setVisibility(View.INVISIBLE);

        closeKeyboard();

        setUpSounds();  //any future sound features should be added here;

        setGameTextAndLogicAndEnding(gameTextView);
        setRatingBarListener();
    }

    public void gameCreationSequence() {

        ss = new SpannableString(gameTextView.getText().toString());

        gamePlayingLayout.setVisibility(View.VISIBLE);
        gameLoadingLayout.setVisibility(View.INVISIBLE);

        keyboardConfiguration(currentWordEditor);

        setEditorLogic();

        comboDisplayChange();

        CharSequence text = gameTextView.getText();

        String gameText = String.valueOf(text);
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
        gameTimer.scheduleAtFixedRate(new TimerTask() {
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
        }, 500, 500);
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

        fetchText(gameTextView);
    }

    private void fetchText(TextView gameTextView) {
        TextPoll.initiateCustomizeTextFetch(gameTextView, this, selectedThemeAndTextIndex);
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
        selectedThemeAndTextIndex = new ArrayList<>();
    }

    private void setEditorLogic() {
        currentWordEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (shouldStartTimer) {
                    setTimerUpdateGameStatsPresentation();
                    shouldStartTimer = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAddedKey(before, count)) {
                    logicOnAddedKey(s, start);
                } else {    //removed key
                    forwardCommand = false;
                    logicOnRemovingKey();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentWordEditor.removeTextChangedListener(this);

                if (forwardCommand) {
                    paintEditorTextBasedOnWordFlags(true);
                    gameTextWordOffset++;
                } else {
                    if (gameTextWordOffset != 0) {
                        gameTextWordOffset--;
                    }

                    paintEditorTextBasedOnWordFlags(false);

                    forwardCommand = true;
                }

                currentWordEditor.addTextChangedListener(this);

                if (needClearance) {
                    if (currentWordIndex + 1 != wordsMapper.size()) {
                        spaceKeyIncreaseCorrectKeysWhenFullyCorrectWordTyped();
                    }

                    currentWordEditor.removeTextChangedListener(this);
                    currentWordEditor.getText().clear();
                    currentWordEditor.setSelection(0);
                    currentWordEditor.addTextChangedListener(this);

                    paintGameTextBasedOnWordFlags();

                    gameTextWordOffset = 0;
                    gameTextWordStart = getNextStartWordIndex();

                    if (gameTextWordStart != -1) {
                        String currentExpectedWord = wordsMapper.get(currentWordIndex).first;
                        initializeWordFlagsAndPointsDefaultValue(currentExpectedWord);

                        needClearance = false;
                    } else {
                        finishGame();
                    }
                }
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

        for (int i = textStartingIndex; i < wordFlags.length + textStartingIndex; i++) {
            GameWordStatus status = wordFlags[i - textStartingIndex];

            switch (status) {
                case CORRECT:
                    ForegroundColorSpan green = new ForegroundColorSpan(Color.GREEN);
                    ss.setSpan(green, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;

                case NO_STATUS:
                case WRONG:
                case ALREADY_SEEN:
                    ForegroundColorSpan red = new ForegroundColorSpan(Color.RED);
                    ss.setSpan(red, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;

                case CORRECT_BUT_BEEN_HERE_BEFORE:
                    ForegroundColorSpan yellow = new ForegroundColorSpan(Color.YELLOW);
                    ss.setSpan(yellow, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    break;
            }
        }

        gameTextView.setText(ss);
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
                moveMarkerToNextWord(colorBackGround);
            } else {
                ss.removeSpan(colorBackGround);
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

        updateUserStats(Double.valueOf(correctPercentage),
                Double.valueOf(wpmTextView.getText().toString()),
                Double.valueOf(cpmTextView.getText().toString()),
                Double.valueOf(pointTextView.getText().toString()));
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
                                Long numOfGames = document.getLong(numOfGamesField);
                                Double newAvgAccuracy =
                                        calcNewAvgAfterAddingElement(document.getDouble(accuracyField), numOfGames, accuracy);
                                Double newAvgWPM =
                                        calcNewAvgAfterAddingElement(document.getDouble(WPMField), numOfGames, wpm);
                                Double newAvgCPM =
                                        calcNewAvgAfterAddingElement(document.getDouble(CPMField), numOfGames, cpm);
                                Double newTotalScore =
                                        document.getDouble(totalScoreField) + points;
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
        String theme = (selectedThemeAndTextIndex.get(0)).first.first;
        Integer index = (selectedThemeAndTextIndex.get(0)).first.second;
        String timestamp = (selectedThemeAndTextIndex.get(0)).second.getString("date");

        Map<String, Object> updatedStatistics = new HashMap<>();
        updatedStatistics.put(uidField, mAuth.getUid());
        updatedStatistics.put(emailField, mAuth.getCurrentUser().getEmail());
        updatedStatistics.put(dateField, gameTimeStamp);
        updatedStatistics.put(CPMField, CPM);
        updatedStatistics.put(accuracyField, accuracy);
        updatedStatistics.put(WPMField, WPM);
        updatedStatistics.put(totalScoreField, points);
        updatedStatistics.put(themeField, theme);
        updatedStatistics.put(textIndexField, index);
        updatedStatistics.put(textDateField, timestamp);

        getUserStatsCollection().document(statisticsDocument).collection(gameResultsCollection)
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
        updatedStatistics.put(uidField, mAuth.getUid());
        updatedStatistics.put(emailField, mAuth.getCurrentUser().getEmail());
        updatedStatistics.put(dateField, gameTimeStamp);
        updatedStatistics.put(numOfGamesField, numOfGames);
        updatedStatistics.put(CPMField, newAvgCPM);
        updatedStatistics.put(accuracyField, newAvgAccuracy);
        updatedStatistics.put(WPMField, newAvgWPM);
        updatedStatistics.put(totalScoreField, newTotalScore);

        getUserStatsCollection().document(statisticsDocument).set(updatedStatistics)
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
            return db.collection(usersCollection)
                    .document(mAuth.getUid()).collection(statsCollection);
        }
        return null;
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void paintEditorTextBasedOnWordFlags(boolean isForwardTyping) {
        Editable text = currentWordEditor.getText();
        SpannableStringBuilder mutableSpannable = new SpannableStringBuilder();

        String wordText = text.toString();

        for (int i = 0; i < wordText.length(); i++) {
            SpannableString immutableSpannable;

            if (i >= wordFlags.length) {
                ForegroundColorSpan red = new ForegroundColorSpan(Color.RED);

                String suffix = wordText.substring(i);
                immutableSpannable = new SpannableString(suffix);
                immutableSpannable.setSpan(red, 0, suffix.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                mutableSpannable.append(immutableSpannable);

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

            mutableSpannable.append(immutableSpannable);
        }

        currentWordEditor.setText(mutableSpannable);

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
            return;
        }

        if (pressedKey.equals(expectedKey)) {
            positiveLogicOnAddedCorrectKeyToWord();
        } else {
            resetCombo(false);
            wordFlags[gameTextWordOffset] = GameWordStatus.WRONG;
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
        ss.setSpan(color, nextWordStartIndex, lastIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        gameTextView.setText(ss);
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
        if (inputMethodManager != null){
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }

        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.GameToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    public void playAgain(View view) {
        finish();
        Intent intent = new Intent(GameActivity.this, GameActivity.class);
        startActivity(intent);
    }

    private void setRatingBarListener() {
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        //if rating value is changed,
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, final float rating,
                                        boolean fromUser) {
                String theme = (selectedThemeAndTextIndex.get(0)).first.first;
                Integer index = (selectedThemeAndTextIndex.get(0)).first.second;
                String timestamp = (selectedThemeAndTextIndex.get(0)).second.getString("date");
                getUserStatsCollection().document(statisticsDocument).collection(gameResultsCollection)
                        .whereEqualTo(themeField, theme)
                        .whereEqualTo(textIndexField, index)
                        .whereEqualTo(textDateField, timestamp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    boolean ratedTextPreviously = false;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Double textRating = document.getDouble(textRatingField);
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
        ratingMap.put(textRatingField, rating);
        String theme = (selectedThemeAndTextIndex.get(0)).first.first;
        Integer index = (selectedThemeAndTextIndex.get(0)).first.second;
        getUserStatsCollection().document(statisticsDocument).collection(gameResultsCollection)
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
        String theme = (selectedThemeAndTextIndex.get(0)).first.first;
        Integer index = (selectedThemeAndTextIndex.get(0)).first.second;
        final String composerUid = selectedThemeAndTextIndex.get(0).second.getString("composer");
        db.collection("themes").document(theme).collection("texts")
                .whereEqualTo("mainThemeID", index)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Double textRating = document.getDouble(textRatingField);
                                Long numOfRatings = document.getLong(numOfRatingsField);
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

    private void writeTextRatingInComposerTextsCollection(Long numOfRatings, Double newAvgRating, String documentId, String composerUid) {
        int x = 1;
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
        ratingMap.put(textRatingField, newAvgRating);
        ratingMap.put(numOfRatingsField, numOfRatings);
        return ratingMap;
    }

    private void writeTextRatingInThemesCollection(Long numOfRatings, Double newAvgRating, String documentId) {
        String theme = (selectedThemeAndTextIndex.get(0)).first.first;
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


