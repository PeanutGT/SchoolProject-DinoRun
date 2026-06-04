package com.dino;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * 道具與技能快捷欄顯示 UI 類別。
 * 負責在遊戲畫面底部中央，渲染出玩家擁有的技能/道具快捷欄面板。
 * 顯示項目包含：黃金蘋果 (Q)、牛奶桶 (W)、附魔書 (E)、屏障 (R)、木劍 (F)。
 * 每個槽位皆標示對應的快捷鍵以及當前剩餘道具數量。
 */
public class SkillDisplay {

    private HBox root;                  // 容納所有道具槽位的總水平容器
    private Label appleCountLabel;      // 黃金蘋果數量標籤
    private Label milkCountLabel;       // 牛奶桶數量標籤
    private Label bookCountLabel;       // 附魔書數量標籤
    private Label barrierCountLabel;    // 屏障數量標籤
    private Label swordCountLabel;      // 木劍數量標籤

    /**
     * 建構子：初始化道具快捷欄。
     * 設定容器的對齊與位置（置於畫面底部中央），建立並加入五個技能槽位。
     */
    public SkillDisplay() {
        root = new HBox(10);
        root.setAlignment(Pos.CENTER);
        
        // 將道具欄置於畫面底部水平中央偏左位置
        root.setLayoutX(GameConfig.SCREEN_WIDTH / 2 - 145); 
        root.setLayoutY(GameConfig.SCREEN_HEIGHT - 70);

        appleCountLabel = new Label("0");
        milkCountLabel = new Label("0");
        bookCountLabel = new Label("0");
        barrierCountLabel = new Label("0");
        swordCountLabel = new Label("0");

        // 依序建立各槽位並加入容器
        root.getChildren().addAll(
            createSlot("tool/golden_apple.png", "Q", appleCountLabel),
            createSlot("tool/milk_bucket.png", "W", milkCountLabel),
            createSlot("tool/enchanted_book.png", "E", bookCountLabel),
            createSlot("tool/barrier.png", "R", barrierCountLabel),
            createSlot("tool/wooden_sword.png", "F", swordCountLabel)
        );
        
        // 初始載入當前擁有的道具數量
        update();
    }

    /**
     * 建立單個道具/技能槽位面板。
     * 每個槽位都是一個固定大小 (54x54) 的 StackPane，內含道具圖示、左上角快捷鍵提示及右下角數量標示。
     * @param imagePath 道具圖示的資源名稱
     * @param key 快捷鍵名稱標籤文字（如 "Q"）
     * @param countLabel 指向當前數量顯示標籤的引用
     * @return 包裝完成的 StackPane 槽位節點
     */
    private StackPane createSlot(String imagePath, String key, Label countLabel) {
        StackPane slot = new StackPane();
        slot.setPrefSize(54, 54);
        slot.setMinSize(54, 54);
        slot.setMaxSize(54, 54);
        // 設定像素風灰色的凹凸立體感框線背景
        slot.setStyle("-fx-background-color: #8b8b8b; -fx-border-color: #373737 #ffffff #ffffff #373737; -fx-border-width: 4; -fx-padding: 5;");

        // 載入並設定道具的 ImageView
        Image img = ResourceManager.getImage(imagePath);
        ImageView imgView = new ImageView(img);
        imgView.setSmooth(false); // 關閉平滑縮放以保持像素風格
        imgView.setFitWidth(32);
        imgView.setFitHeight(32);
        StackPane.setAlignment(imgView, Pos.CENTER);

        // 建立左上角快捷鍵標籤
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 0 2;");
        StackPane.setAlignment(keyLabel, Pos.TOP_LEFT);

        // 設定右下角數量標籤樣式
        countLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 0 2;");
        StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);

        // 合併圖示、快捷鍵與數量標籤至槽位容器中
        slot.getChildren().addAll(imgView, keyLabel, countLabel);
        return slot;
    }

    /**
     * 即時更新所有技能與道具剩餘數量的顯示文字。
     * 直接從 GameConfig 中的全域變數讀取當前道具數。
     */
    public void update() {
        appleCountLabel.setText(String.valueOf(GameConfig.goldenAppleCount));
        milkCountLabel.setText(String.valueOf(GameConfig.milkBucketCount));
        bookCountLabel.setText(String.valueOf(GameConfig.enchantedBookCount));
        barrierCountLabel.setText(String.valueOf(GameConfig.barrierCount));
        swordCountLabel.setText(String.valueOf(GameConfig.woodenSwordCount));
    }

    /**
     * 取得技能快捷欄顯示元件的總 HBox 根節點。
     * @return 包含整個快捷欄 UI 的 HBox 元件
     */
    public HBox getView() {
        return root;
    }
}

