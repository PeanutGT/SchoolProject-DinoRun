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

/**
 * 角色選擇場景類別。
 * 負責單人、雙人對戰、雙人合作模式下的選角 UI，
 * 判斷存檔中角色是否解鎖，支援鍵盤 (Left/Right/Enter/Esc) 互動選擇。
 */
public class CharacterSelectScene {

    // 遊戲模式列舉：單人、雙人對戰、雙人合作
    public enum Mode {
        SINGLE,
        VERSUS,
        COOP
    }

    private static final double BASE_WIDTH = GameConfig.SCREEN_WIDTH;
    private static final double BASE_HEIGHT = GameConfig.SCREEN_HEIGHT;
    // 角色識別 ID 陣列
    private static final String[] CHARACTER_IDS = {"dino", "mario", "luigi", "kirby", "lucario", "sonic", "steve"};
    // 角色顯示名稱陣列
    private static final String[] CHARACTER_NAMES = {"DINO", "MARIO", "LUIGI", "KIRBY", "LUCARIO", "SONIC", "STEVE"};
    // 角色對應的預覽圖片檔名
    private static final String[] PREVIEW_IMAGES = {"dino_run1.png", "mario_walk1.png", "luigi_run1.png", "kirby_run1.png", "lucario_run1.png", "sonic_run1.png", "steve_run1.png"};

    private final DinoMain dinoMain; // 遊戲主控制實例
    private final Mode mode;         // 當前選取模式
    private final StackPane root;    // 場景根節點
    private final Label titleLabel;  // 頂部標題（例如模式名稱）
    private final Label turnLabel;   // 玩家選取輪替提示（例如 PLAYER 1 / PLAYER 2）
    private final HBox cards;        // 水平排列的角色卡片容器

    private int selectedIndex = 0;   // 目前預覽選中的卡片索引 (0 ~ 6)
    private int selectingPlayer = 1; // 當前正在選角的玩家代號 (1 或 2)
    private String playerOneCharacter = CHARACTER_IDS[0]; // 玩家一選定角色
    private String playerTwoCharacter = CHARACTER_IDS[0]; // 玩家二選定角色
    private boolean isEnterPressed = false;               // 防止 Enter 鍵按住造成連續判定

    /**
     * 建構子：初始化場景排版與元件設定。
     * @param dinoMain 遊戲主程式物件
     * @param mode 遊戲模式
     */
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

        // 初始化標題文字
        titleLabel = new Label(mode == Mode.SINGLE ? "選擇角色" : (mode == Mode.VERSUS ? "雙人對戰模式" : "雙人合作模式"));
        titleLabel.setFont(Font.font("Courier New", 32));
        titleLabel.setTextFill(Color.BLACK);

        // 初始化選取回合文字，雙人模式時有著色區別 (P1紅, P2藍)
        turnLabel = new Label();
        turnLabel.setFont(Font.font("Courier New", 24));
        turnLabel.setTextFill(Color.RED);
        turnLabel.setText(mode == Mode.SINGLE ? "PLAYER 1" : "PLAYER " + selectingPlayer);

        cards = new HBox(28);
        cards.setAlignment(Pos.CENTER);

        // 下方操作說明的 Label
        Label helpLabel = new Label("方向鍵/WASD 選擇    Enter 確認    Esc 返回    [ ? = 尚未解鎖，請至商店購買 ]");
        helpLabel.setFont(Font.font(13));
        helpLabel.setTextFill(Color.rgb(120, 80, 50));

        layout.getChildren().addAll(titleLabel, turnLabel, cards, helpLabel);
        root.getChildren().add(layout);

