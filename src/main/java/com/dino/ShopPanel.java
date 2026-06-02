package com.dino;

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

public class ShopPanel extends VBox {

    private MainMenuController controller;
    private Label coinLabel;
    
    // 升級項目卡片列表
    private UpgradeCard cardLives;
    private UpgradeCard cardMagnet;
    private UpgradeCard cardMultiplier;
    private UpgradeCard cardJumps;
    private UpgradeCard cardRegen;
    private UpgradeCard cardMoreCoins;

    // 分頁元件
    private int currentPage = 1;
    private GridPane grid;
    private Button prevBtn;
    private Button nextBtn;
    private Label pageLabel;

    public ShopPanel(MainMenuController controller) {
        this.controller = controller;

        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);
        this.setPadding(new Insets(12));
        
        // 復古精緻淺色米質背景樣式
        this.setStyle(
            "-fx-background-color: #efebe9; " + // 淺米色 / 羊皮紙色
            "-fx-border-color: #5d4037; " +      // 深邊框
            "-fx-border-width: 4; " +
            "-fx-border-style: solid; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 4; " +
            "-fx-padding: 12;"
        );
        this.setMaxSize(640, 435); // 縮小商店尺寸，使其更加精緻與緊湊

        // 標題
        Label titleLabel = new Label("★ 恐龍進化商店 ★");
        titleLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#3e2723")); // 改為深褐色以確保高對比度
        DropShadow titleShadow = new DropShadow(2, Color.web("#d7ccc8"));
        titleLabel.setEffect(titleShadow);

