package com.dino;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class NewBoss extends Boss {
    public enum State {
        IDLE, PRE_ATTACK, ATTACK
    }

    private Image[] walkFrames;
    private State currentState = State.IDLE;
    private int walkFrameCounter = 0;
    private double projectileSpeed;

    public NewBoss(Pane root, long activeGameTime, boolean isCoop) {
        // A slightly taller/slimmer boss as placeholder (e.g. width=70, height=90), with 120 HP
        super(root, activeGameTime, isCoop, 70, 90, isCoop ? 240 : 120);
        this.projectileSpeed = isCoop ? 300.0 : 240.0;

        // Reuses bowser walk frames for now, or you can replace them with your own images (e.g. boss_new_walk1.png)
        walkFrames = new Image[] {
                ResourceManager.getImage("boss_bowser_walk1.png"),
                ResourceManager.getImage("boss_bowser_walk2.png")
        };
        visual.setImage(walkFrames[0]);
        // Visual effect: slightly transparent to distinguish it as a ghost/new boss
        visual.setOpacity(0.8);
    }

    @Override
    public String getName() {
        return "Skeleton King";
    }

    @Override
    protected void updateBoss(double speed, long activeGameTime, double dtSeconds) {
        long now = activeGameTime;
        updateWalkAnimation();

        // Simple custom AI for the new boss
        switch (currentState) {
            case IDLE:
                x = screenWidth - 160;
                y = groundY - height;

                if (now - stateTimer > 1500) {
                    currentState = State.PRE_ATTACK;
                    stateTimer = now;
                }
                break;
            case PRE_ATTACK:
                // Pre-attack charge warning: starts shaking slightly
                x = screenWidth - 160 + Math.sin(now * 0.05) * 5;
                if (now - stateTimer > 800) {
                    currentState = State.ATTACK;
                    stateTimer = now;
                    fireProjectiles();
                }
                break;
            case ATTACK:
                x = screenWidth - 160;
                if (now - stateTimer > 600) {
                    currentState = State.IDLE;
                    stateTimer = now;
                }
                break;
        }

        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    private void updateWalkAnimation() {
        walkFrameCounter++;
        if (walkFrameCounter % 15 == 0) {
            int index = (walkFrameCounter / 15) % walkFrames.length;
            visual.setImage(walkFrames[index]);
        }
    }

    private void fireProjectiles() {
        // Fires two quick low fireballs
        BossProjectile p1 = new BossProjectile(
                x,
                groundY - 30,
                40,
                40,
                projectileSpeed,
                new String[] { "boss_fireball_3.png", "boss_fireball_4.png" },
                false);
        projectiles.add(p1);
        root.getChildren().add(p1.getView());
    }
}
