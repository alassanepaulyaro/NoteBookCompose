package com.yaropaul.notebookcompose.screens.write

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaropaul.notebookcompose.data.repository.MongoDB
import com.yaropaul.notebookcompose.model.Mood
import com.yaropaul.notebookcompose.model.NoteBook
import com.yaropaul.notebookcompose.model.RequestState
import com.yaropaul.notebookcompose.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.yaropaul.notebookcompose.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    init {
        getNoteIdArgument()
        fetchSelectedNote()
    }

    private fun getNoteIdArgument() {
        uiState = uiState.copy(
            selectedNoteId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedNote() {
        if (uiState.selectedNoteId != null) {
            Log.e("selectedNoteId", "selectedNoteId  :  " + uiState.selectedNoteId)
            viewModelScope.launch() {
                viewModelScope.launch(Dispatchers.Main) {
                    val note = MongoDB.getSelectedNote(
                        noteId = BsonObjectId.invoke(bsonObjectIdToString(uiState.selectedNoteId!!))
                    ).collect { note ->
                        if (note is RequestState.Success) {
                            setSelectedNote(noteBook = note.data)
                            setTitle(title = note.data.title)
                            setDescription(description = note.data.description)
                            setMood(mood = Mood.valueOf(note.data.mood))
                        }
                    }
                }
            }
        }
    }

    private fun bsonObjectIdToString(objectId: String): String {
        return objectId.removePrefix("BsonObjectId(").removeSuffix(")")
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    private fun setSelectedNote(noteBook: NoteBook) {
        uiState = uiState.copy(selectedNote = noteBook)
    }

    fun updateDateTime(zonedDateTime: ZonedDateTime) {
        uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())
    }

    fun upsertNoteBook(
        noteBook: NoteBook,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedNoteId != null) {
                updateNoteBook(noteBook = noteBook, onSuccess = onSuccess, onError = onError)
            } else {
                insertNoteBook(noteBook = noteBook, onSuccess = onSuccess, onError = onError)
            }
        }
    }

    private suspend fun insertNoteBook(
        noteBook: NoteBook,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = MongoDB.insertNote(noteBook = noteBook.apply {
                if (uiState.updatedDateTime != null) {
                    date = uiState.updatedDateTime!!
                }
            })
            if (result is RequestState.Success) {
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else if (result is RequestState.Error) {
                withContext(Dispatchers.Main) {
                    onError(result.error.message.toString())
                }
            }
        }
    }

    private suspend fun updateNoteBook(
        noteBook: NoteBook,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDB.updateNote(noteBook = noteBook.apply {
            _id = ObjectId.invoke(bsonObjectIdToString(uiState.selectedNoteId!!))
            date = if (uiState.updatedDateTime != null) {
                uiState.updatedDateTime!!
            } else {
                uiState.selectedNote!!.date
            }
        })
        if (result is RequestState.Success) {
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }
}

data class UiState(
    val selectedNoteId: String? = null,
    val selectedNote: NoteBook? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)

