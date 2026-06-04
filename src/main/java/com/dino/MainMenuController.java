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

/**
 * 主選單畫面控制器類別。
 * 負責處理選單按鈕事件（單人、雙人合作、對戰、排行榜、商店、設定與離開），
 * 以及展示動態捲動的遊戲背景動畫（白雲、地面、仙人掌與飛鳥的自主位移）。
 */
public class MainMenuController {

    @FXML
    private StackPane rootBox; // 根容器，用來覆蓋彈出面板（如排行榜）

    @FXML
    private Pane backgroundPane; // 存放捲動背景元件的畫布

    @FXML
    private VBox menuContent; // 選單面板主要內容容器

    @FXML
    private VBox mainContent; // 預設的按鈕面板（單人、雙人等）

    private DinoMain dinoMain; // 主程式參考，用於引導場景切換
    
    // 背景動畫物件列表
    private final List<ImageView> clouds = new ArrayList<>();
    private final List<ImageView> groundImages = new ArrayList<>();
    private final List<Obstacle> cactuses = new ArrayList<>();
    private final List<Bird> birds = new ArrayList<>();
    
    private AnimationTimer backgroundTimer; // 動畫計時器
    private double backgroundSpeed = GameConfig.INITIAL_SPEED; // 當前背景滾動速度

