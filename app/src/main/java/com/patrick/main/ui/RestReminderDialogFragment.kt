package com.patrick.main.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RestReminderDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("休息提醒")
            .setMessage("請盡快找地方休息，確保您的安全。")
            .setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
    }
}
