package com.twain.say.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.twain.say.R
import com.twain.say.data.model.AlertDialogDetails
import com.twain.say.utils.DialogUtil

class NetworkUtil {
    companion object {
        fun isInternetAvailable(ctx: Context): Boolean {
            val result: Boolean
            val connectivityManager =
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
            return result
        }

        fun showNoInternetAlert(ctx: Context) {
            val alertDlg = AlertDialogDetails(
                R.drawable.ic_alert_warning,
                ctx.resources.getString(R.string.alert_title_no_internet),
                ctx.resources.getString(R.string.alert_msg_no_internet),
                ctx.resources.getString(R.string.alert_btn_fetch),
                String(),
                true
            )
            DialogUtil.showNoInternetAlertDialog(ctx as Activity, alertDlg)
        }
    }
}