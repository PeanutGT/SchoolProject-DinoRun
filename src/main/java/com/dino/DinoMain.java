package com.dino;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;

/**
 * 遊戲主入口類別，負責 JavaFX 舞台初始化、全螢幕設定、場景切換以及解析度縮放適配。
 */
public class DinoMain extends Application {

    // JavaFX 主舞台
    private Stage stage;

    @Override
    public void start(Stage stage) {
        // 隱藏全螢幕提示文字
        stage.setFullScreenExitHint("");
        // 禁用預設的全螢幕退出快捷鍵 (Esc)
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        // 初始化音效管理員
        SoundManager.init();

        this.stage = stage;
        // 顯示主選單
        showMainMenu();
    }

    /**
     * 載入並顯示主選單畫面。
     */
    public void showMainMenu() {
        try {
            // 載入主選單 FXML 檔案
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dino/MainMenu.fxml"));
            Parent root = loader.load();

            // 取得控制器並傳入主程式實例以便進行場景切換
            MainMenuController controller = loader.getController();
            controller.setDinoMain(this);

            stage.setTitle("JavaFX Dino Game");
            // 使用縮放包裝器以確保在不同視窗大小下畫面比例正常
            setScenePreservingWindowMode(wrapWithScale(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 建立一個自動縮放的 StackPane 包裝器，使內容能夠等比例縮放以適應視窗大小。
     * @param root 要包裝的根節點
     * @return 縮放後的 StackPane 包裝節點
     */
    private javafx.scene.layout.StackPane wrapWithScale(Parent root) {
        javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane();
        wrapper.setStyle("-fx-background-color: white;");
        wrapper.getChildren().add(root);

        // 監聽寬高變化以動態計算縮放比例
        javafx.beans.value.ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> {
            double w = wrapper.getWidth();
            double h = wrapper.getHeight();
            if (w <= 0 || h <= 0) return;
            
            // 計算寬高縮放比
            double scaleX = w / GameConfig.SCREEN_WIDTH;
            double scaleY = h / GameConfig.SCREEN_HEIGHT;
            double minScale = Math.min(scaleX, scaleY);
            // 設定縮放值
            root.setScaleX(minScale);
            root.setScaleY(minScale);
        };
        wrapper.widthProperty().addListener(sizeListener);
        wrapper.heightProperty().addListener(sizeListener);
        
        return wrapper;
    }

    /**
     * 啟動單人遊戲的角色選擇畫面。
     */
    public void startSinglePlayerGame() {
        showCharacterSelect(CharacterSelectScene.Mode.SINGLE);
    }

    /**
     * 根據選擇的角色啟動單人遊戲。
     * @param character 選擇的角色名稱
     */
    public void startSinglePlayerGame(String character) {
        // 建立單人遊戲場景
        GameScene gameScene = new GameScene(this, character);
        setScenePreservingWindowMode(gameScene.getView());

        // 設定按鍵控制監聽器
        gameScene.setKeyControl(stage.getScene());

        // 請求焦點以確保按鍵輸入生效
        Platform.runLater(() -> gameScene.getView().requestFocus());
    }

    /**
     * 在保留當前視窗狀態（全螢幕/視窗化）的前提下切換場景根節點。
     * @param newRoot 新的根節點
     */
    private void setScenePreservingWindowMode(Parent newRoot) {
        if (stage.getScene() == null) {
            // 若無場景則建立新場景，使用 GameConfig 的解析度設定
            Scene scene = new Scene(newRoot, GameConfig.getScreenWidth(), GameConfig.getScreenHeight());
            // 載入全域樣式表
            scene.getStylesheets().add(getClass().getResource("/com/dino/theme.css").toExternalForm());
            stage.setScene(scene);
        } else {
            // 若已有場景則直接替換根節點，避免視窗模式重置
            stage.getScene().setRoot(newRoot);
        }
    }

    /**
     * 啟動雙人合作模式的角色選擇畫面。
     */
    public void startCoopGame() {
        showCharacterSelect(CharacterSelectScene.Mode.COOP);
    }

    /**
     * 啟動雙人合作模式遊戲。
     * @param playerOneCharacter 玩家一選擇的角色
     * @param playerTwoCharacter 玩家二選擇的角色
     */
    public void startCoopGame(String playerOneCharacter, String playerTwoCharacter) {
        CoopGameScene coopScene = new CoopGameScene(this, playerOneCharacter, playerTwoCharacter);
        setScenePreservingWindowMode(coopScene.getView());

        coopScene.setKeyControl(stage.getScene());

        Platform.runLater(() -> coopScene.getView().requestFocus());
    }

    /**
     * 啟動雙人對戰模式的角色選擇畫面。
     */
    public void startVersusGame() {
        showCharacterSelect(CharacterSelectScene.Mode.VERSUS);
    }

    /**
     * 啟動雙人對戰模式遊戲。
     * @param playerOneCharacter 玩家一選擇的角色
     * @param playerTwoCharacter 玩家二選擇的角色
     */
    public void startVersusGame(String playerOneCharacter, String playerTwoCharacter) {
        VersusGameScene versusGameScene = new VersusGameScene(this, playerOneCharacter, playerTwoCharacter);
        setScenePreservingWindowMode(versusGameScene.getView());

        versusGameScene.setKeyControl(stage.getScene());

        Platform.runLater(() -> versusGameScene.getView().requestFocus());
    }

    /**
     * 顯示角色選擇畫面。
     * @param mode 遊戲模式 (單人 / 合作 / 對戰)
     */
    private void showCharacterSelect(CharacterSelectScene.Mode mode) {
        CharacterSelectScene characterSelectScene = new CharacterSelectScene(this, mode);
        // 使用縮放包裝器包裝角色選擇畫面
        setScenePreservingWindowMode(wrapWithScale(characterSelectScene.getView()));

        characterSelectScene.setKeyControl(stage.getScene());

        Platform.runLater(() -> characterSelectScene.getView().requestFocus());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
