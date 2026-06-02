package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Bounds;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import javafx.scene.control.Label;

public class Dino {

    private Group group;
    private ImageView imageView;
    private Rectangle hitBox;
    private Label hintBubble;
    private PauseTransition hintTimer;

    private Image[] runImages;
    private Image[] jumpImages;
    private Image runImage1;
    private Image runImage2;
    private Image jumpImage;
    private Image fallImage;
    private Image duckImage1;
    private Image duckImage2;
    private Image deadImage;

    private double velocityY = 0;
    private boolean onGround = true;
    private boolean crouching = false;
    private boolean jumpAnimating = false;

    private final double groundY;

    private double standWidth = 42;
    private double standHeight = 45;

    private double duckWidth = 60;
    private double duckHeight = 30;
    private double duckHitBoxX = 6;
    private double duckHitBoxY = 8;
    private double duckHitBoxWidth = 48;
    private double duckHitBoxHeight = 18;
    private boolean downPressed = false;

    private double dinoGroundOffset = 5;

    private int animationCounter = 0;

    private int maxLives = 3;
    private int lives = 3;
    private int extraJumps = 0;
    private boolean invincible = false;
    private boolean devInvincible = false;
    private long invincibleStartTime = 0;
    private final long invincibleDuration = 2000;

    public Dino(double x) {
        this(x, GameConfig.GROUND_Y);
    }

    public Dino(double x, double groundY) {
        this(x, groundY, GameConfig.selectedCharacter);
    }

    public Dino(double x, double groundY, String character) {
        this.groundY = groundY;

        loadCharacterImages(character);

        imageView = new ImageView(runImage1);
        imageView.setSmooth(false);
        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setPreserveRatio(false);

        hitBox = new Rectangle(8, 5, standWidth - 16, standHeight - 10);
        hitBox.setVisible(false);

        hintBubble = new Label();
        hintBubble.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-font-family: 'Courier New', monospace; -fx-padding: 3; -fx-font-weight: bold;");
        hintBubble.setLayoutX(30);
        hintBubble.setLayoutY(-30);
        hintBubble.setVisible(false);

        group = new Group(imageView, hitBox, hintBubble);
        group.setLayoutX(x);
        group.setLayoutY(getStandGroundPosition());

        this.maxLives = 3 + SaveManager.getLivesBonus();
        this.lives = this.maxLives;
        this.extraJumps = SaveManager.getExtraJumps();
    }

    public void showHint(String text) {
        hintBubble.setText(text);
        hintBubble.setVisible(true);
        
        if (hintTimer != null) {
            hintTimer.stop();
        }
        hintTimer = new PauseTransition(Duration.seconds(2.5));
        hintTimer.setOnFinished(e -> hideHint());
        hintTimer.play();
    }

    public void hideHint() {
        hintBubble.setVisible(false);
    }

