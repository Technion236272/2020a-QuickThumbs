package android.technion.quickthumbs.personalArea;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.technion.quickthumbs.AddTextActivity;
import android.technion.quickthumbs.R;
import android.technion.quickthumbs.personalArea.PersonalTexts.TextAdaptor;
import android.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import android.technion.quickthumbs.theme.ThemeAdaptor;
import android.technion.quickthumbs.theme.ThemeDataRow;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TextsActivity extends AppCompatActivity {
    private static final String TAG = AddTextActivity.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private View textCard;
    private TextView loadingText;
    private RelativeLayout personalListLoadingLayout;
    private int howMuchToLoadEachScroll;
    final List<TextDataRow> textsList = new ArrayList<>();
    private DocumentSnapshot lastSnapShot =null;
    boolean noMoreLoading;
    HashMap<String,Boolean> loadedRTextsIDs=new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texts);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        howMuchToLoadEachScroll = 3;
        noMoreLoading =false;

        personalListLoadingLayout = findViewById(R.id.personalListLoadingLayout);
        loadingText = findViewById(R.id.personalListLoadingText);
        textCard = findViewById(R.id.textCard);
        recyclerView = findViewById(R.id.personalTextsRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        personalListLoadingLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        setActionBar();


        checkIfUserHasPersonalTexts();
    }

    private void checkIfUserHasPersonalTexts() {
        db.collection("users").document(mAuth.getUid())
                .collection("texts").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().getDocuments().size() != 0) {
                            Log.d(TAG, "collection is not empty!", task.getException());
                            fetchPersonalTextsList();
                            setRecyclerViewScroller();
                        } else {
                            Log.d(TAG, "no such collection", task.getException());
                            loadingText.setText("There is no personal texts. Please add text to show the list");
                        }
                    }
                });
    }

    private void setRecyclerViewScroller() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling up
                    Log.i("RecyclerView scrolled: ", "scroll down!");
                    if(textsList.size() != 0 && !noMoreLoading){
                        refillTextsCardsList(recyclerView);
                    }
                } else {
                    // Scrolling down
                    Log.i("RecyclerView scrolled: ", "scroll up!");
                }
            }
        });
    }

    private void refillTextsCardsList(final RecyclerView recyclerView) {
        db.collection("users").document(mAuth.getUid()).
                collection("texts").orderBy("playCount", Query.Direction.DESCENDING).
                startAfter(lastSnapShot).limit(howMuchToLoadEachScroll).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document:task.getResult()) {
                                TextDataRow item = createTextCardItem(document);
                                if (loadedRTextsIDs.get(document.getId()) == null ) {
                                    loadedRTextsIDs.put(document.getId(),true);
                                    textsList.add(item);
                                    recyclerView.getAdapter().notifyDataSetChanged();
                                }else{
                                    noMoreLoading =true;
                                    recyclerView.clearOnScrollListeners();
                                }
                            }
                        } else {
                            Log.d(TAG, "getAllThemes:"+  "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private TextDataRow createTextCardItem(DocumentSnapshot document) {
        Log.d(TAG, "getAllThemes:" + document.getId() + " => " + document.getData());
        String title = document.get("title").toString();
        String theme = document.get("theme").toString();
        String text = document.get("text").toString();
        double rating = document.getDouble("rating");
        String numberOfPlays = "0";
        if (document.getLong("playCount") != null) {
            numberOfPlays = document.getLong("playCount").toString();
        }
        String fastestSpeed = "0";
        if (document.getDouble("fastestSpeed") != null) {
            fastestSpeed = document.getDouble("fastestSpeed").toString();
        }
        String best = "0";
        if (document.getLong("best") != null) {
            best = document.getLong("best").toString();
        }
        TextDataRow item = new TextDataRow(title, theme, text, rating,
                numberOfPlays, best, fastestSpeed);

        lastSnapShot=document;
        return item;
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.textsListToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void fetchPersonalTextsList(){
        db.collection("users").document(mAuth.getUid()).collection("texts").
                orderBy("playCount", Query.Direction.DESCENDING).limit(8).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            fillTextCardList(task);
                            setTextAdaptor();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void fillTextCardList(@NonNull Task<QuerySnapshot> task) {
        for (QueryDocumentSnapshot document : task.getResult()) {
            TextDataRow item = createTextCardItem(document);
            textsList.add(item);
            loadedRTextsIDs.put(document.getId(),true);
        }
    }

    private void setTextAdaptor() {
        TextAdaptor adapter = new TextAdaptor(textsList,getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));
        personalListLoadingLayout.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}
