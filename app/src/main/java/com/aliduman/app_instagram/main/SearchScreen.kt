package com.aliduman.app_instagram.main

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliduman.app_instagram.DestinationScreen
import com.aliduman.app_instagram.IgViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchScreen(vm: IgViewModel, navController: NavController) {

    val searchInProgress = vm.searchInProgress.value
    val searchedPosts = vm.searchedPosts.value
    var searchTerm by rememberSaveable {
        mutableStateOf("")
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                searchTerm = searchTerm,
                onSearchTermChanged = { term -> searchTerm = term },
                onSearch = {
                    vm.searchPosts(searchTerm)
                }
            )

            PostList(
                isContextLoading = false,
                postsLoading = searchInProgress,
                posts = searchedPosts,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) { post ->
                navigateTo(navController, DestinationScreen.SinglePost, NavParam("post", post))

            }

            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.SEARCH,
                navController = navController
            )
        }
    }

}

@Composable
fun SearchBar(searchTerm: String, onSearchTermChanged: (String) -> Unit, onSearch: () -> Unit) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = searchTerm,
        onValueChange = onSearchTermChanged,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, CircleShape),
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            focusedLeadingIconColor = Color.Black,
            unfocusedLeadingIconColor = Color.Black,
            disabledLeadingIconColor = Color.Black,
            focusedTrailingIconColor = Color.Black,
            unfocusedTrailingIconColor = Color.Black,
            disabledTrailingIconColor = Color.Black,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray,
            disabledPlaceholderColor = Color.Gray,
            errorIndicatorColor = Color.Transparent,
            errorLeadingIconColor = Color.Transparent,
            errorTrailingIconColor = Color.Transparent,
            errorCursorColor = Color.Transparent,
            errorPlaceholderColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        label = { Text(text = "Search") },
        trailingIcon = {
            IconButton(
                onClick = {
                    onSearch()
                    focusManager.clearFocus()
                }
            ) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon")
            }
        }

    )
}