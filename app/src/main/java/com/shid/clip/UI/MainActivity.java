package com.shid.clip.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.shid.clip.Adapters.ViewPagerAdapter;
import com.shid.clip.R;
import com.shid.clip.Utils.MyBounceInterpolator;
import com.shid.clip.Utils.SharedPref;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager viewPager;

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean isServiceOn = false;
    private SharedPref sharedPref;
    private SparkButton sparkButton;
    private ImageView zoroDay;
    private ImageView zoroNight;

    private static final String CHANNEL_ID = "1001";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPrefNight();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "value of boolean " + isServiceOn);

        setUI();
        setButtonAnimation();
        setupFragments();
        buttonClickListener();
        checkPref();


    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("service on", true)) {

        }
    }

    private void buttonClickListener() {
        sparkButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if (buttonState) {
                    sharedPref.setNightMode(true);
                    restartApp();
                } else {
                    sharedPref.setNightMode(false);
                    restartApp();
                }
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });
    }

    private void setUI() {
        tabLayout = findViewById(R.id.tablayout_id);
        appBarLayout = findViewById(R.id.app_bar_id);
        viewPager = findViewById(R.id.view_pager);
        sparkButton = findViewById(R.id.spark_button);
        zoroDay = findViewById(R.id.image_zoro);
        zoroNight = findViewById(R.id.image2);

    }

    private void setButtonAnimation() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.4, 22);
        animation.setInterpolator(interpolator);

        //   fab.startAnimation(animation);
        sparkButton.startAnimation(animation);
    }

    private void setupFragments() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        //Adding Fragments
        viewPagerAdapter.addFragment(new FragmentHome(), "Clips");
        viewPagerAdapter.addFragment(new FragmentFavorite(), "Favorite(s)");

        //Adapter setup
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void restartApp() {
        this.recreate();
    }

    private void checkPref() {
        sharedPref = new SharedPref(this);

        if (sharedPref.loadNightMode()) {
            setTheme(R.style.DarkTheme);
            zoroNight.setVisibility(View.VISIBLE);
            zoroDay.setVisibility(View.GONE);
            sparkButton.setChecked(true);
        } else {
            setTheme(R.style.DayTheme);
            zoroDay.setVisibility(View.VISIBLE);
            zoroNight.setVisibility(View.GONE);
            sparkButton.setChecked(false);
        }
    }

    private void checkPrefNight() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightMode()) {
            setTheme(R.style.DarkTheme);
           // zoroNight.setVisibility(View.VISIBLE);
            //zoroDay.setVisibility(View.GONE);
        } else {
            setTheme(R.style.DayTheme);
           // zoroDay.setVisibility(View.VISIBLE);
            //zoroNight.setVisibility(View.GONE);
        }
    }

}