    private void loadCharacterImages(String character) {
        if ("mario".equals(character)) {
            standWidth = 48;
            standHeight = 48;
            duckWidth = 48;
            duckHeight = 48;
            duckHitBoxX = 8;
            duckHitBoxY = 28;
            duckHitBoxWidth = 32;
            duckHitBoxHeight = 16;
            dinoGroundOffset = 5;

            runImages = new Image[]{
                    ResourceManager.getImage("mario_walk1.png"),
                    ResourceManager.getImage("mario_walk2.png"),
                    ResourceManager.getImage("mario_walk3.png"),
                    ResourceManager.getImage("mario_walk4.png"),
                    ResourceManager.getImage("mario_walk5.png"),
                    ResourceManager.getImage("mario_walk6.png")
            };
            jumpImages = new Image[]{
                    ResourceManager.getImage("mario_jump1.png"),
                    ResourceManager.getImage("mario_jump2.png"),
                    ResourceManager.getImage("mario_jump3.png")
            };
            jumpImage = jumpImages[0];
            fallImage = jumpImage;
            duckImage1 = ResourceManager.getImage("mario_crouch.png");
            duckImage2 = duckImage1;
            deadImage = ResourceManager.getImage("mario_dead.png");
        } else if ("luigi".equals(character)) {
            standWidth = 43;
            standHeight = 50;
            duckWidth = 43;
            duckHeight = 40;
            duckHitBoxX = 8;
            duckHitBoxY = 24;
            duckHitBoxWidth = 28;
            duckHitBoxHeight = 16;
            dinoGroundOffset = 5;

            runImages = new Image[]{
                    ResourceManager.getImage("luigi_run1.png"),
                    ResourceManager.getImage("luigi_run2.png"),
                    ResourceManager.getImage("luigi_run3.png"),
                    ResourceManager.getImage("luigi_run4.png"),
                    ResourceManager.getImage("luigi_run5.png"),
                    ResourceManager.getImage("luigi_run6.png")
            };
            jumpImages = new Image[]{
                    ResourceManager.getImage("luigi_jump1.png"),
                    ResourceManager.getImage("luigi_jump2.png")
            };
            jumpImage = jumpImages[0];
            fallImage = jumpImage;
            duckImage1 = ResourceManager.getImage("luigi_crouch.png");
            duckImage2 = duckImage1;
            deadImage = ResourceManager.getImage("luigi_dead.png");
        } else if ("kirby".equals(character)) {
            standWidth = 44;
            standHeight = 40;
            duckWidth = 38;
            duckHeight = 18;
            duckHitBoxX = 6;
            duckHitBoxY = 5;
            duckHitBoxWidth = 26;
            duckHitBoxHeight = 9;
            dinoGroundOffset = 5;

            runImages = new Image[]{
                    ResourceManager.getImage("kirby_run1.png"),
                    ResourceManager.getImage("kirby_run2.png"),
                    ResourceManager.getImage("kirby_run3.png"),
                    ResourceManager.getImage("kirby_run4.png")
            };
            jumpImages = new Image[]{
                    ResourceManager.getImage("kirby_jump1.png"),
                    ResourceManager.getImage("kirby_jump2.png"),
                    ResourceManager.getImage("kirby_jump3.png"),
                    ResourceManager.getImage("kirby_jump4.png"),
                    ResourceManager.getImage("kirby_jump5.png"),
                    ResourceManager.getImage("kirby_jump6.png"),
                    ResourceManager.getImage("kirby_jump7.png"),
                    ResourceManager.getImage("kirby_jump8.png"),
                    ResourceManager.getImage("kirby_jump9.png"),
                    ResourceManager.getImage("kirby_jump10.png")
            };
            jumpImage = jumpImages[0];
            fallImage = jumpImage;
            duckImage1 = ResourceManager.getImage("kirby_crouch.png");
            duckImage2 = duckImage1;
            deadImage = ResourceManager.getImage("kirby_dead.png");
        } else if ("lucario".equals(character)) {
            standWidth = 48;
            standHeight = 48;
            duckWidth = 45;
            duckHeight = 29;
            duckHitBoxX = 7;
            duckHitBoxY = 8;
            duckHitBoxWidth = 31;
            duckHitBoxHeight = 17;
            dinoGroundOffset = 5;

            runImages = new Image[]{
                    ResourceManager.getImage("lucario_run1.png"),
                    ResourceManager.getImage("lucario_run2.png"),
                    ResourceManager.getImage("lucario_run3.png"),
                    ResourceManager.getImage("lucario_run4.png"),
                    ResourceManager.getImage("lucario_run5.png"),
                    ResourceManager.getImage("lucario_run6.png")
            };
            jumpImages = new Image[]{
                    ResourceManager.getImage("lucario_jump1.png")
            };
            jumpImage = jumpImages[0];
            fallImage = ResourceManager.getImage("lucario_jump2.png");
            duckImage1 = ResourceManager.getImage("lucario_crouch.png");
            duckImage2 = duckImage1;
            deadImage = ResourceManager.getImage("lucario_dead.png");
        } else if ("sonic".equals(character)) {
            standWidth = 42;
            standHeight = 47;
            duckWidth = 48;
            duckHeight = 30;
            duckHitBoxX = 6;
            duckHitBoxY = 8;
            duckHitBoxWidth = duckWidth - 12;
            duckHitBoxHeight = duckHeight - 12;
            dinoGroundOffset = 5;

            runImages = new Image[]{
                    ResourceManager.getImage("sonic_run1.png"),
                    ResourceManager.getImage("sonic_run2.png"),
                    ResourceManager.getImage("sonic_run3.png"),
                    ResourceManager.getImage("sonic_run4.png")
            };
            jumpImages = new Image[]{
                    ResourceManager.getImage("sonic_jump1.png"),
                    ResourceManager.getImage("sonic_jump2.png"),
                    ResourceManager.getImage("sonic_jump3.png"),
                    ResourceManager.getImage("sonic_jump4.png")
            };
            jumpImage = jumpImages[0];
            fallImage = jumpImage;
            duckImage1 = ResourceManager.getImage("sonic_crouch.png");
            duckImage2 = duckImage1;
            deadImage = ResourceManager.getImage("sonic_dead.png");
        } else if ("steve".equals(character)) {
            standWidth = 42;
            standHeight = 42;
            duckWidth = 42;
            duckHeight = 34;
            duckHitBoxX = 6;
            duckHitBoxY = 8;
            duckHitBoxWidth = duckWidth - 12;
            duckHitBoxHeight = duckHeight - 12;
            dinoGroundOffset = 5;

            runImages = new Image[]{
                    ResourceManager.getImage("steve_run1.png"),
                    ResourceManager.getImage("steve_run2.png"),
                    ResourceManager.getImage("steve_run3.png"),
                    ResourceManager.getImage("steve_run4.png")
            };
            jumpImage = ResourceManager.getImage("steve_jump.png");
            fallImage = ResourceManager.getImage("steve_fall.png");
            jumpImages = new Image[]{jumpImage};
            duckImage1 = ResourceManager.getImage("steve_fall.png");
            duckImage2 = duckImage1;
            deadImage = ResourceManager.getImage("steve_fall.png");
        } else {
            standWidth = 42;
            standHeight = 45;
            duckWidth = 60;
            duckHeight = 30;
            duckHitBoxX = 6;
            duckHitBoxY = 8;
            duckHitBoxWidth = duckWidth - 12;
            duckHitBoxHeight = duckHeight - 12;
            dinoGroundOffset = 5;

            runImages = new Image[]{
                    ResourceManager.getImage("dino_run1.png"),
                    ResourceManager.getImage("dino_run2.png")
            };
            jumpImage = ResourceManager.getImage("dino_jump.png");
            fallImage = jumpImage;
            jumpImages = new Image[]{jumpImage};
            duckImage1 = ResourceManager.getImage("dino_duck1.png");
            duckImage2 = ResourceManager.getImage("dino_duck2.png");
            deadImage = ResourceManager.getImage("dino_dead.png");
        }

        runImage1 = runImages[0];
        runImage2 = runImages.length > 1 ? runImages[1] : runImages[0];
    }

