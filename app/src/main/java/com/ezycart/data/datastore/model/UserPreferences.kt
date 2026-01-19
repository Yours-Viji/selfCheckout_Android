package com.ezycart.data.datastore.model



data class UserPreferences(
    val employeeId: Int = 0,
    val employeeEmail: String = "",
    val employeeName: String = "",
    val xAuthToken: String = "",
    val allowEzyCartLite: Boolean = false,
    val employeeDepartment: String = "",
    val employeePin: String = "",
    val employeePosition: String = "",
    val employeeType: String = "",
    val employeeStatus: Int = 0

)