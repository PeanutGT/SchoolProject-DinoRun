package com.dino;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;

/**
 * 商店面板 UI 類別。
 * 繼承自 VBox。提供一個美觀的實體卡片式進化商店，分為三個分頁：
 * 第一頁：基礎數值進化（生命上限、金幣磁鐵、獲取倍率、開局跳躍）。
 * 第二頁：進階特殊技能（自動回血、金幣出現頻率、問號方塊幸運頻率）。
 * 第三頁：角色解鎖抽獎（扭蛋機解鎖 Mario, Luigi, Kirby 等皮膚）。
 * 支援開發者快速加幣調試，並會動態刷新所有購買按鈕狀態與餘額顯示。
 */
public class ShopPanel extends VBox {

    private MainMenuController controller; // 選單控制器參考
    private Label coinLabel;               // 餘額顯示標籤

    // 商品卡片變數
    private UpgradeCard cardLives;
    private UpgradeCard cardMagnet;
    private UpgradeCard cardMultiplier;
    private UpgradeCard cardJumps;
    private UpgradeCard cardRegen;
    private UpgradeCard cardMoreCoins;
    private UpgradeCard cardQuestionBox;
    private CharacterUnlockCard cardCharUnlock;

    // 分頁變數 (1 ~ 3 頁)
    private int currentPage = 1;
    private GridPane grid;                 // 卡片排布網格
    private Button prevBtn;
    private Button nextBtn;
    private Label pageLabel;

    /**
     * 建構子：初始化面板邊框、米色羊皮紙質地背景樣式、加載所有卡片元件與事件綁定。
     * @param controller 主選單控制器
     */
    public ShopPanel(MainMenuController controller) {
        this.controller = controller;

        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);
        this.setPadding(new Insets(12));

        // 復古精緻淺色米質背景樣式
        this.setStyle(
                "-fx-background-color: #efebe9; " + // 淺米色 / 羊皮紙色
                        "-fx-border-color: #5d4037; " + // 深邊框
                        "-fx-border-width: 4; " +
                        "-fx-border-style: solid; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 4; " +
                        "-fx-padding: 12;");
        this.setMaxSize(640, 435); // 精緻緊湊的視窗規格

        // 商店標題，帶有陰影效果
        Label titleLabel = new Label("★ 恐龍進化商店 ★");
        titleLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#3e2723")); 
        DropShadow titleShadow = new DropShadow(2, Color.web("#d7ccc8"));
        titleLabel.setEffect(titleShadow);

