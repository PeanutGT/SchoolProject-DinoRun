package com.dino;

import java.io.*;
import java.util.*;

public class LeaderboardManager {
    private static final String FILE_PATH = "leaderboard.txt";
    private static final int MAX_SCORES = 50;

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public String name;
        public int score;
        public String characterType;

        public ScoreEntry(String name, int score, String characterType) {
            this.name = name;
            this.score = score;
            this.characterType = characterType;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); // 降冪排序
        }
    }

    public static List<ScoreEntry> loadTopScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return scores;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1]), parts[2]));
                } else if (parts.length == 2) {
                    // 相容舊紀錄
                    scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1]), "dino"));
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

    public static boolean isHighScore(int score) {
        if (score <= 0) return false;
        List<ScoreEntry> scores = loadTopScores();
        if (scores.size() < MAX_SCORES) {
            return true;
        }
        return score > scores.get(scores.size() - 1).score;
    }

    public static void addScore(String name, int score, String characterType) {
        List<ScoreEntry> scores = loadTopScores();
        scores.add(new ScoreEntry(name, score, characterType));
        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (ScoreEntry entry : scores) {
                bw.write(entry.name + "," + entry.score + "," + entry.characterType);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
