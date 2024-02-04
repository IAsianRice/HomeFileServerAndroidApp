package com.example.filetransfertest.fragments

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filetransfertest.FragmentState
import com.example.filetransfertest.getFileName
import com.example.filetransfertest.ui.theme.FileTransferTestTheme
import com.example.filetransfertest.viewModels.AddServerViewModel
import com.example.filetransfertest.viewModels.FragmentViewModel
import com.example.filetransfertest.viewModels.ServerListViewModel


class ViewServerFragment(
    private val serverListViewModel: ServerListViewModel,
    private val fragmentViewModel: FragmentViewModel,
    private val addServerViewModel: AddServerViewModel
) {
    @Composable
    fun Content() {
        val contentResolver = LocalContext.current.contentResolver
        val serverList by serverListViewModel.serverListStateFlow.collectAsState()
        val serverName by serverListViewModel.selectedServerStateFlow.collectAsState()
        val serverOutput by serverList[serverName]!!.messageFlow.collectAsState()
        var message by remember { mutableStateOf("") }
        val resultLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent(), onResult = {uri ->
            if (uri != null) {
                var fileName = getFileName(contentResolver, uri).toString()

                contentResolver.openInputStream(uri)
                    ?.let { serverList[serverName]!!.sendSymmetricEncryptedFormattedMessage(it, 5, extraData = fileName.encodeToByteArray() + 0x03) }
            }
        })
        BaseContent(
            serverList[serverName]?.serverInformation?.title ?: "Name",
            serverList[serverName]?.serverInformation?.local_ip ?: "IP",
            serverList[serverName]?.serverInformation?.port ?: "Port",
            message,
            serverOutput,
            {
                serverListViewModel.removeServerData(
                    serverName!!
                )
                fragmentViewModel.back()
            },
            {
                fragmentViewModel.setFragmentState(FragmentState.ServerLoginFragment)
            },
            {
                resultLauncher.launch("*/*")
            },
            {
                fragmentViewModel.setFragmentState(FragmentState.ViewDirectoryFragment)
            },
            {
                //serverList[serverName]!!.sendAsymmetricDataToServer(api_send_command(message))
                message = ""
            },
            { message = it },
        )
    }

    companion object {
        @Composable
        fun PreviewContent() {
            BaseContent()
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        private fun BaseContent(
            nameText: String = "Name",
            ipText: String = "IP",
            portText: String = "Port",
            rememberedText: String = "Command",
            serverOutputText: String = "Server output",
            removeBtnCallback: () -> Unit = { },
            connectBtnCallback: () -> Unit = { },
            sendFileBtnCallback: () -> Unit = { },
            viewFileBtnCallback: () -> Unit = { },
            secureSendBtnCallback: () -> Unit = { },
            commandOnTextChange: (String) -> Unit = { },
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            )
            {


                Text(text = nameText)
                Text(text = ipText)
                Text(text = portText)
                Text(text = "Server Status")
                Text(text = "Server is currently ")

                Box(
                    modifier = Modifier.fillMaxWidth(1f)
                )
                {
                    Button(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = removeBtnCallback
                    ) {
                        Text(text = "Remove")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(1f)
                )
                {
                    Button(onClick = connectBtnCallback) {
                        Text(
                            text = "Connect",
                            fontSize = 14.sp,
                        )
                    }
                    Button(onClick = sendFileBtnCallback) {
                        Text(
                            text = "Send File",
                            fontSize = 14.sp,
                        )
                    }
                    Button(onClick = viewFileBtnCallback) {
                        Text(
                            text = "View Files",
                            fontSize = 14.sp,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .fillMaxHeight(0.4f)
                )
                {
                    Column(
                        modifier = Modifier.fillMaxWidth(1f)
                    ) {
                        Text(
                            modifier = Modifier.fillMaxHeight(0.1f),
                            text = "Terminal"
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .fillMaxHeight(0.9f)
                                .border(BorderStroke(2.dp, Color.Gray))
                                .background(
                                    Color.Black
                                )
                                .padding(2.dp)
                                .verticalScroll(rememberScrollState()),
                            text = serverOutputText
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth(1f)
                )
                {
                    Column {
                        TextField(
                            modifier = Modifier.fillMaxWidth(1f),
                            value = rememberedText,
                            singleLine = true,
                            onValueChange = commandOnTextChange,
                            label = { Text(text = "Enter Command") })
                        Button(onClick = secureSendBtnCallback) {
                            Text(
                                text = "Send",
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true,)
fun ViewServerFragmentPreview() {
    /*val serverStateFlow = MutableStateFlow<SocketServerService.SocketServiceBinder?>(null)
    val serverListViewModelMockView: ServerListViewModel = ServerListViewModel(serverStateFlow.asStateFlow())
    val fragmentViewModelMockView: FragmentViewModel = FragmentViewModel()*/
    FileTransferTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ViewServerFragment.PreviewContent()
        }
    }
}