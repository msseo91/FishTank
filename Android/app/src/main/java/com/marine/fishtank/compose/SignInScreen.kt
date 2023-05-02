package com.marine.fishtank.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.marine.fishtank.viewmodel.SignInResult
import com.marine.fishtank.viewmodel.SignInViewModel
import com.orhanobut.logger.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit
) {
    Logger.d("Composing SignInScreen")

    val snackbarHostState = remember { SnackbarHostState() }
    var userIdText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val userId by viewModel.userId.collectAsStateWithLifecycle(initialValue = "")
    val password by viewModel.password.collectAsStateWithLifecycle(initialValue = "")
    val signInResult by viewModel.signInResultFlow.collectAsStateWithLifecycle(initialValue = SignInResult.Notyet())

    userIdText = userId
    passwordText = password

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValue ->
        when(signInResult) {
            is SignInResult.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        Modifier
                            .align(Alignment.Center)
                            .size(50.dp))
                }
            }
            is SignInResult.Error -> {
                LaunchedEffect(signInResult) {
                    snackbarHostState.showSnackbar(message = (signInResult as SignInResult.Error).message)
                }
            }
            is SignInResult.Success -> onSignInSuccess()
            is SignInResult.Notyet -> {/* Nothing */}
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue),
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
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
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