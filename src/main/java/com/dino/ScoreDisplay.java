package com.dino;

import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * 遊戲分數與最高分顯示 UI 類別。
 * 負責在畫面上繪製當前得分與最高得分。
 * 使用像素數字圖片（score/0.png ~ 9.png, H.png, I.png）以水平拼接方式動態繪製，
 * 並在特定事件發生時（如突破新高或遊戲結束）支援當前分數的閃爍提示效果。
 */
public class ScoreDisplay {

    private HBox root;                          // 分數顯示的總水平容器
    private HBox currentScoreBox;               // 存放當前得分數字圖片的水平容器
    private HashMap<Character, Image> imageMap; // 快取所有數字與字母圖示的映射表

    private final double digitWidth = 12;       // 像素數字的繪製寬度
    private final double digitHeight = 14;      // 像素數字的繪製高度
    private final boolean showHighScore;        // 是否顯示最高分

    private boolean flashing = false;           // 標記當前是否正在執行分數閃爍動畫
    private int flashingScore = 0;              // 閃爍動畫播放時，暫時鎖定並顯示的分數數值

    /**
     * 預設建構子，預設會顯示最高分數 (HI) 標誌。
     */
    public ScoreDisplay() {
        this(true);
    }

    /**
     * 建構子：初始化分數顯示元件的佈局結構、螢幕位置，並載入所有圖片資源。
     * @param showHighScore 是否需要顯示最高分數標誌
     */
    public ScoreDisplay(boolean showHighScore) {
        this.showHighScore = showHighScore;

        root = new HBox(4);
        root.setAlignment(Pos.CENTER_RIGHT);

        // 將分數顯示面板固定於畫面的右上角
        root.setLayoutX(725);
        root.setLayoutY(30);

        currentScoreBox = new HBox(4);

        imageMap = new HashMap<>();
        loadImages();

        // 預設初始化為 0 分
        update(0, 0);
    }

    /**
     * 載入數字 '0' ~ '9' 以及高分標誌字母 'H'、'I' 的像素圖檔至快取。
     */
    private void loadImages() {
        // 改用 ResourceManager 讀取圖片，注意相對路徑包含資料夾
        imageMap.put('0', ResourceManager.getImage("score/0.png"));
        imageMap.put('1', ResourceManager.getImage("score/1.png"));
        imageMap.put('2', ResourceManager.getImage("score/2.png"));
        imageMap.put('3', ResourceManager.getImage("score/3.png"));
        imageMap.put('4', ResourceManager.getImage("score/4.png"));
        imageMap.put('5', ResourceManager.getImage("score/5.png"));
        imageMap.put('6', ResourceManager.getImage("score/6.png"));
        imageMap.put('7', ResourceManager.getImage("score/7.png"));
        imageMap.put('8', ResourceManager.getImage("score/8.png"));
        imageMap.put('9', ResourceManager.getImage("score/9.png"));
        imageMap.put('H', ResourceManager.getImage("score/H.png"));
        imageMap.put('I', ResourceManager.getImage("score/I.png"));
    }

    /**
     * 更新當前分數與最高分數的顯示。
     * 會重新清除舊的 UI 節點，並重新拼接高分前綴（如 "HI 00120"）與當前分數（如 "00052"）。
     * @param score 當前遊戲得分
     * @param highScore 本地最高紀錄分數
     */
    public void update(int score, int highScore) {
        root.getChildren().clear();
        currentScoreBox.getChildren().clear();

        int scoreToShow;
        // 如果正在播放閃爍動畫，則鎖定顯示閃爍時的分數，否則顯示當前即時分數
        if (flashing) {
            scoreToShow = flashingScore;
        } else {
            scoreToShow = score;
        }

        String currentScoreText = formatScore(scoreToShow);

        // 如果需要顯示最高分數
        if (showHighScore) {
            String highScoreText = "HI " + formatScore(highScore);

            // 逐字元繪製最高分數
            for (int i = 0; i < highScoreText.length(); i++) {
                char c = highScoreText.charAt(i);

                if (c == ' ') {
                    root.getChildren().add(createSpace());
                } else {
                    root.getChildren().add(createImage(c));
                }
            }

            // 在最高分數與當前分數之間插入間隔
            root.getChildren().add(createSpace());
        }

        // 逐字元繪製當前得分並加入 currentScoreBox
        for (int i = 0; i < currentScoreText.length(); i++) {
            char c = currentScoreText.charAt(i);
            currentScoreBox.getChildren().add(createImage(c));
        }

        // 將當前分數容器加入至總顯示容器中
        root.getChildren().add(currentScoreBox);
    }

    /**
     * 將整數分數格式化為固定的 5 位數寬度字串（例如將 8 格式化為 "00008"）。
     * @param score 原始分數數值
     * @return 格式化後的 5 位數寬度字串
     */
    private String formatScore(int score) {
        return String.format("%05d", score);
    }

    /**
     * 根據給定字元建立對應的像素 Image ImageView，並設定非平滑縮放以保持像素美術風格。
     * @param c 要建立的字元（'0'~'9', 'H', 'I'）
     * @return 包裝了對應像素圖片的 ImageView 節點
     */
    private ImageView createImage(char c) {
        Image image = imageMap.get(c);
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(false); // 禁用平滑縮放以防鋸齒模糊，維持復古像素風
        imageView.setFitWidth(digitWidth);
        imageView.setFitHeight(digitHeight);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    /**
     * 建立固定寬度 12 像素的空白區域，做為分隔符號。
     * @return 表示間隙空間的 Region 節點
     */
    private Region createSpace() {
        Region space = new Region();
        space.setPrefWidth(12);
        return space;
    }

    /**
     * 觸發當前分數的閃爍動畫。
     * 使用 JavaFX Timeline 在 850 毫秒內動態切換 Opacity 不透明度，使分數來回閃爍三次，
     * 用於提示玩家取得成就、得分或在遊戲結束時進行視覺強調。
     * @param score 觸發閃爍時需要鎖定顯示的分數值
     */
    public void flashCurrentScore(int score) {
        flashing = true;
        flashingScore = score;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> currentScoreBox.setOpacity(0.2)),
                new KeyFrame(Duration.millis(150), e -> currentScoreBox.setOpacity(1.0)),
                new KeyFrame(Duration.millis(300), e -> currentScoreBox.setOpacity(0.2)),
                new KeyFrame(Duration.millis(450), e -> currentScoreBox.setOpacity(1.0)),
                new KeyFrame(Duration.millis(600), e -> currentScoreBox.setOpacity(0.2)),
                new KeyFrame(Duration.millis(750), e -> currentScoreBox.setOpacity(1.0)),
                new KeyFrame(Duration.millis(850), e -> {
                    flashing = false;
                    currentScoreBox.setOpacity(1.0); // 動態結束，恢復完全不透明度
                }));

        timeline.play();
    }

    /**
     * 取得此分數顯示元件的總 HBox 根節點。
     * @return 包含整個分數繪製的 HBox 元件
     */
    public HBox getView() {
        return root;
    }
}

