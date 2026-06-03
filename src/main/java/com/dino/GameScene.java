package com.dino;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.Group;

public class GameScene {

    private Pane root;
    private DinoMain dinoMain;

    private ImageView ground1;
    private ImageView ground2;

    private ImageView cloud1;
    private ImageView cloud2;
    private ImageView cloud3;

    private Dino dino;
    private String currentCharacter;

    private ArrayList<ObstacleSlot> obstacles;
    private Label signpost;

    private ScoreDisplay scoreDisplay;
    private static int sessionHighScore = 0;

    private int score = 0;
    private int lastScoreSound = 0;
    private int frameCount = 0;

    // 引入 GameConfig 常數
    private double speed = GameConfig.INITIAL_SPEED;

    private final double screenWidth = GameConfig.SCREEN_WIDTH;
    private final double groundY = GameConfig.GROUND_Y;
    private final double groundImageY = GameConfig.GROUND_IMAGE_Y;
    private final double groundWidth = GameConfig.SCREEN_WIDTH;

    private AnimationTimer timer;

    private ImageView gameOverImage;
    private ImageView restartImage;
    private Button gameOverMenuBtn;
    private boolean gameOver = false;

    private boolean bossPhase = false;
    private boolean bossIncoming = false;
    private boolean bossHasAppeared = false;
    private Boss boss = null;
    private Rectangle screenFlash;

    private boolean inBossGracePeriod = false;
    private long bossGracePeriodStartTime = 0;

    private StackPane pauseOverlay;
    private boolean isPaused = false;
    private boolean waitingToStart = true;

    private HeartDisplay heartDisplay;
    private SkillDisplay skillDisplay;
    private List<QuestionBlock> questionBlocks;
    private Rectangle milkFog;
    private int lastQuestionBlockScore = 0;
    private boolean barrierActive = false;
    private long barrierStartTime = 0;
    private long milkFogStartTime = 0;
    private long screenFlashStartTime = 0;

    private Label barrierCountdownLabel;
    private Label extraJumpLabel;

    private List<Coin> coinsList;
    private CoinDisplay coinDisplay;
    private int sessionCoins = 0;
    private int lastCoinSpawnScore = 0;

    private boolean spacePressed = false;
    private boolean upPressed = false;
    private boolean jumpAfterRestart = false;

    // 骨頭迴力鏢變數
    private Group boomerangGroup;
    private ImageView boomerangView;
    private Rectangle boomerangHitBox;
    private boolean boomerangActive = false;
    private double boomerangTime = 0.0;
    private double boomerangStartX = 0.0;
    private double boomerangStartY = 0.0;
    private boolean boomerangHasDamaged = false;
    private double boomerangMaxDist = 350.0;

    // Boss 生命條 UI 變數
    private Pane bossHealthBarContainer;
    private Rectangle bossHealthInnerBar;
    private Label bossHealthLabel;

    // Game Clock
    private long activeGameTime = 0;
    private long lastFrameTime = 0;
    private double regenTimer = 0.0;

    // 引入 GameConfig 常數
    private final double acceleration = GameConfig.ACCELERATION;
    private final double maxSpeed = GameConfig.MAX_SPEED;

    private double distance = 0;

    public GameScene(DinoMain dinoMain) {
        this(dinoMain, GameConfig.selectedCharacter);
    }

    public GameScene(DinoMain dinoMain, String character) {
        this.dinoMain = dinoMain;
        this.currentCharacter = character;

        root = new Pane();
        root.setStyle("-fx-background-color: white;");

        createClouds();
        createGround();

        // 改用 ResourceManager 讀取圖片
        Image gameOverPic = ResourceManager.getImage("gameover.png");

        gameOverImage = new ImageView(gameOverPic);
        gameOverImage.setSmooth(false);
        gameOverImage.setFitWidth(300);
        gameOverImage.setPreserveRatio(true);

        gameOverImage.setX(350);
        gameOverImage.setY(120);
        gameOverImage.setVisible(false);

        // 改用 ResourceManager 讀取圖片
        Image restartPic = ResourceManager.getImage("restart.png");

        restartImage = new ImageView(restartPic);
        restartImage.setSmooth(false);
        restartImage.setFitWidth(40);
        restartImage.setPreserveRatio(true);
        restartImage.setX(470);
        restartImage.setY(170);
        restartImage.setVisible(false);

        // 新增遊戲結束時的「返回主選單」按鈕，居中放置在重新開始按鈕下方
        gameOverMenuBtn = new Button("返回主選單");
        gameOverMenuBtn.setStyle(
                "-fx-background-color: #8B4513; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 2; " +
                        "-fx-font-family: 'Microsoft JhengHei', 'Courier New', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 6 14; " +
                        "-fx-cursor: hand;");
        gameOverMenuBtn.setLayoutX(435);
        gameOverMenuBtn.setLayoutY(230);
        gameOverMenuBtn.setVisible(false);

        gameOverMenuBtn.setOnMouseEntered(e -> gameOverMenuBtn.setStyle(
                "-fx-background-color: #5d4037; " +
                        "-fx-text-fill: #ffca28; " +
                        "-fx-border-color: #ffca28; " +
                        "-fx-border-width: 2; " +
                        "-fx-font-family: 'Microsoft JhengHei', 'Courier New', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 6 14; " +
                        "-fx-cursor: hand;"));

        gameOverMenuBtn.setOnMouseExited(e -> gameOverMenuBtn.setStyle(
                "-fx-background-color: #8B4513; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 2; " +
                        "-fx-font-family: 'Microsoft JhengHei', 'Courier New', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 6 14; " +
                        "-fx-cursor: hand;"));

        gameOverMenuBtn.setOnAction(e -> {
            if (timer != null)
                timer.stop();
            SoundManager.stopGameBgm();
            dinoMain.showMainMenu();
        });

        restartImage.setOnMouseClicked(e -> {
            if (gameOver) {
                restartGame();
            }
        });

        scoreDisplay = new ScoreDisplay();
        dino = new Dino(100, GameConfig.GROUND_Y, character);
        heartDisplay = new HeartDisplay(dino.getMaxLives());
        skillDisplay = new SkillDisplay();
        questionBlocks = new ArrayList<>();
        coinsList = new ArrayList<>();
        coinDisplay = new CoinDisplay();

        signpost = new Label("【操作說明】\n[上方向鍵] 跳躍\n[空白鍵] 開始/骨頭迴力鏢(Boss戰)\n[下方向鍵] 蹲下\n[自訂技能按鍵] 施放技能");
        signpost.setStyle(
                "-fx-background-color: #8B4513; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-font-family: 'Courier New', monospace; -fx-padding: 10; -fx-font-weight: bold;");
        signpost.setLayoutX(300);
        signpost.setLayoutY(GameConfig.GROUND_Y - 120);

        milkFog = new Rectangle(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, Color.rgb(255, 255, 255, 0.7));
        milkFog.setVisible(false);

        barrierCountdownLabel = new Label();
        barrierCountdownLabel.setFont(Font.font("Arial", 36));
        barrierCountdownLabel.setTextFill(Color.BLUE);
        barrierCountdownLabel.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - 80);
        barrierCountdownLabel.setLayoutY(GameConfig.SCREEN_HEIGHT / 2 - 100);
        barrierCountdownLabel.setVisible(false);

