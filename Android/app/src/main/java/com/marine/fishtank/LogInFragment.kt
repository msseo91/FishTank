package com.marine.fishtank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.marine.fishtank.viewmodel.LogInViewModel
import dagger.hilt.android.AndroidEntryPoint

sealed class LogInEvent {
    data class SignIn(val userId: String, val password: String) : LogInEvent()
}

private const val TAG = "LogInFragment"

@AndroidEntryPoint
class LogInFragment : Fragment() {
    private val _viewModel: Lazy<LogInViewModel> by lazy { viewModels() }
    private val viewModel by lazy { _viewModel.value }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")

        if(viewModel.isAlreadySignIn()) {
            Log.d(TAG, "Already sign-in")
            navigate(Screen.LogIn, Screen.FishTank)
        }

        viewModel.signInResult.observe(viewLifecycleOwner) { signIn ->
            if(signIn.result) {
                navigate(Screen.LogIn, Screen.FishTank)
            } else {
                Toast.makeText(context, "Fail to sign-in", Toast.LENGTH_SHORT).show()
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LogInScreen(viewModel) { event ->
                        when (event) {
                            is LogInEvent.SignIn -> {
                                viewModel.signIn(event.userId, event.password)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        viewModel.fetchSavedUser()
    }
}

@Composable
fun LogInScreen(viewModel: LogInViewModel, onEvent: (LogInEvent) -> Unit) {
    var userIdText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val userId by viewModel.userIdData.observeAsState()
    val password by viewModel.userPasswordData.observeAsState()

    userIdText = userId ?: ""
    passwordText = password ?: ""

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
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
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

                    IconButton(onClick = {passwordVisible = !passwordVisible}){
                        Icon(imageVector  = image, description)
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
                onClick = { onEvent(LogInEvent.SignIn(userIdText, passwordText)) },
            ) {
                Text(text = "SIGN IN")
            }
        }
    }
}