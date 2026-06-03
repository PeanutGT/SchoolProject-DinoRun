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

public class Coin {
    private Group group;
    private Circle outerCircle;
    private Circle innerCircle;
    private Text text;
    private Group coinArt;

    private double x;
    private double y;
    private final double radius = 18;
    private double spinTime = Math.random() * 10; // 隨機初始角度，避免所有硬幣完全同步旋轉

    public Coin(double x, double y) {
        this.x = x;
        this.y = y;

        boolean imageLoaded = false;
        coinArt = new Group();

        try {
            // 檢查 coin.png 是否存在於資源路徑下
            String path = "/com/dino/assets/coin.png";
            if (ResourceManager.class.getResource(path) != null) {
                Image coinImage = ResourceManager.getImage("coin.png");
                if (coinImage != null && !coinImage.isError()) {
                    ImageView imageView = new ImageView(coinImage);
                    imageView.setSmooth(false);
                    imageView.setFitWidth(radius * 2);
                    imageView.setFitHeight(radius * 2);
                    imageView.setX(-radius);
                    imageView.setY(-radius);
                    
                    coinArt.getChildren().add(imageView);

                    // 用於碰撞檢測的隱形圓形
                    outerCircle = new Circle(radius);
                    outerCircle.setFill(Color.TRANSPARENT);
                    outerCircle.setStroke(Color.TRANSPARENT); // 設為完全透明，消除灰黑色圈框問題
                    coinArt.getChildren().add(outerCircle);

                    imageLoaded = true;
                }
            }
        } catch (Exception e) {
            // 忽略，繼續使用向量回退
        }

        if (!imageLoaded) {
            // 向量金幣回退方案（當沒有自訂圖片時）
            RadialGradient goldGrad = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFF59D")), // 淺黃
                new Stop(1, Color.web("#FBC02D"))  // 深金
            );

            outerCircle = new Circle(radius, goldGrad);
            outerCircle.setStroke(Color.web("#F57F17")); // 橘紅邊框，強調像素/立體感
            outerCircle.setStrokeWidth(3);

            innerCircle = new Circle(radius - 4, goldGrad);
            innerCircle.setStroke(Color.web("#FFEE58"));
            innerCircle.setStrokeWidth(1.5);

            text = new Text("C");
            text.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
            text.setFill(Color.web("#E65100")); // 深橘色文字
            text.setX(-6.5);
            text.setY(6.5);

            coinArt.getChildren().addAll(outerCircle, innerCircle, text);
        }

        group = new Group(coinArt);
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    public void update(double speed, double dtSeconds, double dinoCenterX, double dinoCenterY, double magnetRadius) {
        // 1. 移動與磁力吸引
        double currentCenterX = x;
        double currentCenterY = y;

        if (magnetRadius > 0) {
            double dx = dinoCenterX - currentCenterX;
            double dy = dinoCenterY - currentCenterY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist < magnetRadius) {
                // 磁力吸引：平滑向恐龍靠攏，吸引速度隨距離拉近變快
                double pullSpeed = 500.0 + (magnetRadius - dist) * 1.5;
                x += (dx / dist) * pullSpeed * dtSeconds;
                y += (dy / dist) * pullSpeed * dtSeconds;
            } else {
                // 一般向左移動
                x -= speed * dtSeconds;
            }
        } else {
            // 一般向左移動
            x -= speed * dtSeconds;
        }

        group.setLayoutX(x);
        group.setLayoutY(y);

        // 2. 3D 旋轉動畫
        spinTime += dtSeconds * 8.0;
        coinArt.setScaleX(Math.sin(spinTime));
    }

    public boolean isOffScreen() {
        return x < -radius * 2;
    }

    public Bounds getHitBoxBounds() {
        // 使用 outerCircle 作為碰撞體
        return outerCircle.localToScene(outerCircle.getBoundsInLocal());
    }

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
