package com.patrick.main.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ModerateFatigueDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("中度疲勞警示")
            .setMessage("偵測到您可能有疲勞徵兆，請確認您已清醒。")
            .setPositiveButton("我已清醒") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        findNavController().popBackStack()
    }
}
