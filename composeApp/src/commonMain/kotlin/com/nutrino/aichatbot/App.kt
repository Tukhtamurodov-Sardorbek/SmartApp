package com.nutrino.aichatbot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.nutrino.aichatbot.di.modules.appModules
import com.nutrino.aichatbot.presentation.screens.AskQuestionScreen
import com.nutrino.aichatbot.presentation.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/**
 * Root composable for the shared application UI.
 *
 * The function applies the custom AppTheme, starts Koin dependency injection, and renders the main
 * Ask Question screen for every supported platform.
 */
@Composable
@Preview
fun App() {
    AppTheme {
        KoinApplication(
            configuration = koinConfiguration(declaration = { modules(appModules) }),
            content = {
                AskQuestionScreen()
            })
    }
}
