package service;

import model.Keranjang;
import model.Produk;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service untuk menyimpan state keranjang belanja
 * agar bisa diakses oleh ChatBotController maupun CartController.
 */
public class KeranjangService {

    // ── Singleton ────────────────────────────────────────────────────────────
    private static KeranjangService instance;

    private KeranjangService() {}

    public static KeranjangService getInstance() {
        if (instance == null) {
            instance = new KeranjangService();
        }
        return instance;
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    private final List<Keranjang> items = new ArrayList<>();

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Tambah produk ke keranjang.
     * Jika produk sudah ada, naikkan jumlahnya 1.
     */
    public void tambahProduk(Produk produk) {
        for (Keranjang item : items) {
            if (item.getProduk().getNamaProduk().equalsIgnoreCase(produk.getNamaProduk())) {
                item.tambah();
                return;
            }
        }
        items.add(new Keranjang(produk, 1));
    }

    /**
     * Kurangi jumlah produk sebesar 1.
     * Jika jumlah menjadi 0, hapus item dari keranjang.
     */
    public void kurangiProduk(Produk produk) {
        items.removeIf(item -> {
            if (item.getProduk().getNamaProduk().equalsIgnoreCase(produk.getNamaProduk())) {
                item.kurang();
                return item.getJumlah() == 0;
            }
            return false;
        });
    }

    /** Hapus item dari keranjang sepenuhnya. */
    public void hapusProduk(Produk produk) {
        items.removeIf(item ->
                item.getProduk().getNamaProduk().equalsIgnoreCase(produk.getNamaProduk())
        );
    }

    /** Kosongkan seluruh keranjang. */
    public void kosongkanKeranjang() {
        items.clear();
    }

    /** Kembalikan seluruh item keranjang (read-only view). */
    public List<Keranjang> getItems() {
        return items;
    }

    /** Hitung total harga semua item. */
    public double getTotalHarga() {
        return items.stream().mapToDouble(Keranjang::getSubtotal).sum();
    }

    /** Hitung total jumlah item (untuk badge di tombol keranjang). */
    public int getTotalJumlah() {
        return items.stream().mapToInt(Keranjang::getJumlah).sum();
    }
}
