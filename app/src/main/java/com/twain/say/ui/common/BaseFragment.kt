package com.twain.say.ui.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.twain.say.utils.NetworkUtil
import com.twain.say.R


abstract class BaseFragment<VM : ViewModel, VB : ViewBinding> : androidx.fragment.app.Fragment() {

    private var _viewModel: VM? = null
    protected val mViewModel get() = _viewModel!!

    private var _binding: VB? = null
    protected val mViewBinding get() = _binding!!

    private var alertDialogProgressBar: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        _viewModel = getViewModel()
        return mViewBinding.root
    }

    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    abstract fun getViewModel(): VM

    open fun showLoadingProgressBar(context: Context, message: String?, cancelable: Boolean) {
        if ((context as AppCompatActivity).isDestroyed) return

        val adb = AlertDialog.Builder(context)
        val view: View = layoutInflater.inflate(R.layout.layout_progressbar, null)
        val tv = view.findViewById<View>(R.id.tvProgressText) as TextView
        tv.text = message
        adb.setView(view)
        alertDialogProgressBar = adb.create()
        alertDialogProgressBar!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialogProgressBar!!.setCancelable(cancelable)
        alertDialogProgressBar!!.show()
    }

    open fun closeProgressBar() {
        if (alertDialogProgressBar == null) {
            return
        }
        alertDialogProgressBar!!.dismiss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}