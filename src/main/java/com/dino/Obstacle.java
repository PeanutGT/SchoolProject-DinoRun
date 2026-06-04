package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * 仙人掌障礙物類別。
 * 負責從 6 種不同造型的仙人掌圖檔中隨機選擇一個、
 * 動態計算貼齊地面的高度、以及設定外包碰撞矩形以供檢測。
 */
public class Obstacle {

    private Group group;         // 包含仙人掌圖片與碰撞箱的組合節點
    private ImageView imageView; // 圖片視圖
    private Rectangle hitBox;    // 隱藏的碰撞矩形箱

    private Image[] cactusImages;// 存載 6 種不同仙人掌的圖片陣列

    private double width;        // 計算後的仙人掌寬度 (套用縮放比例後)
    private double height;       // 計算後的仙人掌高度 (套用縮放比例後)

    private double groundY;      // 跑道的地面垂直座標
    private double groundOffset; // 對齊地面的微調高度

    // 仙人掌的縮放乘數，可調整顯示大小
    private final double imageScale = 1.2;

    /**
     * 建構子：載入所有仙人掌圖片並定位於指定 X 座標。
     * @param x 初始 X 軸座標
     * @param groundY 跑道地面高度
     * @param groundOffset 對齊偏移值
     */
    public Obstacle(double x, double groundY, double groundOffset) {
        this.groundY = groundY;
        this.groundOffset = groundOffset;

        cactusImages = new Image[6];
        for (int i = 0; i < cactusImages.length; i++) {
            // 使用 ResourceManager 快取載入 cactus1.png ~ cactus6.png
            cactusImages[i] = ResourceManager.getImage("cactus" + (i + 1) + ".png");
        }

        imageView = new ImageView();
        imageView.setSmooth(false);
        hitBox = new Rectangle();
        hitBox.setVisible(false);

        group = new Group(imageView, hitBox);
        group.setLayoutX(x);

        // 隨機抽選一張仙人掌圖檔
        randomImage();
    }

    /**
     * 隨影格更新位置。根據目前速度水平向左位移。
     */
    public void update(double speed, double dtSeconds) {
        group.setLayoutX(group.getLayoutX() - speed * dtSeconds);
    }

    /**
     * 重設仙人掌的 X 座標並重新隨機抽選圖片。
     * @param x 新的 X 座標
     */
    public void reset(double x) {
        group.setLayoutX(x);
        randomImage();
    }

    /**
     * 隨機選用一張仙人掌圖片，並重新計算其寬高、貼齊地面坐標與更新碰撞箱大小。
     */
    private void randomImage() {
        int index = (int)(Math.random() * cactusImages.length);
        Image selectedImage = cactusImages[index];

        imageView.setImage(selectedImage);

        // 套用縮放乘數以確定實際繪製寬高
        width = selectedImage.getWidth() * imageScale;
        height = selectedImage.getHeight() * imageScale;

        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        // 確保仙人掌底部準確貼在跑道地面上
        group.setLayoutY(groundY - height + groundOffset);

        // 設定微幅向內收縮的碰撞箱（優化玩家的死角擦過碰撞判定）
        hitBox.setX(4);
        hitBox.setY(4);
        hitBox.setWidth(width - 8);
        hitBox.setHeight(height - 8);
    }

    /**
     * 取得仙人掌當前 X 軸座標。
     */
    public double getX() {
        return group.getLayoutX();
    }

    /**
     * 取得仙人掌當前寬度。
     */
    public double getWidth() {
        return width;
    }

    /**
     * 取得仙人掌組合視圖節點。
     */
    public Group getView() {
        return group;
    }

    /**
     * 取得碰撞箱。
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
     * 檢查當前是否可見。
     */
    public boolean isVisible() {
        return group.isVisible();
    }
}