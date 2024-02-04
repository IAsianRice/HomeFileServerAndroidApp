package com.example.filetransfertest.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.filetransfertest.api_login
import com.example.filetransfertest.ui.theme.FileTransferTestTheme
import com.example.filetransfertest.viewModels.AddServerViewModel
import com.example.filetransfertest.viewModels.FragmentViewModel
import com.example.filetransfertest.viewModels.ServerListViewModel

class ServerLoginFragment(
    private val serverListViewModel: ServerListViewModel,
    private val fragmentViewModel: FragmentViewModel,
    private val addServerViewModel: AddServerViewModel
) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        _Content(loginBtn = { username, password ->
            serverListViewModel.serverListStateFlow.value[serverListViewModel.selectedServerStateFlow.value]!!.sendAsymmetricEncryptedFormattedMessage(
                api_login(username, password), 1L
            )
            fragmentViewModel.back()
        })
    }

    companion object {
        @Composable
        fun PreviewContent() {
            _Content()
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        private fun _Content(
            loginBtn: (String, String) -> Unit = {_,_-> }) {

            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Email field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Perform login action
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    // Login button
                    Button(
                        onClick = {
                            loginBtn(username, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(text = "Login")
                    }
                }
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
fun ServerLoginFragmentPreview() {
    /*val serverStateFlow = MutableStateFlow<SocketServerService.SocketServiceBinder?>(null)
    val serverListViewModelMockView: ServerListViewModel = ServerListViewModel(serverStateFlow.asStateFlow())
    val fragmentViewModelMockView: FragmentViewModel = FragmentViewModel()*/

    FileTransferTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ServerLoginFragment.PreviewContent()
        }
    }
}