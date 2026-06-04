package com.dino;

import java.io.File;

/**
 * 遊戲全域組態設定類別，存放視窗尺寸、物理數值、音量、角色、Boss 屬性、技能、道具機率等。
 */
public class GameConfig {
    // ==========================================
    // 視窗與物理設定
    // ==========================================
    public static final double SCREEN_WIDTH = 1000;              // 虛擬螢幕寬度
    public static final double SCREEN_HEIGHT = 500;             // 虛擬螢幕高度
    public static final double GROUND_Y = 250;                  // 恐龍站立時的地面 Y 軸座標
    public static final double GROUND_IMAGE_Y = 245;            // 地面圖片繪製的 Y 軸座標
    public static final double GRAVITY = 0.8 * 3600;            // 遊戲重力加速度 (每平方秒像素)
    public static final double JUMP_VELOCITY = -13 * 60;        // 跳躍初速度 (向上為負值)
    public static final double FAST_FALL_VELOCITY = 18 * 60;    // 快速下墜初速度 (向下為正值)
    public static final double INITIAL_SPEED = 6 * 60;          // 遊戲初始捲動速度
    public static final double MAX_SPEED = 30 * 60;             // 遊戲最大捲動速度限制
    public static final double ACCELERATION = 0.002 * 3600;     // 每秒捲動速度的加速度
    public static double masterVolume = 0.75;      // 主音量 (0.0 ~ 1.0)
    public static double sfxVolume = 1.0;         // 音效音量 (0.0 ~ 1.0)
    public static double musicVolume = 0.5;       // 背景音樂音量 (0.0 ~ 1.0)
    public static String selectedCharacter = "dino"; // 目前選取的角色代號
    public static boolean devModeEnabled = false;  // 開發者模式開關 (可看見碰撞箱與使用快捷鍵)

    // ==========================================
    // Boss 參數設定 (單人模式)
    // ==========================================
    public static final int BOSS_TRIGGER_SCORE = 1000;          // 觸發 Boss 出現的初始分數
    public static final int BOSS_INTERVAL_SCORE = 2000;         // 之後每隔多少分數觸發 Boss
    public static final int BOSS_HP = 100;                      // Boss 血量
    public static final long BOSS_SURVIVAL_TIME_MS = 100000;    // Boss 最長滯留時間 (毫秒)
    public static final long BOSS_RETREAT_GRACE_PERIOD_MS = 2000; // Boss 撤退時的緩衝時間 (毫秒)
    public static final double BOSS_SLAM_JUMP_VELOCITY = -8 * 60; // Boss 重擊起跳速度
    public static final double BOSS_CHARGE_VELOCITY = -6 * 60;  // Boss 衝鋒垂直調整速度
    public static final double BOSS_BULLET_SPEED = 5 * 60;      // Boss 子彈飛行速度
    public static final double BOSS_SHOCKWAVE_SPEED = 6 * 60;    // Boss 震波移動速度

    // ==========================================
    // Boss 參數設定 (雙人合作模式)
    // ==========================================
    public static final int BOSS_TRIGGER_SCORE_COOP = 1000;
    public static final int BOSS_INTERVAL_SCORE_COOP = 2500;
    public static final int BOSS_HP_COOP = 200;                 // 雙人模式 Boss 血量加倍
    public static final long BOSS_SURVIVAL_TIME_MS_COOP = 100000;
    public static final long BOSS_RETREAT_GRACE_PERIOD_MS_COOP = 3000;
    public static final double BOSS_SLAM_JUMP_VELOCITY_COOP = -9 * 60;
    public static final double BOSS_CHARGE_VELOCITY_COOP = -7 * 60;
    public static final double BOSS_BULLET_SPEED_COOP = 6 * 60;
    public static final double BOSS_SHOCKWAVE_SPEED_COOP = 7 * 60;

