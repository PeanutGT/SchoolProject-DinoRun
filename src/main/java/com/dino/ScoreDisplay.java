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

public class ScoreDisplay {

    private HBox root;
    private HBox currentScoreBox;
    private HashMap<Character, Image> imageMap;

    private final double digitWidth = 12;
    private final double digitHeight = 14;
    private final boolean showHighScore;

    private boolean flashing = false;
    private int flashingScore = 0;

    public ScoreDisplay() {
        this(true);
    }

    public ScoreDisplay(boolean showHighScore) {
        this.showHighScore = showHighScore;

        root = new HBox(4);
        root.setAlignment(Pos.CENTER_RIGHT);

        root.setLayoutX(725);
        root.setLayoutY(30);

        currentScoreBox = new HBox(4);

        imageMap = new HashMap<>();
        loadImages();

        update(0, 0);
    }
    
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

    public void update(int score, int highScore) {
        root.getChildren().clear();
        currentScoreBox.getChildren().clear();

        int scoreToShow;
        if (flashing) {
            scoreToShow = flashingScore;
        } else {
            scoreToShow = score;
        }

        String currentScoreText = formatScore(scoreToShow);

        if (showHighScore) {
            String highScoreText = "HI " + formatScore(highScore);

            for (int i = 0; i < highScoreText.length(); i++) {
                char c = highScoreText.charAt(i);

                if (c == ' ') {
                    root.getChildren().add(createSpace());
                } else {
                    root.getChildren().add(createImage(c));
                }
            }

            root.getChildren().add(createSpace());
        }

        for (int i = 0; i < currentScoreText.length(); i++) {
            char c = currentScoreText.charAt(i);
            currentScoreBox.getChildren().add(createImage(c));
        }

        root.getChildren().add(currentScoreBox);
    }

    private String formatScore(int score) {
        return String.format("%05d", score);
    }

    private ImageView createImage(char c) {
        Image image = imageMap.get(c);
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(false);
        imageView.setFitWidth(digitWidth);
        imageView.setFitHeight(digitHeight);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private Region createSpace() {
        Region space = new Region();
        space.setPrefWidth(12);
        return space;
    }

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
                    currentScoreBox.setOpacity(1.0);
                })
        );

        timeline.play();
    }

    public HBox getView() {
        return root;
    }
}
