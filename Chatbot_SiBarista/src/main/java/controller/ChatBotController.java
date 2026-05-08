package controller;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import java.io.InputStream;

import java.sql.SQLException;

public class ChatBotController {

    // --- Deklarasi elemen FXML ---
    @FXML private Button adminModeButton;
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

    private ChatbotService chatbotService = new ChatbotService();

    @FXML
    public void initialize() {
        chatAreaWrapper.setVisible(false);
        chatAreaWrapper.setManaged(false);
    }

    // --- Handling Tombol & Input ---

    @FXML
    private void handleAdminMode(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/felix_71241153/app/chatbot_sibarista/login-view.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) adminModeButton.getScene().getWindow();

            // 1. Simpan ukuran jendela saat ini
            double width = stage.getWidth();
            double height = stage.getHeight();

            // 2. Set scene baru dengan ukuran yang sama dengan jendela sebelumnya
            javafx.scene.Scene scene = new javafx.scene.Scene(root, width, height);

            stage.setScene(scene);
            stage.setTitle("Admin Login - SiBarista");

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void kirimPesanDari(TextField field, boolean perluTransisi) throws SQLException {
        String input = field.getText().trim();
        if (!input.isEmpty()) {
            if (perluTransisi) {
                transisiKeModeChat();
            }
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
        String input = "Menu";
        if (welcomeBox.isVisible()) {
            transisiKeModeChat();
        }
        prosesInput(input);
        scrollKeBelow();
    }

    @FXML
    private void handleRekomendasi(ActionEvent event) throws SQLException {
        String input = "rekomendasi";
        if (welcomeBox.isVisible()) {
            transisiKeModeChat();
        }
        prosesInput(input);
        scrollKeBelow();
    }

    // --- Logika Internal Controller ---

    private void transisiKeModeChat() {
        welcomeBox.setVisible(false);
        welcomeBox.setManaged(false);
        chatAreaWrapper.setVisible(true);
        chatAreaWrapper.setManaged(true);
    }

//    private void prosesInput(String pesanUser) throws SQLException {
//        tambahGelembungChat(pesanUser, true);
//        String balasanBot = chatbotService.prosesInput(pesanUser);
//        tambahGelembungChat(balasanBot, false);
//    }

    private void prosesInput(String pesanUser) throws SQLException {
        // 1. Tampilkan pesan user di chat
        tambahGelembungChat(pesanUser, true, null);

        // 2. Cek apakah input user adalah nama produk (untuk ambil gambar)
        Produk p = chatbotService.balasanDetail(pesanUser);

        if (p != null) {
            // Jika ketemu produk, ambil teks detail dan objek produknya (untuk gambar)
            String balasanBot = chatbotService.formatDetailProduk(p);
            tambahGelembungChat(balasanBot, false, p);
        } else {
            // Jika bukan produk (misal: "Halo" atau "Menu"), ambil balasan String biasa
            String balasanBot = chatbotService.prosesInput(pesanUser);
            tambahGelembungChat(balasanBot, false, null);
        }
    }

    /**
     * Auto-scroll ke pesan terbawah setelah pesan baru ditambahkan.
     */
    private void scrollKeBelow() {
        // Cari ScrollPane yang menjadi parent dari chatContainer
        javafx.application.Platform.runLater(() -> {
            for (Node node : chatAreaWrapper.getChildren()) {
                if (node instanceof ScrollPane scrollPane) {
                    scrollPane.setVvalue(1.0);
                    break;
                }
            }
        });
    }

    /**
     * Menambahkan gelembung chat ke dalam chatContainer.
     *
     * Tampilan:
     *  - USER  : kanan, background Espresso (#1C0A00), teks Steamed Milk (#FBF7F0)
     *            dengan sudut: 15 15 0 15
     *  - BOT   : kiri, background Latte Tan (#C8A882) tipis / Parchment (#F0E6D3),
     *            teks Espresso (#1C0A00), dengan sudut: 15 15 15 0
     *            ditambah label "☕ SiBarista" di atas gelembung
     */
//    private void tambahGelembungChat(String pesan, boolean isUser) {
//
//        Label labelPesan = new Label(pesan);
//        labelPesan.setWrapText(true);
//        labelPesan.setMaxWidth(420);
//        labelPesan.setPadding(new Insets(12, 18, 12, 18));
//
//        HBox barisChat = new HBox();
//        barisChat.setMaxWidth(Double.MAX_VALUE);
//
//        if (isUser) {
//            // --- Bubble User ---
//            labelPesan.setStyle(
//                    "-fx-background-color: #1C0A00;" +
//                            "-fx-text-fill: #FBF7F0;" +
//                            "-fx-background-radius: 15 15 0 15;" +
//                            "-fx-font-size: 13px;"
//            );
//            barisChat.setAlignment(Pos.CENTER_RIGHT);
//            barisChat.getChildren().add(labelPesan);
//
//        } else {
//            // --- Label nama bot ---
//            Label labelNama = new Label("☕  SiBarista");
//            labelNama.setStyle(
//                    "-fx-text-fill: #A0522D;" +
//                            "-fx-font-size: 11px;" +
//                            "-fx-font-style: italic;"
//            );
//
//            // --- Bubble Bot ---
//            labelPesan.setStyle(
//                    "-fx-background-color: #FBF7F0;" +
//                            "-fx-text-fill: #1C0A00;" +
//                            "-fx-background-radius: 15 15 15 0;" +
//                            "-fx-font-size: 13px;" +
//                            "-fx-border-color: #C8A882;" +
//                            "-fx-border-width: 1;" +
//                            "-fx-border-radius: 15 15 15 0;"
//            );
//
//            VBox botWrapper = new VBox(4, labelNama, labelPesan);
//            botWrapper.setAlignment(Pos.TOP_LEFT);
//
//            barisChat.setAlignment(Pos.CENTER_LEFT);
//            barisChat.getChildren().add(botWrapper);
//        }
//
//        chatContainer.getChildren().add(barisChat);
//    }

    private Image loadGambarProduk(String namaFileGambar) {
        if (namaFileGambar == null || namaFileGambar.isEmpty()) {
            return null;
        }
        try {
            Path resourcesPath = Paths.get(
                    "Chatbot_SiBarista", "src", "main", "resources", "images", namaFileGambar
            );
            if (Files.exists(resourcesPath)) {
                return new Image(resourcesPath.toUri().toString());
            }
            System.out.println("Gambar tidak ditemukan: " + namaFileGambar);
            return null;
        } catch (Exception e) {
            System.out.println("Gagal load gambar: " + e.getMessage());
            return null;
        }
    }

    // Tambahkan parameter Produk p
    private void tambahGelembungChat(String pesan, boolean isUser, Produk p) {
        HBox barisChat = new HBox();
        barisChat.setMaxWidth(Double.MAX_VALUE);

        if (isUser) {
            // --- Bagian User (Tetap Sama) ---
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
            Label labelNama = new Label("☕  SiBarista");
            labelNama.setStyle("-fx-text-fill: #A0522D; -fx-font-size: 11px; -fx-font-style: italic;");

            VBox bubbleBox = new VBox(10);
            bubbleBox.setPadding(new Insets(12, 18, 12, 18));
            bubbleBox.setStyle("-fx-background-color: #FBF7F0; -fx-background-radius: 15 15 15 0; -fx-border-color: #C8A882; -fx-border-width: 1; -fx-border-radius: 15 15 15 0;");

            // 1. TAMBAHKAN GAMBAR DULU (Agar muncul paling atas di gelembung)
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

            // 2. BARU TAMBAHKAN LABEL PESAN (Detail Menu)
            Label labelPesan = new Label(pesan);
            labelPesan.setWrapText(true);
            labelPesan.setStyle("-fx-text-fill: #1C0A00; -fx-font-size: 13px;");
            bubbleBox.getChildren().add(labelPesan);

            // 3. Tambahkan Label Penutup (Optional)
//            if (p != null) {
//                Label labelFooter = new Label("Jika ingin melihat kategori lain, ketik \"Menu\".");
//                labelFooter.setWrapText(true);
//                labelFooter.setStyle("-fx-text-fill: #1C0A00; -fx-font-size: 11px; -fx-font-style: italic;");
//                bubbleBox.getChildren().add(labelFooter);
//            }

            VBox botWrapper = new VBox(4, labelNama, bubbleBox);
            botWrapper.setAlignment(Pos.TOP_LEFT);
            barisChat.setAlignment(Pos.CENTER_LEFT);
            barisChat.getChildren().add(botWrapper);
        }

        chatContainer.getChildren().add(barisChat);
    }


}