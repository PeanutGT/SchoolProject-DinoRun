package com.dino;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 存檔管理員類別。
 * 負責金幣數量、數值升級項目（生命加成、磁力半徑、金幣加倍、額外跳躍、自動回血、幸運方塊）
 * 以及角色解鎖狀態的本機磁碟讀寫（檔案名稱為 savegame.txt），採用關鍵字等於數值的簡易 key-value 結構。
 */
public class SaveManager {
    // 存檔檔案名稱
    private static final String FILE_NAME = "savegame.txt";
    
    /**
     * 動態定位可攜式路徑下的存檔檔案。
     */
    private static File getSaveFile() {
        return new File(GameConfig.getBaseDirectory(), FILE_NAME);
    }

    // 金幣與各基礎數值等級
    private static int coins = 0;
    private static int livesLevel = 0;
    private static int magnetLevel = 0;
    private static int multiplierLevel = 0;
    private static int extraJumpsLevel = 0;
    
    // 進階技能升級
    private static int resurrectionCount = 0; // 一次性復活次數
    private static int regenLevel = 0;         // 自動回血等級
    private static int moreCoinsLevel = 0;     // 提高金幣頻率等級
    private static int questionBoxLevel = 0;   // 幸運問號箱等級

    // 角色解鎖狀態 (dino 預設解鎖，其餘初始為未解鎖 false)
    private static boolean marioUnlocked = false;
    private static boolean luigiUnlocked = false;
    private static boolean kirbyUnlocked = false;
    private static boolean lucarioUnlocked = false;
    private static boolean sonicUnlocked = false;
    private static boolean steveUnlocked = false;
    
    private static boolean loaded = false; // 是否已載入完畢，避免單局內重複讀碟

    /**
     * 同步載入本機存檔。若存檔不存在則初始化並儲存預設檔。
     */
    public static synchronized void load() {
        if (loaded) return;
        
        File file = getSaveFile();
        if (!file.exists()) {
            // 首次運行初始化數值並儲存
            coins = 0;
            livesLevel = 0;
            magnetLevel = 0;
            multiplierLevel = 0;
            extraJumpsLevel = 0;
            resurrectionCount = 0;
            regenLevel = 0;
            moreCoinsLevel = 0;
            questionBoxLevel = 0;

            marioUnlocked = false;
            luigiUnlocked = false;
            kirbyUnlocked = false;
            lucarioUnlocked = false;
            sonicUnlocked = false;
            steveUnlocked = false;
            save();
            loaded = true;
            return;
        }

        // 以 BufferedReader 讀取文字存檔並以 "=" 分割鍵值
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length < 2) continue;
                String key = parts[0].trim();
                String val = parts[1].trim();

