package com.ezycart.presentation.login

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.data.datastore.model.UserPreferences
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.domain.usecase.GetAuthDataUseCase
import com.ezycart.domain.usecase.LoadingManager
import com.ezycart.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getAuthDataUseCase: GetAuthDataUseCase,
    private val preferencesManager: PreferencesManager,
    private val loadingManager: LoadingManager
) : ViewModel() {
    private val _stateFlow = MutableStateFlow(LoginState())
    val stateFlow: StateFlow<LoginState> = _stateFlow.asStateFlow()
    val userPreferences: StateFlow<UserPreferences> =
        preferencesManager.userPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserPreferences()
            )
    val authState = getAuthDataUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun onEmployeePinChange(employeeCode: String) {
        _stateFlow.value = _stateFlow.value.copy(employeePin = employeeCode)
    }

    fun extractEmployeePin(scanned: String): String {
        if (scanned.isEmpty()) return ""
        val afterColon = scanned.substringAfter(':', "")
        return if (afterColon.all { it.isDigit() } && afterColon.isNotEmpty()) {
            afterColon
        } else {
            Regex("\\d+").find(scanned)!!.value
        }
    }
    fun login(pin: String = _stateFlow.value.employeePin) {
        loadingManager.show()
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, error = null)

            when (val result = loginUseCase(pin)) {
                is NetworkResponse.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                    loadingManager.hide()
                }
                is NetworkResponse.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        error = result.message,
                        isLoginSuccessful = false
                    )
                    loadingManager.hide()
                }
            }
        }
    }


}