package androidCourse.technion.quickthumbs.game;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidCourse.technion.quickthumbs.GameLoadingSplashScreenActivity;
import androidCourse.technion.quickthumbs.MainUserActivity;
import androidCourse.technion.quickthumbs.R;
import androidCourse.technion.quickthumbs.multiplayerSearch.GameRoom;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ImmutableList;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static androidCourse.technion.quickthumbs.FirestoreConstants.accuracyField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.CPMField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.WPMField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.emailField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.gameResultsCollection;
import static androidCourse.technion.quickthumbs.FirestoreConstants.dateField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.numOfGamesField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.numOfRatingsField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.statisticsDocument;
import static androidCourse.technion.quickthumbs.FirestoreConstants.statsCollection;
import static androidCourse.technion.quickthumbs.FirestoreConstants.textDateField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.textIndexField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.textRatingField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.themeField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.totalScoreField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.uidField;
import static androidCourse.technion.quickthumbs.FirestoreConstants.usersCollection;
import static com.google.firebase.firestore.SetOptions.merge;

public class GameActivity extends AppCompatActivity {
    Context applicationContext;
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
    private TextView gameReportText;
    private TextView correctOutOfTotalTextView;
    private TextView correctOutOfTotalPercentageTextView;
    private TextView comboDisplayer;
    private TextView pointsChangeIndicator;
    private TextView multiPlayerCounter;
    private TextView wpmCompareNumberView;
    private TextView wpmCompareLineView;
    private FloatingActionButton closingPodiumButton;
    private ImageView onlineIndicator;
    private TextView opponentNameView;
    private RelativeLayout podiumScreen;

    private List<Pair<TextView, TextView>> podiumPlaces;

    private boolean forwardCommand;

    private int gameTextWordStart;

    private List<Pair<String, Integer>> wordsMapper;
    private int currentWordIndex;

    private GameWordStatus[] wordFlags;
    private int gameTextWordOffset;

    private final BackgroundColorSpan wordMarkerForCurrentUser = new BackgroundColorSpan(Color.rgb(255, 102, 0));
    private final BackgroundColorSpan wordMarkerForOtherGameRoomUser = new BackgroundColorSpan(Color.GRAY);

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
    private boolean passOnAfterTextChanged;
    private boolean isPreviousActionIsCorrectOrGameJustStarted;
    private boolean isVibrateOnMistakeOn = true;

    private boolean shouldStartTimer;
    private final int comboThreshold = 4;
    private Timestamp gameTimeStamp;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = GameActivity.class.getName();

    public static TextDataRow selectedTextItem;
    public boolean changed = false;

    private boolean soundsOn = false;
    MediaPlayer positiveMediaPlayer;
    MediaPlayer negativeMediaPlayer;

    private DatabaseReference roomReference;
    private String gameRoomKey;
    private int currentPlayerIndexInRoom;
    private int previousOtherPlayerIndex;
    private long startingTimeStamp;
    private Timer synchronizedMultiplayerCounter;
    private List<Integer> roomPoints;

    private TextView guidance;

    public static final int USER_NAME_MAX_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        applicationContext = getApplicationContext();

        initializeFields();

        setGameTextAndLogicAndEnding(gameTextView);

        gameLoadingLayout.setVisibility(View.VISIBLE);
        gamePlayingLayout.setVisibility(View.INVISIBLE);

        closeKeyboard();

        setUpSounds();  //any future sound features should be added here;

