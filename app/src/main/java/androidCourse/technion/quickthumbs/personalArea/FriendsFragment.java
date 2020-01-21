package androidCourse.technion.quickthumbs.personalArea;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.AccessToken;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidCourse.technion.quickthumbs.R;
import androidCourse.technion.quickthumbs.personalArea.FriendsList.FriendAdaptor;
import androidCourse.technion.quickthumbs.personalArea.FriendsList.FriendItem;

public class FriendsFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = FriendsFragment.class.getName();
    private RecyclerView friendsListRecyclerView;
    private FriendAdaptor friendAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_friends, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setFriendsList(view);
    }

    private void setFriendsList(View view) {
        //handling the recycler view part
        friendsListRecyclerView = view.findViewById(R.id.friendsListRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        friendsListRecyclerView.setHasFixedSize(true);
        // use a linear layout manager

        Query query = db.collection("users").document(getUid(view)).collection("friends")
                .orderBy("TotalScore", Query.Direction.DESCENDING);
        final FirestoreRecyclerOptions<FriendItem> friends = new FirestoreRecyclerOptions.Builder<FriendItem>()
                .setQuery(query, FriendItem.class)
                .build();
        friendAdaptor = new FriendAdaptor(friends, view.getContext(), true);
        friendsListRecyclerView.setAdapter(friendAdaptor);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        friendsListRecyclerView.setLayoutManager(layoutManager);
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

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        friendAdaptor.startListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        friendAdaptor.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        friendAdaptor.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        friendAdaptor.stopListening();
    }

}




