package androidCourse.technion.quickthumbs;

import android.content.Context;
import android.content.Intent;
import androidCourse.technion.quickthumbs.game.GameActivity;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import android.util.Log;

import androidx.annotation.NonNull;

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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TextPoll {
    private static final String TAG = TextPoll.class.getSimpleName();
    final private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    final private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static String[] basicThemes = {"Comedy", "Music", "Movies", "Science", "Games", "Literature"};
    private static Map<String, Boolean> allUserThemes = new HashMap<>();
    private static List<TextDataRow> textItem = new LinkedList<>();
    private static int repeat = 10;
    private static Context context;
    public TextPoll(Context context){
        this.context=context;
    }
    public static void populateTextCache(Context context) {
        try {
            TextDataRow item = CacheHandler.loadFileFromCacheFolder(context);
            //remove the first text item and insert another one
            context.deleteFile("textFile");
//            textItem.remove(0);
            fetchRandomTextSpecifiedForUsers();
        } catch (Exception e) {
                fetchRandomTextSpecifiedForUsers();
        }
    }

    public static void fetchRandomTextSpecifiedForUsers() {
        getAllThemes();
    }

    private static void getAllThemes() {
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

    private static void insertBasicThemes(@NonNull Task<QuerySnapshot> task) {
        for (int i = 0; i < basicThemes.length; i++) {
            allUserThemes.put(basicThemes[i], true);
        }
    }

    public static void insertThemesFromAllThemes(QueryDocumentSnapshot document,Boolean isChosen) {
        String currentThemeName = document.getId();
        allUserThemes.put(currentThemeName, isChosen);
    }

    private static void getUserThemes() {
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

    private static void getRandomTheme() {
        final String choosenTheme = getRandomThemeName();
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

    private static String getRandomThemeName() {
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

    private static void getRandomText(final String choosenTheme, int textsAmount) {
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
                            textItem.add(textCardItem);
                            int playCount = Integer.parseInt(textCardItem.getNumberOfTimesPlayed());
                            String composer = textCardItem.getComposer();
                            changedTextData(playCount,composer,choosenTheme, document.getId());
                        }
                        setIntentAndStartGame();
                    }
                } else {
                    Log.d(TAG, "getRandomText: "+"get failed with ", task.getException());
                }
            }
        });
    }

    private static void setIntentAndStartGame() {
        Intent i = new Intent();
        i.setClass(MainUserActivity.gameBtn.getContext(), GameActivity.class);
        i.putExtra("id",textItem.get(textItem.size()-1).getID());
        i.putExtra("title",textItem.get(textItem.size()-1).getTitle());
        i.putExtra("text",textItem.get(textItem.size()-1).getText());
        i.putExtra("composer",textItem.get(textItem.size()-1).getComposer());
        i.putExtra("theme",textItem.get(textItem.size()-1).getThemeName());
        i.putExtra("date",textItem.get(textItem.size()-1).getDate());
        i.putExtra("rating",textItem.get(textItem.size()-1).getRating());
        i.putExtra("playCount",textItem.get(textItem.size()-1).getNumberOfTimesPlayed());
        i.putExtra("bestScore",textItem.get(textItem.size()-1).getBestScore());
        i.putExtra("fastestSpeed",textItem.get(textItem.size()-1).getFastestSpeed());
        MainUserActivity.gameBtn.getContext().startActivity(i);
    }

    //very useful to copy data from one text collection to another
    public static void copyDocumentFromThemesToTextCollection() {
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

    private static void changedTextData(int value, String composer, String choosenTheme, final String documentID) {
        Map<String, Object> changedText = new HashMap<>();
        changedText.put("playCount", value + 1);
        changedText.put("bestScore", 0);
        changedText.put("bestAvgAccuracy", 0);
        changedText.put("bestCPM", 0);
        changedText.put("bestWpm", 0);
        getSelectedThemeTextsCollection(choosenTheme).document(documentID).set(changedText, SetOptions.merge());
        getTextFromTextsCollection(documentID).set(changedText, SetOptions.merge());
        getUserCollection(composer,"texts").document(documentID).set(changedText, SetOptions.merge());
//        copyDocumentFromThemesToTextCollection();
    }

    private static void browseDocumentsInCollection(CollectionReference collectionToBrowse,
                                                    final String documentIterationsFunctionName,
                                                    final String successfulTaskFunction,
                                                    final String unSuccessfulTaskFunction,
                                                    final String unSuccessfulTaskFunction2){
        collectionToBrowse.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                activateFunction(document, documentIterationsFunctionName);
                            }
//                            getUserThemes();
                            activateFunction(null, successfulTaskFunction);
                        } else {
                            activateFunction(null, unSuccessfulTaskFunction);
                            activateFunction(null, unSuccessfulTaskFunction2);
//                            insertBasicThemes(task);
//                            getUserThemes();
                        }
                    }
                });
    }

    private static void activateFunction(QueryDocumentSnapshot document, String functionName) {
        try {
            Class<?> c = TextPoll.class;
            Method method = c.getDeclaredMethod("TextPoll."+functionName, (Class<?>[]) null);
            method.invoke(null, document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static DocumentReference getTextFromTextsCollection(String documentID) {
        return db.collection("texts").document(documentID);
    }

    private static CollectionReference getUserCollection(String userID, String collecionName) {
        return getUserDocument(userID).collection(collecionName);
    }

    private static DocumentReference getUserDocument(String composer) {
        return db.collection("users").document(composer);
    }

    private static CollectionReference getSelectedThemeTextsCollection(String theme) {
        return getThemesCollection().document(theme).collection("texts");
    }

    private static CollectionReference getThemesCollection() {
        return db.collection("themes");
    }

    private static String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
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