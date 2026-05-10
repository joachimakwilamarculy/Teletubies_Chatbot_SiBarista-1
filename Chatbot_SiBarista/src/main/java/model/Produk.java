package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import database.Database;

public class Produk {
    private String idProduk;
    private String namaProduk;
    private String namaKategori;
    private String deskripsi;
    private int harga;
    private String statusStok;
    private String gambar;

    // Constructor kosong
    public Produk() {
    }

    // Constructor lengkap
    public Produk(String idProduk, String namaProduk, String namaKategori, String deskripsi, int harga, String statusStok, String gambar) {
        this.idProduk = idProduk;
        this.namaProduk = namaProduk;
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.statusStok = statusStok;
        this.gambar = gambar;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getIdProduk() {
        return idProduk;
    }

    public void setIdProduk(String idProduk) {
        this.idProduk = idProduk;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }

    public String getStatusStok() {
        return statusStok;
    }

    public void setStatusStok(String statusStok) {
        this.statusStok = statusStok;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public String getDetailProduk() {
        return String.format("%s (%s) - Rp%d [%s]", namaProduk, namaKategori, harga, statusStok);
    }

    // Method tambahan untuk mempercantik tampilan di Tabel atau UI lainnya
    public String getHargaFormatted() {
        return String.format("Rp%,d", harga).replace(',', '.');
    }

    // Tambahkan toString() untuk memudahkan debugging di console
    @Override
    public String toString() {
        return "Produk{" +
                "id='" + idProduk + '\'' +
                ", nama='" + namaProduk + '\'' +
                ", kategori='" + namaKategori + '\'' +
                '}';
    }

    // ── Database Methods ─────────────────────────────────────────────────────

    /**
     * Mengambil semua data produk dari database
     */
    public List<Produk> getAllProduk() throws SQLException {
        List<Produk> listProduk = new ArrayList<>();

        // Sesuaikan dengan nama tabel di database Anda
        String query = "SELECT * FROM produk";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produk p = new Produk();
                // PENTING: Sesuaikan nama-nama kolom di bawah ini dengan struktur tabel database Anda!
                p.setIdProduk(rs.getString("id_produk"));
                p.setNamaProduk(rs.getString("nama_produk"));
                p.setNamaKategori(rs.getString("id_kategori"));
                p.setDeskripsi(rs.getString("deskripsi"));
                p.setHarga(rs.getInt("harga"));
                p.setStatusStok(rs.getString("status_stok"));
                p.setGambar(rs.getString("gambar"));

                listProduk.add(p);
            }
        }
        return listProduk;
    }

    /**
     * Mengambil data produk berdasarkan kategori tertentu dari database
     */
    // --- Perubahan pada Produk.java ---

    /**
     * Mengambil data produk berdasarkan kategori tertentu dari database.
     * Diubah menggunakan 'int' karena database menggunakan ID angka.
     */
    public List<Produk> getProdukByKategori(int idKategori) throws SQLException {
        List<Produk> listProduk = new ArrayList<>();

        // 1. Sesuaikan nama kolom: Gunakan 'id_kategori' (bukan 'kategori')
        String query = "SELECT * FROM produk WHERE id_kategori = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // 2. Gunakan setInt karena idKategori adalah angka
            stmt.setInt(1, idKategori);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produk p = new Produk();
                    p.setIdProduk(rs.getString("id_produk"));
                    p.setNamaProduk(rs.getString("nama_produk"));

                    // Mengambil ID Kategori sebagai String untuk property namaKategori
                    p.setNamaKategori(rs.getString("id_kategori"));

                    p.setDeskripsi(rs.getString("deskripsi"));
                    p.setHarga(rs.getInt("harga"));
                    p.setStatusStok(rs.getString("status_stok"));
                    p.setGambar(rs.getString("gambar"));

                    listProduk.add(p);
                }
            }
        }
        return listProduk;
    }
}