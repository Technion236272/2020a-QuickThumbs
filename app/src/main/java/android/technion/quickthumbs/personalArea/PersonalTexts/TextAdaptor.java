package android.technion.quickthumbs.personalArea.PersonalTexts;

import android.content.Context;
import android.technion.quickthumbs.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TextAdaptor extends RecyclerView.Adapter<TextViewHolder>{
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

    public int getThemePictureId (@NonNull TextViewHolder holder, final int position) {
        switch (texts.get(position).getThemeName()){
            case "Comedy":
                return R.drawable.funny_trump_icon;
            case "Music":
                return R.drawable.music_icon;
            case "Science":
                return R.drawable.stupid_science_icon;
            case "Games":
                return R.drawable.games_icon;
            case "Literature":
                return R.drawable.literature_icon;
            case "Movies":
            default:
                return R.drawable.movie_icon;
        }
    }


    public void setStarRanking (@NonNull TextViewHolder holder, final int position){
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
        holder.textTheme.setImageResource(getThemePictureId(holder,position));
        holder.textDescription.setText(texts.get(position).getText());
        holder.fastestSpeed.setText(texts.get(position).getFastestSpeed());
        holder.bestScore.setText(texts.get(position).getBestScore());
        holder.numberOfTimesTextPlayed.setText(texts.get(position).getNumberOfTimesPlayed());
        setStarRanking(holder, position);
        int color = getCardColorBasedOnClicks((texts.get(position).isClicked));
        holder.textCard.setCardBackgroundColor(color);

        holder.viewMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! texts.get(position).isExpanded){
                    holder.textExpand.setVisibility(View.VISIBLE);
                    holder.titleExpand.setVisibility(View.VISIBLE);
                    holder.statsExpand.setVisibility(View.VISIBLE);
                }else{
                    holder.textExpand.setVisibility(View.GONE);
                    holder.titleExpand.setVisibility(View.GONE);
                    holder.statsExpand.setVisibility(View.GONE);
                }
                texts.get(position).isExpanded = ! texts.get(position).isExpanded;

                texts.get(position).isClicked = ! texts.get(position).isClicked;
                int color = getCardColorBasedOnClicks(texts.get(position).isClicked);
                holder.textCard.setCardBackgroundColor(color);
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