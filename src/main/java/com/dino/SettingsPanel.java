package com.dino;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/**
 * 遊戲設定面板 UI 類別。
 * 繼承自 VBox。提供主音量、背景音樂與音效音量的拉條（Slider）調整、
 * 切換全螢幕複選框（與 Stage 全螢幕監聽雙向綁定）、以及開啟/關閉開發者除錯模式。
 */
public class SettingsPanel extends VBox {

    /**
     * 建構子：繪製音量控制、全螢幕與開發者模式的元件，並註冊相應的值變化監聽器。
     */
    public SettingsPanel() {
        super(15);
        this.setAlignment(Pos.CENTER);

        // ==========================================
        // 音量調整區塊
        // ==========================================
        VBox volumeBox = new VBox(10);
        volumeBox.setAlignment(Pos.CENTER);

        Label masterVolLabel = new Label("主音量");
        Slider masterVolSlider = new Slider(0, 1, GameConfig.masterVolume);
        masterVolSlider.setMaxWidth(200);
        masterVolSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 滑動更新全域主音量
            SoundManager.setMasterVolume(newVal.doubleValue());
        });

        Label sfxVolLabel = new Label("音效音量");
        Slider sfxVolSlider = new Slider(0, 1, GameConfig.sfxVolume);
        sfxVolSlider.setMaxWidth(200);
        sfxVolSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 滑動更新音效分音量
            SoundManager.setSfxVolume(newVal.doubleValue());
        });

        Label musicVolLabel = new Label("音樂音量");
        Slider musicVolSlider = new Slider(0, 1, GameConfig.musicVolume);
        musicVolSlider.setMaxWidth(200);
        musicVolSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 滑動更新音樂分音量
            SoundManager.setMusicVolume(newVal.doubleValue());
        });

        volumeBox.getChildren().addAll(masterVolLabel, masterVolSlider, sfxVolLabel, sfxVolSlider, musicVolLabel, musicVolSlider);

        // ==========================================
        // 全螢幕切換複選框
        // ==========================================
        CheckBox fullScreenCheck = new CheckBox("全螢幕 (Full Screen)");
        fullScreenCheck.setSelected(GameConfig.isFullScreen);
        
        fullScreenCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            GameConfig.isFullScreen = newVal;
            // 獲取當前主舞台視窗以變更全螢幕狀態
            if (fullScreenCheck.getScene() != null && fullScreenCheck.getScene().getWindow() instanceof javafx.stage.Stage) {
                javafx.stage.Stage stage = (javafx.stage.Stage) fullScreenCheck.getScene().getWindow();
                stage.setFullScreen(newVal);
            }
        });

        // 確保按 ESC 退出全螢幕時（Stage 自帶的退出行為），CheckBox 的勾選狀態能自動同步更新
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

        // ==========================================
        // 開發者 Debug 模式複選框
        // ==========================================
        CheckBox devModeCheck = new CheckBox("啟用開發者模式");
        devModeCheck.setSelected(GameConfig.devModeEnabled);
        devModeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            GameConfig.devModeEnabled = newVal;
        });

        this.getChildren().addAll(volumeBox, fullScreenCheck, devModeCheck);
    }
}
