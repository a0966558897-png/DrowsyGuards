package com.patrick.main.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SevereFatigueDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("疲勞偵測警示")
            .setMessage("系統偵測到您可能處於疲勞狀態。為了您的安全，請選擇一個行動方案。")
            .setPositiveButton("我已清醒") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("我會找地方休息") { dialog, _ ->
                openMapsToNearestStop()
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
    }

    private fun openMapsToNearestStop() {
        val uri = Uri.parse("geo:0,0?q=加油站")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
