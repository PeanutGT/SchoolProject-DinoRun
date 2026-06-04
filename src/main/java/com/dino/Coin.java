package com.dino;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 遊戲金幣類別。
 * 負責渲染硬幣外觀（自訂圖片或向量漸層回退方案）、
 * 實現水平移動以及金幣磁鐵吸附功能（當距離小於磁鐵半徑時，朝恐龍中心靠攏），
 * 並提供硬幣自轉的 3D 擬真縮放動畫（藉由正弦波調變 scaleX）。
 */
public class Coin {
    private Group group;         // JavaFX 群組節點
    private Circle outerCircle;  // 外圈碰撞圓形（隱形或回退邊框）
    private Circle innerCircle;  // 內圈裝飾圓形（向量回退用）
    private Text text;           // "C" 裝飾字樣（向量回退用）
    private Group coinArt;       // 存放硬幣繪圖組件的群組，用來執行旋轉動畫

    private double x;            // 當前 X 軸座標
    private double y;            // 當前 Y 軸座標
    private final double radius = 18; // 金幣半徑
    private double spinTime = Math.random() * 10; // 隨機初始角度，避開全部金幣同步自轉的單調感

    /**
     * 建構子：初始化金幣座標，優先載入 coin.png 圖片，若資源不存在則自動採用高品質向量繪製方案。
     * @param x 誕生點 X
     * @param y 誕生點 Y
     */
    public Coin(double x, double y) {
        this.x = x;
        this.y = y;

        boolean imageLoaded = false;
        coinArt = new Group();

        try {
            // 嘗試解析載入專案的 assets/coin.png 圖檔
            String path = "/com/dino/assets/coin.png";
            if (ResourceManager.class.getResource(path) != null) {
                Image coinImage = ResourceManager.getImage("coin.png");
                if (coinImage != null && !coinImage.isError()) {
                    ImageView imageView = new ImageView(coinImage);
                    imageView.setSmooth(false);
                    imageView.setFitWidth(radius * 2);
                    imageView.setFitHeight(radius * 2);
                    // 讓圖片置中於 (0, 0)
                    imageView.setX(-radius);
                    imageView.setY(-radius);
                    
                    coinArt.getChildren().add(imageView);

                    // 用於碰撞檢測的隱形碰撞圓形
                    outerCircle = new Circle(radius);
                    outerCircle.setFill(Color.TRANSPARENT);
                    outerCircle.setStroke(Color.TRANSPARENT); 
                    coinArt.getChildren().add(outerCircle);

                    imageLoaded = true;
                }
            }
        } catch (Exception e) {
            // 發生異常時直接吞掉，自動轉由向量回退方案繪製
        }

        if (!imageLoaded) {
            // 向量金幣繪製方案（金黃色放射性漸層，橘紅框與中央 C 文字）
            RadialGradient goldGrad = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFF59D")), // 淺黃亮區
                new Stop(1, Color.web("#FBC02D"))  // 深黃暗區
            );

            outerCircle = new Circle(radius, goldGrad);
            outerCircle.setStroke(Color.web("#F57F17")); // 橘紅邊線
            outerCircle.setStrokeWidth(3);

            innerCircle = new Circle(radius - 4, goldGrad);
            innerCircle.setStroke(Color.web("#FFEE58"));
            innerCircle.setStrokeWidth(1.5);

            text = new Text("C");
            text.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
            text.setFill(Color.web("#E65100")); 
            text.setX(-6.5);
            text.setY(6.5);

            coinArt.getChildren().addAll(outerCircle, innerCircle, text);
        }

        group = new Group(coinArt);
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    /**
     * 每影格更新邏輯。處理磁鐵吸引（朝恐龍中心拉取）或普通向左滾動，以及藉由 Sin 計算調變 X 軸寬度，產生自轉動畫。
     * @param speed 地面滾動速度
     * @param dtSeconds 經過秒數
     * @param dinoCenterX 恐龍中心 X
     * @param dinoCenterY 恐龍中心 Y
     * @param magnetRadius 當前吃到的磁鐵半徑
     */
    public void update(double speed, double dtSeconds, double dinoCenterX, double dinoCenterY, double magnetRadius) {
        double currentCenterX = x;
        double currentCenterY = y;

        if (magnetRadius > 0) {
            // 計算與恐龍的直線距離
            double dx = dinoCenterX - currentCenterX;
            double dy = dinoCenterY - currentCenterY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            // 進入磁鐵吸引磁場範圍
            if (dist < magnetRadius) {
                // 磁力拉扯速度：隨距離拉近而大幅提高加速度
                double pullSpeed = 500.0 + (magnetRadius - dist) * 1.5;
                x += (dx / dist) * pullSpeed * dtSeconds;
                y += (dy / dist) * pullSpeed * dtSeconds;
            } else {
                // 磁場外：普通背景捲動
                x -= speed * dtSeconds;
            }
        } else {
            x -= speed * dtSeconds;
        }

        group.setLayoutX(x);
        group.setLayoutY(y);

        // 藉由 sine 波縮放 scaleX，展示擬真自轉動畫
        spinTime += dtSeconds * 8.0;
        coinArt.setScaleX(Math.sin(spinTime));
    }

    /**
     * 判定金幣是否已完全滾出左方螢幕。
     */
    public boolean isOffScreen() {
        return x < -radius * 2;
    }

    /**
     * 取得金幣圓形碰撞矩形邊界。
     */
    public Bounds getHitBoxBounds() {
        return outerCircle.localToScene(outerCircle.getBoundsInLocal());
    }

    /**
     * 取得金幣 JavaFX 視圖。
     */
    public Group getView() {
        return group;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
