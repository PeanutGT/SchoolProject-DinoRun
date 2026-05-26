package com.dino;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import javafx.util.Duration;
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

public class GameScene {

    private Pane root;
    private DinoMain dinoMain;

    private ImageView ground1;
    private ImageView ground2;

    private ImageView cloud1;
    private ImageView cloud2;
    private ImageView cloud3;

    private Dino dino;

    private ArrayList<ObstacleSlot> obstacles;

    private ScoreDisplay scoreDisplay;
    private static int sessionHighScore = 0;

    private int score = 0;
    private int lastScoreSound = 0;
    @SuppressWarnings("unused")
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
    private Button menuButtonGameOver;
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

    private boolean jumpPressed = false;
    private boolean jumpAfterRestart = false;

    // Game Clock
    private long activeGameTime = 0;
    private long lastFrameTime = 0;

    // 引入 GameConfig 常數
    private final double acceleration = GameConfig.ACCELERATION;
    private final double maxSpeed = GameConfig.MAX_SPEED;

    private double distance = 0;
    
    private ImageView coinIconHud;
    private Label coinCountLabel;
    public GameScene(DinoMain dinoMain) {
        this(dinoMain, GameConfig.selectedCharacter);
    }

    private void createCoinHud() {
        coinIconHud = new ImageView(ResourceManager.getImage("tool/coin.png"));
        coinIconHud.setSmooth(true);
        coinIconHud.setFitWidth(28);
        coinIconHud.setPreserveRatio(true);

        coinCountLabel = new Label(String.valueOf(SaveManager.getMoney()));
        coinCountLabel.setFont(Font.font("Menlo", 18));
        coinCountLabel.setTextFill(Color.BLACK);

        javafx.scene.layout.HBox coinHud = new javafx.scene.layout.HBox(6, coinIconHud, coinCountLabel);
        coinHud.setAlignment(Pos.CENTER_RIGHT);
        coinHud.setLayoutX(screenWidth - 130);
        coinHud.setLayoutY(12);

        root.getChildren().add(coinHud);
    }

    private void updateCoinHud() {
        if (coinCountLabel != null) {
            coinCountLabel.setText(String.valueOf(SaveManager.getMoney()));
        }
    }

    private void showCoinPopup(double x, double y) {
        ImageView coin = new ImageView(ResourceManager.getImage("tool/coin.png"));
        coin.setFitWidth(24);
        coin.setPreserveRatio(true);
        coin.setLayoutX(x - 12);
        coin.setLayoutY(y - 20);

        Label plus = new Label("+1$");
        plus.setFont(Font.font(16));
        plus.setTextFill(Color.web("#FFCC00"));
        plus.setLayoutX(x + 10);
        plus.setLayoutY(y - 18);

        root.getChildren().addAll(coin, plus);

        TranslateTransition tt = new TranslateTransition(Duration.millis(800), coin);
        tt.setByY(-50);
        FadeTransition ft = new FadeTransition(Duration.millis(800), coin);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        TranslateTransition tt2 = new TranslateTransition(Duration.millis(800), plus);
        tt2.setByY(-50);
        FadeTransition ft2 = new FadeTransition(Duration.millis(800), plus);
        ft2.setFromValue(1.0);
        ft2.setToValue(0.0);

        tt.setOnFinished(ev -> {
            root.getChildren().removeAll(coin, plus);
        });

        tt.play();
        ft.play();
        tt2.play();
        ft2.play();
    }

    public GameScene(DinoMain dinoMain, String character) {
        this.dinoMain = dinoMain;

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

        restartImage.setOnMouseClicked(e -> {
            if (gameOver) {
                restartGame();
            }
        });

        menuButtonGameOver = new Button("返回主選單");
        // 初始位置留空，於 gameOver() 時動態置中於重開按鈕下方
        menuButtonGameOver.setLayoutX(0);
        menuButtonGameOver.setLayoutY(0);
        menuButtonGameOver.setVisible(false);
        menuButtonGameOver.setOnAction(e -> {
            if (timer != null) timer.stop();
            dinoMain.showMainMenu();
        });

        scoreDisplay = new ScoreDisplay();
        heartDisplay = new HeartDisplay();
        skillDisplay = new SkillDisplay();
        questionBlocks = new ArrayList<>();

        dino = new Dino(100, GameConfig.GROUND_Y, character);

        milkFog = new Rectangle(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, Color.rgb(255, 255, 255, 0.7));
        milkFog.setVisible(false);


        obstacles = new ArrayList<>();

        obstacles.add(new ObstacleSlot(850, groundY));
        obstacles.add(new ObstacleSlot(1150, groundY));
        obstacles.add(new ObstacleSlot(1450, groundY));

        root.getChildren().addAll(
                cloud1,
                cloud2,
                cloud3,
                gameOverImage,
                restartImage,
            menuButtonGameOver,
                ground1,
                ground2,
                dino.getView()
        );

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
                scoreDisplay.getView(),
                skillDisplay.getView()
        );