    // ==========================================
    // New Boss (空洞騎士) 參數設定
    // ==========================================
    public static final int NEW_BOSS_HP = 120;                  // 單人版空洞騎士血量
    public static final int NEW_BOSS_HP_COOP = 240;             // 雙人版空洞騎士血量
    public static final double NEW_BOSS_DASH_SPEED = 1000.0;     // 衝刺攻擊速度
    public static final long NEW_BOSS_IDLE_DURATION_MS = 2000;  // 動作間的發呆等待時間 (毫秒)
    public static final long NEW_BOSS_SHIFT_DURATION_MS = 500;  // 位移動作持續時間
    public static final double NEW_BOSS_CLONE_DELAY_SECS = 0.4; // 殘影產生的時間間隔
    public static final long NEW_BOSS_DEATH_DURATION_MS = 1500; // 死亡動畫播放時間 (毫秒)

    // ==========================================
    // 技能與道具設定
    // ==========================================
    public static final double SWORD_ATTACK_RANGE = 600;        // 木劍攻擊的有效範圍距離 (像素)
    public static final int OBSTACLE_CLEAR_SCORE = 50;          // 擊碎障礙物所獲得的分數加成
    public static final int MILK_SCORE_BONUS = 500;             // 喝牛奶直接獲得的分數
    public static final long MILK_FOG_DURATION_MS = 5000;       // 牛奶霧氣隱身效果的持續時間 (毫秒)
    public static final long BARRIER_DURATION_MS = 12000;       // 護盾防護效果的持續時間 (毫秒)
    public static final int QUESTION_BLOCK_INTERVAL = 250;      // 每 250 分生成一個問號方塊
    public static final int COIN_SPAWN_INTERVAL = 40;           // 每 40 分生成一個金幣

    // ==========================================
    // 障礙物與飛龍 (Bird) 距離設定
    // ==========================================
    public static final double OBSTACLE_MIN_DISTANCE_BASE = 220; // 障礙物生成的最小基本距離
    public static final double OBSTACLE_DISTANCE_SPEED_RATIO = 28.0 / 60.0; // 隨速度拉長生成距離的比例
    public static final double OBSTACLE_MAX_RANDOM_DISTANCE = 350; // 隨機額外生成距離的最大值
    public static final int BIRD_APPEAR_SCORE = 300;            // 飛鳥開始出現的起點分數
    public static final double BIRD_SPAWN_PROBABILITY = 0.35;    // 障礙物生成時為飛鳥的機率
    public static final double OBSTACLE_MIN_DISTANCE_BASE_VERSUS = 200; // 對戰模式障礙物最小距離
    public static final double OBSTACLE_DISTANCE_SPEED_RATIO_VERSUS = 22.0 / 60.0;
    public static final double OBSTACLE_MAX_RANDOM_DISTANCE_VERSUS = 260;

    // ==========================================
    // 玩家背包道具持有數量 (Q:金蘋果, W:牛奶, E:附魔書, R:護盾, F:木劍)
    // ==========================================
    public static int goldenAppleCount = 0;
    public static int milkBucketCount = 0;
    public static int enchantedBookCount = 0;
    public static int barrierCount = 0;
    public static int woodenSwordCount = 0;

    // ==========================================
    // 道具在問號方塊中隨機掉落的權重 (總和即為分母，數值越高機率越高)
    // ==========================================
    public static int weightGoldenApple = 5;
    public static int weightMilkBucket = 30;
    public static int weightEnchantedBook = 30;
    public static int weightBarrier = 5;
    public static int weightWoodenSword = 40;
    public static boolean isFullScreen = false;                 // 目前是否為全螢幕

    public static double getScreenWidth() { return SCREEN_WIDTH; }
    public static double getScreenHeight() { return SCREEN_HEIGHT; }

    /**
     * 動態獲取當前執行檔所在的根目錄，實作可攜式路徑 (Portable Mode)。
     * @return 程式運行的基準資料夾路徑 File 物件
     */
    public static File getBaseDirectory() {
        try {
            File file = new File(GameConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (file.isFile()) {
                // 如果是打包後的 jar 或 exe 檔，回傳該檔案所在的目錄
                return file.getParentFile();
            } else {
                // 如果是在 IDE 中執行 (指向 target/classes)，則退回專案根目錄
                File parent = file.getParentFile();
                if (parent != null && parent.getName().equals("target")) {
                    return parent.getParentFile();
                }
                return file;
            }
        } catch (Exception e) {
            // 例外發生時，退而求其次取得系統 user.dir
            return new File(System.getProperty("user.dir"));
        }
    }
}
