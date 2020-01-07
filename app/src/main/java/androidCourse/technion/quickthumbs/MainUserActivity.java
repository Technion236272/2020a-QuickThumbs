package androidCourse.technion.quickthumbs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import Utils.AppOpeningSplashScreen;
import androidCourse.technion.quickthumbs.R;

import androidCourse.technion.quickthumbs.multiplayerSearch.Room;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import androidCourse.technion.quickthumbs.theme.ThemeSelectPopUp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainUserActivity extends Fragment {
    private static final String TAG = MainUserActivity.class.getSimpleName();
    private FirebaseAuth fireBaseAuth;
    private FirebaseFirestore db;
    public static Button gameBtn;
    public static Button startMultiGameButton;
    DatabaseReference mDatabase;
    DatabaseReference searchingRooms;
    DatabaseReference searchingRoomsLevel1;
    DatabaseReference gameRooms;
    private int triesCounter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main_user, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated (View view,
                               Bundle savedInstanceState){
        final View fragmentView = view;
        fireBaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseDatabase instance = FirebaseDatabase.getInstance();

        mDatabase = instance.getReference().child("searchAndGame");
        searchingRooms = mDatabase.child("searchingRooms");
        searchingRoomsLevel1 = searchingRooms.child("level1");
        gameRooms = mDatabase.child("GameRooms");

        gameBtn = view.findViewById(R.id.startGameButton);
        gameBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           ThemeSelectPopUp popUpWindow = new ThemeSelectPopUp();
                                           popUpWindow.showPopupWindow(v,fragmentView.findViewById(R.id.RelativeLayout1));
                                       }
                                   }
        );

        startMultiGameButton = view.findViewById(R.id.startMultiGameButton);
        startMultiGameButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        startMultiGameButton.setClickable(false);
                                                        startSearchForGame();
                                                    }
                                                }
        );

        closeKeyboard();

        setOpeningSplashScreen();
    }


    private void startSearchForGame() {
        triesCounter = 0;
        int magicNumber = 10;

        Query potentialRooms = searchingRoomsLevel1.orderByKey().limitToLast(magicNumber);
        potentialRooms.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long childrenCount = dataSnapshot.getChildrenCount();
                if (childrenCount == 0) {
                    new FetchRandomTextId().execute();

                    return;
                }

                int chosenIndex = new Random().nextInt((int) childrenCount);
                int i = 0;

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (chosenIndex == i) {
                        final String roomKey = data.getKey();
                        final Room roomPrevious = data.getValue(Room.class);

                        mDatabase.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                MutableData searchingRoomsLevel1 = mutableData.child("searchingRooms").child("level1");
                                searchingRoomsLevel1.child(roomKey).setValue(null); //removing from level 1.

                                Room gameRoom = new Room(roomPrevious.user1, roomPrevious.location1, fireBaseAuth.getUid(), 0, roomPrevious.textId);
                                mutableData.child("GameRooms").child(roomKey).setValue(gameRoom);   //adding as an active multi-player game.

                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                if (b) {  //was committed
                                    startMultiGameButton.setClickable(true);

                                    Room gameRoom = dataSnapshot.child("GameRooms").child(roomKey).getValue(Room.class);
                                    String textId = gameRoom.textId;

                                    Context context = getActivity().getApplicationContext();
                                    Intent i = new Intent(context, GameLoadingSplashScreenActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.putExtra("id", textId);
                                    i.putExtra("roomKey", roomKey);
                                    i.putExtra("indexInRoom", 2);
                                    context.startActivity(i);
                                } else {
                                    if (triesCounter++ == 3) {
                                        new FetchRandomTextId().execute();
                                    }
                                }
                            }
                        });

                        break;
                    }

                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    private void startMultiplayerGameWhenGameCreatedForThisRoom(final String roomKey, final String textId, final int indexInRoom) {
        gameRooms.child(roomKey).addValueEventListener(new ValueEventListener() {
                                                                                                   @Override
                                                                                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                       if (!dataSnapshot.exists() ||
                                                                                                               dataSnapshot.getValue(Room.class).user2 == null) {
                                                                                                           return;
                                                                                                       }

                                                                                                       startMultiGameButton.setClickable(true);
                                                                                                       //game starts here;
                                                                                                       Context context = getActivity().getApplicationContext();
                                                                                                       Intent i = new Intent(context, GameLoadingSplashScreenActivity.class);
                                                                                                       i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                                                                       i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                       i.putExtra("id", textId);
                                                                                                       i.putExtra("roomKey", roomKey);
                                                                                                       i.putExtra("indexInRoom", indexInRoom);
                                                                                                       context.startActivity(i);

                                                                                                       gameRooms.child(roomKey).removeEventListener(this);
                                                                                                   }

                                                                                                   @Override
                                                                                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                                   }
                                                                                               }

        );
    }

    public void createSeparateRoom(final String textId) {
        final String key = searchingRoomsLevel1.push().getKey();

        //TODO transaction needed here
        searchingRoomsLevel1.child(key).setValue(new Room(fireBaseAuth.getUid(), 0, null, 0, textId));
        gameRooms.child(key).setValue(new Room(fireBaseAuth.getUid(), 0, null, 0, textId))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startMultiplayerGameWhenGameCreatedForThisRoom(key, textId, 1);
                        } else {
                            throw new RuntimeException("bug I guess");
                        }
                    }
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (checkIfUserLoggedIn(currentUser, account, isLoggedIn)) return;
    }

    private void setButtonListener(Button button, final Class<? extends AppCompatActivity> moveToActivityClass) {
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          if (v.getId() == R.id.startGameButton) {
                                              startActivity(new Intent(getActivity(), moveToActivityClass));
                                          }
                                      }
                                  }
        );
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (checkIfUserLoggedIn(currentUser, account, isLoggedIn)) return;
        //the part where i insert the user to the db just ot make sure he's there in case no user has been made
    }

    private void setOpeningSplashScreen() {
        AppOpeningSplashScreen.Builder splash = new AppOpeningSplashScreen.Builder(getActivity());
        //        Set custom color of background:
        splash.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        //Set custom image for background:
//        splash.setBackgroundImage(getResources().getDrawable(R.mipmap.ic_launcher_foreground));
        //Set custom image for splash:
        splash.setSplashImage(getResources().getDrawable(R.drawable.ic_launcher_foreground));
        //Set custom color of splash image:
        splash.setSplashImageColor(getResources().getColor(R.color.primaryDarkColor));
        splash.create();
//        splash.setOneShotStart(false);
        splash.perform();
    }

    private boolean checkIfUserLoggedIn(FirebaseUser currentUser, GoogleSignInAccount account, boolean isLoggedIn) {
        if (currentUser != null || account != null && !account.isExpired() || isLoggedIn) {
            return false;
        }
        Intent i = new Intent(getActivity(), MainActivity.class);
        getActivity().finish();
        startActivity(i);
        return true;
    }


    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private static class TextFetcherParam {
        Context context;

        TextFetcherParam(Context context) {
            this.context = context;
        }
    }

    private class FetchRandomTextId extends AsyncTask<Void, Void, Void> {
        private String[] basicThemes = {"Comedy", "Music", "Movies", "Science", "Games", "Literature"};
        private Map<String, Boolean> allUserThemes = new HashMap<>();
        private TextDataRow textCardItem = null;

        @Override
        protected Void doInBackground(Void... voids) {
            fetchRandomTextSpecifiedForUsers();

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
                                    insertThemesFromAllThemes(document, true);
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

        public void insertThemesFromAllThemes(QueryDocumentSnapshot document, Boolean isChosen) {
            String currentThemeName = document.getId();
            allUserThemes.put(currentThemeName, isChosen);
        }

        private void getUserThemes() {
            getUserCollection(getUid(), "themes").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "getUserThemes:" + document.getId() + " => " + document.getData());
                                    insertThemesFromAllThemes(document, document.getBoolean("isChosen"));
                                }
                                getRandomTheme();
                            } else {
                                //there is no user prefences- take all -> don't change the themes
                                Log.d(TAG, "getUserThemes:" + "Error getting documents: ", task.getException());
                                getRandomTheme();
                            }
                        }
                    });
        }

        private void getRandomTheme() {
            final String choosenTheme = getRandomThemeName();
            //now reach for the theme texts and check the number of texts in the theme
            getThemesCollection().document(choosenTheme).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "getRandomTheme:" + "DocumentSnapshot data: " + document.getData());
                            int textsAmount = document.getLong("textsCount").intValue();
                            getRandomText(choosenTheme, textsAmount);
                        } else {
                            Log.d(TAG, "getRandomTheme:" + "No such document");
                            //TODO: is it possible that we will reach here?
                        }
                    } else {
                        Log.d(TAG, "getRandomTheme:" + "get failed with ", task.getException());
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
                        if (task.getResult().isEmpty()) {
                            fetchRandomTextSpecifiedForUsers();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                textCardItem = TextDataRow.createTextCardItem(document, null, -1);
                                String textId = document.getId();
                                try {
                                    Class<?> c = MainUserActivity.class;
                                    Method method = c.getDeclaredMethod("createSeparateRoom", String.class);
                                    method.invoke(MainUserActivity.this, textId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                        }
                    } else {
                        Log.d(TAG, "getRandomText: " + "get failed with ", task.getException());
                    }
                }
            });
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
            FirebaseUser currentUser = fireBaseAuth.getCurrentUser();
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (account != null && currentUser == null) {
                return account.getId();
            } else if (currentUser != null) {
                return fireBaseAuth.getUid();
            } else {
                return accessToken.getUserId();
            }
        }

    }


}