    /**
     * JavaFX 自動呼叫的初始化方法。
     * 設定背景剪裁區域、建立捲動背景、啟動計時器與播放主選單背景音樂。
     */
    @FXML
    private void initialize() {
        // 限制背景顯示寬高，防止溢出舞台
        backgroundPane.setMaxSize(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        backgroundPane.setClip(new Rectangle(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        menuContent.setMaxSize(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        
        // 建立選單畫面的滾動背景素材
        createAnimatedBackground();
        // 啟動動畫滾動
        startBackgroundAnimation();
        // 播放選單背景音樂
        SoundManager.playMenuBgm();
    }

    /**
     * 設定 DinoMain 執行個體以便能進行場景切換。
     * @param dinoMain 主程式實例
     */
    public void setDinoMain(DinoMain dinoMain) {
        this.dinoMain = dinoMain;
    }

    /**
     * 按下「單人遊戲」按鈕時呼叫。
     */
    @FXML
    private void startGame() {
        if (dinoMain != null) {
            SoundManager.stopMenuBgm();
            stopBackgroundAnimation();
            dinoMain.startSinglePlayerGame();
        }
    }

    /**
     * 按下「雙人合作」按鈕時呼叫。
     */
    @FXML
    private void startCoopGame() {
        if (dinoMain != null) {
            SoundManager.stopMenuBgm();
            dinoMain.startCoopGame();
        }
    }

    /**
     * 按下「雙人對戰」按鈕時呼叫。
     */
    @FXML
    private void startVersusGame() {
        if (dinoMain != null) {
            SoundManager.stopMenuBgm();
            stopBackgroundAnimation();
            dinoMain.startVersusGame();
        }
    }

    // 排行榜顯示容器
    private VBox leaderboardContentContainer;
    // 記錄目前在排行榜分頁選中的是單人榜還是雙人榜
    private boolean isCoopLeaderboard = false;

    /**
     * 按下「排行榜」按鈕時呼叫。
     * 動態建立排行榜的彈出面板並覆蓋在主畫面上。
     */
    @FXML
    private void showLeaderboard() {
        // 建立排行榜面板的外觀結構
        VBox leaderboardPanel = new VBox(15);
        leaderboardPanel.setAlignment(Pos.CENTER);
        leaderboardPanel.setMaxSize(500, 520);
        leaderboardPanel.setStyle("-fx-background-color: #3e2723; -fx-border-color: #d7ccc8; -fx-border-width: 4; -fx-border-style: solid; -fx-padding: 15;");

        // 標題 Label
        Label title = new Label("🏆 TOP DINOS 🏆");
        title.setStyle("-fx-text-fill: #ffd54f; -fx-font-family: 'Courier New'; -fx-font-size: 28; -fx-font-weight: bold;");

        // 排行榜分頁按鈕 (單人榜 vs 雙人榜)
        HBox tabBox = new HBox(10);
        tabBox.setAlignment(Pos.CENTER);
        Button singleBtn = new Button("單人榜");
        Button coopBtn = new Button("雙人榜");
        
        // 分頁按鈕樣式設定
        String activeStyle = "-fx-background-color: #ffca28; -fx-text-fill: #3e2723; -fx-font-family: 'Courier New'; -fx-font-weight: bold;";
        String inactiveStyle = "-fx-background-color: #5d4037; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-weight: bold;";
        
        singleBtn.setStyle(activeStyle);
        coopBtn.setStyle(inactiveStyle);

        leaderboardContentContainer = new VBox(10);
        leaderboardContentContainer.setAlignment(Pos.CENTER);

        // 單人分頁切換邏輯
        singleBtn.setOnAction(e -> {
            isCoopLeaderboard = false;
            singleBtn.setStyle(activeStyle);
            coopBtn.setStyle(inactiveStyle);
            updateLeaderboardContent();
        });

        // 雙人分頁切換邏輯
        coopBtn.setOnAction(e -> {
            isCoopLeaderboard = true;
            singleBtn.setStyle(inactiveStyle);
            coopBtn.setStyle(activeStyle);
            updateLeaderboardContent();
        });

        tabBox.getChildren().addAll(singleBtn, coopBtn);
        leaderboardPanel.getChildren().addAll(title, tabBox, leaderboardContentContainer);

        // 載入並繪製排行榜名單
        updateLeaderboardContent();

        // 建立返回按鈕
        Button closeBtn = new Button("[ 返回主選單 ]");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #5d4037; -fx-text-fill: #ffca28; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand;"));
        closeBtn.setOnAction(e -> rootBox.getChildren().remove(leaderboardPanel));

        leaderboardPanel.getChildren().add(closeBtn);
        rootBox.getChildren().add(leaderboardPanel);
    }
    
    /**
     * 讀取並更新排行榜的 UI 名冊內容，為前三名建立領獎台(Podium)效果。
     */
    private void updateLeaderboardContent() {
        leaderboardContentContainer.getChildren().clear();
        // 自 SaveManager/LeaderboardManager 載入資料
        List<LeaderboardManager.ScoreEntry> scores = LeaderboardManager.loadTopScores(isCoopLeaderboard);
        
        if (scores.isEmpty()) {
            Label emptyLabel = new Label("尚無任何分數紀錄！");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 16;");
            leaderboardContentContainer.getChildren().add(emptyLabel);
        } else {
            HBox podiumBox = new HBox(15);
            podiumBox.setAlignment(Pos.BOTTOM_CENTER);
            
            // 建立冠亞季軍柱狀台
            VBox firstPlace = createPodiumSpot(scores, 0, "#ffca28", "冠軍", 100);
            VBox secondPlace = createPodiumSpot(scores, 1, "#e0e0e0", "亞軍", 80);
            VBox thirdPlace = createPodiumSpot(scores, 2, "#bcaaa4", "季軍", 60);
            
            if (secondPlace != null) podiumBox.getChildren().add(secondPlace);
            if (firstPlace != null) podiumBox.getChildren().add(firstPlace);
            if (thirdPlace != null) podiumBox.getChildren().add(thirdPlace);
            
            leaderboardContentContainer.getChildren().add(podiumBox);

            // 若有第 4 名（索引 3）以後的分數，使用滾動面板以列表形式顯示
            if (scores.size() > 3) {
                VBox listVBox = new VBox(8);
                listVBox.setAlignment(Pos.TOP_CENTER);
                listVBox.setStyle("-fx-background-color: transparent;");
                for (int i = 3; i < scores.size(); i++) {
                    LeaderboardManager.ScoreEntry entry = scores.get(i);
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    
                    HBox charactersBox = new HBox(5);
                    charactersBox.setAlignment(Pos.CENTER);
                    
                    // 玩家一的角色頭像
                    ImageView imgView1 = new ImageView(getCharacterImage(entry.characterType));
                    imgView1.setFitWidth(24);
                    imgView1.setPreserveRatio(true);
                    imgView1.setSmooth(false);
                    charactersBox.getChildren().add(imgView1);
                    
                    // 雙人模式下玩家二的角色頭像
                    if (isCoopLeaderboard && entry.characterType2 != null) {
                        ImageView imgView2 = new ImageView(getCharacterImage(entry.characterType2));
                        imgView2.setFitWidth(24);
                        imgView2.setPreserveRatio(true);
                        imgView2.setSmooth(false);
                        charactersBox.getChildren().add(imgView2);
                    }
                    
                    Label infoLabel = new Label(String.format("%2d. %-15s %6d", (i + 1), entry.name, entry.score));
                    infoLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 16; -fx-font-weight: bold;");
                    
                    row.getChildren().addAll(charactersBox, infoLabel);
                    listVBox.getChildren().add(row);
                }
                
                ScrollPane scrollPane = new ScrollPane(listVBox);
                scrollPane.setPrefViewportHeight(130);
                scrollPane.setMaxWidth(400);
                scrollPane.setStyle("-fx-background: #4e342e; -fx-background-color: transparent; -fx-padding: 10; -fx-border-color: #d7ccc8; -fx-border-width: 2;");
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setFitToWidth(true);
                
                leaderboardContentContainer.getChildren().add(scrollPane);
            }
        }
    }
    
    /**
     * 輔助建立排行榜中前三名領獎台的個別元件。
     */
    private VBox createPodiumSpot(List<LeaderboardManager.ScoreEntry> scores, int index, String color, String titleText, double baseHeight) {
        if (index >= scores.size()) return null;
        LeaderboardManager.ScoreEntry entry = scores.get(index);
        
        VBox spot = new VBox(5);
        spot.setAlignment(Pos.BOTTOM_CENTER);
        
        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New'; -fx-font-size: 18; -fx-font-weight: bold;");
        
        HBox charactersBox = new HBox(5);
        charactersBox.setAlignment(Pos.BOTTOM_CENTER);
        
        // 冠軍頭像略大
        ImageView imgView1 = new ImageView(getCharacterImage(entry.characterType));
        imgView1.setFitWidth(index == 0 ? 48 : 36);
        imgView1.setPreserveRatio(true);
        imgView1.setSmooth(false);
        charactersBox.getChildren().add(imgView1);
        
        if (isCoopLeaderboard && entry.characterType2 != null) {
            ImageView imgView2 = new ImageView(getCharacterImage(entry.characterType2));
            imgView2.setFitWidth(index == 0 ? 48 : 36);
            imgView2.setPreserveRatio(true);
            imgView2.setSmooth(false);
            charactersBox.getChildren().add(imgView2);
        }
        
        Label nameLabel = new Label(entry.name);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 14; -fx-font-weight: bold;");
        
        Label scoreLabel = new Label(String.valueOf(entry.score));
        scoreLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New'; -fx-font-size: 16; -fx-font-weight: bold;");
        
        VBox base = new VBox();
        base.setAlignment(Pos.CENTER);
        base.setPrefSize(100, baseHeight);
        base.setStyle("-fx-background-color: #5d4037; -fx-border-color: #8d6e63; -fx-border-width: 2;");
        base.getChildren().addAll(nameLabel, scoreLabel);
        
        spot.getChildren().addAll(titleLabel, charactersBox, base);
        return spot;
    }

    /**
     * 依據角色識別名稱獲取對應的第一張跑步圖檔，用於排行榜展示。
     */
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

    /**
     * 按下「商店」按鈕時呼叫。
     * 清空選單介面並載入商店控制面板。
     */
    @FXML
    private void openShop() {
        menuContent.getChildren().clear();
        ShopPanel shopPanel = new ShopPanel(this);
        menuContent.getChildren().add(shopPanel);
    }

    /**
     * 按下「遊戲設定」按鈕時呼叫。
     * 清空選單介面並載入設定面板。
     */
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

    /**
     * 按下「離開遊戲」時呼叫。
     */
    @FXML
    private void exitGame() {
        Platform.exit();
    }

    /**
     * 還原顯示預設的主按鈕群。
     */
    void showMainButtons() {
        menuContent.getChildren().clear();
        menuContent.getChildren().add(mainContent);
    }

    /**
     * 建立背景所需的動態素材，包括雲朵、拼接地面、以及點綴性的仙人掌與小鳥。
     */
    private void createAnimatedBackground() {
        backgroundPane.getChildren().clear();
        clouds.clear();
        groundImages.clear();
        cactuses.clear();
        birds.clear();

        // 加入雲朵
        Image cloudImage = ResourceManager.getImage("cloud.png");
        addCloud(cloudImage, 250, 90, 80);
        addCloud(cloudImage, 500, 120, 70);
        addCloud(cloudImage, 720, 75, 90);

        // 加入兩片地面以進行無縫拼接循環捲動
        Image groundImage = ResourceManager.getImage("ground.png");
        addGround(groundImage, 0);
        addGround(groundImage, GameConfig.SCREEN_WIDTH);

        // 預設在背景放入幾株仙人掌與小鳥作為動態裝飾
        addCactus(250);
        addCactus(500);
        addCactus(750);
        addBird(670, GameConfig.GROUND_Y - 60);
        addBird(880, GameConfig.GROUND_Y - 90);
    }

    /**
     * 新增一片白雲至背景中。
     */
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

    /**
     * 新增一片地面至背景中。
     */
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

    /**
     * 新增一株仙人掌裝飾至背景。
     */
    private void addCactus(double x) {
        Obstacle cactus = new Obstacle(x, GameConfig.GROUND_Y, 5);
        cactuses.add(cactus);
        backgroundPane.getChildren().add(cactus.getView());
    }

    /**
     * 新增一隻裝飾飛鳥至背景。
     */
    private void addBird(double x, double y) {
        Bird bird = new Bird(x, y);
        birds.add(bird);
        backgroundPane.getChildren().add(bird.getView());
    }

    /**
     * 啟動背景物件的移動動畫計時器 (AnimationTimer)。
     */
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

    /**
     * 計算每個背景物件的水平位移，並於移出螢幕時將其重設回右側，實現無縫滾動。
     */
    private void updateBackground(double dtSeconds) {
        // 背景速度隨著時間微幅加速
        backgroundSpeed = Math.min(GameConfig.MAX_SPEED, backgroundSpeed + GameConfig.ACCELERATION * dtSeconds);

        // 雲朵以較慢的速度移動，產生遠景深度差 (Parallax)
        for (ImageView cloud : clouds) {
            cloud.setX(cloud.getX() - backgroundSpeed * 0.25 * dtSeconds);
            if (cloud.getX() < -100) {
                cloud.setX(GameConfig.SCREEN_WIDTH + Math.random() * 240);
            }
        }
        
        // 雙地面拼接位移邏輯
        if (groundImages.size() >= 2) {
            ImageView g1 = groundImages.get(0);
            ImageView g2 = groundImages.get(1);
            
            g1.setX(g1.getX() - backgroundSpeed * dtSeconds);
            g2.setX(g2.getX() - backgroundSpeed * dtSeconds);

            // 若第一塊地面完全滾出左方，則挪到第二塊地面右側拼接
            if (g1.getX() <= -GameConfig.SCREEN_WIDTH) {
                g1.setX(g2.getX() + GameConfig.SCREEN_WIDTH);
            }
            // 反之亦然
            if (g2.getX() <= -GameConfig.SCREEN_WIDTH) {
                g2.setX(g1.getX() + GameConfig.SCREEN_WIDTH);
            }
        }

        // 移動仙人掌
        for (Obstacle cactus : cactuses) {
            cactus.update(backgroundSpeed, dtSeconds);
            if (cactus.getX() < -cactus.getWidth()) {
                cactus.reset(GameConfig.SCREEN_WIDTH + Math.random() * 500);
            }
        }

        // 移動裝飾飛鳥
        for (Bird bird : birds) {
            bird.update(backgroundSpeed, dtSeconds);
            if (bird.getX() < -50) {
                bird.reset(GameConfig.SCREEN_WIDTH + Math.random() * 500, GameConfig.GROUND_Y - 60 - Math.random() * 30);
            }
        }
    }

    /**
     * 停止背景動畫計時器。
     */
    private void stopBackgroundAnimation() {
        if (backgroundTimer != null) {
            backgroundTimer.stop();
        }
    }
}
