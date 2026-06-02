package com.dino;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SettingsPanel extends VBox {

    public SettingsPanel() {
        super(15);
        this.setAlignment(Pos.CENTER);

        // 音量調整
        VBox volumeBox = new VBox(10);
        volumeBox.setAlignment(Pos.CENTER);

        Label masterVolLabel = new Label("主音量");
        Slider masterVolSlider = new Slider(0, 1, GameConfig.masterVolume);
        masterVolSlider.setMaxWidth(200);
        masterVolSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.setMasterVolume(newVal.doubleValue());
        });

        Label sfxVolLabel = new Label("音效音量");
        Slider sfxVolSlider = new Slider(0, 1, GameConfig.sfxVolume);
        sfxVolSlider.setMaxWidth(200);
        sfxVolSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.setSfxVolume(newVal.doubleValue());
        });

        Label musicVolLabel = new Label("音樂音量");
        Slider musicVolSlider = new Slider(0, 1, GameConfig.musicVolume);
        musicVolSlider.setMaxWidth(200);
        musicVolSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.setMusicVolume(newVal.doubleValue());
        });

        volumeBox.getChildren().addAll(masterVolLabel, masterVolSlider, sfxVolLabel, sfxVolSlider, musicVolLabel, musicVolSlider);

        // 介面大小
        Label sizeLabel = new Label("介面大小 (需重啟遊戲)");
        HBox sizeButtons = new HBox(10);
        sizeButtons.setAlignment(Pos.CENTER);
        Button sizeNormal = new Button("100%");
        Button sizeLarge = new Button("120%");
        sizeNormal.setOnAction(e -> GameConfig.uiScale = 1.0);
        sizeLarge.setOnAction(e -> GameConfig.uiScale = 1.2);
        sizeButtons.getChildren().addAll(sizeNormal, sizeLarge);

        // 開發者模式
        CheckBox devModeCheck = new CheckBox("啟用開發者模式");
        devModeCheck.setSelected(GameConfig.devModeEnabled);
        devModeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            GameConfig.devModeEnabled = newVal;
        });

        this.getChildren().addAll(volumeBox, sizeLabel, sizeButtons, devModeCheck);
    }
}
