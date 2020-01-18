package androidCourse.technion.quickthumbs;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidCourse.technion.quickthumbs.R;

import androidCourse.technion.quickthumbs.Utils.CacheHandler;
import androidCourse.technion.quickthumbs.Utils.FriendRequestMessageService;
import androidCourse.technion.quickthumbs.database.FriendsDatabaseHandler;
import androidCourse.technion.quickthumbs.personalArea.ProfileActivity;
import androidCourse.technion.quickthumbs.personalArea.TextsActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import static androidCourse.technion.quickthumbs.personalArea.ProfileActivity.profilePicture;


public class MainPager extends AppCompatActivity {
    private static final String TAG = MainPager.class.getSimpleName();
    private FragmentPagerAdapter adapterViewPager;
    private TextView pageTitle;
    private ImageButton statsButton;
    private TextView statsTitle;
    private ImageButton textsButton;
    private TextView textsTitle;
    private TextView backToMainTitleFromTexts;
    private TextView backToMainTitleFromStatistics;
    private ImageButton backToMainButtonFromTexts;
    private ImageButton backToMainButtonFromStatistics;
    private FirebaseAuth mAuth;

    public static ViewPager vpPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);

        mAuth = FirebaseAuth.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic("user_sent_request" + getUid());
        FirebaseMessaging.getInstance().subscribeToTopic("friend_accepted_user_request" + getUid());
        FirebaseMessaging.getInstance().subscribeToTopic("user_game_invite" + getUid());

        FriendRequestMessageService friendRequestMessageService = new FriendRequestMessageService();
        friendRequestMessageService.createNotificationChannel(getApplicationContext());

        vpPager = findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        TabLayout tabLayout = findViewById(R.id.view_pager_tab);
        tabLayout.setupWithViewPager(vpPager, true);
        if(Build.VERSION.SDK_INT>=17){
            tabLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }else{
            turnDotIndicatorToInvisible(tabLayout);
        }

        initializeFields();

        setToolbarButtonListeners();

        setPagerOnChangeListener();
        vpPager.setCurrentItem(1);

        getDataFromDB();

    }

    private String getUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (currentUser != null) {
            return mAuth.getUid();
        } else if (account != null) {
            return account.getId();
        } else {
            return accessToken.getUserId();
        }
    }

    private void getDataFromDB() {
        FirebaseAuth fireBaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CacheHandler cacheHandler = new CacheHandler(getApplicationContext());
        cacheHandler.getPersonalThemesDataFromDB();
        new CacheHandler.DownloadFromStorage().execute();

        new CacheHandler.TextCacheRefill().execute();

//        new CacheHandler.FriendsUpdateFrindsList().execute();

    }

    private void turnDotIndicatorToInvisible(TabLayout tabLayout) {
        ViewGroup tabStrip = (ViewGroup) tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View tabView = tabStrip.getChildAt(i);
            if (tabView != null) {
                int paddingStart = tabView.getPaddingLeft();
                int paddingTop = tabView.getPaddingTop();
                int paddingEnd = tabView.getPaddingRight();
                int paddingBottom = tabView.getPaddingBottom();
                ViewCompat.setBackground(tabView, AppCompatResources.getDrawable(tabView.getContext(), R.color.primaryColor));
                ViewCompat.setPaddingRelative(tabView, paddingStart, paddingTop, paddingEnd, paddingBottom);
            }
        }
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
                        setToolbarMainActivityButtonsVisibility(View.INVISIBLE);
                        setToolbarStatisticsButtonsVisibility(View.VISIBLE);
                        setToolbarTextsButtonsVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        pageTitle.setText("");
                        setToolbarMainActivityButtonsVisibility(View.VISIBLE);
                        setToolbarStatisticsButtonsVisibility(View.INVISIBLE);
                        setToolbarTextsButtonsVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        pageTitle.setText("Texts");
                        setToolbarMainActivityButtonsVisibility(View.INVISIBLE);
                        setToolbarStatisticsButtonsVisibility(View.INVISIBLE);
                        setToolbarTextsButtonsVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void setToolbarMainActivityButtonsVisibility(int visibility) {
        statsTitle.setVisibility(visibility);
        textsTitle.setVisibility(visibility);
        textsButton.setVisibility(visibility);
        statsButton.setVisibility(visibility);
    }

    private void setToolbarStatisticsButtonsVisibility(int visibility){
        backToMainButtonFromStatistics.setVisibility(visibility);
        backToMainTitleFromStatistics.setVisibility(visibility);
    }

    private void setToolbarTextsButtonsVisibility(int visibility){
        backToMainButtonFromTexts.setVisibility(visibility);
        backToMainTitleFromTexts.setVisibility(visibility);
    }

    private void setToolbarButtonListeners() {
        setStatsButtonListener();
        setStatsTitleListener();
        setTextsButtonListener();
        setTextsTitleListener();
        setBackToMainFromTextsTitleListener();
        setBackToMainFromTextsImgButtonListener();
        setBackToMainFromStatisticsImgButtonListener();
        setBackToMainFromStatisticsTitleListener();
    }

    private void setBackToMainFromTextsTitleListener() {
        backToMainTitleFromTexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(1);
            }
        });
    }

    private void setBackToMainFromTextsImgButtonListener() {
        backToMainButtonFromTexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(1);
            }
        });
    }

    private void setBackToMainFromStatisticsImgButtonListener() {
        backToMainButtonFromStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(1);
            }
        });
    }

    private void setBackToMainFromStatisticsTitleListener() {
        backToMainTitleFromStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpPager.setCurrentItem(1);
            }
        });
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
        backToMainButtonFromStatistics = findViewById(R.id.backToMainFromStatisticsImgButton);
        backToMainButtonFromTexts = findViewById(R.id.backToMainFromTextsImgButton);
        backToMainTitleFromStatistics = findViewById(R.id.backToMainFromStatisticsTitle);
        backToMainTitleFromTexts = findViewById(R.id.backToMainFomTextsTitle);
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

    @Override
    public void finish(){
        super.finish();
        FirebaseAuth fireBaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CacheHandler cacheHandler = new CacheHandler(getApplicationContext());
        cacheHandler.updateUserThemesSelectionOnDB();

        new CacheHandler.TextCacheRefill().execute();

    }

    @Override
    protected void onStart() {
        super.onStart();
//        FirebaseAuth fireBaseAuth = FirebaseAuth.getInstance();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CacheHandler cacheHandler = new CacheHandler(getApplicationContext());
//        cacheHandler.getPersonalThemesDataFromDB();
//        new CacheHandler.DownloadFromStorage().execute();
    }
}

