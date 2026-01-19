package com.ezycart.model

data class EmployeeLoginResponse(
    val addProductInfo: Boolean,
    val addPromotionInfo: Boolean,
    val allowEzycartLite: Boolean,
    val changeProductInfo: Boolean,
    val changePromotionInfo: Boolean,
    val email: String,
    val employeeDepartment: String,
    val employeeName: String,
    val employeePin: String,
    val employeePosition: String,
    val employeeType: String,
    val id: Int,
    val merchantId: Int,
    val outletId: Int,
    val outletName: String,
    val phoneNum: String,
    val status: Int,
    val token: String,
    val updatedDate: String,
    val createdDate: String
)


enum class EmployeeType {
    STORE_MANAGER,
    SUPER_ADMIN,
    ADMIN,
    SUPERVISOR,
    CHIEF_CASHIER,
    PREPAID_CASHIER,
    PREPAID_ADMIN,
    TROLLEY_ASSISTANT;
    companion object {
        fun getEmployeeTypeData(role: String): EmployeeType? {
            return when (role.lowercase().replace(" ","")) {
                "superadmin" -> SUPER_ADMIN
                "admin" -> ADMIN
                "chiefcashier" -> CHIEF_CASHIER
                "storemanager" -> STORE_MANAGER
                "supervisor" -> SUPERVISOR
                "prepaidcashier" -> PREPAID_CASHIER
                "trolleyassistant" -> TROLLEY_ASSISTANT
                "prepaidadmin" -> PREPAID_ADMIN
                else -> null
            }
        }


    }
    fun canModifyCart(): Boolean {
        return (this == SUPER_ADMIN || this == ADMIN || this == PREPAID_ADMIN || this == PREPAID_CASHIER)
    }
    fun canSeeSettings(): Boolean {
        return (this == SUPER_ADMIN || this == ADMIN )
    }
}

