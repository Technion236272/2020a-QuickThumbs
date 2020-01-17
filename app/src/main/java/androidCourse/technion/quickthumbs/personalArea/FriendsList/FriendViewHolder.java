package androidCourse.technion.quickthumbs.personalArea.FriendsList;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidCourse.technion.quickthumbs.R;

public class FriendViewHolder extends RecyclerView.ViewHolder{
    CardView friendCard;
    TextView friendName;
    TextView friendTotalScore;
    ImageView friendProfilePicture;
    FloatingActionButton addFriendButton;
    FloatingActionButton removeRequestButton;
    FloatingActionButton playWithFriend;


    public FriendViewHolder(@NonNull View itemView) {
        super(itemView);
        friendCard = itemView.findViewById(R.id.friendCardView);
        friendProfilePicture = itemView.findViewById(R.id.friendProfilePicture);
        friendName = itemView.findViewById(R.id.friendName);
        friendTotalScore = itemView.findViewById(R.id.friendTotalScore);
        addFriendButton = itemView.findViewById(R.id.addFriendButton);
        removeRequestButton = itemView.findViewById(R.id.removeRequestButton);
        playWithFriend = itemView.findViewById(R.id.playWithFriend);
    }
}
