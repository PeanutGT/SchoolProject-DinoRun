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

public class VersusGameScene {

    private static final double BASE_WIDTH = GameConfig.SCREEN_WIDTH;
    private static final double BASE_HEIGHT = GameConfig.SCREEN_HEIGHT;
    private static final double PLAYER_X = 100;
    private static final double PLAYER_ONE_GROUND_Y = 200;
    private static final double PLAYER_TWO_GROUND_Y = 420;
    private static final double PLAYER_ONE_GROUND_IMAGE_Y = PLAYER_ONE_GROUND_Y - 5;
    private static final double PLAYER_TWO_GROUND_IMAGE_Y = PLAYER_TWO_GROUND_Y - 5;

    private final DinoMain dinoMain;
    private final Pane root;
    private final Dino playerOne;
    private final Dino playerTwo;
    private final List<ImageView> clouds = new ArrayList<>();
    private final List<ImageView> groundImages = new ArrayList<>();
    private final List<ObstacleSlot> playerOneObstacles = new ArrayList<>();
    private final List<ObstacleSlot> playerTwoObstacles = new ArrayList<>();
    private final HeartDisplay playerOneHearts;
    private final HeartDisplay playerTwoHearts;
    private final ScoreDisplay scoreDisplay;
    private final ImageView gameOverImage;
    private final ImageView restartImage;
    private final StackPane pauseOverlay;

    private AnimationTimer timer;
    private double speed = GameConfig.INITIAL_SPEED;
    private double distance = 0;
    private int score = 0;
    private int lastFlashScore = 0;
    private boolean gameOver = false;
    private boolean paused = false;
    private boolean waitingToStart = true;
    private boolean playerOneJumpPressed = false;
    private boolean playerTwoJumpPressed = false;

    // Game Clock
    private long activeGameTime = 0;
    private long lastFrameTime = 0;

    public VersusGameScene(DinoMain dinoMain) {
        this(dinoMain, GameConfig.selectedCharacter, GameConfig.selectedCharacter);
    }

    public VersusGameScene(DinoMain dinoMain, String playerOneCharacter, String playerTwoCharacter) {
        this.dinoMain = dinoMain;

        root = new Pane();
        root.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        root.setStyle("-fx-background-color: white;");

        createClouds();
        createGround(PLAYER_ONE_GROUND_IMAGE_Y);
        createGround(PLAYER_TWO_GROUND_IMAGE_Y);

        playerOne = new Dino(PLAYER_X, PLAYER_ONE_GROUND_Y, playerOneCharacter);
        playerTwo = new Dino(PLAYER_X, PLAYER_TWO_GROUND_Y, playerTwoCharacter);
        playerTwo.getView().setOpacity(0.82);

        createObstacles(playerOneObstacles, PLAYER_ONE_GROUND_Y, 840);
        createObstacles(playerTwoObstacles, PLAYER_TWO_GROUND_Y, 980);

        playerOneHearts = new HeartDisplay();
        playerTwoHearts = new HeartDisplay();
        playerTwoHearts.getView().setLayoutY(225);

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

        startGameLoop();
    }

    private void createClouds() {
        Image cloudImage = ResourceManager.getImage("cloud.png");
        addCloud(cloudImage, 260, 42, 80);
        addCloud(cloudImage, 620, 76, 70);
        addCloud(cloudImage, 430, 270, 78);
        addCloud(cloudImage, 770, 300, 86);
    }

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

    private void createObstacles(List<ObstacleSlot> obstacles, double groundY, double startX) {
        obstacles.add(new ObstacleSlot(startX, groundY));
        obstacles.add(new ObstacleSlot(startX + 340, groundY));
        obstacles.add(new ObstacleSlot(startX + 700, groundY));

        for (ObstacleSlot obstacle : obstacles) {
            root.getChildren().add(obstacle.getCactus().getView());
            root.getChildren().add(obstacle.getBird().getView());
        }
    }

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

