package com.kaunghtetmon.contactdatabase.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaunghtetmon.contactdatabase.ui.screens.AddContactScreen
import com.kaunghtetmon.contactdatabase.ui.screens.ContactListScreen
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModel
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModelFactory

@Composable
fun AppNavigation(application: Application) {
    val navController = rememberNavController()
    val contactViewModel: ContactViewModel = viewModel(
        factory = ContactViewModelFactory(application)
    )

    NavHost(navController = navController, startDestination = "add_contact") {
        composable("add_contact") {
            AddContactScreen(
                contactViewModel = contactViewModel,
                onNavigateToContactList = {
                    navController.navigate("contact_list")
                }
            )
        }
        composable("contact_list") {
            ContactListScreen(
                contactViewModel = contactViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
