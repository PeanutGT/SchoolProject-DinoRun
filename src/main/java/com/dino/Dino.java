package com.dino;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Bounds;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.Label;

/**
 * 恐龍（玩家角色）類別，負責控制角色的物理運動（跳躍、下墜、蹲下）、生命值、
 * 碰撞箱、無敵時間、二段跳、不同角色皮膚/圖片載入，以及多人模式下的幽靈與復活狀態。
 */
public class Dino {

    // 包含 ImageView、碰撞箱與提示泡泡的 JavaFX 群組節點
    private Group group;
    // 角色圖片顯示器
    private ImageView imageView;
    // 角色的碰撞箱
    private Rectangle hitBox;
    // 遊戲中的對話/提示泡泡
    private Label hintBubble;
    // 提示泡泡的顯示計時器
    private PauseTransition hintTimer;

    // 動畫圖片陣列
    private Image[] runImages;      // 跑步動畫圖
    private Image[] jumpImages;     // 跳躍動畫圖
    private Image runImage1;
    private Image runImage2;
    private Image jumpImage;
    private Image fallImage;
    private Image duckImage1;
    private Image duckImage2;
    private Image deadImage;

    // 物理運動相關屬性
    private double velocityY = 0;        // Y 軸垂直速度
    private boolean onGround = true;      // 是否在地面上
    private boolean crouching = false;    // 是否處於蹲下狀態
    private boolean jumpAnimating = false; // 是否正在播放跳躍動畫

    // 地面 Y 座標基準值
    private final double groundY;

    // 站立狀態下的寬高設定
    private double standWidth = 42;
    private double standHeight = 45;

    // 蹲下狀態下的寬高與碰撞箱設定
    private double duckWidth = 60;
    private double duckHeight = 30;
    private double duckHitBoxX = 6;
    private double duckHitBoxY = 8;
    private double duckHitBoxWidth = 48;
    private double duckHitBoxHeight = 18;
    private boolean downPressed = false;  // 下方向鍵是否被按住

    // 恐龍腳部相對於地面的微調偏差值
    private double dinoGroundOffset = 5;

    // 動畫格計數器
    private int animationCounter = 0;

    // 生命值系統
    private int maxLives = 3;             // 最大生命值
    private int lives = 3;                // 當前生命值
    private int extraJumps = 0;           // 可用的額外跳躍次數（商店購買）
    private boolean invincible = false;    // 是否處於受傷後的短暫無敵狀態
    private boolean devInvincible = false; // 開發者無敵模式（不死之身）
    private long invincibleStartTime = 0;  // 無敵開始時間
    private final long invincibleDuration = 2000; // 無敵持續時間（毫秒）

    // 多人模式狀態
    private boolean isGhost = false;       // 是否為幽靈狀態（死亡後）
    private boolean hasGoldenApple = false; // 是否持有金蘋果（可用於復活）

    // 二段跳相關屬性
    private Image wingImage1;             // 振翅動畫第一格
    private Image wingImage2;             // 振翅動畫第二格
    private boolean doubleJumpUnlocked = false; // 是否解鎖永久二段跳
    private boolean canDoubleJump = false;      // 當前跳躍中是否還能進行二段跳
    private boolean doubleJumping = false;       // 是否正在進行二段跳
    private int wingFrameCounter = 0;     // 翅膀動畫格計數

    /**
     * 建構子：預設使用 GameConfig.GROUND_Y 作為地面高度
     * @param x 初始 X 座標
     */
    public Dino(double x) {
        this(x, GameConfig.GROUND_Y);
    }

    /**
     * 建構子：預設使用當前設定的角色名稱
     * @param x 初始 X 座標
     * @param groundY 地面 Y 座標
     */
    public Dino(double x, double groundY) {
        this(x, groundY, GameConfig.selectedCharacter);
    }

