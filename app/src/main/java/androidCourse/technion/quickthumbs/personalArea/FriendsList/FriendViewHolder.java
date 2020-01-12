package androidCourse.technion.quickthumbs.personalArea.FriendsList;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import androidCourse.technion.quickthumbs.R;

public class FriendViewHolder extends RecyclerView.ViewHolder{
    CardView friendCard;
    TextView friendName;
    TextView friendTotalScore;
    ImageView friendProfilePicture;


    public FriendViewHolder(@NonNull View itemView) {
        super(itemView);
        friendCard = itemView.findViewById(R.id.friendCardView);
        friendProfilePicture = itemView.findViewById(R.id.friendProfilePicture);
        friendName = itemView.findViewById(R.id.friendName);
        friendTotalScore = itemView.findViewById(R.id.friendTotalScore);
    }
}
