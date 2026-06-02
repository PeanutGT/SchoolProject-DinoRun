package com.dino;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class Boss {
    public enum State {
        IDLE, PRE_RANGED, RANGED, PRE_SLAM, SLAM, PRE_S_RANGED, S_RANGED
    }

    private Group group;
    private ImageView visual;
    private Image[] walkFrames;
    private Rectangle hitBox;

    private int hp = GameConfig.BOSS_HP;
    private long startTime;
    private long stateTimer;
    private State currentState = State.IDLE;

    private double x, y;
    private final double screenWidth = GameConfig.SCREEN_WIDTH;
    private final double groundY = GameConfig.GROUND_Y;

    private final double width = 84;
    private final double height = 81;
    private final double startX = screenWidth - 150;
    private final double startY = groundY - height;

    private Pane root;
    private List<BossProjectile> projectiles = new ArrayList<>();

    private double velocityY = 0;
    private int walkFrameCounter = 0;

    public Boss(Pane root, long activeGameTime) {
        this.root = root;
        this.x = startX;
        this.y = startY;

        walkFrames = new Image[] {
                ResourceManager.getImage("boss_bowser_walk1.png"),
                ResourceManager.getImage("boss_bowser_walk2.png")
        };

        visual = new ImageView(walkFrames[0]);
        visual.setSmooth(false);
        visual.setFitWidth(width);
        visual.setFitHeight(height);
        visual.setPreserveRatio(false);

        hitBox = new Rectangle(width, height);
        hitBox.setVisible(false);

        group = new Group(visual, hitBox);
        group.setLayoutX(x);
        group.setLayoutY(y);

        root.getChildren().add(group);

        startTime = activeGameTime;
        stateTimer = activeGameTime;
    }

    public void update(double speed, long activeGameTime, double dtSeconds) {
        long now = activeGameTime;
        updateWalkAnimation();

        // Update Boss AI State Machine
        switch (currentState) {
            case IDLE:
                x = startX;
                y = startY;

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
                    velocityY = GameConfig.BOSS_SLAM_JUMP_VELOCITY; // 向上跳躍
                }
                break;
            case SLAM:
                y += velocityY * dtSeconds;
                velocityY += GameConfig.GRAVITY * dtSeconds;
                if (y >= startY) {
                    y = startY;
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

        // Update Projectiles
        Iterator<BossProjectile> it = projectiles.iterator();
        while (it.hasNext()) {
            BossProjectile p = it.next();
            p.update(speed * dtSeconds, dtSeconds);
            if (p.isOffScreen()) {
                root.getChildren().remove(p.getView());
                it.remove();
            }
        }
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
                GameConfig.BOSS_BULLET_SPEED,
                new String[] { "boss_fireball_1.png", "boss_fireball_2.png", "boss_fireball_3.png",
                        "boss_fireball_4.png" },
                false);
        projectiles.add(p);
        root.getChildren().add(p.getView());
    }

    private void fireShockwave() {
        // 地震波使用貼地直線型火焰
        BossProjectile p = new BossProjectile(
                x,
                groundY - 30,
                64,
                24,
                GameConfig.BOSS_SHOCKWAVE_SPEED,
                new String[] { "boss_fireball_1.png", "boss_fireball_2.png" },
                false);
        projectiles.add(p);
        root.getChildren().add(p.getView());
    }

    private void fireSFireball() {
        // S型火球，從中空高度射出，以 110 px/s 的慢速進行上下 S 型漂移
        double bulletY = groundY - 80;
        BossProjectile p = new BossProjectile(
                x,
                bulletY,
                64,
                24,
                110.0, // 大幅降低速度，讓正弦波波動極其清晰好躲避
                new String[] { "boss_fire_1.png", "boss_fire_2.png" },
                true);
        projectiles.add(p);
        root.getChildren().add(p.getView());
    }

    public boolean checkCollision(Bounds dinoBounds) {
        // 檢查 Boss 本體碰撞
        if (getHitBoxBounds().intersects(dinoBounds))
            return true;

        // 檢查所有彈幕與震波
        for (BossProjectile p : projectiles) {
            if (p.getHitBoxBounds().intersects(dinoBounds))
                return true;
        }
        return false;
    }

    public boolean isDefeated(long activeGameTime) {
        return activeGameTime - startTime >= GameConfig.BOSS_SURVIVAL_TIME_MS; // 存活滿指定時間即算擊退
    }

    public void removeAllProjectiles() {
        for (BossProjectile p : projectiles) {
            root.getChildren().remove(p.getView());
        }
        projectiles.clear();
        root.getChildren().remove(group);
    }

    public Bounds getHitBoxBounds() {
        return hitBox.localToScene(hitBox.getBoundsInLocal());
    }

    // 內部類別：處理 Boss 產生的所有投射物 (子彈與震波)
    class BossProjectile {
        private Group pGroup;
        private Rectangle pHitBox;
        private double pX, pY;
        private double pSpeed;

        private boolean isSWave = false;
        private double baselineY;
        private double waveTime = 0.0;
        private double amplitude = 60.0; // 上下振幅 60 像素
        private double frequency = 5.0; // 頻率

        private ImageView pImageView;
        private Image[] frames;
        private int frameCounter = 0;

        public BossProjectile(double x, double y, double w, double h, double speed, String[] imageNames,
                boolean isSWave) {
            this.pX = x;
            this.pY = y;
            this.baselineY = y;
            this.pSpeed = speed;
            this.isSWave = isSWave;

            frames = new Image[imageNames.length];
            for (int i = 0; i < imageNames.length; i++) {
                frames[i] = ResourceManager.getImage(imageNames[i]);
            }

            pImageView = new ImageView(frames[0]);
            pImageView.setSmooth(false);
            pImageView.setFitWidth(w);
            pImageView.setFitHeight(h);
            pImageView.setPreserveRatio(false);

            pHitBox = new Rectangle(w - 8, h - 12);
            pHitBox.setX(4);
            pHitBox.setY(6);
            pHitBox.setVisible(false);

            pGroup = new Group(pImageView, pHitBox);
            pGroup.setLayoutX(x);
            pGroup.setLayoutY(y);
        }

        public void update(double deltaSpeed, double dtSeconds) {
            // 移動速度 = 投射物基礎速度 + 當前遊戲地板速度
            pX -= (pSpeed * dtSeconds + deltaSpeed);
            pGroup.setLayoutX(pX);

            if (isSWave) {
                waveTime += dtSeconds * frequency;
                pY = baselineY + amplitude * Math.sin(waveTime);
                pGroup.setLayoutY(pY);
            }
            updateAnimation();
        }

        private void updateAnimation() {
            frameCounter++;
            if (frameCounter % 8 == 0) {
                int index = (frameCounter / 8) % frames.length;
                pImageView.setImage(frames[index]);
            }
        }

        public boolean isOffScreen() {
            return pX < -200;
        }

        public Bounds getHitBoxBounds() {
            return pHitBox.localToScene(pHitBox.getBoundsInLocal());
        }

        public Group getView() {
            return pGroup;
        }
    }
}