    /**
     * 完整建構子：初始化角色各項屬性、載入圖片資源、設定 UI 與碰撞箱
     * @param x 初始 X 座標
     * @param groundY 地面 Y 座標
     * @param character 角色代號（例如 mario, luigi, kirby, lucario, sonic, steve 等）
     */
    public Dino(double x, double groundY, String character) {
        this.groundY = groundY;

        // 依據角色載入對應的圖檔與設定尺寸
        loadCharacterImages(character);

        imageView = new ImageView(runImage1);
        imageView.setSmooth(false);
        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setPreserveRatio(false);

        // 初始化站立狀態的碰撞箱（相較於圖片略微縮小以優化碰撞體驗）
        hitBox = new Rectangle(8, 5, standWidth - 16, standHeight - 10);
        hitBox.setVisible(false);

        // 初始化提示泡泡 UI
        hintBubble = new Label();
        hintBubble.setStyle(
                "-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-font-family: 'Courier New', monospace; -fx-padding: 3; -fx-font-weight: bold;");
        hintBubble.setLayoutX(30);
        hintBubble.setLayoutY(-30);
        hintBubble.setVisible(false);

        // 載入二段跳的翅膀相關圖片
        wingImage1 = ResourceManager.getImage("wing1.jpg");
        wingImage2 = ResourceManager.getImage("wing2.jpg");

        // 將所有元件加入角色群組
        group = new Group(imageView, hitBox, hintBubble);
        group.setLayoutX(x);
        group.setLayoutY(getStandGroundPosition());

        // 從存檔管理員讀取升級項（生命加成與初始額外跳躍次數）
        this.maxLives = 3 + SaveManager.getLivesBonus();
        this.lives = this.maxLives;
        this.extraJumps = SaveManager.getExtraJumps(); 
        this.doubleJumpUnlocked = false; // 預設關閉永久二段跳，需由遊戲邏輯動態解鎖
    }

    /**
     * 顯示提示對話泡泡，並於 2.5 秒後自動隱藏。
     * @param text 提示文字
     */
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

    /**
     * 隱藏提示對話泡泡。
     */
    public void hideHint() {
        hintBubble.setVisible(false);
    }

    /**
     * 依據選擇的角色名稱，載入對應的圖檔資源、設定動畫影格、並設定該角色專屬的碰撞箱與地面偏移量。
     * @param character 角色名稱
     */
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

