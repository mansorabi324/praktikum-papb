# NavigationLab - Modul 08: Coroutines & Flow

Aplikasi Android sederhana untuk menampilkan data pengguna dari [DummyJSON](https://dummyjson.com) secara reaktif menggunakan **Kotlin Coroutines** dan **Flow**. Proyek ini merupakan hasil refactoring NavigationLab Modul 07: proses jaringan yang sebelumnya memakai callback sekarang memakai `suspend function`, state UI dikelola dengan `StateFlow`, dan pencarian dijalankan otomatis dengan `debounce`.

## Daftar Isi

- [Fitur](#fitur)
- [Arsitektur](#arsitektur)
- [Struktur Proyek](#struktur-proyek)
- [Teknologi](#teknologi)
- [Endpoint API](#endpoint-api)
- [Detail Implementasi](#detail-implementasi)
- [Persyaratan](#persyaratan)
- [Menjalankan Aplikasi](#menjalankan-aplikasi)
- [Skenario Pengujian](#skenario-pengujian)
- [Catatan](#catatan)
- [Penulis](#penulis)

## Fitur

- Menampilkan daftar pengguna dari REST API DummyJSON.
- Pencarian reaktif dengan debounce **500 ms**.
- Hasil pencarian diperbarui otomatis tanpa tombol submit.
- Tombol **Refresh** untuk memuat ulang data.
- Tombol **Coba Lagi** saat terjadi error.
- Halaman detail pengguna berdasarkan `id`.
- Indikator loading, pesan error, dan empty state.
- State dikelola dengan `StateFlow` dan dikoleksi secara lifecycle-aware.
- Navigasi Compose ke Home, Detail, Profile, dan About.

## Arsitektur

Proyek memakai pemisahan sederhana antara UI, ViewModel, repository, dan remote data source.

```text
+-----------------------------------------+
|  HomeScreen / DetailScreen (Compose)    |
|  mengirim event, merender state         |
+------------------+----------------------+
                   | collectAsStateWithLifecycle()
                   v
+-----------------------------------------+
|  HomeViewModel / DetailViewModel        |
|  StateFlow, viewModelScope, debounce    |
+------------------+----------------------+
                   | suspend function
                   v
+-----------------------------------------+
|  StudentRepository (interface)          |
+------------------+----------------------+
                   v
+-----------------------------------------+
|  DefaultStudentRepository               |
|  mapping DTO -> model                   |
+------------------+----------------------+
                   v
+-----------------------------------------+
|  StudentRemoteDataSource                |
+------------------+----------------------+
                   v
+-----------------------------------------+
|  StudentApiService (Retrofit)           |
+------------------+----------------------+
                   v
          https://dummyjson.com
```

Alur data utama:

1. UI memanggil event ViewModel, misalnya `onSearchQueryChange()` atau `onRefresh()`.
2. ViewModel memperbarui `StateFlow`.
3. `viewModelScope` menjalankan operasi `suspend` ke repository.
4. Repository mengambil data dari `StudentRemoteDataSource`.
5. Retrofit memanggil API dan mengembalikan DTO.
6. DTO dipetakan menjadi model `Student`.
7. ViewModel memperbarui `HomeUiState`.
8. Compose merender ulang UI dari state terbaru.

## Struktur Proyek

```text
app/src/main/java/com/example/myapplication/
├── MainActivity.kt
├── AboutScreen.kt
├── ProfileScreen.kt
├── data/
│   ├── model/
│   │   └── Student.kt
│   ├── remote/
│   │   ├── RetrofitClient.kt
│   │   ├── StudentApiService.kt
│   │   ├── StudentRemoteDataSource.kt
│   │   └── dto/
│   │       ├── UserDto.kt
│   │       └── UserResponse.kt
│   └── repository/
│       ├── StudentRepository.kt
│       └── DefaultStudentRepository.kt
├── navigation/
│   ├── NavigationLabApp.kt
│   └── Routes.kt
└── ui/
    ├── detail/
    │   ├── DetailUiState.kt
    │   └── DetailViewModel.kt
    ├── home/
    │   ├── HomeUiState.kt
    │   └── HomeViewModel.kt
    ├── screen/
    │   ├── HomeScreen.kt
    │   └── DetailScreen.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

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

Contoh:

```text
GET https://dummyjson.com/users?limit=10
GET https://dummyjson.com/users/1
GET https://dummyjson.com/users/search?q=john
```

## Detail Implementasi

### 1. Suspend Function pada API

Semua operasi jaringan di `StudentApiService` memakai `suspend`, sehingga pemanggilannya harus berada di dalam coroutine.

```kotlin
interface StudentApiService {
    @GET("users")
    suspend fun getUsers(
        @Query("limit") limit: Int = 10
    ): UserResponse

    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: Int
    ): UserDto

    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String
    ): UserResponse
}
```

### 2. StateFlow pada ViewModel

`HomeViewModel` menyimpan state UI dan query pencarian sebagai `MutableStateFlow`, lalu mengekspos versi read-only dengan `asStateFlow()`.

```kotlin
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
```

### 3. Pencarian Reaktif dengan Debounce

Perubahan query diproses di `viewModelScope` dengan operator Flow:

```kotlin
@OptIn(FlowPreview::class)
private fun observeSearch() {
    viewModelScope.launch {
        searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .collectLatest { query ->
                loadData(query)
            }
    }
}
```

Penjelasan:

- `debounce(500)` menunda pemrosesan sampai pengguna berhenti mengetik selama 500 ms.
- `distinctUntilChanged()` mencegah pemrosesan query yang sama berulang kali.
- `collectLatest` membatalkan pemrosesan lama jika ada query baru.

### 4. Refresh dan Retry

`refresh()` memakai query yang sedang aktif. Jika query kosong, aplikasi mengambil daftar awal; jika ada query, aplikasi mencari ulang dengan query tersebut.

```kotlin
fun refresh() {
    val query = searchQuery.value
    viewModelScope.launch {
        loadData(query)
    }
}

fun retry() {
    refresh()
}
```

### 5. Error Handling

`loadData()` menangani loading, sukses, dan error. `CancellationException` dilempar kembali agar pembatalan coroutine tidak dianggap sebagai error aplikasi.

```kotlin
private suspend fun loadData(query: String) {
    _uiState.value = _uiState.value.copy(
        isLoading = true,
        errorMessage = null
    )
    try {
        val data = if (query.isBlank()) {
            repository.getStudents()
        } else {
            repository.searchStudents(query)
        }
        _uiState.value = HomeUiState(students = data)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        _uiState.value = HomeUiState(
            errorMessage = e.message ?: "Gagal mengambil data"
        )
    }
}
```

### 6. Lifecycle-Aware State Collection

Di `NavigationLabApp`, state dikoleksi dengan `collectAsStateWithLifecycle()` agar koleksi berhenti saat UI tidak aktif.

```kotlin
val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
val searchQuery by homeViewModel.searchQuery.collectAsStateWithLifecycle()
```

### 7. Halaman Detail

`DetailViewModel.loadStudent(id)` mengambil data berdasarkan `id`. Pemanggilan dipicu dari `LaunchedEffect(studentId)` saat halaman detail dibuka.

```kotlin
LaunchedEffect(studentId) {
    detailViewModel.loadStudent(studentId)
}
```

## Persyaratan

- Android Studio versi terbaru yang mendukung AGP 9.3.3 dan compileSdk 37.
- JDK 17 atau lebih baru (disarankan memakai JDK bawaan Android Studio).
- Emulator Android atau perangkat fisik dengan Android 7.0 (API 24) atau lebih baru.
- Koneksi internet untuk mengakses DummyJSON.
- Gradle Wrapper 9.5.0 sudah tersedia di proyek.

## Menjalankan Aplikasi

1. Buka folder proyek di Android Studio.
2. Tunggu proses Gradle Sync selesai.
3. Pastikan emulator/perangkat terhubung.
4. Jalankan konfigurasi `app`.

Atau melalui terminal:

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
| 1 | Buka aplikasi | Daftar 10 pengguna dari DummyJSON tampil |
| 2 | Ketik `john` pada field pencarian | Hasil pencarian muncul otomatis setelah +/-500 ms |
| 3 | Mengetik cepat beberapa karakter | Hanya query terakhir yang diproses, tanpa crash |
| 4 | Tekan **Lihat Detail** | Halaman detail menampilkan nama, email, dan university |
| 5 | Tekan **Kembali ke Home** | Kembali ke daftar pengguna |
| 6 | Matikan internet lalu tekan **Refresh** | Muncul pesan error dan tombol **Coba Lagi** |
| 7 | Nyalakan internet lalu tekan **Coba Lagi** | Data berhasil dimuat kembali |
| 8 | Tekan **Refresh** dengan query aktif | Hasil pencarian untuk query tersebut dimuat ulang |
| 9 | Tekan **Refresh** dengan query kosong | Daftar awal pengguna dimuat ulang |
| 10 | Cari kata yang tidak ada | Muncul empty state "Tidak ada hasil" |

## Catatan

- Data DummyJSON bersifat statis, sehingga tampilan sebelum dan sesudah **Refresh** bisa terlihat sama.
- Nama aplikasi pada launcher masih `My Application` (bawaan template), sedangkan judul UI menggunakan `NavigationLab`.
- Proyek menggunakan `INTERNET` permission yang sudah dideklarasikan di `AndroidManifest.xml`.
- Tidak ada API key yang diperlukan.
- Repository ini hanya memuat kode sumber dan README. Laporan, screenshot, dan folder `tambahan` tidak disertakan.

## Penulis

- **Nama:** Naufal Abiyyu Arhab
- **NIM:** 245150201111029
- **Kelas:** TIF-B
- **Mata Kuliah:** Pengembangan Aplikasi Perangkat Bergerak
- **Asisten:** Rafi Ananta Nugraha, Mohamad Robi Alwan
