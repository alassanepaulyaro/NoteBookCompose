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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.BsonObjectId

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
                    )

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

    fun setSelectedNote(noteBook: NoteBook) {
        uiState = uiState.copy(selectedNote = noteBook)
    }
}

data class UiState(
    val selectedNoteId: String? = null,
    val selectedNote: NoteBook? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral
)

