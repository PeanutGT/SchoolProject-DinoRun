package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HeartDisplay {

    private Group root;

    private Image fullHeartImage;
    private Image emptyHeartImage;

    private ImageView heart1;
    private ImageView heart2;
    private ImageView heart3;

    private final double heartSize = 24;

    public HeartDisplay() {
        root = new Group();

        fullHeartImage = ResourceManager.getImage("heart_full.png");
        emptyHeartImage = ResourceManager.getImage("heart_empty.png");

        heart1 = createHeart(30, 25);
        heart2 = createHeart(60, 25);
        heart3 = createHeart(90, 25);

        root.getChildren().addAll(heart1, heart2, heart3);

        update(3);
    }

    private ImageView createHeart(double x, double y) {
        ImageView heart = new ImageView(fullHeartImage);
        heart.setSmooth(false);

        heart.setFitWidth(heartSize);
        heart.setFitHeight(heartSize);
        heart.setPreserveRatio(true);

        heart.setX(x);
        heart.setY(y);

        return heart;
    }

    public void update(int lives) {
        if (lives >= 1) {
            heart1.setImage(fullHeartImage);
        } else {
            heart1.setImage(emptyHeartImage);
        }

        if (lives >= 2) {
            heart2.setImage(fullHeartImage);
        } else {
            heart2.setImage(emptyHeartImage);
        }

        if (lives >= 3) {
            heart3.setImage(fullHeartImage);
        } else {
            heart3.setImage(emptyHeartImage);
        }
    }

    public Group getView() {
        return root;
    }
}