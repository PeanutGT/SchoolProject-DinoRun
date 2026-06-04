package com.dino;

import javafx.geometry.Bounds;

/**
 * 障礙物插槽類別。
 * 屬於「物件池」的預載包裝器。每個插槽內同時封裝了一個仙人掌（Cactus）與一隻飛鳥（Bird）。
 * 藉由切換 Type (CACTUS / BIRD) 來動態決定目前是顯示仙人掌還是顯示飛鳥，
 * 避免了頻繁在遊戲中實例化 (new) 物件，從而有效優化 GC 與畫面流暢度。
 */
public class ObstacleSlot {

    // 障礙物類型列舉
    public enum Type {
        CACTUS,
        BIRD
    }

    private Obstacle cactus;     // 封裝的仙人掌實體
    private Bird bird;           // 封裝的飛鳥實體
    private Type type;           // 目前生效顯示的障礙物類型

    private double x;            // 當前插槽的 X 座標

    /**
     * 建構子：預先建立仙人掌與小鳥，預設啟用仙人掌狀態。
     * @param x 初始 X
     * @param groundY 地面高度
     */
    public ObstacleSlot(double x, double groundY) {
        this.x = x;

        cactus = new Obstacle(x, groundY, 5);
        bird = new Bird(x, groundY - 130);

        type = Type.CACTUS;

        cactus.setVisible(true);
        bird.setVisible(false);
    }

    /**
     * 獲取當前生效顯示的障礙物碰撞邊界。
     */
    public Bounds getHitBoxBounds() {
        if (type == Type.CACTUS) {
            return cactus.getHitBox().localToScene(
                    cactus.getHitBox().getBoundsInLocal()
            );
        } else {
            return bird.getHitBox().localToScene(
                    bird.getHitBox().getBoundsInLocal()
            );
        }
    }

    /**
     * 隨影格移動生效中的障礙物位置，並同步更新插槽的 X 軸座標。
     */
    public void update(double speed, double dtSeconds) {
        if (type == Type.CACTUS) {
            cactus.update(speed, dtSeconds);
            x = cactus.getX();
        } else {
            bird.update(speed, dtSeconds);
            x = bird.getX();
        }
    }

    /**
     * 重設插槽位置，並依據目前遊戲分數重新隨機指派為仙人掌或小鳥。
     * @param newX 新的 X 座標
     * @param score 當前分數
     * @param groundY 跑道地面高度
     */
    public void reset(double newX, int score, double groundY) {
        x = newX;

        // 分數未達門檻 (BIRD_APPEAR_SCORE = 300) 時，小鳥尚未解鎖出現，只生成仙人掌
        if (score < GameConfig.BIRD_APPEAR_SCORE) {
            setCactus(newX);
            return;
        }

        // 達到門檻後，有 BIRD_SPAWN_PROBABILITY (0.35) 的機率生成為小鳥
        if (Math.random() < GameConfig.BIRD_SPAWN_PROBABILITY) {
            setBird(newX, randomBirdY(groundY));
        } else {
            setCactus(newX);
        }
    }

    /**
     * 將插槽狀態切換為仙人掌，顯示仙人掌並隱藏飛鳥。
     */
    private void setCactus(double newX) {
        type = Type.CACTUS;

        cactus.setVisible(true);
        bird.setVisible(false);

        cactus.reset(newX);
    }

    /**
     * 將插槽狀態切換為飛鳥，顯示飛鳥並隱藏仙人掌。
     */
    private void setBird(double newX, double y) {
        type = Type.BIRD;

        cactus.setVisible(false);
        bird.setVisible(true);

        bird.reset(newX, y);
    }

    /**
     * 隨機指派小鳥飛行的 Y 高度（高中低三種高度），設計相應的躲避方式。
     * @param groundY 地面 Y 高度
     * @return 隨機推算出的飛鳥 Y 座標值
     */
    private double randomBirdY(double groundY) {
        double r = Math.random();

        if (r < 0.4) {
            // 1. 高飛鳥：飛得很高，玩家可以直接站立在下方走過，起跳會撞到
            return groundY - 90;
        } else if (r < 0.8) {
            // 2. 中飛鳥：中等高度，玩家可以趴下蹲過，也可以大跳躍跳過
            return groundY - 60;
        } else {
            // 3. 貼地低飛鳥：飛得很低，必須大跳躍才能跳過
            return groundY - 35;
        }
    }

    public double getX() {
        return x;
    }

    /**
     * 取得目前生效障礙物的寬度。
     */
    public double getWidth() {
        if (type == Type.CACTUS) {
            return cactus.getWidth();
        } else {
            return 80;
        }
    }

    public Obstacle getCactus() {
        return cactus;
    }

    public Bird getBird() {
        return bird;
    }

    public Type getType() {
        return type;
    }
}