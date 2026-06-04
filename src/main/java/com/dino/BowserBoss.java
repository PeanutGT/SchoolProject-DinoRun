package com.dino;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class BowserBoss extends Boss {
    public enum State {
        IDLE, PRE_RANGED, RANGED, PRE_SLAM, SLAM, PRE_S_RANGED, S_RANGED
    }

    private Image[] walkFrames;
    private State currentState = State.IDLE;

    private int walkFrameCounter = 0;
    
    private double slamJumpVelocity;
    private double bulletSpeed;
    private double shockwaveSpeed;

    public BowserBoss(Pane root, long activeGameTime, boolean isCoop) {
        super(root, activeGameTime, isCoop, 84, 81, isCoop ? GameConfig.BOSS_HP_COOP : GameConfig.BOSS_HP);
        
        this.slamJumpVelocity = isCoop ? GameConfig.BOSS_SLAM_JUMP_VELOCITY_COOP : GameConfig.BOSS_SLAM_JUMP_VELOCITY;
        this.bulletSpeed = isCoop ? GameConfig.BOSS_BULLET_SPEED_COOP : GameConfig.BOSS_BULLET_SPEED;
        this.shockwaveSpeed = isCoop ? GameConfig.BOSS_SHOCKWAVE_SPEED_COOP : GameConfig.BOSS_SHOCKWAVE_SPEED;

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

    @Override
    protected void updateBoss(double speed, long activeGameTime, double dtSeconds) {
        long now = activeGameTime;
        updateWalkAnimation();

        // Update Boss AI State Machine
        switch (currentState) {
            case IDLE:
                x = screenWidth - 150;
                y = groundY - height;

                if (now - stateTimer > getObstacleLikeAttackDelay(speed)) {
                    pickRandomAttack(activeGameTime);
                }
                break;
            case PRE_RANGED:
                visual.setOpacity(0.75);
                if (now - stateTimer > 1000) {
                    currentState = State.RANGED;
                    stateTimer = now;
                    visual.setOpacity(1.0);
                    fireBullet();
                }
                break;
            case RANGED:
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
                    velocityY = slamJumpVelocity; // 往上跳躍
                }
                break;
            case SLAM:
                y += velocityY * dtSeconds;
                velocityY += GameConfig.GRAVITY * dtSeconds;
                if (y >= groundY - height) {
                    y = groundY - height;
                    velocityY = 0;
                    fireShockwave();
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
                    fireSFireball();
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

    private void updateWalkAnimation() {
        walkFrameCounter++;
        if (walkFrameCounter % 12 == 0) {
            int index = (walkFrameCounter / 12) % walkFrames.length;
            visual.setImage(walkFrames[index]);
        }
    }

    private long getObstacleLikeAttackDelay(double speed) {
        double obstacleSpacing = 220 + speed * (28.0 / 60.0);
        double framesUntilNextObstacle = obstacleSpacing / speed;
        return Math.max(650, Math.round(framesUntilNextObstacle * 1000));
    }

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
