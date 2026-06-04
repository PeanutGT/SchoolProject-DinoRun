package com.dino;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Random;

/**
 * 音效與音樂管理員類別。
 * 使用 JavaFX AudioClip 來低延遲播放短音效（跳躍、受傷、得分、各類技能聲），
 * 使用 MediaPlayer 來播放與控制背景音樂 (BGM)（包括主選單音樂與多首關卡音樂的隨機輪播）。
 * 支援主音量、背景音樂音量、音效音量的動態調整。
 */
public class SoundManager {

    // 短音效 AudioClip (適合快速重疊播放)
    private static AudioClip jumpSound;
    private static AudioClip hitSound;
    private static AudioClip scoreSound;
    private static MediaPlayer menuBgm;

    // 技能專用音效
    private static AudioClip appleSound;
    private static AudioClip milkSound;
    private static AudioClip bookSound;
    private static AudioClip barrierSound;
    private static AudioClip swordSound;

    // 關卡隨機輪播的背景音樂檔名清單
    private static final String[] gameBgms = {
        "sounds/bgm1.mp3",
        "sounds/bgm2.mp3",
        "sounds/bgm3.mp3",
        "sounds/bgm4.mp3",
        "sounds/bgm5.mp3",
        "sounds/bgm6.mp3"
    };
    
    private static MediaPlayer currentGameBgm; // 目前正在播放的關卡音樂播發器
    private static final Random random = new Random();

    private static boolean initialized = false; // 是否完成載入標記

    /**
     * 初始化：預先載入所有音效檔與主選單背景音樂。
     */
    public static void init() {
        if (initialized) {
            return;
        }

        jumpSound = loadSound("jump.wav");
        hitSound = loadSound("hit.wav");
        scoreSound = loadSound("score.wav");

        // 載入技能與主選單音樂（位於 assets 資料夾下）
        menuBgm = loadMediaPlayer("sounds/menu1.mp3");
        appleSound = loadSound("sounds/Golden_apple_sound.mp3");
        milkSound = loadSound("sounds/Milk_drinking_sound.mp3");
        bookSound = loadSound("sounds/Enchant_sound.mp3");
        barrierSound = loadSound("sounds/Barrier_sound.mp3");
        swordSound = loadSound("sounds/Sword_sound.mp3");

        initialized = true;
    }

    /**
     * 讀取短音效 AudioClip。
     * @param fileName 檔名
     */
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

    /**
     * 讀取背景音樂 MediaPlayer。
     * @param fileName 檔名
     */
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

    // ==========================================
    // 音效播放 API (音量 = 主音量 * 音效分音量)
    // ==========================================
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

    /**
     * 播放選單 BGM (循環播放)。
     */
    public static void playMenuBgm() {
        if (menuBgm != null && menuBgm.getStatus() != MediaPlayer.Status.PLAYING) {
            menuBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
            menuBgm.setCycleCount(MediaPlayer.INDEFINITE);
            menuBgm.play();
        }
    }

    /**
     * 停止選單 BGM。
     */
    public static void stopMenuBgm() {
        if (menuBgm != null && menuBgm.getStatus() == MediaPlayer.Status.PLAYING) {
            menuBgm.stop();
        }
    }

    /**
     * 啟動遊戲背景音樂輪播。
     * 自動隨機挑選一首 BGM 開始播放，當播放完畢後自動觸發 EndOfMedia 事件播放下一首，達到無限輪播。
     */
    public static void playGameBgm() {
        stopMenuBgm();
        stopGameBgm(); // 釋放上一局音樂資源

        String nextBgm = gameBgms[random.nextInt(gameBgms.length)];
        try {
            String path = "/com/dino/assets/" + nextBgm;
            String url = SoundManager.class.getResource(path).toExternalForm();
            Media media = new Media(url);
            currentGameBgm = new MediaPlayer(media);
            currentGameBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
            
            // 監聽播放完畢，自動換下一首
            currentGameBgm.setOnEndOfMedia(() -> {
                playGameBgm();
            });
            
            currentGameBgm.play();
        } catch (Throwable t) {
            System.err.println("無法播放遊戲背景音樂或多媒體庫缺失: " + nextBgm + " (" + t.getMessage() + ")");
        }
    }

    /**
     * 停止遊戲背景音樂並釋放播發器資源以防止記憶體洩漏。
     */
    public static void stopGameBgm() {
        if (currentGameBgm != null) {
            currentGameBgm.stop();
            currentGameBgm.dispose(); 
            currentGameBgm = null;
        }
    }

    /**
     * 暫停音樂（暫停面板彈出時呼叫）。
     */
    public static void pauseGameBgm() {
        if (currentGameBgm != null) {
            currentGameBgm.pause();
        }
    }

    /**
     * 恢復音樂。
     */
    public static void resumeGameBgm() {
        if (currentGameBgm != null) {
            currentGameBgm.play();
        }
    }

    // ==========================================
    // 音量動態變更 API
    // ==========================================
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

    /**
     * 同步更新正在播放的主選單或關卡音樂音量。
     */
    public static void updateMusicVolume() {
        if (menuBgm != null) {
            menuBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
        }
        if (currentGameBgm != null) {
            currentGameBgm.setVolume(GameConfig.masterVolume * GameConfig.musicVolume);
        }
    }
}