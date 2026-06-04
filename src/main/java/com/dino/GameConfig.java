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
    public static final double MAX_SPEED = 30 * 60;
    public static final double ACCELERATION = 0.002 * 3600;
    public static double masterVolume = 0.75;      // 0.0 ~ 1.0
    public static double sfxVolume = 1.0;         // 0.0 ~ 1.0
    public static double musicVolume = 0.5;       // 0.0 ~ 1.0
    public static String selectedCharacter = "dino";
    public static boolean devModeEnabled = false;

    // Boss 參數設定 (單人)
    public static final int BOSS_TRIGGER_SCORE = 2000;
    public static final int BOSS_INTERVAL_SCORE = 2000;
    public static final int BOSS_HP = 100;
    public static final long BOSS_SURVIVAL_TIME_MS = 100000;
    public static final long BOSS_RETREAT_GRACE_PERIOD_MS = 2000;
    public static final double BOSS_SLAM_JUMP_VELOCITY = -8 * 60;
    public static final double BOSS_CHARGE_VELOCITY = -6 * 60;
    public static final double BOSS_BULLET_SPEED = 5 * 60;
    public static final double BOSS_SHOCKWAVE_SPEED = 6 * 60;

    // Boss 參數設定 (雙人合作)
    public static final int BOSS_TRIGGER_SCORE_COOP = 2500;
    public static final int BOSS_INTERVAL_SCORE_COOP = 2500;
    public static final int BOSS_HP_COOP = 200;
    public static final long BOSS_SURVIVAL_TIME_MS_COOP = 100000;
    public static final long BOSS_RETREAT_GRACE_PERIOD_MS_COOP = 3000;
    public static final double BOSS_SLAM_JUMP_VELOCITY_COOP = -9 * 60;
    public static final double BOSS_CHARGE_VELOCITY_COOP = -7 * 60;
    public static final double BOSS_BULLET_SPEED_COOP = 6 * 60;
    public static final double BOSS_SHOCKWAVE_SPEED_COOP = 7 * 60;

    // New Boss (空洞騎士) 參數設定
    public static final int NEW_BOSS_HP = 120;
    public static final int NEW_BOSS_HP_COOP = 240;
    public static final double NEW_BOSS_DASH_SPEED = 1000.0;
    public static final long NEW_BOSS_IDLE_DURATION_MS = 2000;
    public static final long NEW_BOSS_SHIFT_DURATION_MS = 500;
    public static final double NEW_BOSS_CLONE_DELAY_SECS = 0.4;
    public static final long NEW_BOSS_DEATH_DURATION_MS = 1500;

    // 技能與道具設定
    public static final double SWORD_ATTACK_RANGE = 600;
    public static final int OBSTACLE_CLEAR_SCORE = 50;
    public static final int MILK_SCORE_BONUS = 500;
    public static final long MILK_FOG_DURATION_MS = 5000;
    public static final long BARRIER_DURATION_MS = 12000;
    public static final int QUESTION_BLOCK_INTERVAL = 250;
    public static final int COIN_SPAWN_INTERVAL = 40; // 每 40 分生成金幣

    // 障礙物與飛龍設定
    public static final double OBSTACLE_MIN_DISTANCE_BASE = 220;
    public static final double OBSTACLE_DISTANCE_SPEED_RATIO = 28.0 / 60.0;
    public static final double OBSTACLE_MAX_RANDOM_DISTANCE = 350;
    public static final int BIRD_APPEAR_SCORE = 300;
    public static final double BIRD_SPAWN_PROBABILITY = 0.35;
    public static final double OBSTACLE_MIN_DISTANCE_BASE_VERSUS = 200;
    public static final double OBSTACLE_DISTANCE_SPEED_RATIO_VERSUS = 22.0 / 60.0;
    public static final double OBSTACLE_MAX_RANDOM_DISTANCE_VERSUS = 260;

    // 道具持有數量 (Q, W, E, R, F)
    public static int goldenAppleCount = 0;
    public static int milkBucketCount = 0;
    public static int enchantedBookCount = 0;
    public static int barrierCount = 0;
    public static int woodenSwordCount = 0;

    // 道具掉落權重 (數值越高越容易出現)
    public static int weightGoldenApple = 5;
    public static int weightMilkBucket = 30;
    public static int weightEnchantedBook = 30;
    public static int weightBarrier = 5;
    public static int weightWoodenSword = 40;
    public static boolean isFullScreen = false;

    public static double getScreenWidth() { return SCREEN_WIDTH; }
    public static double getScreenHeight() { return SCREEN_HEIGHT; }
}
