package androidCourse.technion.quickthumbs.theme;

import androidCourse.technion.quickthumbs.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ThemeViewHolder extends RecyclerView.ViewHolder {
    CardView cardView;
    TextView title;
    TextView description;
    ImageView imageView;

    public ThemeViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.cardView);
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
