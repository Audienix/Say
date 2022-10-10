package com.twain.say.data.model

data class AlertDialogDetails(
    val iconRes: Int,
    val alertTitle:String,
    val alertMsg: String,
    val btnPositiveText: String,
    val btnNegativeText: String,
    val isAlertCancelable: Boolean
)