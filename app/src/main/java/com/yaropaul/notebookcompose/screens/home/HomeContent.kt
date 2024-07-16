package com.yaropaul.notebookcompose.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.yaropaul.notebookcompose.components.NoteBookHolder
import com.yaropaul.notebookcompose.model.Mood
import com.yaropaul.notebookcompose.model.NoteBook
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import java.time.LocalDate


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    contentNotes: Map<LocalDate, List<NoteBook>>,
    onClick: (String) -> Unit
) {
    if (contentNotes.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(start = paddingValues.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = paddingValues.calculateEndPadding(LayoutDirection.Ltr))
        ) {
            contentNotes.forEach { (localDate, noteBooks) ->
                stickyHeader(key = localDate) {
                    DateHeader(localDate = localDate)
                }
                items(
                    items = noteBooks,
                    key = { it._id.toString() }
                ) {
                    NoteBookHolder(noteBook = it, onClick = onClick)
                }
            }
        }
    } else {
        EmptyPage()
    }
}

@Composable
fun EmptyPage(
    title: String = "Empty Diary",
    subtitle: String = "Write Something"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = subtitle,
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

@Composable
fun DateHeader(localDate: LocalDate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%02d", localDate.dayOfMonth),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = localDate.dayOfWeek.toString().take(3),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = localDate.month.toString().lowercase().replaceFirstChar { it.titlecase() },
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = "${localDate.year}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyPagePreview() {
    EmptyPage("Empty Note", "Write Something")
}

@Preview(showBackground = true)
@Composable
fun DateHeaderPreview() {
    DateHeader(localDate = LocalDate.now())
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    HomeContent(paddingValues = PaddingValues(all = 2.dp), contentNotes = createNotesMap()) {
    }
}


// Function to create a map of notes
fun createNotesMap(): Map<LocalDate, List<NoteBook>> {
    // Create NoteBook objects
    val noteBook1 = NoteBook().apply {
        ownerId = "owner1"
        mood = Mood.Happy.name
        title = "My First NoteBook"
        description = "This is a description for the first notebook."
        images = realmListOf("image1.png", "image2.png")
        date = RealmInstant.now()
    }

    val noteBook2 = NoteBook().apply {
        ownerId = "owner2"
        mood = Mood.Shameful.name
        title = "My Second NoteBook"
        description = "This is a description for the second notebook."
        images = realmListOf("image3.png", "image4.png")
        date = RealmInstant.now()
    }

    val noteBook3 = NoteBook().apply {
        ownerId = "owner3"
        mood = Mood.Angry.name
        title = "My Third NoteBook"
        description = "This is a description for the third notebook."
        images = realmListOf("image5.png", "image6.png")
        date = RealmInstant.now()
    }

    // Group the NoteBooks by LocalDate
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return mapOf(
        today to listOf(noteBook1, noteBook2),
        yesterday to listOf(noteBook3)
    )
}
