package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.home.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenDetail: (Int) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("NavigationLab", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Data pengguna dari DummyJSON",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                label = { Text("Cari profil") },
                placeholder = { Text("Contoh: John") },
                singleLine = true
            )
            OutlinedButton(onClick = onRefresh) {
                Text("Refresh")
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gagal memuat data",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }

            uiState.students.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isBlank()) {
                            "Belum ada data pengguna"
                        } else {
                            "Tidak ada hasil untuk \"$searchQuery\""
                        }
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.students, key = { it.id }) { student ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = student.email,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = student.university,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedButton(
                                onClick = { onOpenDetail(student.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Lihat Detail")
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOpenProfile,
                modifier = Modifier.weight(1f)
            ) {
                Text("Profile")
            }
            OutlinedButton(
                onClick = onOpenAbout,
                modifier = Modifier.weight(1f)
            ) {
                Text("About")
            }
        }
    }
}
