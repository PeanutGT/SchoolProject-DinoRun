package com.dino;

import javafx.scene.media.AudioClip;

import java.io.File;

public class SoundManager {

    private static AudioClip jumpSound;
    private static AudioClip hitSound;
    private static AudioClip scoreSound;

    // 技能音效
    private static AudioClip appleSound;
    private static AudioClip milkSound;
    private static AudioClip bookSound;
    private static AudioClip barrierSound;
    private static AudioClip swordSound;

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        jumpSound = loadSound("jump.wav");
        hitSound = loadSound("hit.wav");
        scoreSound = loadSound("score.wav");

        // 載入技能音效 (存放於 assets/sounds 目錄下)
        appleSound = loadSound("sounds/Golden_apple_sound.mp3");
        milkSound = loadSound("sounds/Milk_drinking_sound.mp3");
        bookSound = loadSound("sounds/Enchant_sound.mp3");
        barrierSound = loadSound("sounds/Barrier_sound.mp3");
        swordSound = loadSound("sounds/Sword_sound.mp3");

        // 預熱，避免第一次播放延遲
        if (jumpSound != null) jumpSound.play(0);
        if (hitSound != null) hitSound.play(0);
        if (scoreSound != null) scoreSound.play(0);
        if (appleSound != null) appleSound.play(0);
        if (milkSound != null) milkSound.play(0);
        if (bookSound != null) bookSound.play(0);
        if (barrierSound != null) barrierSound.play(0);
        if (swordSound != null) swordSound.play(0);

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

    public static void playAppleSound() {
        if (appleSound != null) {
            appleSound.setVolume(GameConfig.masterVolume);
            appleSound.play();
        }
    }

    public static void playMilkSound() {
        if (milkSound != null) {
            milkSound.setVolume(GameConfig.masterVolume);
            milkSound.play();
        }
    }

    public static void playBookSound() {
        if (bookSound != null) {
            bookSound.setVolume(GameConfig.masterVolume);
            bookSound.play();
        }
    }

    public static void playBarrierSound() {
        if (barrierSound != null) {
            barrierSound.setVolume(GameConfig.masterVolume);
            barrierSound.play();
        }
    }

    public static void playSwordSound() {
        if (swordSound != null) {
            swordSound.setVolume(GameConfig.masterVolume);
            swordSound.play();
        }
    }

    
}