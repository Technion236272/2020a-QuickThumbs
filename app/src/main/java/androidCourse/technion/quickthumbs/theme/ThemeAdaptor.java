package androidCourse.technion.quickthumbs.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import androidCourse.technion.quickthumbs.R;
import androidCourse.technion.quickthumbs.Utils.CacheHandler;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ThemeAdaptor extends RecyclerView.Adapter<ThemeViewHolder> {
    List<ThemeDataRow> themes;
    Boolean[] flags;
    Map<String,Boolean> selectedThemes;
    Context context;
    private static final String TAG = ThemeAdaptor.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CountDownTimer countDownTimer;
    private TextView timerTextView;
    private CacheHandler cacheHandler;

    public ThemeAdaptor(final List<ThemeDataRow> themes, final Context context,
                        CountDownTimer timer, TextView timerTextView) {
        this.themes = themes;
        this.context = context;
        flags = new Boolean[themes.size()];
        this.timerTextView = timerTextView;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
//        selectedThemes=selected;
        cacheHandler = new CacheHandler(context);
        selectedThemes = cacheHandler.loadThemesFromSharedPreferences();

        countDownTimer=timer;
        this.timerTextView = timerTextView;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.theme_row_layout, parent, false);
        ThemeViewHolder holder = new ThemeViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, final int position) {
        holder.title.setText(themes.get(position).themeName);
        holder.description.setText(themes.get(position).description);
        holder.imageView.setImageResource(themes.get(position).imageId);

        int color = getCardColorBasedOnClicks(selectedThemes.get(themes.get(position).themeName));
        holder.cardView.setCardBackgroundColor(color);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                countDownTimer.cancel();
                timerTextView.setText("");
                boolean previousValue =false;
                if(selectedThemes.get(themes.get(position).themeName) != null){
                    previousValue = selectedThemes.get(themes.get(position).themeName);
                }
                //for the recycler view
                boolean newValue = !previousValue;
                selectedThemes.put(themes.get(position).themeName,newValue);

                //for the db
                cacheHandler.saveThemesToSharedPreferences(selectedThemes);

                int color = getCardColorBasedOnClicks(newValue);
                ((CardView) v).setCardBackgroundColor(color);
            }
        });


    }

    private int getCardColorBasedOnClicks(Boolean flag) {
        int colorResource = flag ? R.color.secondaryLightColor : R.color.cardview_light_background;
        return ContextCompat.getColor(context, colorResource);
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }
}
