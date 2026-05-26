package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class Bird {

    private Group group;
    private ImageView imageView;
    private Rectangle hitBox;

    private Image birdImage1;
    private Image birdImage2;

    private int animationCounter = 0;

    private final double width = 50;
    private final double height = 35;

    public Bird(double x, double y) {
        // 改用 ResourceManager 讀取圖片
        birdImage1 = ResourceManager.getImage("bird1.png");
        birdImage2 = ResourceManager.getImage("bird2.png");

        imageView = new ImageView(birdImage1);
        imageView.setSmooth(false);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        hitBox = new Rectangle(6, 6, width - 12, height - 12);
        hitBox.setVisible(false);

        group = new Group(imageView, hitBox);
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    public void update(double speed, double dtSeconds) {
        group.setLayoutX(group.getLayoutX() - speed * dtSeconds);
        updateAnimation();
    }

    public void flap() {
        updateAnimation();
    }

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

    public void reset(double x, double y) {
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    public double getX() {
        return group.getLayoutX();
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
