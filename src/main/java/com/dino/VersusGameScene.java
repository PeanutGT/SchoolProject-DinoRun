package com.dino;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;

/**
 * 雙人對戰模式遊戲場景類別。
 * 採上下分屏設計：上方為玩家一 (Player One) 的獨立跑道，下方為玩家二 (Player Two) 的獨立跑道。
 * 雙方速度同步，但障礙物獨立生成。一旦某方玩家先耗盡生命值，對戰即結束並判定另一方獲勝。
 */
public class VersusGameScene {

    private static final double BASE_WIDTH = GameConfig.SCREEN_WIDTH;
    private static final double BASE_HEIGHT = GameConfig.SCREEN_HEIGHT;
    private static final double PLAYER_X = 100; // 恐龍固定的 X 軸位置
    
    // 雙跑道的不同地面垂直座標
    private static final double PLAYER_ONE_GROUND_Y = 200;      // 跑道一地面高度
    private static final double PLAYER_TWO_GROUND_Y = 420;      // 跑道二地面高度
    private static final double PLAYER_ONE_GROUND_IMAGE_Y = PLAYER_ONE_GROUND_Y - 5;
    private static final double PLAYER_TWO_GROUND_IMAGE_Y = PLAYER_TWO_GROUND_Y - 5;

    private final DinoMain dinoMain; // 主程式參考
    private final Pane root;         // 獨立畫布根容器
    private final Dino playerOne;    // 玩家一
    private final Dino playerTwo;    // 玩家二

    // 背景素材清單
    private final List<ImageView> clouds = new ArrayList<>();
    private final List<ImageView> groundImages = new ArrayList<>();

    // 雙跑道獨立生成的障礙物 Slot 集合
    private final List<ObstacleSlot> playerOneObstacles = new ArrayList<>();
    private final List<ObstacleSlot> playerTwoObstacles = new ArrayList<>();

    // 雙玩家獨立生命心心顯示 UI
    private final HeartDisplay playerOneHearts;
    private final HeartDisplay playerTwoHearts;
    
    private final ScoreDisplay scoreDisplay; // 頂部分數顯示
    private final ImageView gameOverImage;   // 遊戲結束圖片
    private final ImageView restartImage;    // 重新開始圖示
    private final StackPane pauseOverlay;    // 暫停覆蓋面板

    private AnimationTimer timer;            // 主計時器
    private double speed = GameConfig.INITIAL_SPEED; // 目前滾動速度
    private double distance = 0;             // 累積奔跑距離
    private int score = 0;                   // 當前分數
    private int lastFlashScore = 0;          // 上次微閃分數點
    private boolean gameOver = false;        // 遊戲是否結束
    private boolean paused = false;          // 是否暫停
    private boolean waitingToStart = true;   // 是否等待開始鍵
    
    // 防止按鍵連發起跳
    private boolean playerOneJumpPressed = false;
    private boolean playerTwoJumpPressed = false;

    // 對戰微秒時鐘
    private long activeGameTime = 0;
    private long lastFrameTime = 0;

    /**
     * 建構子：預設角色初始化。
     */
    public VersusGameScene(DinoMain dinoMain) {
        this(dinoMain, GameConfig.selectedCharacter, GameConfig.selectedCharacter);
    }

    /**
     * 完整建構子：初始化對戰視圖、分屏跑道、雙恐龍渲染、各自的障礙物池生成，以及對戰音效。
     * @param dinoMain 主程式
     * @param playerOneCharacter 玩家一角色代號
     * @param playerTwoCharacter 玩家二角色代號
     */
    public VersusGameScene(DinoMain dinoMain, String playerOneCharacter, String playerTwoCharacter) {
        this.dinoMain = dinoMain;

        root = new Pane();
        root.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        root.setStyle("-fx-background-color: white;");

        // 載入多朵雲朵背景
        createClouds();
        // 載入兩條獨立拼接的地面
        createGround(PLAYER_ONE_GROUND_IMAGE_Y);
        createGround(PLAYER_TWO_GROUND_IMAGE_Y);

        // 實例化兩個玩家恐龍
        playerOne = new Dino(PLAYER_X, PLAYER_ONE_GROUND_Y, playerOneCharacter);
        playerTwo = new Dino(PLAYER_X, PLAYER_TWO_GROUND_Y, playerTwoCharacter);
        playerTwo.getView().setOpacity(0.82); // 調整 P2 透明度以利分辨

        // 為上、下兩個跑道獨立生成各 3 個仙人掌/飛鳥插槽
        createObstacles(playerOneObstacles, PLAYER_ONE_GROUND_Y, 840);
        createObstacles(playerTwoObstacles, PLAYER_TWO_GROUND_Y, 980);

        // 雙人血量 UI (P2的血量顯示在中間分欄偏下位置 Y225)
        playerOneHearts = new HeartDisplay();
        playerTwoHearts = new HeartDisplay();
        playerTwoHearts.getView().setLayoutY(225);

        // 分數顯示（對戰不列入單人最高分計算）
        scoreDisplay = new ScoreDisplay(false);
        scoreDisplay.getView().setLayoutX(880);

        gameOverImage = new ImageView(ResourceManager.getImage("gameover.png"));
        gameOverImage.setSmooth(false);
        gameOverImage.setFitWidth(300);
        gameOverImage.setPreserveRatio(true);
        gameOverImage.setX(350);
        gameOverImage.setVisible(false);

        restartImage = new ImageView(ResourceManager.getImage("restart.png"));
        restartImage.setSmooth(false);
        restartImage.setFitWidth(40);
        restartImage.setPreserveRatio(true);
        restartImage.setX(470);
        restartImage.setVisible(false);
        restartImage.setOnMouseClicked(e -> {
            if (gameOver) {
                restartGame();
            }
        });

        // 載入暫停選單
        pauseOverlay = createPauseOverlay();

        root.getChildren().addAll(
                playerOne.getView(),
                playerTwo.getView(),
                playerOneHearts.getView(),
                playerTwoHearts.getView(),
                scoreDisplay.getView(),
                gameOverImage,
                restartImage,
                pauseOverlay
        );

        // 啟動主更新迴圈
        startGameLoop();
    }

