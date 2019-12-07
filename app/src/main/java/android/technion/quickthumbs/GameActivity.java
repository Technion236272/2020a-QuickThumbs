package android.technion.quickthumbs;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //remove the status bar but also my bars
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final EditText editText = (EditText) findViewById(R.id.currentWord);
        editText.requestFocus();
        // showing the soft keyboard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setShowSoftInputOnFocus(true);
        }
//        editText.setOnEditorActionListener(new EditText.OnEditorActionListener(){
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                String value = editText.getText().toString();
//                //TODO .. write your respective logic to add data to your textView
//
//                editText.setText(""); // clear the text in your editText
//                return true;
//            }
//        });
        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.GameToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

}