        gameCreationSequence();
        setRatingBarListener();
        setActionBar();
    }

    public void gameCreationSequence() {
        String gameText = gameTextView.getText().toString();
        ss = new SpannableString(gameText);
        gameTextView.setText(ss, TextView.BufferType.SPANNABLE);

        gamePlayingLayout.setVisibility(View.VISIBLE);
        gameLoadingLayout.setVisibility(View.INVISIBLE);

        if (gameRoomKey == null) {
            keyboardConfiguration(currentWordEditor);
            setEditorLogic();
        } else {
            setCountDownAndGameCounter();
            currentWordEditor.setFocusable(false);
        }

        comboDisplayChange();

        String[] words = gameText.split(" ");

        wordsMapper = setWordsMapper(words);

        currentWordIndex = -1;
        moveMarkerToNextWord(wordMarkerForCurrentUser);
        currentWordIndex = 0;

        if (gameRoomKey != null) {
            onlineIndicator.setVisibility(View.VISIBLE);
            moveUserMarkerToNextWord(wordMarkerForOtherGameRoomUser, 0, gameTextView);
            setRealTimeListenerForRoomInformationChanges();
            closingPodiumButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    podiumScreen.setVisibility(View.INVISIBLE);
                }
            });
        }

        initializeWordFlagsAndPointsDefaultValue(words[0]);
        gameTextWordOffset = 0;

        setUpSounds();  //any future sound features should be added here;
    }

    private void setCountDownAndGameCounter() {
        final boolean[] needToSetEditor = {true};
        final Handler mHandler = new Handler();
        synchronizedMultiplayerCounter = new Timer();
        synchronizedMultiplayerCounter.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int fixedAmountOfSecondsUntilMultiplayerGameStart = 10;
                        int spentTimeInSeconds = (int) ((System.currentTimeMillis() - startingTimeStamp) / 1000);

                        int timeLeftUntilGameStart = fixedAmountOfSecondsUntilMultiplayerGameStart - spentTimeInSeconds;
                        timeLeftUntilGameStart = timeLeftUntilGameStart < 0 ? (-1) * timeLeftUntilGameStart : timeLeftUntilGameStart;
                        multiPlayerCounter.setText(String.valueOf(timeLeftUntilGameStart));

                        if (timeLeftUntilGameStart == 0 && needToSetEditor[0]) {
                            currentWordEditor.setFocusableInTouchMode(true);
                            keyboardConfiguration(currentWordEditor);
                            setEditorLogic();

                            gameStartTimeStamp = System.currentTimeMillis();
                            setTimerUpdateGameStatsPresentation();

                            needToSetEditor[0] = !needToSetEditor[0];
                        }

                    }
                });
            }
        }, 0, 500);
    }

    private void setRealTimeListenerForRoomInformationChanges() {
        final Handler mHandler = new Handler();
        roomReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    return Transaction.success(mutableData);
                }

                GameRoom gameRoom = mutableData.getValue(GameRoom.class);
                boolean isOpponentOnline;
                final String opponentName;

                switch (currentPlayerIndexInRoom) {
                    case 1:
                        gameRoom.usr1Online = true;
                        opponentName = gameRoom.user2;
                        isOpponentOnline = gameRoom.usr2Online;

                        break;
                    case 2:
                        gameRoom.usr2Online = true;
                        isOpponentOnline = gameRoom.usr1Online;
                        opponentName = gameRoom.user1;

                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + currentPlayerIndexInRoom);
                }

                final int min = Math.min(USER_NAME_MAX_SIZE, opponentName.length());
                final int color = isOpponentOnline ? Color.GREEN : Color.GRAY;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Resources res = getResources();
                        final Drawable drawable = res.getDrawable(R.drawable.circle_online_indication);
                        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        onlineIndicator.setBackground(drawable);

                        opponentNameView.setText(opponentName.subSequence(0, min));
                    }
                });

                mutableData.setValue(gameRoom);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });

        roomReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                GameRoom room = dataSnapshot.getValue(GameRoom.class);
                int toPosition;
                boolean isOpponentOnline;
                int currentPlayerPoints;

                switch (currentPlayerIndexInRoom) {
                    case 1:
                        toPosition = room.location2;
                        isOpponentOnline = room.usr2Online;
                        currentPlayerPoints = room.usr1Points;

                        break;
                    case 2:
                        toPosition = room.location1;
                        isOpponentOnline = room.usr1Online;
                        currentPlayerPoints = room.usr2Points;

                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + currentPlayerIndexInRoom);
                }

                int color = isOpponentOnline ? Color.GREEN : Color.GRAY;
                Resources res = getResources();
                final Drawable drawable = res.getDrawable(R.drawable.circle_online_indication);
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                onlineIndicator.setBackground(drawable);

                ImmutableList<Integer> currentRoomPoints = ImmutableList.of(room.usr1Points, room.usr2Points);

                if (!roomPoints.equals(currentRoomPoints)) { //some player (current player too) updated end points, and current player finished game.
                    roomPoints = currentRoomPoints;

                    if (gameTextWordStart == -1) {
                        List<Pair<Integer, String>> allUsersResults = new ArrayList<>();
                        allUsersResults.add(new Pair<>(room.usr1Points, room.user1));
                        allUsersResults.add(new Pair<>(room.usr2Points, room.user2));

                        List<Pair<Pair<Integer, String>, Boolean>> podiumResults = new ArrayList<>();
                        for (Pair<Integer, String> p : allUsersResults) {
                            if (p.first != -1) {
                                boolean isCurrentUser = p.second.equals(MainUserActivity.localUserName);
                                podiumResults.add(new Pair<>(p, isCurrentUser));
                            }
                        }

                        Collections.sort(podiumResults, new Comparator<Pair<Pair<Integer, String>, Boolean>>() {
                            @Override
                            public int compare(Pair<Pair<Integer, String>, Boolean> o1, Pair<Pair<Integer, String>, Boolean> o2) {
                                return o2.first.first - o1.first.first;
                            }
                        });

                        updatePodium(podiumResults);
                    }

                    return;
                }

                TextView textToChange = gameTextWordStart == -1 ? gameReportText : gameTextView;

                moveUserMarkerToNextWord(wordMarkerForOtherGameRoomUser, toPosition, textToChange);

                previousOtherPlayerIndex++;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePodium(List<Pair<Pair<Integer, String>, Boolean>> podiumResults) {
        if (Build.VERSION.SDK_INT >= 17) {
            LinearLayout layout = findViewById(R.id.playersResultsLayout);
            layout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        int amountOfPlayersOnPodium = Math.min(3, podiumResults.size());

        for (int i = 0; i < amountOfPlayersOnPodium; i++) {
            Pair<Pair<Integer, String>, Boolean> pairBooleanPair = podiumResults.get(i);
            Pair<Integer, String> p = pairBooleanPair.first;
            Boolean isCurrentUser = pairBooleanPair.second;
            Integer points = p.first;
            String name = p.second;

            Pair<TextView, TextView> viewPair = podiumPlaces.get(i);
            TextView nameView = viewPair.first;
            TextView pointsView = viewPair.second;

            if (Build.VERSION.SDK_INT >= 17) {
                nameView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                pointsView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }

            int colorPrimitive = isCurrentUser ? Color.GREEN : Color.BLACK;
            ForegroundColorSpan color = new ForegroundColorSpan(colorPrimitive);

            //String nameToPresent = name.substring(0, Math.min(name.length(), USER_NAME_MAX_SIZE));
            SpannableString ss = new SpannableString(name);

            ss.setSpan(color, 0, name.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            nameView.setText(ss);

            pointsView.setText(String.valueOf(points));
        }

        if (podiumScreen.getVisibility() == View.INVISIBLE) {
            podiumScreen.setVisibility(View.VISIBLE);
        }

        Button playAgainButton = findViewById(R.id.playAgainButton);
        if (playAgainButton != null) {
            playAgainButton.setVisibility(View.INVISIBLE);
        }

        YoYo.with(Techniques.Landing)
                .duration(1500)
//                .onEnd()
                .playOn(podiumScreen);

        for (int i = 0; i < amountOfPlayersOnPodium; i++) {
            Pair<TextView, TextView> viewPair = podiumPlaces.get(i);
            TextView nameView = viewPair.first;
//            TextView pointsView = viewPair.second;

            Techniques techniques = i == 0 ? Techniques.Bounce : Techniques.Shake;

            YoYo.with(techniques)
                    .delay(2000)
                    .duration(600)
                    .repeat(100)
                    .playOn(nameView);
        }
    }

    private void updateRemoteUserPosition() {
        roomReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                GameRoom room = mutableData.getValue(GameRoom.class);

                switch (currentPlayerIndexInRoom) {
                    case 1:
                        room.location1++;

                        break;
                    case 2:
                        room.location2++;

                        break;
                }

                mutableData.setValue(room);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
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
        if (soundsOn) {
            double randomDouble = Math.random();
            randomDouble = randomDouble * 3;
            int randomInt = (int) randomDouble;

            assert (randomInt >= 0 && randomInt <= 2);

            positiveMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.typing_sound);
            negativeMediaPlayer = MediaPlayer.create(getApplicationContext(), catNoises.get(randomInt));
        }
    }

    private void setUpSoundsOnComplete() {
        if (soundsOn) {
            setUpSoundOnComplete(positiveMediaPlayer);
            setUpSoundOnComplete(negativeMediaPlayer);
        }
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

        if (gameRoomKey == null) {
            long inactiveDeltaTime = System.currentTimeMillis() - gameStopTimeStamp;
            gameStartTimeStamp += inactiveDeltaTime;
        }

        setUpSounds();
    }

    @Override
    protected void onStop() {
        gameStopTimeStamp = System.currentTimeMillis();
        if (gameRoomKey != null) {
            synchronizedMultiplayerCounter.cancel();
            removeRoomIfNeeded();
        }

        super.onStop();
    }

    private void removeRoomIfNeeded() {
        roomReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    return Transaction.success(mutableData);
                }

                GameRoom gameRoom = mutableData.getValue(GameRoom.class);
                boolean isOtherUserOnline;
                switch (currentPlayerIndexInRoom) {
                    case 1:
                        isOtherUserOnline = gameRoom.usr2Online;

                        break;
                    case 2:
                        isOtherUserOnline = gameRoom.usr1Online;

                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + currentPlayerIndexInRoom);
                }

                if (!isOtherUserOnline) {
                    mutableData.setValue(null);
                } else {
                    switch (currentPlayerIndexInRoom) {
                        case 1:
                            gameRoom.usr1Online = false;

                            break;
                        case 2:
                            gameRoom.usr2Online = false;

                            break;
                    }

                    mutableData.setValue(gameRoom);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }

    @Override
    protected void onPause() {
        gameStopTimeStamp = System.currentTimeMillis();
        super.onPause();
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
//            Log.d(TAG, "being set");
            gameTextView.setText(i.getExtras().getString("text"));
            String id = i.getExtras().getString("id");
            String title = i.getExtras().getString("title");
            String text = i.getExtras().getString("text");
            String composer = i.getExtras().getString("composer");
            String theme = i.getExtras().getString("theme");
            String date = i.getExtras().getString("date");
            Double rating = i.getExtras().getDouble("rating");
            String numberOfTimesPlayed = i.getExtras().getString("playCount");
            String bestScore = i.getExtras().getString("bestScore");
            String fastestSpeed = i.getExtras().getString("fastestSpeed");
            String roomKey = i.getExtras().getString("roomKey");
            int indexInRoom = i.getExtras().getInt("indexInRoom");
            long startingTimeStampLocal = i.getExtras().getLong("startingTimeStamp", 0);

            selectedTextItem = new TextDataRow(id, title, theme, text, date, composer,
                    rating, numberOfTimesPlayed, bestScore, fastestSpeed, false, false, roomKey, null, indexInRoom);

            gameRoomKey = roomKey;
            currentPlayerIndexInRoom = indexInRoom;
            startingTimeStamp = startingTimeStampLocal;

            if (gameRoomKey != null) {
                roomReference = FirebaseDatabase.getInstance().getReference().child("searchAndGame").child("GameRooms").child(gameRoomKey);
            }

            changed = true;
        }
        if (changed == true) {
            gameTextView.setText(selectedTextItem.getText());
            changed = false;
        }
    }

    private void initializeFields() {
        comboCounter = 0;
        collectedPoints = 0;
        comboOptions = ImmutableList.of(1, 2, 4, 8, 10);
        roomPoints = ImmutableList.of(-1, -1);
        currentComboIndex = 0;
        shouldStartTimer = true;
        correctKeysAmount = 0;
        gameTextWordStart = 0;
        gameStopTimeStamp = 0;
        previousOtherPlayerIndex = 0;
        needClearance = false;
        forwardCommand = true;
        passOnAfterTextChanged = false;
        isPreviousActionIsCorrectOrGameJustStarted = true;

        currentWordEditor = findViewById(R.id.currentWord);
        gameTextView = findViewById(R.id.displayText);
        pointTextView = findViewById(R.id.pointsValue);
        wpmTextView = findViewById(R.id.WPMValue);
        cpmTextView = findViewById(R.id.CPMValue);
        gameLoadingLayout = findViewById(R.id.gameLoadingLayout);
        gamePlayingLayout = findViewById(R.id.gameTextLayout);
        gameReportLayout = findViewById(R.id.gameReportLayout);
        gameReportText = findViewById(R.id.gameReportTextLayout);
        correctOutOfTotalTextView = findViewById(R.id.correctOutOfTotalTextView);
        correctOutOfTotalPercentageTextView = findViewById(R.id.correctOutOfTotalPercentageTextView);
        comboDisplayer = findViewById(R.id.comboDisplayer);
        pointsChangeIndicator = findViewById(R.id.changeIndicator);
        multiPlayerCounter = findViewById(R.id.multiPlayerCounter);
        closingPodiumButton = findViewById(R.id.closingPodiumButton);
        wpmCompareNumberView = findViewById(R.id.wpmCompareNumber);
        wpmCompareLineView = findViewById(R.id.wpmCompareLine);
        onlineIndicator = findViewById(R.id.onlineIndicator);
        opponentNameView = findViewById(R.id.opponentName);

        podiumScreen = findViewById(R.id.podiumScreen);

        guidance = findViewById(R.id.guidance);

        podiumPlaces = new ArrayList<>();

        podiumPlaces.add(new Pair<TextView, TextView>((TextView) findViewById(R.id.placement1), (TextView) findViewById(R.id.placement1Points)));
        podiumPlaces.add(new Pair<TextView, TextView>((TextView) findViewById(R.id.placement2), (TextView) findViewById(R.id.placement2Points)));
        podiumPlaces.add(new Pair<TextView, TextView>((TextView) findViewById(R.id.placement3), (TextView) findViewById(R.id.placement3Points)));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        gameTimeStamp = new Timestamp(new Date());
    }

    private void setEditorLogic() {
        currentWordEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Log.d(TAG, "beforeTextChanged start ...");

                if (shouldStartTimer && gameRoomKey == null) {
                    gameStartTimeStamp = System.currentTimeMillis();
                    setTimerUpdateGameStatsPresentation();
                    shouldStartTimer = false;
                }

//                Log.d(TAG, "beforeTextChanged finish ...");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.d(TAG, "onTextChanged start ...");

                if (!currentWordEditor.hasFocus()) {
//                    Log.d(TAG, "onTextChanged finished because of s.clear() ...");
                    passOnAfterTextChanged = true;
                    currentWordEditor.requestFocus();

                    return;
                }

                if (isAddedKey(before, count)) {
                    logicOnAddedKey(s, start);
                } else {    //removed key
                    forwardCommand = false;
                    logicOnRemovingKey();
                }

//                Log.d(TAG, "onTextChanged finish ...");
            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.d(TAG, "afterTextChanged start ...");

                if (passOnAfterTextChanged) {
//                    Log.d(TAG, "afterTextChanged finished because of s.clear() ...");
                    passOnAfterTextChanged = false;

                    return;
                }

                if (forwardCommand) {
                    gameTextWordOffset++;

                    if (!needClearance) {
                        paintGameTextBasedOnWordFlags();
                    } else {
                        currentWordEditor.clearFocus();
                        s.clear();
                        needClearance = false;

                        if (currentWordIndex + 1 != wordsMapper.size()) {
                            moveMarkerToNextWord(wordMarkerForCurrentUser);
                            spaceKeyIncreaseCorrectKeysWhenFullyCorrectWordTyped();
                        }

                        paintGameTextBasedOnWordFlags();

                        gameTextWordOffset = 0;
                        gameTextWordStart = getNextStartWordIndex();

                        if (gameTextWordStart != -1) {
                            String currentExpectedWord = wordsMapper.get(currentWordIndex).first;
                            initializeWordFlagsAndPointsDefaultValue(currentExpectedWord);

                            if (gameRoomKey != null) {
                                updateRemoteUserPosition();
                            }

                            setGuidanceText(currentExpectedWord);

                        } else {
                            turnOffGuidance();

                            finishGame();
                        }
                    }

                } else {
                    if (gameTextWordOffset != 0) {
                        gameTextWordOffset--;
                    }

                    paintGameTextBasedOnWordFlags();

                    forwardCommand = true;
                }

//                Log.d(TAG, "afterTextChanged finish ...");
            }

        });
    }

    private void turnOffGuidance() {
        guidance.setVisibility(View.INVISIBLE);
    }

    private void setGuidanceText(String currentExpectedWord) {
        if (currentExpectedWord == wordsMapper.get(wordsMapper.size() - 1).first) {
            guidance.setText("Press space to finish game");
            guidance.setVisibility(View.VISIBLE);
            guidance.setTextSize(17);
            guidance.setBackground(getResources().getDrawable(R.color.secondaryLightColor));
            guidance.setTextColor(getResources().getColor(R.color.secondaryTextColor));
        } else {
            guidance.setText("Press space to move to the next word");
            guidance.setTextColor(getResources().getColor(R.color.secondaryColor));
            guidance.setVisibility(View.VISIBLE);
        }
    }

    private void spaceKeyIncreaseCorrectKeysWhenFullyCorrectWordTyped() {
        boolean giveExtraCorrectCharacterForSpacePress = true;

        for (GameWordStatus status : wordFlags) {
            if (!status.equals(GameWordStatus.CORRECT) && !status.equals(GameWordStatus.CORRECT_BUT_BEEN_HERE_BEFORE)) {
                giveExtraCorrectCharacterForSpacePress = false;

                break;
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
            if (currentWordIndex + 1 == wordsMapper.size()) {
                Spannable gameTextSpannable = (Spannable) gameTextView.getText();
                gameTextSpannable.removeSpan(wordMarkerForCurrentUser);
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

        if (gameRoomKey != null) {
            synchronizedMultiplayerCounter.cancel();
            updateUserRealTimeData(collectedPoints);
        }
        String wpm = wpmTextView.getText().toString();
        setWpmIndicationInReportScreen(wpm);

        closeKeyboard();

        gameReportLayout.setVisibility(View.VISIBLE);
        gamePlayingLayout.setVisibility(View.INVISIBLE);

        gameReportText.setText(gameTextView.getText(), TextView.BufferType.SPANNABLE);

        int totalCharacters = gameTextView.getText().toString().length();

        correctOutOfTotalTextView.setText(String.format("%s/%s", correctKeysAmount, totalCharacters));

        int correctPercentage = (int) (((float) correctKeysAmount / (float) totalCharacters) * 100f);

        correctOutOfTotalPercentageTextView.setText(correctPercentage + "%");


        updateUserStatistics(correctPercentage);
    }

    public DocumentReference getUserDocument() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(applicationContext);
        String users = "users";

        if (mAuth.getCurrentUser() != null) {
            return db.collection(users)
                    .document(mAuth.getUid());
        } else if (googleAccount != null) {
            return db.collection(users)
                    .document(googleAccount.getId());
        } else {
            return db.collection(users)
                    .document(accessToken.getUserId());
        }
    }

    private void setWpmIndicationInReportScreen(final String currentGameWpn) {
        getUserDocument().collection("stats").document("statistics").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
//                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                Double avgWpm = document.getDouble("avgWPM");
                                setSpeedComparedNumber(currentGameWpn, String.valueOf(avgWpm));

                            } else {
//                                Log.d(TAG, "No such document - reading statistics");
                            }
                        } else {
//                            Log.d(TAG, "reading statistics failed with ", task.getException());
                        }
                    }
                });
    }

    private void setSpeedComparedNumber(String currentGameWpm, String userAverageWpm) {
        Integer currentWpm = Integer.valueOf(currentGameWpm);
        double wpmDouble = Double.parseDouble(userAverageWpm);
        int previousWpnAverage = (int) wpmDouble;
        int wpmCompareNumber = currentWpm - previousWpnAverage;
        String displayedNumber = String.valueOf(Math.abs(wpmCompareNumber));
        SpannableString spannableString = SpannableString.valueOf(displayedNumber);
        ForegroundColorSpan color;
        String textIndication;
        Techniques techniques;

        if (wpmCompareNumber >= 0) {
            color = new ForegroundColorSpan(Color.GREEN);
            textIndication = "Better than your average by ";
            techniques = Techniques.Bounce;
        } else {
            color = new ForegroundColorSpan(Color.RED);
            textIndication = "Worse than your average by ";
            techniques = Techniques.Shake;
        }

        spannableString.setSpan(color, 0, displayedNumber.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        wpmCompareNumberView.setText(spannableString);
        wpmCompareLineView.setText(textIndication);

        YoYo.with(techniques).repeat(1000).playOn(wpmCompareNumberView);
    }

    private void updateUserRealTimeData(final int collectedPoints) {
        roomReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    return Transaction.success(mutableData);
                }

                GameRoom gameRoom = mutableData.getValue(GameRoom.class);

                switch (currentPlayerIndexInRoom) {
                    case 1:
                        gameRoom.usr1Points = collectedPoints;

                        break;
                    case 2:
                        gameRoom.usr2Points = collectedPoints;

                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + currentPlayerIndexInRoom);
                }

                mutableData.setValue(gameRoom);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }

    private void updateUserStatistics(int correctPercentage) {
        ((TextView) (findViewById(R.id.reportWPMValue))).setText(wpmTextView.getText());
        ((TextView) (findViewById(R.id.reportCPMValue))).setText(cpmTextView.getText());
        ((TextView) (findViewById(R.id.reportPointsValue))).setText(pointTextView.getText());

        Double wpm = Double.valueOf(wpmTextView.getText().toString());
        Double cpm = Double.valueOf(cpmTextView.getText().toString());
        Double points = Double.valueOf(pointTextView.getText().toString());
        updateUserStats(Double.valueOf(correctPercentage), wpm, cpm, points);
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
//                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
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
//                                Log.d(TAG, "No such document");
                                writeToUserStatistics(1, accuracy, wpm, cpm, points);
                                writeGameResult(wpm, cpm, accuracy, points);
                            }
                        } else {
//                            Log.d(TAG, "get failed with ", task.getException());
                            writeToUserStatistics(1, accuracy, wpm, cpm, points);
                            writeGameResult(wpm, cpm, accuracy, points);
                        }
                    }
                });
    }

    private void writeGameResult(Double WPM, Double CPM, Double accuracy, Double points) {
        String theme = selectedTextItem.getThemeName();
        String index = selectedTextItem.getTextId();
        String timestamp = selectedTextItem.getDate();

        Map<String, Object> updatedStatistics = new HashMap<>();
        updatedStatistics.put(uidField, getUid());
        if (mAuth.getCurrentUser() != null) {
            updatedStatistics.put(emailField, mAuth.getCurrentUser().getEmail());
        }
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
//                        Log.d(TAG, "user game result successfully written!");
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
        updatedStatistics.put(uidField, getUid());
        if (mAuth.getCurrentUser() != null) {
            updatedStatistics.put(emailField, mAuth.getCurrentUser().getEmail());
        }
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
//                        Log.d(TAG, "user average statistics successfully written!");
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
                    .document(getUid()).collection(statsCollection);
        } else {
            return db.collection(usersCollection)
                    .document(getUid()).collection(statsCollection);
        }