    /**
     * 初始化多雲背景。
     */
    private void createClouds() {
        Image cloudImage = ResourceManager.getImage("cloud.png");
        addCloud(cloudImage, 260, 42, 80);
        addCloud(cloudImage, 620, 76, 70);
        addCloud(cloudImage, 430, 270, 78);
        addCloud(cloudImage, 770, 300, 86);
    }

    /**
     * 輔助加入雲朵。
     */
    private void addCloud(Image image, double x, double y, double width) {
        ImageView cloud = new ImageView(image);
        cloud.setSmooth(false);
        cloud.setPreserveRatio(true);
        cloud.setFitWidth(width);
        cloud.setX(x);
        cloud.setY(y);
        clouds.add(cloud);
        root.getChildren().add(cloud);
    }

    /**
     * 建立特定垂直高度的拼接跑道。
     */
    private void createGround(double y) {
        Image groundImage = ResourceManager.getImage("ground.png");
        ImageView groundOne = createGroundImage(groundImage, 0, y);
        ImageView groundTwo = createGroundImage(groundImage, BASE_WIDTH, y);
        groundImages.add(groundOne);
        groundImages.add(groundTwo);
        root.getChildren().addAll(groundOne, groundTwo);
    }

    private ImageView createGroundImage(Image groundImage, double x, double y) {
        ImageView ground = new ImageView(groundImage);
        ground.setSmooth(false);
        ground.setPreserveRatio(true);
        ground.setFitWidth(BASE_WIDTH);
        ground.setX(x);
        ground.setY(y);
        return ground;
    }

    /**
     * 建立特定跑道的獨立障礙物插槽集合。
     */
    private void createObstacles(List<ObstacleSlot> obstacles, double groundY, double startX) {
        obstacles.add(new ObstacleSlot(startX, groundY));
        obstacles.add(new ObstacleSlot(startX + 340, groundY));
        obstacles.add(new ObstacleSlot(startX + 700, groundY));

        for (ObstacleSlot obstacle : obstacles) {
            root.getChildren().add(obstacle.getCactus().getView());
            root.getChildren().add(obstacle.getBird().getView());
        }
    }

    /**
     * 建立暫停覆蓋選單與返回、繼續按鈕。
     */
    private StackPane createPauseOverlay() {
        StackPane overlay = new StackPane();
        overlay.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setVisible(false);

        VBox menu = new VBox(18);
        menu.setAlignment(Pos.CENTER);

        Label title = new Label("對戰暫停");
        title.setFont(Font.font(30));
        title.setTextFill(Color.WHITE);

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        Button resumeButton = new Button("繼續");
        Button restartButton = new Button("重新開始");
        Button menuButton = new Button("返回主選單");

        resumeButton.setOnAction(e -> togglePause());
        restartButton.setOnAction(e -> restartGame());
        menuButton.setOnAction(e -> {
            stop();
            dinoMain.showMainMenu();
        });

        buttons.getChildren().addAll(resumeButton, restartButton, menuButton);
        menu.getChildren().addAll(title, buttons);
        overlay.getChildren().add(menu);
        return overlay;
    }

