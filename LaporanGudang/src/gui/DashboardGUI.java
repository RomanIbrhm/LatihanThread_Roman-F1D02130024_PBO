package gui;

import dao.LaporanDAO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.sql.SQLException; // <-- PASTIKAN INI DI-IMPORT
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardGUI extends JFrame {

    private JButton btnGenerate;
    private JButton btnTruncate; // <-- BARU: Tombol untuk truncate
    private JLabel lblStatus;

    private JLabel lblTotalPenjualan;
    private JLabel lblJumlahPelanggan;
    private JLabel lblTotalStok;

    private LaporanDAO dao;
    private ExecutorService pool = Executors.newFixedThreadPool(3);

    private final Color COLOR_BACKGROUND = new Color(24, 26, 33);
    private final Color COLOR_PANEL = new Color(34, 37, 49);
    private final Color COLOR_TEXT_PRIMARY = new Color(230, 230, 230);
    private final Color COLOR_TEXT_SECONDARY = new Color(150, 150, 150);
    private final Color COLOR_ACCENT = new Color(0, 123, 255);
    private final Color COLOR_DATA = new Color(40, 167, 69);
    private final Color COLOR_DANGER = new Color(220, 53, 69); // <-- BARU: Warna merah

    public DashboardGUI() {
        super("Dashboard Laporan Toko (Modern)");
        dao = new LaporanDAO();
        initComponents();
    }

    private void initComponents() {
        JPanel controlPanel = new JPanel(new BorderLayout(20, 10));
        controlPanel.setBackground(COLOR_BACKGROUND);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // --- DIUBAH: Panel baru untuk menampung 2 tombol ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(COLOR_BACKGROUND);

        btnGenerate = new JButton("GENERATE LAPORAN");
        styleButton(btnGenerate, COLOR_ACCENT); // Terapkan style

        // --- BARU: Buat tombol Truncate ---
        btnTruncate = new JButton("RESET DATA");
        styleButton(btnTruncate, COLOR_DANGER); // Terapkan style dengan warna bahaya

        buttonPanel.add(btnGenerate);
        buttonPanel.add(btnTruncate);
        // ------------------------------------
        
        lblStatus = new JLabel("Status: Idle");
        lblStatus.setForeground(COLOR_TEXT_SECONDARY);
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        
        controlPanel.add(buttonPanel, BorderLayout.CENTER); // <-- DIUBAH: Tambahkan panel tombol
        controlPanel.add(lblStatus, BorderLayout.SOUTH);

        JPanel reportPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        reportPanel.setBackground(COLOR_BACKGROUND);
        reportPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTotalPenjualan = new JLabel("Rp 0");
        lblJumlahPelanggan = new JLabel("0 Orang");
        lblTotalStok = new JLabel("0 Unit");

        reportPanel.add(createWidgetPanel("TOTAL PENJUALAN", lblTotalPenjualan, COLOR_DATA));
        reportPanel.add(createWidgetPanel("JUMLAH PELANGGAN", lblJumlahPelanggan, COLOR_ACCENT));
        reportPanel.add(createWidgetPanel("TOTAL STOK", lblTotalStok, Color.ORANGE));

        add(controlPanel, BorderLayout.NORTH);
        add(reportPanel, BorderLayout.CENTER);

        // --- Event Listener ---
        btnGenerate.addActionListener(e -> generateLaporan());
        btnTruncate.addActionListener(e -> truncateData()); // <-- BARU: Listener untuk truncate

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 350);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
    }

    private JPanel createWidgetPanel(String title, JLabel dataLabel, Color dataColor) {
        // ... (Tidak ada perubahan di method ini) ...
        JPanel widget = new JPanel(new BorderLayout());
        widget.setBackground(COLOR_PANEL);
        widget.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BACKGROUND, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(COLOR_TEXT_SECONDARY);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        widget.add(lblTitle, BorderLayout.NORTH);

        dataLabel.setForeground(dataColor);
        dataLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        dataLabel.setHorizontalAlignment(SwingConstants.LEFT);
        widget.add(dataLabel, BorderLayout.CENTER);

        return widget;
    }

    /**
     * DIUBAH: Menerima parameter warna
     */
    private void styleButton(JButton button, Color color) {
        button.setBackground(color); // <-- DIUBAH
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * BARU: Helper method untuk mengaktifkan/menonaktifkan kedua tombol
     */
    private void setButtonsEnabled(boolean enabled) {
        btnGenerate.setEnabled(enabled);
        btnTruncate.setEnabled(enabled);
    }

    private void generateLaporan() {
        setButtonsEnabled(false); // <-- DIUBAH: Panggil helper
        btnGenerate.setText("MEMPROSES...");
        lblStatus.setText("Status: Memproses...");
        
        lblTotalPenjualan.setText("...");
        lblJumlahPelanggan.setText("...");
        lblTotalStok.setText("...");

        Runnable taskA = () -> {
            try {
                double total = dao.getA_TotalPenjualan();
                SwingUtilities.invokeLater(() -> {
                    lblTotalPenjualan.setText("Rp " + String.format("%,.0f", total));
                    cekJikaSelesai();
                });
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> lblTotalPenjualan.setText("Error!"));
            }
        };

        Runnable taskB = () -> {
            try {
                int total = dao.getB_JumlahPelanggan();
                SwingUtilities.invokeLater(() -> {
                    lblJumlahPelanggan.setText(total + " Orang");
                    cekJikaSelesai();
                });
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> lblJumlahPelanggan.setText("Error!"));
            }
        };

        Runnable taskC = () -> {
            try {
                int total = dao.getC_TotalStok();
                SwingUtilities.invokeLater(() -> {
                    lblTotalStok.setText(total + " Unit");
                    cekJikaSelesai();
                });
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> lblTotalStok.setText("Error!"));
            }
        };

        pool.submit(taskA); 
        pool.submit(taskB); 
        pool.submit(taskC); 
    }

    /**
     * BARU: Method untuk menjalankan truncate di background thread
     */
    private void truncateData() {
        // 1. Tampilkan konfirmasi
        int choice = JOptionPane.showConfirmDialog(this,
            "Ini akan menghapus SEMUA data (sales, products, dan customers).\n" +
            "Tindakan ini tidak bisa dibatalkan. Anda yakin?",
            "Konfirmasi Reset Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (choice != JOptionPane.YES_OPTION) {
            return; // Batalkan jika pengguna memilih "No"
        }

        // 2. Update GUI di EDT
        setButtonsEnabled(false); // Nonaktifkan tombol
        lblStatus.setText("Status: Mereset data...");
        
        // 3. Buat task untuk background thread
        Runnable task = () -> {
            try {
                // Panggil method DAO yang berbahaya
                dao.truncateAllTables();
                
                // 4. Update GUI di EDT setelah sukses
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Status: Data berhasil direset!");
                    lblTotalPenjualan.setText("Rp 0");
                    lblJumlahPelanggan.setText("0 Orang");
                    lblTotalStok.setText("0 Unit");
                    setButtonsEnabled(true);
                    btnGenerate.setText("GENERATE LAPORAN");
                });
            } catch (SQLException e) {
                // 5. Update GUI di EDT jika gagal
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Status: Gagal mereset data!");
                    setButtonsEnabled(true);
                    btnGenerate.setText("GENERATE LAPORAN");
                });
                e.printStackTrace();
            }
        };
        
        // 6. Jalankan task
        pool.submit(task);
    }

    private synchronized void cekJikaSelesai() {
        if (!lblTotalPenjualan.getText().contains("...") &&
            !lblJumlahPelanggan.getText().contains("...") &&
            !lblTotalStok.getText().contains("...")
        ) {
            lblStatus.setText("Status: Laporan Selesai!");
            setButtonsEnabled(true); // <-- DIUBAH: Panggil helper
            btnGenerate.setText("GENERATE LAPORAN");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DashboardGUI().setVisible(true);
        });
    }
}