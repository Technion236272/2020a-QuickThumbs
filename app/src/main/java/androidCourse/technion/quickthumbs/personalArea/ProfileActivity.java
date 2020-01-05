package androidCourse.technion.quickthumbs.personalArea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidCourse.technion.quickthumbs.MainActivity;
import androidCourse.technion.quickthumbs.R;
import androidCourse.technion.quickthumbs.game.GameActivity;

import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import static android.app.Activity.RESULT_OK;

public class ProfileActivity extends Fragment {
    static int PReqCode = 1;
    static int REQUESCODE = 1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private static final String TAG = GameActivity.class.getSimpleName();
    private ImageView profilePicture;
    private Uri pickedImgUri;
    //Next lines are Strings used as params
    public static String FACEBOOK_FIELD_PROFILE_IMAGE = "picture.type(large)";
    public static String FACEBOOK_FIELDS = "fields";
    public static final String MyPREFERENCES = "ProfilePrefs";
    private SharedPreferences sharedpreferences;
    private UploadTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        uploadTask = null;

        displayStatistics();

        setLogOutButton();


        profilePicture = getActivity().findViewById(R.id.profilePicture);

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 22) {

                    checkAndRequestForPermission();
                } else {
                    openGallery();
                }


            }
        });


        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        loadPictureFromSharedPrefrences();

    }

    private void loadPictureFromSharedPrefrences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Boolean isUsingGalleryPhoto = sharedPreferences.getBoolean("galleryProfilePicture", false);
        Boolean isUsingFacebookPhoto = sharedPreferences.getBoolean("facebookProfilePicture", false);
        Boolean isUsingGooglePhoto = sharedPreferences.getBoolean("googleProfilePicture", false);
        if (isUsingGalleryPhoto || isUsingFacebookPhoto || isUsingGooglePhoto) {
            String getImageBitmap = sharedPreferences.getString("ProfilePictureBitmapEncoded", "");
            profilePicture.setImageBitmap(decodeBase64(getImageBitmap));
            return;
        }
        setFacebookProfilePicture();
        isUsingFacebookPhoto = sharedPreferences.getBoolean("facebookProfilePicture", false);
        if (!isUsingFacebookPhoto) {
            setGoogleProfilePicture();
        }

    }

    private void savePictureOnSharedPrefrences(String checkIfAccountTypeUsed, Bitmap yourbitmap) {
        sharedpreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isLoadedToStorage", false);
        editor.putBoolean("galleryProfilePicture", false);
        editor.putBoolean("facebookProfilePicture", false);
        editor.putBoolean("googleProfilePicture", false);
        editor.putBoolean(checkIfAccountTypeUsed, true);//set the profile picture type
        editor.putString("ProfilePictureBitmapEncoded", encodeTobase64(yourbitmap));
        editor.apply();
    }

    // method for base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    // method for bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
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
                savePictureOnSharedPrefrences("googleProfilePicture", bitmap);
                profilePicture.setImageBitmap(bitmap);

                Bitmap bmpCopy = bitmap.copy(bitmap.getConfig(), true);
                MyTaskParams myTaskParams = new MyTaskParams("google",bmpCopy);
                new UploadToStorage().execute(myTaskParams);
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
                                            savePictureOnSharedPrefrences("facebookProfilePicture", bitmap);

                                            MyTaskParams myTaskParams = new MyTaskParams("facebook",bitmap);
                                            new UploadToStorage().execute(myTaskParams);
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


    private void checkAndRequestForPermission() {
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

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pickedImgUri);
                savePictureOnSharedPrefrences("galleryProfilePicture", bitmap);
                profilePicture.setImageBitmap(bitmap);

                Bitmap bmpCopy = bitmap.copy(bitmap.getConfig(), true);
                MyTaskParams myTaskParams = new MyTaskParams("gallery",bmpCopy);
                new UploadToStorage().execute(myTaskParams);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    // update user photo and name
    private void storeProfilePhoto(final String source, Bitmap pickedImgBitmap) {
        StorageReference storageRef = storage.getReference().child("users");
        StorageReference userStorage = storageRef.child(getUid());
        StorageReference profilePictureRef = userStorage.child("/profilePicture.JPEG");

        // Get the data from an ImageView as bytes
//        profilePicture.setDrawingCacheEnabled(true);
//        profilePicture.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        pickedImgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("source", source)
                .build();

        // Upload file and metadata to the path 'users/user_id/profilePicture.JPEG'
        uploadTask = profilePictureRef.putBytes(data, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                sharedpreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("isLoadedToStorage", true);
                showMessage("Profile picture uploaded successfully!");
            }
        });

    }

    private static class MyTaskParams {
        String photoSource;
        Bitmap imageBitmap;

        MyTaskParams(String photoSource, Bitmap imageUri) {
            this.photoSource = photoSource;
            this.imageBitmap = imageBitmap;
        }
    }

    private class UploadToStorage extends AsyncTask<MyTaskParams, Void, Void> {
        @Override
        protected Void doInBackground(MyTaskParams... params) {
            //cancel ongoing upload
            if (uploadTask != null && (uploadTask.isInProgress() || uploadTask.isPaused()) ) {
                uploadTask.cancel();
            }
            storeProfilePhoto(params[0].photoSource,params[0].imageBitmap);
            return null;
        }

        protected void onProgressUpdate() {
            //            setProgressPercent(progress[0]);
        }

        protected void onPostExecute() {
        //            showDialog("Downloaded " + result + " bytes");
        }

    }

    private class DownloadFromStorage extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //cancel ongoing upload
            if (uploadTask != null && (uploadTask.isInProgress() || uploadTask.isPaused()) ) {
                uploadTask.cancel();
            }
            loadProfilePhoto();
            return null;
        }

        protected void onProgressUpdate() {
            //            setProgressPercent(progress[0]);
        }

        protected void onPostExecute() {
            //            showDialog("Downloaded " + result + " bytes");
        }

    }

    private void loadProfilePhoto() {
        StorageReference storageRef = storage.getReference().child("users");
        StorageReference userStorage = storageRef.child(getUid());
        StorageReference profilePictureRef = userStorage.child(getUid() + "/profilePicture.JPEG");

        storageRef.child("users/"+getUid()+"/profilePicture.JPEG").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/Uid/profilePicture.JPEG'
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                    savePictureOnSharedPrefrences("galleryProfilePicture", bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    // simple method to show toast message
    private void showMessage(String message) {
        Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
    }

    private void setLogOutButton() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedInOnFacebook = accessToken != null && !accessToken.isExpired();
        if (isLoggedInOnFacebook){
            getView().findViewById(R.id.logOutButton).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.facebook_log_out_button).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            facebookLogOut(v);
                        }
                    }
            );
        }else{
            getView().findViewById(R.id.facebook_log_out_button).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.logOutButton).setOnClickListener(
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
        if(mAuth.getCurrentUser() !=null){
            return db.collection("users")
                    .document(mAuth.getUid());
        }else if (googleAccount != null) {
            return db.collection("users")
                    .document(googleAccount.getId());
        }else{
            return db.collection("users")
                    .document(accessToken.getUserId());
        }
//        return null;
    }

    private void displayStatistics() {
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
                                setStatisticsTextViews(avgAccuracy,avgWPM,avgCPM,totalScore);
                            } else {
                                Log.d(TAG, "No such document - reading statistics");
                            }
                        } else {
                            Log.d(TAG, "reading statistics failed with ", task.getException());
                        }
                    }
                });
    }

    private void setStatisticsTextViews(Double avgAccuracy, Double avgWPM, Double avgCPM, Double totalScore) {
        DecimalFormat df = new DecimalFormat("#.##");
        TextView avgAccuracyText = getView().findViewById(R.id.AccuracyValue);
        avgAccuracyText.setText(String.valueOf(df.format(avgAccuracy)));
        TextView avgWPMText = getView().findViewById(R.id.WPMValue);
        avgWPMText.setText(String.valueOf(df.format(avgWPM)));
        TextView avgCPMText = getView().findViewById(R.id.CPMValue);
        avgCPMText.setText(String.valueOf(df.format(avgCPM)));
        TextView totalScoreText = getView().findViewById(R.id.ScoreValue);
        totalScoreText.setText(String.valueOf(df.format(totalScore)));
    }

    public void moveToFriendsActivity(View view){
        Intent intent = new Intent(getActivity(),FriendsActivity.class);
        startActivity(intent);
    }

    public void facebookLogOut(View view){
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

    public void logOut(View view){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedInOnFacebook = accessToken != null && !accessToken.isExpired();
        if(currentUser != null && account==null){
            mAuth.signOut();
            Intent i = new Intent(getActivity(), MainActivity.class);
            getActivity().finish();
            startActivity(i);
        }else{
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(getActivity(),gso);
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
        if (currentUser!=null){
            return mAuth.getUid();
        }else if (account != null){
            return account.getId();
        }else{
            return accessToken.getUserId();
        }
    }

}