        // 建立並顯示右上角金幣 HUD
        createCoinHud();

        createPauseOverlay();
        root.getChildren().add(pauseOverlay);

        startGameLoop();
    }

    private void createPauseOverlay() {
        pauseOverlay = new StackPane();
        pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        pauseOverlay.setPrefSize(screenWidth, 600); // 確保能覆蓋整個畫面

        VBox pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);

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
            if (timer != null) timer.stop();
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
            if (activeGameTime - barrierStartTime >= GameConfig.BARRIER_DURATION_MS) {
                barrierActive = false;
                dino.getView().setOpacity(1.0);
            }
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

        if (bossPhase && boss != null) {
            boss.update(speed, activeGameTime, dtSeconds);
            if (boss.isDefeated(activeGameTime)) {
                bossPhase = false;
                boss.removeAllProjectiles();
                boss = null;
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
                if (barrierActive) return; // 屏障啟動時免傷
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
        gameOver = true;
        if (score > sessionHighScore) {
            sessionHighScore = score;
        }
        scoreDisplay.update(score, sessionHighScore);
        dino.die();
        gameOverImage.setVisible(true);
        restartImage.setVisible(true);
        if (menuButtonGameOver != null) {
            // 強制計算大小以取得正確寬度，然後置中於 restartImage 正下方
            menuButtonGameOver.applyCss();
            menuButtonGameOver.layout();
            double btnWidth = menuButtonGameOver.getWidth();
            double rx = restartImage.getX();
            double rwidth = restartImage.getBoundsInParent().getWidth();
            double rheight = restartImage.getBoundsInParent().getHeight();
            double btnX = rx + rwidth / 2.0 - btnWidth / 2.0;
            double btnY = restartImage.getY() + rheight + 8; // 8px 間距
            menuButtonGameOver.setLayoutX(btnX);
            menuButtonGameOver.setLayoutY(btnY);
            menuButtonGameOver.setVisible(true);
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
        boss = createBossInstance(activeGameTime);
        
        screenFlash.setVisible(true);
        screenFlashStartTime = activeGameTime;
    }

    private Boss createBossInstance(long activeGameTime) {
        // 未來可在這裡決定要產生哪種 Boss 類型
        return new Boss(root, activeGameTime, screenWidth - 150);
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
    }

    private void updateClouds(double dtSeconds) {
        double cloudSpeed = speed * 0.25 * dtSeconds;

        cloud1.setX(cloud1.getX() - cloudSpeed);
        cloud2.setX(cloud2.getX() - cloudSpeed);
        cloud3.setX(cloud3.getX() - cloudSpeed);

        if (cloud1.getX() < -100) resetCloud(cloud1);
        if (cloud2.getX() < -100) resetCloud(cloud2);
        if (cloud3.getX() < -100) resetCloud(cloud3);
    }

    private void updateScore(double dtSeconds) {
        distance += speed * dtSeconds;
        int newScore = (int)(distance / 50);

        if (newScore > score) {
            score = newScore;
            if (score > 0 && score / 100 > lastScoreSound / 100) {
                lastScoreSound = score;
                SoundManager.playScore();
                scoreDisplay.flashCurrentScore(score);
            }
            
            if (score > 0 && score % GameConfig.QUESTION_BLOCK_INTERVAL == 0 && score != lastQuestionBlockScore) {
                lastQuestionBlockScore = score;
                QuestionBlock qb = new QuestionBlock(screenWidth, groundY);
                questionBlocks.add(qb);
                root.getChildren().add(qb.getView());
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
                // Allow returning to main menu when game is over; otherwise toggle pause
                if (gameOver) {
                    if (timer != null) timer.stop();
                    dinoMain.showMainMenu();
                } else {
                    togglePause();
                }
                return;
            }
            if (isPaused) return;

            if (e.getCode() == KeyCode.W && !jumpPressed) {
                jumpPressed = true;
                if (waitingToStart) {
                    waitingToStart = false;
                    if (dino.jump()) {
                        SoundManager.playJump();
                    }
                    return;
                }
                if (gameOver) {
                    restartGame();
                    waitingToStart = false;
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
            if (e.getCode() == KeyCode.S && !gameOver) {
                dino.pressDown();
            }

            // 技能觸發區塊
            if (e.getCode() == KeyCode.Q && GameConfig.goldenAppleCount > 0) {
                GameConfig.goldenAppleCount--;
                dino.healToFull();
                heartDisplay.update(dino.getLives());
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
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (isPaused) return;

            if (e.getCode() == KeyCode.W) {
                jumpPressed = false;
                if (!gameOver) {
                    dino.releaseJump();
                }
            }
            if (e.getCode() == KeyCode.S && !gameOver) {
                dino.releaseDown();
            }
        });
    }

    private void togglePause() {
        if (gameOver) return;

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            pauseOverlay.setVisible(true);
        } else {
            pauseOverlay.setVisible(false);
            root.requestFocus(); // 將焦點還給遊戲容器
            lastFrameTime = 0; // 重置時間計算
            timer.start();
        }
    }

    private void restartGame() {
        gameOver = false;
        score = 0;
        distance = 0;
        frameCount = 0;
        speed = GameConfig.INITIAL_SPEED; // 套用常數
        lastScoreSound = 0;
        jumpAfterRestart = false;
        waitingToStart = true;
        activeGameTime = 0;
        lastFrameTime = 0;

        if (boss != null) {
            boss.removeAllProjectiles();
            boss = null;
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
        
        GameConfig.goldenAppleCount = 0;
        GameConfig.milkBucketCount = 0;
        GameConfig.enchantedBookCount = 0;
        GameConfig.barrierCount = 0;
        GameConfig.woodenSwordCount = 0;
        skillDisplay.update();

        gameOverImage.setVisible(false);
        restartImage.setVisible(false);
        if (menuButtonGameOver != null) menuButtonGameOver.setVisible(false);

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
        // 改為直接給予玩家局外金錢（1$）
        SaveManager.addMoney(1);
        skillDisplay.update();
        SoundManager.playScore();
        updateCoinHud();
        // 在角色位置顯示金幣跳起來的動畫
        Bounds b = dino.getHitBoxBounds();
        double popupX = b.getMinX() + b.getWidth() / 2.0;
        double popupY = b.getMinY();
        showCoinPopup(popupX, popupY);
    }

    @SuppressWarnings("unused")
    private void clearObstaclesInFront() {
        double dinoMaxX = dino.getHitBoxBounds().getMaxX();
        double attackRangeMaxX = dinoMaxX + GameConfig.SWORD_ATTACK_RANGE;

        for (ObstacleSlot obstacle : obstacles) {
            double obsMinX = obstacle.getHitBoxBounds().getMinX();
            // 在攻擊範圍內的障礙物全清
            if (obsMinX >= dinoMaxX && obsMinX <= attackRangeMaxX) {
                resetObstacle(obstacle);
                distance += GameConfig.OBSTACLE_CLEAR_SCORE * 50;
            }
        }
    }

    private boolean isCloudOverlapping(ImageView targetCloud, double newX, double newY) {
        ImageView[] clouds = {cloud1, cloud2, cloud3};
        double cloudWidth = targetCloud.getBoundsInLocal().getWidth();
        double cloudHeight = targetCloud.getBoundsInLocal().getHeight();

        for (ImageView cloud : clouds) {
            if (cloud == targetCloud) continue;
            double otherX = cloud.getX();
            double otherY = cloud.getY();
            double otherWidth = cloud.getBoundsInLocal().getWidth();
            double otherHeight = cloud.getBoundsInLocal().getHeight();

            boolean overlapX = newX < otherX + otherWidth + 80 && newX + cloudWidth + 80 > otherX;
            boolean overlapY = newY < otherY + otherHeight + 30 && newY + cloudHeight + 30 > otherY;

            if (overlapX && overlapY) return true;
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

    public Pane getView() {
        return root;
    }
}
