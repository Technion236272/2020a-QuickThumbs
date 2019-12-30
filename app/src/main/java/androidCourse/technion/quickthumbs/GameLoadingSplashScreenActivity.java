package androidCourse.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidCourse.technion.quickthumbs.game.GameActivity;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import android.util.Log;
import android.view.View;

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


public class GameLoadingSplashScreenActivity extends AppCompatActivity {
    private static final String TAG = GameLoadingSplashScreenActivity.class.getSimpleName();
    final private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    final private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static String[] basicThemes = {"Comedy", "Music", "Movies", "Science", "Games", "Literature"};
    private static Map<String, Boolean> allUserThemes = new HashMap<>();
    private View easySplashScreen;
    private EasySplashScreen config;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        config = new EasySplashScreen(GameLoadingSplashScreenActivity.this)
                .withFullScreen()
                .withSplashTimeOut(7000)
                .withBackgroundColor(Color.parseColor("#1a1b29"))
//                .withHeaderText("Header")
//                .withFooterText("Footer")
                .withBeforeLogoText("Choosing From Preferred themes")
                .withAfterLogoText(" ")
                .withLogo(R.drawable.game_loading_icon);

//        config.getHeaderTextView().setTextColor(Color.WHITE);
//        config.getFooterTextView().setTextColor(Color.WHITE);
        config.getBeforeLogoTextView().setTextColor(Color.WHITE);
        config.getAfterLogoTextView().setTextColor(Color.WHITE);
//        config.getLogo().setScaleType(CENTER);
        easySplashScreen = config.create();
        setContentView(easySplashScreen);
        new FetchRandomText().execute();
    }

    private class FetchRandomText extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            fetchRandomTextSpecifiedForUsers();
//            finish();
            return null;
        }

        protected void onProgressUpdate() {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute() {
//            showDialog("Downloaded " + result + " bytes");
        }

        public void fetchRandomTextSpecifiedForUsers() {
            getAllThemes();
        }

        private void getAllThemes() {
            CollectionReference themesCollection = getThemesCollection();
            themesCollection.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    insertThemesFromAllThemes(document,true);
                                }
                                getUserThemes();
                            } else {
                                insertBasicThemes(task);
                                getUserThemes();
                            }
                        }
                    });
        }

        private void insertBasicThemes(@NonNull Task<QuerySnapshot> task) {
            for (int i = 0; i < basicThemes.length; i++) {
                allUserThemes.put(basicThemes[i], true);
            }
        }

        public void insertThemesFromAllThemes(QueryDocumentSnapshot document,Boolean isChosen) {
            String currentThemeName = document.getId();
            allUserThemes.put(currentThemeName, isChosen);
        }

        private void getUserThemes() {
            getUserCollection(getUid(),"themes").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "getUserThemes:"+ document.getId() + " => " + document.getData());
                                    insertThemesFromAllThemes(document,document.getBoolean("isChosen"));
                                }
                                getRandomTheme();
                            } else {
                                //there is no user prefences- take all -> don't change the themes
                                Log.d(TAG, "getUserThemes:"+ "Error getting documents: ", task.getException());
                                getRandomTheme();
                            }
                        }
                    });
        }

        private void getRandomTheme() {
            sleep(1);
            final String choosenTheme = getRandomThemeName();
            config.getLogo().setImageResource(getThemePictureId(choosenTheme));
//            config.getLogo().setScaleType(CENTER);
            config.getBeforeLogoTextView().setText("Chosen Theme is: "+ choosenTheme);
            config.getAfterLogoTextView().setText("Loading the text");
            //now reach for the theme texts and check the number of texts in the theme
            getThemesCollection().document(choosenTheme).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "getRandomTheme:"+ "DocumentSnapshot data: " + document.getData());
                            int textsAmount = document.getLong("textsCount").intValue();
                            getRandomText(choosenTheme, textsAmount);
                        } else {
                            Log.d(TAG, "getRandomTheme:"+"No such document");
                            //TODO: is it possible that we will reach here?
                        }
                    } else {
                        Log.d(TAG, "getRandomTheme:"+ "get failed with ", task.getException());
                        //TODO: is it possible that we will reach here?
                    }
                }
            });
        }

        private void sleep(int seconds) {
            try {
                TimeUnit.SECONDS.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public int getThemePictureId (String themeName) {
            switch (themeName){
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

        private String getRandomThemeName() {
            List<String> userChosenThemes = new LinkedList<>();
            for (String theme : allUserThemes.keySet()) {
                if (allUserThemes.get(theme)) {
                    userChosenThemes.add(theme);
                }
            }
            // if the user has no themes selected we will choose all for him
            if (userChosenThemes.isEmpty()) {
                for (String theme : allUserThemes.keySet()) {
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
                        if (task.getResult().isEmpty()){
                            fetchRandomTextSpecifiedForUsers();
                            Log.d(TAG, "getRandomText: another round");
                        }
                        else{
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "getRandomText: "+ "DocumentSnapshot data: " + document.getData());
                                TextDataRow textCardItem = TextDataRow.createTextCardItem(document);
                                int playCount = Integer.parseInt(textCardItem.getNumberOfTimesPlayed());
                                String composer = textCardItem.getComposer();
                                changedTextData(playCount,composer,choosenTheme, document.getId());
                                setIntentAndStartGame(textCardItem);
                                break;
                            }
                        }
                    } else {
                        Log.d(TAG, "getRandomText: "+"get failed with ", task.getException());
                    }
                }
            });
        }

        private void setIntentAndStartGame(TextDataRow textCardItem) {
            Intent i = new Intent();
            i.setClass(getApplicationContext(), GameActivity.class);
            i.putExtra("id",textCardItem.getID());
            i.putExtra("title",textCardItem.getTitle());
            i.putExtra("text",textCardItem.getText());
            i.putExtra("composer",textCardItem.getComposer());
            i.putExtra("theme",textCardItem.getThemeName());
            i.putExtra("date",textCardItem.getDate());
            i.putExtra("rating",textCardItem.getRating());
            i.putExtra("playCount",textCardItem.getNumberOfTimesPlayed());
            i.putExtra("bestScore",textCardItem.getBestScore());
            i.putExtra("fastestSpeed",textCardItem.getFastestSpeed());
            sleep(3);
            startActivity(i);
        }

        //very useful to copy data from one text collection to another
        public void copyDocumentFromThemesToTextCollection() {
            for (String theme : basicThemes){
                getSelectedThemeTextsCollection(theme).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, "getAllThemes:"+ document.getId() + " => " + document.getData());
                                        getTextFromTextsCollection(document.getId()).set(document.getData(), SetOptions.merge());
                                        String composer=document.get("composer").toString();
                                        getUserCollection(composer,"texts").document(document.getId()).set(document.getData(), SetOptions.merge());
                                    }
                                } else {
                                    Log.d(TAG, "getAllThemes:"+  "Error getting documents: ", task.getException());
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
            getUserCollection(composer,"texts").document(documentID).set(changedText, SetOptions.merge());
//        copyDocumentFromThemesToTextCollection();
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
            if (account != null && currentUser == null){
                return account.getId();
            }else if (currentUser!=null){
                return mAuth.getUid();
            }else{
                return accessToken.getUserId();
            }
        }


    }
}
