# Tucil3_13524014

Ice Sliding Puzzle Solver untuk Tugas Kecil 3 IF2211 Strategi Algoritma.

Program ini membaca puzzle dari file `.txt`, memvalidasi input, lalu mencari solusi dengan algoritma pathfinding. Permainan menggunakan mekanik es: setelah memilih arah, aktor terus meluncur sampai berhenti tepat sebelum obstacle. Program juga mendukung checkpoint bernomor yang harus dilewati berurutan, lava, traversal cost, playback solusi, penyimpanan hasil, dan GUI JavaFX.

## Fitur

- Parser dan validator input `.txt`.
- Solver CLI untuk:
  - Uniform Cost Search (UCS)
  - Greedy Best First Search (GBFS)
  - A* Search
- Heuristik untuk GBFS dan A*:
  - H1: Manhattan ke goal
  - H2: Manhattan ke target wajib berikutnya
  - H3: rantai Manhattan melalui checkpoint tersisa lalu goal
- Visualisasi step-by-step di CLI.
- Playback CLI dengan command sederhana.
- Simpan solusi dan snapshot iterasi ke file `.txt`.
- GUI JavaFX untuk memilih file, memilih algoritma/heuristik, menjalankan solver, melihat board, playback, dan menyimpan hasil.

## Requirement

- Java 21
- Maven

Cek instalasi:

```powershell
java -version
mvn -version
```

JavaFX tidak perlu diinstal manual karena dependency JavaFX dikelola oleh Maven melalui `pom.xml`.

## Struktur Singkat

```text
Tucil3_13524014/
|-- src/main/java/   # Source code Java
|-- bin/             # Folder keluaran sesuai struktur tugas
|-- test/            # File input dan output pengujian
|-- doc/             # Laporan dan gambar laporan
|-- pom.xml          # Konfigurasi Maven
`-- README.md        # Dokumentasi program
```

## Compile

Jalankan dari root project:

```powershell
mvn clean compile
```

Folder `target/` akan dibuat otomatis oleh Maven sebagai hasil build. Setelah itu jalankan CLI atau GUI sesuai tata cara dibawah ini.

## Menjalankan CLI

Mode interaktif:

```powershell
mvn exec:java
```

Dengan argumen file input:

```powershell
mvn exec:java "-Dexec.args=test\test1.txt"
```

Setelah program berjalan:

1. Masukkan path file input jika belum diberikan lewat argumen.
2. Pilih algoritma: `UCS`, `GBFS`, atau `A*`.
3. Jika memilih `GBFS` atau `A*`, pilih heuristik: `H1`, `H2`, atau `H3`.
4. Program menampilkan solusi, total cost, waktu eksekusi, jumlah iterasi, dan visualisasi board tiap step.
5. Pilih apakah ingin playback.
6. Pilih apakah ingin menyimpan solusi.

Command playback CLI:

```text
n          next step
p          previous step
j <nomor>  jump ke step tertentu, contoh: j 3
q          keluar dari playback
```

## Menjalankan GUI

```powershell
mvn javafx:run
```

Cara menggunakan GUI:

1. Klik `Choose File`.
2. Pilih file puzzle `.txt`.
3. Pilih algoritma: `UCS`, `GBFS`, atau `A*`.
4. Pilih heuristik untuk `GBFS` atau `A*`.
5. Klik `Solve`.
6. Gunakan tombol playback:
   - `Previous`
   - `Play/Pause`
   - `Next`
   - speed slider
   - jump-to-step
7. Klik `Save Results` untuk menyimpan solusi dan snapshot iterasi.

Dropdown heuristik otomatis dinonaktifkan saat memilih UCS karena UCS tidak memakai heuristik.

## Format Input

Format file input:

```text
N M
<N baris layout board>
<N baris traversal cost, masing-masing M integer>
```

Karakter board:

```text
*  jalan es
X  obstacle/wall
L  lava
Z  start
O  goal
0-9 checkpoint berurutan
```

Contoh:

```text
5 9
XXXXXXXXX
X*******X
XZ*0*1*OX
X*******X
XXXXXXXXX
999 999 999 999 999 999 999 999 999
999 1   1   1   1   1   1   1   999
999 1   1   1   1   1   1   1   999
999 1   1   1   1   1   1   1   999
999 999 999 999 999 999 999 999 999
```

## Algoritma

- UCS menggunakan `f(n) = g(n)`.
- GBFS menggunakan `f(n) = h(n)`.
- A* menggunakan `f(n) = g(n) + h(n)`.

`g(n)` adalah akumulasi movement cost dari start sampai state sekarang. `h(n)` adalah estimasi biaya tersisa berdasarkan heuristik.

Program memakai `PriorityQueue` dan `bestG` untuk menghindari ekspansi state yang lebih buruk atau setara secara berlebihan. Identitas state terdiri dari posisi aktor dan `nextCheckpointIndex`.

```

## Author

Yusuf Faishal Listyardi  
NIM 13524014
