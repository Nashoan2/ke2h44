package org.fossify.keyboard.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.fossify.commons.dialogs.FilePickerDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.*
import org.fossify.commons.interfaces.RefreshRecyclerViewListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.fossify.keyboard.R
import org.fossify.keyboard.adapters.ClipsActivityAdapter
import org.fossify.keyboard.databinding.ActivityManageClipboardItemsBinding
import org.fossify.keyboard.dialogs.AddOrEditClipDialog
import org.fossify.keyboard.dialogs.ExportClipsDialog
import org.fossify.keyboard.extensions.clipsDB
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.helpers.ClipsHelper
import org.fossify.keyboard.models.Clip
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class ManageClipboardItemsActivity : SimpleActivity(), RefreshRecyclerViewListener {
    companion object {
        private const val PICK_EXPORT_CLIPS_INTENT = 21
        private const val PICK_IMPORT_CLIPS_SOURCE_INTENT = 22
    }

    private val binding by viewBinding(ActivityManageClipboardItemsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()
        updateClips()

        if (intent.getBooleanExtra("action_backup", false)) {
            exportClips()
        } else if (intent.getBooleanExtra("action_restore", false)) {
            importClips()
        }

        binding.apply {
            setupEdgeToEdge(padBottomSystem = listOf(clipboardItemsList))
            setupMaterialScrollListener(binding.clipboardItemsList, binding.clipboardAppbar)

            clipboardItemsPlaceholder.text = "${getText(R.string.manage_clipboard_empty)}\n\n${getText(R.string.manage_clips)}"
            clipboardItemsPlaceholder2.apply {
                underlineText()
                setTextColor(getProperPrimaryColor())
                setOnClickListener {
                    addOrEditClip()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.clipboardAppbar, NavigationIcon.Arrow)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_EXPORT_CLIPS_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            exportClipsTo(outputStream)
        } else if (requestCode == PICK_IMPORT_CLIPS_SOURCE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val inputStream = contentResolver.openInputStream(resultData.data!!)
            parseFile(inputStream)
        }
    }


    private fun setupOptionsMenu() {
        binding.clipboardToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_clipboard_item -> {
                    addOrEditClip()
                    true
                }

                R.id.export_clips -> {
                    exportClips()
                    true
                }

                R.id.import_clips -> {
                    importClips()
                    true
                }

                else -> false
            }
        }
    }

    override fun refreshItems() {
        updateClips()
    }

    private var clipsSwipeHelper: ItemTouchHelper? = null

    private fun updateClips() {
        ensureBackgroundThread {
            val clips = clipsDB.getClips().toMutableList() as ArrayList<Clip>
            runOnUiThread {
                ClipsActivityAdapter(this, clips, binding.clipboardItemsList, this) {
                    addOrEditClip(it as Clip)
                }.apply {
                    binding.clipboardItemsList.adapter = this
                }

                if (clipsSwipeHelper == null) {
                    val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                        ): Boolean = false

                        override fun getSwipeDirs(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder
                        ): Int {
                            val adapter = recyclerView.adapter as? ClipsActivityAdapter ?: return 0
                            val pos = viewHolder.bindingAdapterPosition
                            if (pos !in 0 until adapter.items.size) return 0
                            return ItemTouchHelper.LEFT
                        }

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val adapter = binding.clipboardItemsList.adapter as? ClipsActivityAdapter ?: return
                            val pos = viewHolder.bindingAdapterPosition
                            if (pos !in 0 until adapter.items.size) return
                            val clip = adapter.items[pos]
                            if (clip.isPinned) {
                                adapter.notifyItemChanged(pos)
                                toast(R.string.cannot_delete_pinned)
                                return
                            }
                            ensureBackgroundThread {
                                if (clip.id != null) {
                                    clipsDB.delete(clip.id!!.toLong())
                                }
                                runOnUiThread {
                                    updateClips()
                                }
                            }
                        }
                    }
                    clipsSwipeHelper = ItemTouchHelper(swipeCallback)
                    clipsSwipeHelper!!.attachToRecyclerView(binding.clipboardItemsList)
                }

                binding.apply {
                    clipboardItemsList.beVisibleIf(clips.isNotEmpty())
                    clipboardItemsPlaceholder.beVisibleIf(clips.isEmpty())
                    clipboardItemsPlaceholder2.beVisibleIf(clips.isEmpty())
                }
            }
        }
    }

    private fun addOrEditClip(clip: Clip? = null) {
        AddOrEditClipDialog(this, clip) {
            updateClips()
        }
    }

    data class BackupItem(val value: String, val isPinned: Boolean, val order: Int)

    private fun exportClips() {
        if (isQPlus()) {
            ExportClipsDialog(this, config.lastExportedClipsFolder, true) { path, filename ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, filename)
                    addCategory(Intent.CATEGORY_OPENABLE)

                    try {
                        startActivityForResult(this, PICK_EXPORT_CLIPS_INTENT)
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        showErrorToast(e)
                    }
                }
            }
        } else {
            handlePermission(PERMISSION_WRITE_STORAGE) {
                if (it) {
                    ExportClipsDialog(this, config.lastExportedClipsFolder, false) { path, filename ->
                        val file = File(path)
                        getFileOutputStream(file.toFileDirItem(this), true) {
                            exportClipsTo(it)
                        }
                    }
                }
            }
        }
    }

    private fun exportClipsTo(outputStream: OutputStream?) {
        if (outputStream == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        ensureBackgroundThread {
            val pinnedClips = clipsDB.getPinnedClips()
            val recentClips = clipsDB.getRecentClips()

            if (pinnedClips.isEmpty() && recentClips.isEmpty()) {
                toast(R.string.no_entries_for_exporting)
                return@ensureBackgroundThread
            }

            val clipsList = ArrayList<Map<String, Any>>()

            pinnedClips.forEachIndexed { index, clip ->
                clipsList.add(
                    mapOf(
                        "value" to clip.value,
                        "isPinned" to true,
                        "order" to index
                    )
                )
            }

            recentClips.forEachIndexed { index, clip ->
                clipsList.add(
                    mapOf(
                        "value" to clip.value,
                        "isPinned" to false,
                        "order" to index
                    )
                )
            }

            val backupMap = mapOf(
                "version" to 2,
                "exportedAt" to System.currentTimeMillis(),
                "totalCount" to clipsList.size,
                "clips" to clipsList
            )

            val json = GsonBuilder().setPrettyPrinting().create().toJson(backupMap)
            outputStream.bufferedWriter().use { out ->
                out.write(json)
            }

            toast(R.string.exporting_successful)
        }
    }

    private fun importClips() {
        if (isQPlus()) {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "application/json",
                        "text/json",
                        "text/plain",
                        "application/octet-stream"
                    )
                )

                try {
                    startActivityForResult(this, PICK_IMPORT_CLIPS_SOURCE_INTENT)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        } else {
            handlePermission(PERMISSION_READ_STORAGE) {
                if (it) {
                    FilePickerDialog(this) {
                        ensureBackgroundThread {
                            parseFile(File(it).inputStream())
                        }
                    }
                }
            }
        }
    }

    private fun parseFile(inputStream: InputStream?) {
        if (inputStream == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        var clipsImported = 0
        ensureBackgroundThread {
            try {
                val items = ArrayList<BackupItem>()
                val jsonElement = inputStream.bufferedReader().use { reader ->
                    JsonParser.parseReader(reader)
                }

                if (jsonElement.isJsonObject) {
                    val root = jsonElement.asJsonObject
                    if (root.has("clips") && root.get("clips").isJsonArray) {
                        val clipsArray = root.getAsJsonArray("clips")
                        for (elem in clipsArray) {
                            if (elem.isJsonObject) {
                                val o = elem.asJsonObject
                                val v = o.get("value")?.asString?.trim() ?: ""
                                val isPinned = o.get("isPinned")?.asBoolean ?: false
                                val order = o.get("order")?.asInt ?: items.size
                                if (v.isNotEmpty()) {
                                    items.add(BackupItem(v, isPinned, order))
                                }
                            } else if (elem.isJsonPrimitive) {
                                val v = elem.asString.trim()
                                if (v.isNotEmpty()) {
                                    items.add(BackupItem(v, false, items.size))
                                }
                            }
                        }
                    }
                } else if (jsonElement.isJsonArray) {
                    val array = jsonElement.asJsonArray
                    for (elem in array) {
                        if (elem.isJsonObject) {
                            val o = elem.asJsonObject
                            val v = o.get("value")?.asString?.trim() ?: ""
                            val isPinned = o.get("isPinned")?.asBoolean ?: false
                            val order = o.get("order")?.asInt ?: items.size
                            if (v.isNotEmpty()) {
                                items.add(BackupItem(v, isPinned, order))
                            }
                        } else if (elem.isJsonPrimitive) {
                            val v = elem.asString.trim()
                            if (v.isNotEmpty()) {
                                items.add(BackupItem(v, false, items.size))
                            }
                        }
                    }
                }

                if (items.isEmpty()) {
                    runOnUiThread {
                        toast(R.string.no_new_entries_for_importing)
                    }
                    return@ensureBackgroundThread
                }

                val pinnedItems = items.filter { it.isPinned }.sortedBy { it.order }
                val recentItems = items.filter { !it.isPinned }.sortedBy { it.order }

                // The database queries clips with `ORDER BY id DESC`.
                // To preserve the exact visual order where order=0 is at the top,
                // item 0 must receive a higher ID than item 1, which must be higher than item 2, etc.
                // Therefore, we insert them in REVERSE order (from bottom to top),
                // so that the top-most item (index 0) is inserted last and gets the highest auto-increment ID!

                for (i in pinnedItems.indices.reversed()) {
                    val item = pinnedItems[i]
                    clipsDB.deleteClipWithValue(item.value)
                    val clip = Clip(id = null, value = item.value, isPinned = true)
                    val newId = clipsDB.insertOrUpdate(clip)
                    if (newId > 0) {
                        clipsImported++
                    }
                }

                for (i in recentItems.indices.reversed()) {
                    val item = recentItems[i]
                    clipsDB.deleteClipWithValue(item.value)
                    val clip = Clip(id = null, value = item.value, isPinned = false)
                    val newId = clipsDB.insertOrUpdate(clip)
                    if (newId > 0) {
                        clipsImported++
                    }
                }

                clipsDB.trimRecentClips()

                runOnUiThread {
                    val msg = if (clipsImported > 0) R.string.importing_successful else R.string.no_new_entries_for_importing
                    toast(msg)
                    updateClips()
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}
