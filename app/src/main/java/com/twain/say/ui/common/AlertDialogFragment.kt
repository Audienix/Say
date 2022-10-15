package com.twain.say.ui.common

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.twain.say.data.model.AlertDialogDetails

class AlertDialogFragment(context: Context) : MaterialAlertDialogBuilder(context) {
    lateinit var onResponse: (alertDialogInterface: DialogInterface, r: ResponseType) -> Unit

    enum class ResponseType {
        YES, NO, OK, CANCEL
    }

    fun show(
        alertDialogDetails: AlertDialogDetails,
        listener: (
            alertDialog: DialogInterface,
            rt: ResponseType
        ) -> Unit
    ) {
        val materialDialog = MaterialAlertDialogBuilder(context)
        materialDialog.apply {
            setTitle(alertDialogDetails.alertTitle)
            setMessage(alertDialogDetails.alertMsg)
            setIcon(alertDialogDetails.iconRes)
            setNegativeButton(alertDialogDetails.btnNegativeText) { dialog, _ ->
                onResponse(
                    dialog,
                    ResponseType.NO
                )
            }
            setPositiveButton(alertDialogDetails.btnPositiveText) { dialog, _ ->
                onResponse(
                    dialog,
                    ResponseType.YES
                )
            }
            onResponse = listener
            setCancelable(true)
            show()
        }
    }
}