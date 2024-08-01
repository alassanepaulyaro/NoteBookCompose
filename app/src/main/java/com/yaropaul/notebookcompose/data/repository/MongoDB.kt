package com.yaropaul.notebookcompose.data.repository

import com.yaropaul.notebookcompose.model.NoteBook
import com.yaropaul.notebookcompose.model.RequestState
import com.yaropaul.notebookcompose.utils.Constants.APP_ID
import com.yaropaul.notebookcompose.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object MongoDB : MongoRepository {
    private lateinit var realm: Realm
    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(NoteBook::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<NoteBook>(query = "ownerId == $0", user.id),
                        name = "User's NoteBook"
                    )
                }.log(LogLevel.ALL).build()
            realm = Realm.open(config)
        }
    }

    override fun getAllNoteBooks(): Flow<Notes> {
        return if (user != null) {
            try {
                realm.query<NoteBook>(query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING).asFlow().map { result ->
                        RequestState.Success(data = result.list.groupBy {
                            it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        })
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override fun getSelectedNote(noteId: ObjectId): Flow<RequestState<NoteBook>> {
        return if (user != null) {
            try {
                realm.query<NoteBook>(query = "_id == $0", noteId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun insertNote(noteBook: NoteBook): RequestState<NoteBook> {
        return if (user != null) {
            realm.write {
                try {
                    val addedNote = copyToRealm(noteBook.apply { ownerId = user.id })
                    RequestState.Success(data = addedNote)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateNote(noteBook: NoteBook): RequestState<NoteBook> {
        return if (user != null) {
            realm.write {
                val queryNote = query<NoteBook>(query = "_id == $0", noteBook._id).first().find()
                if (queryNote != null) {
                    queryNote.title = noteBook.title
                    queryNote.description = noteBook.description
                    queryNote.mood = noteBook.mood
                    queryNote.images = noteBook.images
                    queryNote.date = noteBook.date
                    RequestState.Success(data = queryNote)
                } else {
                    RequestState.Error(error = Exception("Queried note book does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteNote(id: ObjectId): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                val note =
                    query<NoteBook>(query = "_id == $0 AND ownerId == $1", id, user.id)
                        .first().find()
                if (note != null) {
                    try {
                        delete(note)
                        RequestState.Success(data = true)
                    } catch (e: Exception) {
                        RequestState.Error(e)
                    }
                } else {
                    RequestState.Error(Exception("Note does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteAllNotes(): RequestState<Boolean> {
        return if (user != null) {
            realm.write {
                val diaries = this.query<NoteBook>("ownerId == $0", user.id).find()
                try {
                    delete(diaries)
                    RequestState.Success(data = true)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override fun getFilteredNotes(zonedDateTime: ZonedDateTime): Flow<Notes> {
        return if (user != null) {
            try {
                realm.query<NoteBook>(
                    "ownerId == $0 AND date < $1 AND date > $2",
                    user.id,
                    RealmInstant.from(
                        LocalDateTime.of(
                            zonedDateTime.toLocalDate().plusDays(1),
                            LocalTime.MIDNIGHT
                        ).toEpochSecond(zonedDateTime.offset), 0
                    ),
                    RealmInstant.from(
                        LocalDateTime.of(
                            zonedDateTime.toLocalDate(),
                            LocalTime.MIDNIGHT
                        ).toEpochSecond(zonedDateTime.offset), 0
                    ),
                ).asFlow().map { result ->
                    RequestState.Success(
                        data = result.list.groupBy {
                            it.date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                    )
                }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        } else {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }
}

private class UserNotAuthenticatedException : Exception("User is not Logged in.")