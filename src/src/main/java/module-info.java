module com.dino {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.dino to javafx.fxml;
    exports com.dino;
}
