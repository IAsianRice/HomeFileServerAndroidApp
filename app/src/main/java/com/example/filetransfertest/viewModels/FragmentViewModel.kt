package com.example.filetransfertest.viewModels

import android.util.Log
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import com.example.filetransfertest.FragmentState
import com.example.filetransfertest.publicKeyToPem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Stack

class FragmentViewModel : ViewModel() {
    private val _fragmentStateFlow = MutableStateFlow<FragmentState>(FragmentState.ServerListFragment)
    val fragmentStateFlow = _fragmentStateFlow.asStateFlow()

    private val fragmentStack = Stack<FragmentState>()

    init {
        fragmentStack.push(FragmentState.ServerListFragment)
    }

    fun setFragmentState(state: FragmentState) {
        _fragmentStateFlow.value = state
        fragmentStack.push(state)
    }

    fun back(): Boolean {
        fragmentStack.pop()
        if (fragmentStack.empty())
        {
            return false
        }
        val state = fragmentStack.peek()
        _fragmentStateFlow.value = state
        return true
    }

    override fun onCleared() {
        super.onCleared()

        Log.d("Viewmodel", "Cleared")
    }
}