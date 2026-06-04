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

/**
 * 單人模式遊戲場景類別。
 * 負責掌控單人遊戲的主遊戲迴圈（AnimationTimer），
 * 管理背景捲動、障礙物生成與碰撞偵測、金幣與問號方塊互動、玩家技能施放（QWERF）、
 * Boss 戰啟動與生命值管理、暫停面板設定、遊戲結束結算以及排行榜紀錄登錄。
 */
public class GameScene {

    private Pane root;              // 遊戲場景根畫布
    private DinoMain dinoMain;      // 遊戲主入口實例參考

    // 背景地面圖片，兩張拼接循環捲動
    private ImageView ground1;
    private ImageView ground2;

    // 背景雲朵圖片
    private ImageView cloud1;
    private ImageView cloud2;
    private ImageView cloud3;

    private Dino dino;              // 玩家控制的恐龍角色實體
    private String currentCharacter;// 當前選用的角色代號

    private ArrayList<ObstacleSlot> obstacles; // 存放仙人掌/飛鳥障礙物的插槽池
    private Label signpost;         // 遊戲開始時的綠色木牌操作說明

    private ScoreDisplay scoreDisplay;        // 頂部分數與最高分顯示 UI
    private static int sessionHighScore = 0;  // 當前遊戲連玩期間的最高分數

    private int score = 0;           // 當前分數
    private int lastScoreSound = 0;  // 記錄上一次播放分數突破音效時的分數點
    private int frameCount = 0;      // 動畫影格計數器

    private double speed = GameConfig.INITIAL_SPEED; // 當前捲動速度（像素/秒）

    // 快取全域常數以利存取
    private final double screenWidth = GameConfig.SCREEN_WIDTH;
    private final double groundY = GameConfig.GROUND_Y;
    private final double groundImageY = GameConfig.GROUND_IMAGE_Y;
    private final double groundWidth = GameConfig.SCREEN_WIDTH;

    private AnimationTimer timer;   // 動畫計時器物件

    // 遊戲結束相關 UI
    private ImageView gameOverImage;
    private ImageView restartImage;
    private Button gameOverMenuBtn;
    private boolean gameOver = false;

    // Boss 戰相關屬性
    private boolean bossPhase = false;       // 是否正在 Boss 戰階段
    private boolean bossIncoming = false;    // 是否處於 Boss 即將登場的過渡期 (等待場上障礙物清除)
    private int nextBossScore = GameConfig.BOSS_TRIGGER_SCORE; // 下一次觸發 Boss 戰的分數門檻
    private Boss boss = null;                // 當前登場的 Boss 物件
    private Rectangle screenFlash;           // Boss 登場時閃爍的紅色半透明遮罩

    private boolean inBossGracePeriod = false;    // Boss 被擊敗後，障礙物暫停生成的安全緩衝期
    private long bossGracePeriodStartTime = 0;    // 緩衝期開始時間 (毫秒)

    private StackPane pauseOverlay;  // 暫停覆蓋面板
    private boolean isPaused = false;        // 是否處於暫停狀態
    private boolean waitingToStart = true;   // 是否在遊戲開始前等待玩家按鍵

    // 道具與狀態 UI 變數
    private HeartDisplay heartDisplay;       // 心形生命值顯示 UI
    private SkillDisplay skillDisplay;       // 技能/道具欄顯示 UI
    private List<QuestionBlock> questionBlocks; // 當前畫面上的問號方塊列表
    private Rectangle milkFog;               // 喝牛奶後產生的致盲白霧矩形
    private int lastQuestionBlockScore = 0;  // 上一次生成問號方塊時的分數
    private boolean barrierActive = false;   // 護盾屏障是否處於啟動狀態
    private long barrierStartTime = 0;       // 護盾屏障啟動時的遊戲時間
    private long milkFogStartTime = 0;       // 牛奶致盲啟動時的遊戲時間
    private long screenFlashStartTime = 0;   // 螢幕紅閃啟動時的遊戲時間

    private Label barrierCountdownLabel;     // 護盾剩餘秒數倒數標籤
    private Label extraJumpLabel;            // 額外跳躍次數標籤

    private List<Coin> coinsList;            // 當前畫面上的金幣列表
    private CoinDisplay coinDisplay;         // 金幣統計顯示 UI
    private int sessionCoins = 0;            // 本次單局遊戲中收集到的金幣數
    private int lastCoinSpawnScore = 0;      // 上一次生成金幣時的分數

    // 防止鍵盤按鍵連發觸發多次反應的旗標
    private boolean spacePressed = false;
    private boolean upPressed = false;
    private boolean jumpAfterRestart = false;// 用於重新開始後立刻自動起跳的旗標

    // 骨頭迴力鏢技能變數（Boss戰中按空白鍵拋出）
    private Group boomerangGroup;            // 迴力鏢組合節點（ImageView 與 HitBox）
    private ImageView boomerangView;         // 迴力鏢圖片視圖
    private Rectangle boomerangHitBox;       // 迴力鏢碰撞箱
    private boolean boomerangActive = false; // 迴力鏢是否在空中飛行
    private double boomerangTime = 0.0;      // 迴力鏢飛行累積時間 (秒)
    private double boomerangStartX = 0.0;    // 拋出時的起點 X 座標
    private double boomerangStartY = 0.0;    // 拋出時的起點 Y 座標
    private boolean boomerangHasDamaged = false; // 此發迴力鏢是否已對 Boss 造成過傷害 (防重複碰撞)
    private double boomerangMaxDist = 350.0; // 迴力鏢最大飛行伸展距離

