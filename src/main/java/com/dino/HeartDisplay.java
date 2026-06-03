package com.dino;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HeartDisplay {

    private Group root;

    private Image fullHeartImage;
    private Image emptyHeartImage;

    private List<ImageView> hearts;
    private final double heartSize = 24;

    public HeartDisplay() {
        this(3 + SaveManager.getLivesBonus());
    }

    public HeartDisplay(int maxLives) {
        root = new Group();

        fullHeartImage = ResourceManager.getImage("heart_full.png");
        emptyHeartImage = ResourceManager.getImage("heart_empty.png");
        
        hearts = new ArrayList<>();

        for (int i = 0; i < maxLives; i++) {
            ImageView heart = createHeart(30 + i * 30, 25);
            hearts.add(heart);
            root.getChildren().add(heart);
        }

        update(maxLives);
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
        for (int i = 0; i < hearts.size(); i++) {
            ImageView heart = hearts.get(i);
            if (i < lives) {
                heart.setImage(fullHeartImage);
            } else {
                heart.setImage(emptyHeartImage);
            }
        }
    }

    public Group getView() {
        return root;
    }
}