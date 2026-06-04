package com.dino;

import java.io.*;
import java.util.*;

/**
 * 排行榜資料管理員類別。
 * 負責單人模式排行榜 (leaderboard.txt) 與雙人合作模式排行榜 (leaderboard_coop.txt)
 * 的本機磁碟 CSV 格式讀寫。
 * 最大保存紀錄為 50 筆，並提供依分數遞減的自訂排序比較器。
 */
public class LeaderboardManager {
    // 獨立的單人與雙人存檔檔案名稱
    private static final String FILE_NAME = "leaderboard.txt";
    private static final String FILE_NAME_COOP = "leaderboard_coop.txt";
    
    // 排行榜紀錄的最大上限筆數
    private static final int MAX_SCORES = 50;

    /**
     * 排行榜單條分數紀錄的資料載體內部類別。
     * 實作 Comparable 介面，以便呼叫 Collections.sort 進行降冪排序。
     */
    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;           // 玩家大名
        public int score;             // 歷史得分
        public String characterType;  // 玩家一所用角色名稱
        public String characterType2; // 玩家二所用角色名稱（單人榜為 null）

        public ScoreEntry(String name, int score, String characterType, String characterType2) {
            this.name = name;
            this.score = score;
            this.characterType = characterType;
            this.characterType2 = characterType2;
        }

        /**
         * 比較器覆寫：依照分數進行由高至低的降冪排序。
         */
        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); 
        }
    }

    /**
     * 獲取對應榜單的可攜式 File 物件。
     */
    private static File getFile(boolean isCoop) {
        return new File(GameConfig.getBaseDirectory(), isCoop ? FILE_NAME_COOP : FILE_NAME);
    }

    /**
     * 讀取並加載排行榜的前 50 筆高分名單。
     * @param isCoop 是否為雙人合作榜單
     * @return 排序完畢的分數紀錄陣列清單
     */
    public static List<ScoreEntry> loadTopScores(boolean isCoop) {
        List<ScoreEntry> scores = new ArrayList<>();
        File file = getFile(isCoop);
        if (!file.exists()) {
            return scores;
        }

        // 以逗號分割讀取 CSV
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (isCoop) {
                    if (parts.length >= 4) {
                        scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3]));
                    }
                } else {
                    if (parts.length >= 3) {
                        scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1]), parts[2], null));
                    } else if (parts.length == 2) {
                        // 相容歷史舊版本存檔（舊版僅有名稱與分數）
                        scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1]), "dino", null));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 進行排序
        Collections.sort(scores);
        // 超過 50 筆時進行截斷，維持前 50 名
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }
        return scores;
    }

    /**
     * 判定當前單局分數是否足以擠進前 50 名排行榜（如果榜單未滿 50 筆則必上榜）。
     * @param score 當前得分
     * @param isCoop 是否為合作模式
     * @return 是否破紀錄/擠進排行榜
     */
    public static boolean isHighScore(int score, boolean isCoop) {
        if (score <= 0) return false;
        List<ScoreEntry> scores = loadTopScores(isCoop);
        if (scores.size() < MAX_SCORES) {
            return true;
        }
        // 與第 50 名（即最後一名）進行比對
        return score > scores.get(scores.size() - 1).score;
    }

    /**
     * 加入一筆新分數紀錄。重新排序並存回本機硬碟檔案。
     */
    public static void addScore(String name, int score, String characterType, String characterType2, boolean isCoop) {
        List<ScoreEntry> scores = loadTopScores(isCoop);
        scores.add(new ScoreEntry(name, score, characterType, characterType2));
        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }

        // 以 FileWriter 覆寫 CSV 資料庫
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getFile(isCoop)))) {
            for (ScoreEntry entry : scores) {
                if (isCoop) {
                    bw.write(entry.name + "," + entry.score + "," + entry.characterType + "," + entry.characterType2);
                } else {
                    bw.write(entry.name + "," + entry.score + "," + entry.characterType);
                }
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
