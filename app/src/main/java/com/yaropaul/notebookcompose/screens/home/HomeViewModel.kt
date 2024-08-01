package com.yaropaul.notebookcompose.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.yaropaul.notebookcompose.connectivity.ConnectivityObserver
import com.yaropaul.notebookcompose.connectivity.NetworkConnectivityObserver
import com.yaropaul.notebookcompose.data.database.ImageToDeleteDao
import com.yaropaul.notebookcompose.data.database.entity.ImageToDelete
import com.yaropaul.notebookcompose.data.repository.MongoDB
import com.yaropaul.notebookcompose.data.repository.Notes
import com.yaropaul.notebookcompose.model.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {
    var notes: MutableState<Notes> = mutableStateOf(RequestState.Idle)
    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)

    init {
        observeAllNoteBook()
        viewModelScope.launch {
            connectivity.observe().collect { network = it }
        }
    }

    private fun observeAllNoteBook() {
        viewModelScope.launch {
            MongoDB.getAllNoteBooks().collect { result ->
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