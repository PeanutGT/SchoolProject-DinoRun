package com.dino;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * 新 Boss (空洞騎士 Hollow Knight) 類別。
 * 繼承自 Boss。擁有獨特的 AI 狀態（IDLE、Y_SHIFT 高度準備、DASH 衝刺、DEATH 淨化死亡）。
 * 進入二階段 (HP <= 60) 時會開啟分身殘影 (Clone) 行為：
 * Boss 本體與分身會在不同高度（地面與空中）前後交錯衝刺，強烈考驗玩家對高低跳躍的掌握度。
 */
public class NewBoss extends Boss {
    
    // 空洞騎士狀態列舉：IDLE(閒置)、Y_SHIFT(高度調整過渡)、DASH(橫向衝刺攻擊)、DEATH(死亡硬直/被封印動畫)
    public enum State {
        IDLE, Y_SHIFT, DASH, DEATH
    }

    private State currentState = State.IDLE;
    private double startY;              // 位移前起始 Y 高度
    private double targetY;             // 目標 Y 高度
    private double dashSpeed = GameConfig.NEW_BOSS_DASH_SPEED; // 衝刺位移速度

    // 分身影格相關元件 (用於 HP <= 60 二階段)
    private ImageView cloneVisual;
    private Rectangle cloneHitBox;
    private Group cloneGroup;
    private double cloneX;
    private double cloneY;
    private boolean cloneActive = false;
    private double cloneTargetY;
    private boolean cloneDashing = false;

    // 時間常數定義，引入自 GameConfig
    private static final long IDLE_DURATION = GameConfig.NEW_BOSS_IDLE_DURATION_MS;   
    private static final long SHIFT_DURATION = GameConfig.NEW_BOSS_SHIFT_DURATION_MS;   
    private static final double CLONE_DELAY_SECS = GameConfig.NEW_BOSS_CLONE_DELAY_SECS; 

    // 死亡消逝狀態計時
    private boolean isDead = false;
    private long deathTime = 0;
    private static final long DEATH_DURATION = GameConfig.NEW_BOSS_DEATH_DURATION_MS; // 死亡停留 1.5 秒

    // 跑道高度坐標定義
    private final double X_START = screenWidth - 150;
    private final double Y_GROUND = groundY - height;      // 地面層高度
    private final double Y_MID = groundY - height - 30;    // 空中層高度

