package com.example.filetransfertest.fragments

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filetransfertest.FragmentState
import com.example.filetransfertest.R
import com.example.filetransfertest.ServerConnection
import com.example.filetransfertest.ServerState
import com.example.filetransfertest.ui.theme.FileTransferTestTheme
import com.example.filetransfertest.viewModels.AddServerViewModel
import com.example.filetransfertest.viewModels.FragmentViewModel
import com.example.filetransfertest.viewModels.ServerListViewModel

class ServerListFragment(
    private val serverListViewModel: ServerListViewModel,
    private val fragmentViewModel: FragmentViewModel,
    private val addServerViewModel: AddServerViewModel
) {
    @Composable
    fun Content() {
        val serverDataList by serverListViewModel.serverListStateFlow.collectAsState()
        _Content(
            {
                fragmentViewModel.setFragmentState(FragmentState.SearchServerFragment)
            },
            serverDataList,
            onClick = {
                serverListViewModel.setFocusedServerID(it.serverInformation.id)
                fragmentViewModel.setFragmentState(FragmentState.ViewServerFragment)
            })

    }

    companion object {
        @Composable
        fun PreviewContent() {
            _Content()
        }

        @Composable
        private fun _Content(
            addContent: () -> Unit = {},
            serverDataList: MutableMap<Long, ServerConnection> = mutableMapOf(),
            onClick: (ServerConnection) -> Unit = {}
        ) {

            ServerListView(serverDataList) {
                onClick(it)
            }
            Box()
            {
                FloatingActionButton(
                    onClick = addContent,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd) // Align the FAB to the bottom end (bottom right)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, // Use your desired icon
                        contentDescription = "Add"
                    )
                }
            }
        }

        @Composable
        private fun ServerListView(serverDataList: MutableMap<Long, ServerConnection>,
                                   onClick: (ServerConnection) -> Unit) {
            Log.d("Service", "View Reconstruction Base")
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(serverDataList.toList()) { (id, value) ->
                    ListItem(id, value) {
                        onClick(value)
                    }
                }
            }
        }

        @Composable
        private fun ListItem(id: Long, item: ServerConnection, onClick: () -> Unit) {
            val serverState by item.stateFlow.collectAsState()


            Log.d("Service", "View Reconstruction ${item.serverInformation.title}")
            when (serverState) {
                ServerState.Error -> Log.d(
                    "Service",
                    "Service ${item.serverInformation.title}: Error"
                )

                ServerState.Offline -> Log.d(
                    "Service",
                    "Service ${item.serverInformation.title}: Offline"
                )

                ServerState.Online -> Log.d(
                    "Service",
                    "Service ${item.serverInformation.title}: Online"
                )

                ServerState.Unresponsive -> Log.d(
                    "Service",
                    "Service ${item.serverInformation.title}: Unresponsive"
                )

                ServerState.Connected -> Log.d(
                    "Service",
                    "Service ${item.serverInformation.title}: Connected"
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        onClick()
                    }
            ) {
                Row() {
                    Row() {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "ServerImage",
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                        )
                        Column {
                            Text(
                                text = item.serverInformation.title,
                                fontSize = 18.sp,
                            )
                            Text(
                                text = "${item.serverInformation.local_ip}:${item.serverInformation.port}",
                                fontSize = 14.sp,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Row() {
                        Column {
                            Text(
                                text = "Server Status",
                                fontSize = 14.sp,
                            )
                            Text(
                                text =
                                when (serverState) {
                                    ServerState.Error -> "Error"
                                    ServerState.Offline -> "Offline"
                                    ServerState.Online -> "Online"
                                    ServerState.Unresponsive -> "Unresponsive"
                                    ServerState.Connected -> "Connected"
                                },
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
fun ServerListFragmentPreview() {
    /*val serverStateFlow = MutableStateFlow<SocketServerService.SocketServiceBinder?>(null)
    val serverListViewModelMockView: ServerListViewModel = ServerListViewModel(serverStateFlow.asStateFlow())
    val fragmentViewModelMockView: FragmentViewModel = FragmentViewModel()*/
    FileTransferTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ServerListFragment.PreviewContent()
        }
    }
}