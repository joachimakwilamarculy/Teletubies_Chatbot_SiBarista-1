module com.felix_71241153.app.chatbot_sibarista {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.base;
    requires java.desktop;
    requires jbcrypt;

    // Tambahkan baris ini untuk mengizinkan TableView membaca data dari package model
    opens model to javafx.base, javafx.fxml;

    // Baris yang sudah ada
    opens com.felix_71241153.app.chatbot_sibarista to javafx.base, javafx.fxml;
    opens controller to javafx.fxml;

    exports com.felix_71241153.app.chatbot_sibarista;
    exports controller;
    exports model;
}