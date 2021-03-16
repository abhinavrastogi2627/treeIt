package com.zero.shareby.activities;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.zero.shareby.R;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class MyAppIntro {
    public static final String APP_INTRO_KEY="app_intro_check_key";
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(!pref.getBoolean(APP_INTRO_KEY,true)){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }

        addSlide(AppIntro2Fragment.newInstance("Welcome","This is Share NearBy App",R.drawable.society, Color.parseColor("#E91E63")));
        addSlide(AppIntro2Fragment.newInstance("Borrow","Post request to all neighbors instantly",R.drawable.borrow1, Color.parseColor("#9C27B0")));
        addSlide(AppIntro2Fragment.newInstance("Chat within app","Personal chats for privacy",R.drawable.chat_image, Color.parseColor("#2196F3")));
        addSlide(AppIntro2Fragment.newInstance("Time to get Started","Interact to know your neighbours",R.drawable.meet_people, Color.parseColor("#2196F3")));

        //setColorTransitionsEnabled(true);
        setSlideOverAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        gotoLoginActivity();
    }



    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        gotoLoginActivity();
    }

    private void gotoLoginActivity(){
        SharedPreferences.Editor editor=pref.edit();
        editor.putBoolean(APP_INTRO_KEY,false);
        editor.apply();
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }


}