    /**
     * 啟動主更新迴圈。
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
     * 對戰模式每影格邏輯更新：距離統計、雲朵地面位移、並獨立更新 P1 與 P2 的障礙物及碰撞偵測。
     */
    private void update(double dtSeconds) {
        if (gameOver) {
            return;
        }

        if (waitingToStart) {
            return;
        }

        distance += speed * dtSeconds;
        score = (int) (distance / 50);
        if (score > 0 && score / 100 > lastFlashScore / 100) {
            lastFlashScore = score;
            scoreDisplay.flashCurrentScore(score);
            SoundManager.playScore();
        }
        speed = Math.min(GameConfig.MAX_SPEED, speed + GameConfig.ACCELERATION * dtSeconds);

        // 更新四地面的水平位移
        updateGrounds(dtSeconds);
        updateClouds(dtSeconds);

        // 獨立更新玩家一物理與其跑道碰撞
        updatePlayer(playerOne, playerOneObstacles, PLAYER_ONE_GROUND_Y, dtSeconds);
        if (gameOver) {
            return;
        }
        // 獨立更新玩家二物理與其跑道碰撞
        updatePlayer(playerTwo, playerTwoObstacles, PLAYER_TWO_GROUND_Y, dtSeconds);
        
        // 更新血量數值
        updateDisplays();
    }

    /**
     * 捲動四個地面。
     */
    private void updateGrounds(double dtSeconds) {
        for (int i = 0; i < groundImages.size(); i += 2) {
            ImageView groundOne = groundImages.get(i);
            ImageView groundTwo = groundImages.get(i + 1);

            groundOne.setX(groundOne.getX() - speed * dtSeconds);
            groundTwo.setX(groundTwo.getX() - speed * dtSeconds);

            if (groundOne.getX() <= -BASE_WIDTH) {
                groundOne.setX(groundTwo.getX() + BASE_WIDTH);
            }
            if (groundTwo.getX() <= -BASE_WIDTH) {
                groundTwo.setX(groundOne.getX() + BASE_WIDTH);
            }
        }
    }

    /**
     * 捲動雲朵。
     */
    private void updateClouds(double dtSeconds) {
        double cloudSpeed = speed * 0.25 * dtSeconds;

        for (ImageView cloud : clouds) {
            cloud.setX(cloud.getX() - cloudSpeed);
            if (cloud.getX() < -100) {
                cloud.setX(BASE_WIDTH + Math.random() * 240);
            }
        }
    }

    /**
     * 更新指定玩家的物理動畫與障礙物位移檢測。若某方恐龍 lives 為 0，則呼叫 endGame() 判定結束。
     */
    private void updatePlayer(Dino player, List<ObstacleSlot> obstacles, double groundY, double dtSeconds) {
        player.update(activeGameTime, dtSeconds);

        for (ObstacleSlot obstacle : obstacles) {
            obstacle.update(speed, dtSeconds);
            if (obstacle.getX() < -obstacle.getWidth()) {
                resetObstacle(obstacles, obstacle, groundY);
            }

            // 碰撞檢測
            if (player.getHitBoxBounds().intersects(obstacle.getHitBoxBounds())) {
                boolean damaged = player.hit(activeGameTime);
                if (damaged) {
                    SoundManager.playHit();
                    updateDisplays();
                    if (player.isDead()) {
                        endGame(player); // 遊戲結束
                    }
                }
            }
        }
    }

    /**
     * 重設指定跑道的單個障礙物至最右側。使用對戰模式專屬的安全距離常數。
     */
    private void resetObstacle(List<ObstacleSlot> obstacles, ObstacleSlot obstacle, double groundY) {
        double rightMostX = BASE_WIDTH;
        for (ObstacleSlot other : obstacles) {
            rightMostX = Math.max(rightMostX, other.getX());
        }

        double minDistance = GameConfig.OBSTACLE_MIN_DISTANCE_BASE_VERSUS + speed * GameConfig.OBSTACLE_DISTANCE_SPEED_RATIO_VERSUS;
        double randomDistance = Math.random() * GameConfig.OBSTACLE_MAX_RANDOM_DISTANCE_VERSUS;
        obstacle.reset(rightMostX + minDistance + randomDistance, score, groundY);
    }

    private void updateDisplays() {
        playerOneHearts.update(playerOne.getLives());
        playerTwoHearts.update(playerTwo.getLives());
        scoreDisplay.update(score, 0);
    }

    /**
     * 終止遊戲更新並展示獲勝者結算。
     * @param loser 輸掉（扣光生命值）的 Dino 實體
     */
    private void endGame(Dino loser) {
        gameOver = true;
        timer.stop();
        loser.die();
        showGameOverFor(loser);
    }

