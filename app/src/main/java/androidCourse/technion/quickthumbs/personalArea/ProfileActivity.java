package androidCourse.technion.quickthumbs.personalArea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidCourse.technion.quickthumbs.MainActivity;
import androidCourse.technion.quickthumbs.R;
import androidCourse.technion.quickthumbs.Utils.CacheHandler;
import androidCourse.technion.quickthumbs.game.GameActivity;
import androidCourse.technion.quickthumbs.personalArea.FriendsList.FriendAdaptor;
import androidCourse.technion.quickthumbs.personalArea.FriendsList.FriendItem;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class ProfileActivity extends Fragment {
    static int PReqCode = 1;
    static int CReqCode = 2;
    static int REQUESCODE = 1;
    static int CAMERACODE = 2;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private static final String TAG = GameActivity.class.getSimpleName();
    public static ImageView profilePicture;
    private ImageView galleryButton;
    private ImageButton cameraButton;
    private Uri pickedImgUri;
    private Uri cameraPhotoURI;
    //Next lines are Strings used as params
    public static String FACEBOOK_FIELD_PROFILE_IMAGE = "picture.type(large)";
    public static String FACEBOOK_FIELDS = "fields";
    private SharedPreferences sharedpreferences;
    private CacheHandler cacheHandler;
    private RecyclerView friendsListRecyclerView;
    final ArrayList<FriendItem> friendsList = new ArrayList<>();
    private DocumentSnapshot lastSnapShot = null;
    boolean noMoreLoading;
    private int howMuchToLoadEachScroll;
    private HashMap<String, Boolean> friendsMap;
    private List<String> friendsIdList;
    HashMap<String, Boolean> loadedFriendsIDs = new HashMap<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        howMuchToLoadEachScroll = 3;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        cameraPhotoURI = null;
        profilePicture = view.findViewById(R.id.profilePicture);
        cacheHandler = new CacheHandler(getContext());

        friendsListRecyclerView = view.findViewById(R.id.friendsListRecyclerView);
        friendsListRecyclerView.setHasFixedSize(true);

        friendsMap = new HashMap<>();
        friendsIdList = new LinkedList<>();
        checkIfUserHasFriends();


        displayStatistics(view);

        setLogOutButton(view);
        setSendFriendRequestButton(view);

        profilePictureSettings(view);

        view.findViewById(R.id.shareButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onShareClicked();
                    }
                }
        );
    }

    private void setSendFriendRequestButton(final View view) {
        view.findViewById(R.id.sendFriendRequestButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText email = view.findViewById(R.id.emailEditText);
                        String friendEmail = email.getText().toString();
                        if (friendEmail != null) {
                            addFriendRequestToDatabaseIfEmailExists(friendEmail);
                        }
                    }
                }
        );
    }

    private void addFriendRequestToDatabaseIfEmailExists(String friendEmail) {
        db.collection("users").whereEqualTo("email", friendEmail).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String userDocumentId = getUserDocument().getId();
                            for (QueryDocumentSnapshot friendDocument : task.getResult()) {
                                addFriendRequestToSenderFriendsCollection(userDocumentId, friendDocument.getId());
                                addFriendRequestToReceiverFriendsCollection(userDocumentId, friendDocument.getId());
                            }
                        }
                    }
                });
    }

    private void addFriendRequestToSenderFriendsCollection(final String userDocumentId, final String friendDocumentId) {
        //adds only if the new friend does not exist in the collection or is deleted
        final Map<String, Object> friendMap = new HashMap<>();
        friendMap.put("status", "requestSent");
        getUserDocument().collection("friends").document(friendDocumentId)
                .get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot friendDocumentSnapshot = task.getResult();
                            if (friendDocumentSnapshot.exists()) {
                                String current_status = friendDocumentSnapshot.getString("status");
                                if (current_status.equals("deleted")) {
                                    addFriend(userDocumentId, friendDocumentId, friendMap);
                                }
                            } else {
                                addFriend(userDocumentId, friendDocumentId, friendMap);
                            }
                        } else {
                            addFriend(userDocumentId, friendDocumentId, friendMap);
                        }
                    }
                }
        );
    }

    private void addFriend(String userDocumentId, String friendDocumentId, Map<String, Object> friendMap) {
        db.collection("users").document(userDocumentId).collection("friends")
                .document(friendDocumentId)
                .set(friendMap, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        new CacheHandler.FriendsUpdateFrindsList().execute();
                        Log.d(TAG, "friend document set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error setting friend document", e);
                    }
                });
    }

    private void addFriendRequestToReceiverFriendsCollection(final String userDocumentId, final String friendDocumentId) {
        //adds only if the new friend does not exist in the collection or is deleted
        final Map<String, Object> friendMap = new HashMap<>();
        friendMap.put("status", "requestReceived");
        db.collection("users").document(friendDocumentId).collection("friends")
                .document(userDocumentId)
                .get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDocumentSnapshot = task.getResult();
                            if (userDocumentSnapshot.exists()) {
                                String current_status = userDocumentSnapshot.getString("status");
                                if (current_status.equals("deleted")) {
                                    addFriend(friendDocumentId, userDocumentId, friendMap);
                                }
                            } else {
                                addFriend(friendDocumentId, userDocumentId, friendMap);
                            }
                        } else {
                            addFriend(friendDocumentId, userDocumentId, friendMap);
                        }
                    }
                }
        );
    }

    private void checkIfUserHasFriends() {
        friendsMap = cacheHandler.getUserfriendsMap();
        friendsIdList = cacheHandler.getUserfriendsList();
        if (friendsList.size() != 0 ){
            fetchPersonalFriendsList();
            setRecyclerViewScroller();
        }
    }


    private void setRecyclerViewScroller() {
        friendsListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling up
                    Log.i("RecyclerView scrolled: ", "scroll down!");
                    if (friendsList.size() != 0 && !noMoreLoading) {
                        refillFriendsList(recyclerView);
                    }
                } else if (dy < 0) {
                    // Scrolling down
                    Log.i("RecyclerView scrolled: ", "scroll up!");
                } else if (dx < 0) {
                    getActivity().finish();
                    //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            }
        });
    }

    private void refillFriendsList(RecyclerView recyclerView) {
        db.collection("users").whereIn("uid", friendsIdList)
                .orderBy("email", Query.Direction.DESCENDING)
                .startAfter(lastSnapShot).limit(howMuchToLoadEachScroll).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            fillFriendsList(task);
                            friendsListRecyclerView.getAdapter().notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "getAllThemes:" + "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void fetchPersonalFriendsList() {
        db.collection("users").whereIn("uid", friendsIdList)
                .orderBy("email", Query.Direction.DESCENDING).limit(5).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            fillFriendsList(task);
                            setFriendAdaptor();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void fillFriendsList(Task<QuerySnapshot> task) {
        if (task.getResult() == null) {
            return;
        }
        for (DocumentSnapshot document : task.getResult()) {
            String status = document.getString("status");
            if (status == null || !status.equals("accepted")) continue;
            FriendItem item = new FriendItem(document, status);
            if (loadedFriendsIDs.get(document.getId()) == null) {
                loadedFriendsIDs.put(document.getId(), true);
                friendsList.add(item);
                lastSnapShot = document;
            } else {
                noMoreLoading = true;
                friendsListRecyclerView.clearOnScrollListeners();
            }
        }
    }

    private void setFriendAdaptor() {
        FriendAdaptor adapter = new FriendAdaptor(friendsList, getActivity());
        friendsListRecyclerView.setAdapter(adapter);
        friendsListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void profilePictureSettings(View view) {
        setGalleryPictureLoadListener(view);

        setCameraPictureLoadListener(view);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        boolean isImageLoaded = cacheHandler.loadPictureFromSharedPrefrences(profilePicture);
        if (isImageLoaded) {
            return;
        }
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedInOnFacebook = accessToken != null && !accessToken.isExpired();
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (isLoggedInOnFacebook) {
            setFacebookProfilePicture();
        } else if (googleAccount != null) {
            setGoogleProfilePicture();
        }
    }

    private void setCameraPictureLoadListener(View view) {
        cameraButton = view.findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 22) {

                    checkAndRequestForCameraPermission();
                } else {
                    openCamera();
                }


            }
        });
    }

    private void setGalleryPictureLoadListener(View view) {
        galleryButton = view.findViewById(R.id.galleryButton);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 22) {

                    checkAndRequestForGalleyPermission();
                } else {
                    openGallery();
                }


            }
        });
    }


    private void setGoogleProfilePicture() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (googleAccount != null) {
            URL url = null;
            Uri photoUrl = googleAccount.getPhotoUrl();
            try {
                url = new URL(photoUrl.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                InputStream in = (InputStream) url.getContent();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                cacheHandler.savePictureOnSharedPrefrences("googleProfilePicture", bitmap);
                profilePicture.setImageBitmap(bitmap);

                Bitmap bmpCopy = bitmap.copy(bitmap.getConfig(), true);
                CacheHandler.MyTaskParams myTaskParams = new CacheHandler.MyTaskParams("google", bmpCopy, profilePicture);
                new CacheHandler.UploadToStorage().execute(myTaskParams);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setFacebookProfilePicture() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn) {
            //If the user is LoggedIn then continue
            Bundle parameters = new Bundle();
            parameters.putString(FACEBOOK_FIELDS, FACEBOOK_FIELD_PROFILE_IMAGE);
            /* make the API call */
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "me",
                    parameters,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            /* handle the result */
                            if (response != null) {
                                try {
                                    JSONObject data = response.getJSONObject();
                                    //Log.w(TAG, "Data: " + response.toString());

                                    if (data.has("picture")) {
                                        boolean is_silhouette = data.getJSONObject("picture").getJSONObject("data").getBoolean("is_silhouette");
                                        if (!is_silhouette) {
                                            //Silhouette is used when the FB user has no upload any profile image
                                            URL profilePicUrl = new URL(data.getJSONObject("picture").getJSONObject("data").getString("url"));
                                            InputStream in = (InputStream) profilePicUrl.getContent();
                                            Bitmap bitmap = BitmapFactory.decodeStream(in);
                                            profilePicture.setImageBitmap(bitmap);
                                            cacheHandler.savePictureOnSharedPrefrences("facebookProfilePicture", bitmap);

                                            CacheHandler.MyTaskParams myTaskParams = new CacheHandler.MyTaskParams("facebook", bitmap, profilePicture);
                                            new CacheHandler.UploadToStorage().execute(myTaskParams);
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.w(TAG, "Response null");
                            }
                        }
                    }
            ).executeAsync();
        }
    }


    private void checkAndRequestForGalleyPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this.getActivity(), "Please accept for required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        } else {
            openGallery();
        }
    }

    private void checkAndRequestForCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                Toast.makeText(this.getActivity(), "Please accept for required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        CReqCode);
            }

        } else {
            openCamera();
        }
    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    private void openCamera() {
        //TODO: open gallery intent and wait for user to pick an image !
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraPhotoURI = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", getOutputMediaFile(MEDIA_TYPE_IMAGE));
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoURI);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(cameraIntent, CAMERACODE);

    }

    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cameraButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            try {
                Bitmap bitmapOriginal = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pickedImgUri);
                Matrix matrix = new Matrix();
                matrix.postScale(0.5f, 0.5f);
                Bitmap bitmap = Bitmap.createBitmap(bitmapOriginal, 100, 100, 100, 100, matrix, true);
                cacheHandler.savePictureOnSharedPrefrences("galleryProfilePicture", bitmapOriginal);
                profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmapOriginal, 200, 200, false));

                Bitmap bmpCopy = bitmap.copy(bitmap.getConfig(), true);
                CacheHandler.MyTaskParams myTaskParams = new CacheHandler.MyTaskParams("gallery", bmpCopy, profilePicture);
                new CacheHandler.UploadToStorage().execute(myTaskParams);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (resultCode == RESULT_OK && requestCode == CAMERACODE) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            if (cameraPhotoURI != null) {
                try {
                    Bitmap bitmapOriginal = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), cameraPhotoURI);
                    Matrix matrix = new Matrix();
                    matrix.postScale(0.5f, 0.5f);
                    Bitmap bitmap = Bitmap.createBitmap(bitmapOriginal, 100, 100, 100, 100, matrix, true);
                    cacheHandler.savePictureOnSharedPrefrences("galleryProfilePicture", bitmapOriginal);
                    profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmapOriginal, 200, 200, false));

                    Bitmap bmpCopy = bitmap.copy(bitmap.getConfig(), true);
                    CacheHandler.MyTaskParams myTaskParams = new CacheHandler.MyTaskParams("gallery", bmpCopy, profilePicture);
                    new CacheHandler.UploadToStorage().execute(myTaskParams);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    // simple method to show toast message
    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void setLogOutButton(View view) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedInOnFacebook = accessToken != null && !accessToken.isExpired();
        if (isLoggedInOnFacebook) {
            view.findViewById(R.id.logOutButton).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.facebook_log_out_button).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            facebookLogOut(v);
                        }
                    }
            );
        } else {
            view.findViewById(R.id.facebook_log_out_button).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.logOutButton).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logOut(v);
                        }
                    }
            );
        }
    }

    private DocumentReference getUserDocument() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (mAuth.getCurrentUser() != null) {
            return db.collection("users")
                    .document(mAuth.getUid());
        } else if (googleAccount != null) {
            return db.collection("users")
                    .document(googleAccount.getId());
        } else {
            return db.collection("users")
                    .document(accessToken.getUserId());
        }
