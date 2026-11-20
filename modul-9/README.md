# NavigationLab - Modul 09: DataStore & Room

Aplikasi Android hasil pengembangan NavigationLab Modul 08 dengan tambahan penyimpanan lokal menggunakan **Preferences DataStore** dan **Room**. Aplikasi tetap menampilkan data pengguna dari [DummyJSON](https://dummyjson.com), lalu menambahkan fitur favorit yang tersimpan secara persisten di perangkat.

## Fitur

- Menampilkan daftar pengguna dari REST API DummyJSON.
- Pencarian reaktif dengan debounce 500 ms.
- Query pencarian terakhir disimpan dan dipulihkan menggunakan DataStore.
- Halaman detail pengguna berdasarkan `id`.
- Simpan dan hapus mahasiswa favorit dari halaman detail.
- Halaman favorit yang membaca data langsung dari Room.
- Perubahan favorit otomatis tercermin di UI melalui `Flow`.
- Pengurutan favorit A-Z / Z-A yang disimpan di DataStore.
- Navigasi Compose ke Home, Detail, Favorit, Profile, dan About.

## Arsitektur

Proyek memakai pemisahan sederhana antara UI, ViewModel, repository, dan sumber data.

```text
+-----------------------------------------+
|  HomeScreen / DetailScreen /            |
|  FavoritesScreen (Compose)              |
+------------------+----------------------+
                   | collectAsStateWithLifecycle()
                   v
+-----------------------------------------+
|  HomeViewModel / DetailViewModel /      |
|  FavoritesViewModel                     |
+------------------+----------------------+
                   | suspend / Flow
                   v
+-----------------------------------------+
|  StudentRepository / FavoriteRepository |
+------------------+----------------------+
                   |                    |
                   v                    v
+----------------------------+  +----------------------------+
|  Retrofit + DummyJSON      |  |  Room (navigation_lab.db)  |
+----------------------------+  +----------------------------+
                                |
                                v
                       +----------------------------+
                       | Preferences DataStore      |
                       | (user_preferences)         |
                       +----------------------------+
```

Alur utama:

1. UI mengirim event ke ViewModel.
2. ViewModel membaca atau menulis melalui repository.
3. Data jaringan diambil dari DummyJSON melalui Retrofit.
4. Favorit disimpan di Room dan diamati sebagai `Flow`.
5. Preferensi pencarian dan urutan disimpan di DataStore.
6. Compose merender ulang UI dari state terbaru.

## Struktur Proyek

```text
app/src/main/java/com/example/myapplication/
├── MainActivity.kt
├── AboutScreen.kt
├── ProfileScreen.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── FavoriteStudentDao.kt
│   │   └── FavoriteStudentEntity.kt
│   ├── model/
│   │   └── Student.kt
│   ├── preferences/
│   │   ├── UserPreferencesDataStore.kt
│   │   └── UserPreferencesRepository.kt
│   ├── remote/
│   │   ├── RetrofitClient.kt
│   │   ├── StudentApiService.kt
│   │   ├── StudentRemoteDataSource.kt
│   │   └── dto/
│   │       ├── UserDto.kt
│   │       └── UserResponse.kt
│   └── repository/
│       ├── DefaultStudentRepository.kt
│       ├── FavoriteRepository.kt
│       └── StudentRepository.kt
├── navigation/
│   ├── NavigationLabApp.kt
│   └── Routes.kt
└── ui/
    ├── detail/
    │   ├── DetailUiState.kt
    │   └── DetailViewModel.kt
    ├── favorites/
    │   ├── FavoritesUiState.kt
    │   └── FavoritesViewModel.kt
    ├── home/
    │   ├── HomeUiState.kt
    │   └── HomeViewModel.kt
    ├── screen/
    │   ├── DetailScreen.kt
    │   ├── FavoritesScreen.kt
    │   └── HomeScreen.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Penyimpanan Lokal

### Preferences DataStore

- Nama file: `user_preferences`
- Key:
  - `last_search_query` (`String`)
  - `sort_ascending` (`Boolean`)
- Digunakan untuk memulihkan query pencarian terakhir dan menyimpan pilihan urutan favorit.

### Room

- Nama database: `navigation_lab.db`
- Tabel: `favorite_students`
- Kolom:
  - `id` (`Int`, primary key)
  - `firstName` (`String`)
  - `lastName` (`String`)
  - `email` (`String`)
- DAO menyediakan:
  - `observeFavorites(): Flow<List<FavoriteStudentEntity>>`
  - `observeIsFavorite(id: Int): Flow<Boolean>`
  - `insert(student: FavoriteStudentEntity)`
  - `deleteById(id: Int)`

## Teknologi

| Kategori | Teknologi |
|---|---|
| Bahasa | Kotlin 2.2.10 |
| UI | Jetpack Compose, Material 3 |
| Compose BOM | 2026.02.01 |
| Navigasi | Navigation Compose 2.9.8 |
| Lifecycle | Lifecycle 2.11.0 |
| Async | Kotlin Coroutines 1.11.0 |
| Networking | Retrofit 2.11.0 + Gson Converter |
| Penyimpanan preferensi | Preferences DataStore 1.3.0 |
| Penyimpanan lokal | Room 2.8.5 + KSP 2.2.10-2.0.2 |
| API | DummyJSON |
| Build | Android Gradle Plugin 9.3.3, Gradle 9.5.0 |
| SDK | compileSdk 37, targetSdk 37, minSdk 24 |
| Java | Source/target Java 11 |

## Endpoint API

Base URL: `https://dummyjson.com/`

| Method | Endpoint | Fungsi |
|---|---|---|
| `GET` | `users?limit=10` | Mengambil daftar pengguna |
| `GET` | `users/{id}` | Mengambil detail pengguna |
| `GET` | `users/search?q={query}` | Mencari pengguna berdasarkan kata kunci |

## Persyaratan

- Android Studio versi terbaru yang mendukung AGP 9.3.3 dan compileSdk 37.
- JDK 17 atau lebih baru.
- Emulator Android atau perangkat fisik dengan Android 7.0 (API 24) atau lebih baru.
- Koneksi internet untuk mengakses DummyJSON.
- Gradle Wrapper 9.5.0 sudah tersedia di proyek.

## Menjalankan Aplikasi

1. Buka folder `modul-9` di Android Studio.
2. Tunggu proses Gradle Sync selesai.
3. Pastikan emulator/perangkat terhubung.
4. Jalankan konfigurasi `app`.

Atau melalui terminal dari folder `modul-9`:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

APK hasil build:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Skenario Pengujian

| No | Skenario | Hasil yang Diharapkan |
|---|---|---|
| 1 | Buka aplikasi setelah query tersimpan | Query terakhir muncul kembali |
| 2 | Simpan mahasiswa sebagai favorit | Status favorit menjadi aktif |
| 3 | Tutup dan buka ulang aplikasi | Data favorit tetap tersedia |
| 4 | Hapus mahasiswa dari favorit | Data hilang dari Room dan status berubah |
| 5 | Buka Database Inspector | Tabel `favorite_students` dapat diamati |
| 6 | Buka halaman Favorit | Daftar favorit dari Room tampil |
| 7 | Ubah urutan favorit | Urutan A-Z / Z-A berubah dan bertahan |
| 8 | Hapus favorit dari DetailScreen | FavoritesScreen ikut berubah tanpa refresh manual |
| 9 | Matikan internet | Data Room tetap tersedia; permintaan jaringan dapat masuk error state |
| 10 | Navigasi Home - Detail - Favorit - Back | Back stack berjalan normal |

## Catatan

- Folder ini hanya memuat kode sumber dan README.
- Laporan, PDF, screenshot, dan file pendukung lain tidak disertakan.
- Nama aplikasi pada launcher masih `My Application` (bawaan template), sedangkan judul UI menggunakan `NavigationLab`.
- Proyek menggunakan permission `INTERNET` yang sudah dideklarasikan di `AndroidManifest.xml`.
- Tidak ada API key yang diperlukan.

## Penulis

- **Nama:** Naufal Abiyyu Arhab
- **NIM:** 245150201111029
- **Kelas:** TIF-B
- **Mata Kuliah:** Pengembangan Aplikasi Perangkat Bergerak
- **Asisten:** Rafi Ananta Nugraha, Mohamad Robi Alwan
