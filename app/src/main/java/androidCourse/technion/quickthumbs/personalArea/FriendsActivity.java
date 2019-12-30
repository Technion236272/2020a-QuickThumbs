package androidCourse.technion.quickthumbs.personalArea;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import androidCourse.technion.quickthumbs.R;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setActionBar();
        setFriendsRecyclerViewAdapter();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.friendsToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setFriendsRecyclerViewAdapter(){
        /*
        TODO
        RecyclerView recyclerView = findViewById(R.id.texts_recycler_view);
        Query query = getUserTextsCollection().orderBy("text", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<InsertedText> options = new FirestoreRecyclerOptions.Builder<InsertedText>()
                .setQuery(query, InsertedText.class)
                .build();
        adapter = new RecyclerAdapter(options);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
         */
    }
}
