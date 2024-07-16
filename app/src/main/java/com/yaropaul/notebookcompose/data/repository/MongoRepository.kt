package com.yaropaul.notebookcompose.data.repository

import com.yaropaul.notebookcompose.model.NoteBook
import com.yaropaul.notebookcompose.model.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Notes = RequestState<Map<LocalDate, List<NoteBook>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllNoteBooks(): Flow<Notes>
}