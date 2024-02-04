package com.example.filetransfertest.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.filetransfertest.services.SocketServerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class AddServerViewModel() : ViewModel() {

    private val _discoveredServerDataStateFlow = MutableStateFlow<SocketServerService.DiscoveredServerData>(SocketServerService.DiscoveredServerData("","",""))
    val discoveredServerDataStateFlow = _discoveredServerDataStateFlow.asStateFlow()

    fun setDiscoveredServerData(serverData: SocketServerService.DiscoveredServerData) {
        _discoveredServerDataStateFlow.value = serverData
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("Viewmodel", "Cleared")
    }

}