    public void toggleDevInvincible() {
        devInvincible = !devInvincible;
        if (devInvincible) {
            group.setOpacity(0.5);
        } else {
            group.setOpacity(1.0);
        }
    }

    public boolean hit(long activeGameTime) {
        if (invincible || devInvincible) {
            return false;
        }

        lives--;
        invincible = true;
        invincibleStartTime = activeGameTime;

        return true;
    }

    private void updateInvincible(long activeGameTime) {
        if (!invincible) {
            group.setVisible(true);
            return;
        }

        long now = activeGameTime;
        if (now - invincibleStartTime >= invincibleDuration) {
            invincible = false;
            group.setVisible(true);
            return;
        }

        group.setVisible((now / 120) % 2 == 0);
    }

    public boolean jump() {
        if (onGround && !crouching) {
            velocityY = GameConfig.JUMP_VELOCITY;
            onGround = false;
            jumpAnimating = jumpImages.length > 1;
            imageView.setImage(jumpImage);
            return true;
        } else if (!onGround && !crouching && extraJumps > 0) {
            velocityY = GameConfig.JUMP_VELOCITY;
            jumpAnimating = jumpImages.length > 1;
            imageView.setImage(jumpImage);
            extraJumps--;
            return true;
        }
        return false;
    }

    public void addExtraJump() {
        extraJumps++;
    }

    public int getExtraJumps() {
        return extraJumps;
    }

    public void healToFull() {
        this.maxLives = 3 + SaveManager.getLivesBonus();
        this.lives = this.maxLives;
    }

    public int getMaxLives() {
        return this.maxLives;
    }

    public void releaseJump() {
        if (!onGround && velocityY < 0) {
            // 將向上的速度砍半
            velocityY *= 0.5;
        }
    }

    public void fastFall() {
        if (!onGround) {
            velocityY = GameConfig.FAST_FALL_VELOCITY;
        }
    }

