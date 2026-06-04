package com.dino;

import javafx.geometry.Bounds;
import javafx.scene.shape.Circle;
import javafx.animation.RotateTransition;

import com.dino.Boss;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;

import javafx.animation.AnimationTimer;
import javafx.animation.Animation;
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

/**
 * 雙人合作遊戲場景類別。
 * 負責掌控雙人模式的核心遊戲迴圈（AnimationTimer），
 * 管理兩位玩家（Player One & Player Two）的物理狀態（例如各自控制起跳、下蹲）、
 * 彼此接觸以救起幽靈（Revive）、使用金蘋果解救伴侶（Rescue）、雙人各自的生命 UI 顯示、
 * 合作模式 Boss 戰、雙迴力鏢拋出軌跡，以及雙人合作排行榜登錄。
 */
public class CoopGameScene {

    private Pane root;              // 遊戲場景根畫布
    private DinoMain dinoMain;      // 遊戲主入口參考

    // 循環捲動的拼接地面
    private ImageView ground1;
    private ImageView ground2;

    // 循環捲動的雲朵
    private ImageView cloud1;
    private ImageView cloud2;
    private ImageView cloud3;

    // 雙人合作玩家實體
    private Dino playerOne;
    private Dino playerTwo;
    private String currentCharacter;  // 玩家一所選角色
    private String currentCharacter2; // 玩家二所選角色

    private ArrayList<ObstacleSlot> obstacles; // 障礙物插槽清單
    private Label signpost;         // 起點說明牌

    private ScoreDisplay scoreDisplay;        // 頂部分數顯示
    private static int sessionHighScore = 0;  // 單次執行最高分

    private int score = 0;           // 當前分數
    private int lastScoreSound = 0;  // 上次播放分數突破音效之分數
    private int frameCount = 0;      // 影格計數器

    private double speed = GameConfig.INITIAL_SPEED; // 目前速度

    // 快取全域常數
    private final double screenWidth = GameConfig.SCREEN_WIDTH;
    private final double groundY = GameConfig.GROUND_Y;
    private final double groundImageY = GameConfig.GROUND_IMAGE_Y;
    private final double groundWidth = GameConfig.SCREEN_WIDTH;

    private AnimationTimer timer;   // 動畫計時器

    // 結算 UI
    private ImageView gameOverImage;
    private ImageView restartImage;
    private Button gameOverMenuBtn;
    private boolean gameOver = false;

    private Rectangle screenFlash;           // 紅閃遮罩

    private StackPane pauseOverlay;  // 暫停面板
    private boolean isPaused = false;        // 是否暫停
    private boolean waitingToStart = true;   // 是否等待開始

    // 雙人獨立生命顯示
    private HeartDisplay playerOneHearts;
    private HeartDisplay playerTwoHearts;
    private long screenFlashStartTime = 0;
    private boolean barrierActive = false;   // 護盾狀態
    
    // 合作模式 Boss 戰狀態
    private Boss boss;
    private boolean bossPhase = false;
    private boolean bossIncoming = false;
    private boolean bossHasAppeared = false;
    private int nextBossScore = GameConfig.BOSS_TRIGGER_SCORE_COOP; // 雙人合作 Boss 觸發分數
    private boolean inBossGracePeriod = false;                     // Boss 戰後的安全緩衝期
    private double bossGracePeriodTimer = 0.0;
    private Pane bossHealthBarContainer;
    private Rectangle bossHealthInnerBar;
    private Label bossHealthLabel;

    // 雙人金蘋果圖示 UI (有此圖示代表能按 Shift 救活已變幽靈的隊友)
    private ImageView p1AppleIcon;
    private ImageView p2AppleIcon;

    // 合作模式迴力鏢變數 (雙人各自按 Space/Enter 拋出)
    private boolean boomerangActive = false;
    private Circle boomerangHitBox;
    private ImageView boomerangView;
    private boolean boomerangReturning = false;
    private double boomerangBaseX;
    private double boomerangBaseY;
    private double boomerangTargetX;
    private double boomerangTargetY;
    private double boomerangProgress = 0.0;
    private boolean boomerangHasDamaged = false;
    private double boomerangMaxDist = 350.0;
    private RotateTransition boomerangRotate;

