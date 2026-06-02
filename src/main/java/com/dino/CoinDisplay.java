package com.dino;

import java.util.HashMap;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CoinDisplay {
    private HBox root;
    private HBox digitsBox;
    private HashMap<Character, Image> imageMap;
    
    private final double digitWidth = 12;
    private final double digitHeight = 14;
    
    public CoinDisplay() {
        root = new HBox(6);
        root.setAlignment(Pos.CENTER_LEFT);
        
        // 為了支援動態生命心形條（最高 6 顆心，寬度約 30 + 6 * 30 = 210px），
        // 將金幣計數器擺放在 X=230 的位置，非常和諧！
        root.setLayoutX(230);
        root.setLayoutY(24);

        // 1. 載入硬幣圖示 (有 coin.png 就用圖片，沒有就用精緻向量圖案)
        javafx.scene.Node coinIconNode = null;
        try {
            String path = "/com/dino/assets/coin.png";
            if (ResourceManager.class.getResource(path) != null) {
                Image coinImage = ResourceManager.getImage("coin.png");
                if (coinImage != null && !coinImage.isError()) {
                    ImageView imgView = new ImageView(coinImage);
                    imgView.setSmooth(false);
                    imgView.setFitWidth(16);
                    imgView.setFitHeight(16);
                    coinIconNode = imgView;
                }
            }
        } catch (Exception e) {
            // 忽略
        }

        if (coinIconNode == null) {
            Circle coinIcon = new Circle(8);
            RadialGradient goldGrad = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFF59D")),
                new Stop(1, Color.web("#FBC02D"))
            );
            coinIcon.setFill(goldGrad);
            coinIcon.setStroke(Color.web("#F57F17"));
            coinIcon.setStrokeWidth(1.5);
            coinIconNode = coinIcon;
        }

        // 2. 載入與分數系統完全一致的像素數字對照表
        imageMap = new HashMap<>();
        for (char c = '0'; c <= '9'; c++) {
            imageMap.put(c, ResourceManager.getImage("score/" + c + ".png"));
        }

        // 3. 乘號「x」使用與主選單一致的深褐色像素質感文字
        Label multiplyLabel = new Label("x");
        multiplyLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        multiplyLabel.setTextFill(Color.web("#5d4037"));

        // 放置像素數字圖片的容器
        digitsBox = new HBox(2);
        digitsBox.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(coinIconNode, multiplyLabel, digitsBox);
        
        // 預設更新為 0（在遊戲內會隨 sessionCoins 即時同步）
        update(0);
    }

    public void update(int coins) {
        digitsBox.getChildren().clear();
        String coinsText = String.valueOf(coins);
        for (int i = 0; i < coinsText.length(); i++) {
            char c = coinsText.charAt(i);
            digitsBox.getChildren().add(createDigitImage(c));
        }
    }

    private ImageView createDigitImage(char c) {
        Image image = imageMap.get(c);
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(false);
        imageView.setFitWidth(digitWidth);
        imageView.setFitHeight(digitHeight);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public HBox getView() {
        return root;
    }
}
