package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class QuestionBlock {

    private Group group;
    private ImageView imageView;
    private Rectangle hitBox;

    private double x;
    private double y;
    private final double width = 40;
    private final double height = 40;

    public QuestionBlock(double x, double groundY) {
        this.x = x;
        // 方塊浮在半空中
        this.y = groundY - 120;

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

    public void update(double speed, double dtSeconds) {
        x -= speed * dtSeconds;
        group.setLayoutX(x);
    }

    public boolean isOffScreen() {
        return x < -width;
    }

    public javafx.geometry.Bounds getHitBoxBounds() {
        return hitBox.localToScene(hitBox.getBoundsInLocal());
    }

    public Group getView() {
        return group;
    }
}
