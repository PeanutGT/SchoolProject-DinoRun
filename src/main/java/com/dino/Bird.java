package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * 飛鳥障礙物類別。
 * 包含兩張飛行姿態圖片（翅膀向上/翅膀向下），並依幀數計數器進行切換播放，
 * 設定相應的飛鳥 Y 座標（高空/低空）以及碰撞矩形。
 */
public class Bird {

    private Group group;         // 鳥的 JavaFX 節點群組
    private ImageView imageView; // 圖片顯示器
    private Rectangle hitBox;    // 碰撞箱

    private Image birdImage1;    // 振翅圖片一（上）
    private Image birdImage2;    // 振翅圖片二（下）

    private int animationCounter = 0; // 動畫幀計數

    private final double width = 50;  // 飛鳥固定寬度
    private final double height = 35; // 飛鳥固定高度

    /**
     * 建構子：初始化位置與載入鳥的兩格振翅動畫圖片。
     * @param x 初始 X 軸座標
     * @param y 初始 Y 軸座標
     */
    public Bird(double x, double y) {
        // 使用 ResourceManager 快取載入 bird1.png 與 bird2.png
        birdImage1 = ResourceManager.getImage("bird1.png");
        birdImage2 = ResourceManager.getImage("bird2.png");

        imageView = new ImageView(birdImage1);
        imageView.setSmooth(false);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        // 碰撞箱尺寸：比圖片四周稍微縮小 6 像素，提高容錯
        hitBox = new Rectangle(6, 6, width - 12, height - 12);
        hitBox.setVisible(false);

        group = new Group(imageView, hitBox);
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    /**
     * 隨影格更新。位移位置並播放振翅動畫。
     */
    public void update(double speed, double dtSeconds) {
        group.setLayoutX(group.getLayoutX() - speed * dtSeconds);
        updateAnimation();
    }

    /**
     * 振翅方法（單獨更新一格動畫）。
     */
    public void flap() {
        updateAnimation();
    }

    /**
     * 以 10 影格為週期，輪流切換展示鳥的兩張圖片，產生展翅飛行的動畫效果。
     */
    private void updateAnimation() {
        animationCounter++;
        if (animationCounter % 10 == 0) {
            if (imageView.getImage() == birdImage1) {
                imageView.setImage(birdImage2);
            } else {
                imageView.setImage(birdImage1);
            }
        }
    }

    /**
     * 重設飛鳥位置。
     * @param x 新的 X 座標
     * @param y 新的 Y 座標
     */
    public void reset(double x, double y) {
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    /**
     * 取得飛鳥當前 X 座標。
     */
    public double getX() {
        return group.getLayoutX();
    }

    /**
     * 取得飛鳥視圖節點。
     */
    public Group getView() {
        return group;
    }

    /**
     * 取得飛鳥碰撞箱。
     */
    public Rectangle getHitBox() {
        return hitBox;
    }

    /**
     * 設定顯示或隱藏。
     */
    public void setVisible(boolean visible) {
        group.setVisible(visible);
    }

    /**
     * 取得當前顯示狀態。
     */
    public boolean isVisible() {
        return group.isVisible();
    }
}
