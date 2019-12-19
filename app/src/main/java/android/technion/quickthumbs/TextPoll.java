package android.technion.quickthumbs;

import android.technion.quickthumbs.game.GameActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TextPoll {
    private static final String TAG = TextPoll.class.getSimpleName();

    public static String fetchRandomText(final TextView gameTextView, final GameActivity objectToInvokeOn) {
        final String selectedText = "default text for usage";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("texts");
        collection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> textsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String currentText = document.getString("text");
                                textsList.add(currentText);
                            }
                            int textsListSize = textsList.size();
                            String randomText = textsList.get(new Random().nextInt(textsListSize));
                            Log.d(TAG, "gottenText is: " + randomText);
                            gameTextView.setText(randomText);
                            try {
                                Class<?> c = GameActivity.class;
                                Method method = c.getDeclaredMethod("gameCreationSequence", (Class<?>[]) null);
                                method.invoke(objectToInvokeOn, (Object[]) null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        return selectedText;
    }


    //very useful to copy data from one to another
    public static void copyDocumentFromThemesToTextCollection() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        db.collection("themes").document(mAuth.getUid()).collection("themes").get()
        String[] themesNames={"Comedy","Music","Movies","Science","Games","Literature"};
        for (String theme : themesNames){
            db.collection("themes").document(theme).collection("texts").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "getAllThemes:"+ document.getId() + " => " + document.getData());
                                    db.collection("texts/").document(document.getId()).set(document.getData(), SetOptions.merge());
                                }
                            } else {
                                Log.d(TAG, "getAllThemes:"+  "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    private static void changedTextData(int value,String choosenTheme, final String documentID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Map<String, Object> changedText = new HashMap<>();
        changedText.put("playCount", value + 1);

        db.collection("themes/" + choosenTheme + "/texts").document(documentID).set(changedText, SetOptions.merge());
        db.collection("texts/").document(documentID).set(changedText, SetOptions.merge());
//        copyDocumentFromThemesToTextCollection();
    }


    private static void getRandomText(final String choosenTheme, int textsAmount, final TextView gameTextView, final GameActivity objectToInvokeOn) {
        final String selectedText = "default text for usage";
        final int chosenIndex = (new Random().nextInt(textsAmount)) + 1;
        //now reach for the theme texts and check the number of texts in there
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db.collection("themes").document(choosenTheme).collection("texts").whereEqualTo("mainThemeID", chosenIndex).
                get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "getRandomText: "+ "hereeeeeeee"+ "chosen index is: "+chosenIndex);
                if (task.isSuccessful()) {
                    Log.d(TAG, "getRandomText: "+ "succcesssss :   "+task.getResult().size());
                    if (task.getResult().isEmpty()){
                        initiateCustomizeTextFetch(gameTextView,objectToInvokeOn);
                    }
                    else{
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "getRandomText: "+ "DocumentSnapshot data: " + document.getData());

                            String randomText = document.getString("text");
                            Log.d(TAG, "getRandomText: "+"gottenText is: " + randomText);
                            gameTextView.setText(randomText);
                            try {
                                Class<?> c = GameActivity.class;
                                Method method = c.getDeclaredMethod("gameCreationSequence", (Class<?>[]) null);
                                method.invoke(objectToInvokeOn, (Object[]) null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            int playCount = document.getLong("playCount").intValue();
                            changedTextData(playCount,choosenTheme, document.getId());
                            return;
                        }
                    }
                } else {
                    Log.d(TAG, "getRandomText: "+"get failed with ", task.getException());
                }
            }
        });
    }

    private static void getRandomTheme(final Map<String, Boolean> allUserThemes, final TextView gameTextView, final GameActivity objectToInvokeOn) {
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
        final String choosenTheme = userChosenThemes.get(new Random().nextInt(themesListSize));
        //now reach for the theme texts and check the number of texts in there
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db.collection("themes").document(choosenTheme).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
//                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Log.d(TAG, "getRandomTheme:"+ "DocumentSnapshot data: " + document.getData());

                        int textsAmount = document.getLong("textsCount").intValue();

                        Log.d(TAG, "choosenTheme: "+choosenTheme + "textsAmount: "+textsAmount);
                        getRandomText(choosenTheme, textsAmount, gameTextView, objectToInvokeOn);
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

    private static void getUserThemes(final Map<String, Boolean> allThemes, final TextView gameTextView, final GameActivity objectToInvokeOn) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db.collection("users").document(mAuth.getUid()).collection("themes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "getUserThemes:"+ document.getId() + " => " + document.getData());
                                Boolean isChosen = document.getBoolean("isChosen");
                                String themeName = document.getId();
                                allThemes.put(themeName, isChosen);
                            }
                            getRandomTheme(allThemes, gameTextView, objectToInvokeOn);

                        } else {
                            //there is no user prefences- take all -> don't change the themes
                            Log.d(TAG, "getUserThemes:"+ "Error getting documents: ", task.getException());
                            getRandomTheme(allThemes, gameTextView, objectToInvokeOn);


                        }
                    }
                });
    }

    public static void getAllThemes(final TextView gameTextView, final GameActivity objectToInvokeOn) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        db.collection("themes").document(mAuth.getUid()).collection("themes").get()
        db.collection("themes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Boolean> allThemes = new HashMap<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "getAllThemes:"+ document.getId() + " => " + document.getData());
//                                Boolean isChosen = document.getBoolean("isChosen");
                                String currentThemeName = document.getId();
                                allThemes.put(currentThemeName, true);
                            }
                            getUserThemes(allThemes, gameTextView, objectToInvokeOn);
                        } else {
                            Log.d(TAG, "getAllThemes:"+  "Error getting documents: ", task.getException());
                            String[] basicThemes = {"Comedy", "Music", "Movies", "Science", "Games", "Literature"};
                            for (int i = 0; i < basicThemes.length; i++) {
                                allThemes.put(basicThemes[i], true);
                            }
                            getUserThemes(allThemes, gameTextView, objectToInvokeOn);
                        }
                    }
                });
    }

    public static void initiateCustomizeTextFetch(final TextView gameTextView, final GameActivity objectToInvokeOn) {
        getAllThemes(gameTextView, objectToInvokeOn);
    }
}
