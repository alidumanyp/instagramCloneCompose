package com.aliduman.app_instagram.main

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliduman.app_instagram.IgViewModel
import com.aliduman.app_instagram.R
import com.aliduman.app_instagram.data.PostData

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SinglePostScreen(navController: NavController, vm: IgViewModel, post: PostData) {

    Scaffold { padding ->
        post.userId?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(padding)
            ) {
                Text(text = "Back", modifier = Modifier.clickable { navController.popBackStack() })
                CommonDivider()
                SinglePostDisplay(navController = navController, vm = vm, post = post)
            }
        }
    }

}

@Composable
fun SinglePostDisplay(navController: NavController, vm: IgViewModel, post: PostData) {
    val userData = vm.userData.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Card(
                shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            ) {
                CommonImage(data = post.userImage, modifier = Modifier.wrapContentHeight())
            }
            Text(text = post.username ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp))

            if (userData?.userId == post.userId) {
                // current user's post. don't show anything
            } else if (userData?.following?.contains(post.userId) == true) {
                Text(
                    text = "Following",
                    color = Color.Gray,
                    modifier = Modifier.clickable {
                        vm.onFollowClick(post.userId!!)
                    }
                )
            } else {
                Text(
                    text = "Follow",
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        vm.onFollowClick(post.userId!!)
                    }
                )
            }
        }
    }
    Box {
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
        CommonImage(
            data = post.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_like),
            contentDescription = "likes",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Red)
        )
        Text(text = " ${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 0.dp))
    }

    Row(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
    }

    Row(modifier = Modifier.padding(8.dp)) {
        Text(text = "10 comments", color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
    }

}