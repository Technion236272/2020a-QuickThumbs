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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidCourse.technion.quickthumbs.GameLoadingSplashScreenActivity;
import androidCourse.technion.quickthumbs.MainPager;
import androidCourse.technion.quickthumbs.MainUserActivity;
import androidCourse.technion.quickthumbs.R;
import androidCourse.technion.quickthumbs.Utils.CacheHandler;
import androidCourse.technion.quickthumbs.database.FriendsDatabaseHandler;
import androidCourse.technion.quickthumbs.database.GameDatabaseInviteHandler;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;

import static androidCourse.technion.quickthumbs.MainUserActivity.friendUid;
import static androidCourse.technion.quickthumbs.MainUserActivity.gameRoomsReference;
import static androidCourse.technion.quickthumbs.MainUserActivity.mainUserActivityInstance;
import static androidCourse.technion.quickthumbs.MainUserActivity.valueEventListener;
import static androidCourse.technion.quickthumbs.Utils.CacheHandler.getNextTextFromSelectedTheme;
import static androidCourse.technion.quickthumbs.personalArea.ProfileActivity.requestsIdList;

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
        if ( friendsList.get(position).isApproved()){
            holder.addFriendButton.setVisibility(View.GONE);
            holder.removeRequestButton.setVisibility(View.GONE);
        }
//        else{//TODO: set it back to on
//            holder.playWithFriend.setVisibility(View.GONE);
//        }
        setPlayButtonListener(holder, friendsList.get(position));
        setAddFriendButton(holder, friendsList.get(position),position);
        setRemoveRequestButton(holder, friendsList.get(position),position);

    }

    private void setPlayButtonListener(@NonNull FriendViewHolder holder, final FriendItem friendItem) {
        holder.playWithFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here last search for a game is canceled
//                gameRoomsReference.removeEventListener(valueEventListener);//TODO: if alreadt=y searching then remove it
                //here the room for the game is created
//                MainUserActivity.Myparam myparam = new MainUserActivity.Myparam(friendItem.getId());
//                new MainUserActivity.FetchRandomTextForFriendsRoom().execute(myparam);
//                friendUid = friendItem.getId();

                TextDataRow textCardItem = getNextTextFromSelectedTheme(getRandomThemeName());
                MainUserActivity.Myparam myparam = new MainUserActivity.Myparam(friendItem, textCardItem.getTextId());
                Class<?> c = MainUserActivity.class;
                try {
                    Method method = c.getDeclaredMethod("createSpecialRoom", MainUserActivity.Myparam.class);
                    method.invoke(mainUserActivityInstance, myparam);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private String getRandomThemeName() {
        CacheHandler cacheHandler = new CacheHandler(context);
        Map<String, Boolean> allUserThemes = cacheHandler.loadThemesFromSharedPreferences();
        List<String> userChosenThemes = new LinkedList<>();
        for (String theme : allUserThemes.keySet()) {
            if (allUserThemes.get(theme)) {
                userChosenThemes.add(theme);
            }
        }
        // if the user has no themes selected we will choose all for him
        if (userChosenThemes.isEmpty()) {
            for (String theme : allUserThemes.keySet()) {
                userChosenThemes.add(theme);
            }
        }
        //choose random theme from the user themes
        int themesListSize = userChosenThemes.size();
        return userChosenThemes.get(new Random().nextInt(themesListSize));
    }

    private void setAddFriendButton(@NonNull FriendViewHolder holder, final FriendItem friendItem, final int position) {
        holder.addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsDatabaseHandler friendsDatabaseHandler = new FriendsDatabaseHandler();
                friendsDatabaseHandler.addFriend(friendItem.getId(), context);
                friendItem.setApproved(true);
                requestsIdList.remove(position);
            }

        });
    }

    private void setRemoveRequestButton(@NonNull FriendViewHolder holder, final FriendItem friendItem, final int position) {
        holder.removeRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsDatabaseHandler friendsDatabaseHandler = new FriendsDatabaseHandler();
                friendsDatabaseHandler.removeRequest(friendItem.getId(), context);
                requestsIdList.remove(position);
            }

        });
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
