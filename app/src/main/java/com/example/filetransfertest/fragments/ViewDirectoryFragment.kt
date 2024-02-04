package com.example.filetransfertest.fragments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filetransfertest.Directory
import com.example.filetransfertest.File
import com.example.filetransfertest.ui.theme.FileTransferTestTheme
import com.example.filetransfertest.viewModels.AddServerViewModel
import com.example.filetransfertest.viewModels.FragmentViewModel
import com.example.filetransfertest.viewModels.ServerListViewModel
import java.util.Stack


class ViewDirectoryFragment(
    private val serverListViewModel: ServerListViewModel,
    private val fragmentViewModel: FragmentViewModel,
    private val addServerViewModel: AddServerViewModel
) {
    @Composable
    fun Content() {
        val serverList by serverListViewModel.serverListStateFlow.collectAsState()
        val serverName by serverListViewModel.selectedServerStateFlow.collectAsState()
        val directory by serverList[serverName]!!.userSession!!.baseDirectoryStateFlow.collectAsState()
        _Content(directory)
    }

    companion object {
        @Composable
        fun PreviewContent() {
            var dir = Directory(
                "Main", "Directory", listOf(
                    Directory(
                        "2nd Dir", "Directory", listOf(
                            Directory("2nd Dir", "Directory", listOf()),
                            Directory("3nd Dir", "Directory", listOf()),
                            File("text2.txt", "Directory", "TXT", 34)
                        )
                    ),
                    Directory(
                        "3nd Dir", "Directory", listOf(
                            Directory("2nd Dir", "Directory", listOf()),
                            Directory("3nd Dir", "Directory", listOf()),
                            File("text2.txt", "Directory", "TXT", 34)
                        )
                    ),
                    File("text.txt", "Directory", "TXT", 34)
                )
            )
            _Content(dir)
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        private fun _Content(rootDirectory: Directory = Directory("Empty", "Directory", listOf())) {
            var currentDirectory by remember { mutableStateOf<Directory>(rootDirectory) }
            var prevDirectory by remember { mutableStateOf<Stack<String>>(Stack()) }

            Column {
                TopAppBar(
                    title = { Text(text = rootDirectory.name) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (prevDirectory.size > 0)
                                prevDirectory.pop()
                            var itrDir = rootDirectory
                            var depth = 0
                            while (depth < prevDirectory.size) {
                                itrDir.content.forEach() { dir ->
                                    when (dir) {
                                        is Directory -> {
                                            if (dir.name == prevDirectory[depth]) {
                                                itrDir = dir
                                                depth++
                                            }
                                        }

                                        is File -> {}
                                    }
                                }
                            }
                            currentDirectory = itrDir

                        }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    itemsIndexed(currentDirectory.content) { i, file ->
                        when (file) {
                            is Directory -> {
                                DirectoryItemRow(file) {
                                    currentDirectory = file
                                    prevDirectory.push(currentDirectory.name)
                                }
                            }

                            is File -> {
                                FileItemRow(file) {

                                }
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun DirectoryItemRow(directory: Directory, onItemClick: () -> Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick() }
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.width(16.dp))

                Text(text = directory.name ?: "", fontSize = 18.sp)
            }
        }

        @Composable
        fun FileItemRow(file: File, onItemClick: () -> Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick() }
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.width(16.dp))

                Text(text = file.name ?: "", fontSize = 18.sp)
            }
        }
    }
}

@Composable
@Preview(showBackground = true,)
fun ViewDirectoryFragmentPreview() {
    /*val serverStateFlow = MutableStateFlow<SocketServerService.SocketServiceBinder?>(null)
    val serverListViewModelMockView: ServerListViewModel = ServerListViewModel(serverStateFlow.asStateFlow())
    val fragmentViewModelMockView: FragmentViewModel = FragmentViewModel()*/
    FileTransferTestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ViewDirectoryFragment.PreviewContent()
        }
    }
}