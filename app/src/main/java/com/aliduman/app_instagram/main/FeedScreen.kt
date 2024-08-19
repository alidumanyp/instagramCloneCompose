package com.aliduman.app_instagram.main

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.aliduman.app_instagram.IgViewModel

@Composable
fun FeedScreen(navController: NavController, vm: IgViewModel) {
    Text(text = "Feed")

}