    /**
     * 根據輸家調整 Game Over 與重玩按鈕的垂直定位高度，以便在該玩家的跑道中央進行提示。
     * @param loser 輸家
     */
    private void showGameOverFor(Dino loser) {
        if (loser == playerOne) {
            // P1 輸：Game Over 顯示在上方跑道 (Y72)
            gameOverImage.setY(72);
            restartImage.setY(122);
        } else {
            // P2 輸：Game Over 顯示在下方跑道 (Y292)
            gameOverImage.setY(292);
            restartImage.setY(342);
        }

        gameOverImage.setVisible(true);
        restartImage.setVisible(true);
    }

    /**
     * 綁定對戰模式下的雙鍵盤操作設定。
     * 玩家一 (P1): W 跳躍、S 蹲下
     * 玩家二 (P2): Up 跳躍、Down 蹲下
     */
    public void setKeyControl(Scene scene) {
        Scale scale = new Scale(1, 1);
        root.getTransforms().add(scale);

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            if (Double.isNaN(w) || Double.isNaN(h)) {
                w = BASE_WIDTH;
                h = BASE_HEIGHT;
            }

            double minScale = Math.min(w / BASE_WIDTH, h / BASE_HEIGHT);
            scale.setX(minScale);
            scale.setY(minScale);
            root.setTranslateX((w - BASE_WIDTH * minScale) / 2);
            root.setTranslateY((h - BASE_HEIGHT * minScale) / 2);
        };

        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
        sizeListener.changed(null, null, null);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (gameOver) {
                    stop();
                    dinoMain.showMainMenu();
                } else {
                    togglePause();
                }
                return;
            }

            // 遊戲結束時按空白鍵可以重新開始
            if (e.getCode() == KeyCode.SPACE && gameOver) {
                restartGame();
                waitingToStart = false;
                return;
            }

            if (paused || gameOver) {
                return;
            }

            // P1 W 鍵跳躍
            if (e.getCode() == KeyCode.W && !playerOneJumpPressed) {
                playerOneJumpPressed = true;
                if (waitingToStart) {
                    startGameWithJump(playerOne);
                    return;
                }
                if (playerOne.jump()) {
                    SoundManager.playJump();
                }
            } 
            // P2 Up 鍵跳躍
            else if (e.getCode() == KeyCode.UP && !playerTwoJumpPressed) {
                playerTwoJumpPressed = true;
                if (waitingToStart) {
                    startGameWithJump(playerTwo);
                    return;
                }
                if (playerTwo.jump()) {
                    SoundManager.playJump();
                }
            } else if (waitingToStart) {
                return;
            } 
            // 雙人下蹲
            else if (e.getCode() == KeyCode.S) {
                playerOne.pressDown();
            } else if (e.getCode() == KeyCode.DOWN) {
                playerTwo.pressDown();
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) {
                playerOneJumpPressed = false;
                playerOne.releaseJump();
            } else if (e.getCode() == KeyCode.S) {
                playerOne.releaseDown();
            } else if (e.getCode() == KeyCode.UP) {
                playerTwoJumpPressed = false;
                playerTwo.releaseJump();
            } else if (e.getCode() == KeyCode.DOWN) {
                playerTwo.releaseDown();
            }
        });
    }

    private void startGameWithJump(Dino player) {
        waitingToStart = false;
        if (player.jump()) {
            SoundManager.playJump();
        }
    }

    private void togglePause() {
        paused = !paused;
        pauseOverlay.setVisible(paused);

        if (paused) {
            timer.stop();
        } else {
            lastFrameTime = 0; 
            timer.start();
            Platform.runLater(() -> root.requestFocus());
        }
    }

    /**
     * 重設對戰，將雙玩家物理狀態重設，並將各自跑道障礙物清空重擺。
     */
    private void restartGame() {
        gameOver = false;
        paused = false;
        waitingToStart = true;
        speed = GameConfig.INITIAL_SPEED;
        distance = 0;
        score = 0;
        lastFlashScore = 0;
        activeGameTime = 0;
        lastFrameTime = 0;
        playerOneJumpPressed = false;
        playerTwoJumpPressed = false;
        gameOverImage.setVisible(false);
        restartImage.setVisible(false);
        pauseOverlay.setVisible(false);

        playerOne.reset();
        playerTwo.reset();
        playerTwo.getView().setOpacity(0.82);
        
        // 分別重設上、下兩個跑道的障礙物組
        resetObstacleGroup(playerOneObstacles, PLAYER_ONE_GROUND_Y, 840);
        resetObstacleGroup(playerTwoObstacles, PLAYER_TWO_GROUND_Y, 980);
        updateDisplays();

        timer.start();
        Platform.runLater(() -> root.requestFocus());
    }

    private void resetObstacleGroup(List<ObstacleSlot> obstacles, double groundY, double startX) {
        for (int i = 0; i < obstacles.size(); i++) {
            obstacles.get(i).reset(startX + i * 360, score, groundY);
        }
    }

    private void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * 取得對戰視圖。
     */
    public Pane getView() {
        return root;
    }
}
