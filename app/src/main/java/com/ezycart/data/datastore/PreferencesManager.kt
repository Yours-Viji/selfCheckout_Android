package com.ezycart.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ezycart.data.datastore.model.UserPreferences
import com.ezycart.domain.model.AppMode
import com.ezycart.model.EmployeeLoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val MERCHANT_ID = stringPreferencesKey("merchant_id")
        private val OUTLET_ID = stringPreferencesKey("outlet_id")
        private val IS_DEVICE_ACTIVATED = booleanPreferencesKey("is_device_activated")
        private val APP_MODE = stringPreferencesKey("app_mode")
        private val SHOPPING_CART_ID = stringPreferencesKey("cart_id")

        private val X_AUTH_TOKEN = stringPreferencesKey("x_auth_token")
        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val ALLOW_EZY_LITE = booleanPreferencesKey("allowEzycartLite")
        private val EMPLOYEE_EMAIL = stringPreferencesKey("email")
        private val EMPLOYEE_DEPARTMENT = stringPreferencesKey("employeeDepartment")
        private val EMPLOYEE_PIN = stringPreferencesKey("employeePin")
        private val EMPLOYEE_NAME = stringPreferencesKey("employeeName")
        private val EMPLOYEE_POSITION = stringPreferencesKey("employeePosition")
        private val EMPLOYEE_TYPE = stringPreferencesKey("employeeType")
        private val EMPLOYEE_ID = intPreferencesKey("id")
        private val EMPLOYEE_STATUS = intPreferencesKey("status")
        private val CAN_SHOW_PRICE_CHECKER = booleanPreferencesKey("showPriceChecker")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                allowEzyCartLite = preferences[ALLOW_EZY_LITE] ?: false,
                employeeEmail = preferences[EMPLOYEE_EMAIL] ?: "",
                employeeDepartment = preferences[EMPLOYEE_DEPARTMENT] ?: "",
                xAuthToken = preferences[X_AUTH_TOKEN] ?: "",
                employeePin = preferences[EMPLOYEE_PIN] ?: "",
                employeeName = preferences[EMPLOYEE_NAME] ?: "",
                employeePosition = preferences[EMPLOYEE_POSITION] ?: "",
                employeeType = preferences[EMPLOYEE_TYPE] ?: "",
                employeeId = preferences[EMPLOYEE_ID] ?: 0,
                employeeStatus = preferences[EMPLOYEE_STATUS] ?: 0,
            )
        }
    suspend fun saveEmployeeDetails(data: EmployeeLoginResponse) {
        dataStore.edit { preferences ->
            preferences[EMPLOYEE_EMAIL] = data.email
            preferences[EMPLOYEE_DEPARTMENT] = data.employeeDepartment
            preferences[X_AUTH_TOKEN] = data.token
            preferences[EMPLOYEE_PIN] = data.employeePin
            preferences[EMPLOYEE_NAME] = data.employeeName
            preferences[EMPLOYEE_POSITION] = data.employeePosition
            preferences[EMPLOYEE_TYPE] = data.employeeType
            preferences[EMPLOYEE_ID] = data.id
            preferences[EMPLOYEE_STATUS] = data.status
        }
          saveXAuthToken(data.token)
    }
    suspend fun saveCartId(cartId:String){
        dataStore.edit { preferences ->
            preferences[SHOPPING_CART_ID] = cartId
        }
    }

    suspend fun saveAuthToken(token:String){
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }
    suspend fun saveXAuthToken(token:String){
        dataStore.edit { preferences ->
            preferences[X_AUTH_TOKEN] = token
        }
    }
    suspend fun saveJwtToken(token:String){
        dataStore.edit { preferences ->
            preferences[JWT_TOKEN] = token
        }
    }
    suspend fun saveMerchantId(id:String){
        dataStore.edit { preferences ->
            preferences[MERCHANT_ID] = id
        }
    }
    suspend fun saveOutletId(id:String){
        dataStore.edit { preferences ->
            preferences[OUTLET_ID] = id
        }
    }

    suspend fun setDeviceActivated(){
        dataStore.edit { preferences ->
            preferences[IS_DEVICE_ACTIVATED] = true
        }
    }
    suspend fun setPriceCheckerStatus(status:Boolean){
        dataStore.edit { preferences ->
            preferences[CAN_SHOW_PRICE_CHECKER] = status
        }
    }
    suspend fun setAppMode(appMode: AppMode){
        dataStore.edit { preferences ->
            preferences[APP_MODE] = appMode.name
        }
    }
    suspend fun getAppMode(): AppMode {
        val name = dataStore.data.first()[APP_MODE] ?: AppMode.EzyLite.name
        return AppMode.valueOf(name)
    }
    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getAuthToken(): String? {
        return dataStore.data.first()[AUTH_TOKEN]
    }
    suspend fun getEmployeeName(): String {
        return dataStore.data.first()[EMPLOYEE_NAME] ?: ""
    }
    suspend fun getShoppingCartId(): String {
        return dataStore.data.first()[SHOPPING_CART_ID] ?: ""
    }

    suspend fun getXAuthToken(): String? {
        return dataStore.data.first()[X_AUTH_TOKEN]
    }
    suspend fun getJwtToken(): String {
        return dataStore.data.first()[JWT_TOKEN] ?: ""
    }
     fun getCartId(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[SHOPPING_CART_ID] ?: ""
        }.distinctUntilChanged()
    }
    suspend fun getMerchantId(): String {
        return dataStore.data.first()[MERCHANT_ID] ?: "11"
    }

    suspend fun getOutletId(): String {
        return dataStore.data.first()[OUTLET_ID] ?: "19"
    }

    suspend fun getEmployeeId(): Int {
        return dataStore.data.first()[EMPLOYEE_ID] ?: 0
    }

    suspend fun canShowPriceChecker(): Boolean {
        return dataStore.data.first()[CAN_SHOW_PRICE_CHECKER] ?: true
    }

     fun isDeviceActivated(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_DEVICE_ACTIVATED] ?: false
        }.distinctUntilChanged()
    }

    suspend fun clearEmployeeDetails() {
        dataStore.edit { preferences ->

            preferences.remove(EMPLOYEE_EMAIL)
            preferences.remove(EMPLOYEE_DEPARTMENT)
            preferences.remove(X_AUTH_TOKEN)
            preferences.remove(EMPLOYEE_PIN)
            preferences.remove(EMPLOYEE_NAME)
            preferences.remove(EMPLOYEE_POSITION)
            preferences.remove(EMPLOYEE_TYPE)
            preferences.remove(EMPLOYEE_ID)
            preferences.remove(EMPLOYEE_STATUS)
            preferences.remove(SHOPPING_CART_ID)
            preferences.remove(AUTH_TOKEN)
        }
    }
}