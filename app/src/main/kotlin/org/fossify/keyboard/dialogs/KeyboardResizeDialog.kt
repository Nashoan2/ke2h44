package org.fossify.keyboard.dialogs

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.DialogKeyboardResizeBinding
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.extensions.getKeyboardDialogBuilder
import org.fossify.keyboard.extensions.safeStorageContext
import org.fossify.keyboard.extensions.setupKeyboardDialogStuff

class KeyboardResizeDialog {
    private var dialog: AlertDialog? = null
    private val binding: DialogKeyboardResizeBinding
    private var currentPercentage: Int

    constructor(activity: BaseSimpleActivity, callback: (newPercentage: Int) -> Unit) {
        binding = DialogKeyboardResizeBinding.inflate(activity.layoutInflater)
        currentPercentage = activity.config.keyboardHeightPercentage.coerceIn(100, 180)
        setupControls(
            onSave = {
                activity.config.keyboardHeightPercentage = currentPercentage
                callback(currentPercentage)
            }
        )

        val builder = activity.getAlertDialogBuilder()
        activity.setupDialogStuff(binding.root, builder) { alertDialog ->
            dialog = alertDialog
        }
    }

    constructor(inputView: View, callback: (newPercentage: Int) -> Unit) {
        val themedContext = ContextThemeWrapper(inputView.context.safeStorageContext, R.style.MyKeyboard_Alert)
        binding = DialogKeyboardResizeBinding.inflate(LayoutInflater.from(themedContext))
        val config = themedContext.config
        currentPercentage = config.keyboardHeightPercentage.coerceIn(100, 180)
        setupControls(
            onSave = {
                config.keyboardHeightPercentage = currentPercentage
                callback(currentPercentage)
            }
        )

        val builder = themedContext.getKeyboardDialogBuilder()
        themedContext.setupKeyboardDialogStuff(inputView.windowToken, binding.root, builder) { alertDialog ->
            dialog = alertDialog
        }
    }

    private fun setupControls(onSave: () -> Unit) {
        updateUI()

        binding.seekbarKeyboardHeight.apply {
            max = 80 // 100 to 180
            progress = (currentPercentage - 100).coerceIn(0, 80)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        currentPercentage = (100 + progress).coerceIn(100, 180)
                        updatePercentageLabel()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        binding.btnHeightMinus.setOnClickListener {
            currentPercentage = (currentPercentage - 5).coerceIn(100, 180)
            updateUI()
        }

        binding.btnHeightPlus.setOnClickListener {
            currentPercentage = (currentPercentage + 5).coerceIn(100, 180)
            updateUI()
        }

        binding.btnPreset100.setOnClickListener { setPreset(100) }
        binding.btnPreset120.setOnClickListener { setPreset(120) }
        binding.btnPreset140.setOnClickListener { setPreset(140) }
        binding.btnPreset160.setOnClickListener { setPreset(160) }
        binding.btnPreset180.setOnClickListener { setPreset(180) }

        binding.dialogBtnResizeReset.setOnClickListener {
            setPreset(100)
        }

        binding.dialogBtnResizeCancel.setOnClickListener {
            dialog?.dismiss()
        }

        binding.dialogBtnResizeDone.setOnClickListener {
            onSave()
            dialog?.dismiss()
        }
    }

    private fun setPreset(percentage: Int) {
        currentPercentage = percentage.coerceIn(100, 180)
        updateUI()
    }

    private fun updateUI() {
        binding.seekbarKeyboardHeight.progress = (currentPercentage - 100).coerceIn(0, 80)
        updatePercentageLabel()
    }

    private fun updatePercentageLabel() {
        binding.dialogResizePercentage.text = "$currentPercentage%"
    }
}
