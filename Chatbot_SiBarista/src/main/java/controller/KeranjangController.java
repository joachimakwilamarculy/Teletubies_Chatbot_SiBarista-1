package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Keranjang;
import model.Produk;
import service.KeranjangService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Controller untuk halaman Keranjang.
 *
 * Layout FXML yang diharapkan (cart-view.fxml):
 *
 *  BorderPane
 *  ├── top    : HBox (header — tombol back + judul "Keranjang")
 *  ├── center : VBox
 *  │            ├── TabPane / HBox kategori (Semua, Kopi, Non-Kopi, dll.)
 *  │            └── ScrollPane → VBox#menuListContainer   ← daftar produk
 *  └── bottom : VBox
 *               ├── Separator
 *               └── HBox (label "Total" + label#totalLabel + tombol "Checkout")
 */
public class KeranjangController {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private Button backButton;

    /** Kontainer daftar produk (dari database). */
    @FXML private VBox menuListContainer;

    /** Kontainer daftar item keranjang. */
    @FXML private VBox cartItemContainer;

    /** Label total harga di bagian bawah. */
    @FXML private Label totalLabel;

    /** Label judul tab/bagian yang sedang aktif. */
    @FXML private Label sectionTitle;

    // ── Services ──────────────────────────────────────────────────────────────
    private final KeranjangService keranjangService   = KeranjangService.getInstance();
    private final Produk produkModel = new Produk();
    // ── Format Rupiah ─────────────────────────────────────────────────────────
    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // ── Init ──────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        muatSemuaMenu();
        refreshKeranjang();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/felix_71241153/app/chatbot_sibarista/Chat-view.fxml")
            );
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();

            double w = stage.getWidth();
            double h = stage.getHeight();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, w, h);
            stage.setScene(scene);
            stage.setTitle("SiBarista – Chatbot");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // ── Filter Kategori ───────────────────────────────────────────────────────
    private void muatSemuaMenu() {
        if (sectionTitle != null) sectionTitle.setText("Semua Menu");
        try {
            List<Produk> daftarProduk = produkModel.getAllProduk();
            tampilkanDaftarProduk(daftarProduk);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleFilterSemua(ActionEvent event) {
        muatSemuaMenu();
    }

    // 1. Ubah navigasi tombol agar mengirim angka ID (sesuaikan dengan ID di DB-mu)
    @FXML
    private void handleFilterKopi(ActionEvent event) {
        muatMenuByKategori(1); // Contoh: 1 adalah ID untuk Kopi
    }

    @FXML
    private void handleFilterNonKopi(ActionEvent event) {
        muatMenuByKategori(2); // Contoh: 2 adalah ID untuk Non-Kopi
    }

    @FXML
    private void handleFilterMakanan(ActionEvent event) {
        muatMenuByKategori(3); // Contoh: 3 adalah ID untuk Snack
    }

    // 2. Ubah parameter method ini dari String menjadi int
    private void muatMenuByKategori(int idKategori) {
        // Ganti judul section manual karena kita cuma kirim angka
        if (sectionTitle != null) {
            if (idKategori == 1) sectionTitle.setText("Kopi");
            else if (idKategori == 2) sectionTitle.setText("Non-Kopi");
            else if (idKategori == 3) sectionTitle.setText("Snack");
        }

        try {
            // Sekarang memanggil method Produk yang menerima int
            List<Produk> daftarProduk = produkModel.getProdukByKategori(idKategori);
            tampilkanDaftarProduk(daftarProduk);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Render Kartu Produk ───────────────────────────────────────────────────

    /**
     * Render ulang daftar produk di menuListContainer.
     * Setiap produk ditampilkan sebagai kartu horizontal dengan tombol [+].
     */
    private void tampilkanDaftarProduk(List<Produk> produkList) {
        menuListContainer.getChildren().clear();

        for (Produk p : produkList) {
            HBox kartu = buatKartuProduk(p);
            menuListContainer.getChildren().add(kartu);
        }
    }

    private HBox buatKartuProduk(Produk p) {
        // ── Gambar ────────────────────────────────────────────────────────────
        ImageView iv = new ImageView();
        iv.setFitWidth(70);
        iv.setFitHeight(70);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-background-radius: 10;");

        Image img = loadGambarProduk(p.getGambar());
        if (img != null) iv.setImage(img);

        // ── Info Teks ─────────────────────────────────────────────────────────
        Label lblNama = new Label(p.getNamaProduk());
        lblNama.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1C0A00;"
        );
        lblNama.setWrapText(true);

        Label lblKategori = new Label(p.getNamaKategori());
        lblKategori.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #A0522D; -fx-font-style: italic;"
        );

        Label lblHarga = new Label(formatRupiah(p.getHarga()));
        lblHarga.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #3D1A00;"
        );

        VBox infoBox = new VBox(3, lblNama, lblKategori, lblHarga);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // ── Tombol + ──────────────────────────────────────────────────────────
        Button btnTambah = new Button("+");
        btnTambah.setStyle(
                "-fx-background-color: #1C0A00;" +
                        "-fx-text-fill: #FBF7F0;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 36px;" +
                        "-fx-min-height: 36px;" +
                        "-fx-max-width: 36px;" +
                        "-fx-max-height: 36px;" +
                        "-fx-padding: 0;"
        );
        btnTambah.setOnAction(e -> {
            keranjangService.tambahProduk(p);
            refreshKeranjang();
        });

        // ── Hover Effect ──────────────────────────────────────────────────────
        btnTambah.setOnMouseEntered(e ->
                btnTambah.setStyle(btnTambah.getStyle().replace("#1C0A00", "#6B3A2A"))
        );
        btnTambah.setOnMouseExited(e ->
                btnTambah.setStyle(btnTambah.getStyle().replace("#6B3A2A", "#1C0A00"))
        );

        // ── Kartu ─────────────────────────────────────────────────────────────
        HBox kartu = new HBox(12, iv, infoBox, btnTambah);
        kartu.setAlignment(Pos.CENTER_LEFT);
        kartu.setPadding(new Insets(12, 16, 12, 16));
        kartu.setStyle(
                "-fx-background-color: #FBF7F0;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #C8A882;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);"
        );

        VBox.setMargin(kartu, new Insets(0, 0, 8, 0));
        return kartu;
    }

    // ── Render Keranjang ──────────────────────────────────────────────────────

    /**
     * Render ulang seluruh daftar item di keranjang
     * dan perbarui label total harga.
     */
    private void refreshKeranjang() {
        cartItemContainer.getChildren().clear();

        List<Keranjang> items = keranjangService.getItems();

        if (items.isEmpty()) {
            Label kosong = new Label("Keranjang masih kosong ☕");
            kosong.setStyle(
                    "-fx-text-fill: #A0522D; -fx-font-size: 13px; -fx-font-style: italic;"
            );
            kosong.setPadding(new Insets(12));
            cartItemContainer.getChildren().add(kosong);
        } else {
            for (Keranjang item : items) {
                HBox baris = buatBarisKeranjang(item);
                cartItemContainer.getChildren().add(baris);
            }
        }

        // Update total
        if (totalLabel != null) {
            totalLabel.setText(formatRupiah(keranjangService.getTotalHarga()));
        }
    }

    private HBox buatBarisKeranjang(Keranjang item) {
        Produk p = item.getProduk();

        Label lblNama = new Label(p.getNamaProduk());
        lblNama.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1C0A00;"
        );
        lblNama.setWrapText(true);

        Label lblSubtotal = new Label(formatRupiah(item.getSubtotal()));
        lblSubtotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #3D1A00;");

        VBox namaBox = new VBox(2, lblNama, lblSubtotal);
        namaBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(namaBox, Priority.ALWAYS);

        // Tombol −
        Button btnKurang = new Button("−");
        btnKurang.setStyle(qtyBtnStyle());
        btnKurang.setOnAction(e -> {
            keranjangService.kurangiProduk(p);
            refreshKeranjang();
        });

        // Label jumlah
        Label lblJumlah = new Label(String.valueOf(item.getJumlah()));
        lblJumlah.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1C0A00;" +
                        "-fx-min-width: 24; -fx-alignment: center;"
        );

        // Tombol +
        Button btnTambah = new Button("+");
        btnTambah.setStyle(qtyBtnStyle());
        btnTambah.setOnAction(e -> {
            keranjangService.tambahProduk(p);
            refreshKeranjang();
        });

        HBox qtyBox = new HBox(6, btnKurang, lblJumlah, btnTambah);
        qtyBox.setAlignment(Pos.CENTER);

        HBox baris = new HBox(10, namaBox, qtyBox);
        baris.setAlignment(Pos.CENTER_LEFT);
        baris.setPadding(new Insets(8, 12, 8, 12));
        baris.setStyle(
                "-fx-background-color: #F0E6D3;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #C8A882;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;"
        );
        VBox.setMargin(baris, new Insets(0, 0, 6, 0));

        return baris;
    }

    private String qtyBtnStyle() {
        return  "-fx-background-color: #1C0A00;" +
                "-fx-text-fill: #FBF7F0;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 50%;" +
                "-fx-min-width: 28px;" +
                "-fx-min-height: 28px;" +
                "-fx-max-width: 28px;" +
                "-fx-max-height: 28px;" +
                "-fx-padding: 0;";
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    @FXML
    private void handleCheckout(ActionEvent event) {
        // TODO: implementasi logika checkout (simpan ke DB, cetak struk, dll.)
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Checkout");
        alert.setHeaderText("Pesanan Diterima ☕");
        alert.setContentText(
                "Total pembayaran: " + formatRupiah(keranjangService.getTotalHarga()) +
                        "\n\nTerima kasih telah memesan di SiBarista!"
        );
        alert.showAndWait();
        keranjangService.kosongkanKeranjang();
        refreshKeranjang();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Image loadGambarProduk(String namaFile) {
        if (namaFile == null || namaFile.isEmpty()) return null;
        try {
            Path path = Paths.get("Chatbot_SiBarista", "src", "main", "resources", "images", namaFile);
            if (Files.exists(path)) return new Image(path.toUri().toString());
        } catch (Exception e) {
            System.out.println("Gagal load gambar: " + e.getMessage());
        }
        return null;
    }

    private String formatRupiah(double angka) {
        return RUPIAH.format(angka);
    }
}