//        return null;
    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (currentUser != null) {
            return mAuth.getUid();
        } else if (account != null) {
            return account.getId();
        } else {
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
        if (isVibrateOnMistakeOn) {
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

        if (++comboCounter == comboThreshold) {
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
        if (soundsOn) {
            comboMakeSound(positiveMediaPlayer);
        }
    }

    private void comboMakeNegativeSound() {
        if (soundsOn) {
            comboMakeSound(negativeMediaPlayer);
        }
    }

    private void comboMakeSound(MediaPlayer mediaPlayer) {
        if (soundsOn) {
            mediaPlayer.start();
            setUpSounds();
        }
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
        moveUserMarkerToNextWord(color, currentWordIndex + 1, gameTextView);
    }

    private void moveUserMarkerToNextWord(BackgroundColorSpan color, int toIndex, TextView textViewSpannableToUpdate) {
        Pair<String, Integer> pair = wordsMapper.get(toIndex);
        Integer nextWordStartIndex = pair.second;
        String nextWord = pair.first;

        int lastIndex = nextWordStartIndex + nextWord.length();

        Spannable gameTextSpannable = (Spannable) textViewSpannableToUpdate.getText();
        gameTextSpannable.setSpan(color, nextWordStartIndex, lastIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
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
                String index = selectedTextItem.getTextId();
                String timestamp = selectedTextItem.getDate();
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
//                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Double textRating = document.getDouble(textRatingField);
                                        if (textRating != null)
                                            ratedTextPreviously = true;
                                    }
                                    if (!ratedTextPreviously) {
                                        writeRatingIntoUserGameResult(rating);
                                        updateTextRatingInDatabase(rating);

                                    }
                                } else {
//                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });
    }

    private void writeRatingIntoUserGameResult(float rating) {
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put(textRatingField, rating);
        String theme = selectedTextItem.getThemeName();
        String index = selectedTextItem.getTextId();
        getUserStatsCollection().document(statisticsDocument).collection(gameResultsCollection)
                .document(theme + "-" + index.toString() + "-" + gameTimeStamp.toString())
                .set(ratingMap, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "text rating successfully written into user game result!");
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
        String index = selectedTextItem.getTextId();
        String timestamp = selectedTextItem.getDate();
        final String composerUid = selectedTextItem.getComposer();
        db.collection("themes").document(theme).collection("texts")
                .document(index)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult()!=null) {
                            DocumentSnapshot textDoc = task.getResult();
                            Double textRating = textDoc.getDouble(textRatingField);
                            Long numOfRatings = textDoc.getLong(numOfRatingsField);
                            if (textRating != null && numOfRatings != null) {
                                Double newAvgRating = (numOfRatings * textRating + rating) / (numOfRatings + 1);
                                writeTextRatingInThemesCollection(numOfRatings + 1, newAvgRating, textDoc.getId());
                                writeTextRatingInTextsCollection(numOfRatings + 1, newAvgRating, textDoc.getId());
                                writeTextRatingInComposerTextsCollection(numOfRatings + 1, newAvgRating, textDoc.getId(), composerUid);
                            } else {
                                writeTextRatingInThemesCollection((long) 1, (double) rating, textDoc.getId());
                                writeTextRatingInTextsCollection((long) 1, (double) rating, textDoc.getId());
                                writeTextRatingInComposerTextsCollection((long) 1, (double) rating, textDoc.getId(), composerUid);
                            }
                        }
                    }
                });
    }

    private void updateComposerTextBestScoreAndWPM(final Double wpm, final Double score) {
        db.collection("users").document(selectedTextItem.getComposer()).collection("texts")
                .document(selectedTextItem.getTextId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
//                            Log.d(TAG, document.getId() + " => " + document.getData());
                            Double bestScore = document.getDouble("bestScore");
                            Double bestWPM = document.getDouble("bestWpm");
                            Long playCount = document.getLong("playCount");
                            Map<String, Object> textStatistics = new HashMap<>();
                            if (bestScore != null && bestWPM != null && playCount != null) {
                                if (bestScore < score) {
                                    textStatistics.put("bestScore", score);
                                } else {
                                    textStatistics.put("bestScore", bestScore);
                                }
                                if (bestWPM < wpm) {
                                    textStatistics.put("bestWpm", wpm);
                                } else {
                                    textStatistics.put("bestWpm", bestWPM);
                                }
                                textStatistics.put("playCount", playCount + 1);
                                writeTextStatisticsIntoComposerTextsCollection(textStatistics, document.getId());
                            } else {
                                textStatistics.put("bestScore", score);
                                textStatistics.put("bestWpm", wpm);
                                textStatistics.put("playCount", 1);
                                writeTextStatisticsIntoComposerTextsCollection(textStatistics, document.getId());
                            }
                        } else {
//                            Log.d(TAG, "Error getting text document to write best score and wpm: ", task.getException());
                        }
                    }
                });
    }

    private void writeTextStatisticsIntoComposerTextsCollection(final Map<String, Object> textStatistics, String documentId) {
        db.collection("users").document(selectedTextItem.getComposer())
                .collection("texts")
                .document(documentId)
                .set(textStatistics, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateTextStatisticsGlobally(textStatistics);
//                        Log.d(TAG, "best score successfully updated into composer collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text best score into composer collection", e);
                    }
                });
    }

    private void updateTextStatisticsGlobally(Map<String, Object> textStatistics) {
        db.collection("themes").document(selectedTextItem.getThemeName()).collection("texts")
                .document(selectedTextItem.getTextId())
                .set(textStatistics, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "best score successfully updated into themes collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text best score into themes collection", e);
                    }
                });

        db.collection("texts").document(selectedTextItem.getTextId())
                .set(textStatistics, merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "best score successfully updated into texts collection!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing text best score into texts collection", e);
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
//                        Log.d(TAG, "text rating successfully written into composer collection!");
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
        String theme = selectedTextItem.getThemeName();
        db.collection("themes").document(theme).collection("texts")
                .document(documentId)
                .set(getRatingMap(numOfRatings, newAvgRating), merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "text rating successfully written into themes collection!");
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
        db.collection("texts").document(documentId)
                .set(getRatingMap(numOfRatings, newAvgRating), merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "text rating successfully written into texts collection!");
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


