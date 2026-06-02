package com.dino;

import java.io.*;

public class SaveManager {
    private static final String FILE_PATH = "savegame.txt";
    
    private static int coins = 0;
    private static int livesLevel = 0;
    private static int magnetLevel = 0;
    private static int multiplierLevel = 0;
    private static int extraJumpsLevel = 0;
    
    private static boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            coins = 100; // 預設進入遊戲有 100 金幣！
            save();      // 立即存檔
            loaded = true;
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    try {
                        int value = Integer.parseInt(parts[1].trim());
                        switch (key) {
                            case "coins": coins = value; break;
                            case "livesLevel": livesLevel = value; break;
                            case "magnetLevel": magnetLevel = value; break;
                            case "multiplierLevel": multiplierLevel = value; break;
                            case "extraJumpsLevel": extraJumpsLevel = value; break;
                        }
                    } catch (NumberFormatException nfe) {
                        // 忽略格式錯誤的列
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("讀取存檔失敗: " + e.getMessage());
        }
        loaded = true;
    }

    public static synchronized void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            bw.write("coins=" + coins); bw.newLine();
            bw.write("livesLevel=" + livesLevel); bw.newLine();
            bw.write("magnetLevel=" + magnetLevel); bw.newLine();
            bw.write("multiplierLevel=" + multiplierLevel); bw.newLine();
            bw.write("extraJumpsLevel=" + extraJumpsLevel); bw.newLine();
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
}
