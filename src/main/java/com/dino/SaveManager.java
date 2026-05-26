package com.dino;

import java.util.prefs.Preferences;

public class SaveManager {
    private static final Preferences PREF = Preferences.userNodeForPackage(SaveManager.class);
    private static final String KEY_MONEY = "money";

    public static int getMoney() {
        return PREF.getInt(KEY_MONEY, 0);
    }

    public static void setMoney(int v) {
        PREF.putInt(KEY_MONEY, v);
    }

    public static void addMoney(int delta) {
        int cur = getMoney();
        int next = cur + delta;
        if (next < 0) next = 0;
        setMoney(next);
    }
}
