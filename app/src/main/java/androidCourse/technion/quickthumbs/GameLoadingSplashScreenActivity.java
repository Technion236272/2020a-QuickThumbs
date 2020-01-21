package androidCourse.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidCourse.technion.quickthumbs.Utils.CacheHandler;
import androidCourse.technion.quickthumbs.game.GameActivity;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import gr.net.maroulis.library.EasySplashScreen;

import static android.widget.ImageView.ScaleType.FIT_XY;
import static androidCourse.technion.quickthumbs.Utils.CacheHandler.checkIfTextListNeedToBeRefilled;
import static androidCourse.technion.quickthumbs.Utils.CacheHandler.getNextTextFromSelectedTheme;


public class GameLoadingSplashScreenActivity extends AppCompatActivity {
    private static final String TAG = GameLoadingSplashScreenActivity.class.getSimpleName();
    final private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    final private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static String[] basicThemes = {"Comedy", "Music", "Movies", "Science", "Games", "Literature"};
    private static Map<String, Boolean> allUserThemes = new HashMap<>();
    private View easySplashScreen;
    private EasySplashScreen config;
    private CountDownTimer timer;
    private int font_size = 70;
    private int countDownFromSelectedTheme = 4000; // num_of_seconds*1000

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CacheHandler cacheHandler = new CacheHandler(getApplicationContext());
        allUserThemes = cacheHandler.loadThemesFromSharedPreferences();

        setScreenModifications();
        Intent fromWhichActivityYouCameFrom = getIntent();

