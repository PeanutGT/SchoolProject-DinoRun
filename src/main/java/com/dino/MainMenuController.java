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
    }

    public void setDinoMain(DinoMain dinoMain) {
        this.dinoMain = dinoMain;
    }

    @FXML
    private void startGame() {
        if (dinoMain != null) {
            stopBackgroundAnimation();
            dinoMain.startSinglePlayerGame();
        }
    }

    @FXML
    private void startCoopGame() {
        if (dinoMain != null) {
            dinoMain.startCoopGame();
        }
    }

    @FXML
    private void startVersusGame() {
        if (dinoMain != null) {
            stopBackgroundAnimation();
            dinoMain.startVersusGame();
        }
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
    private void openShop() {
        stopBackgroundAnimation();
        menuContent.getChildren().clear();

        Label label = new Label("商店");
        label.setFont(Font.font(24));

        Label balanceLabel = new Label("目前金錢: " + SaveManager.getMoney() + "$");
        balanceLabel.setFont(Font.font(18));

        VBox shopBox = new VBox(12);
        shopBox.setAlignment(Pos.CENTER);

        // Placeholder item - user will provide images later
        Button buyHealth = new Button("購買：Max HP +1 (成本 5$)");
        buyHealth.setOnAction(e -> {
            int money = SaveManager.getMoney();
            if (money >= 5) {
                SaveManager.addMoney(-5);
                balanceLabel.setText("目前金錢: " + SaveManager.getMoney() + "$");
                // Apply effect later; for now just acknowledge purchase
            }
        });

        Button backButton = new Button("返回主選單");
        backButton.setOnAction(e -> {
            showMainButtons();
            startBackgroundAnimation();
        });

        shopBox.getChildren().addAll(balanceLabel, buyHealth, backButton);

        menuContent.getChildren().addAll(label, shopBox);
    }

    @FXML
    private void exitGame() {
        Platform.exit();
    }

    private void showMainButtons() {
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
