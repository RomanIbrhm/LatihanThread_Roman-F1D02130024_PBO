package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LaporanDAO {

    private void simulasiQueryBerat(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public double getA_TotalPenjualan() throws SQLException {
        String sql = "SELECT SUM(total_amount) AS total FROM sales";
        double total = 0;

        simulasiQueryBerat(3000);

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getDouble("total");
            }
        }
        return total;
    }

    public int getB_JumlahPelanggan() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM customers";
        int total = 0;

        simulasiQueryBerat(1000);

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getInt("total");
            }
        }
        return total;
    }

    public int getC_TotalStok() throws SQLException {
        String sql = "SELECT SUM(stock) AS total FROM products";
        int total = 0;

        simulasiQueryBerat(2000);

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                total = rs.getInt("total");
            }
        }
        return total;
    }

    public void truncateAllTables() throws SQLException {
        // Kita butuh 3 perintah SQL
        String sqlSales = "TRUNCATE TABLE sales";
        String sqlProducts = "TRUNCATE TABLE products";
        String sqlCustomers = "TRUNCATE TABLE customers";

        // Kita gunakan Statement biasa karena tidak ada parameter
        try (Connection c = DBUtil.getConnection();
             Statement s = c.createStatement()) {
            
            // Nonaktifkan foreign key checks untuk sementara jika ada relasi
            s.execute("SET FOREIGN_KEY_CHECKS=0");
            
            // Eksekusi semua truncate
            s.execute(sqlSales);
            s.execute(sqlProducts);
            s.execute(sqlCustomers);
            
            // Aktifkan kembali foreign key checks
            s.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }
}

