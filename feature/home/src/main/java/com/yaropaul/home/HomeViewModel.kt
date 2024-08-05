package com.yaropaul.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.yaropaul.mongo.database.ImageToDeleteDao
import com.yaropaul.mongo.database.entity.ImageToDelete
import com.yaropaul.mongo.repository.MongoDB
import com.yaropaul.mongo.repository.Notes
import com.yaropaul.util.connectivity.ConnectivityObserver
import com.yaropaul.util.connectivity.NetworkConnectivityObserver
import com.yaropaul.util.model.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {

    private lateinit var allNotesJob: Job
    private lateinit var filteredNotesJob: Job
    var notes: MutableState<Notes> = mutableStateOf(RequestState.Idle)
    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)
    var dateIsSelected by mutableStateOf(false)
        private set

    init {
        getNotes()
        viewModelScope.launch {
            connectivity.observe().collect { network = it }
        }
    }

    fun getNotes(zonedDateTime: ZonedDateTime? = null) {
        dateIsSelected = zonedDateTime != null
        notes.value = RequestState.Loading
        if (dateIsSelected && zonedDateTime != null) {
            observeFilteredNotes(zonedDateTime = zonedDateTime)
        } else {
            observeAllNoteBook()
        }
    }

    private fun observeAllNoteBook() {
        allNotesJob = viewModelScope.launch {
            if (::filteredNotesJob.isInitialized) {
                filteredNotesJob.cancelAndJoin()
            }
            MongoDB.getAllNoteBooks().collect { result ->
                notes.value = result
            }
        }
    }

    private fun observeFilteredNotes(zonedDateTime: ZonedDateTime) {
        filteredNotesJob = viewModelScope.launch {
            if (::allNotesJob.isInitialized) {
                allNotesJob.cancelAndJoin()
            }
            MongoDB.getFilteredNotes(zonedDateTime = zonedDateTime).collect { result ->
                notes.value = result
            }
        }
    }

    fun deleteAllNotes(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (network == ConnectivityObserver.Status.Available) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imagesDirectory = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imagesDirectory)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val imagePath = "images/${userId}/${ref.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imageToDeleteDao.addImageToDelete(
                                        ImageToDelete(
                                            remoteImagePath = imagePath
                                        )
                                    )
                                }
                            }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = MongoDB.deleteAllNotes()
                        if (result is RequestState.Success) {
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } else if (result is RequestState.Error) {
                            withContext(Dispatchers.Main) {
                                onError(result.error)
                            }
                        }
                    }
                }
                .addOnFailureListener { onError(it) }
        } else {
            onError(Exception("No Internet Connection."))
        }
    }
}