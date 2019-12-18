package android.technion.quickthumbs;

import android.technion.quickthumbs.game.GameActivity;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextPoll {
    private static final String TAG = AddTextActivity.class.getName();

    public static String fetchRandomText(final TextView gameTextView, final GameActivity objectToInvokeOn) {
        final String selectedText = "default text for usage";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("texts");

        collection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> texts = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String currentText = document.getString("text");
                                texts.add(currentText);
                            }

                            int textsListSize = texts.size();
                            String randomText = texts.get(new Random().nextInt(textsListSize));
                            Log.d(TAG, "gottenText is: " + randomText);

                            gameTextView.setText(randomText);

                            try {
                                Class<?> c = GameActivity.class;
                                Method method = c.getDeclaredMethod("gameCreationSequence", (Class<?>[]) null);
                                method.invoke(objectToInvokeOn, (Object[]) null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return selectedText;
    }

}
