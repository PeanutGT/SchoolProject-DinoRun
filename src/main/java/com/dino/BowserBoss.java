package com.dino;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * 庫巴 Boss 類別。
 * 繼承自 Boss。擁有獨立的狀態機（IDLE, 蓄力遠程, 射擊, 蓄力砸地, 砸地, 蓄力波浪, 波浪射擊），
 * 能射出不同高度的火球、往空中跳躍並於落地時產生衝擊震波，以及射出上下起伏的正弦波火球。
 */
public class BowserBoss extends Boss {
    
    // 庫巴狀態列舉：IDLE(閒置)、PRE_RANGED(遠程火球蓄力)、RANGED(射火球)、
    // PRE_SLAM(砸地蓄力)、SLAM(跳起砸地中)、PRE_S_RANGED(正弦火球蓄力)、S_RANGED(射正弦火球)
    public enum State {
        IDLE, PRE_RANGED, RANGED, PRE_SLAM, SLAM, PRE_S_RANGED, S_RANGED
    }

    private Image[] walkFrames;         // 走路動畫影格
    private State currentState = State.IDLE; // 當前狀態，預設為閒置

    private int walkFrameCounter = 0;   // 走路動畫影格計數器
    
    private double slamJumpVelocity;    // 砸地起跳垂直速度
    private double bulletSpeed;         // 火球移動速度
    private double shockwaveSpeed;      // 地震波移動速度

    /**
     * 建構子：依據是否為合作模式調整血量上限、跳躍初速與子彈初速。
     */
    public BowserBoss(Pane root, long activeGameTime, boolean isCoop) {
        super(root, activeGameTime, isCoop, 84, 81, isCoop ? GameConfig.BOSS_HP_COOP : GameConfig.BOSS_HP);
        
        this.slamJumpVelocity = isCoop ? GameConfig.BOSS_SLAM_JUMP_VELOCITY_COOP : GameConfig.BOSS_SLAM_JUMP_VELOCITY;
        this.bulletSpeed = isCoop ? GameConfig.BOSS_BULLET_SPEED_COOP : GameConfig.BOSS_BULLET_SPEED;
        this.shockwaveSpeed = isCoop ? GameConfig.BOSS_SHOCKWAVE_SPEED_COOP : GameConfig.BOSS_SHOCKWAVE_SPEED;

        // 載入庫巴左右踏步的走路圖片
        walkFrames = new Image[] {
                ResourceManager.getImage("boss_bowser_walk1.png"),
                ResourceManager.getImage("boss_bowser_walk2.png")
        };

        visual.setImage(walkFrames[0]);
    }

    @Override
    public String getName() {
        return "Bowser";
    }

