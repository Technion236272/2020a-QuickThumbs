package android.technion.quickthumbs;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class AddTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);

        final TextView titleHeadline = (TextView) findViewById(R.id.titleHeadline);
        titleHeadline.setTextColor(Color.GRAY);
        final TextView titleValue = (TextView) findViewById(R.id.titleValue);
        titleValue.setTextColor(Color.WHITE);
        titleValue.requestFocus();
        // showing the soft keyboard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            titleValue.setShowSoftInputOnFocus(true);
        }

        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar)findViewById(R.id.AddTextToolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