                switch (key) {
                    case "coins": coins = Integer.parseInt(val); break;
                    case "livesLevel": livesLevel = Integer.parseInt(val); break;
                    case "magnetLevel": magnetLevel = Integer.parseInt(val); break;
                    case "multiplierLevel": multiplierLevel = Integer.parseInt(val); break;
                    case "extraJumpsLevel": extraJumpsLevel = Integer.parseInt(val); break;
                    case "resurrectionCount": resurrectionCount = Integer.parseInt(val); break;
                    case "regenLevel": regenLevel = Integer.parseInt(val); break;
                    case "moreCoinsLevel": moreCoinsLevel = Integer.parseInt(val); break;
                    case "questionBoxLevel": questionBoxLevel = Integer.parseInt(val); break;
                    case "marioUnlocked": marioUnlocked = Boolean.parseBoolean(val); break;
                    case "luigiUnlocked": luigiUnlocked = Boolean.parseBoolean(val); break;
                    case "kirbyUnlocked": kirbyUnlocked = Boolean.parseBoolean(val); break;
                    case "lucarioUnlocked": lucarioUnlocked = Boolean.parseBoolean(val); break;
                    case "sonicUnlocked": sonicUnlocked = Boolean.parseBoolean(val); break;
                    case "steveUnlocked": steveUnlocked = Boolean.parseBoolean(val); break;
                }
            }
        } catch (Exception e) {
            System.err.println("讀取存檔失敗: " + e.getMessage());
        }

        loaded = true;
    }

    /**
     * 同步儲存所有資料至本機。
     */
    public static synchronized void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getSaveFile()))) {
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

    // 金幣基本增刪檢測
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

    // 各大升級項等級讀寫
    public static int getLivesLevel() { load(); return livesLevel; }
    public static void setLivesLevel(int lvl) { load(); livesLevel = lvl; save(); }

    public static int getMagnetLevel() { load(); return magnetLevel; }
    public static void setMagnetLevel(int lvl) { load(); magnetLevel = lvl; save(); }

    public static int getMultiplierLevel() { load(); return multiplierLevel; }
    public static void setMultiplierLevel(int lvl) { load(); multiplierLevel = lvl; save(); }

    public static int getExtraJumpsLevel() { load(); return extraJumpsLevel; }
    public static void setExtraJumpsLevel(int lvl) { load(); extraJumpsLevel = lvl; save(); }

    /** 每級 +1 滴血 */
    public static int getLivesBonus() {
        return getLivesLevel(); 
    }

    /** 依磁鐵等級轉換為實際吸引半徑 */
    public static double getMagnetRadius() {
        switch (getMagnetLevel()) {
            case 1: return 100.0;
            case 2: return 200.0;
            case 3: return 350.0;
            default: return 0.0;
        }
    }

    /** 依金幣加倍等級轉換為實際收穫硬幣乘數 */
    public static int getCoinMultiplier() {
        switch (getMultiplierLevel()) {
            case 1: return 2;
            case 2: return 3;
            case 3: return 5;
            default: return 1;
        }
    }

    /** 依升級等級轉換為實際額外跳躍次數 */
    public static int getExtraJumps() {
        return getExtraJumpsLevel();
    }

    // 1. 一次性復活次數 (100 元)
    public static int getResurrectionCount() { load(); return resurrectionCount; }
    public static void setResurrectionCount(int val) { load(); resurrectionCount = val; save(); }
    public static boolean hasResurrection() { return getResurrectionCount() > 0; }
    public static void useResurrection() { setResurrectionCount(0); }

    // 2. 緩慢自動回血等級 (100 元)
    public static int getRegenLevel() { load(); return regenLevel; }
    public static void setRegenLevel(int val) { load(); regenLevel = val; save(); }
    public static boolean hasRegen() { return getRegenLevel() > 0; }

    // 3. 提高金幣頻率等級 (100 元)
    public static int getMoreCoinsLevel() { load(); return moreCoinsLevel; }
    public static void setMoreCoinsLevel(int val) { load(); moreCoinsLevel = val; save(); }
    public static boolean hasMoreCoins() { return getMoreCoinsLevel() > 0; }

    // 4. 幸運問號箱等級
    public static int getQuestionBoxLevel() { load(); return questionBoxLevel; }
    public static void setQuestionBoxLevel(int val) { load(); questionBoxLevel = val; save(); }

    /**
     * 檢查指定角色代號是否解鎖。
     * @param charId 角色代號
     * @return 是否已解鎖
     */
    public static boolean isCharacterUnlocked(String charId) {
        load();
        switch (charId) {
            case "dino":    return true;  // Dino 預設永遠解鎖，不可被鎖定
            case "mario":   return marioUnlocked;
            case "luigi":   return luigiUnlocked;
            case "kirby":   return kirbyUnlocked;
            case "lucario": return lucarioUnlocked;
            case "sonic":   return sonicUnlocked;
            case "steve":   return steveUnlocked;
            default:        return false;
        }
    }

    /**
     * 解鎖特定角色。
     */
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

    /** 回傳除了 dino 之外已解鎖的角色總數（0 ~ 6）*/
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
     * 隨機抽取解鎖一位尚未擁有的角色，並返回其名稱；若全數解鎖，則回傳 null。
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
