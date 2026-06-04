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

/**
 * 抽象 Boss 基礎類別。
 * 定義了 Boss 的共用屬性（血量、坐標、子彈清單、雙人標記等）與共用行為，
 * 例如子彈更新、碰撞檢測（Boss本體與子彈獨立碰撞判定）、傷害處理、工廠生成方法，
 * 以及內部類別 BossProjectile（處理火焰彈或波浪式彈幕）。
 */
public abstract class Boss {
    protected Group group;         // JavaFX 視圖群組
    protected ImageView visual;    // Boss 外觀圖片顯示器
    protected Rectangle hitBox;    // 碰撞箱

    protected int hp;              // 當前生命值
    protected int maxHp;           // 最大生命值
    protected long startTime;      // 登場時間
    protected long stateTimer;     // 當前狀態的持續計時器

    protected double x, y;         // 座標點
    protected final double screenWidth = GameConfig.SCREEN_WIDTH;
    protected final double groundY = GameConfig.GROUND_Y;

    protected double width;        // Boss 寬度
    protected double height;       // Boss 高度

    protected Pane root;           // 畫布 Pane 節點
    protected List<BossProjectile> projectiles = new ArrayList<>(); // 存放目前在場上的投射子彈
    protected boolean isCoop;      // 是否為雙人合作模式
    protected double velocityY = 0;// Boss 垂直速度

    /**
     * 建構子：初始化座標、設定寬高外觀、放置碰撞箱並將 Boss 加入主渲染層。
     */
    public Boss(Pane root, long activeGameTime, boolean isCoop, double width, double height, int maxHp) {
        this.isCoop = isCoop;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.root = root;
        this.width = width;
        this.height = height;
        // 預設誕生在螢幕右側
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

    /**
     * 獲取 Boss 名稱。
     */
    public abstract String getName();

    /**
     * 抽象子類實作：由子類 Boss 實作其具體的攻擊模式狀態機（如跳躍砸地、衝鋒、射子彈等）。
     */
    protected abstract void updateBoss(double speed, long activeGameTime, double dtSeconds);

    /**
     * 每幀更新：驅動子類 Boss 的狀態機，並更新所有投射子彈的位置。
     * 若子彈滾出螢幕則自動從畫面上移除以釋放記憶體。
     */
    public void update(double speed, long activeGameTime, double dtSeconds) {
        updateBoss(speed, activeGameTime, dtSeconds);

        // 更新投射物子彈
        Iterator<BossProjectile> it = projectiles.iterator();
        while (it.hasNext()) {
            BossProjectile p = it.next();
            // 子彈位移需受當前遊戲滾動速度加乘
            p.update(speed * dtSeconds, dtSeconds);
            if (p.isOffScreen()) {
                root.getChildren().remove(p.getView());
                it.remove();
            }
        }
    }

    /**
     * 碰撞檢測。判斷恐龍是否撞上 Boss 本體或其射出的投射子彈。
     * @param dinoBounds 恐龍碰撞箱場景邊界
     * @return 是否碰撞
     */
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

    /**
     * 檢查 Boss 是否被打倒（HP 歸零，或在場上存活時間超時）。
     */
    public boolean isDefeated(long activeGameTime) {
        long survivalTimeMs = isCoop ? GameConfig.BOSS_SURVIVAL_TIME_MS_COOP : GameConfig.BOSS_SURVIVAL_TIME_MS;
        return hp <= 0 || (activeGameTime - startTime >= survivalTimeMs);
    }

    /**
     * 承受傷害。
     */
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

    /**
     * 當 Boss 被擊敗時，清空目前留在場上所有未銷毀的子彈投射物，並將 Boss 節點從 Pane 拔除。
     */
    public void removeAllProjectiles() {
        for (BossProjectile p : projectiles) {
            root.getChildren().remove(p.getView());
        }
        projectiles.clear();
        root.getChildren().remove(group);
    }

    /**
     * 取得 Boss 碰撞箱場景邊界。
     */
    public Bounds getHitBoxBounds() {
        return hitBox.localToScene(hitBox.getBoundsInLocal());
    }

    /**
     * 工廠方法：隨機召喚 BowserBoss（庫巴）或 NewBoss（空洞騎士）。
     * @param root 渲染畫布
     * @param activeGameTime 遊戲累積時間
     * @param isCoop 是否為雙人合作
     */
    public static Boss spawnRandomBoss(Pane root, long activeGameTime, boolean isCoop) {
        double r = Math.random();
        if (r < 0.5) {
            return new BowserBoss(root, activeGameTime, isCoop);
        } else {
            return new NewBoss(root, activeGameTime, isCoop);
        }
    }

    /**
     * 內部類別：處理 Boss 產生的所有投射物 (子彈與震波)。
     */
    public class BossProjectile {
        private Group pGroup;
        private Rectangle pHitBox;
        private double pX, pY;
        private double pSpeed;

        private boolean isSWave = false; // 是否為正弦波式震波
        private double baselineY;
        private double waveTime = 0.0;
        private double amplitude = 60.0; // 上下波浪振幅 60 像素
        private double frequency = 5.0; // 頻率

        private ImageView pImageView;
        private Image[] frames;
        private int frameCounter = 0;

        /**
         * 建立投射物。
         * @param x 初始 X 座標
         * @param y 初始 Y 座標
         * @param w 寬度
         * @param h 高度
         * @param speed 向左飛行的相對速度
         * @param imageNames 投射物所用動畫圖片檔名陣列
         * @param isSWave 是否呈現正弦曲線上下波動
         */
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

        /**
         * 更新投射物位移，若設定為正弦波波浪，則藉由 Math.sin() 在 Y 軸方向動態推算。
         */
        public void update(double deltaSpeed, double dtSeconds) {
            // 移動速度 = 投射物基礎向左飛行速度 + 目前遊戲地板的向左移動速度
            pX -= (pSpeed * dtSeconds + deltaSpeed);
            pGroup.setLayoutX(pX);

            if (isSWave) {
                waveTime += dtSeconds * frequency;
                pY = baselineY + amplitude * Math.sin(waveTime);
                pGroup.setLayoutY(pY);
            }
            updateAnimation();
        }

        /**
         * 每 8 影格循環切換子彈影格，產生動態火焰球之視覺效果。
         */
        private void updateAnimation() {
            frameCounter++;
            if (frameCounter % 8 == 0) {
                int index = (frameCounter / 8) % frames.length;
                pImageView.setImage(frames[index]);
            }
        }

        /**
         * 判定投射物是否已飛出左方界外（小於 -200 像素）。
         */
        public boolean isOffScreen() {
            return pX < -200;
        }

        /**
         * 獲取子彈碰撞矩形邊界。
         */
        public Bounds getHitBoxBounds() {
            return pHitBox.localToScene(pHitBox.getBoundsInLocal());
        }

        /**
         * 獲取子彈視圖節點。
         */
        public Group getView() {
            return pGroup;
        }
    }
}