        // 金幣餘額顯示
        coinLabel = new Label();
        coinLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 18));
        coinLabel.setTextFill(Color.web("#5d4037")); // 改為深褐色以確保高對比度
        updateCoinLabel();

        // 網格佈局放置商品
        grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // 1. 永久生命
        cardLives = new UpgradeCard(
            "永久生命上限",
            "開始遊戲時獲得額外生命（上限 6）",
            new int[]{10, 25, 50}, // 升級費用
            3,
            () -> SaveManager.getLivesLevel(),
            (lvl) -> SaveManager.setLivesLevel(lvl),
            () -> "目前: " + (3 + SaveManager.getLivesLevel()) + " HP"
        );

        // 2. 金幣磁鐵
        cardMagnet = new UpgradeCard(
            "金幣磁鐵效果",
            "自動吸引一定半徑內的所有金幣",
            new int[]{15, 30, 60},
            3,
            () -> SaveManager.getMagnetLevel(),
            (lvl) -> SaveManager.setMagnetLevel(lvl),
            () -> {
                double rad = SaveManager.getMagnetRadius();
                return rad > 0 ? "目前吸引半徑: " + (int)rad + "px" : "目前無磁力";
            }
        );

        // 3. 金幣倍率
        cardMultiplier = new UpgradeCard(
            "金幣獲取倍率",
            "獲得金幣時的倍率加成（最高 5 倍）",
            new int[]{20, 40, 80},
            3,
            () -> SaveManager.getMultiplierLevel(),
            (lvl) -> SaveManager.setMultiplierLevel(lvl),
            () -> "目前倍率: " + SaveManager.getCoinMultiplier() + "x"
        );

        // 4. 永久多段跳
        cardJumps = new UpgradeCard(
            "永久空中多段跳",
            "初始即可在空中多段跳躍，不需吃書",
            new int[]{30, 70},
            2,
            () -> SaveManager.getExtraJumpsLevel(),
            (lvl) -> SaveManager.setExtraJumpsLevel(lvl),
            () -> "目前空中跳躍: " + (1 + SaveManager.getExtraJumpsLevel()) + " 次"
        );

        // 6. 緩慢自動回血
        cardRegen = new UpgradeCard(
            "緩慢自動回血",
            "每過 40 秒自動回復 1 點生命（不超過上限）",
            new int[]{100},
            1,
            () -> SaveManager.getRegenLevel(),
            (lvl) -> SaveManager.setRegenLevel(lvl),
            () -> SaveManager.hasRegen() ? "目前狀態: 已啟用" : "目前狀態: 未啟用"
        );

        // 7. 提高金幣頻率
        cardMoreCoins = new UpgradeCard(
            "提高金幣頻率",
            "金幣生成的頻率提升一倍（每20分生成一次）",
            new int[]{100},
            1,
            () -> SaveManager.getMoreCoinsLevel(),
            (lvl) -> SaveManager.setMoreCoinsLevel(lvl),
            () -> SaveManager.hasMoreCoins() ? "目前狀態: 已啟用" : "目前狀態: 未啟用"
        );

        // 分頁按鈕與控制列
        HBox pageBox = new HBox(15);
        pageBox.setAlignment(Pos.CENTER);

        prevBtn = new Button("[ 上一頁 ]");
        prevBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #5d4037; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 14; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        prevBtn.setOnMouseEntered(e -> {
            if (!prevBtn.isDisable()) {
                prevBtn.setStyle(
                    "-fx-background-color: #5d4037; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Courier New'; " +
                    "-fx-font-size: 14; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                );
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
                    "-fx-cursor: hand;"
                );
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
            "-fx-cursor: hand;"
        );
        nextBtn.setOnMouseEntered(e -> {
            if (!nextBtn.isDisable()) {
                nextBtn.setStyle(
                    "-fx-background-color: #5d4037; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Courier New'; " +
                    "-fx-font-size: 14; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                );
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
                    "-fx-cursor: hand;"
                );
            }
        });
        nextBtn.setOnAction(e -> {
            if (currentPage < 2) {
                currentPage++;
                SoundManager.playJump();
                updatePageUI();
            }
        });

        pageBox.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        // 底部按鈕區塊
        HBox bottomBox = new HBox(20);
        bottomBox.setAlignment(Pos.CENTER);

        // 返回按鈕
        Button backBtn = new Button("[ 返回主選單 ]");
        backBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #5d4037; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 16; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
            "-fx-background-color: #5d4037; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 16; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        ));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #5d4037; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 16; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        ));
        backBtn.setOnAction(e -> {
            SoundManager.playJump(); // 播放點選音效
            controller.showMainButtons();
        });

        // 開發者按鈕 (像素風紅橘配色)
        Button devBtn = new Button("[ 開發者：+100金幣 ]");
        devBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #c62828; " + // 醒目的紅色
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 16; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        devBtn.setOnMouseEntered(e -> devBtn.setStyle(
            "-fx-background-color: #c62828; " +
            "-fx-text-fill: #ffd54f; " + // 黃金字體
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 16; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        ));
        devBtn.setOnMouseExited(e -> devBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #c62828; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 16; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        ));
        devBtn.setOnAction(e -> {
            SaveManager.addCoins(100);
            updateCoinLabel();
            SoundManager.playScore(); // 播放得分音效
            
            // 即時更新所有卡片的購買按鈕狀態與當前數值
            cardLives.updateUI();
            cardMagnet.updateUI();
            cardMultiplier.updateUI();
            cardJumps.updateUI();
            cardRegen.updateUI();
            cardMoreCoins.updateUI();
        });

        bottomBox.getChildren().addAll(backBtn, devBtn);

        this.getChildren().addAll(titleLabel, coinLabel, grid, pageBox, bottomBox);
        updatePageUI();
    }

    private void updateCoinLabel() {
        coinLabel.setText("★ 目前擁有金幣: " + SaveManager.getCoins() + " ★");
    }

    private void updatePageUI() {
        grid.getChildren().clear();
        if (currentPage == 1) {
            grid.add(cardLives, 0, 0);
            grid.add(cardMagnet, 1, 0);
            grid.add(cardMultiplier, 0, 1);
            grid.add(cardJumps, 1, 1);
            
            prevBtn.setDisable(true);
            prevBtn.setOpacity(0.4);
            nextBtn.setDisable(false);
            nextBtn.setOpacity(1.0);
        } else {
            grid.add(cardRegen, 0, 0);
            grid.add(cardMoreCoins, 1, 0);
            
            prevBtn.setDisable(false);
            prevBtn.setOpacity(1.0);
            nextBtn.setDisable(true);
            nextBtn.setOpacity(0.4);
        }
        pageLabel.setText("頁面: " + currentPage + " / 2");
    }

    // 升級項目的卡片元件
    private class UpgradeCard extends VBox {
        private String name;
        private String description;
        private int[] costs;
        private int maxLevel;
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
                java.util.function.Supplier<String> getStatusSupplier
        ) {
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
                "-fx-background-color: #d7ccc8; " + // 淺米褐色，卡片底色
                "-fx-border-color: #8d6e63; " +
                "-fx-border-width: 2; " +
                "-fx-background-radius: 6; " +
                "-fx-border-radius: 4;"
            );

            // 商品名稱
            Label nameLabel = new Label(name);
            nameLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 14));
            nameLabel.setTextFill(Color.web("#3e2723")); // 改為深褐色以確保高對比度

            // 目前等級進度條 (例如 [★][★][☆])
            levelStarsLabel = new Label();
            levelStarsLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 11));
            levelStarsLabel.setTextFill(Color.web("#e65100")); // 活力橘色

            // 描述
            Label descLabel = new Label(description);
            descLabel.setFont(Font.font("Microsoft JhengHei", FontWeight.NORMAL, 11));
            descLabel.setTextFill(Color.web("#5d4037")); // 舒適的深棕色
            descLabel.setWrapText(true);
            descLabel.setMinHeight(24);

            // 當前狀態 (例如 "目前 HP: 4")
            statusLabel = new Label();
            statusLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
            statusLabel.setTextFill(Color.web("#006064")); // 深青色，醒目提示

            // 購買按鈕
            buyButton = new Button();
            buyButton.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 12));
            buyButton.setPrefWidth(256);

            this.getChildren().addAll(nameLabel, levelStarsLabel, descLabel, statusLabel, buyButton);
            updateUI();
        }

        public void updateUI() {
            int currentLevel = getLevelSupplier.get();
            
            // 更新星星進度條
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

            // 更新當前狀態資訊
            statusLabel.setText(getStatusSupplier.get());

            // 處理購買按鈕邏輯
            if (currentLevel >= maxLevel) {
                // 已封頂
                buyButton.setText("已封頂 (MAX)");
                buyButton.setDisable(true);
                buyButton.setStyle(
                    "-fx-background-color: #cfd8dc; " + // 淺灰藍，標準淺色滿等樣式
                    "-fx-text-fill: #90a4ae; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: default;"
                );
                buyButton.setOnMouseEntered(null);
                buyButton.setOnMouseExited(null);
                buyButton.setOnAction(null);
            } else {
                int cost = costs[currentLevel];
                buyButton.setText(String.format("升級: %d 金幣", cost));
                
                int playerCoins = SaveManager.getCoins();
                if (playerCoins >= cost) {
                    // 可以購買
                    buyButton.setDisable(false);
                    buyButton.setStyle(
                        "-fx-background-color: #2e7d32; " + // 寶石綠
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
                    );
                    
                    buyButton.setOnMouseEntered(e -> buyButton.setStyle(
                        "-fx-background-color: #388e3c; " + // 亮綠色 hover
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
                    ));
                    buyButton.setOnMouseExited(e -> buyButton.setStyle(
                        "-fx-background-color: #2e7d32; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
                    ));
                    
                    buyButton.setOnAction(e -> {
                        // 扣除硬幣並升級
                        if (SaveManager.spendCoins(cost)) {
                            setLevelConsumer.accept(currentLevel + 1);
                            SoundManager.playAppleSound(); // 播放升級成功清脆音效！
                            
                            // 更新整個商店 UI
                            ShopPanel.this.updateCoinLabel();
                            ShopPanel.this.cardLives.updateUI();
                            ShopPanel.this.cardMagnet.updateUI();
                            ShopPanel.this.cardMultiplier.updateUI();
                            ShopPanel.this.cardJumps.updateUI();
                            ShopPanel.this.cardRegen.updateUI();
                            ShopPanel.this.cardMoreCoins.updateUI();
                        }
                    });
                } else {
                    // 金幣不足
                    buyButton.setDisable(true);
                    buyButton.setStyle(
                        "-fx-background-color: #e0e0e0; " + // 淺灰白，標準淺色金幣不足樣式
                        "-fx-text-fill: #9e9e9e; " +         // 灰色字體
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: default;"
                    );
                    buyButton.setOnMouseEntered(null);
                    buyButton.setOnMouseExited(null);
                    buyButton.setOnAction(null);
                }
            }
        }
    }
}
