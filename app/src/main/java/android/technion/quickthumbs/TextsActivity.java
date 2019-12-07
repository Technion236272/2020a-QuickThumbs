package android.technion.quickthumbs;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class TextsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texts);

        setActionBar();
        setTextsRecyclerViewAdapter();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.textsToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setTextsRecyclerViewAdapter(){
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

class Text {
    String title;
    String text;
    boolean isExpanded;
    int image;


    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String geText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
