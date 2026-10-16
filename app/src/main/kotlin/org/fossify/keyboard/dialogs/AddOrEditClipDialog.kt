package org.fossify.keyboard.dialogs

import androidx.appcompat.app.AlertDialog
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.extensions.showKeyboard
import org.fossify.commons.extensions.toast
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.DialogAddOrEditClipBinding
import org.fossify.keyboard.helpers.ClipsHelper
import org.fossify.keyboard.models.Clip

class AddOrEditClipDialog(val activity: BaseSimpleActivity, val originalClip: Clip?, val callback: () -> Unit) {
    init {
        val binding = DialogAddOrEditClipBinding.inflate(activity.layoutInflater).apply {
            if (originalClip != null) {
                addClipValue.setText(originalClip.value)
                addClipIsPinned.isChecked = originalClip.isPinned
            } else {
                addClipIsPinned.isChecked = true
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.showKeyboard(binding.addClipValue)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val clipValue = binding.addClipValue.text.toString().trim()
                        if (clipValue.isEmpty()) {
                            activity.toast(R.string.value_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val isPinned = binding.addClipIsPinned.isChecked
                        val clip = Clip(originalClip?.id, clipValue, isPinned)

                        ensureBackgroundThread {
                            ClipsHelper(activity).insertClip(clip)
                            activity.runOnUiThread {
                                callback()
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }
}
