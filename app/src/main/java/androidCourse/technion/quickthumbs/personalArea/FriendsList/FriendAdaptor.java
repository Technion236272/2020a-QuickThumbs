package androidCourse.technion.quickthumbs.personalArea.FriendsList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

import static androidCourse.technion.quickthumbs.GameInvitePopUp.FriendInvitepopupWindow;
import static androidCourse.technion.quickthumbs.MainUserActivity.friendUid;
import static androidCourse.technion.quickthumbs.MainUserActivity.gameRoomsReference;
import static androidCourse.technion.quickthumbs.MainUserActivity.mainUserActivityInstance;
import static androidCourse.technion.quickthumbs.MainUserActivity.valueEventListener;
import static androidCourse.technion.quickthumbs.Utils.CacheHandler.getNextTextFromSelectedTheme;

public class FriendAdaptor extends FirestoreRecyclerAdapter<FriendItem, FriendViewHolder> {
    FirestoreRecyclerOptions<FriendItem> friendsList;
    Context context;
    Boolean isFriend;
    private static final String TAG = FriendAdaptor.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public FriendAdaptor(@NonNull FirestoreRecyclerOptions<FriendItem> friendsList, Context context, Boolean isFriend) {
        super(friendsList);
        this.context = context;
        this.friendsList = friendsList;
        this.isFriend = isFriend;
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
    public void onBindViewHolder(@NonNull final FriendViewHolder holder, final int position, @NonNull final FriendItem friendItem) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("users");
        StorageReference userStorage = storageRef.child(friendItem.getuid());
        StorageReference profilePictureRef = userStorage.child("/profilePicture.JPEG");
        final long ONE_MEGABYTE = 1024 * 1024;
        try {
            profilePictureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    holder.friendProfilePicture.setImageBitmap(picture);
                    setHolderData(holder, position, friendItem);
//                showMessage("picture was loaded from storage");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    setHolderData(holder, position, friendItem);
                }
            });
        } catch (Exception e) {
            setHolderData(holder, position, friendItem);
            //no such picture exist
        }
        setHolderData(holder, position, friendItem);

    }

    private void setHolderData(@NonNull FriendViewHolder holder, int position, @NonNull FriendItem friendItem) {
        holder.friendName.setText(friendItem.getName());
        holder.friendTotalScore.setText(String.valueOf(friendItem.getTotalScore()));
        if (isFriend) {
            holder.addFriendButton.setVisibility(View.GONE);
            holder.removeRequestButton.setVisibility(View.GONE);
        } else {
            holder.playWithFriend.setVisibility(View.GONE);
            holder.friendTotalScore.setVisibility(View.GONE);
            holder.totalScoreHeader.setVisibility(View.GONE);
        }
        setPlayButtonListener(holder, friendItem);
        setAddFriendButton(holder, friendItem, position);
        setRemoveRequestButton(holder, friendItem, position);
    }

    private void setPlayButtonListener(@NonNull FriendViewHolder holder, final FriendItem friendItem) {
        holder.playWithFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here last search for a game is canceled
                closeOtherMultiPlayerCalls();
                //here the room for the game is created
                createGameInviteCall(friendItem);
                //close popUp invite room
                if (FriendInvitepopupWindow != null) {
                    FriendInvitepopupWindow.dismiss();
                }
            }

        });
    }

    private void closeOtherMultiPlayerCalls() {
        Class<?> c = MainUserActivity.class;
        try {
            Method method = c.getDeclaredMethod("setCloseMultiplayerSerchButtonListener", null);
            method.invoke(mainUserActivityInstance, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void createGameInviteCall(FriendItem friendItem) {
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
                friendsDatabaseHandler.addFriend(friendItem.getuid(), context);
            }

        });
    }

    private void setRemoveRequestButton(@NonNull FriendViewHolder holder, final FriendItem friendItem, final int position) {
        holder.removeRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsDatabaseHandler friendsDatabaseHandler = new FriendsDatabaseHandler();
                friendsDatabaseHandler.removeRequest(friendItem.getuid(), context);
            }

        });
    }

    private int getCardColorBasedOnClicks(Boolean flag) {
        int colorResource = flag ? R.color.secondaryLightColor : R.color.cardview_light_background;
        return ContextCompat.getColor(context, colorResource);
    }

    @Override
    public int getItemCount() {
        return friendsList.getSnapshots().size();
    }

}
