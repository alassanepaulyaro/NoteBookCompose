package com.yaropaul.notebookcompose.screens.write

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.yaropaul.notebookcompose.data.repository.MongoDB
import com.yaropaul.notebookcompose.model.GalleryImage
import com.yaropaul.notebookcompose.model.GalleryState
import com.yaropaul.notebookcompose.model.Mood
import com.yaropaul.notebookcompose.model.NoteBook
import com.yaropaul.notebookcompose.model.RequestState
import com.yaropaul.notebookcompose.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.yaropaul.notebookcompose.utils.fetchImagesFromFirebase
import com.yaropaul.notebookcompose.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val galleryState = GalleryState()
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
            viewModelScope.launch() {
                viewModelScope.launch(Dispatchers.Main) {
                    MongoDB.getSelectedNote(
                        noteId = BsonObjectId.invoke(bsonObjectIdToString(uiState.selectedNoteId!!))
                    ).catch {
                        emit(RequestState.Error(Exception("Note is already deleted.")))
                    }.collect { note ->
                        if (note is RequestState.Success) {
                            setSelectedNote(noteBook = note.data)
                            setTitle(title = note.data.title)
                            setDescription(description = note.data.description)
                            setMood(mood = Mood.valueOf(note.data.mood))

                            fetchImagesFromFirebase(
                                remoteImagePaths = note.data.images,
                                onImageDownload = { downloadedImage ->
                                    galleryState.addImage(
                                        GalleryImage(
                                            image = downloadedImage,
                                            remoteImagePath = extractImagePath(
                                                fullImageUrl = downloadedImage.toString()
                                            ),
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
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
                uploadImagesToFirebase()
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
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    fun deleteNote(
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedNoteId != null) {
                val result =
                    MongoDB.deleteNote(id = ObjectId.invoke(bsonObjectIdToString(uiState.selectedNoteId!!)))
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
    }

    fun addImage(image: Uri, imageType: String) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )
    }

    private fun uploadImagesToFirebase() {
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
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

