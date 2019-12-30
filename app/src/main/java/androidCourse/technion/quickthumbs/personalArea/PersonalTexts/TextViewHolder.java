package androidCourse.technion.quickthumbs.personalArea.PersonalTexts;

import androidCourse.technion.quickthumbs.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TextViewHolder extends RecyclerView.ViewHolder {
    CardView textCard;
    TextView textTitle;
    TextView textDescription;
    TextView numberOfTimesTextPlayed;
    TextView fastestSpeed;
    TextView bestScore;
    ImageView textTheme;
    ImageView viewMoreButton;
    ImageView star1;
    ImageView star2;
    ImageView star3;
    ImageView star4;
    ImageView star5;
    LinearLayout textExpand;
    LinearLayout titleExpand;
    LinearLayout statsExpand;

    public TextViewHolder(@NonNull View itemView) {
        super(itemView);
        textCard = itemView.findViewById(R.id.textCard);
        textTheme = itemView.findViewById(R.id.textTheme);
        viewMoreButton = itemView.findViewById(R.id.viewMoreButton);
        textTitle = itemView.findViewById(R.id.textTitle);
        textDescription = itemView.findViewById(R.id.textDescription);
        numberOfTimesTextPlayed = itemView.findViewById(R.id.itemNumberOfPlays);
        fastestSpeed = itemView.findViewById(R.id.itemFastestSpeed);
        bestScore = itemView.findViewById(R.id.itemBestScore);
        star1 = itemView.findViewById(R.id.starsRanking1);
        star2 = itemView.findViewById(R.id.starsRanking2);
        star3 = itemView.findViewById(R.id.starsRanking3);
        star4 = itemView.findViewById(R.id.starsRanking4);
        star5 = itemView.findViewById(R.id.starsRanking5);
        textExpand = itemView.findViewById(R.id.textLayoutExpand);
        titleExpand = itemView.findViewById(R.id.titleLayoutExpand);
        statsExpand = itemView.findViewById(R.id.statsLayoutExpand);
    }
}
