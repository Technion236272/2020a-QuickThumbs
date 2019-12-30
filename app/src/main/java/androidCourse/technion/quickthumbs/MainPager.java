package androidCourse.technion.quickthumbs;

import android.os.Bundle;
import androidCourse.technion.quickthumbs.R;

import androidCourse.technion.quickthumbs.personalArea.ProfileActivity;
import androidCourse.technion.quickthumbs.personalArea.TextsActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class MainPager extends AppCompatActivity {
    FragmentPagerAdapter adapterViewPager;
    TextView pageTitle;
    ImageButton statsButton;
    TextView statsTitle;
    ImageButton textsButton;
    TextView textsTitle;
    ViewPager vpPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);
        vpPager = findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        TabLayout tabLayout = findViewById(R.id.view_pager_tab);
        tabLayout.setupWithViewPager(vpPager, true);

        initializeFields();

        setToolbarButtonListeners();

        setPagerOnChangeListener();
        vpPager.setCurrentItem(1);
    }

    private void setPagerOnChangeListener() {
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){}

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        pageTitle.setText("Statistics");
                        makeAllToolbarButtonsInvisible();
                        break;
                    case 1:
                        pageTitle.setText("");
                        makeAllToolbarButtonsVisible();
                        break;
                    case 2:
                        pageTitle.setText("Texts");
                        makeAllToolbarButtonsInvisible();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void makeAllToolbarButtonsVisible() {
        statsTitle.setVisibility(View.VISIBLE);
        textsTitle.setVisibility(View.VISIBLE);
        textsButton.setVisibility(View.VISIBLE);
        statsButton.setVisibility(View.VISIBLE);
    }

    private void makeAllToolbarButtonsInvisible() {
        statsTitle.setVisibility(View.INVISIBLE);
        textsTitle.setVisibility(View.INVISIBLE);
        textsButton.setVisibility(View.INVISIBLE);
        statsButton.setVisibility(View.INVISIBLE);
    }

    private void setToolbarButtonListeners() {
        setStatsButtonListener();
        setStatsTitleListener();
        setTextsButtonListener();
        setTextsTitleListener();
    }

    private void setTextsTitleListener() {
        textsTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(2);
            }
        });
    }

    private void setTextsButtonListener() {
        textsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(2);
            }
        });
    }

    private void setStatsTitleListener() {
        statsTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(0);
            }
        });
    }

    private void setStatsButtonListener() {
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(0);
            }
        });
    }

    private void initializeFields() {
        pageTitle = findViewById(R.id.PageTile);
        statsButton = findViewById(R.id.statsImageButton);
        statsTitle = findViewById(R.id.statsTitle);
        textsButton = findViewById(R.id.textsImageButton);
        textsTitle = findViewById(R.id.textsTitle);
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show Stats Fragment
                    return new ProfileActivity();
                case 1: // Fragment # 1 - This will show Main User Fragment
                    return new MainUserActivity();
              case 2: // Fragment # 2 - This will show Texts Fragment
                    return new TextsActivity();
                default:
                    return null;
            }
        }
    }
}
