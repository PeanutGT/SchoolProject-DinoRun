module com.dino {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires transitive javafx.graphics;
    requires java.prefs;

    opens com.dino to javafx.fxml;
    exports com.dino;
}
