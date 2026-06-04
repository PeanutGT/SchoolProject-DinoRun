package com.dino;

import java.io.*;
import java.util.*;

public class LeaderboardManager {
    private static final String FILE_NAME = "leaderboard.txt";
    private static final String FILE_NAME_COOP = "leaderboard_coop.txt";
    private static final int MAX_SCORES = 50;

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public int score;
        public String characterType;
        public String characterType2; // null for single player

        public ScoreEntry(String name, int score, String characterType, String characterType2) {
            this.name = name;
            this.score = score;
            this.characterType = characterType;
            this.characterType2 = characterType2;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); // 降冪排序
        }
    }

    private static File getFile(boolean isCoop) {
        return new File(GameConfig.getBaseDirectory(), isCoop ? FILE_NAME_COOP : FILE_NAME);
    }

    public static List<ScoreEntry> loadTopScores(boolean isCoop) {
        List<ScoreEntry> scores = new ArrayList<>();
        File file = getFile(isCoop);
        if (!file.exists()) {
            return scores;
        }

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
                        // 相容舊版資料
                        scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1]), "dino", null));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }
        return scores;
    }

    public static boolean isHighScore(int score, boolean isCoop) {
        if (score <= 0) return false;
        List<ScoreEntry> scores = loadTopScores(isCoop);
        if (scores.size() < MAX_SCORES) {
            return true;
        }
        return score > scores.get(scores.size() - 1).score;
    }

    public static void addScore(String name, int score, String characterType, String characterType2, boolean isCoop) {
        List<ScoreEntry> scores = loadTopScores(isCoop);
        scores.add(new ScoreEntry(name, score, characterType, characterType2));
        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }

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
