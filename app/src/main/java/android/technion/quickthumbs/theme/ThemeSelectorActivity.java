package android.technion.quickthumbs.theme;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.technion.quickthumbs.R;

import java.util.ArrayList;
import java.util.List;

public class ThemeSelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_selector);

        setActionBar();
        RecyclerView recyclerView = findViewById(R.id.themeRecycleView);

        List<ThemeDataRow> data = fillWithData();
        ThemeAdaptor adapter = new ThemeAdaptor(data, getApplication());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.themeSelectorToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    public List<ThemeDataRow> fillWithData() {

        List<ThemeDataRow> data = new ArrayList<>();

        data.add(new ThemeDataRow("Comedy", "funny times with your keyboard", R.drawable.funny_trump_icon));
        data.add(new ThemeDataRow("Music", "start singing once you recognized the song", R.drawable.music_icon));
        data.add(new ThemeDataRow("Movies", "so exiting that you will forget typing", R.drawable.movie_icon));
        data.add(new ThemeDataRow("Science", "full of science", R.drawable.stupid_science_icon));
        data.add(new ThemeDataRow("Games", "are you a gamer? your place is here", R.drawable.games_icon));
        data.add(new ThemeDataRow("Literature", "book warm? don't forget to type", R.drawable.literature_icon));

        return data;
    }
}
