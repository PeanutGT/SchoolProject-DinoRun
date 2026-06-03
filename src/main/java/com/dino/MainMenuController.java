package com.dino;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class MainMenuController {

    @FXML
    private StackPane rootBox;

    @FXML
    private Pane backgroundPane;

    @FXML
    private VBox menuContent;

    @FXML
    private VBox mainContent;

    private DinoMain dinoMain;
    private final List<ImageView> clouds = new ArrayList<>();
    private final List<ImageView> groundImages = new ArrayList<>();
    private final List<Obstacle> cactuses = new ArrayList<>();
    private final List<Bird> birds = new ArrayList<>();
    private AnimationTimer backgroundTimer;
    private double backgroundSpeed = GameConfig.INITIAL_SPEED;

    @FXML
    private void initialize() {
        backgroundPane.setMaxSize(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        backgroundPane.setClip(new Rectangle(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        menuContent.setMaxSize(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        createAnimatedBackground();
        startBackgroundAnimation();
        SoundManager.playMenuBgm();
    }

    public void setDinoMain(DinoMain dinoMain) {
        this.dinoMain = dinoMain;
    }

    @FXML
    private void startGame() {
        if (dinoMain != null) {
            SoundManager.stopMenuBgm();
            stopBackgroundAnimation();
            dinoMain.startSinglePlayerGame();
        }
    }

    @FXML
    private void startCoopGame() {
        if (dinoMain != null) {
            SoundManager.stopMenuBgm();
            dinoMain.startCoopGame();
        }
    }

    @FXML
    private void startVersusGame() {
        if (dinoMain != null) {
            SoundManager.stopMenuBgm();
            stopBackgroundAnimation();
            dinoMain.startVersusGame();
        }
    }

    @FXML
    private void showLeaderboard() {
        VBox leaderboardPanel = new VBox(15);
        leaderboardPanel.setAlignment(Pos.CENTER);
        leaderboardPanel.setMaxSize(500, 480);
        leaderboardPanel.setStyle("-fx-background-color: #3e2723; -fx-border-color: #d7ccc8; -fx-border-width: 4; -fx-border-style: solid; -fx-padding: 15;");

        Label title = new Label("★ TOP DINOS ★");
        title.setStyle("-fx-text-fill: #ffd54f; -fx-font-family: 'Courier New'; -fx-font-size: 28; -fx-font-weight: bold;");

        List<LeaderboardManager.ScoreEntry> scores = LeaderboardManager.loadTopScores();
        if (scores.isEmpty()) {
            Label emptyLabel = new Label("目前還沒有紀錄喔！");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 16;");
            leaderboardPanel.getChildren().addAll(title, emptyLabel);
        } else {
            // 頒獎台區塊 (Top 3)
            HBox podiumBox = new HBox(15);
            podiumBox.setAlignment(Pos.BOTTOM_CENTER);
            
            VBox firstPlace = createPodiumSpot(scores, 0, "#ffca28", "冠軍", 100);
            VBox secondPlace = createPodiumSpot(scores, 1, "#e0e0e0", "亞軍", 80);
            VBox thirdPlace = createPodiumSpot(scores, 2, "#bcaaa4", "季軍", 60);
            
            if (secondPlace != null) podiumBox.getChildren().add(secondPlace);
            if (firstPlace != null) podiumBox.getChildren().add(firstPlace);
            if (thirdPlace != null) podiumBox.getChildren().add(thirdPlace);
            
            leaderboardPanel.getChildren().addAll(title, podiumBox);

            // 滾動列表區塊 (Top 4-50)
            if (scores.size() > 3) {
                VBox listVBox = new VBox(8);
                listVBox.setAlignment(Pos.TOP_CENTER);
                listVBox.setStyle("-fx-background-color: transparent;");
                for (int i = 3; i < scores.size(); i++) {
                    LeaderboardManager.ScoreEntry entry = scores.get(i);
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    
                    ImageView imgView = new ImageView(getCharacterImage(entry.characterType));
                    imgView.setFitWidth(24);
                    imgView.setPreserveRatio(true);
                    imgView.setSmooth(false);
                    
                    Label infoLabel = new Label(String.format("%2d. %-15s %6d", (i + 1), entry.name, entry.score));
                    infoLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 16; -fx-font-weight: bold;");
                    
                    row.getChildren().addAll(imgView, infoLabel);
                    listVBox.getChildren().add(row);
                }
                
                ScrollPane scrollPane = new ScrollPane(listVBox);
                scrollPane.setPrefViewportHeight(130);
                scrollPane.setMaxWidth(400);
                scrollPane.setStyle("-fx-background: #4e342e; -fx-background-color: transparent; -fx-padding: 10; -fx-border-color: #d7ccc8; -fx-border-width: 2;");
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 隱藏滾動條
                scrollPane.setFitToWidth(true);
                
                leaderboardPanel.getChildren().add(scrollPane);
            }
        }

        Button closeBtn = new Button("[ 回主選單 ]");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #5d4037; -fx-text-fill: #ffca28; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand;"));
        closeBtn.setOnAction(e -> rootBox.getChildren().remove(leaderboardPanel));

        leaderboardPanel.getChildren().add(closeBtn);
        rootBox.getChildren().add(leaderboardPanel);
    }
    
    private VBox createPodiumSpot(List<LeaderboardManager.ScoreEntry> scores, int index, String color, String titleText, double baseHeight) {
        if (index >= scores.size()) return null;
        LeaderboardManager.ScoreEntry entry = scores.get(index);
        
        VBox spot = new VBox(5);
        spot.setAlignment(Pos.BOTTOM_CENTER);
        
        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold;");
        
        ImageView imgView = new ImageView(getCharacterImage(entry.characterType));
        imgView.setFitWidth(index == 0 ? 48 : 36);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(false);
        
        Label nameLabel = new Label(entry.name);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 14; -fx-font-weight: bold;");
        
        Label scoreLabel = new Label(String.valueOf(entry.score));
        scoreLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New'; -fx-font-size: 16; -fx-font-weight: bold;");
        
        VBox base = new VBox();
        base.setAlignment(Pos.CENTER);
        base.setPrefSize(90, baseHeight);
        base.setStyle("-fx-background-color: #5d4037; -fx-border-color: #8d6e63; -fx-border-width: 2;");
        base.getChildren().addAll(nameLabel, scoreLabel);
        
        spot.getChildren().addAll(titleLabel, imgView, base);
        return spot;
    }

    private Image getCharacterImage(String characterId) {
        if (characterId == null) return ResourceManager.getImage("dino_run1.png");
        switch (characterId) {
            case "mario": return ResourceManager.getImage("mario_walk1.png");
            case "luigi": return ResourceManager.getImage("luigi_run1.png");
            case "kirby": return ResourceManager.getImage("kirby_run1.png");
            case "lucario": return ResourceManager.getImage("lucario_run1.png");
            case "sonic": return ResourceManager.getImage("sonic_run1.png");
            case "steve": return ResourceManager.getImage("steve_run1.png");
            case "dino":
            default: return ResourceManager.getImage("dino_run1.png");
        }
    }

    @FXML
    private void openShop() {
        menuContent.getChildren().clear();
        ShopPanel shopPanel = new ShopPanel(this);
        menuContent.getChildren().add(shopPanel);
    }

    @FXML
    private void openSettings() {
        menuContent.getChildren().clear();

        Label label = new Label("遊戲設定");
        label.setFont(Font.font(24));

        SettingsPanel settingsPanel = new SettingsPanel();

        Button backButton = new Button("返回主選單");
        backButton.setOnAction(e -> showMainButtons());

        menuContent.getChildren().addAll(label, settingsPanel, backButton);
    }

    @FXML
    private void exitGame() {
        Platform.exit();
    }

    void showMainButtons() {
        menuContent.getChildren().clear();
        menuContent.getChildren().add(mainContent);
    }

    private void createAnimatedBackground() {
        backgroundPane.getChildren().clear();
        clouds.clear();
        groundImages.clear();
        cactuses.clear();
        birds.clear();

        Image cloudImage = ResourceManager.getImage("cloud.png");
        addCloud(cloudImage, 250, 90, 80);
        addCloud(cloudImage, 500, 120, 70);
        addCloud(cloudImage, 720, 75, 90);

        Image groundImage = ResourceManager.getImage("ground.png");
        addGround(groundImage, 0);
        addGround(groundImage, GameConfig.SCREEN_WIDTH);

        addCactus(250);
        addCactus(500);
        addCactus(750);
        addBird(670, GameConfig.GROUND_Y - 60);
        addBird(880, GameConfig.GROUND_Y - 90);
    }

    private void addCloud(Image image, double x, double y, double width) {
        ImageView cloud = new ImageView(image);
        cloud.setSmooth(false);
        cloud.setPreserveRatio(true);
        cloud.setFitWidth(width);
        cloud.setX(x);
        cloud.setY(y);
        clouds.add(cloud);
        backgroundPane.getChildren().add(cloud);
    }

    private void addGround(Image image, double x) {
        ImageView ground = new ImageView(image);
        ground.setSmooth(false);
        ground.setPreserveRatio(true);
        ground.setFitWidth(GameConfig.SCREEN_WIDTH);
        ground.setX(x);
        ground.setY(GameConfig.GROUND_IMAGE_Y);
        groundImages.add(ground);
        backgroundPane.getChildren().add(ground);
    }

    private void addCactus(double x) {
        Obstacle cactus = new Obstacle(x, GameConfig.GROUND_Y, 5);
        cactuses.add(cactus);
        backgroundPane.getChildren().add(cactus.getView());
    }

    private void addBird(double x, double y) {
        Bird bird = new Bird(x, y);
        birds.add(bird);
        backgroundPane.getChildren().add(bird.getView());
    }

    private void startBackgroundAnimation() {
        backgroundTimer = new AnimationTimer() {
            private long lastNow = 0;
            @Override
            public void handle(long now) {
                if (lastNow == 0) {
                    lastNow = now;
                    return;
                }
                double dtSeconds = (now - lastNow) / 1_000_000_000.0;
                lastNow = now;
                updateBackground(dtSeconds);
            }
        };
        backgroundTimer.start();
    }

    private void updateBackground(double dtSeconds) {
        backgroundSpeed = Math.min(GameConfig.MAX_SPEED, backgroundSpeed + GameConfig.ACCELERATION * dtSeconds);

        for (ImageView cloud : clouds) {
            cloud.setX(cloud.getX() - backgroundSpeed * 0.25 * dtSeconds);
            if (cloud.getX() < -100) {
                cloud.setX(GameConfig.SCREEN_WIDTH + Math.random() * 240);
            }
        }
        
        if (groundImages.size() >= 2) {
            ImageView g1 = groundImages.get(0);
            ImageView g2 = groundImages.get(1);
            
            g1.setX(g1.getX() - backgroundSpeed * dtSeconds);
            g2.setX(g2.getX() - backgroundSpeed * dtSeconds);

            if (g1.getX() <= -GameConfig.SCREEN_WIDTH) {
                g1.setX(g2.getX() + GameConfig.SCREEN_WIDTH);
            }
            if (g2.getX() <= -GameConfig.SCREEN_WIDTH) {
                g2.setX(g1.getX() + GameConfig.SCREEN_WIDTH);
            }
        }

        for (Obstacle cactus : cactuses) {
            cactus.update(backgroundSpeed, dtSeconds);
            if (cactus.getX() < -cactus.getWidth()) {
                cactus.reset(GameConfig.SCREEN_WIDTH + Math.random() * 500);
            }
        }

        for (Bird bird : birds) {
            bird.update(backgroundSpeed, dtSeconds);
            if (bird.getX() < -50) {
                bird.reset(GameConfig.SCREEN_WIDTH + Math.random() * 500, GameConfig.GROUND_Y - 60 - Math.random() * 30);
            }
        }
    }

    private void stopBackgroundAnimation() {
        if (backgroundTimer != null) {
            backgroundTimer.stop();
        }
    }
}
