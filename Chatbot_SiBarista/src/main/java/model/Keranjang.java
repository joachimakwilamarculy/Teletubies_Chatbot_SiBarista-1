package model;

/**
 * Model satu item di dalam keranjang belanja.
 * Menyimpan referensi ke Produk dan jumlah (quantity).
 */
public class Keranjang {

    private Produk produk;
    private int    jumlah;

    public Keranjang(Produk produk, int jumlah) {
        this.produk = produk;
        this.jumlah = jumlah;
    }

    // ── Getter & Setter ───────────────────────────────────────────────────────

    public Produk getProduk()          { return produk; }
    public void   setProduk(Produk p)  { this.produk = p; }

    public int  getJumlah()            { return jumlah; }
    public void setJumlah(int jumlah)  { this.jumlah = jumlah; }

    // ── Kalkulasi ─────────────────────────────────────────────────────────────

    /** Subtotal = harga × jumlah. */
    public double getSubtotal() {
        return produk.getHarga() * jumlah;
    }

    /** Tambah jumlah 1. */
    public void tambah() { this.jumlah++; }

    /** Kurangi jumlah 1 (minimal 0). */
    public void kurang() { if (this.jumlah > 0) this.jumlah--; }
}