        // 首次更新角色列表顯示
        updateView();
    }

    /**
     * 重新繪製所有角色卡片（在移動選擇時呼叫以更新外框顏色等）。
     */
    private void updateView() {
        cards.getChildren().clear();
        for (int i = 0; i < CHARACTER_IDS.length; i++) {
            cards.getChildren().add(createCard(i));
        }
    }

    /**
     * 動態建立個別角色的卡片 Node。
     * @param index 角色索引
     * @return 裝載圖片與名稱的 VBox 容器
     */
    private VBox createCard(int index) {
        boolean isSelected = (index == selectedIndex);
        // 自 SaveManager 驗證此角色是否已被購買/解鎖
        boolean unlocked = SaveManager.isCharacterUnlocked(CHARACTER_IDS[index]);

        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(150, 150);

        // 卡片外框與背景矩形
        Rectangle frame = new Rectangle(126, 94);
        if (!unlocked) {
            // 未解鎖卡片外觀：灰暗背景
            frame.setFill(isSelected ? Color.rgb(60, 40, 30) : Color.rgb(40, 40, 40));
            frame.setStroke(isSelected ? Color.web("#FFD54F") : Color.rgb(100, 100, 100));
            frame.setStrokeWidth(isSelected ? 4 : 2);
        } else {
            // 已解鎖卡片外觀：亮白背景
            frame.setFill(isSelected ? Color.rgb(232, 234, 237) : Color.WHITE);
            frame.setStroke(isSelected ? Color.rgb(32, 33, 36) : Color.rgb(95, 99, 104));
            frame.setStrokeWidth(isSelected ? 4 : 2);
        }

        Pane previewPane = new Pane(frame);
        previewPane.setPrefSize(126, 94);

        if (unlocked) {
            // 若解鎖則繪製角色外觀預覽圖，微調部分角色（如超音鼠）的縮放寬度
            ImageView preview = new ImageView(ResourceManager.getImage(PREVIEW_IMAGES[index]));
            preview.setSmooth(false);
            preview.setFitWidth("sonic".equals(CHARACTER_IDS[index]) ? 58 : 52);
            preview.setPreserveRatio(true);
            preview.setLayoutX(48);
            preview.setLayoutY(28);
            previewPane.getChildren().add(preview);
        } else {
            // 未解鎖時顯示問號圖示
            Label qMark = new Label("?");
            qMark.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            qMark.setTextFill(isSelected ? Color.web("#FFD54F") : Color.rgb(150, 150, 150));
            qMark.setLayoutX(38);
            qMark.setLayoutY(18);
            previewPane.getChildren().add(qMark);
        }

        // 角色名字標籤，未解鎖顯示為 ???
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

    /**
     * 設定鍵盤按鍵監聽器（綁定於當前 Scene）。
     * @param scene 當前的 JavaFX Scene
     */
    public void setKeyControl(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                // Esc 返回主選單
                dinoMain.showMainMenu();
                return;
            }

            // 左/上/A/W：向左選取
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.UP || e.getCode() == KeyCode.A || e.getCode() == KeyCode.W) {
                moveSelection(-1);
            } 
            // 右/下/D/S：向右選取
            else if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.D || e.getCode() == KeyCode.S) {
                moveSelection(1);
            } 
            // Enter：確認選擇
            else if (e.getCode() == KeyCode.ENTER) {
                if (!isEnterPressed) {
                    isEnterPressed = true;
                    confirmSelection();
                }
            }
        });
        
        // 釋放按鍵時重設 Enter 鎖定
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                isEnterPressed = false;
            }
        });
    }

    /**
     * 移動卡片選取高亮。
     * @param delta 方向位移量 (-1 或 1)
     */
    private void moveSelection(int delta) {
        selectedIndex = (selectedIndex + delta + CHARACTER_IDS.length) % CHARACTER_IDS.length;
        updateView();
    }

    /**
     * 確認當前高亮的角色選取。
     * 若角色尚未解鎖，會播放受傷音效並彈出 2 秒警告文字。
     * 若為雙人模式，則先記錄 P1，再轉由 P2 選角後才正式載入場景。
     */
    private void confirmSelection() {
        String character = CHARACTER_IDS[selectedIndex];

        // 檢查是否未解鎖
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
            // 單人模式：直接寫入 GameConfig 並啟動單人遊戲
            GameConfig.selectedCharacter = character;
            dinoMain.startSinglePlayerGame(character);
        } else {
            // 雙人模式：需要確認兩位玩家的選擇
            if (selectingPlayer == 1) {
                playerOneCharacter = character;
                selectingPlayer = 2;
                turnLabel.setText("PLAYER 2");
                turnLabel.setTextFill(Color.BLUE);
            } else {
                playerTwoCharacter = character;
                // 啟動對應的雙人合作或對戰場景
                if (mode == Mode.VERSUS) {
                    dinoMain.startVersusGame(playerOneCharacter, playerTwoCharacter);
                } else {
                    dinoMain.startCoopGame(playerOneCharacter, playerTwoCharacter);
                }
            }
        }
    }

    /**
     * 取得角色選擇場景的視圖根容器。
     */
    public StackPane getView() {
        return root;
    }
}
