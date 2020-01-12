package androidCourse.technion.quickthumbs.personalArea.PersonalTexts;

import android.content.Context;

import androidCourse.technion.quickthumbs.GameLoadingSplashScreenActivity;
import androidCourse.technion.quickthumbs.R;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TextAdaptor extends RecyclerView.Adapter<TextViewHolder> {
    List<TextDataRow> texts;
    Context context;
    private static final String TAG = TextAdaptor.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public TextAdaptor(List<TextDataRow> texts, Context context) {
        this.context = context;
        this.texts = texts;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_list_item, parent, false);
        TextViewHolder holder = new TextViewHolder(v);

        return holder;
    }

    public int getThemePictureId(@NonNull TextViewHolder holder, final int position) {
        switch (texts.get(position).getThemeName()) {
            case "Comedy":
                return R.drawable.comedy;
            case "Music":
                return R.drawable.music;
            case "Science":
                return R.drawable.science;
            case "Games":
                return R.drawable.games;
            case "Literature":
                return R.drawable.literature;
            case "Movies":
            default:
                return R.drawable.movies;
        }
    }


    public void setStarRanking(@NonNull TextViewHolder holder, final int position) {
        holder.star1.setVisibility(View.INVISIBLE);
        holder.star2.setVisibility(View.INVISIBLE);
        holder.star3.setVisibility(View.INVISIBLE);
        holder.star4.setVisibility(View.INVISIBLE);
        holder.star5.setVisibility(View.INVISIBLE);

        switch ((int) texts.get(position).getRating()) {
            case 5:
                holder.star5.setVisibility(View.VISIBLE);
            case 4:
                holder.star4.setVisibility(View.VISIBLE);
            case 3:
                holder.star3.setVisibility(View.VISIBLE);
            case 2:
                holder.star2.setVisibility(View.VISIBLE);
            case 1:
                holder.star1.setVisibility(View.VISIBLE);
            default:
                break;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final TextViewHolder holder, final int position) {
        holder.textTitle.setText(texts.get(position).getTitle());
        holder.textTheme.setImageResource(getThemePictureId(holder, position));
        holder.textDescription.setText(texts.get(position).getText());
        holder.fastestSpeed.setText(texts.get(position).getFastestSpeed());
        holder.bestScore.setText(texts.get(position).getBestScore());
        holder.numberOfTimesTextPlayed.setText(texts.get(position).getNumberOfTimesPlayed());
        setStarRanking(holder, position);
        int color = getCardColorBasedOnClicks((texts.get(position).isClicked()));
        holder.textCard.setCardBackgroundColor(color);
        holder.viewMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!texts.get(position).isExpanded()) {
                    holder.textExpand.setVisibility(View.VISIBLE);
                    holder.titleExpand.setVisibility(View.VISIBLE);
                    holder.statsExpand.setVisibility(View.VISIBLE);
                    holder.playLayoutExpand.setVisibility(View.VISIBLE);
                } else {
                    holder.textExpand.setVisibility(View.GONE);
                    holder.titleExpand.setVisibility(View.GONE);
                    holder.statsExpand.setVisibility(View.GONE);
                    holder.playLayoutExpand.setVisibility(View.GONE);
                }
                texts.get(position).isExpanded = !texts.get(position).isExpanded;

                texts.get(position).isClicked = !texts.get(position).isClicked;
                int color = getCardColorBasedOnClicks(texts.get(position).isClicked);
                holder.textCard.setCardBackgroundColor(color);
            }


        });
        holder.playTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, GameLoadingSplashScreenActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.putExtra("id", texts.get(position).getTextId());
                i.putExtra("title", texts.get(position).getTitle());
                i.putExtra("text", texts.get(position).getText());
                i.putExtra("composer", texts.get(position).getComposer());
                i.putExtra("theme", texts.get(position).getThemeName());
                i.putExtra("date", texts.get(position).getDate());
                i.putExtra("rating", texts.get(position).getRating());
                i.putExtra("playCount", texts.get(position).getNumberOfTimesPlayed());
                i.putExtra("bestScore", texts.get(position).getBestScore());
                i.putExtra("fastestSpeed", texts.get(position).getFastestSpeed());
                context.startActivity(i);
            }


        });


    }

    private int getCardColorBasedOnClicks(Boolean flag) {
        int colorResource = flag ? R.color.secondaryLightColor : R.color.cardview_light_background;
        return ContextCompat.getColor(context, colorResource);
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }
}
