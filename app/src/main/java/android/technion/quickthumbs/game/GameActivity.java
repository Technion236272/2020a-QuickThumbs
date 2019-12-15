package android.technion.quickthumbs.game;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.technion.quickthumbs.R;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    private EditText currentWordEditor;
    private Boolean needClearance;
    private TextView gameTextView;
    private TextView pointTextView;
    private TextView wpmTextView;
    private TextView cpmTextView;
    private SpannableString ss;
    private RelativeLayout gamePlayingLayout;
    private RelativeLayout gameReportLayout;
    private TextView gameReportText;
    private TextView correctOutOfTotalTextView;
    private TextView correctOutOfTotalPercentageTextView;
    private TextView comboDisplayer;

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

    private int collectedPoints;
    private Integer[] wordPoints;
    private List<Integer> comboOptions;
    private int currentComboIndex;
    private int comboCounter;

    private boolean shouldStartTimer;
    private final int comboThreshold = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializeFields();

        keyboardConfiguration(currentWordEditor);

        setEditorLogic();

        setGameText(gameTextView);

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
    }

    private void comboDisplayChange() {
        comboDisplayer.setText("X" + comboOptions.get(currentComboIndex));
    }

    private void setTimerUpdateGameStatsPresentation() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long timePassedFromStartGame = System.currentTimeMillis() - gameStartTimeStamp;

                int cpm = (int) (((double) correctKeysAmount / (double) timePassedFromStartGame) * 1000d * 60d);
                String cpmString = String.valueOf(cpm);

                pointTextView.setText(String.valueOf(collectedPoints));
                wpmTextView.setText(String.valueOf(cpm / 5));
                cpmTextView.setText(cpmString);
            }
        }, 500, 500);
    }

    @Override
    protected void onStart() {
        super.onStart();
        long inactiveDeltaTime = System.currentTimeMillis() - gameStopTimeStamp;

        gameStartTimeStamp += inactiveDeltaTime;
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

    private void setGameText(TextView gameTextView) {
        String gameText = fetchText();

        ss = new SpannableString(gameText);

        gameTextView.setText(ss);
    }

    private String fetchText() {
        return "Remember when you were young You shone like the Sun Shine on, you crazy diamond Now there's a look in your eyes Like black holes in the sky Shine on, you crazy diamond";
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
        currentWordEditor = findViewById(R.id.currentWord);
        gameTextView = findViewById(R.id.displayText);
        pointTextView = findViewById(R.id.pointsValue);
        wpmTextView = findViewById(R.id.WPMValue);
        cpmTextView = findViewById(R.id.CPMValue);
        gamePlayingLayout = findViewById(R.id.gameTextLayout);
        gameReportLayout = findViewById(R.id.gameReportLayout);
        gameReportText = findViewById(R.id.gameReportTextLayout);
        correctOutOfTotalTextView = findViewById(R.id.correctOutOfTotalTextView);
        correctOutOfTotalPercentageTextView = findViewById(R.id.correctOutOfTotalPercentageTextView);
        comboDisplayer = findViewById(R.id.comboDisplayer);
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
            correctKeysAmount++;
            increaseCombo();
            increasePoints(gameTextWordOffset - 1, false);
        }
    }

    private void increasePoints(int gameTextWordOffsetLocal, boolean shouldRememberPoints) {
        Integer combo = comboOptions.get(currentComboIndex);
        int pointsToAdd = combo * 10;

        if (shouldRememberPoints) {
            wordPoints[gameTextWordOffsetLocal] = pointsToAdd;
        }

        collectedPoints += pointsToAdd;
    }

    private void increaseCombo() {
        if (comboCounter++ == comboThreshold) {
            comboCounter = 0;

            if (currentComboIndex < comboOptions.size() - 1) {
                currentComboIndex++;
                comboDisplayChange();
            }
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
        resetCombo();

        if (gameTextWordOffset < wordFlags.length) {    //will not!! get here if position in EditText is 0 and tries to remove.
            GameWordStatus wordFlag = wordFlags[gameTextWordOffset - 1];

            reducePointsBasedOnPreviousEarnings();

            if (wordFlag == GameWordStatus.CORRECT || wordFlag == GameWordStatus.CORRECT_BUT_BEEN_HERE_BEFORE) {
                correctKeysAmount--;
            }

            wordFlags[gameTextWordOffset - 1] = GameWordStatus.ALREADY_SEEN;


        }
    }

    private void reducePointsBasedOnPreviousEarnings() {
        int pointsEarnedForThisCharacterPreviously = wordPoints[gameTextWordOffset - 1];
        collectedPoints -= pointsEarnedForThisCharacterPreviously;
        wordPoints[gameTextWordOffset - 1] = 0;
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
                resetCombo();
                expectedKey = null;
            }

            updateWordFlags(pressedKey, expectedKey);
        }
    }

    private void resetCombo() {
        comboCounter = 0;
        currentComboIndex = 0;
        comboDisplayChange();
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

        correctOutOfTotalPercentageTextView.setText(String.valueOf(correctPercentage) + "%");
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

    private void updateWordFlags(String pressedKey, String expectedKey) {
        if (expectedKey == null) {
            return;
        }

        if (pressedKey.equals(expectedKey)) {
            increaseCombo();

            if (wordFlags[gameTextWordOffset].equals(GameWordStatus.NO_STATUS)) {
                wordFlags[gameTextWordOffset] = GameWordStatus.CORRECT;
                correctKeysAmount++;
            } else {
                wordFlags[gameTextWordOffset] = GameWordStatus.CORRECT_BUT_BEEN_HERE_BEFORE;
                correctKeysAmount++;
            }

            increasePoints(gameTextWordOffset, true);
        } else {
            resetCombo();
            wordFlags[gameTextWordOffset] = GameWordStatus.WRONG;
        }
    }

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

        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.GameToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