    /**
     * 庫巴核心狀態更新狀態機。
     */
    @Override
    protected void updateBoss(double speed, long activeGameTime, double dtSeconds) {
        long now = activeGameTime;
        updateWalkAnimation();

        switch (currentState) {
            case IDLE:
                // 閒置狀態下固定靠在右側，貼齊地面
                x = screenWidth - 150;
                y = groundY - height;

                // 超過攻擊延遲時間後，隨機挑選一招發動
                if (now - stateTimer > getObstacleLikeAttackDelay(speed)) {
                    pickRandomAttack(activeGameTime);
                }
                break;
            case PRE_RANGED:
                // 蓄力狀態：半透明提示玩家即將攻擊
                visual.setOpacity(0.75);
                if (now - stateTimer > 1000) {
                    currentState = State.RANGED;
                    stateTimer = now;
                    visual.setOpacity(1.0);
                    fireBullet(); // 發射火球
                }
                break;
            case RANGED:
                // 射擊後短暫停留 500 毫秒即恢復閒置
                if (now - stateTimer > 500) {
                    currentState = State.IDLE;
                    stateTimer = now;
                    visual.setOpacity(1.0);
                }
                break;
            case PRE_SLAM:
                visual.setOpacity(0.75);
                if (now - stateTimer > 1000) {
                    currentState = State.SLAM;
                    stateTimer = now;
                    visual.setOpacity(1.0);
                    velocityY = slamJumpVelocity; // 給予向上的起跳初速度
                }
                break;
            case SLAM:
                // 砸地物理公式更新 Y 軸座標
                y += velocityY * dtSeconds;
                velocityY += GameConfig.GRAVITY * dtSeconds;
                // 落地檢測
                if (y >= groundY - height) {
                    y = groundY - height;
                    velocityY = 0;
                    fireShockwave(); // 落地瞬間在地面產生衝擊震波
                    currentState = State.IDLE;
                    stateTimer = now;
                    visual.setOpacity(1.0);
                }
                break;
            case PRE_S_RANGED:
                visual.setOpacity(0.75);
                if (now - stateTimer > 1000) {
                    currentState = State.S_RANGED;
                    stateTimer = now;
                    visual.setOpacity(1.0);
                    fireSFireball(); // 發射正弦波上下晃動的火球
                }
                break;
            case S_RANGED:
                if (now - stateTimer > 500) {
                    currentState = State.IDLE;
                    stateTimer = now;
                    visual.setOpacity(1.0);
                }
                break;
        }

        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    /**
     * 每 12 影格循環踏步走路動畫。
     */
    private void updateWalkAnimation() {
        walkFrameCounter++;
        if (walkFrameCounter % 12 == 0) {
            int index = (walkFrameCounter / 12) % walkFrames.length;
            visual.setImage(walkFrames[index]);
        }
    }

    /**
     * 計算合適的攻擊間隔，依據目前遊戲速度動態拉長，確保難度曲線平滑。
     */
    private long getObstacleLikeAttackDelay(double speed) {
        double obstacleSpacing = 220 + speed * (28.0 / 60.0);
        double framesUntilNextObstacle = obstacleSpacing / speed;
        return Math.max(650, Math.round(framesUntilNextObstacle * 1000));
    }

    /**
     * 隨機抽取蓄力技能。
     */
    private void pickRandomAttack(long activeGameTime) {
        double r = Math.random();
        if (r < 0.40) {
            currentState = State.PRE_RANGED;
        } else if (r < 0.60) {
            currentState = State.PRE_SLAM;
        } else {
            currentState = State.PRE_S_RANGED;
        }
        stateTimer = activeGameTime;
    }

    /**
     * 發射火球：50% 機率為高空火球（需蹲下躲避），50% 機率為低空火球（需起跳）。
     */
    private void fireBullet() {
        boolean high = Math.random() > 0.5;
        double bulletY = high ? groundY - 80 : groundY - 30;
        BossProjectile p = new BossProjectile(
                x,
                bulletY,
                48,
                48,
                bulletSpeed,
                new String[] { "boss_fireball_1.png", "boss_fireball_2.png", "boss_fireball_3.png",
                        "boss_fireball_4.png" },
                false);
        projectiles.add(p);
        root.getChildren().add(p.getView());
    }

    /**
     * 發射落地衝擊震波：屬於寬扁的地面障礙物。
     */
    private void fireShockwave() {
        BossProjectile p = new BossProjectile(
                x,
                groundY - 30,
                64,
                24,
                shockwaveSpeed,
                new String[] { "boss_fireball_1.png", "boss_fireball_2.png" },
                false);
        projectiles.add(p);
        root.getChildren().add(p.getView());
    }

    /**
     * 發射正弦上下晃動的波浪火球：標記 isSWave 為 true，使其以 Sin 波浮動前進，干擾玩家判斷。
     */
    private void fireSFireball() {
        double bulletY = groundY - 80;
        BossProjectile p = new BossProjectile(
                x,
                bulletY,
                64,
                24,
                110.0,
                new String[] { "boss_fire_1.png", "boss_fire_2.png" },
                true);
        projectiles.add(p);
        root.getChildren().add(p.getView());
    }
}
