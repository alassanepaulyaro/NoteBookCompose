package com.yaropaul.mongo.repository

import com.yaropaul.util.model.NoteBook
import com.yaropaul.util.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.BsonObjectId
import java.time.LocalDate
import java.time.ZonedDateTime

typealias Notes = RequestState<Map<LocalDate, List<NoteBook>>>

internal interface MongoRepository {
    fun configureTheRealm()
    fun getAllNoteBooks(): Flow<Notes>
    fun getSelectedNote(noteId: BsonObjectId): Flow<RequestState<NoteBook>>
    suspend fun insertNote(noteBook : NoteBook): RequestState<NoteBook>
    suspend fun updateNote(noteBook : NoteBook): RequestState<NoteBook>
    suspend fun deleteNote(id : BsonObjectId): RequestState<Boolean>
    suspend fun deleteAllNotes(): RequestState<Boolean>
    fun getFilteredNotes(zonedDateTime: ZonedDateTime): Flow<Notes>
}