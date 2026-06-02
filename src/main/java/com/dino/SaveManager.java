package com.dino;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private static final String FILE_PATH = "savegame.txt";
    
    private static int coins = 0;
    private static int livesLevel = 0;
    private static int magnetLevel = 0;
    private static int multiplierLevel = 0;
    private static int extraJumpsLevel = 0;
    
    // 新增的三個 100 元進階功能
    private static int resurrectionCount = 0;
    private static int regenLevel = 0;
    private static int moreCoinsLevel = 0;
    private static int questionBoxLevel = 0;

    // 角色解鎖狀態 (dino 預設解鎖，其餘初始為 false)
    private static boolean marioUnlocked = false;
    private static boolean luigiUnlocked = false;
    private static boolean kirbyUnlocked = false;
    private static boolean lucarioUnlocked = false;
    private static boolean sonicUnlocked = false;
    private static boolean steveUnlocked = false;
    
    private static boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;
        
        // 每次打開遊戲都重設為全新的初始存檔！
        coins = 0;
        livesLevel = 0;
        magnetLevel = 0;
        multiplierLevel = 0;
        extraJumpsLevel = 0;
        resurrectionCount = 0;
        regenLevel = 0;
        moreCoinsLevel = 0;
        questionBoxLevel = 0;

        // 初始時所有進階角色皆鎖定
        marioUnlocked = false;
        luigiUnlocked = false;
        kirbyUnlocked = false;
        lucarioUnlocked = false;
        sonicUnlocked = false;
        steveUnlocked = false;
        
        save(); // 立即寫入檔案，覆蓋舊有存檔
        loaded = true;
    }

    public static synchronized void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            bw.write("coins=" + coins); bw.newLine();
            bw.write("livesLevel=" + livesLevel); bw.newLine();
            bw.write("magnetLevel=" + magnetLevel); bw.newLine();
            bw.write("multiplierLevel=" + multiplierLevel); bw.newLine();
            bw.write("extraJumpsLevel=" + extraJumpsLevel); bw.newLine();
            bw.write("resurrectionCount=" + resurrectionCount); bw.newLine();
            bw.write("regenLevel=" + regenLevel); bw.newLine();
            bw.write("moreCoinsLevel=" + moreCoinsLevel); bw.newLine();
            bw.write("questionBoxLevel=" + questionBoxLevel); bw.newLine();
            bw.write("marioUnlocked=" + marioUnlocked); bw.newLine();
            bw.write("luigiUnlocked=" + luigiUnlocked); bw.newLine();
            bw.write("kirbyUnlocked=" + kirbyUnlocked); bw.newLine();
            bw.write("lucarioUnlocked=" + lucarioUnlocked); bw.newLine();
            bw.write("sonicUnlocked=" + sonicUnlocked); bw.newLine();
            bw.write("steveUnlocked=" + steveUnlocked); bw.newLine();
        } catch (Exception e) {
            System.err.println("寫入存檔失敗: " + e.getMessage());
        }
    }

    public static int getCoins() { load(); return coins; }
    public static void addCoins(int amount) { load(); coins += amount; save(); }
    public static boolean spendCoins(int amount) {
        load();
        if (coins >= amount) {
            coins -= amount;
            save();
            return true;
        }
        return false;
    }

    public static int getLivesLevel() { load(); return livesLevel; }
    public static void setLivesLevel(int lvl) { load(); livesLevel = lvl; save(); }

    public static int getMagnetLevel() { load(); return magnetLevel; }
    public static void setMagnetLevel(int lvl) { load(); magnetLevel = lvl; save(); }

    public static int getMultiplierLevel() { load(); return multiplierLevel; }
    public static void setMultiplierLevel(int lvl) { load(); multiplierLevel = lvl; save(); }

    public static int getExtraJumpsLevel() { load(); return extraJumpsLevel; }
    public static void setExtraJumpsLevel(int lvl) { load(); extraJumpsLevel = lvl; save(); }

    // 遊戲內實際的加成獲取方法
    public static int getLivesBonus() {
        return getLivesLevel(); // 每級 +1 生命
    }

    public static double getMagnetRadius() {
        switch (getMagnetLevel()) {
            case 1: return 100.0;
            case 2: return 200.0;
            case 3: return 350.0;
            default: return 0.0;
        }
    }

    public static int getCoinMultiplier() {
        switch (getMultiplierLevel()) {
            case 1: return 2;
            case 2: return 3;
            case 3: return 5;
            default: return 1;
        }
    }

    public static int getExtraJumps() {
        return getExtraJumpsLevel(); // 每級 +1 跳躍次數
    }

    // 1. 一次性復活功能 (100 元)
    public static int getResurrectionCount() { load(); return resurrectionCount; }
    public static void setResurrectionCount(int val) { load(); resurrectionCount = val; save(); }
    public static boolean hasResurrection() { return getResurrectionCount() > 0; }
    public static void useResurrection() { setResurrectionCount(0); }

    // 2. 緩慢自動回血 (100 元)
    public static int getRegenLevel() { load(); return regenLevel; }
    public static void setRegenLevel(int val) { load(); regenLevel = val; save(); }
    public static boolean hasRegen() { return getRegenLevel() > 0; }

    // 3. 提高金幣頻率 (100 元)
    public static int getMoreCoinsLevel() { load(); return moreCoinsLevel; }
    public static void setMoreCoinsLevel(int val) { load(); moreCoinsLevel = val; save(); }
    public static boolean hasMoreCoins() { return getMoreCoinsLevel() > 0; }

    // 4. 問號箱強化
    public static int getQuestionBoxLevel() { load(); return questionBoxLevel; }
    public static void setQuestionBoxLevel(int val) { load(); questionBoxLevel = val; save(); }

    // 角色解鎖系統
    public static boolean isCharacterUnlocked(String charId) {
        load();
        switch (charId) {
            case "dino":    return true;  // Dino 預設永遠解鎖
            case "mario":   return marioUnlocked;
            case "luigi":   return luigiUnlocked;
            case "kirby":   return kirbyUnlocked;
            case "lucario": return lucarioUnlocked;
            case "sonic":   return sonicUnlocked;
            case "steve":   return steveUnlocked;
            default:        return false;
        }
    }

    public static void unlockCharacter(String charId) {
        load();
        switch (charId) {
            case "mario":   marioUnlocked = true; break;
            case "luigi":   luigiUnlocked = true; break;
            case "kirby":   kirbyUnlocked = true; break;
            case "lucario": lucarioUnlocked = true; break;
            case "sonic":   sonicUnlocked = true; break;
            case "steve":   steveUnlocked = true; break;
        }
        save();
    }

    /** 回傳除了 dino 之外已解鎖的角色數量（0 ~ 6）*/
    public static int getUnlockedCharactersCount() {
        load();
        int count = 0;
        if (marioUnlocked)   count++;
        if (luigiUnlocked)   count++;
        if (kirbyUnlocked)   count++;
        if (lucarioUnlocked) count++;
        if (sonicUnlocked)   count++;
        if (steveUnlocked)   count++;
        return count;
    }

    /**
     * 隨機解鎖一位尚未解鎖的角色，並返回其 ID。
     * 若所有角色皆已解鎖則返回 null。
     */
    public static String unlockRandomCharacter() {
        load();
        List<String> locked = new ArrayList<>();
        if (!marioUnlocked)   locked.add("mario");
        if (!luigiUnlocked)   locked.add("luigi");
        if (!kirbyUnlocked)   locked.add("kirby");
        if (!lucarioUnlocked) locked.add("lucario");
        if (!sonicUnlocked)   locked.add("sonic");
        if (!steveUnlocked)   locked.add("steve");
        if (locked.isEmpty()) return null;

        String chosen = locked.get((int)(Math.random() * locked.size()));
        unlockCharacter(chosen);
        return chosen;
    }
}
