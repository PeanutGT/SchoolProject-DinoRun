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

/**
 * 收集金幣計數器 UI 顯示類別。
 * 以 HBox 包裝。在畫面右上角（鄰近分數顯示處）繪製金幣圖示、乘號字樣「x」、
 * 以及以像素數字圖片（score/0.png ~ 9.png）動態拼接的硬幣統計數值。
 */
public class CoinDisplay {
    private HBox root;                  // 總水平容器
    private HBox digitsBox;             // 存放數字圖片的容器
    private HashMap<Character, Image> imageMap; // 像素數字圖檔快取

    private final double digitWidth = 12;  // 像素數字圖片固定寬度
    private final double digitHeight = 14; // 像素數字圖片固定高度

    /**
     * 建構子：初始化容器，擺放在指定座標（X=590, Y=30），優先載入金幣圖片，或以黃金向量圓形回退。
     */
    public CoinDisplay() {
        root = new HBox(6);
        root.setAlignment(Pos.CENTER_LEFT);
        
        // 座標放置於 X=590, Y=30
        root.setLayoutX(590);
        root.setLayoutY(30);

        // 1. 載入硬幣圖示 (有 coin.png 就用圖片，沒有就用向量圓)
        javafx.scene.Node coinIconNode = null;
        try {
            String path = "/com/dino/assets/coin.png";
            if (ResourceManager.class.getResource(path) != null) {
                Image coinImage = ResourceManager.getImage("coin.png");
                if (coinImage != null && !coinImage.isError()) {
                    ImageView imgView = new ImageView(coinImage);
                    imgView.setSmooth(false);
                    imgView.setFitWidth(24);
                    imgView.setFitHeight(24);
                    coinIconNode = imgView;
                }
            }
        } catch (Exception e) {
            // 忽略，採用向量圓
        }

        if (coinIconNode == null) {
            Circle coinIcon = new Circle(12);
            RadialGradient goldGrad = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFF59D")),
                new Stop(1, Color.web("#FBC02D"))
            );
            coinIcon.setFill(goldGrad);
            coinIcon.setStroke(Color.web("#F57F17"));
            coinIcon.setStrokeWidth(2.25);
            coinIconNode = coinIcon;
        }

        // 2. 載入 0-9 像素數字圖片，快取在 imageMap 中
        imageMap = new HashMap<>();
        for (char c = '0'; c <= '9'; c++) {
            imageMap.put(c, ResourceManager.getImage("score/" + c + ".png"));
        }

        // 3. 乘號「x」文字標籤
        Label multiplyLabel = new Label("x");
        multiplyLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        multiplyLabel.setTextFill(Color.web("#5d4037"));

        digitsBox = new HBox(2);
        digitsBox.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(coinIconNode, multiplyLabel, digitsBox);
        
        // 預設更新為 0
        update(0);
    }

    /**
     * 當金幣統計變更時更新顯示。
     * 將數值轉成字串，逐一讀取字元圖檔並以水平拼接。
     * @param coins 目前吃到的金幣數
     */
    public void update(int coins) {
        digitsBox.getChildren().clear();
        String coinsText = String.valueOf(coins);
        for (int i = 0; i < coinsText.length(); i++) {
            char c = coinsText.charAt(i);
            digitsBox.getChildren().add(createDigitImage(c));
        }
    }

    /**
     * 建立特定數字字元的像素 ImageView 節點。
     */
    private ImageView createDigitImage(char c) {
        Image image = imageMap.get(c);
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(false);
        imageView.setFitWidth(digitWidth);
        imageView.setFitHeight(digitHeight);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    /**
     * 取得 HBox 視圖根節點。
     */
    public HBox getView() {
        return root;
    }
}
