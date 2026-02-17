module org.example.courtslot {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.courtslot to javafx.fxml;
    exports org.example.courtslot;
    exports org.example.courtslot.controller;
    opens org.example.courtslot.controller to javafx.fxml;
}