        distance += speed * dtSeconds;
        score = (int) (distance / 50);
        if (score > 0 && score / 100 > lastFlashScore / 100) {
            lastFlashScore = score;
            scoreDisplay.flashCurrentScore(score);
            SoundManager.playScore();
        }
        speed = Math.min(GameConfig.MAX_SPEED, speed + GameConfig.ACCELERATION * dtSeconds);

        updateGrounds(dtSeconds);
        updateClouds(dtSeconds);
        updatePlayer(playerOne, playerOneObstacles, PLAYER_ONE_GROUND_Y, dtSeconds);
        if (gameOver) {
            return;
        }
        updatePlayer(playerTwo, playerTwoObstacles, PLAYER_TWO_GROUND_Y, dtSeconds);
        updateDisplays();
    }

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

    private void updateClouds(double dtSeconds) {
        double cloudSpeed = speed * 0.25 * dtSeconds;

        for (ImageView cloud : clouds) {
            cloud.setX(cloud.getX() - cloudSpeed);
            if (cloud.getX() < -100) {
                cloud.setX(BASE_WIDTH + Math.random() * 240);
            }
        }
    }

    private void updatePlayer(Dino player, List<ObstacleSlot> obstacles, double groundY, double dtSeconds) {
        player.update(activeGameTime, dtSeconds);

        for (ObstacleSlot obstacle : obstacles) {
            obstacle.update(speed, dtSeconds);
            if (obstacle.getX() < -obstacle.getWidth()) {
                resetObstacle(obstacles, obstacle, groundY);
            }

            if (player.getHitBoxBounds().intersects(obstacle.getHitBoxBounds())) {
                boolean damaged = player.hit(activeGameTime);
                if (damaged) {
                    SoundManager.playHit();
                    updateDisplays();
                    if (player.isDead()) {
                        endGame(player);
                    }
                }
            }
        }
    }

    private void resetObstacle(List<ObstacleSlot> obstacles, ObstacleSlot obstacle, double groundY) {
        double rightMostX = BASE_WIDTH;
        for (ObstacleSlot other : obstacles) {
            rightMostX = Math.max(rightMostX, other.getX());
        }

        double minDistance = 240 + speed * (26.0 / 60.0);
        double randomDistance = Math.random() * 260;
        obstacle.reset(rightMostX + minDistance + randomDistance, score, groundY);
    }

    private void updateDisplays() {
        playerOneHearts.update(playerOne.getLives());
        playerTwoHearts.update(playerTwo.getLives());
        scoreDisplay.update(score, 0);
    }

    private void endGame(Dino loser) {
        gameOver = true;
        timer.stop();
        loser.die();
        showGameOverFor(loser);
    }

    private void showGameOverFor(Dino loser) {
        if (loser == playerOne) {
            gameOverImage.setY(72);
            restartImage.setY(122);
        } else {
            gameOverImage.setY(292);
            restartImage.setY(342);
        }

        gameOverImage.setVisible(true);
        restartImage.setVisible(true);
    }

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

            if (e.getCode() == KeyCode.SPACE && gameOver) {
                restartGame();
                waitingToStart = false;
                return;
            }

            if (paused || gameOver) {
                return;
            }

            if (e.getCode() == KeyCode.W && !playerOneJumpPressed) {
                playerOneJumpPressed = true;
                if (waitingToStart) {
                    startGameWithJump(playerOne);
                    return;
                }
                if (playerOne.jump()) {
                    SoundManager.playJump();
                }
            } else if (e.getCode() == KeyCode.UP && !playerTwoJumpPressed) {
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
            } else if (e.getCode() == KeyCode.S) {
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
            lastFrameTime = 0; // 重置時間計算
            timer.start();
            Platform.runLater(() -> root.requestFocus());
        }
    }

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

    public Pane getView() {
        return root;
    }
}
