package com.example.abys.logic

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import org.junit.Test

class MainViewModelInstantiationTest {
    @Test
    fun `new instance factory can construct main viewmodel`() {
        val provider = ViewModelProvider(ViewModelStore(), ViewModelProvider.NewInstanceFactory())

        provider[MainViewModel::class.java]
    }
}

