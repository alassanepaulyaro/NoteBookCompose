package com.yaropaul.write

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.yaropaul.ui.components.DisplayAlertDialog
import com.yaropaul.util.model.Mood
import com.yaropaul.util.model.NoteBook
import com.yaropaul.util.toInstant
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WriteTopBar(
    selectedNote: NoteBook?,
    moodName: () -> String,
    onDateTimeUpdated: (ZonedDateTime) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val dateDialog = rememberSheetState()
    val timeDialog = rememberSheetState()
    var dateTimeUpdated by remember { mutableStateOf(false) }
    val formatedDate = remember(key1 = currentDate) {
        DateTimeFormatter
            .ofPattern("dd MMM yyy")
            .format(currentDate).uppercase()
    }
    val formatedTime = remember(key1 = currentTime) {
        DateTimeFormatter
            .ofPattern("hh:mm a")
            .format(currentTime).uppercase()
    }

    val selectedNoteDateTime = remember(selectedNote) {
        if (selectedNote != null) {
            SimpleDateFormat("dd MMM yyy, hh:mm a", Locale.getDefault())
                .format(Date.from(selectedNote.date.toInstant())).uppercase()
        } else {
            "Unknown"
        }
    }

    CenterAlignedTopAppBar(
        title = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = moodName(),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (selectedNote != null && dateTimeUpdated) "$formatedDate, $formatedTime"
                    else if (selectedNote != null) selectedNoteDateTime
                    else "$formatedDate, $formatedTime",
                    style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                    textAlign = TextAlign.Center
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back Arrow Icon"
                )
            }
        },
        actions = {
            if (dateTimeUpdated) {
                IconButton(onClick = {
                    currentDate = LocalDate.now()
                    currentTime = LocalTime.now()
                    dateTimeUpdated = false
                    onDateTimeUpdated(
                        ZonedDateTime.of(
                            currentDate,
                            currentTime,
                            ZoneId.systemDefault()
                        )
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                IconButton(onClick = {
                    dateDialog.show()
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (selectedNote != null) {
                DeleteNoteAction(
                    selectedNote = selectedNote,
                    onDeleteConfirmed = onDeleteConfirmed
                )
            }
        }
    )

    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date { localDate ->
            currentDate = localDate
            timeDialog.show()
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )

    ClockDialog(
        state = timeDialog,
        selection = ClockSelection.HoursMinutes { hours, minutes ->
            currentTime = LocalTime.of(hours, minutes)
            dateTimeUpdated = true
            onDateTimeUpdated(
                ZonedDateTime.of(
                    currentDate,
                    currentTime,
                    ZoneId.systemDefault()
                )
            )
        })
}

@Composable
internal fun DeleteNoteAction(
    selectedNote: NoteBook?,
    onDeleteConfirmed: () -> Unit
) {
    var expended by remember { mutableStateOf(false) }
    var openDialog by remember { mutableStateOf(false) }
    DropdownMenu(expanded = expended, onDismissRequest = { expended = false }) {
        DropdownMenuItem(
            text = {
                Text(text = "delete")
            },
            onClick = {
                openDialog = true
                expended = false
            }
        )
    }
    DisplayAlertDialog(
        title = "Delete",
        message = "Are you sure you want to permanently delete this note '${selectedNote?.title}'?",
        dialogOpened = openDialog,
        onDialogClosed = {
            openDialog = false
        },
        onYesClicked = onDeleteConfirmed
    )
    IconButton(onClick = { expended = !expended }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Overflow Menu Icon",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteNoteActionPreview() {
    val noteBook3 = NoteBook().apply {
        ownerId = "owner3"
        mood = Mood.Angry.name
        title = "My Third NoteBook"
        description = "This is a description for the third notebook."
        images = realmListOf("image5.png", "image6.png")
        date = RealmInstant.now()
    }
    DeleteNoteAction(noteBook3) {}
}