    // Boss 生命條 UI 節點
    private Pane bossHealthBarContainer;     // 生命值外框容器
    private Rectangle bossHealthInnerBar;    // 紅色生命條長度矩形
    private Label bossHealthLabel;           // Boss 名稱與血量數值文字

    // 遊戲微秒時鐘
    private long activeGameTime = 0;         // 遊戲有效運行累積時間 (毫秒，暫停期間不累加)
    private long lastFrameTime = 0;          // 上一影格的時間戳記 (奈秒)
    private double regenTimer = 0.0;         // 自動回血（商店升級）計時器 (秒)

    // 快取加速度與最高速
    private final double acceleration = GameConfig.ACCELERATION;
    private final double maxSpeed = GameConfig.MAX_SPEED;

    private double distance = 0;             // 累積奔跑距離 (像素，用來換算分數)

    /**
     * 建構子：使用預設角色初始化。
     */
    public GameScene(DinoMain dinoMain) {
        this(dinoMain, GameConfig.selectedCharacter);
    }

    /**
     * 完整建構子：建立遊戲場景畫布、繪製背景、綁定 UI、載入玩家角色與準備障礙物插槽。
     * @param dinoMain 遊戲主入口實例
     * @param character 選用角色
     */
    public GameScene(DinoMain dinoMain, String character) {
        this.dinoMain = dinoMain;
        this.currentCharacter = character;

        root = new Pane();
        root.setStyle("-fx-background-color: white;");

        // 建立背景裝飾與物理地面
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

        // 初始化分數統計顯示、生命顯示、技能顯示與各個容器清單
        scoreDisplay = new ScoreDisplay();
        dino = new Dino(100, GameConfig.GROUND_Y, character);
        heartDisplay = new HeartDisplay(dino.getMaxLives());
        skillDisplay = new SkillDisplay();
        questionBlocks = new ArrayList<>();
        coinsList = new ArrayList<>();
        coinDisplay = new CoinDisplay();

        // 關卡起點的操作說明告示牌
        signpost = new Label("【操作說明】\n[上方向鍵] 跳躍\n[空白鍵] 開始/骨頭迴力鏢(Boss戰)\n[下方向鍵] 蹲下\n[自訂技能按鍵] 施放技能");
        signpost.setStyle(
                "-fx-background-color: #8B4513; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-font-family: 'Courier New', monospace; -fx-padding: 10; -fx-font-weight: bold;");
        signpost.setLayoutX(300);
        signpost.setLayoutY(GameConfig.GROUND_Y - 120);

        // 牛奶所致盲的半透明白霧矩形
        milkFog = new Rectangle(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, Color.rgb(255, 255, 255, 0.7));
        milkFog.setVisible(false);

        // 屏障護盾倒數計時 Label
        barrierCountdownLabel = new Label();
        barrierCountdownLabel.setFont(Font.font("Arial", 36));
        barrierCountdownLabel.setTextFill(Color.BLUE);
        barrierCountdownLabel.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - 80);
        barrierCountdownLabel.setLayoutY(GameConfig.SCREEN_HEIGHT / 2 - 100);
        barrierCountdownLabel.setVisible(false);

        // 額外跳躍提示 Label
        extraJumpLabel = new Label();
        extraJumpLabel.setFont(Font.font("Arial", 24));
        extraJumpLabel.setTextFill(Color.MAGENTA);
        extraJumpLabel.setLayoutX(20);
        extraJumpLabel.setLayoutY(150);
        extraJumpLabel.setVisible(false);

        // 初始化三大障礙物滾動插槽 (預設在不同 X 位置)
        obstacles = new ArrayList<>();
        obstacles.add(new ObstacleSlot(850, groundY));
        obstacles.add(new ObstacleSlot(1150, groundY));
        obstacles.add(new ObstacleSlot(1450, groundY));

        // 依序將素材節點加入 Pane
        root.getChildren().addAll(
                cloud1,
                cloud2,
                cloud3,
                ground1,
                ground2,
                signpost,
                dino.getView());

        // 紅閃矩形（受傷或 Boss 出現時閃爍）
        screenFlash = new Rectangle(screenWidth, GameConfig.SCREEN_HEIGHT, Color.rgb(255, 0, 0, 0.5));
        screenFlash.setVisible(false);
        root.getChildren().add(screenFlash);

        // 將所有障礙物（仙人掌與鳥）的視覺 View 加到 Pane 中
        for (ObstacleSlot obstacle : obstacles) {
            root.getChildren().add(obstacle.getCactus().getView());
            root.getChildren().add(obstacle.getBird().getView());
        }

        // 把 UI 裝飾與面板放到上層
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

        // 創建並添加暫停覆蓋面板
        createPauseOverlay();
        root.getChildren().add(pauseOverlay);

