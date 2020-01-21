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

public class FriendRequestsFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = FriendRequestsFragment.class.getName();
    private RecyclerView requestsListRecyclerView;
    private FriendAdaptor requestAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friend_requests_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setRequestsList(view);
    }
    private void setRequestsList(View view) {
        //handling the recycler view part
        requestsListRecyclerView = view.findViewById(R.id.requestsListRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        requestsListRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        requestsListRecyclerView.setLayoutManager(layoutManager);

        Query query = db.collection("users").document(getUid(view)).collection("requests")
                .orderBy("email", Query.Direction.DESCENDING);
        final FirestoreRecyclerOptions<FriendItem> requests = new FirestoreRecyclerOptions.Builder<FriendItem>()
                .setQuery(query, FriendItem.class)
                .build();
        requestAdaptor = new FriendAdaptor(requests, view.getContext(), false);
        requestsListRecyclerView.setAdapter(requestAdaptor);
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
        requestAdaptor.startListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requestAdaptor.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        requestAdaptor.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        requestAdaptor.stopListening();
    }

}




