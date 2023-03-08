package com.marine.fishtank.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marine.fishtank.viewmodel.SignInViewModel
import com.orhanobut.logger.Logger

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit
) {
    Logger.d("Composing SignInScreen")

    var userIdText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val userId by viewModel.userId.collectAsStateWithLifecycle(initialValue = "")
    val password by viewModel.password.collectAsStateWithLifecycle(initialValue = "")
    val signInResult by viewModel.signInResultFlow.collectAsStateWithLifecycle()

    userIdText = userId ?: ""
    passwordText = password ?: ""

    if (signInResult.result) {
        onSignInSuccess()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                //.background(colorResource(id = R.color.loyal_blue))
                .padding(15.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                value = userIdText,
                maxLines = 1,
                label = { Text(text = "id") },
                placeholder = { Text("id") },
                onValueChange = { userIdText = it },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = passwordText,
                maxLines = 1,
                onValueChange = { passwordText = it },
                placeholder = { Text("Password") },
                label = { Text(text = "password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                onClick = {
                    viewModel.signIn(userIdText, passwordText)
                },
            ) {
                Text(text = "SIGN IN")
            }
        }
    }
}

@Preview
@Composable
fun PreviewSignInScreen() {
    SignInScreen(onSignInSuccess = {})
}