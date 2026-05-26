package com.dino;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SkillDisplay {

    private HBox root;
    private Label appleCountLabel;

    public SkillDisplay() {
        root = new HBox(10);
        root.setAlignment(Pos.CENTER);
        
        // 置於畫面底部中央
        root.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - 25); 
        root.setLayoutY(GameConfig.SCREEN_HEIGHT - 80);

        appleCountLabel = new Label("0");

        root.getChildren().addAll(
            createSlot("tool/golden_apple.png", "Q", appleCountLabel)
        );
        
        update();
    }

    private VBox createSlot(String imagePath, String key, Label countLabel) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);

        Label keyLabel = new Label(key);
        keyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        keyLabel.setTextFill(Color.DARKGRAY);

        Image img = ResourceManager.getImage(imagePath);
        ImageView imgView = new ImageView(img);
        imgView.setSmooth(false);
        imgView.setFitWidth(32);
        imgView.setFitHeight(32);

        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        countLabel.setTextFill(Color.BLACK);

        box.getChildren().addAll(keyLabel, imgView, countLabel);
        return box;
    }

    public void update() {
        appleCountLabel.setText(String.valueOf(GameConfig.goldenAppleCount));
    }

    public HBox getView() {
        return root;
    }
}
