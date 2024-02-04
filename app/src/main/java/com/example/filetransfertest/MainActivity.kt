package com.example.filetransfertest

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.filetransfertest.database.ServerDatabase
import com.example.filetransfertest.fragments.AddServerFragment
import com.example.filetransfertest.fragments.SearchServerFragment
import com.example.filetransfertest.fragments.ServerListFragment
import com.example.filetransfertest.fragments.ServerLoginFragment
import com.example.filetransfertest.fragments.ViewDirectoryFragment
import com.example.filetransfertest.fragments.ViewServerFragment
import com.example.filetransfertest.ui.theme.FileTransferTestTheme
import com.example.filetransfertest.viewModels.AddServerViewModel
import com.example.filetransfertest.viewModels.FragmentViewModel
import com.example.filetransfertest.viewModels.ServerListViewModel


/*
    Fragment States for Pages
 */
sealed class FragmentState {
    object ServerListFragment : FragmentState()
    object AddServerFragment : FragmentState()
    object ViewServerFragment : FragmentState()
    object ServerLoginFragment : FragmentState()
    object ViewDirectoryFragment : FragmentState()
    object SearchServerFragment : FragmentState()
}

class MainActivity : ComponentActivity() {
    /*
        server Database for the activity (Holds servers you added)
     */
    companion object {
        lateinit var database: ServerDatabase
    }

    //private lateinit var serverService: SocketServerService
    /*
        ViewModels for the activity
     */
    private val serverListViewModel: ServerListViewModel by viewModels()
    private val fragmentViewModel: FragmentViewModel by viewModels()
    private val addServerViewModel: AddServerViewModel by viewModels()

    /*
        Fragment Classes
     */
    private lateinit var serverListFragment: ServerListFragment
    private lateinit var addServerFragment: AddServerFragment
    private lateinit var viewServerFragment: ViewServerFragment
    private lateinit var serverLoginFragment: ServerLoginFragment
    private lateinit var viewDirectoryFragment: ViewDirectoryFragment
    private lateinit var searchServerFragment: SearchServerFragment

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the Database
        database = Room.databaseBuilder(this, ServerDatabase::class.java, "my-cloud-db").build()
        // Initialize the view models
        //serverListViewModel = ViewModelProvider(this, ServerListViewModel.Factory(serverServiceBinderStateFlow))[ServerListViewModel::class.java]
        // Initialize the Fragment Classes
        serverListFragment = ServerListFragment(serverListViewModel, fragmentViewModel, addServerViewModel)
        addServerFragment = AddServerFragment(serverListViewModel, fragmentViewModel, addServerViewModel)
        viewServerFragment = ViewServerFragment(serverListViewModel, fragmentViewModel, addServerViewModel)
        serverLoginFragment = ServerLoginFragment(serverListViewModel, fragmentViewModel, addServerViewModel)
        viewDirectoryFragment = ViewDirectoryFragment(serverListViewModel, fragmentViewModel, addServerViewModel)
        searchServerFragment = SearchServerFragment(serverListViewModel, fragmentViewModel, addServerViewModel)

        //viewModel.setServerData(serverDatabase.getServerDataList(this))

        // Back Button Functionality
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!fragmentViewModel.back())
                {
                    finish()
                }
            }
        })

        // Main Compose Content
        setContent {
            FileTransferTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FragmentContainer() // Fragment States
                }
            }
        }
    }

    @Composable
    fun FragmentContainer() {
        // Observe FragmentStates and Change Dynamically
        val currentFragmentState by fragmentViewModel.fragmentStateFlow.collectAsState()
        // TODO("Make a Interface for Fragments Such that they will have a Content Function")
        when (currentFragmentState)
        {
            FragmentState.AddServerFragment -> addServerFragment.Content()
            FragmentState.ServerListFragment -> serverListFragment.Content()
            FragmentState.ViewServerFragment -> viewServerFragment.Content()
            FragmentState.ServerLoginFragment -> serverLoginFragment.Content()
            FragmentState.ViewDirectoryFragment -> viewDirectoryFragment.Content()
            FragmentState.SearchServerFragment -> searchServerFragment.Content()
        }
    }
}
