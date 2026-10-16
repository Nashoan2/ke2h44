package org.fossify.keyboard.adapters

import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.adapters.MyRecyclerViewAdapter
import org.fossify.commons.dialogs.ConfirmationDialog
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.setupViewBackground
import org.fossify.commons.extensions.toast
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.interfaces.ItemMoveCallback
import org.fossify.commons.interfaces.ItemTouchHelperContract
import org.fossify.commons.interfaces.RefreshRecyclerViewListener
import org.fossify.commons.interfaces.StartReorderDragListener
import org.fossify.commons.views.MyRecyclerView
import org.fossify.keyboard.R
import org.fossify.keyboard.databinding.ItemClipInActivityBinding
import org.fossify.keyboard.dialogs.AddOrEditClipDialog
import org.fossify.keyboard.extensions.clipsDB
import org.fossify.keyboard.helpers.ClipsHelper
import org.fossify.keyboard.models.Clip
import java.util.Collections

class ClipsActivityAdapter(
    activity: BaseSimpleActivity, var items: ArrayList<Clip>, recyclerView: MyRecyclerView, val listener: RefreshRecyclerViewListener, itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick), ItemTouchHelperContract {

    private var touchHelper: ItemTouchHelper? = null
    private var startReorderDragListener: StartReorderDragListener
    private var wasClipMoved = false

    init {
        setupDragListener(true)

        touchHelper = ItemTouchHelper(ItemMoveCallback(this))
        touchHelper!!.attachToRecyclerView(recyclerView)

        startReorderDragListener = object : StartReorderDragListener {
            override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper?.startDrag(viewHolder)
            }
        }
    }

    override fun getActionMenuId() = R.menu.cab_clips

    override fun prepareActionMode(menu: Menu) {
        val selectedClips = getSelectedItems()
        val allPinned = selectedClips.isNotEmpty() && selectedClips.all { it.isPinned }
        menu.apply {
            findItem(R.id.cab_pin)?.apply {
                setTitle(if (allPinned) R.string.unpin_text else R.string.pin_text)
                setIcon(if (allPinned) R.drawable.ic_pin_outline_gray else R.drawable.ic_pin_cyan)
            }
            findItem(R.id.cab_edit).isVisible = isOneItemSelected()
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_pin -> togglePinSelected()
            R.id.cab_edit -> editClip()
            R.id.cab_delete -> askConfirmDelete()
        }
    }

    override fun getSelectableItemCount() = items.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = items.getOrNull(position)?.id?.toInt()

    override fun getItemKeyPosition(key: Int) = items.indexOfFirst { it.id?.toInt() == key }

    override fun onActionModeCreated() {
        notifyDataSetChanged()
    }

    override fun onActionModeDestroyed() {
        if (wasClipMoved) {
            wasClipMoved = false
            ensureBackgroundThread {
                val pinnedClips = items.filter { it.isPinned }
                activity.clipsDB.reorderPinnedClips(pinnedClips)
                activity.runOnUiThread {
                    notifyDataSetChanged()
                }
            }
        } else {
            notifyDataSetChanged()
        }
        wasClipMoved = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(ItemClipInActivityBinding.inflate(layoutInflater, parent, false).root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item, true, true) { itemView, layoutPosition ->
            setupView(itemView, item, holder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = items.size

    private fun editClip() {
        val selectedClip = getSelectedItems().firstOrNull() ?: return
        AddOrEditClipDialog(activity, selectedClip) {
            listener.refreshItems()
            finishActMode()
        }
    }

    private fun askConfirmDelete() {
        ConfirmationDialog(activity, "", R.string.proceed_with_deletion, R.string.yes, R.string.cancel) {
            deleteSelection()
        }
    }

    private fun deleteSelection() {
        val selectedClips = getSelectedItems()
        val pinnedClips = selectedClips.filter { it.isPinned }
        val unpinnedToDelete = selectedClips.filter { !it.isPinned }

        if (pinnedClips.isNotEmpty()) {
            activity.toast(R.string.cannot_delete_pinned)
        }

        if (unpinnedToDelete.isEmpty()) {
            finishActMode()
            return
        }

        val deletePositions = unpinnedToDelete.mapNotNull { clip ->
            val index = items.indexOf(clip)
            if (index != -1) index else null
        }

        items.removeAll(unpinnedToDelete)
        removeSelectedItems(ArrayList(deletePositions))

        ensureBackgroundThread {
            unpinnedToDelete.forEach { clip ->
                activity.clipsDB.delete(clip.id!!.toLong())
            }

            activity.runOnUiThread {
                listener.refreshItems()
                finishActMode()
            }
        }
    }

    private fun getSelectedItems() = items.filter { selectedKeys.contains(it.id!!.toInt()) } as ArrayList<Clip>

    private fun setupView(view: View, clip: Clip, holder: ViewHolder) {
        if (clip.id == null) {
            return
        }

        val isSelected = selectedKeys.contains(clip.id!!.toInt())
        ItemClipInActivityBinding.bind(view).apply {
            root.setupViewBackground(activity)
            clipValue.text = clip.value
            clipValue.setTextColor(textColor)
            clipDragHandle.applyColorFilter(textColor)

            clipPinBtn.apply {
                setImageResource(if (clip.isPinned) R.drawable.ic_pin_cyan else R.drawable.ic_pin_outline_gray)
                setOnClickListener {
                    togglePin(clip)
                }
            }

            clipDragHandle.beVisibleIf(clip.isPinned)
            clipHolder.isSelected = isSelected
            clipDragHandle.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN && clip.isPinned) {
                    startReorderDragListener.requestDrag(holder)
                }
                false
            }
        }
    }

    private fun togglePin(clip: Clip) {
        ensureBackgroundThread {
            val newPinned = !clip.isPinned
            if (clip.id != null) {
                activity.clipsDB.updatePinned(clip.id!!, newPinned)
            }
            activity.runOnUiThread {
                activity.toast(if (newPinned) R.string.text_pinned else R.string.text_unpinned)
                listener.refreshItems()
            }
        }
    }

    private fun togglePinSelected() {
        val selectedClips = getSelectedItems()
        val anyUnpinned = selectedClips.any { !it.isPinned }
        val targetPinState = anyUnpinned
        ensureBackgroundThread {
            selectedClips.forEach { clip ->
                if (clip.id != null) {
                    activity.clipsDB.updatePinned(clip.id!!, targetPinState)
                }
            }
            activity.runOnUiThread {
                activity.toast(if (targetPinState) R.string.text_pinned else R.string.text_unpinned)
                listener.refreshItems()
                finishActMode()
            }
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val fromClip = items.getOrNull(fromPosition)
        val toClip = items.getOrNull(toPosition)
        if (fromClip?.isPinned != true || toClip?.isPinned != true) {
            return
        }
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
        wasClipMoved = true
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {}

    override fun onRowClear(myViewHolder: ViewHolder?) {
        if (wasClipMoved) {
            wasClipMoved = false
            ensureBackgroundThread {
                val pinnedClips = items.filter { it.isPinned }
                activity.clipsDB.reorderPinnedClips(pinnedClips)
            }
        }
    }
}
