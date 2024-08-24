package com.aliduman.app_instagram.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.aliduman.app_instagram.IgViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FeedScreen(navController: NavController, vm: IgViewModel) {
    Scaffold {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "FeedScreen")
            }

            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.FEED,
                navController = navController
            )
        }
    }
}