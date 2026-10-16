package org.fossify.keyboard.dialogs

import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.keyboard.databinding.DialogKeyboardResizeBinding
import org.fossify.keyboard.extensions.config
import kotlin.math.roundToInt

class KeyboardResizeDialog(
    val activity: BaseSimpleActivity,
    val callback: (newPercentage: Int) -> Unit
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogKeyboardResizeBinding.inflate(activity.layoutInflater)
    private var currentPercentage = activity.config.keyboardHeightPercentage

    init {
        updatePercentageUI()

        var startY = 0f
        var initialHeightPx = 0
        val baseContainerHeight = (220 * activity.resources.displayMetrics.density).toInt()
        val currentContainerHeight = (baseContainerHeight * (currentPercentage / 100f)).toInt()
        binding.dialogKeyboardContainer.layoutParams.height = currentContainerHeight
        binding.dialogKeyboardContainer.requestLayout()

        val touchListener = View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    initialHeightPx = binding.dialogKeyboardContainer.height
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - startY
                    val newHeightPx = (initialHeightPx - deltaY).toInt().coerceIn(
                        (baseContainerHeight * 0.70f).toInt(),
                        (baseContainerHeight * 1.60f).toInt()
                    )
                    binding.dialogKeyboardContainer.layoutParams.height = newHeightPx
                    binding.dialogKeyboardContainer.requestLayout()

                    currentPercentage = ((newHeightPx.toFloat() / baseContainerHeight.toFloat()) * 100f).roundToInt().coerceIn(70, 160)
                    updatePercentageUI()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    true
                }
                else -> false
            }
        }

        binding.dialogResizeTopHandle.setOnTouchListener(touchListener)
        binding.dialogBtnResizeMove.setOnTouchListener(touchListener)

        binding.dialogBtnResizeReset.setOnClickListener {
            currentPercentage = 100
            binding.dialogKeyboardContainer.layoutParams.height = baseContainerHeight
            binding.dialogKeyboardContainer.requestLayout()
            updatePercentageUI()
        }

        binding.dialogBtnResizeDone.setOnClickListener {
            activity.config.keyboardHeightPercentage = currentPercentage
            callback(currentPercentage)
            dialog?.dismiss()
        }

        val builder = activity.getAlertDialogBuilder()
        activity.setupDialogStuff(binding.root, builder) { alertDialog ->
            dialog = alertDialog
        }
    }

    private fun updatePercentageUI() {
        binding.dialogResizePercentage.text = "$currentPercentage%"
    }
}
