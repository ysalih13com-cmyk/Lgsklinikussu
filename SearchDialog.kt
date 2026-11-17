package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.ui.navigation.Screen

data class SearchResultItem(
    val type: String, // "Ünite", "Kart", "Soru", "Yanlış"
    val title: String,
    val subtitle: String,
    val screenTarget: Screen
)

@Composable
fun SearchDialog(
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val results = remember(query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) emptyList()
        else {
            val list = mutableListOf<SearchResultItem>()
            // Search Units
            LgsCurriculum.courses.forEach { course ->
                course.units.forEach { unit ->
                    if (unit.lowercase().contains(q) || course.name.lowercase().contains(q)) {
                        list.add(SearchResultItem("Ünite", "${course.icon} ${course.name} · $unit", "Ünite sayfasına git", Screen.UnitDetail(course.id, unit)))
                    }
                }
            }
            // Search Flashcards
            LgsCurriculum.flashcards.forEach { card ->
                if (card.front.lowercase().contains(q) || card.back.lowercase().contains(q)) {
                    list.add(SearchResultItem("Kart", card.front, card.back, Screen.UnitDetail(card.courseId, card.unit, "kart")))
                }
            }
            // Search Questions
            LgsCurriculum.quizQuestions.forEach { qItem ->
                if (qItem.question.lowercase().contains(q)) {
                    list.add(SearchResultItem("Soru", qItem.question, qItem.explanation, Screen.UnitDetail(qItem.courseId, qItem.unit, "test")))
                }
            }
            list.take(30)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔍 LGS Klinik Arama")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Konu, soru, kavram veya formül ara...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (results.isEmpty() && query.isNotBlank()) {
                    Text("Sonuç bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.height(250.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(results) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onNavigate(item.screenTarget)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                item.type,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                    }
                                    Text(item.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