        extraJumpLabel = new Label();
        extraJumpLabel.setFont(Font.font("Arial", 24));
        extraJumpLabel.setTextFill(Color.MAGENTA);
        extraJumpLabel.setLayoutX(20);
        extraJumpLabel.setLayoutY(150);
        extraJumpLabel.setVisible(false);

        obstacles = new ArrayList<>();

        obstacles.add(new ObstacleSlot(850, groundY));
        obstacles.add(new ObstacleSlot(1150, groundY));
        obstacles.add(new ObstacleSlot(1450, groundY));

        root.getChildren().addAll(
                cloud1,
                cloud2,
                cloud3,
                ground1,
                ground2,
                signpost,
                dino.getView());

        screenFlash = new Rectangle(screenWidth, GameConfig.SCREEN_HEIGHT, Color.rgb(255, 0, 0, 0.5));
        screenFlash.setVisible(false);
        root.getChildren().add(screenFlash);

        for (ObstacleSlot obstacle : obstacles) {
            root.getChildren().add(obstacle.getCactus().getView());
            root.getChildren().add(obstacle.getBird().getView());
        }

        root.getChildren().addAll(
                milkFog,
                heartDisplay.getView(),
                coinDisplay.getView(),
                scoreDisplay.getView(),
                skillDisplay.getView(),
                barrierCountdownLabel,
                extraJumpLabel,
                gameOverImage,
                restartImage,
                gameOverMenuBtn);

        createPauseOverlay();
        root.getChildren().add(pauseOverlay);