    private List<Coin> coinsList;            // 金幣清單
    private CoinDisplay coinDisplay;         // 金幣顯示
    private int sessionCoins = 0;            // 累積金幣
    private int lastCoinSpawnScore = 0;      // 上次生成金幣的分數

    private boolean spacePressed = false;    // 防止 P1 按鍵連發
    private boolean upPressed = false;       // 防止 P2 按鍵連發
    private boolean jumpAfterRestart = false;// 重新開始自動跳躍

    // 微秒時鐘與自動回血
    private long activeGameTime = 0;
    private long lastFrameTime = 0;
    private double regenTimer = 0.0;

    private final double acceleration = GameConfig.ACCELERATION;
    private final double maxSpeed = GameConfig.MAX_SPEED;

    private double distance = 0;             // 累積奔跑距離

    /**
     * 建構子：使用預設角色。
     */
    public CoopGameScene(DinoMain dinoMain) {
        this(dinoMain, GameConfig.selectedCharacter, GameConfig.selectedCharacter);
    }

    /**
     * 完整建構子：初始化雙人合作面板、雙恐龍載入、心心容器擺放、金蘋果 UI 設定以及按鍵起跳提示。
     * @param dinoMain 遊戲主程式
     * @param playerOneCharacter 玩家一角色代號
     * @param playerTwoCharacter 玩家二角色代號
     */
    public CoopGameScene(DinoMain dinoMain, String playerOneCharacter, String playerTwoCharacter) {
        this.dinoMain = dinoMain;
        this.currentCharacter = playerOneCharacter;
        this.currentCharacter2 = playerTwoCharacter;

        root = new Pane();
        root.setStyle("-fx-background-color: white;");

        // 初始化背景與拼接地面
        createClouds();
        createGround();

        // 載入遊戲結束與重玩按鈕圖檔
        Image gameOverPic = ResourceManager.getImage("gameover.png");
        gameOverImage = new ImageView(gameOverPic);
        gameOverImage.setSmooth(false);
        gameOverImage.setFitWidth(300);
        gameOverImage.setPreserveRatio(true);
        gameOverImage.setX(350);
        gameOverImage.setY(120);
        gameOverImage.setVisible(false);

        Image restartPic = ResourceManager.getImage("restart.png");
        restartImage = new ImageView(restartPic);
        restartImage.setSmooth(false);
        restartImage.setFitWidth(40);
        restartImage.setPreserveRatio(true);
        restartImage.setX(470);
        restartImage.setY(170);
        restartImage.setVisible(false);

        gameOverMenuBtn = new Button("返回");
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

        // 分數、金幣與雙角色載入。P1 高度與位置略在前，P2 略在後，並設為 82% 透明度以利在視覺上區分彼此
        scoreDisplay = new ScoreDisplay();
        coinDisplay = new CoinDisplay();
        coinsList = new ArrayList<>();
        playerOne = new Dino(110, GameConfig.GROUND_Y, playerOneCharacter);
        playerTwo = new Dino(40, GameConfig.GROUND_Y, playerTwoCharacter);
        playerTwo.getView().setOpacity(0.82);

        // 雙玩家各自生命血量 UI 高度錯開 (P1:Y15, P2:Y45)
        playerOneHearts = new HeartDisplay(playerOne.getMaxLives());
        playerOneHearts.getView().setLayoutY(15);
        playerTwoHearts = new HeartDisplay(playerTwo.getMaxLives());
        playerTwoHearts.getView().setLayoutY(45);

        signpost = new Label("【雙人合作說明】\nP1: [W] 跳躍 [S] 蹲下\nP2: [↑] 跳躍 [↓] 蹲下\n道具: 依照畫面提示操作");
        signpost.setStyle(
                "-fx-background-color: #8B4513; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-font-family: 'Courier New', monospace; -fx-padding: 10; -fx-font-weight: bold;");
        signpost.setLayoutX(300);
        signpost.setLayoutY(GameConfig.GROUND_Y - 120);

        // 初始化兩位玩家頭像邊的金蘋果圖案 UI
        Image appleImg = ResourceManager.getImage("tool/golden_apple.png");
        p1AppleIcon = new ImageView(appleImg);
        p1AppleIcon.setFitWidth(20);
        p1AppleIcon.setPreserveRatio(true);
        p1AppleIcon.setLayoutX(100);
        p1AppleIcon.setLayoutY(15);
        p1AppleIcon.setVisible(false);

        p2AppleIcon = new ImageView(appleImg);
        p2AppleIcon.setFitWidth(20);
        p2AppleIcon.setPreserveRatio(true);
        p2AppleIcon.setLayoutX(100);
        p2AppleIcon.setLayoutY(45);
        p2AppleIcon.setVisible(false);

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
                playerOne.getView(),
                playerTwo.getView());

