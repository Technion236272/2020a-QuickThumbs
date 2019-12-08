package android.technion.quickthumbs.theme;

import android.content.Context;
import android.technion.quickthumbs.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class ThemeAdaptor extends RecyclerView.Adapter<ThemeViewHolder> {
    List<ThemeDataRow> themes;
    Boolean[] flags;
    Context context;

    public ThemeAdaptor(List<ThemeDataRow> themes, Context context) {
        this.themes = themes;
        this.context = context;

        flags = new Boolean[themes.size()];
        Arrays.fill(flags, true);
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

        int color = getCardColorBasedOnClicks(flags[position]);
        holder.cardView.setCardBackgroundColor(color);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flags[position] = !flags[position]; //save the state when needed

                int color = getCardColorBasedOnClicks(flags[position]);
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
