package com.aklzo.kakeiboandroid.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken.asStateFlow()

    fun setToken(token: String) {
        _idToken.value = token
    }

    fun clearToken() {
        _idToken.value = null
    }
}
