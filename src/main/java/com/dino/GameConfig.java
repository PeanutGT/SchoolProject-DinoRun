package com.dino;

public class GameConfig {
    // 視窗設定
    public static final double SCREEN_WIDTH = 1000;
    public static final double SCREEN_HEIGHT = 500;
    public static final double GROUND_Y = 250;
    public static final double GROUND_IMAGE_Y = 245;
    public static final double GRAVITY = 0.8 * 3600;
    public static final double JUMP_VELOCITY = -13 * 60;
    public static final double FAST_FALL_VELOCITY = 18 * 60;
    public static final double INITIAL_SPEED = 6 * 60;
    public static final double MAX_SPEED = 16 * 60;
    public static final double ACCELERATION = 0.002 * 3600;
    public static double masterVolume = 0.5;      // 0.0 ~ 1.0
    public static double uiScale = 1.0;           // 1.0 = 800x400, 1.2 = 960x480
    public static String selectedCharacter = "dino";
    public static boolean devModeEnabled = false;

    // Boss 戰設定
    public static final int BOSS_TRIGGER_SCORE = 1000;
    public static final int BOSS_HP = 1000;
    public static final long BOSS_SURVIVAL_TIME_MS = 30000;
    public static final long BOSS_RETREAT_GRACE_PERIOD_MS = 2000;
    public static final double BOSS_SLAM_JUMP_VELOCITY = -8 * 60;
    public static final double BOSS_CHARGE_VELOCITY = -6 * 60;
    public static final double BOSS_BULLET_SPEED = 3 * 60;
    public static final double BOSS_SHOCKWAVE_SPEED = 4 * 60;

    // 技能與道具設定
    public static final double SWORD_ATTACK_RANGE = 400;
    public static final int OBSTACLE_CLEAR_SCORE = 50;
    public static final int MILK_SCORE_BONUS = 500;
    public static final long MILK_FOG_DURATION_MS = 5000;
    public static final long BARRIER_DURATION_MS = 5000;
    public static final int QUESTION_BLOCK_INTERVAL = 250;

    // 道具持有數量 (Q, W, E, R, F)
    public static int goldenAppleCount = 0;
    public static int milkBucketCount = 0;
    public static int enchantedBookCount = 0;
    public static int barrierCount = 0;
    public static int woodenSwordCount = 0;

    public static double getScreenWidth() { return SCREEN_WIDTH * uiScale; }
    public static double getScreenHeight() { return SCREEN_HEIGHT * uiScale; }
}
