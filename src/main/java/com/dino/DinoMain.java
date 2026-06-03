package com.dino;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;

public class DinoMain extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        SoundManager.init();

        this.stage = stage;
        showMainMenu();
    }

    public void showMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dino/MainMenu.fxml"));
            Parent root = loader.load();

            MainMenuController controller = loader.getController();
            controller.setDinoMain(this);

            stage.setTitle("JavaFX Dino Game");
            setScenePreservingWindowMode(wrapWithScale(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.layout.StackPane wrapWithScale(Parent root) {
        javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane();
        wrapper.setStyle("-fx-background-color: white;");
        wrapper.getChildren().add(root);

        javafx.beans.value.ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> {
            double w = wrapper.getWidth();
            double h = wrapper.getHeight();
            if (w <= 0 || h <= 0) return;
            
            double scaleX = w / GameConfig.SCREEN_WIDTH;
            double scaleY = h / GameConfig.SCREEN_HEIGHT;
            double minScale = Math.min(scaleX, scaleY);
            root.setScaleX(minScale);
            root.setScaleY(minScale);
        };
        wrapper.widthProperty().addListener(sizeListener);
        wrapper.heightProperty().addListener(sizeListener);
        
        return wrapper;
    }

    public void startSinglePlayerGame() {
        showCharacterSelect(CharacterSelectScene.Mode.SINGLE);
    }

    public void startSinglePlayerGame(String character) {
        GameScene gameScene = new GameScene(this, character);
        setScenePreservingWindowMode(gameScene.getView());

        gameScene.setKeyControl(stage.getScene());

        Platform.runLater(() -> gameScene.getView().requestFocus());
    }

    private void setScenePreservingWindowMode(Parent newRoot) {
        if (stage.getScene() == null) {
            Scene scene = new Scene(newRoot, GameConfig.getScreenWidth(), GameConfig.getScreenHeight());
            scene.getStylesheets().add(getClass().getResource("/com/dino/theme.css").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(newRoot);
        }
    }

    public void startCoopGame() {
        showCharacterSelect(CharacterSelectScene.Mode.COOP);
    }

    public void startCoopGame(String playerOneCharacter, String playerTwoCharacter) {
        CoopGameScene coopScene = new CoopGameScene(this, playerOneCharacter, playerTwoCharacter);
        setScenePreservingWindowMode(coopScene.getView());

        coopScene.setKeyControl(stage.getScene());

        Platform.runLater(() -> coopScene.getView().requestFocus());
    }

    public void startVersusGame() {
        showCharacterSelect(CharacterSelectScene.Mode.VERSUS);
    }

    public void startVersusGame(String playerOneCharacter, String playerTwoCharacter) {
        VersusGameScene versusGameScene = new VersusGameScene(this, playerOneCharacter, playerTwoCharacter);
        setScenePreservingWindowMode(versusGameScene.getView());

        versusGameScene.setKeyControl(stage.getScene());

        Platform.runLater(() -> versusGameScene.getView().requestFocus());
    }

    private void showCharacterSelect(CharacterSelectScene.Mode mode) {
        CharacterSelectScene characterSelectScene = new CharacterSelectScene(this, mode);
        setScenePreservingWindowMode(wrapWithScale(characterSelectScene.getView()));

        characterSelectScene.setKeyControl(stage.getScene());

        Platform.runLater(() -> characterSelectScene.getView().requestFocus());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
