package com.dino;

import javafx.scene.media.AudioClip;

public class SoundManager {

    private static AudioClip jumpSound;
    private static AudioClip hitSound;
    private static AudioClip scoreSound;

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        jumpSound = loadSound("jump.wav");
        hitSound = loadSound("hit.wav");
        scoreSound = loadSound("score.wav");

        // 預熱，避免第一次播放延遲
        jumpSound.play(0);
        hitSound.play(0);
        scoreSound.play(0);

        initialized = true;
    }

    private static AudioClip loadSound(String fileName) {
        try {
            String path = "/com/dino/assets/" + fileName;
            String url = SoundManager.class.getResource(path).toExternalForm();
            return new AudioClip(url);
        } catch (Exception e) {
            System.err.println("找不到音效資源: " + fileName);
            return null;
        }
    }

    public static void playJump() {
        if (jumpSound != null) {
            jumpSound.setVolume(GameConfig.masterVolume); // 設定音量
            jumpSound.play();
        }
    }

    public static void playHit() {
        if (hitSound != null) {
            hitSound.setVolume(GameConfig.masterVolume);
            hitSound.play();
        }
    }

    public static void playScore() {
        if (scoreSound != null) {
            scoreSound.setVolume(GameConfig.masterVolume);
            scoreSound.play();
        }
    }

    
}