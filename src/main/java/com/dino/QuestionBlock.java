package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * 馬利歐式問號方塊類別。
 * 負責渲染半空中的黃色問號方塊（item_block.png），
 * 當玩家起跳碰觸它時，會隨機掉落不同的背包技能道具。
 */
public class QuestionBlock {

    private Group group;         // JavaFX 群組節點
    private ImageView imageView; // 方塊圖片顯示器
    private Rectangle hitBox;    // 碰撞箱

    private double x;            // X 座標
    private double y;            // Y 座標
    private final double width = 40;  // 固定寬度
    private final double height = 40; // 固定高度

    /**
     * 建構子：初始化問號方塊座標。高度預設懸浮在半空中（地面減去 120 像素），方便跳躍頂撞。
     * @param x 初始 X 座標
     * @param groundY 跑道地面高度
     */
    public QuestionBlock(double x, double groundY) {
        this.x = x;
        // 方塊浮在半空中，設計成跳躍頂撞的感覺
        this.y = groundY - 120;

        // 使用 ResourceManager 載入 item_block.png 圖片
        Image blockImage = ResourceManager.getImage("item_block.png");
        imageView = new ImageView(blockImage);
        imageView.setSmooth(false);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        hitBox = new Rectangle(width, height);
        hitBox.setVisible(false);

        group = new Group(imageView, hitBox);
        group.setLayoutX(this.x);
        group.setLayoutY(this.y);
    }

    /**
     * 隨影格更新。配合遊戲背景捲動速度向左滾動位移。
     */
    public void update(double speed, double dtSeconds) {
        x -= speed * dtSeconds;
        group.setLayoutX(x);
    }

    /**
     * 判定方塊是否滾出螢幕左方以外。
     */
    public boolean isOffScreen() {
        return x < -width;
    }

    /**
     * 取得方塊碰撞箱場景邊界。
     */
    public javafx.geometry.Bounds getHitBoxBounds() {
        return hitBox.localToScene(hitBox.getBoundsInLocal());
    }

    /**
     * 取得方塊視圖。
     */
    public Group getView() {
        return group;
    }
}
