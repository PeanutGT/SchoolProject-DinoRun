package com.dino;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Random;

public class SoundManager {

    private static AudioClip jumpSound;
    private static AudioClip hitSound;
    private static AudioClip scoreSound;
    private static MediaPlayer menuBgm;

    // 技能音效
    private static AudioClip appleSound;
    private static AudioClip milkSound;
    private static AudioClip bookSound;
    private static AudioClip barrierSound;
    private static AudioClip swordSound;

    private static final String[] gameBgms = {
        "sounds/bgm1.mp3",
        "sounds/bgm2.mp3",
        "sounds/bgm3.mp3",
        "sounds/bgm4.mp3",
        "sounds/bgm5.mp3",
        "sounds/bgm6.mp3"
    };
    private static MediaPlayer currentGameBgm;
    private static final Random random = new Random();

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        jumpSound = loadSound("jump.wav");
        hitSound = loadSound("hit.wav");
        scoreSound = loadSound("score.wav");

        // 載入技能音效 (存放於 assets/sounds 目錄下)
        menuBgm = loadMediaPlayer("sounds/menu1.mp3");
        appleSound = loadSound("sounds/Golden_apple_sound.mp3");
        milkSound = loadSound("sounds/Milk_drinking_sound.mp3");
        bookSound = loadSound("sounds/Enchant_sound.mp3");
        barrierSound = loadSound("sounds/Barrier_sound.mp3");
        swordSound = loadSound("sounds/Sword_sound.mp3");

        // 移除預熱邏輯，因為 JavaFX 播放 MP3 遇到 play(0) 依然會原音量播放，導致主選單發出怪聲

        initialized = true;
    }

    private static AudioClip loadSound(String fileName) {
        try {
            String path = "/com/dino/assets/" + fileName;
            java.net.URL resourceUrl = SoundManager.class.getResource(path);
            if (resourceUrl == null) {
                System.err.println("找不到音效資源: " + fileName);
                return null;
            }
            String url = resourceUrl.toExternalForm();
            return new AudioClip(url);
        } catch (Throwable t) {
            System.err.println("無法載入音效資源或底層多媒體庫缺失: " + fileName + " (" + t.getMessage() + ")");
            return null;
        }
    }

    private static MediaPlayer loadMediaPlayer(String fileName) {
        try {
            String path = "/com/dino/assets/" + fileName;
            java.net.URL resourceUrl = SoundManager.class.getResource(path);
            if (resourceUrl == null) {
                System.err.println("找不到音樂資源: " + fileName);
                return null;
            }
            String url = resourceUrl.toExternalForm();
            Media media = new Media(url);
            return new MediaPlayer(media);
        } catch (Throwable t) {
            System.err.println("無法載入音樂資源或底層多媒體庫缺失: " + fileName + " (" + t.getMessage() + ")");
            return null;
        }
    }

    public static void playJump() {
        if (jumpSound != null) {
            jumpSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playHit() {
        if (hitSound != null) {
            hitSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playScore() {
        if (scoreSound != null) {
            scoreSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playAppleSound() {
        if (appleSound != null) {
            appleSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playMilkSound() {
        if (milkSound != null) {
            milkSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playBookSound() {
        if (bookSound != null) {
            bookSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playBarrierSound() {
        if (barrierSound != null) {
            barrierSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playSwordSound() {
        if (swordSound != null) {
            swordSound.play(GameConfig.masterVolume * GameConfig.sfxVolume);
        }
    }

    public static void playMenuBgm() {
        if (menuBgm != null && menuBgm.getStatus() != MediaPlayer.Status.PLAYING) {
            menuBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
            menuBgm.setCycleCount(MediaPlayer.INDEFINITE);
            menuBgm.play();
        }
    }

    public static void stopMenuBgm() {
        if (menuBgm != null && menuBgm.getStatus() == MediaPlayer.Status.PLAYING) {
            menuBgm.stop();
        }
    }

    public static void playGameBgm() {
        stopMenuBgm();
        stopGameBgm(); // 停止目前播放的遊戲 BGM，準備播放下一首

        String nextBgm = gameBgms[random.nextInt(gameBgms.length)];
        try {
            String path = "/com/dino/assets/" + nextBgm;
            String url = SoundManager.class.getResource(path).toExternalForm();
            Media media = new Media(url);
            currentGameBgm = new MediaPlayer(media);
            currentGameBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
            
            // 播放完畢後自動隨機切換下一首，達到無縫輪播
            currentGameBgm.setOnEndOfMedia(() -> {
                playGameBgm();
            });
            
            currentGameBgm.play();
        } catch (Throwable t) {
            System.err.println("無法播放遊戲背景音樂或多媒體庫缺失: " + nextBgm + " (" + t.getMessage() + ")");
        }
    }

    public static void stopGameBgm() {
        if (currentGameBgm != null) {
            currentGameBgm.stop();
            currentGameBgm.dispose(); // 釋放資源
            currentGameBgm = null;
        }
    }

    public static void pauseGameBgm() {
        if (currentGameBgm != null) {
            currentGameBgm.pause();
        }
    }

    public static void resumeGameBgm() {
        if (currentGameBgm != null) {
            currentGameBgm.play();
        }
    }

    public static void setMasterVolume(double v) {
        GameConfig.masterVolume = v;
        updateMusicVolume();
    }

    public static void setMusicVolume(double v) {
        GameConfig.musicVolume = v;
        updateMusicVolume();
    }

    public static void setSfxVolume(double v) {
        GameConfig.sfxVolume = v;
    }

    public static void updateMusicVolume() {
        if (menuBgm != null) {
            menuBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
        }
        if (currentGameBgm != null) {
            currentGameBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
        }
    }
}