package com.aliduman.app_instagram.main

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliduman.app_instagram.DestinationScreen
import com.aliduman.app_instagram.IgViewModel
import com.aliduman.app_instagram.R
import com.aliduman.app_instagram.data.PostData


data class PostRow(
    var post1: PostData? = null,
    var post2: PostData? = null,
    var post3: PostData? = null
) {
    fun isFull() = post1 != null && post2 != null && post3 != null
    fun add(post: PostData) {
        if (post1 == null) {
            post1 = post
        } else if (post2 == null) {
            post2 = post
        } else if (post3 == null) {
            post3 = post
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyPostScreen(navController: NavController, vm: IgViewModel) {
    val newPostImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val encoded = Uri.encode(it.toString())
                val route = DestinationScreen.NewPost.createRoute(encoded)
                navController.navigate(route)
            }
        }

    val userData = vm.userData.value
    val isLoading = vm.inProgress.value
    val postLoading = vm.refreshPostsProgress.value
    val posts = vm.posts.value

    val followers = vm.followers.value

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row {
                    ProfileImage(imageUrl = userData?.imageUrl) {
                        newPostImageLauncher.launch("image/*")
                    }
                    Text(
                        text = "${posts.size}\n posts",
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${vm.followers.value.size}\nfollowers",
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${userData?.following?.size ?: 0}\nfollowing",
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    val usernameDisplay =
                        if (userData?.username == null) "" else "@${userData?.username}"
                    Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                    Text(text = usernameDisplay)
                    Text(text = userData?.bio ?: "")
                }
                OutlinedButton(
                    onClick = {
                        navigateTo(navController, DestinationScreen.Profile)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    ),
                    shape = RoundedCornerShape(10)
                ) {
                    Text(text = "Edit Profile", color = Color.Black)
                }
                PostList(
                    isContextLoading = isLoading,
                    postsLoading = postLoading,
                    posts = posts,
                    modifier = Modifier
                        .weight(1f)
                        .padding(1.dp)
                        .fillMaxSize()
                ) { post ->
                    navigateTo(navController, DestinationScreen.SinglePost, NavParam("post", post))
                }
            }
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.POST,
                navController = navController
            )
        }

        if (isLoading) {
            CommonProgressSpinner()
        }
    }

}

@Composable
fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(top = 16.dp)
        .clickable { onClick.invoke() }
    ) {
        UserImageCard(
            userImage = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )
        Card(
            shape = CircleShape,
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.background(Color.Blue),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    onPostClick: (PostData) -> Unit
) {
    if (postsLoading) {
        CommonProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading) {
                Text(text = "No posts available")
            }
        }
    } else {
        LazyColumn(modifier = modifier) {
            val rows = arrayListOf<PostRow>()
            var currentRow = PostRow()
            rows.add(currentRow)
            for (post in posts) {
                if (currentRow.isFull()) {
                    currentRow = PostRow()
                    rows.add(currentRow)
                }
                currentRow.add(post)
            }

            items(items = rows) { row ->
                PostsRow(item = row, onPostClick = onPostClick)
            }

        }
    }

}

@Composable
fun PostsRow(item: PostRow, onPostClick: (PostData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        PostImage(
            imageUrl = item.post1?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    item.post1?.let { post -> onPostClick(post) }
                }
        )
        PostImage(
            imageUrl = item.post2?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    item.post2?.let { post -> onPostClick(post) }
                }
        )
        PostImage(
            imageUrl = item.post3?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    item.post3?.let { post -> onPostClick(post) }
                }
        )

    }

}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()
        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) { }
        }
        CommonImage(
            data = imageUrl,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )

    }
}