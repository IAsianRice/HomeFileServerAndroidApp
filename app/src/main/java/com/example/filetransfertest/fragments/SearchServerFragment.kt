package com.example.filetransfertest.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filetransfertest.FragmentState
import com.example.filetransfertest.R
import com.example.filetransfertest.services.SocketServerService
import com.example.filetransfertest.ui.theme.FileTransferTestTheme
import com.example.filetransfertest.viewModels.AddServerViewModel
import com.example.filetransfertest.viewModels.FragmentViewModel
import com.example.filetransfertest.viewModels.ServerListViewModel


class SearchServerFragment(
    private val serverListViewModel: ServerListViewModel,
    private val fragmentViewModel: FragmentViewModel,
    private val addServerViewModel: AddServerViewModel
) {

    @Composable
    fun Content() {
        val serverDiscoveredList by serverListViewModel.socketServiceBinder!!.getServerDiscoveryStateFlow().collectAsState()
        BaseContent(
            onRefreshClick = {
                serverListViewModel.searchForServers()
            },
            addContent = {
                addServerViewModel.setDiscoveredServerData(SocketServerService.DiscoveredServerData("","",""))
                fragmentViewModel.setFragmentState(FragmentState.AddServerFragment)
            },
            serverDiscoveredList = serverDiscoveredList,
            onClick = {
                addServerViewModel.setDiscoveredServerData(it)
                fragmentViewModel.setFragmentState(FragmentState.AddServerFragment)
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
        private fun BaseContent(
            addContent: () -> Unit = {},
            onRefreshClick: () -> Unit = {},
            serverDiscoveredList: MutableList<SocketServerService.DiscoveredServerData> = arrayListOf<SocketServerService.DiscoveredServerData>(),
            onClick: (SocketServerService.DiscoveredServerData) -> Unit = {}
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = { Text(text = "Find Servers") },
                    actions = {
                        IconButton(onClick = onRefreshClick) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        }
                    },
                )
                ServerListView(serverDiscoveredList) {
                    onClick(it)
                }
                LoadingScreen()

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
        private fun ServerListView(serverDataList: MutableList<SocketServerService.DiscoveredServerData>,
                                   onClick: (SocketServerService.DiscoveredServerData) -> Unit) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(serverDataList) { broadcast ->
                    ListItem(broadcast) {
                        onClick(broadcast)
                    }
                }
            }
        }

        @Composable
        private fun ListItem(broadcast: SocketServerService.DiscoveredServerData,
            onClick: () -> Unit) {
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
                                text = "${broadcast.localIP}: ${broadcast.publicIP}",
                                fontSize = 18.sp,
                            )
                            Text(
                                text = broadcast.port,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
        @Composable
        fun LoadingScreen() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(48.dp)
                        .width(48.dp),
                    color = Color.Red
                )
                Text(
                    text = "Finding Servers...",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
@Composable
@Preview(showBackground = true,)
fun SearchServerFragmentPreview() {
    /*val serverStateFlow = MutableStateFlow<SocketServerService.SocketServiceBinder?>(null)
    val serverListViewModelMockView: ServerListViewModel = ServerListViewModel( serverStateFlow.asStateFlow())
    val fragmentViewModelMockView: FragmentViewModel = FragmentViewModel()
    val addServerViewModelMockView: AddServerViewModel = AddServerViewModel()*/
    FileTransferTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SearchServerFragment.PreviewContent()
        }
    }
}
