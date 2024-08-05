package com.yaropaul.write

import android.annotation.SuppressLint
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
import com.yaropaul.mongo.database.ImageToDeleteDao
import com.yaropaul.mongo.database.ImageToUploadDao
import com.yaropaul.mongo.database.entity.ImageToDelete
import com.yaropaul.mongo.database.entity.ImageToUpload
import com.yaropaul.mongo.repository.MongoDB
import com.yaropaul.ui.GalleryImage
import com.yaropaul.ui.GalleryState
import com.yaropaul.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.yaropaul.util.fetchImagesFromFirebase
import com.yaropaul.util.model.Mood
import com.yaropaul.util.model.NoteBook
import com.yaropaul.util.model.RequestState
import com.yaropaul.util.toRealmInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
internal class WriteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {

    val galleryState = GalleryState()
    internal var uiState by mutableStateOf(UiState())
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

    @SuppressLint("NewApi")
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
            deleteImagesFromFirebase(
                images = galleryState.imagesToBeDeleted.map { it.remoteImagePath }
            )
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
                        uiState.selectedNote?.let { deleteImagesFromFirebase(images = it.images) }
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
            imagePath.putFile(galleryImage.image).addOnProgressListener {
                val sessionUri = it.uploadSessionUri
                if (sessionUri != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        imageToUploadDao.addImageToUpload(
                            ImageToUpload(
                                remoteImagePath = galleryImage.remoteImagePath,
                                imageUri = galleryImage.image.toString(),
                                sessionUri = sessionUri.toString()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if (images != null) {
            images.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        } else {
            galleryState.imagesToBeDeleted.map { it.remoteImagePath }.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        }
    }
}


internal data class UiState(
    val selectedNoteId: String? = null,
    val selectedNote: NoteBook? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)