        dino.showHint("按空白鍵或上鍵開始！");
        // 啟動 AnimationTimer
        startGameLoop();
    }

    /**
     * 建立暫停選單面板，包括設定 SettingsPanel 區塊、以及繼續、重玩、主選單三個按鈕。
     */
    private void createPauseOverlay() {
        pauseOverlay = new StackPane();
        pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        pauseOverlay.setPrefSize(screenWidth, 600); 

        // 使用 VBox 垂直排列內容
        VBox pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setTranslateY(-30);

        Label pauseLabel = new Label("遊戲暫停");
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setFont(Font.font(30));

        // 引入設定面板控制項以利在暫停時調整音量或看按鍵配置
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

    /**
     * 載入並初始化三朵白雲的圖片與初始位置。
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
     * 載入地面圖片並拼接在適當高度，用於循環滾動。
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
     * 啟動 JavaFX 動畫計時器主迴圈，記錄幀的精確時間差並驅動 update 邏輯。
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
                // 累積轉換成毫秒，用於不受暫停影響的相對遊戲時間
                activeGameTime += deltaTime / 1_000_000;
                double dtSeconds = deltaTime / 1_000_000_000.0;
                update(dtSeconds);
            }
        };
        timer.start();
    }

    /**
     * 每影格核心更新方法。
     * 處理包括重新開始起跳、速度微加、分數累積、恐龍狀態更新、金幣磁鐵吸引、
     * 問號方塊吃取、牛奶致盲白幕、護盾時效倒數、商店加成自動回血、
     * 骨頭迴力鏢軌跡與碰撞 Boss、Boss 行為模式與生命條更新、碰撞檢測以及雲地循環滾動。
     * @param dtSeconds 距離上一影格經過之秒數
     */
    private void update(double dtSeconds) {
        if (gameOver) {
            return;
        }

        if (waitingToStart) {
            return;
        }

        // 若標記重玩起跳，在更新之初立刻觸發跳躍
        if (jumpAfterRestart) {
            if (dino.jump()) {
                SoundManager.playJump();
            }
            jumpAfterRestart = false;
        }

        frameCount++;

        // 依據加速度公式更新目前畫捲滾動速度
        updateSpeed(dtSeconds);
        // 累積距離與計算分數
        updateScore(dtSeconds);

        // 更新角色重力物理與動畫幀
        dino.update(activeGameTime, dtSeconds);

        // 獲取恐龍中心座標與磁鐵半徑，用於吸引金幣
        double dinoCenterX = dino.getView().getLayoutX() + dino.getView().getBoundsInLocal().getWidth() / 2.0;
        double dinoCenterY = dino.getView().getLayoutY() + dino.getView().getBoundsInLocal().getHeight() / 2.0;
        double magnetRadius = SaveManager.getMagnetRadius();

        // 逐一更新金幣狀態
        java.util.Iterator<Coin> coinIt = coinsList.iterator();
        while (coinIt.hasNext()) {
            Coin coin = coinIt.next();
            // 金幣受磁鐵吸引而向恐龍靠攏，並受背景捲動影響左移
            coin.update(speed, dtSeconds, dinoCenterX, dinoCenterY, magnetRadius);
            
            // 碰撞金幣處理
            if (coin.getHitBoxBounds().intersects(dino.getHitBoxBounds())) {
                root.getChildren().remove(coin.getView());
                coinIt.remove();

                // 計算金幣收集加倍數
                int multiplier = SaveManager.getCoinMultiplier();
                int coinsEarned = 1 * multiplier;
                sessionCoins += coinsEarned;
                SaveManager.addCoins(coinsEarned);
                coinDisplay.update(sessionCoins);

                SoundManager.playScore();
                // 畫面上顯示浮空加分特效
                showFloatingText("+" + coinsEarned, coin.getX(), coin.getY());
            } else if (coin.isOffScreen()) {
                root.getChildren().remove(coin.getView());
                coinIt.remove();
            }
        }

        // 更新問號方塊位置
        java.util.Iterator<QuestionBlock> it = questionBlocks.iterator();
        while (it.hasNext()) {
            QuestionBlock qb = it.next();
            qb.update(speed, dtSeconds);
            // 碰撞問號方塊：吃取隨機道具
            if (qb.getHitBoxBounds().intersects(dino.getHitBoxBounds())) {
                root.getChildren().remove(qb.getView());
                it.remove();
                
                giveRandomItem();
            } else if (qb.isOffScreen()) {
                root.getChildren().remove(qb.getView());
                it.remove();
            }
        }

        // 更新護盾(屏障)的計時與倒數標籤
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

        // 更新額外跳躍提示標籤
        if (dino.getExtraJumps() > 0) {
            extraJumpLabel.setVisible(true);
            extraJumpLabel.setText("額外跳躍: " + dino.getExtraJumps());
        } else {
            extraJumpLabel.setVisible(false);
        }

        // 牛奶致盲計時檢測
        if (milkFog.isVisible()) {
            if (activeGameTime - milkFogStartTime >= GameConfig.MILK_FOG_DURATION_MS) {
                milkFog.setVisible(false);
            }
        }

        // 紅閃計時檢測
        if (screenFlash.isVisible()) {
            if (activeGameTime - screenFlashStartTime >= 500) {
                screenFlash.setVisible(false);
            }
        }

        // 處理自動緩慢回血功能（商店購買）
        if (SaveManager.hasRegen() && !dino.isDead() && dino.getLives() < dino.getMaxLives()) {
            regenTimer += dtSeconds;
            // 依自動回血等級縮短冷卻秒數
            double targetTime = 40.0;
            if (SaveManager.getRegenLevel() == 2) targetTime = 20.0;
            else if (SaveManager.getRegenLevel() == 3) targetTime = 10.0;

            if (regenTimer >= targetTime) {
                regenTimer = 0.0;
                dino.healOne();
                heartDisplay.update(dino.getLives());
                SoundManager.playAppleSound();
                showFloatingText("+1 HP", dino.getView().getLayoutX() + 20, dino.getView().getLayoutY() - 30);
            }
        } else {
            regenTimer = 0.0;
        }

        // 更新骨頭迴力鏢軌跡與碰撞檢測
        if (boomerangActive) {
            boomerangTime += dtSeconds;
            // 快速自轉以展示迴力鏢效果
            boomerangView.setRotate(boomerangView.getRotate() + dtSeconds * 720.0);

            double duration = 1;      // 總飛行往返時間為 1 秒
            double outward = 0.25;    // 前 0.25 秒為向右飛行
            double maxDist = boomerangMaxDist;

            if (boomerangTime < outward) {
                // 向右直飛
                double pct = boomerangTime / outward;
                double curX = boomerangStartX + maxDist * pct;
                double curY = boomerangStartY;
                boomerangGroup.setLayoutX(curX);
                boomerangGroup.setLayoutY(curY);
            } else if (boomerangTime < duration) {
                // 往返飛行：朝向此時恐龍所在的中心位置回收
                double pct = (boomerangTime - outward) / (duration - outward);
                double outX = boomerangStartX + maxDist;
                double outY = boomerangStartY;
                double targetX = dino.getView().getLayoutX() + dino.getView().getBoundsInLocal().getWidth() / 2.0 - 16;
                double targetY = dino.getView().getLayoutY() + dino.getView().getBoundsInLocal().getHeight() / 2.0 - 16;

                double curX = outX * (1.0 - pct) + targetX * pct;
                double curY = outY * (1.0 - pct) + targetY * pct;
                boomerangGroup.setLayoutX(curX);
                boomerangGroup.setLayoutY(curY);
            } else {
                // 飛行完畢，回收
                root.getChildren().remove(boomerangGroup);
                boomerangActive = false;
            }

            // 迴力鏢擊中 Boss 的碰撞偵測
            if (boomerangActive && !boomerangHasDamaged && bossPhase && boss != null) {
                if (boomerangHitBox.localToScene(boomerangHitBox.getBoundsInLocal())
                        .intersects(boss.getHitBoxBounds())) {
                    boss.takeDamage(10);
                    boomerangHasDamaged = true;
                    SoundManager.playHit();

                    // 在 Boss 上方浮現受傷數值
                    double damageX = boss.getHitBoxBounds().getCenterX() - 20;
                    double damageY = boss.getHitBoxBounds().getMinY() - 20;
                    showFloatingText("-10 HP", damageX, damageY);
                }
            }
        }

        // 更新 Boss 行為模式與生命血條顯示
        if (bossPhase && boss != null) {
            boss.update(speed, activeGameTime, dtSeconds);

            // 更新 Boss 生命血條長度與文字
            if (bossHealthBarContainer != null && bossHealthInnerBar != null && bossHealthLabel != null) {
                double hpPct = (double) boss.getHp() / (double) boss.getMaxHp();
                bossHealthInnerBar.setWidth(296 * hpPct);
                bossHealthLabel.setText("BOSS: " + boss.getName() + " (" + boss.getHp() + "/" + boss.getMaxHp() + ")");
            }

            // 檢查 Boss 是否被打倒
            if (boss.isDefeated(activeGameTime)) {
                bossPhase = false;
                boss.removeAllProjectiles();
                boss = null;

                // 移除 Boss 生命條 UI
                if (bossHealthBarContainer != null) {
                    root.getChildren().remove(bossHealthBarContainer);
                    bossHealthBarContainer = null;
                }

                // 移除仍在空中的迴力鏢
                if (boomerangActive) {
                    root.getChildren().remove(boomerangGroup);
                    boomerangActive = false;
                }

                // 啟動撤退安全期，避免立刻生成小障礙物造成躲避死角
                inBossGracePeriod = true;
                bossGracePeriodStartTime = activeGameTime;
            }
        } else if (inBossGracePeriod) {
            // 安全期計時
            if (activeGameTime - bossGracePeriodStartTime >= GameConfig.BOSS_RETREAT_GRACE_PERIOD_MS) {
                inBossGracePeriod = false;
                resetAllObstacles(); // 安全期結束後，將障礙物拉回螢幕右側重新開始排列
            }
        } else {
            // 普通關卡：更新常規障礙物位移
            updateObstacles(dtSeconds);
        }

        // 全域碰撞偵測
        checkCollision();

        // 捲動地面與雲朵背景
        updateGround(dtSeconds);
        updateClouds(dtSeconds);
    }

    /**
     * 進行玩家碰撞障礙物/Boss 的檢測。
     * 若有屏障護盾 (barrierActive) 則直接撞碎障礙物而不扣血；否則調用 dino.hit() 判斷生命值。
     */
    private void checkCollision() {
        if (bossPhase && boss != null) {
            // 檢測與 Boss 或其子彈/火球的碰撞
            if (boss.checkCollision(dino.getHitBoxBounds())) {
                if (barrierActive)
                    return; 
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
            // 檢測普通障礙物碰撞
            for (ObstacleSlot obstacle : obstacles) {
                if (dino.getHitBoxBounds().intersects(obstacle.getHitBoxBounds())) {
                    if (barrierActive) {
                        // 護盾狀態：直接把仙人掌/小鳥撞飛重設，並獲得加分
                        resetObstacle(obstacle);
                        distance += GameConfig.OBSTACLE_CLEAR_SCORE * 50;
                        SoundManager.playScore();
                    } else {
                        // 常規碰撞扣血
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

    /**
     * 遊戲結束結算。
     * 停止音樂、將分數與歷史最高分對比、播放死亡動作、檢測並登錄排行榜名單，最後顯示結算 UI 按鈕。
     */
    private void gameOver() {
        SoundManager.stopGameBgm();
        gameOver = true;
        if (score > sessionHighScore) {
            sessionHighScore = score;
        }
        scoreDisplay.update(score, sessionHighScore);
        dino.die();

        // 判斷分數是否足以上榜
        if (LeaderboardManager.isHighScore(score, false)) {
            Platform.runLater(() -> {
                // 彈出 JavaFX TextInputDialog 對話框提示輸入大名
                TextInputDialog dialog = new TextInputDialog("Player");
                dialog.setTitle("排行榜");
                dialog.setHeaderText("破紀錄啦！");
                dialog.setContentText("請輸入你的名字：");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    String cleanName = name.replace(",", "").trim();
                    if (cleanName.isEmpty())
                        cleanName = "Unknown";
                    // 記錄分數至資料庫
                    LeaderboardManager.addScore(cleanName, score, currentCharacter, null, false);
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

    /**
     * 取得目前畫面上所有障礙物中最右方的 X 座標點，用於防止生成重疊或距離過近。
     */
    private double getRightMostObstacleX() {
        double maxX = screenWidth;
        for (ObstacleSlot obstacle : obstacles) {
            if (obstacle.getX() > maxX) {
                maxX = obstacle.getX();
            }
        }
        return maxX;
    }

    /**
     * 重設單個障礙物，將其計算至最右方位置，保持與前一個障礙物有足夠的物理安全距離。
     */
    private void resetObstacle(ObstacleSlot obstacle) {
        // 安全距離隨遊戲滾動速度提升而拉長，保證玩家反應空間
        double minDistance = GameConfig.OBSTACLE_MIN_DISTANCE_BASE + speed * GameConfig.OBSTACLE_DISTANCE_SPEED_RATIO;
        double randomDistance = Math.random() * GameConfig.OBSTACLE_MAX_RANDOM_DISTANCE;
        double rightMostX = getRightMostObstacleX();
        double newX = rightMostX + minDistance + randomDistance;
        obstacle.reset(newX, score, groundY);
    }

    /**
     * 移動障礙物。若障礙物完全出界（滾至左方外），如果在 Boss 戰準備期間，則不再重設（以排空場景）；否則重設。
     * 若已清空所有殘留障礙物，且正等待 Boss 來臨，則立刻啟動 Boss 戰。
     */
    private void updateObstacles(double dtSeconds) {
        boolean allCleared = true;
        for (ObstacleSlot obstacle : obstacles) {
            obstacle.update(speed, dtSeconds);
            if (obstacle.getX() < -obstacle.getWidth()) {
                if (!bossIncoming && !bossPhase) {
                    resetObstacle(obstacle);
                }
            } else {
                allCleared = false; // 畫面上還有障礙物
            }
        }

        // 若場上障礙物全部排空，且 bossIncoming 為 true，則正式進入 Boss 戰
        if (bossIncoming && allCleared) {
            triggerBossPhase();
        }
    }

    /**
     * 觸發 Boss 階段。
     * 實例化 Boss 怪物、移開障礙物、閃爍紅幕、動態繪製 Boss 紅色生命血條與背景容器。
     */
    private void triggerBossPhase() {
        bossIncoming = false;
        bossPhase = true;
        // 隨機抽選一位 Boss 登場 (例如庫巴或空洞騎士)
        boss = Boss.spawnRandomBoss(root, activeGameTime, false);

        // 清理畫面上殘留的障礙物 (防微小穿幫)
        for (ObstacleSlot obstacle : obstacles) {
            obstacle.reset(-200, score, groundY);
        }

        screenFlash.setFill(Color.rgb(255, 0, 0, 0.5));
        screenFlash.setVisible(true);
        screenFlashStartTime = activeGameTime;

        // 創建 Boss 生命值血條 UI 容器
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

        // 確保將生命條加在致盲迷霧層之下、背景圖層之上以防重疊問題
        int insertIndex = root.getChildren().indexOf(milkFog);
        if (insertIndex != -1) {
            root.getChildren().add(insertIndex, bossHealthBarContainer);
        } else {
            root.getChildren().add(bossHealthBarContainer);
        }
    }

    /**
     * 更新地面位移。當第一塊地面滾出左方螢幕時，挪到第二塊地面右側無縫拼接。
     */
    private void updateGround(double dtSeconds) {
        ground1.setX(ground1.getX() - speed * dtSeconds);
        ground2.setX(ground2.getX() - speed * dtSeconds);

        if (ground1.getX() <= -groundWidth) {
            ground1.setX(ground2.getX() + groundWidth);
        }
        if (ground2.getX() <= -groundWidth) {
            ground2.setX(ground1.getX() + groundWidth);
        }

        // 移動起點的操作說明招牌
        if (signpost.getLayoutX() > -300) {
            signpost.setLayoutX(signpost.getLayoutX() - speed * dtSeconds);
        }
    }

    /**
     * 更新背景雲朵位移（速度為地面的 0.25 倍，產生視差 Parallax 效果）。
     */
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

    /**
     * 依據奔跑距離轉換分數。
     * 並於達到特定間距時，生成問號道具方塊、金幣，以及預警 Boss 的來臨。
     */
    private void updateScore(double dtSeconds) {
        distance += speed * dtSeconds;
        int newScore = (int) (distance / 50);

        if (newScore > score) {
            score = newScore;
            // 每隔 100 分播放突破音效並微閃分數 UI
            if (score > 0 && score / 100 > lastScoreSound / 100) {
                lastScoreSound = score;
                SoundManager.playScore();
                scoreDisplay.flashCurrentScore(score);
            }

            // 依商店升級的幸運方塊等級，縮短問號方塊的生成分數間隔
            int qbInterval = GameConfig.QUESTION_BLOCK_INTERVAL;
            int qbLevel = SaveManager.getQuestionBoxLevel();
            if (qbLevel == 1) {
                qbInterval = 200;
            } else if (qbLevel == 2) {
                qbInterval = 150;
            } else if (qbLevel >= 3) {
                qbInterval = 100;
            }

            // 到了生成問號方塊的分數點
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

            // 到了生成金幣的分數點（如果升級了金幣磁鐵加成則生成間隔減半）
            int coinSpawnInterval = SaveManager.hasMoreCoins() ? 20 : GameConfig.COIN_SPAWN_INTERVAL;
            if (score > 0 && score % coinSpawnInterval == 0 && score != lastCoinSpawnScore) {
                lastCoinSpawnScore = score;
                double coinY;
                double r = Math.random();
                if (r < 0.4) {
                    coinY = groundY - 20; // 地面金幣
                } else if (r < 0.7) {
                    coinY = groundY - 60; // 低空金幣 (可蹲可跳)
                } else {
                    coinY = groundY - 110;// 高空金幣
                }
                // 獲取一個避開仙人掌起跳點的 X 座標以防玩家無法吃到
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

            // 檢查是否達到召喚 Boss 的分數門檻
            if (score >= nextBossScore) {
                bossIncoming = true;
                nextBossScore += GameConfig.BOSS_INTERVAL_SCORE;
            }
        }
        scoreDisplay.update(score, sessionHighScore);
    }

    /**
     * 隨著時間的推移微量增加滾動速度。
     */
    private void updateSpeed(double dtSeconds) {
        speed += acceleration * dtSeconds;
        if (speed > maxSpeed) {
            speed = maxSpeed;
        }
    }

    /**
     * 綁定鍵盤按鍵控制與螢幕尺寸適配監聽。
     * 設定了視窗自動比例等比例縮放以防畫面崩壞。
     * 支援跳躍(Up)、蹲下(Down)、確定與迴力鏢(Space)、五個技能鍵(QWERF)以及開發者調試按鍵(F1~F5)。
     */
    public void setKeyControl(Scene scene) {
        // 設定畫面自適應等比例縮放 Matrix
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

            // 保持畫面置中對齊
            root.setTranslateX((w - GameConfig.SCREEN_WIDTH * minScale) / 2);
            root.setTranslateY((h - GameConfig.SCREEN_HEIGHT * minScale) / 2);
        };
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
        sizeListener.changed(null, null, null);

        // 按鍵按下偵測
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                // Esc 切換暫停狀態
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
                    // Boss 戰期間，空白鍵充當攻擊鍵：扔出骨頭迴力鏢
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

            // ==========================================
            // 技能與背包道具使用按鍵判定 (Q, W, E, R, F)
            // ==========================================
            if (e.getCode() == KeyCode.Q && GameConfig.goldenAppleCount > 0) {
                // 金蘋果 (Q): 補滿生命值
                GameConfig.goldenAppleCount--;
                SoundManager.playAppleSound();
                dino.healToFull();
                heartDisplay.update(dino.getLives());
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.W && GameConfig.milkBucketCount > 0) {
                // 牛奶 (W): 獲得500pt距離加成，但致盲5秒
                GameConfig.milkBucketCount--;
                SoundManager.playMilkSound();
                distance += GameConfig.MILK_SCORE_BONUS * 50;
                milkFog.setVisible(true);
                milkFogStartTime = activeGameTime;
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.E && GameConfig.enchantedBookCount > 0) {
                // 附魔書 (E): 獲得額外跳躍次數 (空中多跳一次)
                GameConfig.enchantedBookCount--;
                SoundManager.playBookSound();
                dino.addExtraJump();
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.R && GameConfig.barrierCount > 0) {
                // 屏障護盾 (R): 開啟 12 秒完全免傷狀態，角色半透明呈現
                GameConfig.barrierCount--;
                SoundManager.playBarrierSound();
                barrierActive = true;
                barrierStartTime = activeGameTime;
                dino.getView().setOpacity(0.8);
                skillDisplay.update();
            } else if (e.getCode() == KeyCode.F && GameConfig.woodenSwordCount > 0) {
                // 木劍 (F): 清除目前畫面上所有小怪與障礙物並換算成碎骨加分 (對Boss無效)
                GameConfig.woodenSwordCount--;
                SoundManager.playSwordSound();
                clearScreenObstacles();
                skillDisplay.update();
            }

            // ==========================================
            // 開發者 Debug 工具按鍵區 (需在設定中開啟 devModeEnabled)
            // ==========================================
            if (GameConfig.devModeEnabled) {
                if (e.getCode() == KeyCode.F1) {
                    // F1: 無敵開關
                    dino.toggleDevInvincible();
                } else if (e.getCode() == KeyCode.F2) {
                    // F2: 增加 100 分
                    distance += 100 * 50;
                } else if (e.getCode() == KeyCode.F3) {
                    // F3: 分數直接跳至 950 分 (方便調試 Boss 登場)
                    distance = 950 * 50;
                } else if (e.getCode() == KeyCode.F4) {
                    // F4: 強制在恐龍前方丟下一個問號箱
                    QuestionBlock qb = new QuestionBlock(dino.getHitBoxBounds().getMaxX() + 50, GameConfig.GROUND_Y);
                    questionBlocks.add(qb);
                    int idx = root.getChildren().indexOf(milkFog);
                    if (idx != -1) {
                        root.getChildren().add(idx, qb.getView());
                    } else {
                        root.getChildren().add(qb.getView());
                    }
                } else if (e.getCode() == KeyCode.F5) {
                    // F5: 強制觸發 Boss 登場
                    if (!bossPhase) {
                        triggerBossPhase();
                    }
                } else if (e.getCode() == KeyCode.F6) {
                    // 秒殺 Boss
                    if (bossPhase && boss != null) {
                        boss.takeDamage(99999);
                    }
                }
            }
        });

        // 按鍵釋放偵測
        scene.setOnKeyReleased(e -> {
            if (isPaused)
                return;

            if (e.getCode() == KeyCode.SPACE) {
                spacePressed = false;
            }
            if (e.getCode() == KeyCode.UP) {
                upPressed = false;
                if (!gameOver) {
                    // 釋放跳躍鍵：觸發速度衰減，支持微操長短跳
                    dino.releaseJump();
                }
            }
            if (e.getCode() == KeyCode.DOWN && !gameOver) {
                dino.releaseDown();
            }
        });
    }

    /**
     * 開啟或關閉暫停選單。暫停期間會停止 AnimationTimer 並靜音 BGM。
     */
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
     * 重設所有遊戲數值，清除畫面殘留元素，重新啟動全新單局。
     */
    private void restartGame() {
        SoundManager.stopGameBgm(); 
        gameOver = false;
        score = 0;
        distance = 0;
        frameCount = 0;
        speed = GameConfig.INITIAL_SPEED; 
        lastScoreSound = 0;
        jumpAfterRestart = false;
        waitingToStart = true;
        signpost.setLayoutX(300);
        dino.showHint("按空白鍵或上鍵開始！");
        activeGameTime = 0;
        lastFrameTime = 0;

        // 清除場上的 Boss 與子彈
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
        nextBossScore = GameConfig.BOSS_TRIGGER_SCORE;
        inBossGracePeriod = false;

        lastQuestionBlockScore = 0;
        barrierActive = false;
        milkFog.setVisible(false);
        dino.getView().setOpacity(1.0);

        // 清空問號箱與金幣
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

        // 重置玩家技能庫存
        GameConfig.goldenAppleCount = 0;
        GameConfig.milkBucketCount = 0;
        GameConfig.enchantedBookCount = 0;
        GameConfig.barrierCount = 0;
        GameConfig.woodenSwordCount = 0;
        skillDisplay.update();

        gameOverImage.setVisible(false);
        restartImage.setVisible(false);
        gameOverMenuBtn.setVisible(false);

        // 重設恐龍角色物理屬性
        dino.reset();
        heartDisplay.update(dino.getLives());

        // 重新排布初始障礙物
        resetAllObstacles();
        scoreDisplay.update(score, sessionHighScore);
    }

    /**
     * 根據当前速度與基本間距，重新排布所有仙人掌/小鳥的位置。
     */
    private void resetAllObstacles() {
        double minDistance = GameConfig.OBSTACLE_MIN_DISTANCE_BASE + speed * GameConfig.OBSTACLE_DISTANCE_SPEED_RATIO;
        double startX = score == 0 ? 850 : GameConfig.SCREEN_WIDTH + minDistance;
        for (ObstacleSlot obstacle : obstacles) {
            obstacle.reset(startX, score, groundY);
            double randomDistance = Math.random() * GameConfig.OBSTACLE_MAX_RANDOM_DISTANCE;
            startX += minDistance + randomDistance;
        }
    }

    /**
     * 隨機賦予玩家一項方塊開出的道具，機率按照 GameConfig 設定的權重執行。
     */
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

    /**
     * 木劍技能：遍歷目前畫面上所有障礙物，將其 View 節點移除，
     * 並增加額外加分，隨後向後方再補回新的障礙物以維持插槽數量。
     */
    private void clearScreenObstacles() {
        java.util.Iterator<ObstacleSlot> iterator = obstacles.iterator();
        java.util.List<ObstacleSlot> replacementObstacles = new java.util.ArrayList<>();

        while (iterator.hasNext()) {
            ObstacleSlot obstacle = iterator.next();
            double obsMinX = obstacle.getHitBoxBounds().getMinX();
            double obsMaxX = obstacle.getHitBoxBounds().getMaxX();

            // 僅清除螢幕內可見區段的障礙物，避免誤傷即將生成的未出現障礙物
            if (obsMaxX > 0 && obsMinX < GameConfig.SCREEN_WIDTH) {
                root.getChildren().remove(obstacle.getCactus().getView());
                root.getChildren().remove(obstacle.getBird().getView());

                iterator.remove();

                distance += GameConfig.OBSTACLE_CLEAR_SCORE * 50;

                // 準備在末尾補回一個插槽物件
                replacementObstacles.add(new ObstacleSlot(GameConfig.SCREEN_WIDTH, groundY));
            }
        }

        // 把補回的障礙物加入畫布，確保 Z-index 渲染層級在 UI 倒數層之下
        int insertIndex = root.getChildren().indexOf(milkFog);
        if (insertIndex == -1)
            insertIndex = root.getChildren().size();

        for (ObstacleSlot newSlot : replacementObstacles) {
            root.getChildren().add(insertIndex, newSlot.getCactus().getView());
            root.getChildren().add(insertIndex + 1, newSlot.getBird().getView());
            obstacles.add(newSlot);
            resetObstacle(newSlot); 
        }
    }

    /**
     * 檢測即將生成的新雲朵與當前畫面上其他雲朵是否會重疊過密。
     */
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

            // 檢測水平 80 像素、垂直 30 像素的安全邊界
            boolean overlapX = newX < otherX + otherWidth + 80 && newX + cloudWidth + 80 > otherX;
            boolean overlapY = newY < otherY + otherHeight + 30 && newY + cloudHeight + 30 > otherY;

            if (overlapX && overlapY)
                return true;
        }
        return false;
    }

    /**
     * 將飄出螢幕左側的雲朵重置回右側隨機位置，會嘗試避開其他雲朵以防堆疊重合。
     */
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

    /**
     * 在指定位置產生浮空並逐漸淡出的文字動畫（如傷害、金幣加成、血量加成等）。
     */
    private void showFloatingText(String text, double x, double y) {
        Label label = new Label(text);
        label.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
        label.setTextFill(Color.web("#FFD54F"));
        label.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 3, 0, 1, 1);");
        label.setLayoutX(x);
        label.setLayoutY(y);
        root.getChildren().add(label);

        // 垂直向上漂移位移 Transition
        TranslateTransition translate = new TranslateTransition(Duration.millis(600), label);
        translate.setByY(-50);

        // 透明度漸變淡出 Transition
        FadeTransition fade = new FadeTransition(Duration.millis(600), label);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        // 平行合併播放動畫，結束後從畫布中將其銷毀
        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.setOnFinished(e -> root.getChildren().remove(label));
        parallel.play();
    }

    /**
     * 計算安全的金幣生成 X 位置，避免金幣直接落在障礙物正上方導致玩家起跳時直接撞死。
     * @return 一個安全合規的 X 座標
     */
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

    /**
     * 拋出骨頭迴力鏢技能（僅在 Boss 戰階段被空白鍵觸發）。
     * 會動態計算與當前 Boss 的間距以調整最大伸展距離。
     */
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

        // 迴力鏢碰撞箱
        boomerangHitBox = new Rectangle(0, 0, 32, 32);
        boomerangHitBox.setVisible(false);

        // 封裝成 JavaFX Group 節點
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

        // 設定起點座標
        boomerangGroup.setLayoutX(startX);
        boomerangGroup.setLayoutY(startY);
        boomerangStartX = startX;
        boomerangStartY = startY;

        boomerangActive = true;
        boomerangTime = 0.0;
        boomerangHasDamaged = false;

        SoundManager.playScore();

        // 把迴力鏢加到最上層以防被其他節點遮蓋
        root.getChildren().add(boomerangGroup);
    }

    /**
     * 取得遊戲場景的主 Pane 視圖。
     */
    public Pane getView() {
        return root;
    }
}
