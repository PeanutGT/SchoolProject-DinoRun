package com.dino;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class NewBoss extends Boss {
    public enum State {
        IDLE, Y_SHIFT, DASH, DEATH
    }

    private State currentState = State.IDLE;
    private double startY;
    private double targetY;
    private double dashSpeed = GameConfig.NEW_BOSS_DASH_SPEED; // 快速衝刺速度

    // Clone fields (for Stage 2)
    private ImageView cloneVisual;
    private Rectangle cloneHitBox;
    private Group cloneGroup;
    private double cloneX;
    private double cloneY;
    private boolean cloneActive = false;
    private double cloneTargetY;
    private boolean cloneDashing = false;

    // Timing constants
    private static final long IDLE_DURATION = GameConfig.NEW_BOSS_IDLE_DURATION_MS;   // 等大概兩次玩家的攻擊時間 (2秒)
    private static final long SHIFT_DURATION = GameConfig.NEW_BOSS_SHIFT_DURATION_MS;   // Y軸位移準備時間 (0.5秒)
    private static final double CLONE_DELAY_SECS = GameConfig.NEW_BOSS_CLONE_DELAY_SECS; // 分身延遲衝刺時間 (0.4秒)

    // Death transition
    private boolean isDead = false;
    private long deathTime = 0;
    private static final long DEATH_DURATION = GameConfig.NEW_BOSS_DEATH_DURATION_MS; // 死亡變為 BIND 圖片，停留 1.5 秒

    // Positions
    private final double X_START = screenWidth - 150;
    private final double Y_GROUND = groundY - height;
    private final double Y_MID = groundY - height - 30;

    public NewBoss(Pane root, long activeGameTime, boolean isCoop) {
        // Hollow Knight: width=100 (拉寬), height=90, 根據單雙人設定 HP
        super(root, activeGameTime, isCoop, 100, 90, isCoop ? GameConfig.NEW_BOSS_HP_COOP : GameConfig.NEW_BOSS_HP);

        // Flip boss visual horizontally (橫向反轉)
        visual.setScaleX(-1);

        // Initialize Clone Group
        cloneVisual = new ImageView();
        cloneVisual.setSmooth(false);
        cloneVisual.setFitWidth(width);
        cloneVisual.setFitHeight(height);
        cloneVisual.setPreserveRatio(false);
        // Flip clone visual horizontally (橫向反轉)
        cloneVisual.setScaleX(-1);

        cloneHitBox = new Rectangle(width, height);
        cloneHitBox.setVisible(false);

        cloneGroup = new Group(cloneVisual, cloneHitBox);

        // Initial setup
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

    private void updateVisualImage() {
        if (isDead) {
            visual.setImage(ResourceManager.getImage("knight_bind.png"));
            return;
        }

        boolean isStage2 = hp <= 60;
        if (currentState == State.DASH) {
            String imgName = isStage2 ? "knight2_stand.png" : "knight_stand.png";
            visual.setImage(ResourceManager.getImage(imgName));
        } else {
            String imgName = isStage2 ? "knight2_run.png" : "knight_run.png";
            visual.setImage(ResourceManager.getImage(imgName));
        }
    }

    @Override
    protected void updateBoss(double speed, long activeGameTime, double dtSeconds) {
        long now = activeGameTime;

        if (currentState == State.DEATH) {
            updateVisualImage();
            return;
        }

        // State Machine
        switch (currentState) {
            case IDLE:
                x = X_START;
                y = Y_GROUND;
                group.setVisible(true);
                updateVisualImage();

                if (now - stateTimer > IDLE_DURATION) {
                    currentState = State.Y_SHIFT;
                    stateTimer = now;
                    startY = y;

                    // Choose target Y (Ground or Mid)
                    boolean targetMid = Math.random() < 0.5;
                    targetY = targetMid ? Y_MID : Y_GROUND;

                    // Stage 2 Clone Setup
                    if (hp <= 60) {
                        cloneActive = true;
                        cloneDashing = false;
                        cloneTargetY = targetMid ? Y_GROUND : Y_MID;
                        cloneX = X_START;
                        cloneY = cloneTargetY;
                        cloneGroup.setLayoutX(cloneX);
                        cloneGroup.setLayoutY(cloneY);
                        cloneVisual.setImage(ResourceManager.getImage("knight2_run.png"));

                        if (!root.getChildren().contains(cloneGroup)) {
                            // Add clone to screen under UI layers
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
                // Boss Dashing (Always dashes to the left edge)
                x -= dashSpeed * dtSeconds;
                group.setLayoutX(x);
                updateVisualImage();

                boolean bossDone = (x < -150);

                // Clone Dashing (Stage 2)
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

                // If both Boss and Clone are done, reset to IDLE
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

    private void removeClone() {
        if (cloneActive) {
            cloneActive = false;
            cloneDashing = false;
            root.getChildren().remove(cloneGroup);
        }
    }

    @Override
    public boolean checkCollision(Bounds dinoBounds) {
        if (currentState == State.DEATH) {
            return false; // Dead boss doesn't hurt player
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

    @Override
    public boolean isDefeated(long activeGameTime) {
        long survivalTimeMs = isCoop ? GameConfig.BOSS_SURVIVAL_TIME_MS_COOP : GameConfig.BOSS_SURVIVAL_TIME_MS;
        if (activeGameTime - startTime >= survivalTimeMs) {
            removeClone();
            return true;
        }

        if (hp <= 0) {
            if (!isDead) {
                isDead = true;
                deathTime = activeGameTime;
                currentState = State.DEATH;
                removeClone();
                // Clear any projectiles
                for (BossProjectile p : projectiles) {
                    root.getChildren().remove(p.getView());
                }
                projectiles.clear();
                group.setVisible(true); // Make sure the boss is visible to show BIND image
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
