package com.aliduman.app_instagram.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aliduman.app_instagram.DestinationScreen
import com.aliduman.app_instagram.IgViewModel
import com.aliduman.app_instagram.R
import com.aliduman.app_instagram.main.CommonProgressSpinner
import com.aliduman.app_instagram.main.navigateTo

@Composable
fun LoginScreen(
    navController: NavController,
    vm: IgViewModel
) {
    val focus = LocalFocusManager.current


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val emailState = remember { mutableStateOf("") }
            val passState = remember { mutableStateOf("") }

            Image(
                painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = "instagram logo",
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(text = "Login", modifier = Modifier.padding(8.dp), fontSize = 30.sp, fontFamily = FontFamily.Serif)
            OutlinedTextField(value = emailState.value, onValueChange = { emailState.value = it}, label = {Text(text = "Email")})
            OutlinedTextField(value = passState.value, onValueChange = { passState.value = it}, label = {Text(text = "Password")}, visualTransformation = PasswordVisualTransformation())
            Button(
                onClick = {
                    focus.clearFocus(force = true)
                    vm.onLogin(emailState.value, passState.value)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "LOGIN")
            }

            Text(
                text = "New here? Go to signup ->",
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.Singup)
                    }
            )

        }
        val isLoading = vm.inProgress.value
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}