package com.yaropaul.write.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yaropaul.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.yaropaul.util.Screen
import com.yaropaul.util.model.Mood
import com.yaropaul.write.WriteScreen
import com.yaropaul.write.WriteViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY)
        {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val context = LocalContext.current
        val viewModel: WriteViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val galleryState = viewModel.galleryState
        val pagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0f) {
            Mood.entries.size
        }
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }

        LaunchedEffect(key1 = uiState) {
            android.util.Log.e("SelectedNote", "${uiState.selectedNoteId}")
        }

        WriteScreen(
            uiState = uiState,
            moodName = { Mood.entries[pageNumber].name },
            pagerState = pagerState,
            galleryState = galleryState,
            onTitleChanged = { viewModel.setTitle(title = it) },
            onDescriptionChanged = { viewModel.setDescription(description = it) },
            onDeleteConfirmed = {
                viewModel.deleteNote(
                    onSuccess = {
                        android.widget.Toast.makeText(
                            context,
                            "Deleted",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    },
                    onError = { message ->
                        android.widget.Toast.makeText(
                            context,
                            message,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    })
            },
            onDateTimeUpdated = { viewModel.updateDateTime(zonedDateTime = it) },
            onBackPressed = onBackPressed,
            onSaveClicked = {
                viewModel.upsertNoteBook(
                    noteBook = it.apply { mood = Mood.entries[pageNumber].name },
                    onSuccess = { onBackPressed() },
                    onError = { message ->
                        android.widget.Toast.makeText(
                            context,
                            message,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onImageSelect = {
                val type = context.contentResolver.getType(it)?.split("/")?.last() ?: "jpg"
                viewModel.addImage(
                    image = it,
                    imageType = type
                )
            },
            onImageDeleteClicked = {
                galleryState.removeImage(it)
            }
        )
    }
}