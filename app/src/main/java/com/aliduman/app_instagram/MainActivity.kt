package com.aliduman.app_instagram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliduman.app_instagram.auth.LoginScreen
import com.aliduman.app_instagram.auth.ProfileScreen
import com.aliduman.app_instagram.auth.SingupScreen
import com.aliduman.app_instagram.data.PostData
import com.aliduman.app_instagram.main.FeedScreen
import com.aliduman.app_instagram.main.MyPostScreen
import com.aliduman.app_instagram.main.NewPostScreen
import com.aliduman.app_instagram.main.NotificationMessage
import com.aliduman.app_instagram.main.SearchScreen
import com.aliduman.app_instagram.main.SinglePostScreen
import com.aliduman.app_instagram.ui.theme.App_instagramTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App_instagramTheme {
                InstagramApp()
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object Singup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Feed : DestinationScreen("feed")
    object Search : DestinationScreen("search")
    object MyPost : DestinationScreen("mypost")
    object Profile : DestinationScreen("profile")
    object NewPost : DestinationScreen("newpost/{imageUri}") {
        fun createRoute(uri: String) = "newpost/$uri"
    }

    object SinglePost : DestinationScreen("singlepost")
}

@Composable
fun InstagramApp() {
    val vm = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.Singup.route) {
        composable(DestinationScreen.Singup.route) {
            SingupScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Feed.route) {
            FeedScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Search.route) {
            SearchScreen(vm = vm, navController = navController)
        }
        composable(DestinationScreen.MyPost.route) {
            MyPostScreen(vm = vm, navController = navController)
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(vm = vm, navController = navController)
        }
        composable(DestinationScreen.NewPost.route) {
            val imageUri = it.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(navController = navController, vm = vm, encodedUri = it)
            }
        }
        composable(DestinationScreen.SinglePost.route) {
            val postData =
                navController.previousBackStackEntry?.arguments?.getParcelable<PostData>("post")
            postData?.let {
                SinglePostScreen(navController = navController, vm = vm, post = postData)
            }
        }


    }
}