            runImages = new Image[] {
                    ResourceManager.getImage("mario_walk1.png"),
                    ResourceManager.getImage("mario_walk2.png"),
                    ResourceManager.getImage("mario_walk3.png"),
                    ResourceManager.getImage("mario_walk4.png"),
                    ResourceManager.getImage("mario_walk5.png"),
                    ResourceManager.getImage("mario_walk6.png")
            };
            jumpImages = new Image[] {
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

            runImages = new Image[] {
                    ResourceManager.getImage("luigi_run1.png"),
                    ResourceManager.getImage("luigi_run2.png"),
                    ResourceManager.getImage("luigi_run3.png"),
                    ResourceManager.getImage("luigi_run4.png"),
                    ResourceManager.getImage("luigi_run5.png"),
                    ResourceManager.getImage("luigi_run6.png")
            };
            jumpImages = new Image[] {
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

            runImages = new Image[] {
                    ResourceManager.getImage("kirby_run1.png"),
                    ResourceManager.getImage("kirby_run2.png"),
                    ResourceManager.getImage("kirby_run3.png"),
                    ResourceManager.getImage("kirby_run4.png")
            };
            jumpImages = new Image[] {
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

            runImages = new Image[] {
                    ResourceManager.getImage("lucario_run1.png"),
                    ResourceManager.getImage("lucario_run2.png"),
                    ResourceManager.getImage("lucario_run3.png"),
                    ResourceManager.getImage("lucario_run4.png"),
                    ResourceManager.getImage("lucario_run5.png"),
                    ResourceManager.getImage("lucario_run6.png")
            };
            jumpImages = new Image[] {
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

            runImages = new Image[] {
                    ResourceManager.getImage("sonic_run1.png"),
                    ResourceManager.getImage("sonic_run2.png"),
                    ResourceManager.getImage("sonic_run3.png"),
                    ResourceManager.getImage("sonic_run4.png")
            };
            jumpImages = new Image[] {
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

            runImages = new Image[] {
                    ResourceManager.getImage("steve_run1.png"),
                    ResourceManager.getImage("steve_run2.png"),
                    ResourceManager.getImage("steve_run3.png"),
                    ResourceManager.getImage("steve_run4.png")
            };
            jumpImage = ResourceManager.getImage("steve_jump.png");
            fallImage = ResourceManager.getImage("steve_fall.png");
            jumpImages = new Image[] { jumpImage };
            duckImage1 = ResourceManager.getImage("steve_fall.png");
            duckImage2 = duckImage1;
            deadImage = ResourceManager.getImage("steve_fall.png");
        } else {
            // 預設經典恐龍外觀與設定
            standWidth = 42;
            standHeight = 45;
            duckWidth = 60;
            duckHeight = 30;
            duckHitBoxX = 6;
            duckHitBoxY = 8;
            duckHitBoxWidth = duckWidth - 12;
            duckHitBoxHeight = duckHeight - 12;
            dinoGroundOffset = 5;

            runImages = new Image[] {
                    ResourceManager.getImage("dino_run1.png"),
                    ResourceManager.getImage("dino_run2.png")
            };
            jumpImage = ResourceManager.getImage("dino_jump.png");
            fallImage = jumpImage;
            jumpImages = new Image[] { jumpImage };
            duckImage1 = ResourceManager.getImage("dino_duck1.png");
            duckImage2 = ResourceManager.getImage("dino_duck2.png");
            deadImage = ResourceManager.getImage("dino_dead.png");
        }

        runImage1 = runImages[0];
        runImage2 = runImages.length > 1 ? runImages[1] : runImages[0];
    }

    /**
     * 切換開發者無敵模式，開啟時角色會呈現半透明。
     */
    public void toggleDevInvincible() {
        devInvincible = !devInvincible;
        if (devInvincible) {
            group.setOpacity(0.5);
        } else {
            group.setOpacity(1.0);
        }
    }

    /**
     * 當角色碰撞障礙物時呼叫。如果處於無敵狀態則無效；否則扣除生命，並觸發受傷無敵狀態。
     * @param activeGameTime 當前遊戲運行的累積時間（毫秒）
     * @return 是否成功扣血（若成功受傷返回 true，因無敵免疫返回 false）
     */
    public boolean hit(long activeGameTime) {
        if (invincible || devInvincible || isGhost) {
            return false;
        }

        lives--;
        invincible = true;
        invincibleStartTime = activeGameTime;

        return true;
    }

    /**
     * 更新無敵狀態，如果仍在無敵期間，則讓角色以 120ms 為週期閃爍。
     * @param activeGameTime 當前遊戲時間
     */
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

        // 以時間取模計算奇偶來決定顯示或隱藏，產生閃爍效果
        group.setVisible((now / 120) % 2 == 0);
    }

    /**
     * 執行跳躍邏輯。包含地面起跳、空中二段跳以及商店購買的額外跳躍處理。
     * @return 是否成功觸發跳躍
     */
    public boolean jump() {
        if (isDead() || isGhost)
            return false;

        if (onGround && !crouching) {
            // 地面起跳
            velocityY = GameConfig.JUMP_VELOCITY;
            onGround = false;
            jumpAnimating = jumpImages.length > 1;
            imageView.setImage(jumpImage);
            canDoubleJump = doubleJumpUnlocked;
            doubleJumping = false;
            return true;
        } else if (!onGround && !crouching) {
            // 空中二段跳處理
            if (canDoubleJump) {
                velocityY = GameConfig.JUMP_VELOCITY;

                // 二段跳時直接用二段跳圖檔(含有恐龍與翅膀)替換主圖檔，使用固定大小防止因圖片比例不同產生忽大忽小的抖動
                imageView.setPreserveRatio(false);
                imageView.setFitWidth(standWidth * 1.1);
                imageView.setFitHeight(standHeight * 1.1);
                imageView.setImage(wingImage1);

                jumpAnimating = false; // 二段跳期間只播振翅動畫
                canDoubleJump = false;
                doubleJumping = true;
                wingFrameCounter = 0;
                return true;
            } else if (extraJumps > 0) {
                // 使用商店購買的額外跳躍
                velocityY = GameConfig.JUMP_VELOCITY;
                jumpAnimating = jumpImages.length > 1;
                imageView.setImage(jumpImage);
                extraJumps--;
                return true;
            }
        }
        return false;
    }

    /**
     * 增加一次額外跳躍次數（例如遊戲中吃到道具）。
     */
    public void addExtraJump() {
        extraJumps++;
    }

    /**
     * 取得當前額外跳躍次數。
     */
    public int getExtraJumps() {
        return extraJumps;
    }

    /**
     * 將生命值補滿至最大值（包含商店血量上限加成）。
     */
    public void healToFull() {
        this.maxLives = 3 + SaveManager.getLivesBonus();
        this.lives = this.maxLives;
    }

    /**
     * 回復一滴血，但不超過最大生命值。
     */
    public void healOne() {
        if (lives < maxLives) {
            lives++;
        }
    }

    /**
     * 取得最大生命值。
     */
    public int getMaxLives() {
        return this.maxLives;
    }

    /**
     * 當放開跳躍鍵時，若仍在上升階段則將上升速度砍半，實現「輕按短跳、長按高跳」的效果。
     */
    public void releaseJump() {
        if (!onGround && velocityY < 0) {
            velocityY *= 0.5;
        }
    }

    /**
     * 快速下墜（在空中按向下鍵時觸發）。
     */
    public void fastFall() {
        if (!onGround) {
            velocityY = GameConfig.FAST_FALL_VELOCITY;
        }
    }

    /**
     * 按下向下鍵：若在地面則蹲下，若在空中則加速下墜。
     */
    public void pressDown() {
        downPressed = true;
        if (onGround) {
            crouch();
        } else {
            fastFall();
        }
    }

    /**
     * 放開向下鍵：若在地面則起立。
     */
    public void releaseDown() {
        downPressed = false;
        if (onGround) {
            standUp();
        }
    }

    /**
     * 使角色蹲下：修改圖片顯示尺寸與碰撞箱，並更新 Y 座標至蹲下地面基準。
     */
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

    /**
     * 使角色還原站立狀態：回復站立圖片尺寸、站立碰撞箱與地面 Y 座標。
     */
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

    /**
     * 取得當前剩餘生命值。
     */
    public int getLives() {
        return lives;
    }

    /**
     * 檢查角色是否在地面上。
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * 檢查角色是否死亡（生命值小於等於 0）。
     */
    public boolean isDead() {
        return lives <= 0;
    }

    /**
     * 檢查角色是否處於幽靈狀態。
     */
    public boolean isGhost() {
        return isGhost;
    }

    /**
     * 檢查是否持有金蘋果。
     */
    public boolean getHasGoldenApple() {
        return hasGoldenApple;
    }

    /**
     * 設定是否持有金蘋果。
     */
    public void setHasGoldenApple(boolean b) {
        this.hasGoldenApple = b;
    }

    /**
     * 轉換成幽靈狀態（多人模式中一玩家死亡後，變成半透明幽靈在空中漂浮，無法觸發碰撞）。
     */
    public void becomeGhost() {
        isGhost = true;
        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setImage(deadImage);
        group.setOpacity(0.5);
        // 設定漂浮的 Y 座標
        group.setLayoutY(groundY - 120);
        hitBox.setVisible(false); // 隱藏碰撞箱以避免再度受傷
    }

    /**
     * 從幽靈狀態復活（獲得 1 點生命值與無敵狀態）。
     * @param activeGameTime 當前遊戲時間
     */
    public void revive(long activeGameTime) {
        isGhost = false;
        lives = 1;
        invincible = true;
        invincibleStartTime = activeGameTime;
        group.setOpacity(1.0);
        standUp();
    }

    /**
     * 播放死亡動作：替換為死亡圖片並確保角色可見。
     */
    public void die() {
        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setImage(deadImage);
        group.setVisible(true);
    }

    /**
     * 重設角色所有狀態，用於重新開始遊戲。
     */
    public void reset() {
        this.maxLives = 3 + SaveManager.getLivesBonus();
        this.lives = this.maxLives;
        invincible = false;
        devInvincible = false;
        isGhost = false;
        hasGoldenApple = false;
        velocityY = 0;
        onGround = true;
        crouching = false;
        jumpAnimating = false;
        downPressed = false;
        extraJumps = SaveManager.getExtraJumps();
        doubleJumpUnlocked = false; 
        canDoubleJump = false;
        doubleJumping = false;

        imageView.setFitWidth(standWidth);
        imageView.setFitHeight(standHeight);
        imageView.setPreserveRatio(false);
        imageView.setImage(runImage1);

        hitBox.setX(8);
        hitBox.setY(5);
        hitBox.setWidth(standWidth - 16);
        hitBox.setHeight(standHeight - 10);

        group.setLayoutY(getStandGroundPosition());
        group.setVisible(true);
        group.setOpacity(1.0);
    }

    /**
     * 取得碰撞箱在場景中的實際邊界，用於進行碰撞偵測。
     */
    public Bounds getHitBoxBounds() {
        return hitBox.localToScene(hitBox.getBoundsInLocal());
    }

    /**
     * 每影格更新邏輯：處理跳躍物理、各類動畫影格切換與無敵狀態。
     * @param activeGameTime 遊戲累積運行時間（毫秒）
     * @param dtSeconds 距離上一格所經過的時間（秒）
     */
    public void update(long activeGameTime, double dtSeconds) {
        if (isGhost) {
            updateJump(dtSeconds);
            // 幽靈狀態下不播放跑步或蹲下動畫
            return;
        }

        updateJump(dtSeconds);
        if (doubleJumping) {
            updateDoubleJumpAnimation();
        } else {
            updateJumpAnimation();
            if (crouching) {
                updateDuckAnimation();
            } else {
                updateRunAnimation();
            }
        }
        updateInvincible(activeGameTime);
    }

    /**
     * 更新二段跳的翅膀振翅動畫。
     */
    private void updateDoubleJumpAnimation() {
        wingFrameCounter++;
        if (wingFrameCounter % 6 == 0) {
            if ((wingFrameCounter / 6) % 2 == 0) {
                imageView.setImage(wingImage1);
            } else {
                imageView.setImage(wingImage2);
            }
        }
    }

    /**
     * 更新跳躍與重力物理公式。
     * @param dtSeconds 經過時間秒數
     */
    private void updateJump(double dtSeconds) {
        if (isGhost) {
            // 幽靈固定漂浮於半空
            group.setLayoutY(groundY - 120);
            velocityY = 0;
            return;
        }

        if (!onGround) {
            // 依垂直速度變更 Y 座標
            group.setLayoutY(group.getLayoutY() + velocityY * dtSeconds);

            // 滯空時間優化：當速度接近 0 (即將到達跳躍頂點) 時，暫時減輕重力以獲得滯空感
            if (Math.abs(velocityY) < 2.5 * 60) {
                velocityY += GameConfig.GRAVITY * 0.4 * dtSeconds;
            } else {
                velocityY += GameConfig.GRAVITY * dtSeconds;
            }

            // 落地偵測
            if (group.getLayoutY() >= getStandGroundPosition()) {
                velocityY = 0;
                onGround = true;
                doubleJumping = false;
                imageView.setPreserveRatio(false);
                imageView.setFitWidth(standWidth);
                imageView.setFitHeight(standHeight);

                // 若落地時仍按著向下鍵，自動進入蹲下狀態；否則還原為跑步
                if (downPressed) {
                    crouch();
                } else {
                    group.setLayoutY(getStandGroundPosition());
                    jumpAnimating = false;
                    imageView.setImage(runImage1);
                }
            } else if (!jumpAnimating && !doubleJumping && velocityY > 0) {
                // 在空中且下墜時，使用下墜圖檔
                imageView.setImage(fallImage);
            }
        }
    }

    /**
     * 更新地面跑步動畫。
     */
    private void updateRunAnimation() {
        if (!onGround)
            return;
        animationCounter++;
        if (animationCounter % 7 == 0) {
            int index = (animationCounter / 7) % runImages.length;
            imageView.setImage(runImages[index]);
        }
    }

    /**
     * 更新跳躍動畫影格。
     */
    private void updateJumpAnimation() {
        if (!jumpAnimating || onGround)
            return;
        animationCounter++;
        if (animationCounter % 6 == 0) {
            int index = (animationCounter / 6) % jumpImages.length;
            imageView.setImage(jumpImages[index]);
        }
    }

    /**
     * 更新蹲下（爬行）動畫影格。
     */
    private void updateDuckAnimation() {
        if (!onGround)
            return;
        animationCounter++;
        if (animationCounter % 13 == 0) {
            if (imageView.getImage() == duckImage1) {
                imageView.setImage(duckImage2);
            } else {
                imageView.setImage(duckImage1);
            }
        }
    }

    /**
     * 取得站立狀態下的地面坐標（需考慮角色地面偏移量與高度）。
     */
    private double getStandGroundPosition() {
        return groundY - standHeight + dinoGroundOffset;
    }

    /**
     * 取得蹲下狀態下的地面坐標。
     */
    private double getDuckGroundPosition() {
        return groundY - duckHeight + dinoGroundOffset;
    }

    /**
     * 取得角色的 JavaFX 視圖節點。
     */
    public Group getView() {
        return group;
    }

    /**
     * 取得碰撞箱矩形元件。
     */
    public Rectangle getHitBox() {
        return hitBox;
    }
}
