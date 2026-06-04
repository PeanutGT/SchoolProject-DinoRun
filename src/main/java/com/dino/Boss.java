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

public abstract class Boss {
    protected Group group;
    protected ImageView visual;
    protected Rectangle hitBox;

    protected int hp;
    protected int maxHp;
    protected long startTime;
    protected long stateTimer;

    protected double x, y;
    protected final double screenWidth = GameConfig.SCREEN_WIDTH;
    protected final double groundY = GameConfig.GROUND_Y;

    protected double width;
    protected double height;

    protected Pane root;
    protected List<BossProjectile> projectiles = new ArrayList<>();
    protected boolean isCoop;
    protected double velocityY = 0;

    public Boss(Pane root, long activeGameTime, boolean isCoop, double width, double height, int maxHp) {
        this.isCoop = isCoop;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.root = root;
        this.width = width;
        this.height = height;
        this.x = screenWidth - 150;
        this.y = groundY - height;

        visual = new ImageView();
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

    public abstract String getName();

    protected abstract void updateBoss(double speed, long activeGameTime, double dtSeconds);

    public void update(double speed, long activeGameTime, double dtSeconds) {
        updateBoss(speed, activeGameTime, dtSeconds);

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
        long survivalTimeMs = isCoop ? GameConfig.BOSS_SURVIVAL_TIME_MS_COOP : GameConfig.BOSS_SURVIVAL_TIME_MS;
        return hp <= 0 || (activeGameTime - startTime >= survivalTimeMs); // 存活滿指定時間或HP歸零則撤退
    }

    public void takeDamage(int amount) {
        this.hp -= amount;
        if (this.hp < 0) {
            this.hp = 0;
        }
    }

    public int getHp() {
        return this.hp;
    }

    public int getMaxHp() {
        return this.maxHp;
    }

    public double getX() {
        return this.x;
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

    // Factory method to spawn a random boss
    public static Boss spawnRandomBoss(Pane root, long activeGameTime, boolean isCoop) {
        double r = Math.random();
        if (r < 0.5) {
            return new BowserBoss(root, activeGameTime, isCoop);
        } else {
            return new NewBoss(root, activeGameTime, isCoop);
        }
    }

    // 內部類別：處理 Boss 產生的所有投射物 (子彈與震波)
    public class BossProjectile {
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
