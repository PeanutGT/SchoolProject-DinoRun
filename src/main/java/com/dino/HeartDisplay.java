package com.dino;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 心形生命值顯示 UI 類別。
 * 負責繪製左上角代表玩家當前剩餘生命力的愛心圖示。
 * 分為實心心（fullHeartImage）與空心心（emptyHeartImage），並於扣血或回血時動態刷圖。
 */
public class HeartDisplay {

    private Group root;                  // JavaFX 群組節點
    private Image fullHeartImage;        // 實心愛心圖片
    private Image emptyHeartImage;       // 空心愛心圖片

    private List<ImageView> hearts;      // 存放愛心 ImageView 的清單
    private final double heartSize = 24; // 單個愛心繪製大小 (24x24 像素)

    /**
     * 建構子：預設使用商店生命加成上限。
     */
    public HeartDisplay() {
        this(3 + SaveManager.getLivesBonus());
    }

    /**
     * 完整建構子：初始化愛心列表。依據最大生命值生成相應個數的愛心，水平一字排開。
     * @param maxLives 最大生命值
     */
    public HeartDisplay(int maxLives) {
        root = new Group();

        // 使用 ResourceManager 載入 heart_full.png 與 heart_empty.png
        fullHeartImage = ResourceManager.getImage("heart_full.png");
        emptyHeartImage = ResourceManager.getImage("heart_empty.png");
        
        hearts = new ArrayList<>();

        for (int i = 0; i < maxLives; i++) {
            // 每個心橫向間距 30 像素
            ImageView heart = createHeart(30 + i * 30, 25);
            hearts.add(heart);
            root.getChildren().add(heart);
        }

        // 初始化顯示為滿血狀態
        update(maxLives);
    }

    /**
     * 建立個別愛心 ImageView 並設定固定尺寸。
     */
    private ImageView createHeart(double x, double y) {
        ImageView heart = new ImageView(fullHeartImage);
        heart.setSmooth(false);

        heart.setFitWidth(heartSize);
        heart.setFitHeight(heartSize);
        heart.setPreserveRatio(true);

        heart.setX(x);
        heart.setY(y);

        return heart;
    }

    /**
     * 當角色生命扣除或回復時更新愛心圖示。
     * @param lives 當前生命剩餘值
     */
    public void update(int lives) {
        for (int i = 0; i < hearts.size(); i++) {
            ImageView heart = hearts.get(i);
            // 索引小於 lives 顯示滿心，否則顯示空心
            if (i < lives) {
                heart.setImage(fullHeartImage);
            } else {
                heart.setImage(emptyHeartImage);
            }
        }
    }

    /**
     * 取得心形顯示 UI 的群組節點。
     */
    public Group getView() {
        return root;
    }
}