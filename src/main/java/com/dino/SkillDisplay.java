package com.dino;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class SkillDisplay {

    private HBox root;
    private Label appleCountLabel;
    private Label milkCountLabel;
    private Label bookCountLabel;
    private Label barrierCountLabel;
    private Label swordCountLabel;

    public SkillDisplay() {
        root = new HBox(10);
        root.setAlignment(Pos.CENTER);
        
        // 置於畫面底部中央
        root.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - 145); 
        root.setLayoutY(GameConfig.SCREEN_HEIGHT - 70);

        appleCountLabel = new Label("0");
        milkCountLabel = new Label("0");
        bookCountLabel = new Label("0");
        barrierCountLabel = new Label("0");
        swordCountLabel = new Label("0");

        root.getChildren().addAll(
            createSlot("tool/golden_apple.png", "Q", appleCountLabel),
            createSlot("tool/milk_bucket.png", "W", milkCountLabel),
            createSlot("tool/enchanted_book.png", "E", bookCountLabel),
            createSlot("tool/barrier.png", "R", barrierCountLabel),
            createSlot("tool/wooden_sword.png", "F", swordCountLabel)
        );
        
        update();
    }

    private StackPane createSlot(String imagePath, String key, Label countLabel) {
        StackPane slot = new StackPane();
        slot.setPrefSize(54, 54);
        slot.setMinSize(54, 54);
        slot.setMaxSize(54, 54);
        slot.setStyle("-fx-background-color: #8b8b8b; -fx-border-color: #373737 #ffffff #ffffff #373737; -fx-border-width: 4; -fx-padding: 5;");

        Image img = ResourceManager.getImage(imagePath);
        ImageView imgView = new ImageView(img);
        imgView.setSmooth(false);
        imgView.setFitWidth(32);
        imgView.setFitHeight(32);
        StackPane.setAlignment(imgView, Pos.CENTER);

        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 0 2;");
        StackPane.setAlignment(keyLabel, Pos.TOP_LEFT);

        countLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 0 2;");
        StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);

        slot.getChildren().addAll(imgView, keyLabel, countLabel);
        return slot;
    }

    public void update() {
        appleCountLabel.setText(String.valueOf(GameConfig.goldenAppleCount));
        milkCountLabel.setText(String.valueOf(GameConfig.milkBucketCount));
        bookCountLabel.setText(String.valueOf(GameConfig.enchantedBookCount));
        barrierCountLabel.setText(String.valueOf(GameConfig.barrierCount));
        swordCountLabel.setText(String.valueOf(GameConfig.woodenSwordCount));
    }

    public HBox getView() {
        return root;
    }
}
