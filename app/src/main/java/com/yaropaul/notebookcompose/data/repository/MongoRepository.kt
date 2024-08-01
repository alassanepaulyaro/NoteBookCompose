package com.yaropaul.notebookcompose.data.repository

import com.yaropaul.notebookcompose.model.NoteBook
import com.yaropaul.notebookcompose.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.ZonedDateTime

typealias Notes = RequestState<Map<LocalDate, List<NoteBook>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllNoteBooks(): Flow<Notes>
    fun getSelectedNote(noteId: ObjectId): Flow<RequestState<NoteBook>>
    suspend fun insertNote(noteBook : NoteBook): RequestState<NoteBook>
    suspend fun updateNote(noteBook : NoteBook): RequestState<NoteBook>
    suspend fun deleteNote(id : ObjectId): RequestState<Boolean>
    suspend fun deleteAllNotes(): RequestState<Boolean>
    fun getFilteredNotes(zonedDateTime: ZonedDateTime): Flow<Notes>
}