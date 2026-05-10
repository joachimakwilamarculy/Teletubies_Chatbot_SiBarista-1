package controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import service.KeranjangService;
import service.ChatbotService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import model.Produk;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;

public class ChatBotController {

    // --- Deklarasi elemen FXML ---
    @FXML private Button adminModeButton;

    /** Tombol Keranjang di sidebar (menggantikan About). */
    @FXML private Button cartButton;

    @FXML private VBox welcomeBox;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    @FXML private VBox chatAreaWrapper;
    @FXML private VBox chatContainer;
    @FXML private TextField chatInputField;

    // ===== PALETTE COFFEE =====
    // Espresso Black  : #1C0A00
    // Dark Roast      : #3D1A00
    // Mocha           : #6B3A2A
    // Sienna/Cinnamon : #A0522D
    // Latte Tan       : #C8A882
    // Parchment Cream : #F0E6D3
    // Steamed Milk    : #FBF7F0

    private final ChatbotService    chatbotService    = new ChatbotService();
    private final KeranjangService  keranjangService  = KeranjangService.getInstance();

    // =========================================================================
    //  INIT
    // =========================================================================

    @FXML
    public void initialize() {
        chatAreaWrapper.setVisible(false);
        chatAreaWrapper.setManaged(false);
        refreshKeranjangBadge();
    }

    // =========================================================================
    //  TOMBOL KERANJANG
    // =========================================================================

    /**
     * Buka halaman keranjang (keranjang-view.fxml).
     * Dipanggil oleh onAction="#handleKeranjang" pada cartButton di FXML.
     */
    @FXML
    private void handleCart(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource(
                            "/com/felix_71241153/app/chatbot_sibarista/keranjang-view.fxml")
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) cartButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("SiBarista – Keranjang");

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perbarui teks tombol keranjang di sidebar agar menampilkan
     * jumlah item, contoh: "🛒  Keranjang (3)".
     */
    private void refreshKeranjangBadge() {
        if (cartButton == null) return;
        int total = keranjangService.getTotalJumlah();
        if (total > 0) {
            cartButton.setText("🛒  Keranjang (" + total + ")");
        } else {
            cartButton.setText("🛒  Keranjang");
        }
    }

    // =========================================================================
    //  HANDLING TOMBOL & INPUT
    // =========================================================================

    @FXML
    private void handleAdminMode(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource(
                            "/com/felix_71241153/app/chatbot_sibarista/login-view.fxml")
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) adminModeButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("Admin Login - SiBarista");

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void kirimPesanDari(TextField field, boolean perluTransisi) throws SQLException {
        String input = field.getText().trim();
        if (!input.isEmpty()) {
            if (perluTransisi) transisiKeModeChat();
            prosesInput(input);
            field.clear();
            scrollKeBelow();
        }
    }

    @FXML
    private void handleSend(ActionEvent event) throws SQLException {
        kirimPesanDari(messageField, true);
    }

    @FXML
    private void handleSendFromChat(ActionEvent event) throws SQLException {
        kirimPesanDari(chatInputField, false);
    }

    @FXML
    private void handleMenu(ActionEvent event) throws SQLException {
        if (welcomeBox.isVisible()) transisiKeModeChat();
        prosesInput("Menu");
        scrollKeBelow();
    }

    @FXML
    private void handleRekomendasi(ActionEvent event) throws SQLException {
        if (welcomeBox.isVisible()) transisiKeModeChat();
        prosesInput("rekomendasi");
        scrollKeBelow();
    }

    // =========================================================================
    //  LOGIKA INTERNAL
    // =========================================================================

    private void transisiKeModeChat() {
        welcomeBox.setVisible(false);
        welcomeBox.setManaged(false);
        chatAreaWrapper.setVisible(true);
        chatAreaWrapper.setManaged(true);
    }

    private void prosesInput(String pesanUser) throws SQLException {
        tambahGelembungChat(pesanUser, true, null);

        Produk p = chatbotService.balasanDetail(pesanUser);
        if (p != null) {
            tambahGelembungChat(chatbotService.formatDetailProduk(p), false, p);
        } else {
            tambahGelembungChat(chatbotService.prosesInput(pesanUser), false, null);
        }
    }

    private void scrollKeBelow() {
        javafx.application.Platform.runLater(() -> {
            for (Node node : chatAreaWrapper.getChildren()) {
                if (node instanceof ScrollPane scrollPane) {
                    scrollPane.setVvalue(1.0);
                    break;
                }
            }
        });
    }

