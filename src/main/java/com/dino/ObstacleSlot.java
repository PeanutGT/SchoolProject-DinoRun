package com.dino;

import javafx.geometry.Bounds;

public class ObstacleSlot {

    public enum Type {
        CACTUS,
        BIRD
    }

    private Obstacle cactus;
    private Bird bird;
    private Type type;

    private double x;

    public ObstacleSlot(double x, double groundY) {
        this.x = x;

        cactus = new Obstacle(x, groundY, 5);
        bird = new Bird(x, groundY - 130);

        type = Type.CACTUS;

        cactus.setVisible(true);
        bird.setVisible(false);
    }

    public Bounds getHitBoxBounds() {
        if (type == Type.CACTUS) {
            return cactus.getHitBox().localToScene(
                    cactus.getHitBox().getBoundsInLocal()
            );
        } else {
            return bird.getHitBox().localToScene(
                    bird.getHitBox().getBoundsInLocal()
            );
        }
    }

    public void update(double speed, double dtSeconds) {
        if (type == Type.CACTUS) {
            cactus.update(speed, dtSeconds);
            x = cactus.getX();
        } else {
            bird.update(speed, dtSeconds);
            x = bird.getX();
        }
    }

    public void reset(double newX, int score, double groundY) {
        x = newX;

        if (score < 300) {
            setCactus(newX);
            return;
        }

        if (Math.random() < 0.35) {
            setBird(newX, randomBirdY(groundY));
        } else {
            setCactus(newX);
        }
    }

    private void setCactus(double newX) {
        type = Type.CACTUS;

        cactus.setVisible(true);
        bird.setVisible(false);

        cactus.reset(newX);
    }

    private void setBird(double newX, double y) {
        type = Type.BIRD;

        cactus.setVisible(false);
        bird.setVisible(true);

        bird.reset(newX, y);
    }

    private double randomBirdY(double groundY) {
        double r = Math.random();

        if (r < 0.4) {
            // 高飛龍：站著可以通過
            return groundY - 90;
        } else if (r < 0.8) {
            // 中飛龍：蹲下可以過，也可以跳過
            return groundY - 60;
        } else {
            // 貼地飛龍：跳過
            return groundY - 35;
        }
    }

    public double getX() {
        return x;
    }

    public double getWidth() {
        if (type == Type.CACTUS) {
            return cactus.getWidth();
        } else {
            return 80;
        }
    }

    public Obstacle getCactus() {
        return cactus;
    }

    public Bird getBird() {
        return bird;
    }

    public Type getType() {
        return type;
    }
}