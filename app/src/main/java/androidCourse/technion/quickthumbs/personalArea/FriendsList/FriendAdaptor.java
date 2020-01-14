package androidCourse.technion.quickthumbs.personalArea.FriendsList;

import android.content.Context;
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

import androidCourse.technion.quickthumbs.GameLoadingSplashScreenActivity;
import androidCourse.technion.quickthumbs.R;

public class FriendAdaptor extends RecyclerView.Adapter<FriendViewHolder>{
    List<FriendItem> friendsList;
    Context context;
    private static final String TAG = FriendAdaptor.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public FriendAdaptor(List<FriendItem> friendsList, Context context) {
        this.context = context;
        this.friendsList = friendsList;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
        FriendViewHolder holder = new FriendViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendViewHolder holder, final int position) {
        holder.friendName.setText(friendsList.get(position).getName());
        if (friendsList.get(position).getProfilePicture() != null){
            holder.friendProfilePicture.setImageBitmap(friendsList.get(position).getProfilePicture());
        }
        holder.friendTotalScore.setText(friendsList.get(position).getTotalScore().toString());


    }

    private int getCardColorBasedOnClicks(Boolean flag) {
        int colorResource = flag ? R.color.secondaryLightColor : R.color.cardview_light_background;
        return ContextCompat.getColor(context, colorResource);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

}