//        return null;
    }

    private void displayStatistics(final View view) {
        getUserDocument().collection("stats").document("statistics").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                Double avgAccuracy = document.getDouble("avgAccuracy");
                                Double avgWPM = document.getDouble("avgWPM");
                                Double avgCPM = document.getDouble("avgCPM");
                                Double totalScore = document.getDouble("TotalScore");
                                setStatisticsTextViews(view, avgAccuracy, avgWPM, avgCPM, totalScore);
                            } else {
                                Log.d(TAG, "No such document - reading statistics");
                            }
                        } else {
                            Log.d(TAG, "reading statistics failed with ", task.getException());
                        }
                    }
                });
    }

    private void setStatisticsTextViews(View view, Double avgAccuracy, Double avgWPM, Double avgCPM, Double totalScore) {
        DecimalFormat df = new DecimalFormat("#.##");
        TextView avgAccuracyText = view.findViewById(R.id.AccuracyValue);
        avgAccuracyText.setText(String.valueOf(df.format(avgAccuracy)));
        TextView avgWPMText = view.findViewById(R.id.WPMValue);
        avgWPMText.setText(String.valueOf(df.format(avgWPM)));
        TextView avgCPMText = view.findViewById(R.id.CPMValue);
        avgCPMText.setText(String.valueOf(df.format(avgCPM)));
        TextView totalScoreText = view.findViewById(R.id.ScoreValue);
        totalScoreText.setText(String.valueOf(df.format(totalScore)));
    }

    public void moveToFriendsActivity(View view) {
        Intent intent = new Intent(getActivity(), FriendsActivity.class);
        startActivity(intent);
    }

    public void facebookLogOut(View view) {
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent i = new Intent(getActivity(), MainActivity.class);
                getActivity().finish();
                startActivity(i);
            }
        }).executeAsync();
    }

    public void logOut(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedInOnFacebook = accessToken != null && !accessToken.isExpired();
        if (currentUser != null && account == null) {
            mAuth.signOut();
            Intent i = new Intent(getActivity(), MainActivity.class);
            getActivity().finish();
            startActivity(i);
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(getActivity(), gso);
            client.signOut()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mAuth.signOut();
                            Intent i = new Intent(getActivity(), MainActivity.class);
                            getActivity().finish();
                            startActivity(i);
                        }
                    });
        }
    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (currentUser != null) {
            return mAuth.getUid();
        } else if (account != null) {
            return account.getId();
        } else {
            return accessToken.getUserId();
        }
    }

    private static Uri generateContentLink() {
        Uri baseUrl = Uri.parse("https://play.google.com/store/apps/details?id=androidCourse.technion.quickthumbs");
        String domain = "https://quickthumbs.page.link";

        DynamicLink link = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLink(baseUrl)
                .setDomainUriPrefix(domain)
                .setAndroidParameters
                        (new DynamicLink.AndroidParameters.Builder("androidCourse.technion.quickthumbs").build())
                .buildDynamicLink();

        return link.getUri();
    }

    private void onShareClicked() {
        Uri link = generateContentLink();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, link.toString());

        startActivity(Intent.createChooser(intent, "Share Link"));
    }

}



