package androidCourse.technion.quickthumbs.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import androidCourse.technion.quickthumbs.AddTextActivity;
import androidCourse.technion.quickthumbs.game.GameActivity;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import androidCourse.technion.quickthumbs.theme.ThemeDataRow;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static androidCourse.technion.quickthumbs.personalArea.ProfileActivity.profilePicture;

public class CacheHandler {
    //Get Cache Folder in Android
    private static final String TAG = CacheHandler.class.getSimpleName();
    private final String[] themesNames = {"Comedy", "Music", "Movies", "Science", "Games", "Literature"};
    private static Context context;
    private FirebaseFirestore db;
    private static FirebaseAuth mAuth;
    private static String MyPREFERENCES;
    private static FirebaseStorage storage;
    private static UploadTask uploadTask;

    public CacheHandler(Context context) {
        this.context = context;
        uploadTask = null;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        MyPREFERENCES = getUid() + "_preferences";
    }

    public void getPersonalThemesDataFromDB() {
        final Map<String, Boolean> selectedThemes = new HashMap<>();
        db.collection("users").document(getUid()).collection("themes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Boolean currentText = document.getBoolean("isChosen");
                                selectedThemes.put(document.getId(), currentText);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            for (int i = 0; i < themesNames.length; i++) {
                                //this is for the layout show
                                selectedThemes.put(themesNames[i], true);
                                //this is for the db
                                Map<String, Object> currentTheme = new HashMap<>();
                                currentTheme.put("isChosen", true);
                                db.collection("users/" + getUid() + "/themes").document(themesNames[i]).set(currentTheme, SetOptions.merge());
                            }
                        }
                        saveThemesToSharedPreferences(selectedThemes);
                    }
                });
    }

    public void updateUserThemesSelectionOnDB() {
        Map<String, Boolean> selectedThemes = loadThemesFromSharedPreferences();
        for (String theme : selectedThemes.keySet()) {
            final Map<String, Object> themeObject = new HashMap<>();
            themeObject.put("isChosen", selectedThemes.get(theme));
            db.collection("users").document(getUid()).collection("themes").document(theme)
                    .set(themeObject, SetOptions.merge());
        }
    }

    public void saveThemesToSharedPreferences(Map<String, Boolean> selected) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(selected);
        editor.putString("chosen themes", json);
        editor.apply();
    }

    public Map<String, Boolean> loadThemesFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("chosen themes", null);
        Type type = new TypeToken<Map<String, Boolean>>() {
        }.getType();
        Map<String, Boolean> loadedThemes = gson.fromJson(json, type);

        Map<String, Boolean> selected = new HashMap<>();
        for (String theme : themesNames) {
            selected.put(theme, false);
        }
        if (loadedThemes != null) {
            for (String theme : loadedThemes.keySet()) {
                selected.put(theme, loadedThemes.get(theme));
            }
        }

        return selected;
    }

    private static String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (currentUser != null) {
            return mAuth.getUid();
        } else if (account != null) {
            return account.getId();
        } else if (accessToken != null){
            return accessToken.getUserId();
        }
        else {
            return mAuth.toString();
        }
    }

    public static class DownloadFromStorage extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //cancel ongoing upload
            if (uploadTask != null && (uploadTask.isInProgress() || uploadTask.isPaused())) {
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

    private static void loadProfilePhoto() {
        StorageReference storageRef = storage.getReference().child("users");
        StorageReference userStorage = storageRef.child(getUid());
        StorageReference profilePictureRef = userStorage.child("/profilePicture.JPEG");

//        profilePictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                // Got the download URL for 'users/Uid/profilePicture.JPEG'
//                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
//                    savePictureOnSharedPrefrences("galleryProfilePicture", bitmap);
//                    showMessage("picture was loaded from storage");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle any errors
//                showMessage("picture not found on storage");
//            }
//        });
        final long ONE_MEGABYTE = 1024 * 1024;
        profilePictureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                savePictureOnSharedPrefrences("galleryProfilePicture", bitmap);
                profilePicture.setImageBitmap(bitmap);
                showMessage("picture was loaded from storage");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                showMessage("picture not found on storage");
            }
        });
    }

    public boolean loadPictureFromSharedPrefrences(ImageView profilePicture) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Boolean isUsingGalleryPhoto = sharedPreferences.getBoolean("galleryProfilePicture", false);
        Boolean isUsingFacebookPhoto = sharedPreferences.getBoolean("facebookProfilePicture", false);
        Boolean isUsingGooglePhoto = sharedPreferences.getBoolean("googleProfilePicture", false);
        if (isUsingGalleryPhoto || isUsingFacebookPhoto || isUsingGooglePhoto) {
            String getImageBitmap = sharedPreferences.getString("ProfilePictureBitmapEncoded", "");
            profilePicture.setImageBitmap(decodeBase64(getImageBitmap));
            return true;
        }
        return false;
    }

    public static void savePictureOnSharedPrefrences(String checkIfAccountTypeUsed, Bitmap yourbitmap) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
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

    // update user photo and name
    private static void storeProfilePhoto(final String source, Bitmap pickedImgBitmap, ImageView profilePicture) {
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
                SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("isLoadedToStorage", true);
                showMessage("Profile picture uploaded successfully!");
            }
        });

    }

    public static class MyTaskParams {
        String photoSource;
        Bitmap imageBitmap;
        ImageView profilePicture;

        public MyTaskParams(String photoSource, Bitmap imageUri, ImageView profilePicture) {
            this.photoSource = photoSource;
            this.imageBitmap = imageBitmap;
            this.profilePicture = profilePicture;
        }
    }

    public static class UploadToStorage extends AsyncTask<MyTaskParams, Void, Void> {
        @Override
        protected Void doInBackground(MyTaskParams... params) {
            //cancel ongoing upload
            if (uploadTask != null && (uploadTask.isInProgress() || uploadTask.isPaused())) {
                uploadTask.cancel();
            }
            storeProfilePhoto(params[0].photoSource, params[0].imageBitmap, params[0].profilePicture);
            return null;
        }

        protected void onProgressUpdate() {
            //            setProgressPercent(progress[0]);
        }

        protected void onPostExecute() {
            //            showDialog("Downloaded " + result + " bytes");
        }

    }

    // simple method to show toast message
    private static void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    public static File getCacheFolder(Context context) {
        File cacheDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(Environment.getExternalStorageDirectory(), "cachefolder");
            if (!cacheDir.isDirectory()) {
                cacheDir.mkdirs();
            }
        }

        if (!cacheDir.isDirectory()) {
            cacheDir = context.getCacheDir(); //get system cache folder
        }

        return cacheDir;
    }

    //Get App Data Folder in Android
    public static File getDataFolder(Context context) {
        File dataDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dataDir = new File(Environment.getExternalStorageDirectory(), "myappdata");
            if (!dataDir.isDirectory()) {
                dataDir.mkdirs();
            }
        }

        if (!dataDir.isDirectory()) {
            dataDir = context.getFilesDir();
        }

        return dataDir;
    }

    public static void writeFileIntoCacheFolder(Context context, TextDataRow textDataRow) throws IOException {
        byte[] insert = serializeObject(textDataRow);
        // open input stream test.txt for reading purpose.
        File cacheDir = getCacheFolder(context);
        InputStream inStream = new FileInputStream(cacheDir.getPath());
        InputStream inputStream = new BufferedInputStream(inStream, 10240);
        File cacheFile = new File(cacheDir, "textCache");
        FileOutputStream outputStream = new FileOutputStream(cacheFile);

        byte buffer[] = new byte[1024];
        int dataSize;
        int loadedSize = 0;
        while ((dataSize = inputStream.read(buffer)) != -1) {
            loadedSize += dataSize;
            outputStream.write(buffer, 0, dataSize);
        }

        outputStream.close();
    }

    public static TextDataRow loadFileFromCacheFolder(Context context) throws IOException, ClassNotFoundException {
        File cacheDir = getCacheFolder(context);
        File cacheFile = new File(cacheDir, "textCache");
        InputStream fileInputStream = new FileInputStream(cacheFile);
        byte buffer[] = new byte[1024];
        int read = fileInputStream.read(buffer, 0, 1024);
        TextDataRow item = (TextDataRow) deserializeBytes(buffer);
        return item;
    }

    public static byte[] serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytesOut);
        oos.writeObject(obj);
        oos.flush();
        byte[] bytes = bytesOut.toByteArray();
        bytesOut.close();
        oos.close();
        return bytes;
    }

    public static Object deserializeBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bytesIn);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}