    private Image loadGambarProduk(String namaFileGambar) {
        if (namaFileGambar == null || namaFileGambar.isEmpty()) return null;
        try {
            Path resourcesPath = Paths.get(
                    "Chatbot_SiBarista", "src", "main", "resources", "images", namaFileGambar
            );
            if (Files.exists(resourcesPath)) return new Image(resourcesPath.toUri().toString());
            System.out.println("Gambar tidak ditemukan: " + namaFileGambar);
        } catch (Exception e) {
            System.out.println("Gagal load gambar: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    //  GELEMBUNG CHAT
    // =========================================================================

    private void tambahGelembungChat(String pesan, boolean isUser, Produk p) {
        HBox barisChat = new HBox();
        barisChat.setMaxWidth(Double.MAX_VALUE);

        if (isUser) {
            // --- Bubble User ---
            Label labelPesan = new Label(pesan);
            labelPesan.setWrapText(true);
            labelPesan.setMaxWidth(420);
            labelPesan.setPadding(new Insets(12, 18, 12, 18));
            labelPesan.setStyle(
                    "-fx-background-color: #1C0A00;" +
                            "-fx-text-fill: #FBF7F0;" +
                            "-fx-background-radius: 15 15 0 15;" +
                            "-fx-font-size: 13px;"
            );
            barisChat.setAlignment(Pos.CENTER_RIGHT);
            barisChat.getChildren().add(labelPesan);

        } else {
            // --- Bubble Bot ---
            Label labelNama = new Label("☕  SiBarista");
            labelNama.setStyle(
                    "-fx-text-fill: #A0522D; -fx-font-size: 11px; -fx-font-style: italic;"
            );

            VBox bubbleBox = new VBox(10);
            bubbleBox.setPadding(new Insets(12, 18, 12, 18));
            bubbleBox.setStyle(
                    "-fx-background-color: #FBF7F0;" +
                            "-fx-background-radius: 15 15 15 0;" +
                            "-fx-border-color: #C8A882;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 15 15 15 0;"
            );

            // 1. Gambar produk (jika ada)
            if (p != null && p.getGambar() != null && !p.getGambar().isEmpty()) {
                try {
                    Image image = loadGambarProduk(p.getGambar());
                    if (image != null) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(220);
                        imageView.setPreserveRatio(true);
                        bubbleBox.getChildren().add(imageView);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 2. Teks detail produk
            Label labelPesan = new Label(pesan);
            labelPesan.setWrapText(true);
            labelPesan.setStyle("-fx-text-fill: #1C0A00; -fx-font-size: 13px;");
            bubbleBox.getChildren().add(labelPesan);

            // 3. Tombol "Tambah ke Keranjang" — hanya muncul jika ada produk
            if (p != null) {
                Button btnKeranjang = new Button("🛒  Tambah ke Keranjang");
                final String styleDefault =
                        "-fx-background-color: #1C0A00; -fx-text-fill: #FBF7F0;" +
                                "-fx-font-size: 12px; -fx-cursor: hand;" +
                                "-fx-background-radius: 8; -fx-padding: 7 18 7 18;";
                final String styleSuccess =
                        "-fx-background-color: #6B3A2A; -fx-text-fill: #FBF7F0;" +
                                "-fx-font-size: 12px; -fx-cursor: hand;" +
                                "-fx-background-radius: 8; -fx-padding: 7 18 7 18;";

                btnKeranjang.setStyle(styleDefault);

                final Produk produkRef = p;
                btnKeranjang.setOnAction(e -> {
                    keranjangService.tambahProduk(produkRef);
                    refreshKeranjangBadge();

                    // Feedback visual singkat
                    btnKeranjang.setText("✓  Ditambahkan!");
                    btnKeranjang.setStyle(styleSuccess);
                    btnKeranjang.setDisable(true);

                    new Thread(() -> {
                        try { Thread.sleep(1500); } catch (InterruptedException ex) { /* abaikan */ }
                        javafx.application.Platform.runLater(() -> {
                            btnKeranjang.setText("🛒  Tambah ke Keranjang");
                            btnKeranjang.setStyle(styleDefault);
                            btnKeranjang.setDisable(false);
                        });
                    }).start();
                });

                bubbleBox.getChildren().add(btnKeranjang);
            }

            VBox botWrapper = new VBox(4, labelNama, bubbleBox);
            botWrapper.setAlignment(Pos.TOP_LEFT);
            barisChat.setAlignment(Pos.CENTER_LEFT);
            barisChat.getChildren().add(botWrapper);
        }

        chatContainer.getChildren().add(barisChat);
    }
}