    /**
     * 建構子：初始化空洞騎士，設定外觀圖片鏡像翻轉 (因為圖片朝右，需向左翻轉)。
     */
    public NewBoss(Pane root, long activeGameTime, boolean isCoop) {
        // 設定空洞騎士大小為 100x90，並初始化 HP
        super(root, activeGameTime, isCoop, 100, 90, isCoop ? GameConfig.NEW_BOSS_HP_COOP : GameConfig.NEW_BOSS_HP);

        // 橫向鏡像反轉 Boss 圖檔 (向左看)
        visual.setScaleX(-1);

        // 初始化分身殘影群組與碰撞箱
        cloneVisual = new ImageView();
        cloneVisual.setSmooth(false);
        cloneVisual.setFitWidth(width);
        cloneVisual.setFitHeight(height);
        cloneVisual.setPreserveRatio(false);
        // 分身亦向左反轉
        cloneVisual.setScaleX(-1);

        cloneHitBox = new Rectangle(width, height);
        cloneHitBox.setVisible(false);

        cloneGroup = new Group(cloneVisual, cloneHitBox);

        // 初始化外觀與起點位置
        updateVisualImage();
        x = X_START;
        y = Y_GROUND;
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    @Override
    public String getName() {
        return "HOLLOW KNIGHT";
    }

    /**
     * 更新當前繪製的外觀圖片。根據目前 HP 判斷是否為二階段，以及是否正在進行衝刺。
     */
    private void updateVisualImage() {
        if (isDead) {
            // 死亡時展示被橘色光線捆綁封印的 knight_bind.png
            visual.setImage(ResourceManager.getImage("knight_bind.png"));
            return;
        }

        boolean isStage2 = hp <= 60;
        if (currentState == State.DASH) {
            // 衝刺時，切換為滑行拔刀狀態圖片
            String imgName = isStage2 ? "knight2_stand.png" : "knight_stand.png";
            visual.setImage(ResourceManager.getImage(imgName));
        } else {
            // 跑動/閒置時，使用跑步動畫圖片
            String imgName = isStage2 ? "knight2_run.png" : "knight_run.png";
            visual.setImage(ResourceManager.getImage(imgName));
        }
    }

    /**
     * 空洞騎士狀態機邏輯。
     * IDLE -> Y_SHIFT -> DASH -> (完成後循環回到 IDLE)
     */
    @Override
    protected void updateBoss(double speed, long activeGameTime, double dtSeconds) {
        long now = activeGameTime;

        if (currentState == State.DEATH) {
            updateVisualImage();
            return;
        }

        switch (currentState) {
            case IDLE:
                // 閒置時靠在右側底部
                x = X_START;
                y = Y_GROUND;
                group.setVisible(true);
                updateVisualImage();

                if (now - stateTimer > IDLE_DURATION) {
                    currentState = State.Y_SHIFT;
                    stateTimer = now;
                    startY = y;

                    // 50% 機率朝向地面衝刺，50% 機率朝高空衝刺
                    boolean targetMid = Math.random() < 0.5;
                    targetY = targetMid ? Y_MID : Y_GROUND;

                    // 二階段 (HP <= 60)：Boss 會額外釋放分身，在反向軌道隨後衝刺，封鎖跳躍躲避路徑！
                    if (hp <= 60) {
                        cloneActive = true;
                        cloneDashing = false;
                        // 分身走另一條路
                        cloneTargetY = targetMid ? Y_GROUND : Y_MID;
                        cloneX = X_START;
                        cloneY = cloneTargetY;
                        cloneGroup.setLayoutX(cloneX);
                        cloneGroup.setLayoutY(cloneY);
                        cloneVisual.setImage(ResourceManager.getImage("knight2_run.png"));

                        // 將分身渲染層次控制在 Boss 本體之下，防穿幫
                        if (!root.getChildren().contains(cloneGroup)) {
                            int insertIdx = root.getChildren().indexOf(group);
                            if (insertIdx != -1) {
                                root.getChildren().add(insertIdx, cloneGroup);
                            } else {
                                root.getChildren().add(cloneGroup);
                            }
                        }
                    }
                }
                break;

            case Y_SHIFT:
                // 在 Y 軸方向進行短暫插值位移 (500ms)，以完成高空或低空準備動作
                double elapsedShift = now - stateTimer;
                double t = Math.min(1.0, elapsedShift / (double) SHIFT_DURATION);
                y = startY + (targetY - startY) * t;
                updateVisualImage();

                if (elapsedShift >= SHIFT_DURATION) {
                    currentState = State.DASH;
                    stateTimer = now;
                }
                break;

            case DASH:
                // 本體快速向左衝刺 (橫跨螢幕)
                x -= dashSpeed * dtSeconds;
                group.setLayoutX(x);
                updateVisualImage();

                boolean bossDone = (x < -150);

                // 二階段分身以 CLONE_DELAY_SECS (0.4秒) 延遲出發衝刺
                if (cloneActive) {
                    double elapsedDash = (now - stateTimer) / 1000.0;
                    if (elapsedDash >= CLONE_DELAY_SECS) {
                        if (!cloneDashing) {
                            cloneDashing = true;
                            cloneVisual.setImage(ResourceManager.getImage("knight2_stand.png"));
                        }
                        cloneX -= dashSpeed * dtSeconds;
                        cloneGroup.setLayoutX(cloneX);
                    }

                    if (cloneX < -150) {
                        removeClone();
                    }
                }

                // 當本體與分身均完成衝刺滾出螢幕，才將位置重設回右方，並還原至 IDLE
                boolean cloneDone = !cloneActive;
                if (bossDone && cloneDone) {
                    removeClone();
                    x = X_START;
                    y = Y_GROUND;
                    group.setVisible(true);
                    group.setLayoutX(x);
                    group.setLayoutY(y);
                    currentState = State.IDLE;
                    stateTimer = now;
                }
                break;
        }

        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    /**
     * 移除場上的分身並釋放其畫面節點。
     */
    private void removeClone() {
        if (cloneActive) {
            cloneActive = false;
            cloneDashing = false;
            root.getChildren().remove(cloneGroup);
        }
    }

    /**
     * 碰撞檢測。判斷是否碰撞 Boss 本體，若為二階段，則需同時檢測是否碰上分身。
     */
    @Override
    public boolean checkCollision(Bounds dinoBounds) {
        if (currentState == State.DEATH) {
            return false; // 死亡期間 Boss 無傷害判定，防止死後補刀碰撞
        }
        if (super.checkCollision(dinoBounds)) {
            return true;
        }
        if (cloneActive && cloneHitBox != null) {
            Bounds cloneBounds = cloneHitBox.localToScene(cloneHitBox.getBoundsInLocal());
            if (cloneBounds.intersects(dinoBounds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判斷 Boss 是否被擊敗。
     * 若為血量歸零，則觸發 isDead 動畫（變更為封印圖），並在 DEATH_DURATION (1.5秒) 後才宣告徹底勝利。
     */
    @Override
    public boolean isDefeated(long activeGameTime) {
        long survivalTimeMs = isCoop ? GameConfig.BOSS_SURVIVAL_TIME_MS_COOP : GameConfig.BOSS_SURVIVAL_TIME_MS;
        if (activeGameTime - startTime >= survivalTimeMs) {
            removeClone();
            return true; // 時間超時自動退場
        }

        if (hp <= 0) {
            if (!isDead) {
                isDead = true;
                deathTime = activeGameTime;
                currentState = State.DEATH;
                removeClone();
                
                // 打倒 Boss 後清除所有尚未消失的飛行火球子彈
                for (BossProjectile p : projectiles) {
                    root.getChildren().remove(p.getView());
                }
                projectiles.clear();
                group.setVisible(true); 
                updateVisualImage();
            }
            return (activeGameTime - deathTime >= DEATH_DURATION);
        }
        return false;
    }

    @Override
    public void removeAllProjectiles() {
        super.removeAllProjectiles();
        removeClone();
    }
}