        dino.showHint("按空白鍵或上鍵開始！");
        startGameLoop();
    }

    private void createPauseOverlay() {
        pauseOverlay = new StackPane();
        pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        pauseOverlay.setPrefSize(screenWidth, 600); // 確保能覆蓋整個畫面

        VBox pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setTranslateY(-30);

        Label pauseLabel = new Label("遊戲暫停");
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setFont(Font.font(30));

        SettingsPanel settingsPanel = new SettingsPanel();
        settingsPanel.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10;");
        settingsPanel.setMaxWidth(300);

        Button resumeBtn = new Button("繼續遊戲");
        Button restartBtn = new Button("重新開始");
        Button menuBtn = new Button("返回主選單");

        resumeBtn.setOnAction(e -> togglePause());
        restartBtn.setOnAction(e -> {
            isPaused = false;
            pauseOverlay.setVisible(false);
            restartGame();
            lastFrameTime = 0;
            timer.start();
        });
        menuBtn.setOnAction(e -> {
            if (timer != null)
                timer.stop();
            SoundManager.stopGameBgm();
            dinoMain.showMainMenu();
        });

        pauseMenu.getChildren().addAll(pauseLabel, settingsPanel, resumeBtn, restartBtn, menuBtn);
        pauseOverlay.getChildren().add(pauseMenu);
        pauseOverlay.setVisible(false);
    }

    private void createClouds() {
        // 改用 ResourceManager 讀取圖片
        Image cloudImage = ResourceManager.getImage("cloud.png");

        cloud1 = new ImageView(cloudImage);
        cloud1.setSmooth(false);
        cloud2 = new ImageView(cloudImage);
        cloud2.setSmooth(false);
        cloud3 = new ImageView(cloudImage);
        cloud3.setSmooth(false);

        cloud1.setFitWidth(80);
        cloud1.setPreserveRatio(true);
        cloud1.setX(250);
        cloud1.setY(90);

        cloud2.setFitWidth(70);
        cloud2.setPreserveRatio(true);
        cloud2.setX(500);
        cloud2.setY(120);

        cloud3.setFitWidth(90);
        cloud3.setPreserveRatio(true);
        cloud3.setX(720);
        cloud3.setY(75);
    }

    private void createGround() {
        // 改用 ResourceManager 讀取圖片
        Image groundImage = ResourceManager.getImage("ground.png");

        ground1 = new ImageView(groundImage);
        ground1.setSmooth(false);
        ground2 = new ImageView(groundImage);
        ground2.setSmooth(false);

        ground1.setPreserveRatio(true);
        ground2.setPreserveRatio(true);

        ground1.setFitWidth(groundWidth);
        ground2.setFitWidth(groundWidth);

        ground1.setX(0);
        ground1.setY(groundImageY);

        ground2.setX(groundWidth);
        ground2.setY(groundImageY);
    }

    private void startGameLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }
                long deltaTime = (now - lastFrameTime);
                lastFrameTime = now;
                activeGameTime += deltaTime / 1_000_000;
                double dtSeconds = deltaTime / 1_000_000_000.0;
                update(dtSeconds);
            }
        };
        timer.start();
    }

    private void update(double dtSeconds) {
        if (gameOver) {
            return;
        }

        if (waitingToStart) {
            return;
        }

        if (jumpAfterRestart) {
            if (dino.jump()) {
                SoundManager.playJump();
            }
            jumpAfterRestart = false;
        }

        frameCount++;

        updateSpeed(dtSeconds);
        updateScore(dtSeconds);

        dino.update(activeGameTime, dtSeconds);

        // 處理金幣
        double dinoCenterX = dino.getView().getLayoutX() + dino.getView().getBoundsInLocal().getWidth() / 2.0;
        double dinoCenterY = dino.getView().getLayoutY() + dino.getView().getBoundsInLocal().getHeight() / 2.0;
        double magnetRadius = SaveManager.getMagnetRadius();

        java.util.Iterator<Coin> coinIt = coinsList.iterator();
        while (coinIt.hasNext()) {
            Coin coin = coinIt.next();
            coin.update(speed, dtSeconds, dinoCenterX, dinoCenterY, magnetRadius);
            if (coin.getHitBoxBounds().intersects(dino.getHitBoxBounds())) {
                root.getChildren().remove(coin.getView());
                coinIt.remove();

                int multiplier = SaveManager.getCoinMultiplier();
                int coinsEarned = 1 * multiplier;
                sessionCoins += coinsEarned;
                SaveManager.addCoins(coinsEarned);
                coinDisplay.update(sessionCoins);

                SoundManager.playScore();
                showFloatingText("+" + coinsEarned, coin.getX(), coin.getY());
            } else if (coin.isOffScreen()) {
                root.getChildren().remove(coin.getView());
                coinIt.remove();
            }
        }

        // 處理問號方塊
        java.util.Iterator<QuestionBlock> it = questionBlocks.iterator();
        while (it.hasNext()) {
            QuestionBlock qb = it.next();
            qb.update(speed, dtSeconds);
            if (qb.getHitBoxBounds().intersects(dino.getHitBoxBounds())) {
                root.getChildren().remove(qb.getView());
                it.remove();
                
                giveRandomItem();
            } else if (qb.isOffScreen()) {
                root.getChildren().remove(qb.getView());
                it.remove();
            }
        }

        // 處理屏障時間
        if (barrierActive) {
            long remaining = GameConfig.BARRIER_DURATION_MS - (activeGameTime - barrierStartTime);
            if (remaining <= 0) {
                barrierActive = false;
                dino.getView().setOpacity(1.0);
                barrierCountdownLabel.setVisible(false);
            } else {
                barrierCountdownLabel.setVisible(true);
                barrierCountdownLabel.setText("屏障: " + (int) Math.ceil(remaining / 1000.0) + "s");
            }
        }

        // 處理額外跳躍 UI
        if (dino.getExtraJumps() > 0) {
            extraJumpLabel.setVisible(true);
            extraJumpLabel.setText("額外跳躍: " + dino.getExtraJumps());
        } else {
            extraJumpLabel.setVisible(false);
        }

        if (milkFog.isVisible()) {
            if (activeGameTime - milkFogStartTime >= GameConfig.MILK_FOG_DURATION_MS) {
                milkFog.setVisible(false);
            }
        }

        if (screenFlash.isVisible()) {
            if (activeGameTime - screenFlashStartTime >= 500) {
                screenFlash.setVisible(false);
            }
        }

        // 處理緩慢自動回血
        if (SaveManager.hasRegen() && !dino.isDead() && dino.getLives() < dino.getMaxLives()) {
            regenTimer += dtSeconds;
            double targetTime = 40.0;
            if (SaveManager.getRegenLevel() == 2) targetTime = 20.0;
            else if (SaveManager.getRegenLevel() == 3) targetTime = 10.0;

            if (regenTimer >= targetTime) {
                regenTimer = 0.0;
                dino.healOne();
                heartDisplay.update(dino.getLives());
                SoundManager.playAppleSound(); // 播放清脆的回血成功音效！
                showFloatingText("+1 HP", dino.getView().getLayoutX() + 20, dino.getView().getLayoutY() - 30);
            }
        } else {
            regenTimer = 0.0; // 如果滿血或沒有該功能，重置計時器
        }

        // 處理骨頭迴力鏢運動與碰撞
        if (boomerangActive) {
            boomerangTime += dtSeconds;
            // 快速自轉
            boomerangView.setRotate(boomerangView.getRotate() + dtSeconds * 720.0);

            double duration = 0.5;
            double outward = 0.25;
            double maxDist = boomerangMaxDist;

            if (boomerangTime < outward) {
                double pct = boomerangTime / outward;
                double curX = boomerangStartX + maxDist * pct;
                double curY = boomerangStartY;
                boomerangGroup.setLayoutX(curX);
                boomerangGroup.setLayoutY(curY);
            } else if (boomerangTime < duration) {
                double pct = (boomerangTime - outward) / outward;
                double outX = boomerangStartX + maxDist;
                double outY = boomerangStartY;
                // 追蹤恐龍的當前座標，優雅回收到手
                double targetX = dino.getView().getLayoutX() + dino.getView().getBoundsInLocal().getWidth() / 2.0 - 16;
                double targetY = dino.getView().getLayoutY() + dino.getView().getBoundsInLocal().getHeight() / 2.0 - 16;

                double curX = outX * (1.0 - pct) + targetX * pct;
                double curY = outY * (1.0 - pct) + targetY * pct;
                boomerangGroup.setLayoutX(curX);
                boomerangGroup.setLayoutY(curY);
            } else {
                // 回收到手，銷毀
                root.getChildren().remove(boomerangGroup);
                boomerangActive = false;
            }

            // 碰撞檢測：擊中 Boss
            if (boomerangActive && !boomerangHasDamaged && bossPhase && boss != null) {
                if (boomerangHitBox.localToScene(boomerangHitBox.getBoundsInLocal())
                        .intersects(boss.getHitBoxBounds())) {
                    boss.takeDamage(10);
                    boomerangHasDamaged = true;
                    SoundManager.playHit(); // 播放擊中音效

                    // 在 Boss 上方飄字
                    double damageX = boss.getHitBoxBounds().getCenterX() - 20;
                    double damageY = boss.getHitBoxBounds().getMinY() - 20;
                    showFloatingText("-10 HP", damageX, damageY);
                }
            }
        }

        if (bossPhase && boss != null) {
            boss.update(speed, activeGameTime, dtSeconds);

            // 更新 Boss 生命條 UI
            if (bossHealthBarContainer != null && bossHealthInnerBar != null && bossHealthLabel != null) {
                double hpPct = (double) boss.getHp() / 100.0;
                bossHealthInnerBar.setWidth(296 * hpPct);
                bossHealthLabel.setText("BOSS: Bowser (" + boss.getHp() + "/100)");
            }

            if (boss.isDefeated(activeGameTime)) {
                bossPhase = false;
                boss.removeAllProjectiles();
                boss = null;

                // 銷毀生命條 UI
                if (bossHealthBarContainer != null) {
                    root.getChildren().remove(bossHealthBarContainer);
                    bossHealthBarContainer = null;
                }

                // 銷毀在場上的迴力鏢
                if (boomerangActive) {
                    root.getChildren().remove(boomerangGroup);
                    boomerangActive = false;
                }

                inBossGracePeriod = true;
                bossGracePeriodStartTime = activeGameTime;
            }
        } else if (inBossGracePeriod) {
            if (activeGameTime - bossGracePeriodStartTime >= GameConfig.BOSS_RETREAT_GRACE_PERIOD_MS) {
                inBossGracePeriod = false;
                resetAllObstacles();
            }
        } else {
            updateObstacles(dtSeconds);
        }

        checkCollision();

        updateGround(dtSeconds);
        updateClouds(dtSeconds);
    }

    private void checkCollision() {
        if (bossPhase && boss != null) {
            if (boss.checkCollision(dino.getHitBoxBounds())) {
                if (barrierActive)
                    return; // 屏障啟動時免傷
                boolean damaged = dino.hit(activeGameTime);
                if (damaged) {
                    SoundManager.playHit();
                    heartDisplay.update(dino.getLives());
                    if (dino.isDead()) {
                        gameOver();
                    }
                }
            }
        } else {
            for (ObstacleSlot obstacle : obstacles) {
                if (dino.getHitBoxBounds().intersects(obstacle.getHitBoxBounds())) {
                    if (barrierActive) {
                        resetObstacle(obstacle);
                        distance += GameConfig.OBSTACLE_CLEAR_SCORE * 50;
                        SoundManager.playScore();
                    } else {
                        boolean damaged = dino.hit(activeGameTime);
                        if (damaged) {
                            SoundManager.playHit();
                            heartDisplay.update(dino.getLives());
                            if (dino.isDead()) {
                                gameOver();
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private void gameOver() {
        SoundManager.stopGameBgm();
        gameOver = true;
        if (score > sessionHighScore) {
            sessionHighScore = score;
        }
        scoreDisplay.update(score, sessionHighScore);
        dino.die();

        if (LeaderboardManager.isHighScore(score)) {
            Platform.runLater(() -> {
                TextInputDialog dialog = new TextInputDialog("Player");
                dialog.setTitle("排行榜");
                dialog.setHeaderText("破紀錄啦！");
                dialog.setContentText("請輸入你的名字：");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    String cleanName = name.replace(",", "").trim();
                    if (cleanName.isEmpty())
                        cleanName = "Unknown";
                    LeaderboardManager.addScore(cleanName, score, currentCharacter);
                });

                gameOverImage.setVisible(true);
                restartImage.setVisible(true);
                gameOverMenuBtn.setVisible(true);
            });
        } else {
            gameOverImage.setVisible(true);
            restartImage.setVisible(true);
            gameOverMenuBtn.setVisible(true);
        }
    }

    private double getRightMostObstacleX() {
        double maxX = screenWidth;
        for (ObstacleSlot obstacle : obstacles) {
            if (obstacle.getX() > maxX) {
                maxX = obstacle.getX();
            }
        }
        return maxX;
    }

    private void resetObstacle(ObstacleSlot obstacle) {
        double minDistance = 220 + speed * (28.0 / 60.0);
        double randomDistance = Math.random() * 350;
        double rightMostX = getRightMostObstacleX();
        double newX = rightMostX + minDistance + randomDistance;
        obstacle.reset(newX, score, groundY);
    }

    private void updateObstacles(double dtSeconds) {
        boolean allCleared = true;
        for (ObstacleSlot obstacle : obstacles) {
            obstacle.update(speed, dtSeconds);
            if (obstacle.getX() < -obstacle.getWidth()) {
                if (!bossIncoming && !bossPhase) {
                    resetObstacle(obstacle);
                }
            } else {
                allCleared = false;
            }
        }

        if (bossIncoming && allCleared) {
            triggerBossPhase();
        }
    }

    private void triggerBossPhase() {
        bossIncoming = false;
        bossPhase = true;
        boss = new Boss(root, activeGameTime);

        screenFlash.setFill(Color.rgb(255, 0, 0, 0.5));
        screenFlash.setVisible(true);
        screenFlashStartTime = activeGameTime;

        // 創建 Boss 生命值血條 UI
        bossHealthBarContainer = new Pane();
        bossHealthBarContainer.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - 150);
        bossHealthBarContainer.setLayoutY(20);

        Rectangle bgBar = new Rectangle(300, 16);
        bgBar.setFill(Color.DARKGRAY);
        bgBar.setStroke(Color.WHITE);
        bgBar.setStrokeWidth(2);

        bossHealthInnerBar = new Rectangle(296, 12);
        bossHealthInnerBar.setX(2);
        bossHealthInnerBar.setY(2);
        bossHealthInnerBar.setFill(Color.RED);

        bossHealthLabel = new Label("BOSS: Bowser (100/100)");
        bossHealthLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        bossHealthLabel.setTextFill(Color.WHITE);
        bossHealthLabel.setLayoutY(-18);
        bossHealthLabel.setLayoutX(0);
        bossHealthLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 2, 0, 1, 1);");

        bossHealthBarContainer.getChildren().addAll(bgBar, bossHealthInnerBar, bossHealthLabel);

        int insertIndex = root.getChildren().indexOf(milkFog);
        if (insertIndex != -1) {
            root.getChildren().add(insertIndex, bossHealthBarContainer);
        } else {
            root.getChildren().add(bossHealthBarContainer);
        }
    }

    private void updateGround(double dtSeconds) {
        ground1.setX(ground1.getX() - speed * dtSeconds);
        ground2.setX(ground2.getX() - speed * dtSeconds);

        if (ground1.getX() <= -groundWidth) {
            ground1.setX(ground2.getX() + groundWidth);
        }
        if (ground2.getX() <= -groundWidth) {
            ground2.setX(ground1.getX() + groundWidth);
        }

        if (signpost.getLayoutX() > -300) {
            signpost.setLayoutX(signpost.getLayoutX() - speed * dtSeconds);
        }
    }

    private void updateClouds(double dtSeconds) {
        double cloudSpeed = speed * 0.25 * dtSeconds;

        cloud1.setX(cloud1.getX() - cloudSpeed);
        cloud2.setX(cloud2.getX() - cloudSpeed);
        cloud3.setX(cloud3.getX() - cloudSpeed);

        if (cloud1.getX() < -100)
            resetCloud(cloud1);
        if (cloud2.getX() < -100)
            resetCloud(cloud2);
        if (cloud3.getX() < -100)
            resetCloud(cloud3);
    }

    private void updateScore(double dtSeconds) {
        distance += speed * dtSeconds;
        int newScore = (int) (distance / 50);

        if (newScore > score) {
            score = newScore;
            if (score > 0 && score / 100 > lastScoreSound / 100) {
                lastScoreSound = score;
                SoundManager.playScore();
                scoreDisplay.flashCurrentScore(score);
            }

            int qbInterval = GameConfig.QUESTION_BLOCK_INTERVAL;
            int qbLevel = SaveManager.getQuestionBoxLevel();
            if (qbLevel == 1) {
                qbInterval = 200;
            } else if (qbLevel == 2) {
                qbInterval = 150;
            } else if (qbLevel >= 3) {
                qbInterval = 100;
            }

            if (score > 0 && score / qbInterval > lastQuestionBlockScore / qbInterval) {
                lastQuestionBlockScore = score;
                QuestionBlock qb = new QuestionBlock(screenWidth, groundY);
                questionBlocks.add(qb);
                int idx = root.getChildren().indexOf(milkFog);
                if (idx != -1) {
                    root.getChildren().add(idx, qb.getView());
                } else {
                    root.getChildren().add(qb.getView());
                }
            }

            int coinSpawnInterval = SaveManager.hasMoreCoins() ? 20 : GameConfig.COIN_SPAWN_INTERVAL;
            if (score > 0 && score % coinSpawnInterval == 0 && score != lastCoinSpawnScore) {
                lastCoinSpawnScore = score;
                double coinY;
                double r = Math.random();
                if (r < 0.4) {
                    coinY = groundY - 20;
                } else if (r < 0.7) {
                    coinY = groundY - 60;
                } else {
                    coinY = groundY - 110;
                }
                double safeX = getSafeCoinX();
                Coin coin = new Coin(safeX, coinY);
                coinsList.add(coin);
                int idx = root.getChildren().indexOf(milkFog);
                if (idx != -1) {
                    root.getChildren().add(idx, coin.getView());
                } else {
                    root.getChildren().add(coin.getView());
                }
            }

            if (!bossHasAppeared && score >= GameConfig.BOSS_TRIGGER_SCORE) {
                bossIncoming = true;
                bossHasAppeared = true;
            }
        }
        scoreDisplay.update(score, sessionHighScore);
    }

    private void updateSpeed(double dtSeconds) {
        speed += acceleration * dtSeconds;
        if (speed > maxSpeed) {
            speed = maxSpeed;
        }
    }

    public void setKeyControl(Scene scene) {
        // 動態縮放機制
        javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(1, 1);
        root.getTransforms().add(scale);

        javafx.beans.value.ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            if (Double.isNaN(w) || Double.isNaN(h)) {
                w = GameConfig.SCREEN_WIDTH;
                h = GameConfig.SCREEN_HEIGHT;
            }
            double scaleX = w / GameConfig.SCREEN_WIDTH;
            double scaleY = h / GameConfig.SCREEN_HEIGHT;
            double minScale = Math.min(scaleX, scaleY);

            scale.setX(minScale);
            scale.setY(minScale);

            root.setTranslateX((w - GameConfig.SCREEN_WIDTH * minScale) / 2);
            root.setTranslateY((h - GameConfig.SCREEN_HEIGHT * minScale) / 2);
        };
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
        sizeListener.changed(null, null, null); // 立即觸發一次計算初始比例

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePause();
                return;
            }
            if (isPaused)
                return;

            if (e.getCode() == KeyCode.SPACE && !spacePressed) {
                spacePressed = true;
                if (waitingToStart) {
                    waitingToStart = false;
                    dino.hideHint();
                    SoundManager.playGameBgm();
                    return;
                }
                if (gameOver) {
                    restartGame();
                    waitingToStart = false;
                    dino.hideHint();
                    SoundManager.playGameBgm();
                } else {
                    if (bossPhase && boss != null) {
                        throwBoomerang();
                    }
                }
            }

            if (e.getCode() == KeyCode.UP && !upPressed) {
                upPressed = true;
                if (waitingToStart) {
                    waitingToStart = false;
                    dino.hideHint();
                    SoundManager.playGameBgm();
                    if (dino.jump()) {
                        SoundManager.playJump();
                    }
                    return;
                }
                if (gameOver) {
                    restartGame();
                    waitingToStart = false;
                    dino.hideHint();
                    SoundManager.playGameBgm();
                    if (dino.jump()) {
                        SoundManager.playJump();
                    }
                } else {
                    if (dino.jump()) {
                        SoundManager.playJump();
                    }
                }
            }

            if (waitingToStart) {
                return;
            }
            if (e.getCode() == KeyCode.DOWN && !gameOver) {
                dino.pressDown();
            }

            // 技能觸發區塊
            if (e.getCode() == KeyCode.Q && GameConfig.goldenAppleCount > 0) {
                GameConfig.goldenAppleCount--;
                SoundManager.playAppleSound();
                dino.healToFull();
                heartDisplay.update(dino.getLives());
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.W && GameConfig.milkBucketCount > 0) {
                GameConfig.milkBucketCount--;
                SoundManager.playMilkSound();
                distance += GameConfig.MILK_SCORE_BONUS * 50;
                milkFog.setVisible(true);
                milkFogStartTime = activeGameTime;
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.E && GameConfig.enchantedBookCount > 0) {
                GameConfig.enchantedBookCount--;
                SoundManager.playBookSound();
                dino.addExtraJump();
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.R && GameConfig.barrierCount > 0) {
                GameConfig.barrierCount--;
                SoundManager.playBarrierSound();
                barrierActive = true;
                barrierStartTime = activeGameTime;
                dino.getView().setOpacity(0.8);
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.F && GameConfig.woodenSwordCount > 0) {
                GameConfig.woodenSwordCount--;
                SoundManager.playSwordSound();
                clearScreenObstacles();
                skillDisplay.update();
            }

            // 開發者模式專用區塊
            if (GameConfig.devModeEnabled) {
                if (e.getCode() == KeyCode.F1) {
                    dino.toggleDevInvincible();
                } else if (e.getCode() == KeyCode.F2) {
                    distance += 100 * 50;
                } else if (e.getCode() == KeyCode.F3) {
                    distance = 950 * 50;
                } else if (e.getCode() == KeyCode.F4) {
                    // 強制在恐龍前方生成一個道具方塊
                    QuestionBlock qb = new QuestionBlock(dino.getHitBoxBounds().getMaxX() + 50, GameConfig.GROUND_Y);
                    questionBlocks.add(qb);
                    int idx = root.getChildren().indexOf(milkFog);
                    if (idx != -1) {
                        root.getChildren().add(idx, qb.getView());
                    } else {
                        root.getChildren().add(qb.getView());
                    }
                } else if (e.getCode() == KeyCode.F5) {
                    // 強制召喚 Boss
                    if (!bossPhase) {
                        triggerBossPhase();
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (isPaused)
                return;

            if (e.getCode() == KeyCode.SPACE) {
                spacePressed = false;
            }
            if (e.getCode() == KeyCode.UP) {
                upPressed = false;
                if (!gameOver) {
                    dino.releaseJump();
                }
            }
            if (e.getCode() == KeyCode.DOWN && !gameOver) {
                dino.releaseDown();
            }
        });
    }

    private void togglePause() {
        if (gameOver)
            return;

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            SoundManager.pauseGameBgm();
            pauseOverlay.setVisible(true);
        } else {
            pauseOverlay.setVisible(false);
            root.requestFocus(); // 將焦點還給遊戲容器
            lastFrameTime = 0; // 重置時間計算
            timer.start();
            SoundManager.resumeGameBgm();
        }
    }

    private void restartGame() {
        SoundManager.stopGameBgm(); // 確保重新開始前會停止並銷毀當前音樂
        gameOver = false;
        score = 0;
        distance = 0;
        frameCount = 0;
        speed = GameConfig.INITIAL_SPEED; // 套用常數
        lastScoreSound = 0;
        jumpAfterRestart = false;
        waitingToStart = true;
        signpost.setLayoutX(300);
        dino.showHint("按空白鍵或上鍵開始！");
        activeGameTime = 0;
        lastFrameTime = 0;

        if (boss != null) {
            boss.removeAllProjectiles();
            boss = null;
        }
        if (boomerangActive) {
            root.getChildren().remove(boomerangGroup);
            boomerangActive = false;
        }
        if (bossHealthBarContainer != null) {
            root.getChildren().remove(bossHealthBarContainer);
            bossHealthBarContainer = null;
        }
        bossPhase = false;
        bossIncoming = false;
        bossHasAppeared = false;
        inBossGracePeriod = false;

        lastQuestionBlockScore = 0;
        barrierActive = false;
        milkFog.setVisible(false);
        dino.getView().setOpacity(1.0);

        for (QuestionBlock qb : questionBlocks) {
            root.getChildren().remove(qb.getView());
        }
        questionBlocks.clear();

        for (Coin coin : coinsList) {
            root.getChildren().remove(coin.getView());
        }
        coinsList.clear();
        sessionCoins = 0;
        lastCoinSpawnScore = 0;
        regenTimer = 0.0;
        coinDisplay.update(sessionCoins);

        GameConfig.goldenAppleCount = 0;
        GameConfig.milkBucketCount = 0;
        GameConfig.enchantedBookCount = 0;
        GameConfig.barrierCount = 0;
        GameConfig.woodenSwordCount = 0;
        skillDisplay.update();

        gameOverImage.setVisible(false);
        restartImage.setVisible(false);
        gameOverMenuBtn.setVisible(false);

        dino.reset();
        heartDisplay.update(dino.getLives());

        resetAllObstacles();
        scoreDisplay.update(score, sessionHighScore);
    }

    private void resetAllObstacles() {
        double startX = 850;
        for (ObstacleSlot obstacle : obstacles) {
            obstacle.reset(startX, score, groundY);
            startX += 300;
        }
    }

    private void giveRandomItem() {
        int totalWeight = GameConfig.weightGoldenApple + GameConfig.weightMilkBucket +
                GameConfig.weightEnchantedBook + GameConfig.weightBarrier + GameConfig.weightWoodenSword;
        int rand = (int) (Math.random() * totalWeight);

        if (rand < GameConfig.weightGoldenApple) {
            GameConfig.goldenAppleCount++;
            dino.showHint("獲得金蘋果 (Q): 補滿生命值！");
        } else if (rand < GameConfig.weightGoldenApple + GameConfig.weightMilkBucket) {
            GameConfig.milkBucketCount++;
            dino.showHint("獲得牛奶 (W): 獲得500pt但致盲視野5秒！");
        } else if (rand < GameConfig.weightGoldenApple + GameConfig.weightMilkBucket + GameConfig.weightEnchantedBook) {
            GameConfig.enchantedBookCount++;
            dino.showHint("獲得附魔書 (E): 獲得額外跳躍次數！");
        } else if (rand < GameConfig.weightGoldenApple + GameConfig.weightMilkBucket + GameConfig.weightEnchantedBook
                + GameConfig.weightBarrier) {
            GameConfig.barrierCount++;
            dino.showHint("獲得屏障 (R): 12秒無敵護盾！");
        } else {
            GameConfig.woodenSwordCount++;
            dino.showHint("獲得木劍 (F): 清除畫面上所有障礙物！(對Boss無效)");
        }

        skillDisplay.update();
        SoundManager.playScore();
    }

    private void clearScreenObstacles() {
        java.util.Iterator<ObstacleSlot> iterator = obstacles.iterator();
        java.util.List<ObstacleSlot> replacementObstacles = new java.util.ArrayList<>();

        while (iterator.hasNext()) {
            ObstacleSlot obstacle = iterator.next();
            double obsMinX = obstacle.getHitBoxBounds().getMinX();
            double obsMaxX = obstacle.getHitBoxBounds().getMaxX();

            // 判斷是否在畫面內 (只清除螢幕上的障礙物)
            if (obsMaxX > 0 && obsMinX < GameConfig.SCREEN_WIDTH) {
                // 1. 確實將障礙物的圖片從渲染畫面上拔除
                root.getChildren().remove(obstacle.getCactus().getView());
                root.getChildren().remove(obstacle.getBird().getView());

                // 2. 使用 Iterator 安全移除，避免 ConcurrentModificationException
                iterator.remove();

                // 3. 觸發加分
                distance += GameConfig.OBSTACLE_CLEAR_SCORE * 50;

                // 4. 紀錄需要補回的物件，避免破壞物件池機制導致後續無障礙物
                replacementObstacles.add(new ObstacleSlot(GameConfig.SCREEN_WIDTH, groundY));
            }
        }

        // 將新生成的障礙物補回清單與畫面，並確保 Z-index 正確 (在 UI 層之下)
        int insertIndex = root.getChildren().indexOf(milkFog);
        if (insertIndex == -1)
            insertIndex = root.getChildren().size();

        for (ObstacleSlot newSlot : replacementObstacles) {
            root.getChildren().add(insertIndex, newSlot.getCactus().getView());
            root.getChildren().add(insertIndex + 1, newSlot.getBird().getView());
            obstacles.add(newSlot);
            resetObstacle(newSlot); // 重新計算並分配到畫面右側的隨機位置
        }
    }

    private boolean isCloudOverlapping(ImageView targetCloud, double newX, double newY) {
        ImageView[] clouds = { cloud1, cloud2, cloud3 };
        double cloudWidth = targetCloud.getBoundsInLocal().getWidth();
        double cloudHeight = targetCloud.getBoundsInLocal().getHeight();

        for (ImageView cloud : clouds) {
            if (cloud == targetCloud)
                continue;
            double otherX = cloud.getX();
            double otherY = cloud.getY();
            double otherWidth = cloud.getBoundsInLocal().getWidth();
            double otherHeight = cloud.getBoundsInLocal().getHeight();

            boolean overlapX = newX < otherX + otherWidth + 80 && newX + cloudWidth + 80 > otherX;
            boolean overlapY = newY < otherY + otherHeight + 30 && newY + cloudHeight + 30 > otherY;

            if (overlapX && overlapY)
                return true;
        }
        return false;
    }

    private void resetCloud(ImageView cloud) {
        double newX;
        double newY;
        int attempts = 0;

        do {
            newX = 850 + Math.random() * 400;
            newY = 60 + Math.random() * 90;
            attempts++;
        } while (isCloudOverlapping(cloud, newX, newY) && attempts < 20);

        cloud.setX(newX);
        cloud.setY(newY);
    }

    private void showFloatingText(String text, double x, double y) {
        Label label = new Label(text);
        label.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
        label.setTextFill(Color.web("#FFD54F"));
        label.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 3, 0, 1, 1);");
        label.setLayoutX(x);
        label.setLayoutY(y);
        root.getChildren().add(label);

        TranslateTransition translate = new TranslateTransition(Duration.millis(600), label);
        translate.setByY(-50);

        FadeTransition fade = new FadeTransition(Duration.millis(600), label);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.setOnFinished(e -> root.getChildren().remove(label));
        parallel.play();
    }

    private double getSafeCoinX() {
        double candidateX = screenWidth;
        boolean safe = false;
        int attempts = 0;

        while (!safe && attempts < 10) {
            safe = true;
            for (ObstacleSlot obs : obstacles) {
                double obsX = obs.getX();
                double dist = Math.abs(obsX - candidateX);
                // 確保金幣與障礙物的左右距離至少大於 130 像素，保證玩家反應與操作空間
                if (dist < 130.0) {
                    candidateX = Math.max(candidateX, obsX) + 140.0;
                    safe = false;
                    break;
                }
            }
            attempts++;
        }
        return candidateX;
    }

    private void throwBoomerang() {
        if (boomerangActive) {
            return; // 迴力鏢尚未回收，無法再次拋出
        }

        Image boneImg = ResourceManager.getImage("bone.png");
        if (boneImg == null) {
            return;
        }

        boomerangView = new ImageView(boneImg);
        boomerangView.setFitWidth(32);
        boomerangView.setFitHeight(32);
        boomerangView.setSmooth(false);

        // 碰撞箱 (與圖片大小相當)
        boomerangHitBox = new Rectangle(0, 0, 32, 32);
        boomerangHitBox.setVisible(false);

        boomerangGroup = new Group(boomerangView, boomerangHitBox);

        double dinoWidth = dino.getView().getBoundsInLocal().getWidth();
        double dinoHeight = dino.getView().getBoundsInLocal().getHeight();
        double startX = dino.getView().getLayoutX() + dinoWidth - 10;
        double startY = dino.getView().getLayoutY() + dinoHeight / 2 - 16;

        // 動態計算丟到 Boss 的長度
        double targetDist = 350.0;
        if (bossPhase && boss != null) {
            double bossX = boss.getX() + 42.0; // 庫巴的中心 X (寬度為 84，中心在 +42)
            double dinoX = dino.getView().getLayoutX() + dinoWidth / 2.0;
            double distanceToBoss = (bossX - dinoX) + 20.0; // 多飛 20 像素確保覆蓋與碰撞
            targetDist = Math.max(350.0, distanceToBoss);
        }
        boomerangMaxDist = targetDist;

        boomerangGroup.setLayoutX(startX);
        boomerangGroup.setLayoutY(startY);
        boomerangStartX = startX;
        boomerangStartY = startY;

        boomerangActive = true;
        boomerangTime = 0.0;
        boomerangHasDamaged = false;

        // 播放拋擲迴力鏢音效
        SoundManager.playScore();

        // 把迴力鏢加到最上層，以免被其他節點遮蓋
        root.getChildren().add(boomerangGroup);
    }

    public Pane getView() {
        return root;
    }
}
