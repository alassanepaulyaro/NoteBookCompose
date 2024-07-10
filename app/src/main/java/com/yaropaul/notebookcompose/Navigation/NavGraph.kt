package com.yaropaul.notebookcompose.Navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import com.yaropaul.notebookcompose.screens.auth.AuthenticationScreen
import com.yaropaul.notebookcompose.screens.auth.AuthenticationViewModel
import com.yaropaul.notebookcompose.screens.home.HomeScreen
import com.yaropaul.notebookcompose.utils.Constants.APP_ID
import com.yaropaul.notebookcompose.utils.Constants.WRITE_SCREEN_ARGUMENT_KEY
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SetupNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            }
        )
        HomeRoute(
            navigateToWrite = {navController.navigate(Screen.Write.route)}
        )
        WriteRoute()
    }
}

fun NavGraphBuilder.authenticationRoute(navigateToHome: () -> Unit) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState
        val onTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = loadingState,
            onTapState = onTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                onTapState.open()
                viewModel.setLoading(true)
            },
            onTokenIdReceived = { tokenId ->
                viewModel.signInWithMongoAtlas(tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated !")
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(Exception(it))
                        viewModel.setLoading(false)
                    }
                )
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.HomeRoute(navigateToWrite: () -> Unit) {
    composable(route = Screen.Home.route) {
        HomeScreen(onMenuClicked = { },
            navigateToWrite
        )
    }
}

fun NavGraphBuilder.WriteRoute() {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY)
        {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {

    }
}