package com.kongko.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kongko.app.core.datastore.SessionDataStore
import com.kongko.app.core.designsystem.theme.KongkoTheme
import com.kongko.app.features.auth.AuthViewModel
import com.kongko.app.features.auth.LoginScreen
import com.kongko.app.features.auth.OtpScreen
import com.kongko.app.features.chat.ChatListScreen
import com.kongko.app.features.chat.ChatListViewModel
import com.kongko.app.features.chat.ChatRoomScreen
import com.kongko.app.features.chat.ChatRoomViewModel
import com.kongko.app.features.contacts.ContactsScreen
import com.kongko.app.features.contacts.ContactsViewModel
import com.kongko.app.features.profile.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionDataStore: SessionDataStore

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAppPermissions()

        setContent {
            KongkoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KongkoAppNavigation(sessionDataStore)
                }
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungranted.isNotEmpty()) {
            requestPermissionsLauncher.launch(ungranted.toTypedArray())
        }
    }
}

@Composable
fun KongkoAppNavigation(sessionDataStore: SessionDataStore) {
    val navController = rememberNavController()
    val isLoggedIn by sessionDataStore.isLoggedInFlow.collectAsState(initial = false)
    val startDestination = if (isLoggedIn) "chats" else "login"

    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onCodeSent = { phone ->
                    navController.navigate("otp/$phone")
                }
            )
        }

        composable(
            route = "otp/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OtpScreen(
                phoneNumber = phone,
                viewModel = authViewModel,
                onSuccess = {
                    navController.navigate("chats") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("chats") {
            val chatListViewModel: ChatListViewModel = hiltViewModel()
            ChatListScreen(
                viewModel = chatListViewModel,
                onChatClicked = { chatId, title ->
                    navController.navigate("chat/$chatId/$title")
                },
                onNewChatClicked = {
                    navController.navigate("contacts")
                },
                onProfileClicked = {
                    navController.navigate("profile")
                }
            )
        }

        composable(
            route = "chat/{chatId}/{title}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) {
            val chatRoomViewModel: ChatRoomViewModel = hiltViewModel()
            ChatRoomScreen(
                viewModel = chatRoomViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("contacts") {
            val contactsViewModel: ContactsViewModel = hiltViewModel()
            ContactsScreen(
                viewModel = contactsViewModel,
                onBack = { navController.popBackStack() },
                onContactSelected = { chatId ->
                    navController.navigate("chat/$chatId/Obrolan") {
                        popUpTo("contacts") { inclusive = true }
                    }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("chats") { inclusive = true }
                    }
                }
            )
        }
    }
}
