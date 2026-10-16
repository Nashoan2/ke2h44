package org.fossify.keyboard.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.removeUnderlines
import org.fossify.commons.extensions.toast
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.ItemClipOnKeyboardBinding
import org.fossify.keyboard.databinding.ItemSectionLabelBinding
import org.fossify.keyboard.extensions.clipsDB
import org.fossify.keyboard.extensions.getCurrentClip
import org.fossify.keyboard.extensions.getKeyboardDialogBuilder
import org.fossify.keyboard.extensions.setupKeyboardDialogStuff
import android.view.HapticFeedbackConstants
import org.fossify.keyboard.helpers.ClipsHelper
import org.fossify.keyboard.helpers.ITEM_CLIP
import org.fossify.keyboard.helpers.ITEM_SECTION_LABEL
import org.fossify.keyboard.interfaces.RefreshClipsListener
import org.fossify.keyboard.models.Clip
import org.fossify.keyboard.models.ClipsSectionLabel
import org.fossify.keyboard.models.ListItem
import java.util.Collections

class ClipsKeyboardAdapter(
    val context: Context,
    var items: ArrayList<ListItem>,
    val refreshClipsListener: RefreshClipsListener,
    val onStartDrag: ((RecyclerView.ViewHolder) -> Unit)? = null,
    val itemClick: (clip: Clip) -> Unit
) : RecyclerView.Adapter<ClipsKeyboardAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = when (viewType) {
            ITEM_SECTION_LABEL -> ItemSectionLabelBinding.inflate(layoutInflater, parent, false)
            else -> ItemClipOnKeyboardBinding.inflate(layoutInflater, parent, false)
        }

        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item) { itemView ->
            when (item) {
                is Clip -> setupClip(itemView, item, holder)
                is ClipsSectionLabel -> setupSection(itemView, item)
            }

            (itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = item is ClipsSectionLabel
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when {
        items[position] is ClipsSectionLabel -> ITEM_SECTION_LABEL
        else -> ITEM_CLIP
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun setupClip(view: View, clip: Clip, holder: ViewHolder) {
        ItemClipOnKeyboardBinding.bind(view).apply {
            clipValue.apply {
                val cleanedText = clip.value.trim().replace(Regex("[\\r\\n\\t]+"), " ").replace(Regex(" {2,}"), " ")
                text = if (cleanedText.length > 45) {
                    cleanedText.take(42) + "..."
                } else {
                    cleanedText
                }
                removeUnderlines()
            }

            clipPinIndicator.visibility = if (clip.isPinned) View.VISIBLE else View.GONE

            root.setOnClickListener {
                itemClick.invoke(clip)
            }

            clipValue.setOnClickListener {
                itemClick.invoke(clip)
            }

            clipPinIndicator.setOnClickListener {
                togglePinClip(clip)
            }

            if (clip.isPinned) {
                // Drag and drop reordering strictly for pinned clips (no toast / message)
                root.setOnLongClickListener {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onStartDrag?.invoke(holder)
                    true
                }

                clipValue.setOnLongClickListener {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onStartDrag?.invoke(holder)
                    true
                }

                // Long press on pin indicator shows the popup options (Unpin/Delete/Cancel)
                clipPinIndicator.setOnLongClickListener {
                    showClipOptionsPopup(clip, it)
                    true
                }
            } else {
                root.setOnLongClickListener {
                    showClipOptionsPopup(clip, it)
                    true
                }

                clipValue.setOnLongClickListener {
                    showClipOptionsPopup(clip, it)
                    true
                }

                clipPinIndicator.setOnLongClickListener(null)
            }
        }
    }

    fun togglePinClip(clip: Clip) {
        ensureBackgroundThread {
            val newPinned = !clip.isPinned
            val trimmedValue = clip.value.trim()
            if (clip.id != null && clip.id != -1L) {
                context.clipsDB.updatePinned(clip.id!!, newPinned)
            } else {
                val existingId = context.clipsDB.getClipWithValue(trimmedValue)
                if (existingId != null) {
                    context.clipsDB.updatePinned(existingId, newPinned)
                    clip.id = existingId
                } else {
                    val newClip = Clip(null, trimmedValue, newPinned)
                    val newId = context.clipsDB.insertOrUpdate(newClip)
                    clip.id = newId
                }
            }
            clip.isPinned = newPinned
            Handler(Looper.getMainLooper()).post {
                context.toast(if (newPinned) R.string.text_pinned else R.string.text_unpinned)
                refreshClipsListener.refreshClips()
            }
        }
    }

    private fun deleteClip(clip: Clip) {
        if (clip.isPinned) {
            // CRITICAL: Pinned clips MUST NOT be deleted unless unpinned first!
            context.toast(R.string.cannot_delete_pinned)
            return
        }
        ClipsHelper.clearSystemClipboardIfMatches(context, clip.value)
        ensureBackgroundThread {
            if (clip.id != null && clip.id != -1L) {
                context.clipsDB.delete(clip.id!!)
            } else {
                context.clipsDB.deleteRecentClipWithValue(clip.value.trim())
            }
            Handler(Looper.getMainLooper()).post {
                refreshClipsListener.refreshClips()
            }
        }
    }

    /**
     * Shows a persistent options dialog on long press that allows Pinning, Unpinning, or Deleting.
     */
    fun showClipOptionsPopup(clip: Clip, anchorView: View) {
        val uiContext = ContextThemeWrapper(anchorView.context, R.style.MyKeyboard_Alert)
        val popupView = LayoutInflater.from(uiContext).inflate(R.layout.dialog_clip_options, null)

        val clipTextPreview = popupView.findViewById<TextView>(R.id.popup_clip_preview)
        val btnPin = popupView.findViewById<View>(R.id.popup_btn_pin)
        val pinText = popupView.findViewById<TextView>(R.id.popup_pin_text)
        val pinIcon = popupView.findViewById<ImageView>(R.id.popup_pin_icon)
        val btnDelete = popupView.findViewById<View>(R.id.popup_btn_delete)
        val btnCancel = popupView.findViewById<View>(R.id.popup_btn_cancel)

        clipTextPreview.text = clip.value
        if (clip.isPinned) {
            pinText.setText(R.string.unpin_text)
            pinIcon.setImageResource(R.drawable.ic_pin_cyan)
        } else {
            pinText.setText(R.string.pin_text)
            pinIcon.setImageResource(R.drawable.ic_pin_outline_gray)
        }

        var alertDialog: AlertDialog? = null
        var popupWindow: PopupWindow? = null

        val dismissOptions = {
            try {
                alertDialog?.dismiss()
            } catch (ignored: Exception) {}
            try {
                popupWindow?.dismiss()
            } catch (ignored: Exception) {}
        }

        btnPin.setOnClickListener {
            dismissOptions()
            togglePinClip(clip)
        }

        btnDelete.setOnClickListener {
            dismissOptions()
            deleteClip(clip)
        }

        btnCancel.setOnClickListener {
            dismissOptions()
        }

        val windowToken = anchorView.windowToken ?: anchorView.rootView?.windowToken
        var shownAsDialog = false
        if (windowToken != null) {
            try {
                val builder = uiContext.getKeyboardDialogBuilder()
                uiContext.setupKeyboardDialogStuff(
                    windowToken = windowToken,
                    view = popupView,
                    dialog = builder,
                    cancelOnTouchOutside = true
                ) { dialog ->
                    alertDialog = dialog
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                shownAsDialog = true
            } catch (e: Exception) {
                shownAsDialog = false
            }
        }

        if (!shownAsDialog) {
            try {
                val density = uiContext.resources.displayMetrics.density
                val widthPx = (300 * density).toInt()
                popupWindow = PopupWindow(
                    popupView,
                    widthPx,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    false
                ).apply {
                    elevation = 24f
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    isOutsideTouchable = true
                    val anchor = if (anchorView.isAttachedToWindow) anchorView else anchorView.rootView
                    showAtLocation(anchor, Gravity.CENTER, 0, 0)
                }
            } catch (e: Exception) {
                // Fallback: toggle pin directly if popup cannot be shown
                togglePinClip(clip)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupSection(view: View, sectionLabel: ClipsSectionLabel) {
        ItemSectionLabelBinding.bind(view).apply {
            clipsSectionLabel.apply {
                text = sectionLabel.value
                setTextColor(Color.parseColor("#A0ABC0"))
            }
            clipsSectionIcon.visibility = View.GONE
        }
    }

    open inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(any: Any, callback: (itemView: View) -> Unit): View {
            return itemView.apply {
                callback(this)
            }
        }
    }
}
