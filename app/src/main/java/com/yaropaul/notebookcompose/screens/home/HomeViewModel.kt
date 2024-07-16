package com.yaropaul.notebookcompose.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaropaul.notebookcompose.data.repository.MongoDB
import com.yaropaul.notebookcompose.data.repository.Notes
import com.yaropaul.notebookcompose.model.RequestState
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var notes: MutableState<Notes> = mutableStateOf(RequestState.Idle)

    init {
        observeAllNoteBook()
    }

    private fun observeAllNoteBook() {
        viewModelScope.launch {
            MongoDB.getAllNoteBooks().collect { result ->
                notes.value = result
            }
        }
    }
}