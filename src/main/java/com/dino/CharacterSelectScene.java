package com.dino;

import javafx.animation.PauseTransition;
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
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class CharacterSelectScene {

    public enum Mode {
        SINGLE,
        VERSUS,
        COOP
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
    private String playerTwoCharacter = CHARACTER_IDS[0];
    private boolean isEnterPressed = false;

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

        titleLabel = new Label(mode == Mode.SINGLE ? "選擇角色" : (mode == Mode.VERSUS ? "雙人對戰模式" : "雙人合作模式"));
        titleLabel.setFont(Font.font("Courier New", 32));
        titleLabel.setTextFill(Color.BLACK);

        turnLabel = new Label();
        turnLabel.setFont(Font.font("Courier New", 24));
        turnLabel.setTextFill(Color.RED);
        turnLabel.setText(mode == Mode.SINGLE ? "PLAYER 1" : "PLAYER " + selectingPlayer);

        cards = new HBox(28);
        cards.setAlignment(Pos.CENTER);

        Label helpLabel = new Label("方向鍵/WASD 選擇    Enter 確認    Esc 返回    [ ? = 尚未解鎖，請至商店購買 ]");
        helpLabel.setFont(Font.font(13));
        helpLabel.setTextFill(Color.rgb(120, 80, 50));

        layout.getChildren().addAll(titleLabel, turnLabel, cards, helpLabel);
        root.getChildren().add(layout);

        updateView();
    }

    private void updateView() {
        cards.getChildren().clear();
        
        for (int i = 0; i < CHARACTER_IDS.length; i++) {
            cards.getChildren().add(createCard(i));
        }
    }

    private VBox createCard(int index) {
        boolean isSelected = (index == selectedIndex);
        boolean unlocked = SaveManager.isCharacterUnlocked(CHARACTER_IDS[index]);

        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(150, 150);

        Rectangle frame = new Rectangle(126, 94);
        if (!unlocked) {
            frame.setFill(isSelected ? Color.rgb(60, 40, 30) : Color.rgb(40, 40, 40));
            frame.setStroke(isSelected ? Color.web("#FFD54F") : Color.rgb(100, 100, 100));
            frame.setStrokeWidth(isSelected ? 4 : 2);
        } else {
            frame.setFill(isSelected ? Color.rgb(232, 234, 237) : Color.WHITE);
            frame.setStroke(isSelected ? Color.rgb(32, 33, 36) : Color.rgb(95, 99, 104));
            frame.setStrokeWidth(isSelected ? 4 : 2);
        }

        Pane previewPane = new Pane(frame);
        previewPane.setPrefSize(126, 94);

        if (unlocked) {
            ImageView preview = new ImageView(ResourceManager.getImage(PREVIEW_IMAGES[index]));
            preview.setSmooth(false);
            preview.setFitWidth("sonic".equals(CHARACTER_IDS[index]) ? 58 : 52);
            preview.setPreserveRatio(true);
            preview.setLayoutX(48);
            preview.setLayoutY(28);
            previewPane.getChildren().add(preview);
        } else {
            Label qMark = new Label("?");
            qMark.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            qMark.setTextFill(isSelected ? Color.web("#FFD54F") : Color.rgb(150, 150, 150));
            qMark.setLayoutX(38);
            qMark.setLayoutY(18);
            previewPane.getChildren().add(qMark);
        }

        Label name = new Label(unlocked ? CHARACTER_NAMES[index] : "???");
        name.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, 17));
        if (!unlocked) {
            name.setTextFill(isSelected ? Color.web("#FFD54F") : Color.rgb(120, 120, 120));
        } else {
            name.setTextFill(isSelected ? Color.BLACK : Color.rgb(95, 99, 104));
        }

        card.getChildren().addAll(previewPane, name);
        return card;
    }

    public void setKeyControl(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dinoMain.showMainMenu();
                return;
            }

            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.UP || e.getCode() == KeyCode.A || e.getCode() == KeyCode.W) {
                moveSelection(-1);
            } else if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.D || e.getCode() == KeyCode.S) {
                moveSelection(1);
            } else if (e.getCode() == KeyCode.ENTER) {
                if (!isEnterPressed) {
                    isEnterPressed = true;
                    confirmSelection();
                }
            }
        });
        
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                isEnterPressed = false;
            }
        });
    }

    private void moveSelection(int delta) {
        selectedIndex = (selectedIndex + delta + CHARACTER_IDS.length) % CHARACTER_IDS.length;
        updateView();
    }

    private void confirmSelection() {
        String character = CHARACTER_IDS[selectedIndex];

        if (!SaveManager.isCharacterUnlocked(character)) {
            SoundManager.playHit();
            String originalTitle = titleLabel.getText();
            titleLabel.setText("⚠ 此角色尚未解鎖！");
            titleLabel.setTextFill(Color.RED);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> {
                titleLabel.setText(originalTitle);
                titleLabel.setTextFill(Color.BLACK);
            });
            pause.play();
            return;
        }

        if (mode == Mode.SINGLE) {
            GameConfig.selectedCharacter = character;
            dinoMain.startSinglePlayerGame(character);
        } else {
            if (selectingPlayer == 1) {
                playerOneCharacter = character;
                selectingPlayer = 2;
                turnLabel.setText("PLAYER 2");
                turnLabel.setTextFill(Color.BLUE);
            } else {
                playerTwoCharacter = character;
                if (mode == Mode.VERSUS) {
                    dinoMain.startVersusGame(playerOneCharacter, playerTwoCharacter);
                } else {
                    dinoMain.startCoopGame(playerOneCharacter, playerTwoCharacter);
                }
            }
        }
    }

    public StackPane getView() {
        return root;
    }
}