    public void pressDown() {
        downPressed = true;
        if (onGround) {
            crouch();
        } else {
            fastFall();
        }
    }

    public void releaseDown() {
        downPressed = false;
        if (onGround) {
            standUp();
        }
    }

    public void crouch() {
        if (onGround) {
            crouching = true;
            imageView.setFitWidth(duckWidth);
            imageView.setFitHeight(duckHeight);
            imageView.setImage(duckImage1);

            hitBox.setX(duckHitBoxX);
            hitBox.setY(duckHitBoxY);
            hitBox.setWidth(duckHitBoxWidth);
            hitBox.setHeight(duckHitBoxHeight);

            group.setLayoutY(getDuckGroundPosition());
        }
    }

    public void standUp() {
        crouching = false;
        jumpAnimating = false;
        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setImage(runImage1);

        hitBox.setX(8);
        hitBox.setY(5);
        hitBox.setWidth(standWidth - 16);
        hitBox.setHeight(standHeight - 10);

        if (onGround) {
            group.setLayoutY(getStandGroundPosition());
        }
    }

    public int getLives() {
        return lives;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isDead() { return lives <= 0; }

    public void die() {
        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setImage(deadImage);
        group.setVisible(true);
    }

    public void reset() {
        this.maxLives = 3 + SaveManager.getLivesBonus();
        this.lives = this.maxLives;
        invincible = false;
        devInvincible = false;
        velocityY = 0;
        onGround = true;
        crouching = false;
        jumpAnimating = false;
        downPressed = false;
        extraJumps = SaveManager.getExtraJumps();

        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setImage(runImage1);

        hitBox.setX(8);
        hitBox.setY(5);
        hitBox.setWidth(standWidth - 16);
        hitBox.setHeight(standHeight - 10);

        group.setLayoutY(getStandGroundPosition());
        group.setVisible(true);
        group.setOpacity(1.0);
    }

    public Bounds getHitBoxBounds() {
        return hitBox.localToScene(hitBox.getBoundsInLocal());
    }

    public void update(long activeGameTime, double dtSeconds) {
        updateJump(dtSeconds);
        updateJumpAnimation();
        if (crouching) {
            updateDuckAnimation();
        } else {
            updateRunAnimation();
        }
        updateInvincible(activeGameTime);
    }

    private void updateJump(double dtSeconds) {
        if (!onGround) {
            group.setLayoutY(group.getLayoutY() + velocityY * dtSeconds);

            // ⭐ 第二招：最高點滯空時間 (Hang Time)
            // 當速度接近 0 (即將到達頂點或剛開始下墜) 時，暫時減輕重力
            if (Math.abs(velocityY) < 2.5 * 60) {
                velocityY += GameConfig.GRAVITY * 0.4 * dtSeconds;
            } else {
                velocityY += GameConfig.GRAVITY * dtSeconds;
            }

            if (group.getLayoutY() >= getStandGroundPosition()) {
                velocityY = 0;
                onGround = true;

                if (downPressed) {
                    crouch();
                } else {
                    group.setLayoutY(getStandGroundPosition());
                    jumpAnimating = false;
                    imageView.setImage(runImage1);
                }
            } else if (!jumpAnimating && velocityY > 0) {
                imageView.setImage(fallImage);
            }
        }
    }

    private void updateRunAnimation() {
        if (!onGround) return;
        animationCounter++;
        if (animationCounter % 7 == 0) {
            int index = (animationCounter / 7) % runImages.length;
            imageView.setImage(runImages[index]);
        }
    }

    private void updateJumpAnimation() {
        if (!jumpAnimating || onGround) return;
        animationCounter++;
        if (animationCounter % 6 == 0) {
            int index = (animationCounter / 6) % jumpImages.length;
            imageView.setImage(jumpImages[index]);
        }
    }

    private void updateDuckAnimation() {
        if (!onGround) return;
        animationCounter++;
        if (animationCounter % 13 == 0) {
            if (imageView.getImage() == duckImage1) {
                imageView.setImage(duckImage2);
            } else {
                imageView.setImage(duckImage1);
            }
        }
    }

    private double getStandGroundPosition() { return groundY - standHeight + dinoGroundOffset; }
    private double getDuckGroundPosition() { return groundY - duckHeight + dinoGroundOffset; }
    public Group getView() { return group; }
    public Rectangle getHitBox() { return hitBox; }
}
