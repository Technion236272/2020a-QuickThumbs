package androidCourse.technion.quickthumbs;

import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.AccessToken;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidCourse.technion.quickthumbs.personalArea.FriendsList.FriendAdaptor;
import androidCourse.technion.quickthumbs.personalArea.FriendsList.FriendItem;


public class GameInvitePopUp {
    private static final String TAG = GameInvitePopUp.class.getSimpleName();
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public static PopupWindow FriendInvitepopupWindow;
    public void showPopupWindow(final View view, View callingLayout, FriendAdaptor friendAdaptor) {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.game_invite_selector, null);

        //setRecyclerViewHeight(popupView);

        FriendInvitepopupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, //popup width
                ViewGroup.LayoutParams.WRAP_CONTENT, //popup height
                true
        );

        if(Build.VERSION.SDK_INT>=21){
            FriendInvitepopupWindow.setElevation(5.0f);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setBackToMainButton(popupView, FriendInvitepopupWindow);

        setOnDismissListener(FriendInvitepopupWindow, FriendInvitepopupWindow);

        FriendInvitepopupWindow.showAtLocation(callingLayout, Gravity.CENTER, 0, 0);

        setListAdapter(popupView,view, friendAdaptor);
    }

    private void setListAdapter(View popupView, View view, FriendAdaptor friendAdaptor) {
        recyclerView = popupView.findViewById(R.id.friendsRecyclerView);
        String uid = getUid(view);
        Query query = db.collection("users").document(uid).collection("friends")
                .orderBy("TotalScore", Query.Direction.DESCENDING);
        final FirestoreRecyclerOptions<FriendItem> friends = new FirestoreRecyclerOptions.Builder<FriendItem>()
                .setQuery(query, FriendItem.class)
                .build();
        //FriendAdaptor friendAdaptor = new FriendAdaptor(friends, view.getContext(), true);
        recyclerView.setAdapter(friendAdaptor);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    private String getUid(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(view.getContext());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (currentUser != null) {
            return mAuth.getUid();
        } else if (account != null) {
            return account.getId();
        } else {
            return accessToken.getUserId();
        }
    }

//    private void setRecyclerViewHeight(View popupView) {
//        recyclerView = popupView.findViewById(R.id.friendsListRecyclerView);
//        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
//        params.height=ViewGroup.LayoutParams.MATCH_PARENT;
//        recyclerView.setLayoutParams(params);
//    }

    private void setOnDismissListener(PopupWindow popupView, final PopupWindow popupWindow) {
        popupView.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() { }
        });
    }


    private void setBackToMainButton(View popupView, final PopupWindow mPopupWindow) {
        TextView closeButton = popupView.findViewById(R.id.friendsBackToMainPopUpButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });
    }
}

