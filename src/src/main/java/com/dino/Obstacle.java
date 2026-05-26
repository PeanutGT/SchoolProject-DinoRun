package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class Obstacle {

    private Group group;
    private ImageView imageView;
    private Rectangle hitBox;

    private Image[] cactusImages;

    private double width;
    private double height;

    private double groundY;
    private double groundOffset;

    // 如果圖片太大或太小，可以調這個
    private final double imageScale = 1.2;

    public Obstacle(double x, double groundY, double groundOffset) {
        this.groundY = groundY;
        this.groundOffset = groundOffset;

        cactusImages = new Image[6];

        for (int i = 0; i < cactusImages.length; i++) {
            // 優化：改由 ResourceManager 取得快取圖片
            cactusImages[i] = ResourceManager.getImage("cactus" + (i + 1) + ".png");
        }

        imageView = new ImageView();
        imageView.setSmooth(false);
        hitBox = new Rectangle();
        hitBox.setVisible(false);

        group = new Group(imageView, hitBox);
        group.setLayoutX(x);

        randomImage();
    }

    public void update(double speed, double dtSeconds) {
        group.setLayoutX(group.getLayoutX() - speed * dtSeconds);
    }

    public void reset(double x) {
        group.setLayoutX(x);
        randomImage();
    }

    private void randomImage() {
        int index = (int)(Math.random() * cactusImages.length);
        Image selectedImage = cactusImages[index];

        imageView.setImage(selectedImage);

        width = selectedImage.getWidth() * imageScale;
        height = selectedImage.getHeight() * imageScale;

        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        // 讓仙人掌底部貼在地板上
        group.setLayoutY(groundY - height + groundOffset);

        // 碰撞框跟著圖片大小變，但稍微縮小一點
        hitBox.setX(4);
        hitBox.setY(4);
        hitBox.setWidth(width - 8);
        hitBox.setHeight(height - 8);
    }

    public double getX() {
        return group.getLayoutX();
    }

    public double getWidth() {
        return width;
    }

    public Group getView() {
        return group;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setVisible(boolean visible) {
        group.setVisible(visible);
    }

    public boolean isVisible() {
        return group.isVisible();
    }
}