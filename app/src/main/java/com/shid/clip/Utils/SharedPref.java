package com.shid.clip.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private SharedPreferences mySharePref;

    public SharedPref(Context context) {
        mySharePref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    public void setSwitch(Boolean state) {
        SharedPreferences.Editor editor = mySharePref.edit();
        editor.putBoolean("Switch_state", state);
        editor.apply();
    }

    public Boolean loadSwitchState() {
        Boolean state = mySharePref.getBoolean("Switch_state", false);
        return state;
    }

    public void setNightMode(Boolean state) {
        SharedPreferences.Editor editor = mySharePref.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    public Boolean loadNightMode() {
        Boolean state = mySharePref.getBoolean("NightMode", false);
        return state;
    }
}
