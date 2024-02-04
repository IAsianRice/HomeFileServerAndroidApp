package com.example.filetransfertest.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.filetransfertest.database.ServerInformation
import com.example.filetransfertest.services.SocketServerService
import com.example.filetransfertest.ui.theme.FileTransferTestTheme
import com.example.filetransfertest.viewModels.AddServerViewModel
import com.example.filetransfertest.viewModels.FragmentViewModel
import com.example.filetransfertest.viewModels.ServerListViewModel
import kotlinx.coroutines.flow.MutableStateFlow


class AddServerFragment(
    private val serverListViewModel: ServerListViewModel,
    private val fragmentViewModel: FragmentViewModel,
    private val addServerViewModel: AddServerViewModel
    ) {
/*
    private val serverListViewModel: ServerListViewModel by viewModels()
    private val fragmentViewModel: FragmentViewModel by viewModels()
    private val addServerViewModel: AddServerViewModel by viewModels()*/

    @Composable
    fun Content() {
        var serverName by remember { mutableStateOf("") }
        val discoveredServerData by addServerViewModel.discoveredServerDataStateFlow.collectAsState()
        var globalIPAddress by remember { mutableStateOf(discoveredServerData.publicIP) }
        var localIPAddress by remember { mutableStateOf(discoveredServerData.localIP) }
        var port by remember { mutableStateOf(discoveredServerData.port) }

        BaseContent(
            serverName = serverName,
            globalIPAddress = globalIPAddress,
            localIPAddress = localIPAddress,
            port = port,
            onServerNameChange = { serverName = it },
            onGlobalIpAddressChange = { globalIPAddress = it },
            onLocalIpAddressChange = { localIPAddress = it },
            onPortChange = { port = it },
            onSubmitClick = {
                serverListViewModel.addServerData(ServerInformation(title = serverName,
                    internet_ip = globalIPAddress,
                    local_ip = localIPAddress,
                    port = port,
                    description = ""))
                fragmentViewModel.back()
            }
        )
    }

    companion object {
        @Composable
        fun PreviewContent() {
            BaseContent()
        }
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun BaseContent(
            serverName: String = "",
            globalIPAddress: String = "",
            localIPAddress: String = "",
            port: String = "",
            onServerNameChange: (String) -> Unit = {},
            onGlobalIpAddressChange: (String) -> Unit = {},
            onLocalIpAddressChange: (String) -> Unit = {},
            onPortChange: (String) -> Unit = {},
            onSubmitClick: () -> Unit = {}
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = "Server Name")
                TextField(
                    modifier = Modifier.fillMaxWidth(1f),
                    value = serverName,
                    onValueChange = onServerNameChange,
                    label = { Text("Enter Server Name") }
                )
                Text(text = "Local IP Address")
                TextField(
                    modifier = Modifier.fillMaxWidth(1f),
                    value = localIPAddress,
                    onValueChange = onLocalIpAddressChange,
                    label = { Text("Enter IP Address") }
                )
                Text(text = "Public IP Address")
                TextField(
                    modifier = Modifier.fillMaxWidth(1f),
                    value = globalIPAddress,
                    onValueChange = onGlobalIpAddressChange,
                    label = { Text("Enter IP Address") }
                )
                Text(text = "Port")
                TextField(
                    modifier = Modifier.fillMaxWidth(1f),
                    value = port,
                    onValueChange = onPortChange,
                    label = { Text("Enter Port") }
                )
                Box(
                    modifier = Modifier.fillMaxWidth(1f)
                ) {
                    Button(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = onSubmitClick
                    ) {
                        Text(text = "Submit")
                    }
                }
            }
        }
    }




}
@Composable
@Preview(showBackground = true,)
fun AddServerFragmentPreview() {
    val serverStateFlow = MutableStateFlow<SocketServerService.SocketServiceBinder?>(null)
    FileTransferTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AddServerFragment.PreviewContent()
        }
    }
}
