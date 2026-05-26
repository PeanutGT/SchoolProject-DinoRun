package com.dino;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class CharacterSelectScene {

    public enum Mode {
        SINGLE,
        VERSUS
    }

    private static final double BASE_WIDTH = GameConfig.SCREEN_WIDTH;
    private static final double BASE_HEIGHT = GameConfig.SCREEN_HEIGHT;
    private static final String[] CHARACTER_IDS = {"dino", "mario", "luigi", "kirby", "lucario", "sonic", "steve"};
    private static final String[] CHARACTER_NAMES = {"DINO", "MARIO", "LUIGI", "KIRBY", "LUCARIO", "SONIC", "STEVE"};
    private static final String[] PREVIEW_IMAGES = {"dino_run1.png", "mario_walk1.png", "luigi_run1.png", "kirby_run1.png", "lucario_run1.png", "sonic_run1.png", "steve_run1.png"};

    private final DinoMain dinoMain;
    private final Mode mode;
    private final StackPane root;
    private final Label titleLabel;
    private final Label turnLabel;
    private final HBox cards;

    private int selectedIndex = 0;
    private int selectingPlayer = 1;
    private String playerOneCharacter = CHARACTER_IDS[0];

    public CharacterSelectScene(DinoMain dinoMain, Mode mode) {
        this.dinoMain = dinoMain;
        this.mode = mode;

        root = new StackPane();
        root.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        root.setStyle("-fx-background-color: white;");

        VBox layout = new VBox(28);
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        layout.setMaxSize(BASE_WIDTH, BASE_HEIGHT);

        titleLabel = new Label(mode == Mode.SINGLE ? "選擇角色" : "雙人對戰選角");
        titleLabel.setFont(Font.font(34));

        turnLabel = new Label();
        turnLabel.setFont(Font.font(18));

        cards = new HBox(28);
        cards.setAlignment(Pos.CENTER);

        Label helpLabel = new Label("方向鍵選擇    Enter 確認    Esc 返回");
        helpLabel.setFont(Font.font(15));

        layout.getChildren().addAll(titleLabel, turnLabel, cards, helpLabel);
        root.getChildren().add(layout);

        updateView();
    }

    private void updateView() {
        cards.getChildren().clear();
        turnLabel.setText(mode == Mode.SINGLE ? "PLAYER 1" : "PLAYER " + selectingPlayer);

        for (int i = 0; i < CHARACTER_IDS.length; i++) {
            cards.getChildren().add(createCard(i));
        }
    }

    private VBox createCard(int index) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(150, 150);

        Rectangle frame = new Rectangle(126, 94);
        frame.setFill(index == selectedIndex ? Color.rgb(232, 234, 237) : Color.WHITE);
        frame.setStroke(index == selectedIndex ? Color.rgb(32, 33, 36) : Color.rgb(95, 99, 104));
        frame.setStrokeWidth(index == selectedIndex ? 4 : 2);

        ImageView preview = new ImageView(ResourceManager.getImage(PREVIEW_IMAGES[index]));
        preview.setSmooth(false);
        preview.setFitWidth("sonic".equals(CHARACTER_IDS[index]) ? 58 : 52);
        preview.setPreserveRatio(true);

        Pane previewPane = new Pane(frame, preview);
        preview.setLayoutX(48);
        preview.setLayoutY(28);

        Label name = new Label(CHARACTER_NAMES[index]);
        name.setFont(Font.font(17));
        name.setTextFill(index == selectedIndex ? Color.BLACK : Color.rgb(95, 99, 104));

        card.getChildren().addAll(previewPane, name);
        return card;
    }

    public void setKeyControl(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dinoMain.showMainMenu();
                return;
            }

            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.UP) {
                moveSelection(-1);
            } else if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.DOWN) {
                moveSelection(1);
            } else if (e.getCode() == KeyCode.ENTER) {
                confirmSelection();
            }
        });
    }

    private void moveSelection(int delta) {
        selectedIndex = (selectedIndex + delta + CHARACTER_IDS.length) % CHARACTER_IDS.length;
        updateView();
    }

    private void confirmSelection() {
        String selectedCharacter = CHARACTER_IDS[selectedIndex];

        if (mode == Mode.SINGLE) {
            dinoMain.startSinglePlayerGame(selectedCharacter);
            return;
        }

        if (selectingPlayer == 1) {
            playerOneCharacter = selectedCharacter;
            selectingPlayer = 2;
            selectedIndex = 0;
            updateView();
        } else {
            dinoMain.startVersusGame(playerOneCharacter, selectedCharacter);
        }
    }

    public StackPane getView() {
        return root;
    }
}