        if (!fromWhichActivityYouCameFrom.hasExtra("text")) {
            if (fromWhichActivityYouCameFrom.hasExtra("id")) {
                String textId = fromWhichActivityYouCameFrom.getExtras().getString("id");
                String roomKey = fromWhichActivityYouCameFrom.getExtras().getString("roomKey");
                int indexInRoom = fromWhichActivityYouCameFrom.getExtras().getInt("indexInRoom");
                long startingTimeStamp = fromWhichActivityYouCameFrom.getExtras().getLong("startingTimeStamp");
                makeIntentFromSelectedText(textId, roomKey, indexInRoom, startingTimeStamp);
            } else {
                countDownFromSelectedTheme = 7000;
                String beforeLogoText = "Choosing From Preferred themes";
                int selected_IconId = R.drawable.game_loading_icon;
                initializeSplashScreen(countDownFromSelectedTheme, beforeLogoText, selected_IconId);
                splashScreenSettings();
                setContentView(easySplashScreen);
                new FetchRandomText().execute();
            }
        } else {
            TextDataRow selectedTextItem = setTextDataFromSelectedText(fromWhichActivityYouCameFrom);

            final Intent intent = setIntentForTheGame(selectedTextItem);

            countDownFromSelectedTheme = 4000;
            String beforeLogoText = "You choose the text: " + selectedTextItem.getTitle();
            int selected_IconId = getThemePictureId(selectedTextItem.getThemeName());
            initializeSplashScreen(countDownFromSelectedTheme, beforeLogoText, selected_IconId);

            showBestStatsOnScreen(selectedTextItem);

            splashScreenSettings();
            setContentView(easySplashScreen);

            setSplashScreenTimerToActivity(intent);
        }
    }

    private void makeIntentFromSelectedText(String id, final String roomKey, final int indexInRoom, final long startingTimeStamp) {
        db.collection("texts").document(id).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot data = task.getResult();

                    if (data == null) {
//                        Log.d(TAG, "getRandomText: another round");
                    } else {
                        TextDataRow textCardItem = TextDataRow.createTextCardItem(data, roomKey, indexInRoom, startingTimeStamp);
                        final Intent intent = setIntentForTheGame(textCardItem);
                        countDownFromSelectedTheme = 4000;
                        String beforeLogoText = "You choose the text: " + textCardItem.getTitle();
                        int selected_IconId = getThemePictureId(textCardItem.getThemeName());
                        initializeSplashScreen(countDownFromSelectedTheme, beforeLogoText, selected_IconId);

                        showBestStatsOnScreen(textCardItem);

                        splashScreenSettings();
                        setContentView(easySplashScreen);

                        setSplashScreenTimerToActivity(intent);
                    }

                } else {
//                    Log.d(TAG, "getRandomText: " + "get failed with ", task.getException());
                }
            }
        });
    }

    private void showBestStatsOnScreen(TextDataRow selectedTextItem) {
        if (Long.valueOf(selectedTextItem.getNumberOfTimesPlayed()) != 0) {
//            Log.d(TAG, selectedTextItem.getBestScore());
//            Log.d(TAG, selectedTextItem.getFastestSpeed() );

            config.withAfterLogoText("Best score on this text is " + selectedTextItem.getBestScore() + ".\n" +
                    "Fastest speed achieved on this text is " + selectedTextItem.getFastestSpeed() +
                    " words per minute");
            config.getAfterLogoTextView().setGravity(Gravity.CENTER);
        } else {
            config.withAfterLogoText("You are the first one to play on this text!");
        }
    }

    private void initializeSplashScreen(int numberOfMilliseconds, String beforeLogoText, int selected_IconId) {
        config = new EasySplashScreen(GameLoadingSplashScreenActivity.this)
                .withFullScreen()
                .withSplashTimeOut(numberOfMilliseconds)
                .withBackgroundColor(Color.parseColor("#1a1b29"))
                .withHeaderText("")
                .withFooterText("")
                .withBeforeLogoText(beforeLogoText)
                .withAfterLogoText("")
                .withLogo(selected_IconId);
    }

    private void setScreenModifications() {
        switch ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                font_size = 40;
                break;

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                font_size = 20;

                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            default:
                font_size = 15;

                break;
        }
    }

    private void setSplashScreenTimerToActivity(final Intent intent) {
        timer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                config.getFooterTextView().setText(String.valueOf(millisUntilFinished / 1000));
                config.getHeaderTextView().setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                startActivity(intent);
            }
        };

        timer.start();
    }

    private void splashScreenSettings() {
        config.getHeaderTextView().setTextSize(font_size);
        config.getBeforeLogoTextView().setTextSize(font_size);
        config.getAfterLogoTextView().setTextSize(font_size);
        config.getFooterTextView().setTextSize(font_size);
        config.getHeaderTextView().setTextColor(Color.WHITE);
        config.getBeforeLogoTextView().setTextColor(Color.WHITE);
        config.getAfterLogoTextView().setTextColor(Color.WHITE);
        config.getFooterTextView().setTextColor(Color.WHITE);
        config.getLogo().setScaleType(FIT_XY);
        config.getLogo().setAdjustViewBounds(true);
        easySplashScreen = config.create();
        easySplashScreen.canScrollHorizontally(3);
    }

    private TextDataRow setTextDataFromSelectedText(Intent fromWhichActivityYouCameFrom) {
        String id = fromWhichActivityYouCameFrom.getExtras().getString("id");
        String title = fromWhichActivityYouCameFrom.getExtras().getString("title");
        String text = fromWhichActivityYouCameFrom.getExtras().getString("text");
        String composer = fromWhichActivityYouCameFrom.getExtras().getString("composer");
        String theme = fromWhichActivityYouCameFrom.getExtras().getString("theme");
        String date = fromWhichActivityYouCameFrom.getExtras().getString("date");
        Double rating = fromWhichActivityYouCameFrom.getExtras().getDouble("rating");
        String numberOfTimesPlayed = fromWhichActivityYouCameFrom.getExtras().getString("playCount");
        String bestScore = fromWhichActivityYouCameFrom.getExtras().getString("bestScore");
        String fastestSpeed = fromWhichActivityYouCameFrom.getExtras().getString("fastestSpeed");
        String roomKey = fromWhichActivityYouCameFrom.getExtras().getString("roomKey");
        int indexInRoom = fromWhichActivityYouCameFrom.getExtras().getInt("indexInRoom");
        return new TextDataRow(id, title, theme, text, date, composer,
                rating, numberOfTimesPlayed, bestScore, fastestSpeed, false, false, roomKey, null, indexInRoom);
    }

    private class FetchRandomText extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            final String choosenTheme = getRandomThemeName();

            TextDataRow textCardItem = getNextTextFromSelectedTheme(choosenTheme);
            if (textCardItem == null){
                fetchRandomTextSpecifiedForUsers();
            }else{
                sleep(1);
                changeSplashUI(choosenTheme);
                int playCount = Integer.parseInt(textCardItem.getNumberOfTimesPlayed());
                String composer = textCardItem.getComposer();
                changedTextData(playCount, composer, choosenTheme, textCardItem.getTextId());

                updateBestScoresOnUI(textCardItem);

                checkIfTextListNeedToBeRefilled(choosenTheme);
            }

            return null;
        }

        public void updateBestScoresOnUI(final TextDataRow textCardItem) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showBestStatsOnScreen(textCardItem);
                    final Intent intent = setIntentForTheGame(textCardItem);
                    setSplashScreenTimerToActivity(intent);

                }
            });
        }

        public void changeSplashUI(String choosenTheme) {
            final String choosenThemeName = choosenTheme;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Stuff that updates the UI
                    config.getLogo().setImageResource(getThemePictureId(choosenThemeName));
//            config.getLogo().setScaleType(CENTER);
                    config.getBeforeLogoTextView().setText("Chosen Theme is: " + choosenThemeName);
                    config.getAfterLogoTextView().setText("Loading the text");
                }
            });
        }

        private void fetchRandomTextSpecifiedForUsers() {
            sleep(1);
            final String choosenTheme = getRandomThemeName();
            changeSplashUI(choosenTheme);

            //now reach for the theme texts and check the number of texts in the theme
            getThemesCollection().document(choosenTheme).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
//                            Log.d(TAG, "getRandomTheme:" + "DocumentSnapshot data: " + document.getData());
                            int textsAmount = document.getLong("textsCount").intValue();
                            getRandomText(choosenTheme, textsAmount);
                        } else {
//                            Log.d(TAG, "getRandomTheme:" + "No such document");
                            //TODO: is it possible that we will reach here?
                        }
                    } else {
//                        Log.d(TAG, "getRandomTheme:" + "get failed with ", task.getException());
                        //TODO: is it possible that we will reach here?
                    }
                }
            });
        }


        private String getRandomThemeName() {
            List<String> userChosenThemes = new LinkedList<>();
            for (String theme : allUserThemes.keySet()) {
                if (allUserThemes.get(theme)) {
                    userChosenThemes.add(theme);
                }
            }
            // if the user has no themes selected we will choose all for him
            if (userChosenThemes.isEmpty()) {
                for (String theme : basicThemes) {
                    userChosenThemes.add(theme);
                }
            }
            //choose random theme from the user themes
            int themesListSize = userChosenThemes.size();
            return userChosenThemes.get(new Random().nextInt(themesListSize));
        }

        private void getRandomText(final String choosenTheme, int textsAmount) {
            final int chosenIndex = (new Random().nextInt(textsAmount)) + 1;
            //now reach for the theme texts and check the number of texts in there
            getSelectedThemeTextsCollection(choosenTheme).whereEqualTo("mainThemeID", chosenIndex).
                    get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            fetchRandomTextSpecifiedForUsers();
//                            Log.d(TAG, "getRandomText: another round");
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, "getRandomText: " + "DocumentSnapshot data: " + document.getData());
                                TextDataRow textCardItem = TextDataRow.createTextCardItem(document, null, -1, null);
                                int playCount = Integer.parseInt(textCardItem.getNumberOfTimesPlayed());
                                String composer = textCardItem.getComposer();
                                changedTextData(playCount, composer, choosenTheme, document.getId());
                                final Intent intent = setIntentForTheGame(textCardItem);
                                showBestStatsOnScreen(textCardItem);
                                setSplashScreenTimerToActivity(intent);

                                break;
                            }
                        }
                    } else {
//                        Log.d(TAG, "getRandomText: " + "get failed with ", task.getException());
                    }
                }
            });
        }


        //very useful to copy data from one text collection to another
        public void copyDocumentFromThemesToTextCollection() {
            for (String theme : basicThemes) {
                getSelectedThemeTextsCollection(theme).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        Log.d(TAG, "getAllThemes:" + document.getId() + " => " + document.getData());
                                        getTextFromTextsCollection(document.getId()).set(document.getData(), SetOptions.merge());
                                        String composer = document.get("composer").toString();
                                        getUserCollection(composer, "texts").document(document.getId()).set(document.getData(), SetOptions.merge());
                                    }
                                } else {
//                                    Log.d(TAG, "getAllThemes:" + "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        }

        private void changedTextData(int value, String composer, String choosenTheme, final String documentID) {
            Map<String, Object> changedText = new HashMap<>();
            changedText.put("playCount", value + 1);
            getSelectedThemeTextsCollection(choosenTheme).document(documentID).set(changedText, SetOptions.merge());
            getTextFromTextsCollection(documentID).set(changedText, SetOptions.merge());
            getUserCollection(composer, "texts").document(documentID).set(changedText, SetOptions.merge());
            copyDocumentFromThemesToTextCollection();
        }

        private DocumentReference getTextFromTextsCollection(String documentID) {
            return db.collection("texts").document(documentID);
        }

        private CollectionReference getUserCollection(String userID, String collecionName) {
            return getUserDocument(userID).collection(collecionName);
        }

        private DocumentReference getUserDocument(String composer) {
            return db.collection("users").document(composer);
        }

        private CollectionReference getSelectedThemeTextsCollection(String theme) {
            return getThemesCollection().document(theme).collection("texts");
        }

        private CollectionReference getThemesCollection() {
            return db.collection("themes");
        }

        private String getUid() {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (account != null && currentUser == null) {
                return account.getId();
            } else if (currentUser != null) {
                return mAuth.getUid();
            } else {
                return accessToken.getUserId();
            }
        }


    }

    public int getThemePictureId(String themeName) {
        switch (themeName) {
            case "Comedy":
                return R.drawable.comedy;
            case "Music":
                return R.drawable.music;
            case "Science":
                return R.drawable.science;
            case "Games":
                return R.drawable.games;
            case "Literature":
                return R.drawable.literature;
            case "Movies":
            default:
                return R.drawable.movies;
        }
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Intent setIntentForTheGame(TextDataRow textCardItem) {
        Intent i = new Intent();
        i.setClass(getApplicationContext(), GameActivity.class);
        i.putExtra("id", textCardItem.getTextId());
        i.putExtra("title", textCardItem.getTitle());
        i.putExtra("text", textCardItem.getText());
        i.putExtra("composer", textCardItem.getComposer());
        i.putExtra("theme", textCardItem.getThemeName());
        i.putExtra("date", textCardItem.getDate());
        i.putExtra("rating", textCardItem.getRating());
        i.putExtra("playCount", textCardItem.getNumberOfTimesPlayed());
        i.putExtra("bestScore", textCardItem.getBestScore());
        i.putExtra("fastestSpeed", textCardItem.getFastestSpeed());
        i.putExtra("roomKey", textCardItem.getRoomKey());
        i.putExtra("indexInRoom", textCardItem.getIndexInRoom());
        i.putExtra("startingTimeStamp", textCardItem.getStartingTimeStamp());

        return i;
    }
}
