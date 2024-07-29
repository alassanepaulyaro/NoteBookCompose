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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.ZoneId

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

    override fun getSelectedNote(noteId: ObjectId): RequestState<NoteBook> {
        return if (user != null) {
            try {
                val note = realm.query<NoteBook>(query = "_id == $0", noteId).find().first()
                RequestState.Success(data = note)
            } catch (e: Exception) {
                RequestState.Error(e)
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }
}

private class UserNotAuthenticatedException : Exception("User is not Logged in.")