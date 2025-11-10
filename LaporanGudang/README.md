# Dashboard Laporan (Studi Kasus Java Thread + JDBC)

Ini adalah proyek studi kasus yang mendemonstrasikan gabungan konsep **Java Concurrency (Thread)** dan **Java Database Connectivity (JDBC)**.

Aplikasi ini adalah dashboard GUI (Java Swing) modern "dark mode" yang mengambil beberapa data laporan dari database MySQL secara bersamaan (paralel).

![Tangkapan Layar GUI Dashboard](![alt text](image.png))

---

## 🚀 Konsep Utama: Thread + Database

Tujuan utama proyek ini adalah untuk menyelesaikan masalah "GUI Freeze" saat menjalankan tugas berat yang berhubungan dengan database.

### 1. Masalah: Event Dispatch Thread (EDT) "Freeze"

Setiap aplikasi Java Swing berjalan pada satu thread utama yang disebut **Event Dispatch Thread (EDT)**. EDT ini bertugas untuk:
* Menggambar tombol dan label.
* Merespon klik mouse dan input keyboard.

Jika kita menjalankan query database yang lambat (seperti 3 query laporan kita) **langsung di dalam EDT**, maka EDT akan "sibuk" menunggu database selesai. Akibatnya, GUI akan **macet total (freeze)**. Pengguna tidak bisa mengklik apa-apa, dan aplikasi akan terlihat "Not Responding".

### 2. Solusi: `ExecutorService` (Background Thread)

Solusinya adalah mendelegasikan semua "tugas berat" (DB query) ke *thread* terpisah di *background*.

* Kita menggunakan **`ExecutorService`** (sebuah Thread Pool) dengan 3 *thread*. Ini seperti memiliki 3 "pekerja" di dapur.
* Saat tombol "Generate" diklik, EDT tidak menjalankan query. Ia hanya memberikan 3 "pesanan" (tugas) ke 3 pekerja tersebut.
* GUI (EDT) tetap responsif, sementara 3 *thread* pekerja menjalankan 3 query database secara bersamaan (paralel).

### 3. Solusi: `SwingUtilities.invokeLater()`

Setelah *worker thread* selesai (misalnya, `taskB` selesai mengambil jumlah pelanggan), ia **tidak boleh** langsung memperbarui GUI (misal `lblJumlahPelanggan.setText(...)`). Itu berbahaya dan bisa merusak GUI.

* Untuk keamanan, *worker thread* harus mengirimkan hasilnya kembali ke EDT.
* Kita menggunakan **`SwingUtilities.invokeLater(...)`**. Ini adalah cara aman untuk "mengantri" pembaruan GUI agar dieksekusi oleh EDT.

---

## ⚙️ Alur Kerja (Saat "Generate Laporan" Diklik)

1.  **(Thread EDT):** Tombol diklik. GUI langsung di-update ke status "MEMPROSES..." dan tombol dinonaktifkan.
2.  **(Thread EDT):** Tiga objek `Runnable` (taskA, taskB, taskC) dibuat.
3.  **(Pindah Thread):** Ketiga *task* di-submit ke `ExecutorService` (`pool.submit(...)`). Tugas EDT selesai, GUI tetap responsif.
4.  **(Worker Thread 1, 2, 3):** Tiga *thread* dari *pool* secara paralel memanggil method `LaporanDAO` (misal `dao.getA_TotalPenjualan()`). Thread-thread ini "diblokir" saat menunggu `Thread.sleep()` dan database, tapi EDT tetap aman.
5.  **(Kembali ke EDT):** Begitu satu *task* selesai (misal, `taskB` selesai setelah 1 detik), ia membungkus hasilnya ke dalam `SwingUtilities.invokeLater(...)`.
6.  **(Thread EDT):** EDT menerima pesan dan menjalankan kode di dalamnya, yaitu `lblJumlahPelanggan.setText(...)`.
7.  *(Proses 5 & 6 terulang 2 kali lagi saat `taskC` dan `taskA` selesai).*
8.  **(Thread EDT):** Method `cekJikaSelesai()` mendeteksi semua hasil sudah ada, lalu meng-update status akhir ke "Laporan Selesai!" dan mengaktifkan tombol kembali.

---

## 🏛️ Arsitektur Proyek (Pola DAO)

Kode ini sengaja dipisah menggunakan arsitektur **DAO (Data Access Object)**.

* `gui/DashboardGUI.java`:
    * **Peran: View/Controller**.
    * Hanya bertanggung jawab untuk tampilan (Swing) dan merespon *event* (klik tombol).
    * Tidak berisi kode SQL sama sekali. Ia hanya memanggil method DAO.

* `dao/LaporanDAO.java`:
    * **Peran: Data Access Object (DAO)**.
    * Berisi **SEMUA** logika database (query SQL `SELECT`, `TRUNCATE`, dll.).
    * GUI tidak perlu tahu rumitnya query SQL.

* `dao/DBUtil.java`:
    * **Peran: Utility**.
    * Satu-satunya tugasnya adalah mengelola dan menyediakan koneksi ke database (`getConnection()`).

---

## 🛠️ Cara Menjalankan

1.  Pastikan **MySQL Server** (misalnya dari XAMPP) sudah berjalan.
2.  Buka `phpMyAdmin` (atau *tool* sejenis) dan jalankan `database_laporan.sql` untuk membuat tabel `products`, `sales`, dan `customers` di dalam database `belajar`.
3.  Pastikan file `mysql-connector-j-x.x.x.jar` sudah ada di folder `lib` dan sudah ditambahkan ke **Referenced Libraries** di VS Code.
4.  Buka file `src/dao/DBUtil.java` dan pastikan `USER` dan `PASS` sudah sesuai dengan pengaturan MySQL Anda.
5.  Buka dan jalankan file `src/gui/DashboardGUI.java` (Klik kanan > Run Java).