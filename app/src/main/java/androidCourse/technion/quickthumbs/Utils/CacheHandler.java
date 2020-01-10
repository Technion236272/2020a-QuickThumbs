package androidCourse.technion.quickthumbs.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import androidCourse.technion.quickthumbs.AddTextActivity;
import androidCourse.technion.quickthumbs.game.GameActivity;
import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;
import androidCourse.technion.quickthumbs.theme.ThemeDataRow;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

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

public class CacheHandler {
    //Get Cache Folder in Android
    private static final String TAG = CacheHandler.class.getSimpleName();
    private final String[] themesNames={"Comedy","Music","Movies","Science","Games","Literature"};
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public CacheHandler(Context context, FirebaseFirestore db, FirebaseAuth mAuth){
        this.context = context;
        this.db = db;
        this.mAuth = mAuth;
    }

    public void getPersonalThemesDataFromDB() {
        final Map<String,Boolean> selectedThemes = new HashMap<>();
        db.collection("users").document(getUid()).collection("themes").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Boolean currentText = document.getBoolean("isChosen");
                                selectedThemes.put(document.getId(),currentText);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            for (int i=0 ; i<themesNames.length ; i++){
                                //this is for the layout show
                                selectedThemes.put(themesNames[i],true);
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
        for (String theme: selectedThemes.keySet()) {
            final Map<String,Object> themeObject = new HashMap<>();
            themeObject.put("isChosen",selectedThemes.get(theme));
            db.collection("users").document(getUid()).collection("themes").document(theme)
                    .set(themeObject, SetOptions.merge());
        }
    }

    public void saveThemesToSharedPreferences(Map<String, Boolean> selected) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(mAuth+"_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(selected);
        editor.putString("chosen themes", json);
        editor.apply();
    }

    public Map<String, Boolean> loadThemesFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(mAuth+"_preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("chosen themes", null);
        Type type = new TypeToken<Map<String, Boolean>>() {}.getType();
        Map<String, Boolean> loadedThemes = gson.fromJson(json, type);

        Map<String, Boolean> selected = new HashMap<>();
        for (String theme : themesNames){
            selected.put(theme,false);
        }
        if (selected != null) {
            for (String theme : loadedThemes.keySet()){
                    selected.put(theme,loadedThemes.get(theme));
            }
        }

        return selected;
    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (account != null && currentUser == null){
            return account.getId();
        }else if (currentUser!=null){
            return mAuth.getUid();
        }else{
            return accessToken.getUserId();
        }
    }


    public static File getCacheFolder(Context context) {
        File cacheDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(Environment.getExternalStorageDirectory(), "cachefolder");
            if(!cacheDir.isDirectory()) {
                cacheDir.mkdirs();
            }
        }

        if(!cacheDir.isDirectory()) {
            cacheDir = context.getCacheDir(); //get system cache folder
        }

        return cacheDir;
    }

    //Get App Data Folder in Android
    public static File getDataFolder(Context context) {
        File dataDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dataDir = new File(Environment.getExternalStorageDirectory(), "myappdata");
            if(!dataDir.isDirectory()) {
                dataDir.mkdirs();
            }
        }

        if(!dataDir.isDirectory()) {
            dataDir = context.getFilesDir();
        }

        return dataDir;
    }

    public static void writeFileIntoCacheFolder(Context context, TextDataRow textDataRow) throws IOException {
        byte[] insert =  serializeObject(textDataRow);
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
        TextDataRow item=(TextDataRow)deserializeBytes(buffer);
        return item;
    }

    public static byte[] serializeObject(Object obj) throws IOException
    {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytesOut);
        oos.writeObject(obj);
        oos.flush();
        byte[] bytes = bytesOut.toByteArray();
        bytesOut.close();
        oos.close();
        return bytes;
    }

    public static Object deserializeBytes(byte[] bytes) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bytesIn);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}
