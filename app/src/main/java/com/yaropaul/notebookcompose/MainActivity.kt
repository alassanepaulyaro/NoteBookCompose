package com.yaropaul.notebookcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.yaropaul.notebookcompose.Navigation.Screen
import com.yaropaul.notebookcompose.Navigation.SetupNavGraph
import com.yaropaul.notebookcompose.ui.theme.NoteBookComposeTheme
import com.yaropaul.notebookcompose.utils.Constants.APP_ID
import io.realm.kotlin.mongodb.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        //transparent status and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContent {
            NoteBookComposeTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController
                )
            }
        }
    }
}

private fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route
    else Screen.Authentication.route
}
