package com.yaropaul.notebookcompose.screens.auth

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import com.yaropaul.notebookcompose.utils.Constants.CLIENT_ID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    authenticated: Boolean,
    loadingState: Boolean,
    onTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    onButtonClicked: () -> Unit,
    onTokenIdReceived: (String) -> Unit,
    onDialogDismissed: (String) -> Unit,
    navigateHome: () -> Unit
) {
    Scaffold(
        content = {
            ContentWithMessageBar(messageBarState = messageBarState) {
                AuthenticationContent(loadingState, onButtonClicked)
            }
        })
    OneTapSignInWithGoogle(
        state = onTapState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { tokenId ->
            onTokenIdReceived(tokenId)
        },
        onDialogDismissed = { message ->
            onDialogDismissed(message)
        })

    LaunchedEffect(key1 = authenticated) {
        if (authenticated) {
            navigateHome()
        }
    }
}