        screenFlash = new Rectangle(screenWidth, GameConfig.SCREEN_HEIGHT, Color.rgb(255, 0, 0, 0.5));
        screenFlash.setVisible(false);
        root.getChildren().add(screenFlash);

        for (ObstacleSlot obstacle : obstacles) {
            root.getChildren().add(obstacle.getCactus().getView());
            root.getChildren().add(obstacle.getBird().getView());
        }

        root.getChildren().addAll(
                playerOneHearts.getView(),
                p1AppleIcon,
                playerTwoHearts.getView(),
                p2AppleIcon,
                coinDisplay.getView(),
                scoreDisplay.getView(),
                gameOverImage,
                restartImage,
                gameOverMenuBtn);

        createPauseOverlay();
        root.getChildren().add(pauseOverlay);

        playerOne.showHint("按上鍵或W鍵開始遊戲！");
        startGameLoop();
    }

    /**
     * 建立暫停選單。
     */
    private void createPauseOverlay() {
        pauseOverlay = new StackPane();
        pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        pauseOverlay.setPrefSize(screenWidth, GameConfig.SCREEN_HEIGHT);

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
        Button menuBtn = new Button("返回");

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

    /**
     * 載入並定位雲朵。
     */
    private void createClouds() {
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

    /**
     * 載入並定位拼接地面。
     */
    private void createGround() {
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

    /**
     * 啟動動畫計時器。
     */
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

    /**
     * 合作模式每格影格更新邏輯。
     * @param dtSeconds 經過秒數
     */
    private void update(double dtSeconds) {
        if (gameOver) {
            return;
        }

        if (waitingToStart) {
            return;
        }

        if (jumpAfterRestart) {
            if (playerOne.jump() | playerTwo.jump()) {
                SoundManager.playJump();
            }
            jumpAfterRestart = false;
        }

        if (screenFlash.isVisible()) {
            if (activeGameTime - screenFlashStartTime >= 500) {
                screenFlash.setVisible(false);
            }
        }

        frameCount++;

        updateSpeed(dtSeconds);
        updateScore(dtSeconds);

        playerOne.update(activeGameTime, dtSeconds);
        playerTwo.update(activeGameTime, dtSeconds);
        
        // ⭐ 雙人合作特色救援機制：當一玩家變為幽靈，且另一玩家仍存活時，
        // 存活者只要在奔跑中接觸到幽靈的碰撞體，即可立刻以 1HP 救活隊友 (Revive)！
        if (playerOne.isGhost() && !playerTwo.isGhost() && playerOne.getHitBoxBounds().intersects(playerTwo.getHitBoxBounds())) { 
            playerOne.revive(activeGameTime); 
        } else if (playerTwo.isGhost() && !playerOne.isGhost() && playerTwo.getHitBoxBounds().intersects(playerOne.getHitBoxBounds())) { 
            playerTwo.revive(activeGameTime); 
        }

        // 磁鐵金幣吸引：以當前仍存活（非幽靈）的恐龍作為金幣磁鐵的吸力中心
        Dino targetPlayer = (!playerOne.isGhost()) ? playerOne : playerTwo;
        double dinoCenterX = targetPlayer.getView().getLayoutX() + targetPlayer.getView().getBoundsInLocal().getWidth() / 2.0;
        double dinoCenterY = targetPlayer.getView().getLayoutY() + targetPlayer.getView().getBoundsInLocal().getHeight() / 2.0;
        double magnetRadius = SaveManager.getMagnetRadius();

        // 遍歷金幣與雙人獨立碰撞
        java.util.Iterator<Coin> coinIt = coinsList.iterator();
        while (coinIt.hasNext()) {
            Coin coin = coinIt.next();
            coin.update(speed, dtSeconds, dinoCenterX, dinoCenterY, magnetRadius);
            
            boolean hitP1 = !playerOne.isGhost() && coin.getHitBoxBounds().intersects(playerOne.getHitBoxBounds());
            boolean hitP2 = !playerTwo.isGhost() && coin.getHitBoxBounds().intersects(playerTwo.getHitBoxBounds());
            
            if (hitP1 || hitP2) {
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

        // 自動回血（商店加成），依次遞補受傷的恐龍
        if (SaveManager.hasRegen() && !playerOne.isDead() && playerOne.getLives() < playerOne.getMaxLives()) {
            regenTimer += dtSeconds;
            double targetTime = 40.0;
            if (SaveManager.getRegenLevel() == 2) targetTime = 20.0;
            else if (SaveManager.getRegenLevel() == 3) targetTime = 10.0;

            if (regenTimer >= targetTime) {
                regenTimer = 0.0;
                if (playerOne.getLives() < playerOne.getMaxLives()) {
                    playerOne.healOne();
                    playerOneHearts.update(playerOne.getLives());
                } else if (playerTwo.getLives() < playerTwo.getMaxLives()) {
                    playerTwo.healOne();
                    playerTwoHearts.update(playerTwo.getLives());
                }
                showFloatingText("+1 HP", playerOne.getView().getLayoutX() + 20, playerOne.getView().getLayoutY() - 30);
            }
        }
        
        // 合作模式 Boss 戰行為更新
        if (bossPhase && boss != null) {
            boss.update(speed, activeGameTime, dtSeconds);
            bossHealthInnerBar.setWidth(296 * ((double) boss.getHp() / boss.getMaxHp()));
            bossHealthLabel.setText("BOSS: " + boss.getName() + " (" + boss.getHp() + "/" + boss.getMaxHp() + ")");

            if (!bossHasAppeared && boss.getX() < screenWidth - 100) {
                bossHasAppeared = true;
            }

            // 合作 Boss 被擊敗：額外獲得大量距離分數，並且必獎賞雙人各一顆金蘋果
            if (boss.isDefeated(activeGameTime)) {
                bossPhase = false;
                boss.removeAllProjectiles();
                boss = null;
                inBossGracePeriod = true;
                bossGracePeriodTimer = 0.0;
                root.getChildren().remove(bossHealthBarContainer);
                bossHealthBarContainer = null;
                distance += 2000 * 50;
                SoundManager.playScore();
                
                // 存活者獲得復活用金蘋果圖示與技能
                if (!playerOne.isGhost()) {
                    playerOne.setHasGoldenApple(true);
                    p1AppleIcon.setVisible(true);
                }
                if (!playerTwo.isGhost()) {
                    playerTwo.setHasGoldenApple(true);
                    p2AppleIcon.setVisible(true);
                }
                showFloatingText("GOLDEN APPLE!", playerOne.getView().getLayoutX(), playerOne.getView().getLayoutY() - 50);
            }
        } else if (inBossGracePeriod) {
            bossGracePeriodTimer += dtSeconds;
            if (bossGracePeriodTimer >= GameConfig.BOSS_RETREAT_GRACE_PERIOD_MS_COOP / 1000.0) {
                inBossGracePeriod = false;
                resetAllObstacles();
            }
        } else {
            // 普通狀態移動障礙物並檢測碰撞
            updateObstacles(dtSeconds);
            checkCollision();
        }

        // Boss 戰期間碰撞檢測
        if (bossPhase && boss != null) {
            checkBossCollision();
        } else {
            // 到達雙人 Boss 分數
            if (score >= nextBossScore) {
                bossIncoming = true;
                nextBossScore += GameConfig.BOSS_INTERVAL_SCORE_COOP;
            }
        }

        // 更新飛行的骨頭迴力鏢與捲動
        updateBoomerang(dtSeconds);
        updateGround(dtSeconds);
        updateClouds(dtSeconds);
    }

    /**
     * 合作模式下拋出迴力鏢，會追蹤按鍵發起者。
     * @param thrower 拋擲此發迴力鏢的 Dino 實體
     */
    private void throwBoomerang(Dino thrower) {
        if (boomerangActive) return;
        
        boomerangActive = true;
        boomerangReturning = false;
        boomerangHasDamaged = false;
        boomerangProgress = 0.0;
        
        // 設定迴力鏢起點
        boomerangBaseX = thrower.getView().getLayoutX() + 42.0 / 2;
        boomerangBaseY = thrower.getView().getLayoutY() + 45.0 / 2;
        
        double targetDist = 350.0;
        if (bossPhase && boss != null) {
            double bossX = boss.getX() + 42.0;
            double dinoX = thrower.getView().getLayoutX() + 42.0 / 2.0;
            double distanceToBoss = (bossX - dinoX) + 20.0;
            targetDist = Math.max(350.0, distanceToBoss);
        }
        boomerangMaxDist = targetDist;
        
        boomerangTargetX = boomerangBaseX + boomerangMaxDist;
        boomerangTargetY = boomerangBaseY;
        
        if (boomerangView == null) {
            Image boneImg = ResourceManager.getImage("bone.png");
            boomerangView = new ImageView(boneImg);
            boomerangView.setFitWidth(30);
            boomerangView.setPreserveRatio(true);
            
            boomerangHitBox = new Circle(15);
            boomerangHitBox.setFill(Color.TRANSPARENT);
            
            boomerangRotate = new RotateTransition(Duration.millis(300), boomerangView);
            boomerangRotate.setByAngle(360);
            boomerangRotate.setCycleCount(Animation.INDEFINITE);
        }
        
        boomerangView.setLayoutX(boomerangBaseX - 15);
        boomerangView.setLayoutY(boomerangBaseY - 15);
        boomerangHitBox.setLayoutX(boomerangBaseX);
        boomerangHitBox.setLayoutY(boomerangBaseY);
        
        root.getChildren().addAll(boomerangView, boomerangHitBox);
        boomerangRotate.play();
        SoundManager.playJump();
    }
    
    /**
     * 更新雙人模式迴力鏢位移與受傷碰撞。
     */
    private void updateBoomerang(double dtSeconds) {
        if (!boomerangActive) return;

        double flightSpeed = 800.0 * dtSeconds;
        
        if (!boomerangReturning) {
            boomerangProgress += flightSpeed;
            if (boomerangProgress >= boomerangMaxDist) {
                boomerangProgress = boomerangMaxDist;
                boomerangReturning = true;
            }
        } else {
            // 返回至原拋出基準點
            boomerangProgress -= flightSpeed;
            if (boomerangProgress <= 0) {
                boomerangProgress = 0;
                boomerangActive = false;
                boomerangRotate.stop();
                root.getChildren().removeAll(boomerangView, boomerangHitBox);
                return;
            }
        }
        
        double currentX = boomerangBaseX + boomerangProgress;
        boomerangView.setLayoutX(currentX - 15);
        boomerangHitBox.setLayoutX(currentX);

        if (!boomerangHasDamaged && bossPhase && boss != null) {
            if (boomerangHitBox.localToScene(boomerangHitBox.getBoundsInLocal())
                    .intersects(boss.getHitBoxBounds())) {
                boss.takeDamage(10);
                boomerangHasDamaged = true;
                SoundManager.playHit();
                showFloatingText("-10 HP", boss.getHitBoxBounds().getCenterX() - 20, boss.getHitBoxBounds().getMinY() - 20);
            }
        }
    }

    /**
     * 啟動合作 Boss 登場。
     */
    private void triggerBossPhase() {
        bossIncoming = false;
        bossPhase = true;
        bossHasAppeared = false;
        boss = Boss.spawnRandomBoss(root, activeGameTime, true);

        for (ObstacleSlot obstacle : obstacles) {
            obstacle.reset(-200, score, groundY);
        }

        screenFlash.setFill(Color.rgb(255, 0, 0, 0.5));
        screenFlash.setVisible(true);
        screenFlashStartTime = activeGameTime;

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

        bossHealthLabel = new Label("BOSS: " + boss.getName() + " (" + boss.getHp() + "/" + boss.getMaxHp() + ")");
        bossHealthLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        bossHealthLabel.setTextFill(Color.WHITE);
        bossHealthLabel.setLayoutY(-18);
        bossHealthLabel.setLayoutX(0);
        bossHealthLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 2, 0, 1, 1);");

        bossHealthBarContainer.getChildren().addAll(bgBar, bossHealthInnerBar, bossHealthLabel);
        
        root.getChildren().add(bossHealthBarContainer);
    }

    /**
     * 檢測雙玩家與 Boss 碰撞，幽靈則豁免傷害。
     */
    private void checkBossCollision() {
        if (bossPhase && boss != null && bossHasAppeared) {
            if (!playerOne.isGhost() && boss.checkCollision(playerOne.getHitBoxBounds())) {
                boolean damaged = playerOne.hit(activeGameTime);
                if (damaged) {
                    SoundManager.playHit();
                    playerOneHearts.update(playerOne.getLives());
                    if (playerOne.isDead()) playerOne.becomeGhost();
                }
            }
            if (!playerTwo.isGhost() && boss.checkCollision(playerTwo.getHitBoxBounds())) {
                boolean damaged = playerTwo.hit(activeGameTime);
                if (damaged) {
                    SoundManager.playHit();
                    playerTwoHearts.update(playerTwo.getLives());
                    if (playerTwo.isDead()) playerTwo.becomeGhost();
                }
            }
        }
    }

    /**
     * 檢測普通障礙物碰撞。雙玩家各自判斷，若兩名玩家同時均變為幽靈，方宣告 Game Over。
     */
    private void checkCollision() {
        for (ObstacleSlot obstacle : obstacles) {
            checkObstacleCollision(playerOne, obstacle);
            checkObstacleCollision(playerTwo, obstacle);
        }
        // 若兩人皆死亡變為幽靈，則遊戲結束
        if (playerOne.isGhost() && playerTwo.isGhost()) {
            gameOver();
        }
    }
    
    /**
     * 輔助判斷單個玩家與障礙物的碰撞。
     */
    private void checkObstacleCollision(Dino player, ObstacleSlot obstacle) {
        if (player.isGhost()) return;
        if (player.getHitBoxBounds().intersects(obstacle.getHitBoxBounds())) {
            boolean damaged = player.hit(activeGameTime);
            if (damaged) {
                SoundManager.playHit();
                if (player == playerOne) playerOneHearts.update(player.getLives());
                else playerTwoHearts.update(player.getLives());
                if (player.isDead()) {
                    player.becomeGhost(); // 死亡時不宣告 Game Over，而是轉為幽靈
                }
            }
        }
    }

    /**
     * 合作模式結算與排行榜。
     */
    private void gameOver() {
        SoundManager.stopGameBgm();
        gameOver = true;
        if (score > sessionHighScore) {
            sessionHighScore = score;
        }
        scoreDisplay.update(score, sessionHighScore);
        playerOne.die();
        playerTwo.die();

        if (LeaderboardManager.isHighScore(score, true)) {
            Platform.runLater(() -> {
                TextInputDialog dialog = new TextInputDialog("玩家");
                dialog.setTitle("新紀錄");
                dialog.setHeaderText("遊戲結束");
                dialog.setContentText("請輸入您的名字：");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    String cleanName = name.replace(",", "").trim();
                    if (cleanName.isEmpty())
                        cleanName = "Unknown";
                    // 排行榜登錄，傳入兩個角色代號，標記 coop 為 true
                    LeaderboardManager.addScore(cleanName, score, currentCharacter, currentCharacter2, true);
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
        double minDistance = GameConfig.OBSTACLE_MIN_DISTANCE_BASE + speed * GameConfig.OBSTACLE_DISTANCE_SPEED_RATIO;
        double randomDistance = Math.random() * GameConfig.OBSTACLE_MAX_RANDOM_DISTANCE;
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
                root.getChildren().add(coin.getView());
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

    /**
     * 綁定雙人合作模式鍵盤配置。
     * 玩家一 (P1): W 跳躍、S 蹲下、Space 丟迴力鏢
     * 玩家二 (P2): Up 跳躍、Down 蹲下、Enter 丟迴力鏢
     * 金蘋果拯救 (Shift): 若某玩家持有金蘋果，且另一人已變幽靈，按 Shift 會消耗蘋果並將隊友全血復活 (Rescue)！
     */
    public void setKeyControl(Scene scene) {
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
        sizeListener.changed(null, null, null);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePause();
                return;
            }
            if (isPaused)
                return;

            // W 鍵：Player One 跳躍 / 開始遊戲
            if (e.getCode() == KeyCode.W && !spacePressed) {
                spacePressed = true;
                if (waitingToStart) {
                    waitingToStart = false;
                    playerOne.hideHint();
                    SoundManager.playGameBgm();
                    if (playerOne.jump()) SoundManager.playJump();
                    return;
                }
                if (gameOver) {
                    restartGame();
                    waitingToStart = false;
                    playerOne.hideHint();
                    SoundManager.playGameBgm();
                    if (playerOne.jump()) SoundManager.playJump();
                } else {
                    if (playerOne.jump()) {
                        SoundManager.playJump();
                    }
                }
            }

            // Up 鍵：Player Two 跳躍 / 開始遊戲
            if (e.getCode() == KeyCode.UP && !upPressed) {
                upPressed = true;
                if (waitingToStart) {
                    waitingToStart = false;
                    playerOne.hideHint();
                    SoundManager.playGameBgm();
                    if (playerTwo.jump()) SoundManager.playJump();
                    return;
                }
                if (gameOver) {
                    restartGame();
                    waitingToStart = false;
                    playerOne.hideHint();
                    SoundManager.playGameBgm();
                    if (playerTwo.jump()) SoundManager.playJump();
                } else {
                    if (playerTwo.jump()) {
                        SoundManager.playJump();
                    }
                }
            }

            if (waitingToStart) {
                return;
            }
            // S 鍵與 Down 鍵：P1 & P2 蹲下
            if (e.getCode() == KeyCode.S && !gameOver) {
                playerOne.pressDown();
            }
            if (e.getCode() == KeyCode.DOWN && !gameOver) {
                playerTwo.pressDown();
            }

            // ⭐ Shift 鍵：消耗金蘋果拯救死亡變幽靈的夥伴
            if (e.getCode() == KeyCode.SHIFT) {
                // P1 救 P2
                if (playerOne.getHasGoldenApple() && playerTwo.isGhost()) {
                    playerOne.setHasGoldenApple(false);
                    p1AppleIcon.setVisible(false);
                    playerTwo.revive(activeGameTime);
                    playerOne.healToFull();
                    playerOneHearts.update(playerOne.getLives());
                    playerTwoHearts.update(playerTwo.getLives());
                    SoundManager.playAppleSound();
                    showFloatingText("RESCUE!", playerOne.getView().getLayoutX(), playerOne.getView().getLayoutY() - 30);
                }
                // P2 救 P1
                if (playerTwo.getHasGoldenApple() && playerOne.isGhost()) {
                    playerTwo.setHasGoldenApple(false);
                    p2AppleIcon.setVisible(false);
                    playerOne.revive(activeGameTime);
                    playerTwo.healToFull();
                    playerOneHearts.update(playerOne.getLives());
                    playerTwoHearts.update(playerTwo.getLives());
                    SoundManager.playAppleSound();
                    showFloatingText("RESCUE!", playerTwo.getView().getLayoutX(), playerTwo.getView().getLayoutY() - 30);
                }
            }
            
            // Space 鍵：P1 拋出迴力鏢
            if (e.getCode() == KeyCode.SPACE) {
                if (bossPhase && boss != null) {
                    throwBoomerang(playerOne);
                }
            }
            // Enter 鍵：P2 拋出迴力鏢
            if (e.getCode() == KeyCode.ENTER) {
                if (bossPhase && boss != null) {
                    throwBoomerang(playerTwo);
                }
            }

            if (GameConfig.devModeEnabled) {
                if (e.getCode() == KeyCode.F1) {
                    playerOne.toggleDevInvincible();
                } else if (e.getCode() == KeyCode.F2) {
                    distance += 100 * 50;
                } else if (e.getCode() == KeyCode.F3) {
                    distance = 950 * 50;
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (isPaused)
                return;

            if (e.getCode() == KeyCode.W) {
                spacePressed = false;
                playerOne.releaseJump();
            }
            if (e.getCode() == KeyCode.UP) {
                upPressed = false;
                playerTwo.releaseJump();
            }

            if (e.getCode() == KeyCode.S) {
                playerOne.releaseDown();
            }
            if (e.getCode() == KeyCode.DOWN) {
                playerTwo.releaseDown();
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
            root.requestFocus();
            lastFrameTime = 0;
            timer.start();
            SoundManager.resumeGameBgm();
        }
    }

    /**
     * 重設關卡，清空一切場上附屬物，準備新局。
     */
    private void restartGame() {
        gameOver = false;
        score = 0;
        distance = 0;
        frameCount = 0;
        lastScoreSound = 0;
        jumpAfterRestart = false;
        waitingToStart = true;
        signpost.setLayoutX(300);
        playerOne.showHint("按上鍵或W鍵開始遊戲！");
        activeGameTime = 0;
        lastFrameTime = 0;

        barrierActive = false;
        playerOne.getView().setOpacity(1.0);
        playerTwo.getView().setOpacity(0.82);
        
        playerOne.setHasGoldenApple(false);
        p1AppleIcon.setVisible(false);
        playerTwo.setHasGoldenApple(false);
        p2AppleIcon.setVisible(false);
        
        bossPhase = false;
        bossIncoming = false;
        bossHasAppeared = false;
        nextBossScore = GameConfig.BOSS_TRIGGER_SCORE_COOP;
        inBossGracePeriod = false;
        if (boss != null) {
            boss.removeAllProjectiles();
            boss = null;
        }
        if (bossHealthBarContainer != null) {
            root.getChildren().remove(bossHealthBarContainer);
            bossHealthBarContainer = null;
        }

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

        gameOverImage.setVisible(false);
        restartImage.setVisible(false);
        gameOverMenuBtn.setVisible(false);

        playerOne.reset();
        playerTwo.reset();
        playerOneHearts.update(playerOne.getLives());
        playerTwoHearts.update(playerTwo.getLives());

        resetAllObstacles();
        scoreDisplay.update(score, sessionHighScore);
    }

    private void resetAllObstacles() {
        double minDistance = GameConfig.OBSTACLE_MIN_DISTANCE_BASE + speed * GameConfig.OBSTACLE_DISTANCE_SPEED_RATIO;
        double startX = score == 0 ? 850 : GameConfig.SCREEN_WIDTH + minDistance;
        for (ObstacleSlot obstacle : obstacles) {
            obstacle.reset(startX, score, groundY);
            double randomDistance = Math.random() * GameConfig.OBSTACLE_MAX_RANDOM_DISTANCE;
            startX += minDistance + randomDistance;
        }
    }

    private void clearScreenObstacles() {
        java.util.Iterator<ObstacleSlot> iterator = obstacles.iterator();
        java.util.List<ObstacleSlot> replacementObstacles = new java.util.ArrayList<>();

        while (iterator.hasNext()) {
            ObstacleSlot obstacle = iterator.next();
            double obsMinX = obstacle.getHitBoxBounds().getMinX();
            double obsMaxX = obstacle.getHitBoxBounds().getMaxX();

            if (obsMaxX > 0 && obsMinX < GameConfig.SCREEN_WIDTH) {
                root.getChildren().remove(obstacle.getCactus().getView());
                root.getChildren().remove(obstacle.getBird().getView());

                iterator.remove();

                distance += GameConfig.OBSTACLE_CLEAR_SCORE * 50;

                replacementObstacles.add(new ObstacleSlot(GameConfig.SCREEN_WIDTH, groundY));
            }
        }

        int insertIndex = root.getChildren().size();

        for (ObstacleSlot newSlot : replacementObstacles) {
            root.getChildren().add(insertIndex, newSlot.getCactus().getView());
            root.getChildren().add(insertIndex + 1, newSlot.getBird().getView());
            obstacles.add(newSlot);
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

    /**
     * 取得場景視圖根節點。
     */
    public Pane getView() {
        return root;
    }
}
