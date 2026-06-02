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

        // 全螢幕切換
        CheckBox fullScreenCheck = new CheckBox("全螢幕 (Full Screen)");
        fullScreenCheck.setSelected(GameConfig.isFullScreen);
        
        fullScreenCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            GameConfig.isFullScreen = newVal;
            if (fullScreenCheck.getScene() != null && fullScreenCheck.getScene().getWindow() instanceof javafx.stage.Stage) {
                javafx.stage.Stage stage = (javafx.stage.Stage) fullScreenCheck.getScene().getWindow();
                stage.setFullScreen(newVal);
            }
        });

        // 確保按 ESC 退出全螢幕時能同步更新 UI 狀態
        fullScreenCheck.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                    if (newWin instanceof javafx.stage.Stage) {
                        javafx.stage.Stage stage = (javafx.stage.Stage) newWin;
                        stage.fullScreenProperty().addListener((obsFS, oldFS, newFS) -> {
                            GameConfig.isFullScreen = newFS;
                            fullScreenCheck.setSelected(newFS);
                        });
                    }
                });
            }
        });

        // 開發者模式
        CheckBox devModeCheck = new CheckBox("啟用開發者模式");
        devModeCheck.setSelected(GameConfig.devModeEnabled);
        devModeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            GameConfig.devModeEnabled = newVal;
        });

        this.getChildren().addAll(volumeBox, fullScreenCheck, devModeCheck);
    }
}
