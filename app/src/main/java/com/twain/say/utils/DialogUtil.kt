package com.twain.say.utils

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.twain.say.data.model.AlertDialogDetails


class DialogUtil {

    interface OnAlertButtonClickListener {
        fun onPositiveButtonClick(dialog: AlertDialog, inputText: String)
        fun onNegativeButtonClick(dialog: AlertDialog, inputText: String)
    }

    companion object {
        private fun createAlertDialog(
            context: Activity,
            alertDlg: AlertDialogDetails
        ): AlertDialog.Builder {
            val builder = AlertDialog.Builder(context)
            // Set alert dialog properties
            builder.setTitle(alertDlg.alertTitle)
            builder.setMessage(alertDlg.alertMsg)
            builder.setIcon(alertDlg.iconRes)
            return builder
        }

        private fun setupAlertDialog(
            context: Activity,
            alertDlg: AlertDialogDetails
        ): Pair<AlertDialog.Builder, AlertDialog> {
            // Create the AlertDialog
            val alertDialogBuilder = createAlertDialog(context, alertDlg)
            val alertDialog: AlertDialog = alertDialogBuilder.create()
            return Pair(alertDialogBuilder, alertDialog)
        }

        private fun setAlertDialogProperties(
            alertDialog: AlertDialog,
            alertDlg: AlertDialogDetails,
            alertDialogBuilder: AlertDialog.Builder
        ) {
            // Set alert dialog cancelable properties
            alertDialog.setCancelable(alertDlg.isAlertCancelable)
            alertDialog.setCanceledOnTouchOutside(alertDlg.isAlertCancelable)
            alertDialogBuilder.show()
        }

        fun showNoInternetAlertDialog(context: Activity, alertDlg: AlertDialogDetails) {
            val (alertDialogBuilder, alertDialog: AlertDialog) = setupAlertDialog(context, alertDlg)
            // Set alert dialog button properties
            alertDialogBuilder.setPositiveButton(alertDlg.btnPositiveText) { dialog, _ ->
                dialog.dismiss()
            }
            setAlertDialogProperties(alertDialog, alertDlg, alertDialogBuilder)
        }
    }
}