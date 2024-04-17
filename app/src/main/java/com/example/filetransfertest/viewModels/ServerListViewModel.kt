package com.example.filetransfertest.viewModels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.filetransfertest.MainActivity
import com.example.filetransfertest.ServerConnection
import com.example.filetransfertest.database.ServerInformation
import com.example.filetransfertest.services.SocketServerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *  Conceptual: Handler for the list of servers
 *  Job:
 *      Hold Server list in a stateflow
 *      Hold Selected Server from list
 *      Bind to SocketServerService to retrieve server states
 *      Manager for SocketServerService
**/
class ServerListViewModel(application: Application) : AndroidViewModel(application) {

    // Server Service Related Variables
    private val _serverListStateFlow = MutableStateFlow<MutableMap<Long, ServerConnection>>(mutableMapOf())
    var serverListStateFlow: StateFlow<MutableMap<Long, ServerConnection>> = _serverListStateFlow.asStateFlow()

    // Selected Server Name
    private val _selectedServerStateFlow = MutableStateFlow<Long?>(null)
    var selectedServerStateFlow = _selectedServerStateFlow.asStateFlow()

    lateinit var socketServiceBinder: SocketServerService.SocketServiceBinder
    var serverServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            socketServiceBinder = (service as SocketServerService.SocketServiceBinder)
            viewModelScope.launch {
                socketServiceBinder.addServerConnections(
                    ArrayList(
                        MainActivity.database.serverInformationDao().getAllServerInformation()
                    )
                )
            }
            viewModelScope.launch {
                socketServiceBinder.getAllConnectionsStateFlow().asStateFlow().collect() {
                    _serverListStateFlow.value = it
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    init {
        val serviceIntent = Intent(application, SocketServerService::class.java)
        application.startService(serviceIntent)
        application.bindService(serviceIntent, serverServiceConnection, Context.BIND_AUTO_CREATE);
    }

    fun searchForServers() {
        socketServiceBinder.startSearchForServers()
    }

    fun addServerData(serverInformation: ServerInformation) {

        viewModelScope.launch {
            MainActivity.database.serverInformationDao().insert(serverInformation);
        }
        socketServiceBinder.addServerConnection(serverInformation)
    }

    fun setFocusedServerID(id: Long) {
        _selectedServerStateFlow.value = id
    }

    fun removeServerData(id: Long) {
        viewModelScope.launch {
            MainActivity.database.serverInformationDao().deleteById(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("Viewmodel", "Cleared")
    }
}