        // 餘額標籤
        coinLabel = new Label();
        coinLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 18));
        coinLabel.setTextFill(Color.web("#5d4037")); 
        updateCoinLabel();

        // 2x2 擺放卡片的網格容器
        grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // ==========================================
        // 初始化各大商品卡片 (傳入費用、段數、讀取寫入邏輯)
        // ==========================================
        
        // 1. 永久生命上限 (最高 Lv.3, +3生命)
        cardLives = new UpgradeCard(
                "永久生命上限",
                "開始遊戲時獲得額外生命（上限 6）",
                new int[] { 10, 25, 50 }, 
                3,
                () -> SaveManager.getLivesLevel(),
                (lvl) -> SaveManager.setLivesLevel(lvl),
                () -> "目前: " + (3 + SaveManager.getLivesLevel()) + " HP");

        // 2. 金幣磁鐵效果 (最高 Lv.3)
        cardMagnet = new UpgradeCard(
                "金幣磁鐵效果",
                "自動吸引一定半徑內的所有金幣",
                new int[] { 15, 30, 60 },
                3,
                () -> SaveManager.getMagnetLevel(),
                (lvl) -> SaveManager.setMagnetLevel(lvl),
                () -> {
                    double rad = SaveManager.getMagnetRadius();
                    return rad > 0 ? "目前吸引半徑: " + (int) rad + "px" : "目前無磁力";
                });

        // 3. 金幣獲取倍率 (最高 5 倍)
        cardMultiplier = new UpgradeCard(
                "金幣獲取倍率",
                "獲得金幣時的倍率加成（最高 5 倍）",
                new int[] { 20, 40, 80 },
                3,
                () -> SaveManager.getMultiplierLevel(),
                (lvl) -> SaveManager.setMultiplierLevel(lvl),
                () -> "目前倍率: " + SaveManager.getCoinMultiplier() + "x");

        // 4. 開局額外跳躍 (最多額外多 3 次)
        cardJumps = new UpgradeCard(
                "開局額外跳躍",
                "開始遊戲時獲得額外跳躍次數（最多 3 次）",
                new int[] { 50, 100, 150 },
                3,
                () -> SaveManager.getExtraJumpsLevel(),
                (lvl) -> SaveManager.setExtraJumpsLevel(lvl),
                () -> "目前開局額外跳躍: " + SaveManager.getExtraJumps() + " 次");

        // 5. 緩慢自動回血 (最高 Lv.3, 分別為 40/20/10 秒回復 1HP)
        cardRegen = new UpgradeCard(
                "緩慢自動回血",
                "Lv1:40秒, Lv2:20秒, Lv3:10秒回復1點生命",
                new int[] { 100, 200, 300 },
                3,
                () -> SaveManager.getRegenLevel(),
                (lvl) -> SaveManager.setRegenLevel(lvl),
                () -> {
                    int lvl = SaveManager.getRegenLevel();
                    if (lvl == 1) return "目前狀態: 40秒回血";
                    if (lvl == 2) return "目前狀態: 20秒回血";
                    if (lvl == 3) return "目前狀態: 10秒回血";
                    return "目前狀態: 未啟用";
                });

        // 6. 提高金幣頻率 (Lv.1 封頂, 100 元)
        cardMoreCoins = new UpgradeCard(
                "提高金幣頻率",
                "金幣生成的頻率提升一倍（每20分生成一次）",
                new int[] { 100 },
                1,
                () -> SaveManager.getMoreCoinsLevel(),
                (lvl) -> SaveManager.setMoreCoinsLevel(lvl),
                () -> SaveManager.hasMoreCoins() ? "目前狀態: 已啟用" : "目前狀態: 未啟用");

        // 7. 幸運問號箱出現頻率
        cardQuestionBox = new UpgradeCard(
                "問號箱出現頻率",
                "Lv1:每200分出現 Lv2:每150分出現 Lv3:每100分出現",
                new int[] { 100, 200, 300 },
                3,
                () -> SaveManager.getQuestionBoxLevel(),
                (lvl) -> SaveManager.setQuestionBoxLevel(lvl),
                () -> {
                    int lvl = SaveManager.getQuestionBoxLevel();
                    if (lvl == 1) return "目前: 每 200 分出現";
                    if (lvl == 2) return "目前: 每 150 分出現";
                    if (lvl == 3) return "目前: 每 100 分出現";
                    return "目前狀態: 未啟用 (預設每250分)";
                });

        // 8. 角色扭蛋機卡片
        cardCharUnlock = new CharacterUnlockCard();

        // ==========================================
        // 分頁控制面板 (上一頁、下一頁)
        // ==========================================
        HBox pageBox = new HBox(15);
        pageBox.setAlignment(Pos.CENTER);

        prevBtn = new Button("[ 上一頁 ]");
        prevBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #5d4037; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        prevBtn.setOnMouseEntered(e -> {
            if (!prevBtn.isDisable()) {
                prevBtn.setStyle(
                        "-fx-background-color: #5d4037; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-family: 'Courier New'; " +
                                "-fx-font-size: 14; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand;");
            }
        });
        prevBtn.setOnMouseExited(e -> {
            if (!prevBtn.isDisable()) {
                prevBtn.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #5d4037; " +
                                "-fx-font-family: 'Courier New'; " +
                                "-fx-font-size: 14; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand;");
            }
        });
        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                SoundManager.playJump();
                updatePageUI();
            }
        });

        pageLabel = new Label();
        pageLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 14));
        pageLabel.setTextFill(Color.web("#5d4037"));

        nextBtn = new Button("[ 下一頁 ]");
        nextBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #5d4037; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        nextBtn.setOnMouseEntered(e -> {
            if (!nextBtn.isDisable()) {
                nextBtn.setStyle(
                        "-fx-background-color: #5d4037; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-family: 'Courier New'; " +
                                "-fx-font-size: 14; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand;");
            }
        });
        nextBtn.setOnMouseExited(e -> {
            if (!nextBtn.isDisable()) {
                nextBtn.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #5d4037; " +
                                "-fx-font-family: 'Courier New'; " +
                                "-fx-font-size: 14; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: hand;");
            }
        });
        nextBtn.setOnAction(e -> {
            if (currentPage < 3) {
                currentPage++;
                SoundManager.playJump();
                updatePageUI();
            }
        });

        pageBox.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        // ==========================================
        // 底部操控選單：返回按鈕與開發者作弊捷徑
        // ==========================================
        HBox bottomBox = new HBox(20);
        bottomBox.setAlignment(Pos.CENTER);

        // 返回
        Button backBtn = new Button("[ 返回主選單 ]");
        backBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #5d4037; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
                "-fx-background-color: #5d4037; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #5d4037; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"));
        backBtn.setOnAction(e -> {
            SoundManager.playJump();
            controller.showMainButtons();
        });

        // 測試用捷徑按鈕 (每次直接增加 100 金幣)
        Button devBtn = new Button("[ 開發者：+100金幣 ]");
        devBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #c62828; " + 
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
        devBtn.setOnMouseEntered(e -> devBtn.setStyle(
                "-fx-background-color: #c62828; " +
                        "-fx-text-fill: #ffd54f; " + 
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"));
        devBtn.setOnMouseExited(e -> devBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #c62828; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"));
        devBtn.setOnAction(e -> {
            SaveManager.addCoins(100);
            updateCoinLabel();
            SoundManager.playScore(); 

            // 一鍵更新所有商品 UI 卡片狀態
            cardLives.updateUI();
            cardMagnet.updateUI();
            cardMultiplier.updateUI();
            cardJumps.updateUI();
            cardRegen.updateUI();
            cardMoreCoins.updateUI();
            cardQuestionBox.updateUI();
            cardCharUnlock.updateUI();
        });

        bottomBox.getChildren().addAll(backBtn, devBtn);

        this.getChildren().addAll(titleLabel, coinLabel, grid, pageBox, bottomBox);
        // 初始化繪製第一分頁
        updatePageUI();
    }

    /**
     * 更新擁有金幣 Label。
     */
    private void updateCoinLabel() {
        coinLabel.setText("★ 目前擁有金幣: " + SaveManager.getCoins() + " ★");
    }

    /**
     * 切換分頁時重新加入卡片 Node，並設定上一頁下一頁的可選用性。
     */
    private void updatePageUI() {
        grid.getChildren().clear();
        if (currentPage == 1) {
            // 第一頁：四大屬性
            grid.add(cardLives, 0, 0);
            grid.add(cardMagnet, 1, 0);
            grid.add(cardMultiplier, 0, 1);
            grid.add(cardJumps, 1, 1);

            prevBtn.setDisable(true);
            prevBtn.setOpacity(0.4);
            nextBtn.setDisable(false);
            nextBtn.setOpacity(1.0);
        } else if (currentPage == 2) {
            // 第二頁：進階功能
            grid.add(cardRegen, 0, 0);
            grid.add(cardMoreCoins, 1, 0);
            grid.add(cardQuestionBox, 0, 1);

            prevBtn.setDisable(false);
            prevBtn.setOpacity(1.0);
            nextBtn.setDisable(false);
            nextBtn.setOpacity(1.0);
        } else {
            // 第三頁：扭蛋解鎖
            grid.add(cardCharUnlock, 0, 0, 2, 1); // 橫跨兩格展示

            prevBtn.setDisable(false);
            prevBtn.setOpacity(1.0);
            nextBtn.setDisable(true);
            nextBtn.setOpacity(0.4);
        }
        pageLabel.setText("頁面: " + currentPage + " / 3");
    }

    /**
     * 單個升級項目卡片的內層 VBox 元件。
     */
    private class UpgradeCard extends VBox {
        private String name;
        private String description;
        private int[] costs;            // 每級升級費用陣列 (例如 10, 25, 50)
        private int maxLevel;           // 最大滿級數
        private java.util.function.Supplier<Integer> getLevelSupplier;
        private java.util.function.Consumer<Integer> setLevelConsumer;
        private java.util.function.Supplier<String> getStatusSupplier;

        private Label levelStarsLabel;
        private Label statusLabel;
        private Button buyButton;

        public UpgradeCard(
                String name,
                String description,
                int[] costs,
                int maxLevel,
                java.util.function.Supplier<Integer> getLevelSupplier,
                java.util.function.Consumer<Integer> setLevelConsumer,
                java.util.function.Supplier<String> getStatusSupplier) {
            this.name = name;
            this.description = description;
            this.costs = costs;
            this.maxLevel = maxLevel;
            this.getLevelSupplier = getLevelSupplier;
            this.setLevelConsumer = setLevelConsumer;
            this.getStatusSupplier = getStatusSupplier;

            this.setAlignment(Pos.CENTER_LEFT);
            this.setSpacing(4);
            this.setPadding(new Insets(6, 12, 6, 12));
            this.setPrefWidth(280);
            this.setStyle(
                    "-fx-background-color: #d7ccc8; " + // 淺米褐色
                            "-fx-border-color: #8d6e63; " +
                            "-fx-border-width: 2; " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-radius: 4;");

            Label nameLabel = new Label(name);
            nameLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 14));
            nameLabel.setTextFill(Color.web("#3e2723")); 

            // 等級進度條 Label
            levelStarsLabel = new Label();
            levelStarsLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 11));
            levelStarsLabel.setTextFill(Color.web("#e65100")); 

            Label descLabel = new Label(description);
            descLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.NORMAL, 11));
            descLabel.setTextFill(Color.web("#5d4037")); 
            descLabel.setWrapText(true);
            descLabel.setMinHeight(24);

            statusLabel = new Label();
            statusLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
            statusLabel.setTextFill(Color.web("#006064")); 

            buyButton = new Button();
            buyButton.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 12));
            buyButton.setPrefWidth(256);

            this.getChildren().addAll(nameLabel, levelStarsLabel, descLabel, statusLabel, buyButton);
            updateUI();
        }

        /**
         * 更新單張卡片元件的 UI。
         * 繪製星號進度、數值狀態、並依照玩家硬幣與滿等狀態改變「購買按鈕」的啟用與色彩（MAX為灰、可買為綠、不夠錢為淡灰）。
         */
        public void updateUI() {
            int currentLevel = getLevelSupplier.get();

            // 星星進度條更新
            StringBuilder sb = new StringBuilder();
            sb.append("等級: ");
            for (int i = 0; i < maxLevel; i++) {
                if (i < currentLevel) {
                    sb.append("★");
                } else {
                    sb.append("☆");
                }
            }
            sb.append(String.format(" (Lv. %d/%d)", currentLevel, maxLevel));
            levelStarsLabel.setText(sb.toString());

            // 狀態數值更新
            statusLabel.setText(getStatusSupplier.get());

            // 判斷按鈕狀態
            if (currentLevel >= maxLevel) {
                // 1. 已封頂
                buyButton.setText("已封頂 (MAX)");
                buyButton.setDisable(true);
                buyButton.setStyle(
                        "-fx-background-color: #cfd8dc; " + 
                                "-fx-text-fill: #90a4ae; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: default;");
                buyButton.setOnMouseEntered(null);
                buyButton.setOnMouseExited(null);
                buyButton.setOnAction(null);
            } else {
                int cost = costs[currentLevel];
                buyButton.setText(String.format("升級: %d 金幣", cost));

                int playerCoins = SaveManager.getCoins();
                if (playerCoins >= cost) {
                    // 2. 餘額足夠：啟用按鈕，設為綠色
                    buyButton.setDisable(false);
                    buyButton.setStyle(
                            "-fx-background-color: #2e7d32; " + // 綠色
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand;");

                    buyButton.setOnMouseEntered(e -> buyButton.setStyle(
                            "-fx-background-color: #388e3c; " + // Hover 亮綠
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand;"));
                    buyButton.setOnMouseExited(e -> buyButton.setStyle(
                            "-fx-background-color: #2e7d32; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand;"));

                    buyButton.setOnAction(e -> {
                        // 扣錢升級，播放音效，並刷新所有商品 UI 卡片
                        if (SaveManager.spendCoins(cost)) {
                            setLevelConsumer.accept(currentLevel + 1);
                            SoundManager.playAppleSound(); 

                            ShopPanel.this.updateCoinLabel();
                            ShopPanel.this.cardLives.updateUI();
                            ShopPanel.this.cardMagnet.updateUI();
                            ShopPanel.this.cardMultiplier.updateUI();
                            ShopPanel.this.cardJumps.updateUI();
                            ShopPanel.this.cardRegen.updateUI();
                            ShopPanel.this.cardMoreCoins.updateUI();
                            ShopPanel.this.cardQuestionBox.updateUI();
                        }
                    });
                } else {
                    // 3. 餘額不足：禁用按鈕，設為灰色
                    buyButton.setDisable(true);
                    buyButton.setStyle(
                            "-fx-background-color: #e0e0e0; " + 
                                    "-fx-text-fill: #9e9e9e; " + 
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: default;");
                    buyButton.setOnMouseEntered(null);
                    buyButton.setOnMouseExited(null);
                    buyButton.setOnAction(null);
                }
            }
        }
    }

    /**
     * 角色解鎖扭蛋卡片元件。
     */
    private class CharacterUnlockCard extends VBox {
        private static final int UNLOCK_COST = 100; // 固定抽獎費用 100 元
        private static final java.util.Map<String, String> CHAR_DISPLAY_NAMES = java.util.Map.of(
                "mario", "MARIO",
                "luigi", "LUIGI",
                "kirby", "KIRBY",
                "lucario", "LUCARIO",
                "sonic", "SONIC",
                "steve", "STEVE"
        );

        private Label statusLabel;
        private Button buyButton;
        private Label resultLabel;

        public CharacterUnlockCard() {
            this.setAlignment(Pos.CENTER);
            this.setSpacing(6);
            this.setPadding(new Insets(8, 12, 8, 12));
            this.setPrefWidth(575);
            this.setStyle(
                    "-fx-background-color: linear-gradient(to right, #fff3e0, #ffe0b2); " +
                            "-fx-border-color: #e65100; " +
                            "-fx-border-width: 2; " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-radius: 4;");

            Label nameLabel = new Label("🎲 角色解鎖扭蛋");
            nameLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 16));
            nameLabel.setTextFill(Color.web("#e65100"));

            Label descLabel = new Label("花費 100 金幣隨機解鎖一位全新角色！可在選角畫面使用。");
            descLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.NORMAL, 12));
            descLabel.setTextFill(Color.web("#5d4037"));

            statusLabel = new Label();
            statusLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
            statusLabel.setTextFill(Color.web("#006064"));

            buyButton = new Button();
            buyButton.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 13));
            buyButton.setPrefWidth(256);

            // 用於展示抽中角色結果的歡慶 Label
            resultLabel = new Label();
            resultLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 14));
            resultLabel.setTextFill(Color.web("#2e7d32"));

            this.getChildren().addAll(nameLabel, descLabel, statusLabel, buyButton, resultLabel);
            updateUI();
        }

        /**
         * 刷新角色扭蛋機的可用狀態。
         */
        public void updateUI() {
            int unlocked = SaveManager.getUnlockedCharactersCount();
            statusLabel.setText("已解鎖角色: " + unlocked + " / 6");

            if (unlocked >= 6) {
                // 全部抽完解鎖
                buyButton.setText("已全部解鎖 ✔");
                buyButton.setDisable(true);
                buyButton.setStyle(
                        "-fx-background-color: #cfd8dc; " +
                                "-fx-text-fill: #90a4ae; " +
                                "-fx-font-weight: bold; " +
                                "-fx-cursor: default;");
                buyButton.setOnMouseEntered(null);
                buyButton.setOnMouseExited(null);
                buyButton.setOnAction(null);
            } else {
                int playerCoins = SaveManager.getCoins();
                buyButton.setText("解鎖隨機角色: " + UNLOCK_COST + " 金幣");

                if (playerCoins >= UNLOCK_COST) {
                    buyButton.setDisable(false);
                    buyButton.setStyle(
                            "-fx-background-color: #e65100; " + // 醒目亮橘
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand;");
                    buyButton.setOnMouseEntered(e -> buyButton.setStyle(
                            "-fx-background-color: #f57c00; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand;"));
                    buyButton.setOnMouseExited(e -> buyButton.setStyle(
                            "-fx-background-color: #e65100; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: hand;"));
                    buyButton.setOnAction(e -> purchaseUnlock());
                } else {
                    buyButton.setDisable(true);
                    buyButton.setStyle(
                            "-fx-background-color: #e0e0e0; " +
                                    "-fx-text-fill: #9e9e9e; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-cursor: default;");
                    buyButton.setOnMouseEntered(null);
                    buyButton.setOnMouseExited(null);
                    buyButton.setOnAction(null);
                }
            }
        }

        /**
         * 購買扭蛋：扣除 100 元，呼叫 SaveManager 隨機解鎖，顯示 3 秒恭喜特效。
         */
        private void purchaseUnlock() {
            if (!SaveManager.spendCoins(UNLOCK_COST)) return;

            String unlockedChar = SaveManager.unlockRandomCharacter();
            if (unlockedChar != null) {
                String displayName = CHAR_DISPLAY_NAMES.getOrDefault(unlockedChar, unlockedChar.toUpperCase());
                resultLabel.setText("🎉 恭喜！你解鎖了 " + displayName + " ！");
                resultLabel.setTextFill(Color.web("#2e7d32"));
                SoundManager.playAppleSound();
            } else {
                resultLabel.setText("所有角色已解鎖！");
                SaveManager.addCoins(UNLOCK_COST); // 退款
            }

            // 更新全部卡片按鈕
            ShopPanel.this.updateCoinLabel();
            ShopPanel.this.cardLives.updateUI();
            ShopPanel.this.cardMagnet.updateUI();
            ShopPanel.this.cardMultiplier.updateUI();
            ShopPanel.this.cardJumps.updateUI();
            ShopPanel.this.cardRegen.updateUI();
            ShopPanel.this.cardMoreCoins.updateUI();
            ShopPanel.this.cardQuestionBox.updateUI();
            this.updateUI();

            // 3 秒後自動隱藏恭喜中獎 Label
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> resultLabel.setText(""));
            pause.play();
        }
    }
}
