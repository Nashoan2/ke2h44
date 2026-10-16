package org.fossify.keyboard.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import org.fossify.keyboard.dialogs.KeyboardResizeDialog
import org.fossify.keyboard.services.SimpleKeyboardIME
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import org.fossify.keyboard.extensions.KeyboardThemeData
import org.fossify.keyboard.extensions.getCurrentKeyboardTheme
import org.fossify.keyboard.helpers.THEME_HONEY_3D
import org.fossify.keyboard.helpers.THEME_IOS_DARK
import org.fossify.keyboard.helpers.THEME_NEON_CYBER
import org.fossify.keyboard.helpers.THEME_PIANO_3D
import org.fossify.keyboard.helpers.THEME_RETRO_1995
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.inline.InlineContentView
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.updateMarginsRelative
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiCompat.EMOJI_SUPPORTED
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beGoneIf
import org.fossify.commons.extensions.beInvisibleIf
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.darkenColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.isDynamicTheme
import org.fossify.commons.extensions.lightenColor
import org.fossify.commons.extensions.removeUnderlines
import org.fossify.commons.extensions.toast
import org.fossify.commons.helpers.FontHelper
import org.fossify.commons.helpers.HIGHER_ALPHA
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.helpers.isPiePlus
import org.fossify.keyboard.R
import org.fossify.keyboard.activities.ManageClipboardItemsActivity
import org.fossify.keyboard.activities.SettingsActivity
import org.fossify.keyboard.adapters.ClipsKeyboardAdapter
import org.fossify.keyboard.adapters.EmojisAdapter
import org.fossify.keyboard.databinding.ItemEmojiCategoryBinding
import org.fossify.keyboard.databinding.KeyboardKeyPreviewBinding
import org.fossify.keyboard.databinding.KeyboardPopupKeyboardBinding
import org.fossify.keyboard.databinding.KeyboardViewKeyboardBinding
import org.fossify.keyboard.dialogs.SwitchLanguageDialog
import org.fossify.keyboard.extensions.clipsDB
import org.fossify.keyboard.extensions.config
import org.fossify.keyboard.extensions.getCurrentClip
import org.fossify.keyboard.extensions.getCurrentVoiceInputMethod
import org.fossify.keyboard.extensions.getKeyboardBackgroundColor
import org.fossify.keyboard.extensions.getStrokeColor
import org.fossify.keyboard.extensions.isDeviceLocked
import org.fossify.keyboard.extensions.onScroll
import org.fossify.keyboard.extensions.safeStorageContext
import org.fossify.keyboard.helpers.AccessHelper
import org.fossify.keyboard.helpers.EMOJI_SPEC_FILE_PATH
import org.fossify.keyboard.helpers.EmojiData
import org.fossify.keyboard.helpers.EmbeddedVoiceManager
import org.fossify.keyboard.helpers.KeyboardFeedbackManager
import org.fossify.keyboard.helpers.LANGUAGE_ARABIC
import org.fossify.keyboard.helpers.LANGUAGE_TURKISH_Q
import org.fossify.keyboard.helpers.ClipsHelper
import org.fossify.keyboard.helpers.MAX_KEYS_PER_MINI_ROW
import org.fossify.keyboard.helpers.MyKeyboard
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_DELETE
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_EMOJI_OR_LANGUAGE
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_ENTER
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_MODE_CHANGE
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_POPUP_EMOJI
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_POPUP_SETTINGS
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_SHIFT
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_SPACE
import org.fossify.keyboard.helpers.MyKeyboard.Companion.KEYCODE_SYMBOLS_MODE_CHANGE
import org.fossify.keyboard.helpers.RECENTLY_USED_EMOJIS
import org.fossify.keyboard.helpers.ShiftState
import org.fossify.keyboard.helpers.getCategoryIconRes
import org.fossify.keyboard.helpers.parseRawEmojiSpecsFile
import org.fossify.keyboard.interfaces.OnKeyboardActionListener
import org.fossify.keyboard.interfaces.RefreshClipsListener
import org.fossify.keyboard.models.Clip
import org.fossify.keyboard.models.ClipsSectionLabel
import org.fossify.keyboard.models.ListItem
import java.util.Arrays
import java.util.Locale

@SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
class MyKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleRes) {

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        return if (accessHelper?.dispatchHoverEvent(event) == true) {
            true
        } else {
            super.dispatchHoverEvent(event)
        }
    }

    private var keyboardPopupBinding: KeyboardPopupKeyboardBinding? = null
    private var keyboardViewBinding: KeyboardViewKeyboardBinding? = null

    private var accessHelper: AccessHelper? = null
    private val feedbackManager by lazy { KeyboardFeedbackManager(context) }
    private var clipsTouchHelper: ItemTouchHelper? = null

    private var mKeyboard: MyKeyboard? = null
    private var mCurrentKeyIndex: Int = NOT_A_KEY

    private var mLabelTextSize = 0
    private var mKeyTextSize = 0
    private var mSpaceBarTextSize = 0

    private var mTextColor = 0
    private var mBackgroundColor = 0
    private var mKeyboardBackgroundColor = 0
    private var mPrimaryColor = 0
    private var mStrokeColor = 0
    private var mKeyColor = 0
    private var mKeyColorPressed = 0

    private var mPreviewText: TextView? = null
    private val mPreviewPopup: PopupWindow
    private var mPreviewTextSizeLarge = 0
    private var mPreviewHeight = 0

    private val mCoordinates = IntArray(2)
    private val mPopupKeyboard: PopupWindow
    private var mMiniKeyboardContainer: View? = null
    private var mMiniKeyboard: MyKeyboardView? = null
    private var mMiniKeyboardOnScreen = false
    private var mPopupParent: View
    private var mMiniKeyboardOffsetX = 0
    private var mMiniKeyboardOffsetY = 0
    private val mMiniKeyboardCache: MutableMap<MyKeyboard.Key, View?>
    private var mKeys = ArrayList<MyKeyboard.Key>()
    private var mMiniKeyboardSelectedKeyIndex = -1

    var mOnKeyboardActionListener: OnKeyboardActionListener? = null
    private var mVerticalCorrection = 0
    private var mProximityThreshold = 0
    private var mPopupPreviewX = 0
    private var mPopupPreviewY = 0
    private var mLastX = 0
    private var mLastY = 0

    private val mPaint: Paint
    private var mDownTime = 0L
    private var mLastMoveTime = 0L
    private var mLastKey = 0
    private var mLastCodeX = 0
    private var mLastCodeY = 0
    private var mLastKeyPressedCode = 0
    private var mCurrentKey: Int = NOT_A_KEY
    private var mLastKeyTime = 0L
    private var mCurrentKeyTime = 0L
    private val mKeyIndices = IntArray(12)
    private var mPopupX = 0
    private var mPopupY = 0
    private var mRepeatKeyIndex = NOT_A_KEY
    private var mRepeatCount = 0
    private var mPopupLayout = 0
    private var mAbortKey = false
    private var mCursorControlActive = false
    private var mLastSpaceMoveX = 0
    private var mPopupMaxMoveDistance = 0f
    private var mTopSmallNumberSize = 0f
    private var mTopSmallNumberMarginWidth = 0f
    private var mTopSmallNumberMarginHeight = 0f
    private val mSpaceMoveThreshold: Int
    private var ignoreTouches = false

    private var mKeyBackground: Drawable? = null
    private var mSpaceKeyBackground: Drawable? = null
    private var mArKeyBackground: Drawable? = null
    private var mShowKeyBorders: Boolean = false
    private var mUsingSystemTheme: Boolean = true
    private var mVoiceInputMethod: String = ""
    private val mKey3DShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mKey3DFacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mKey3DHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val mKey3DTopShinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val mKey3DShadowRect = RectF()
    private val mKey3DFaceRect = RectF()

    private var mToolbarHolder: View? = null
    private var mClipboardManagerHolder: View? = null
    private var mEmojiPaletteHolder: View? = null
    private var mToolsMenuHolder: View? = null
    private var mTextEditorHolder: View? = null
    private var emojiCompatMetadataVersion = 0

    // For multi-tap
    private var mLastTapTime = 0L

    /** Whether the keyboard bitmap needs to be redrawn before it's blitted.  */
    private var mDrawPending = false

    /** The dirty region in the keyboard bitmap  */
    private val mDirtyRect = Rect()

    /** The keyboard bitmap for faster updates  */
    private var mBuffer: Bitmap? = null

    /** Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer.  */
    private var mKeyboardChanged = false

    /** The canvas for the above mutable keyboard bitmap  */
    private var mCanvas: Canvas? = null

    private var mHandler: Handler? = null

    private val mVoiceManager = EmbeddedVoiceManager(context)

    companion object {
        private const val NOT_A_KEY = -1
        private const val REPEAT_KEY_DELETE_SPECIAL = -2
        private val LONG_PRESSABLE_STATE_SET = intArrayOf(R.attr.state_long_pressable)
        private const val MSG_REMOVE_PREVIEW = 1
        private const val MSG_REPEAT = 2
        private const val MSG_LONGPRESS = 3
        private const val DELAY_AFTER_PREVIEW = 100
        private const val DEBOUNCE_TIME = 70
        private const val REPEAT_INTERVAL = 45
        private const val REPEAT_START_DELAY = 280
        private const val DELETE_REPEAT_START_DELAY = 160
        private val LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout()
        private const val KEY_3D_MIN_DEPTH_PX = 5.5f
        private const val KEY_3D_MAX_DEPTH_PX = 9.5f
    }

    init {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.MyKeyboardView, 0, defStyleRes)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val keyTextSize = 0
        val indexCnt = attributes.indexCount

        try {
            for (i in 0 until indexCnt) {
                when (val attr = attributes.getIndex(i)) {
                    R.styleable.MyKeyboardView_keyTextSize -> mKeyTextSize =
                        attributes.getDimensionPixelSize(attr, 18)
                }
            }
        } finally {
            attributes.recycle()
        }

        mPopupLayout = R.layout.keyboard_popup_keyboard
        mKeyBackground = resources.getDrawable(R.drawable.keyboard_key_selector, context.theme)
        mSpaceKeyBackground = resources.getDrawable(R.drawable.keyboard_space_background, context.theme)
        mVerticalCorrection = resources.getDimension(R.dimen.vertical_correction).toInt()
        mLabelTextSize = resources.getDimension(R.dimen.label_text_size).toInt()
        mSpaceBarTextSize = resources.getDimension(R.dimen.space_bar_text_size).toInt()
        mPreviewHeight = resources.getDimension(R.dimen.key_height).toInt()
        mSpaceMoveThreshold = resources.getDimension(R.dimen.medium_margin).toInt()

        with(safeStorageContext) {
            mTextColor = getProperTextColor()
            mBackgroundColor = getProperBackgroundColor()
            mKeyboardBackgroundColor = getKeyboardBackgroundColor()
            mPrimaryColor = getProperPrimaryColor()
            mStrokeColor = getStrokeColor()
        }

        mPreviewPopup = PopupWindow(context)
        mPreviewText = KeyboardKeyPreviewBinding.inflate(inflater).root
        mPreviewTextSizeLarge = context.resources.getDimension(R.dimen.preview_text_size).toInt()
        mPreviewPopup.contentView = mPreviewText
        mPreviewPopup.setBackgroundDrawable(null)

        mPreviewPopup.isTouchable = false
        mPopupKeyboard = PopupWindow(context)
        mPopupKeyboard.setBackgroundDrawable(null)
        mPopupParent = this
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.textSize = keyTextSize.toFloat()
        mPaint.textAlign = Align.CENTER
        mPaint.alpha = 255
        mMiniKeyboardCache = HashMap()
        mPopupMaxMoveDistance = resources.getDimension(R.dimen.popup_max_move_distance)
        mTopSmallNumberSize = resources.getDimension(R.dimen.small_text_size)
        mTopSmallNumberMarginWidth = resources.getDimension(R.dimen.top_small_number_margin_width)
        mTopSmallNumberMarginHeight = resources.getDimension(R.dimen.top_small_number_margin_height)
    }

    @SuppressLint("HandlerLeak")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mHandler == null) {
            mHandler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MSG_REMOVE_PREVIEW -> mPreviewText!!.visibility = INVISIBLE
                        MSG_REPEAT -> if (repeatKey()) {
                            val repeat = Message.obtain(this, MSG_REPEAT)
                            sendMessageDelayed(repeat, getNextRepeatDelay())
                        }

                        MSG_LONGPRESS -> openPopupIfRequired(msg.obj as MotionEvent)
                    }
                }
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        closeClipboardManager()
        closeEmojiPalette()

        if (visibility == VISIBLE) {
            setupKeyboard(changedView)
        }
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the view will re-layout itself to accommodate the keyboard.
     * @param keyboard the keyboard to display in this view
     */
    fun setKeyboard(keyboard: MyKeyboard) {
        if (mKeyboard != null) {
            showPreview(NOT_A_KEY)
        }

        closeClipboardManager()
        removeMessages()
        mKeyboard = keyboard
        if (width > 0 && mKeyboard != null && mKeyboard!!.mDisplayWidth != width) {
            mKeyboard!!.scaleToWidth(width)
        }
        val keys = mKeyboard!!.mKeys
        mKeys = keys!!.toMutableList() as ArrayList<MyKeyboard.Key>
        requestLayout()
        mKeyboardChanged = true
        invalidateAllKeys()
        computeProximityThreshold(keyboard)
        mMiniKeyboardCache.clear()
        mToolbarHolder?.beVisible()

        accessHelper = AccessHelper(this, mKeyboard?.mKeys.orEmpty())
        ViewCompat.setAccessibilityDelegate(this, accessHelper)

        // Not really necessary to do every time, but will free up views
        // Switching to a different keyboard should abort any pending keys so that the key up
        // doesn't get delivered to the old or new keyboard
        mAbortKey = true // Until the next ACTION_DOWN
    }

    /** Sets the top row above the keyboard containing a couple buttons and the clipboard **/
    fun setKeyboardHolder(binding: KeyboardViewKeyboardBinding) {
        keyboardViewBinding = binding.apply {
            mToolbarHolder = toolbarHolder
            mClipboardManagerHolder = clipboardManagerHolder
            mEmojiPaletteHolder = emojiPaletteHolder
            mToolsMenuHolder = toolsMenuHolder
            mTextEditorHolder = textEditorHolder

            toolbarSticker.setOnClickListener {
                if (toolsMenuHolder.visibility == View.VISIBLE) {
                    closeAllPanels()
                } else {
                    openToolsMenu()
                }
            }

            toolbarEmoji.setOnClickListener {
                if (emojiPaletteHolder.visibility == View.VISIBLE) {
                    closeAllPanels()
                } else {
                    openEmojiPalette()
                }
            }

            voiceInputButton.setOnLongClickListener { context.toast(R.string.switch_to_voice_typing); true }
            voiceInputButton.setOnClickListener {
                if (mVoiceManager.isListening) {
                    mVoiceManager.stopListening()
                } else {
                    val started = mVoiceManager.startListening(
                        onListeningState = { listening ->
                            setVoiceListeningState(listening)
                        },
                        onResult = { text ->
                            mOnKeyboardActionListener?.onText("$text ")
                        }
                    )
                    if (!started && !mVoiceManager.hasPermission()) {
                        mVoiceManager.requestPermission()
                    }
                }
            }

            toolbarCursor.setOnClickListener {
                if (textEditorHolder.visibility == View.VISIBLE) {
                    closeAllPanels()
                } else {
                    openTextEditor()
                }
            }

            pinnedClipboardItems.setOnLongClickListener { context.toast(R.string.clipboard); true; }
            pinnedClipboardItems.setOnClickListener {
                if (clipboardManagerHolder.visibility == View.VISIBLE) {
                    closeAllPanels()
                } else {
                    openClipboardManager()
                }
            }

            toolbarCollapse.setOnClickListener {
                vibrateIfNeeded()
                closeAllPanels()
                (context as? android.inputmethodservice.InputMethodService)?.requestHideSelf(0)
            }

            toolbarSettingsShortcut.setOnClickListener {
                vibrateIfNeeded()
                Intent(context, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(this)
                }
            }

            // Tools Menu actions (Image 90)
            toolsMenuClose.setOnClickListener {
                vibrateIfNeeded()
                closeAllPanels()
            }

            toolsMenuSettings.setOnClickListener {
                vibrateIfNeeded()
                Intent(context, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(this)
                }
            }

            toolItemEditor.setOnClickListener {
                vibrateIfNeeded()
                openTextEditor()
            }

            toolItemResize.setOnClickListener {
                vibrateIfNeeded()
                openResizeMode()
            }

            toolItemTheme.setOnClickListener {
                vibrateIfNeeded()
                Intent(context, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(this)
                }
            }

            toolItemLanguages.setOnClickListener {
                vibrateIfNeeded()
                SwitchLanguageDialog(this@MyKeyboardView) {
                    mOnKeyboardActionListener?.reloadKeyboard()
                }
            }

            // Text Editor / Cursor panel actions
            cursorBtnAbc.setOnClickListener {
                vibrateIfNeeded()
                closeAllPanels()
            }
            cursorBtnAbcHolder.setOnClickListener {
                vibrateIfNeeded()
                closeAllPanels()
            }

            cursorBtnClipboard.setOnClickListener {
                vibrateIfNeeded()
                openClipboardManager()
            }

            cursorBtnUp.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.moveCursorUp()
            }

            cursorBtnDown.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.moveCursorDown()
            }

            cursorBtnLeft.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.moveCursorLeft()
            }

            cursorBtnRight.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.moveCursorRight()
            }

            cursorBtnSelect.setOnClickListener {
                vibrateIfNeeded()
                val active = mOnKeyboardActionListener?.toggleSelectMode() ?: false
                cursorBtnSelect.setTextColor(if (active) Color.parseColor("#00E676") else Color.parseColor("#00BCD4"))
            }

            cursorBtnSelectAll.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.selectAll()
            }

            cursorBtnCopy.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.copyText()
            }

            cursorBtnCut.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.cutText()
            }

            cursorBtnPaste.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.pasteText()
            }

            cursorBtnEnd.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.moveToEnd()
            }
            cursorBtnEnd.setOnLongClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.moveToStart()
                true
            }

            cursorBtnBackspace.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        cursorBtnBackspace.isPressed = true
                        mRepeatKeyIndex = REPEAT_KEY_DELETE_SPECIAL
                        mCurrentKey = NOT_A_KEY
                        mRepeatCount = 0
                        vibrateIfNeeded()
                        mOnKeyboardActionListener?.onKey(KEYCODE_DELETE)
                        val msg = mHandler?.obtainMessage(MSG_REPEAT)
                        if (msg != null) mHandler?.sendMessageDelayed(msg, DELETE_REPEAT_START_DELAY.toLong())
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        resetRepeat()
                        cursorBtnBackspace.isPressed = false
                        false
                    }
                    else -> false
                }
            }

            cursorBtnSpace.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.insertSpace()
            }

            cursorBtnUndo.setOnClickListener {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.undo()
            }

            // Numeric Keypad actions (Matches image 100%)
            fun sendNumericChar(char: String) {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.onText(char)
            }

            fun sendNumericCode(code: Int) {
                vibrateIfNeeded()
                mOnKeyboardActionListener?.onKey(code)
            }

            numKeyPlus.setOnClickListener { sendNumericChar("+") }
            numKey1.setOnClickListener { sendNumericChar("1") }
            numKey2.setOnClickListener { sendNumericChar("2") }
            numKey3.setOnClickListener { sendNumericChar("3") }
            numKeyPercent.setOnClickListener { sendNumericChar("%") }

            numKeyMinus.setOnClickListener { sendNumericChar("-") }
            numKey4.setOnClickListener { sendNumericChar("4") }
            numKey5.setOnClickListener { sendNumericChar("5") }
            numKey6.setOnClickListener { sendNumericChar("6") }
            numKeySpace.setOnClickListener { sendNumericCode(KEYCODE_SPACE) }

            numKeyMultiply.setOnClickListener { sendNumericChar("*") }
            numKey7.setOnClickListener { sendNumericChar("7") }
            numKey8.setOnClickListener { sendNumericChar("8") }
            numKey9.setOnClickListener { sendNumericChar("9") }

            numKeyBackspace.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        numKeyBackspace.isPressed = true
                        mRepeatKeyIndex = REPEAT_KEY_DELETE_SPECIAL
                        mCurrentKey = NOT_A_KEY
                        mRepeatCount = 0
                        vibrateIfNeeded()
                        mOnKeyboardActionListener?.onKey(KEYCODE_DELETE)
                        val msg = mHandler?.obtainMessage(MSG_REPEAT)
                        if (msg != null) mHandler?.sendMessageDelayed(msg, DELETE_REPEAT_START_DELAY.toLong())
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        resetRepeat()
                        numKeyBackspace.isPressed = false
                        false
                    }
                    else -> false
                }
            }

            numKeyDivide.setOnClickListener { sendNumericChar("/") }
            numKeyAbc.setOnClickListener {
                vibrateIfNeeded()
                closeNumericKeypad()
            }
            numKeySymbols.setOnClickListener {
                vibrateIfNeeded()
                closeNumericKeypad()
                mOnKeyboardActionListener?.onKey(MyKeyboard.KEYCODE_SYMBOLS_MODE_CHANGE)
            }
            numKey0.setOnClickListener { sendNumericChar("0") }
            numKeyEquals.setOnClickListener { sendNumericChar("=") }
            numKeyDot.setOnClickListener { sendNumericChar(".") }
            numKeyEnter.setOnClickListener { sendNumericCode(KEYCODE_ENTER) }

            settingsCog.setOnLongClickListener { context.toast(R.string.settings); true; }
            settingsCog.setOnClickListener {
                vibrateIfNeeded()
                Intent(context, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(this)
                }
            }

            clipboardClear.setOnLongClickListener { context.toast(R.string.clear_clipboard_data); true; }
            clipboardClear.setOnClickListener {
                vibrateIfNeeded()
                clearClipboardContent()
                toggleClipboardVisibility(false)
            }

            suggestionsHolder.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    updateSuggestionsToolbarLayout()
                    binding.suggestionsHolder.removeOnLayoutChangeListener(this)
                }
            })
        }



        binding.apply {
            emojiPaletteClose.setOnClickListener {
                vibrateIfNeeded()
                closeEmojiPalette()
            }
        }

        setupResizeOverlay()
    }

    fun setEditorInfo(editorInfo: EditorInfo) {
        emojiCompatMetadataVersion =
            editorInfo.extras?.getInt(EmojiCompat.EDITOR_INFO_METAVERSION_KEY, 0) ?: 0
    }

    fun setupKeyboard(changedView: View? = null) {
        val currentTheme = context.getCurrentKeyboardTheme()
        with(safeStorageContext) {
            mTextColor = currentTheme.normalTextColor
            mBackgroundColor = currentTheme.keyboardBgColor
            mKeyboardBackgroundColor = currentTheme.keyboardBgColor
            mPrimaryColor = currentTheme.toolbarIconColor
            mStrokeColor = currentTheme.normalKeyHighlightColor

            mShowKeyBorders = true
            mUsingSystemTheme = false
            mVoiceInputMethod = config.voiceInputMethod
        }

        val isMainKeyboard = changedView == null || changedView.id != R.id.mini_keyboard_view
        mKeyColor = currentTheme.normalKeyBottomColor
        mKeyColorPressed = currentTheme.normalKeyPressedColor
        mShowKeyBorders = true
        mKeyBackground = resources.getDrawable(R.drawable.keyboard_key_selector_outlined, context.theme)
        mSpaceKeyBackground = resources.getDrawable(R.drawable.keyboard_space_background, context.theme)
        mArKeyBackground = resources.getDrawable(R.drawable.bg_key_ar, context.theme)

        if (!isMainKeyboard) {
            val previewBackground = background as? LayerDrawable
            previewBackground?.findDrawableByLayerId(R.id.button_background_shape)
                ?.applyColorFilter(mBackgroundColor)
            previewBackground?.findDrawableByLayerId(R.id.button_background_stroke)
                ?.applyColorFilter(mStrokeColor)
            if (previewBackground != null) {
                background = previewBackground
            }
        } else {
            background = ColorDrawable(currentTheme.keyboardBgColor)
        }

        keyboardViewBinding?.apply {
            topKeyboardDivider.beGone()
            mToolbarHolder?.layoutDirection = View.LAYOUT_DIRECTION_LTR
            toolbarIconsContainer.layoutDirection = View.LAYOUT_DIRECTION_LTR
            mToolbarHolder?.background = resources.getDrawable(R.drawable.bg_toolbar_neon, context.theme)

            val toolbarIconColor = currentTheme.toolbarIconColor
            toolbarSticker.applyColorFilter(toolbarIconColor)
            toolbarEmoji.applyColorFilter(toolbarIconColor)
            if (mVoiceManager.isListening) {
                voiceInputActiveBg.visibility = View.VISIBLE
                voiceInputButton.applyColorFilter(Color.parseColor("#FF4444"))
            } else {
                voiceInputActiveBg.visibility = View.GONE
                voiceInputButton.applyColorFilter(toolbarIconColor)
            }
            toolbarCursor.applyColorFilter(toolbarIconColor)
            pinnedClipboardItems.applyColorFilter(toolbarIconColor)
            toolbarSettingsShortcut.applyColorFilter(toolbarIconColor)
            toolbarCollapse.clearColorFilter()

            mToolbarHolder?.beVisible()

            topClipboardDivider.beGone()
            clipboardManagerHolder.background = ColorDrawable(currentTheme.keyboardBgColor)
            clipboardManagerHolder.layoutDirection = View.LAYOUT_DIRECTION_LTR

            val isArabic = context.config.keyboardLanguage == LANGUAGE_ARABIC
            val closeLabel = if (isArabic) "أ ب ج" else "ABC"
            toolsMenuClose.text = closeLabel
            cursorBtnAbc.text = closeLabel

            clipboardContentPlaceholder1.setTextColor(Color.parseColor("#80A0C0"))
            clipboardContentPlaceholder2.setTextColor(Color.parseColor("#507090"))
        }

        setupEmojiPalette(
            toolbarColor = currentTheme.toolbarBgColor,
            backgroundColor = currentTheme.keyboardBgColor,
            textColor = currentTheme.normalTextColor
        )
        setupStoredClips()
    }

    fun vibrateIfNeeded() {
        // Disabled completely per user request
    }

    fun performKeypressFeedback(keyCode: Int) {
        feedbackManager.performKeypressFeedback(this, keyCode)
    }

    fun performHapticHandleMove() {
        // Disabled completely per user request
    }

    /**
     * Sets the state of the shift key of the keyboard, if any.
     * @param shifted whether or not to enable the state of the shift key
     * @return true if the shift key state changed, false if there was no change
     */
    private fun setShifted(shiftState: ShiftState) {
        if (mKeyboard?.setShifted(shiftState) == true) {
            invalidateAllKeys()
        }
    }

    /**
     * Returns the state of the shift key of the keyboard, if any.
     * @return true if the shift is in a pressed state, false otherwise
     */
    private fun isShifted(): Boolean {
        return (mKeyboard?.mShiftState ?: ShiftState.OFF) > ShiftState.OFF
    }

    private fun setPopupOffset(x: Int, y: Int) {
        mMiniKeyboardOffsetX = x
        mMiniKeyboardOffsetY = y
        if (mPreviewPopup.isShowing) {
            mPreviewPopup.dismiss()
        }
    }

    private fun adjustCase(label: CharSequence): CharSequence? {
        var newLabel: CharSequence? = label
        if (
            !newLabel.isNullOrEmpty()
            && mKeyboard!!.mShiftState != ShiftState.OFF
            && newLabel.length < 3
            && Character.isLowerCase(newLabel[0])
        ) {
            if (context.config.keyboardLanguage == LANGUAGE_TURKISH_Q) {
                newLabel = newLabel.toString().uppercase(Locale.forLanguageTag("tr"))
            } else {
                newLabel = newLabel.toString().uppercase(Locale.getDefault())
            }
        }
        return newLabel
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mKeyboard == null) {
            val defaultH = (resources.getDimension(R.dimen.key_height) * 4).toInt()
            val w = MeasureSpec.getSize(widthMeasureSpec).let { if (it > 0) it else resources.displayMetrics.widthPixels }
            setMeasuredDimension(w, defaultH)
        } else {
            var width = mKeyboard!!.mMinWidth
            val specSize = MeasureSpec.getSize(widthMeasureSpec)
            if (specSize > 0) {
                width = specSize
            }
            setMeasuredDimension(width, mKeyboard!!.mHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && (w != oldw || h != oldh)) {
            mKeyboard?.scaleToWidth(w)
            mKeyboard?.mKeys?.let {
                mKeys = it.toMutableList() as ArrayList<MyKeyboard.Key>
            }
            mKeyboard?.let { computeProximityThreshold(it) }
            mBuffer?.recycle()
            mBuffer = null
            mCanvas = null
            mKeyboardChanged = true
            mDirtyRect.set(0, 0, w, h)
            mDrawPending = true
            invalidate()
        }
    }

    /**
     * Compute the average distance between adjacent keys (horizontally and vertically) and square it to get the proximity threshold. We use a square here and
     * in computing the touch distance from a key's center to avoid taking a square root.
     * @param keyboard
     */
    private fun computeProximityThreshold(keyboard: MyKeyboard?) {
        if (keyboard == null) {
            return
        }

        val keys = mKeys
        val length = keys.size
        var dimensionSum = 0
        for (i in 0 until length) {
            val key = keys[i]
            dimensionSum += Math.min(key.width, key.height) + key.gap
        }

        if (dimensionSum < 0 || length == 0) {
            return
        }

        mProximityThreshold = (dimensionSum * 1.4f / length).toInt()
        mProximityThreshold *= mProximityThreshold // Square it
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bufferNeedsUpdate = mBuffer == null || mBuffer!!.width != width || mBuffer!!.height != height
        if (mDrawPending || bufferNeedsUpdate || mKeyboardChanged) {
            onBufferDraw()
        }
        mBuffer?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun onBufferDraw() {
        if (width <= 0 || height <= 0) {
            return
        }

        if (mBuffer == null || mBuffer!!.width != width || mBuffer!!.height != height) {
            val w = Math.max(1, width)
            val h = Math.max(1, height)
            mBuffer?.recycle()
            mBuffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBuffer!!)
            mKeyboardChanged = false
            mDirtyRect.set(0, 0, width, height)
            mDrawPending = true
        } else if (mKeyboardChanged) {
            mKeyboardChanged = false
            mDirtyRect.set(0, 0, width, height)
            mDrawPending = true
        }

        if (mKeyboard == null) {
            return
        }

        if (mDirtyRect.isEmpty) {
            mDirtyRect.set(0, 0, width, height)
        }

        mCanvas!!.save()
        val canvas = mCanvas
        canvas!!.clipRect(mDirtyRect)
        val paint = mPaint
        val keys = mKeys
        paint.color = mTextColor
        val customTypeface = FontHelper.getTypeface(context)
        val smallLetterPaint = Paint().apply {
            set(paint)
            color = paint.color.adjustAlpha(0.8f)
            textSize = mTopSmallNumberSize
            typeface = customTypeface
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        handleClipboard()

        val keyCount = keys.size
        for (i in 0 until keyCount) {
            val key = keys[i]
            val code = key.code
            val label = adjustCase(key.label)?.toString()

            val currentTheme = context.getCurrentKeyboardTheme()
            val density = resources.displayMetrics.density
            val depth = (currentTheme.elevationDp * density).coerceIn(KEY_3D_MIN_DEPTH_PX, KEY_3D_MAX_DEPTH_PX)
            val marginTop = 2.5f * density
            val marginBottom = 3.5f * density
            val pressOffset = if (key.pressed) depth - 1.2f * density else 0f
            val faceCenterY = if (key.pressed) {
                (marginTop + pressOffset + key.height - marginBottom - 1.2f * density) / 2f
            } else {
                (marginTop + key.height - marginBottom - depth) / 2f
            }

            setupKeyBackground(key, code, canvas)

            val isArKey = code == KEYCODE_EMOJI_OR_LANGUAGE || label == "AR" || label == "EN"
            val isEnter = code == KEYCODE_ENTER
            val isDelete = code == KEYCODE_DELETE
            val isAccent = isArKey || isDelete || code in arrayOf(
                KEYCODE_SHIFT, KEYCODE_MODE_CHANGE, KEYCODE_SYMBOLS_MODE_CHANGE,
                KEYCODE_POPUP_EMOJI, KEYCODE_POPUP_SETTINGS
            ) || label == "123" || label == "?123"

            val textColor = when {
                isEnter -> if (key.pressed) currentTheme.enterIconColor.adjustAlpha(0.85f) else currentTheme.enterIconColor
                isDelete -> if (key.pressed) currentTheme.deleteIconColor.adjustAlpha(0.85f) else currentTheme.deleteIconColor
                isAccent -> if (key.pressed) currentTheme.accentTextColor.adjustAlpha(0.85f) else currentTheme.accentTextColor
                else -> if (key.pressed) currentTheme.normalTextPressedColor else currentTheme.normalTextColor
            }

            // Switch the character to uppercase if shift is pressed
            if (label?.isNotEmpty() == true) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.isAntiAlias = true
                paint.isFakeBoldText = true
                paint.isSubpixelText = true

                if (label == "123" || label == "?123" || label == "AR" || label == "EN" || label == "لا") {
                    paint.textSize = mLabelTextSize.toFloat()
                } else if (code == KEYCODE_SPACE) {
                    paint.textSize = mSpaceBarTextSize.toFloat()
                } else {
                    paint.textSize = mKeyTextSize.toFloat()
                }

                paint.color = textColor
                paint.setShadowLayer(2f * density, 0f, 1.2f * density, Color.argb(140, 0, 0, 0))

                val rows = label.split("\n")
                val textSize = paint.textSize
                val startY = faceCenterY + ((textSize - paint.descent()) / 2f) - ((textSize / 2f) * (rows.size - 1))
                rows.forEachIndexed { index, row ->
                    canvas.drawText(row, key.width / 2f, startY + textSize * index, paint)
                }

                if (
                    key.topSmallNumber.isNotEmpty()
                    && !(context.config.showNumbersRow && Regex("\\d").matches(key.topSmallNumber))
                ) {
                    val bounds = Rect().also {
                        smallLetterPaint.getTextBounds(
                            key.topSmallNumber,
                            0,
                            key.topSmallNumber.length,
                            it
                        )
                    }

                    smallLetterPaint.isAntiAlias = true
                    smallLetterPaint.isFakeBoldText = true
                    smallLetterPaint.isSubpixelText = true
                    smallLetterPaint.color = currentTheme.subTextColor

                    val smallNumberY = mTopSmallNumberSize + mTopSmallNumberMarginHeight + (if (key.pressed) pressOffset else 0f)
                    canvas.drawText(
                        key.topSmallNumber,
                        key.width - bounds.width() / 2f - mTopSmallNumberMarginWidth,
                        smallNumberY,
                        smallLetterPaint
                    )
                }

                // Draw secondary icons for label-based keys
                drawSecondaryIcon(key, canvas, textColor)

                // Turn off drop shadow
                paint.setShadowLayer(0f, 0f, 0f, 0)
            } else if (key.icon != null && mKeyboard != null) {
                if (code == KEYCODE_SHIFT) {
                    val drawableId = when (mKeyboard!!.mShiftState) {
                        ShiftState.OFF -> R.drawable.ic_caps_outline_vector
                        ShiftState.ON_ONE_CHAR -> R.drawable.ic_caps_vector
                        else -> R.drawable.ic_caps_underlined_vector
                    }
                    key.icon = resources.getDrawable(drawableId)
                }

                if (code == KEYCODE_ENTER) {
                    key.icon!!.applyColorFilter(currentTheme.enterIconColor)
                    key.secondaryIcon?.applyColorFilter(currentTheme.enterIconColor.adjustAlpha(0.7f))
                } else if (code == KEYCODE_DELETE) {
                    key.icon!!.applyColorFilter(currentTheme.deleteIconColor)
                    key.icon!!.alpha = if (key.pressed) 200 else 255
                } else if (isAccent) {
                    key.icon!!.applyColorFilter(currentTheme.accentTextColor)
                    key.secondaryIcon?.applyColorFilter(currentTheme.accentTextColor.adjustAlpha(0.7f))
                } else {
                    key.icon!!.applyColorFilter(textColor)
                }

                val keyIcon = key.icon!!
                val secondaryIcon = key.secondaryIcon
                val centerY = faceCenterY.toInt()

                if (secondaryIcon != null) {
                    val keyIconWidth: Int
                    val keyIconHeight: Int
                    if (code == KEYCODE_DELETE) {
                        keyIconWidth = (key.height * 0.58f).toInt()
                        keyIconHeight = (key.height * 0.44f).toInt()
                    } else {
                        // When secondary icon exists, shrink main icon to 90%
                        keyIconWidth = (keyIcon.intrinsicWidth * 0.9f).toInt()
                        keyIconHeight = (keyIcon.intrinsicHeight * 0.9f).toInt()
                    }

                    val centerX = key.width / 2
                    val keyIconLeft = centerX - keyIconWidth / 2
                    val keyIconTop = centerY - keyIconHeight / 2

                    keyIcon.setBounds(
                        keyIconLeft,
                        keyIconTop,
                        keyIconLeft + keyIconWidth,
                        keyIconTop + keyIconHeight
                    )
                    keyIcon.draw(canvas)

                    drawSecondaryIcon(key, canvas, textColor)
                } else {
                    val iconWidth = if (code == KEYCODE_DELETE) {
                        (key.height * 0.58f).toInt()
                    } else {
                        keyIcon.intrinsicWidth
                    }
                    val iconHeight = if (code == KEYCODE_DELETE) {
                        (key.height * 0.44f).toInt()
                    } else {
                        keyIcon.intrinsicHeight
                    }

                    val drawableX = (key.width - iconWidth) / 2
                    val drawableY = centerY - iconHeight / 2
                    canvas.translate(drawableX.toFloat(), drawableY.toFloat())
                    keyIcon.setBounds(0, 0, iconWidth, iconHeight)
                    keyIcon.draw(canvas)
                    canvas.translate(-drawableX.toFloat(), -drawableY.toFloat())
                }
            }
            canvas.translate(-key.x.toFloat(), -key.y.toFloat())
        }

        // Overlay a dark rectangle to dim the keyboard
        if (mMiniKeyboardOnScreen) {
            paint.color = mKeyboardBackgroundColor.adjustAlpha(0.6f)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

        mCanvas!!.restore()
        mDrawPending = false
        mDirtyRect.setEmpty()
    }

    private fun setupKeyBackground(key: MyKeyboard.Key, keyCode: Int, canvas: Canvas) {
        canvas.translate(key.x.toFloat(), key.y.toFloat())
        val theme = context.getCurrentKeyboardTheme()
        draw3DKeyBackground(key, keyCode, canvas, theme)
    }

    private fun draw3DKeyBackground(key: MyKeyboard.Key, keyCode: Int, canvas: Canvas, theme: KeyboardThemeData) {
        val density = resources.displayMetrics.density
        val isArKey = keyCode == KEYCODE_EMOJI_OR_LANGUAGE || key.label == "AR" || key.label == "EN"
        val isEnter = keyCode == KEYCODE_ENTER
        val isDelete = keyCode == KEYCODE_DELETE
        val isSpace = keyCode == KEYCODE_SPACE
        val isAccent = isArKey || isDelete || keyCode in arrayOf(
            KEYCODE_SHIFT, KEYCODE_MODE_CHANGE, KEYCODE_SYMBOLS_MODE_CHANGE,
            KEYCODE_POPUP_EMOJI, KEYCODE_POPUP_SETTINGS
        ) || key.label == "123" || key.label == "?123"

        val topColor: Int
        val bottomColor: Int
        val highlightColor: Int
        val shadowColor: Int
        val pressedColor: Int

        when {
            isEnter -> {
                topColor = theme.enterKeyTopColor
                bottomColor = theme.enterKeyBottomColor
                highlightColor = theme.enterKeyHighlightColor
                shadowColor = theme.enterKeyShadowColor
                pressedColor = theme.enterKeyPressedColor
            }
            isDelete -> {
                topColor = theme.deleteKeyTopColor
                bottomColor = theme.deleteKeyBottomColor
                highlightColor = theme.deleteKeyHighlightColor
                shadowColor = theme.deleteKeyShadowColor
                pressedColor = theme.deleteKeyPressedColor
            }
            isAccent -> {
                topColor = theme.accentKeyTopColor
                bottomColor = theme.accentKeyBottomColor
                highlightColor = theme.accentKeyHighlightColor
                shadowColor = theme.accentKeyShadowColor
                pressedColor = theme.accentKeyPressedColor
            }
            isSpace -> {
                topColor = theme.spaceKeyTopColor
                bottomColor = theme.spaceKeyBottomColor
                highlightColor = theme.normalKeyHighlightColor
                shadowColor = theme.normalKeyShadowColor
                pressedColor = theme.normalKeyPressedColor
            }
            else -> {
                topColor = theme.normalKeyTopColor
                bottomColor = theme.normalKeyBottomColor
                highlightColor = theme.normalKeyHighlightColor
                shadowColor = theme.normalKeyShadowColor
                pressedColor = theme.normalKeyPressedColor
            }
        }

        val marginH = if (isSpace) 3.5f * density else 2.2f * density
        val marginTop = 2.5f * density
        val marginBottom = 3.5f * density
        val depth = (theme.elevationDp * density).coerceIn(KEY_3D_MIN_DEPTH_PX, KEY_3D_MAX_DEPTH_PX)
        val radius = (theme.cornerRadiusDp * density).coerceIn(4f * density, 14f * density)

        val kw = key.width.toFloat()
        val kh = key.height.toFloat()

        if (key.pressed) {
            val pressOffset = depth - 1.2f * density
            mKey3DFaceRect.set(marginH, marginTop + pressOffset, kw - marginH, kh - marginBottom - 1.2f * density)
            mKey3DFacePaint.shader = null
            mKey3DFacePaint.color = pressedColor
            canvas.drawRoundRect(mKey3DFaceRect, radius, radius, mKey3DFacePaint)

            mKey3DHighlightPaint.strokeWidth = 1.3f * density
            mKey3DHighlightPaint.color = highlightColor
            canvas.drawRoundRect(mKey3DFaceRect, radius, radius, mKey3DHighlightPaint)
        } else {
            // 3D Bottom shadow extrusion
            mKey3DShadowRect.set(marginH, marginTop + depth * 0.35f, kw - marginH, kh - marginBottom)
            mKey3DShadowPaint.color = shadowColor
            canvas.drawRoundRect(mKey3DShadowRect, radius, radius, mKey3DShadowPaint)

            // Raised 3D keycap surface
            mKey3DFaceRect.set(marginH, marginTop, kw - marginH, kh - marginBottom - depth)
            mKey3DFacePaint.shader = LinearGradient(
                0f, mKey3DFaceRect.top, 0f, mKey3DFaceRect.bottom,
                topColor, bottomColor, Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(mKey3DFaceRect, radius, radius, mKey3DFacePaint)

            // Specular rim stroke for ultra-sharp edge definition
            mKey3DHighlightPaint.strokeWidth = 1.2f * density
            mKey3DHighlightPaint.color = highlightColor
            canvas.drawRoundRect(mKey3DFaceRect, radius, radius, mKey3DHighlightPaint)

            // Subtle top reflective shine
            mKey3DTopShinePaint.strokeWidth = 1f * density
            mKey3DTopShinePaint.color = Color.argb(90, 255, 255, 255)
            val lineMargin = radius * 0.55f
            if (mKey3DFaceRect.right - lineMargin > mKey3DFaceRect.left + lineMargin) {
                canvas.drawLine(
                    mKey3DFaceRect.left + lineMargin,
                    mKey3DFaceRect.top + 1.2f * density,
                    mKey3DFaceRect.right - lineMargin,
                    mKey3DFaceRect.top + 1.2f * density,
                    mKey3DTopShinePaint
                )
            }
        }
    }

    private fun getSpaceKeyBackground(): Drawable? {
        if (mShowKeyBorders) {
            return mKeyBackground
        }

        val drawableId = if (mUsingSystemTheme) {
            R.drawable.keyboard_space_background_material
        } else {
            R.drawable.keyboard_space_background
        }

        return resources.getDrawable(drawableId, context.theme)
    }

    private fun getEnterKeyBackground(): Drawable? {
        val drawableId = if (mShowKeyBorders) {
            R.drawable.keyboard_enter_background_outlined
        } else {
            R.drawable.keyboard_enter_background
        }
        return resources.getDrawable(drawableId, context.theme)
    }

    private fun drawSecondaryIcon(key: MyKeyboard.Key, canvas: Canvas, textColor: Int) {
        val secondaryIcon = key.secondaryIcon ?: return
        secondaryIcon.applyColorFilter(
            if (key.pressed) textColor else mTextColor.adjustAlpha(0.6f)
        )
        val secondaryIconWidth = (secondaryIcon.intrinsicWidth * 0.5f).toInt()
        val secondaryIconHeight = (secondaryIcon.intrinsicHeight * 0.5f).toInt()
        val secondaryIconPaddingRight = 10
        val secondaryIconLeft = key.width - secondaryIconPaddingRight - secondaryIconWidth
        val secondaryIconRight = secondaryIconLeft + secondaryIconWidth
        val secondaryIconTop = 14
        val secondaryIconBottom = secondaryIconTop + secondaryIconHeight

        secondaryIcon.setBounds(
            secondaryIconLeft, secondaryIconTop, secondaryIconRight, secondaryIconBottom
        )
        secondaryIcon.draw(canvas)
    }

    fun handleClipboard() {
        if (mToolbarHolder != null && mPopupParent.id != R.id.mini_keyboard_view && context.config.showClipboardContent) {
            val clipboardContent = context.getCurrentClip()
            val ime = (context as? SimpleKeyboardIME) ?: (mOnKeyboardActionListener as? SimpleKeyboardIME)
            if (ime?.shouldIgnoreClip(clipboardContent) == true) {
                hideClipboardViews()
                return
            }
            if (clipboardContent?.isNotEmpty() == true) {
                keyboardViewBinding?.apply {
                    clipboardValue.apply {
                        val cleanedPreview = clipboardContent.toString().trim().replace(Regex("[\\r\\n\\t]+"), " ").replace(Regex(" {2,}"), " ")
                        text = if (cleanedPreview.length > 30) {
                            cleanedPreview.take(27) + "..."
                        } else {
                            cleanedPreview
                        }
                        removeUnderlines()
                        setOnClickListener {
                            mOnKeyboardActionListener!!.onText(clipboardContent.toString())
                            vibrateIfNeeded()
                        }
                    }

                    toggleClipboardVisibility(true)
                }
            } else {
                hideClipboardViews()
            }
        } else {
            hideClipboardViews()
        }
    }

    private fun hideClipboardViews() {
        keyboardViewBinding?.apply {
            clipboardValue.beGone()
            clipboardValue.alpha = 0f
            clipboardClear.beGone()
            clipboardClear.alpha = 0f
        }
    }

    private fun clearClipboardContent() {
        val clipboardManager =
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager) ?: return
        if (isPiePlus()) {
            clipboardManager.clearPrimaryClip()
        } else {
            val clip = ClipData.newPlainText("", "")
            clipboardManager.setPrimaryClip(clip)
        }
    }

    private fun toggleClipboardVisibility(show: Boolean) {
        val binding = keyboardViewBinding ?: return
        if (
            (show && binding.clipboardValue.alpha == 0f) ||
            (!show && binding.clipboardValue.alpha == 1f)
        ) {
            val newAlpha = if (show) 1f else 0f
            val animations = ArrayList<ObjectAnimator>()
            val clipboardValueAnimation = ObjectAnimator.ofFloat(
                binding.clipboardValue, "alpha", newAlpha
            )
            animations.add(clipboardValueAnimation)

            val clipboardClearAnimation = ObjectAnimator.ofFloat(
                binding.clipboardClear, "alpha", newAlpha
            )
            animations.add(clipboardClearAnimation)

            val animSet = AnimatorSet()
            animSet.playTogether(*animations.toTypedArray())
            animSet.duration = 150
            animSet.interpolator = AccelerateInterpolator()
            animSet.doOnStart {
                if (show) {
                    keyboardViewBinding?.clipboardValue?.beVisible()
                    keyboardViewBinding?.clipboardClear?.beVisible()
                }
            }
            animSet.doOnEnd {
                if (!show) {
                    keyboardViewBinding?.clipboardValue?.beGone()
                    keyboardViewBinding?.clipboardClear?.beGone()
                }
            }
            animSet.start()
        }
    }

    private fun getPressedKeyIndex(x: Int, y: Int): Int {
        val exactIndex = mKeys.indexOfFirst {
            it.isInside(x, y)
        }
        if (exactIndex != NOT_A_KEY) {
            return exactIndex
        }

        // Proximity fallback: find closest key center within reasonable touch distance
        var closestIndex = NOT_A_KEY
        var minDistanceSq = Int.MAX_VALUE
        val maxAllowedDist = (36 * resources.displayMetrics.density).toInt()
        val maxAllowedDistSq = maxAllowedDist * maxAllowedDist

        for (i in mKeys.indices) {
            val key = mKeys[i]
            val centerX = key.x + key.width / 2
            val centerY = key.y + key.height / 2
            val dx = x - centerX
            val dy = y - centerY
            val distSq = dx * dx + dy * dy
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq
                closestIndex = i
            }
        }

        return if (minDistanceSq <= maxAllowedDistSq) closestIndex else NOT_A_KEY
    }

    private fun detectAndSendKey(index: Int, x: Int, y: Int, eventTime: Long) {
        if (index != NOT_A_KEY && index in mKeys.indices) {
            val key = mKeys[index]
            getPressedKeyIndex(x, y)
            mOnKeyboardActionListener!!.onKey(key.code)
            mLastTapTime = eventTime
        }
    }

    private fun showPreview(keyIndex: Int) {
        val oldKeyIndex = mCurrentKeyIndex
        val previewPopup = mPreviewPopup
        mCurrentKeyIndex = keyIndex

        if (!context.config.showPopupOnKeypress) {
            return
        }

        // If key changed and preview is on ...
        if (oldKeyIndex != mCurrentKeyIndex) {
            if (previewPopup.isShowing) {
                if (keyIndex == NOT_A_KEY) {
                    mHandler!!.sendMessageDelayed(
                        mHandler!!.obtainMessage(MSG_REMOVE_PREVIEW), DELAY_AFTER_PREVIEW.toLong()
                    )
                }
            }

            if (keyIndex != NOT_A_KEY) {
                showKey(keyIndex)
            }
        }
    }

    private fun showKey(keyIndex: Int) {
        val previewPopup = mPreviewPopup
        val keys = mKeys
        if (keyIndex < 0 || keyIndex >= mKeys.size) {
            return
        }

        val key = keys[keyIndex]
        if (key.code == KEYCODE_SPACE) return // no popup for the language label
        if (key.icon != null) {
            mPreviewText!!.setCompoundDrawables(null, null, null, key.icon)
        } else {
            val customTypeface = FontHelper.getTypeface(context)
            if (key.label.length > 1) {
                mPreviewText!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, mKeyTextSize.toFloat())
                mPreviewText!!.typeface = Typeface.create(customTypeface, Typeface.BOLD)
            } else {
                mPreviewText!!.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mPreviewTextSizeLarge.toFloat()
                )
                mPreviewText!!.typeface = customTypeface
            }

            mPreviewText!!.setCompoundDrawables(null, null, null, null)
            try {
                mPreviewText!!.text = adjustCase(key.label)
            } catch (ignored: Exception) {
            }
        }

        val previewBackground = mPreviewText!!.background as LayerDrawable
        previewBackground.findDrawableByLayerId(R.id.button_background_shape)
            .applyColorFilter(mBackgroundColor)
        previewBackground.findDrawableByLayerId(R.id.button_background_stroke)
            .applyColorFilter(mStrokeColor)
        mPreviewText!!.background = previewBackground

        mPreviewText!!.setTextColor(mTextColor)
        mPreviewText!!.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = Math.max(mPreviewText!!.measuredWidth, key.width)
        val popupHeight = mPreviewHeight
        val lp = mPreviewText!!.layoutParams
        lp?.width = popupWidth
        lp?.height = popupHeight

        mPopupPreviewX = key.x
        mPopupPreviewY = key.y - popupHeight

        mHandler!!.removeMessages(MSG_REMOVE_PREVIEW)
        getLocationInWindow(mCoordinates)
        mCoordinates[0] += mMiniKeyboardOffsetX // Offset may be zero
        mCoordinates[1] += mMiniKeyboardOffsetY // Offset may be zero

        // Set the preview background state
        mPreviewText!!.background.state = if (key.popupResId != 0) {
            LONG_PRESSABLE_STATE_SET
        } else {
            EMPTY_STATE_SET
        }

        mPopupPreviewX += mCoordinates[0]
        mPopupPreviewY += mCoordinates[1]

        // If the popup cannot be shown above the key, put it on the side
        getLocationOnScreen(mCoordinates)
        if (mPopupPreviewY + mCoordinates[1] < 0) {
            // If the key you're pressing is on the left side of the keyboard, show the popup on
            // the right, offset by enough to see at least one key to the left/right.
            if (key.x + key.width <= width / 2) {
                mPopupPreviewX += (key.width * 2.5).toInt()
            } else {
                mPopupPreviewX -= (key.width * 2.5).toInt()
            }
            mPopupPreviewY += popupHeight
        }

        previewPopup.dismiss()

        if (
            key.label.isNotEmpty()
            && key.code != KEYCODE_MODE_CHANGE
            && key.code != KEYCODE_SYMBOLS_MODE_CHANGE
            && key.code != KEYCODE_SHIFT
        ) {
            previewPopup.width = popupWidth
            previewPopup.height = popupHeight
            previewPopup.showAtLocation(
                mPopupParent,
                Gravity.NO_GRAVITY,
                mPopupPreviewX,
                mPopupPreviewY
            )
            mPreviewText!!.visibility = VISIBLE
        }
    }

    /**
     * Requests a redraw of the entire keyboard. Calling [.invalidate] is not sufficient because the keyboard renders the keys to an off-screen buffer and
     * an invalidate() only draws the cached buffer.
     */
    fun invalidateAllKeys() {
        if (width > 0 && height > 0) {
            mDirtyRect.set(0, 0, width, height)
        } else if (mKeyboard != null) {
            mDirtyRect.set(0, 0, mKeyboard!!.mMinWidth, mKeyboard!!.mHeight)
        }
        mDrawPending = true
        invalidate()
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only one key is changing it's content. Any changes that
     * affect the position or size of the key may not be honored.
     * @param keyIndex the index of the key in the attached [MyKeyboard].
     */
    private fun invalidateKey(keyIndex: Int) {
        if (keyIndex < 0 || keyIndex >= mKeys.size) {
            return
        }

        val key = mKeys[keyIndex]
        mDirtyRect.union(
            key.x, key.y,
            key.x + key.width, key.y + key.height
        )
        onBufferDraw()
        invalidate(
            key.x, key.y,
            key.x + key.width, key.y + key.height
        )
    }

    private fun openPopupIfRequired(me: MotionEvent): Boolean {
        // Check if we have a popup layout specified first.
        if (mPopupLayout == 0) {
            return false
        }

        if (mCurrentKey < 0 || mCurrentKey >= mKeys.size) {
            return false
        }

        val popupKey = mKeys[mCurrentKey]
        val result = onLongPress(popupKey, me)
        if (result) {
            mAbortKey = true
            showPreview(NOT_A_KEY)
        }

        return result
    }

    /**
     * Called when a key is long pressed. By default this will open any popup keyboard associated with this key through the attributes
     * popupLayout and popupCharacters.
     * @param popupKey the key that was long pressed
     * @return true if the long press is handled, false otherwise. Subclasses should call the method on the base class if the subclass doesn't wish to
     * handle the call.
     */
    private fun onLongPress(popupKey: MyKeyboard.Key, me: MotionEvent): Boolean {
        if (popupKey.code == KEYCODE_SPACE) {
            return onSpaceBarLongPressed()
        } else if (popupKey.code == KEYCODE_EMOJI_OR_LANGUAGE) {
            return onEmojiOrLanguageLongPressed()
        } else {
            val popupKeyboardId = popupKey.popupResId
            if (popupKeyboardId != 0) {
                mMiniKeyboardContainer = mMiniKeyboardCache[popupKey]

                // For 'number' and 'phone' keyboards the count of popup keys might be bigger than count of keys in the main keyboard.
                // And therefore the width of the key might be smaller than width declared in MyKeyboard.Key.width for the main keyboard.
                val popupKeyWidth = if (popupKey.popupCharacters != null) {
                    popupKey.calcKeyWidth(containerWidth = mMiniKeyboardContainer?.measuredWidth ?: width)
                } else {
                    popupKey.width
                }

                if (mMiniKeyboardContainer == null) {
                    val inflater =
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    keyboardPopupBinding = KeyboardPopupKeyboardBinding.inflate(inflater).apply {
                        mMiniKeyboardContainer = root
                        mMiniKeyboard = miniKeyboardView
                    }

                    val keyboard = if (popupKey.popupCharacters != null) {
                        MyKeyboard(
                            context = context,
                            layoutTemplateResId = popupKeyboardId,
                            characters = popupKey.popupCharacters!!,
                            keyWidth = popupKeyWidth
                        )
                    } else {
                        MyKeyboard(context, popupKeyboardId, 0)
                    }
                    mMiniKeyboard!!.setKeyboard(keyboard)
                    mPopupParent = this
                    mMiniKeyboardContainer!!.measure(
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
                    )
                    mMiniKeyboardCache[popupKey] = mMiniKeyboardContainer
                } else {
                    mMiniKeyboard =
                        mMiniKeyboardCache[popupKey]?.let(KeyboardPopupKeyboardBinding::bind)?.miniKeyboardView
                }

                getLocationInWindow(mCoordinates)
                mPopupX = popupKey.x
                mPopupY = popupKey.y

                // Use popupCharacters length if available, otherwise use actual key count from the loaded keyboard
                val popupKeyCount = popupKey.popupCharacters?.length ?: mMiniKeyboard!!.mKeys.size
                val widthToUse = mMiniKeyboardContainer!!.measuredWidth - (popupKeyCount / 2) * popupKeyWidth
                mPopupX = mPopupX + popupKeyWidth - widthToUse
                mPopupY -= mMiniKeyboardContainer!!.measuredHeight
                val x = mPopupX + mCoordinates[0]
                val y = mPopupY + mCoordinates[1]
                val xOffset = Math.max(0, x)
                mMiniKeyboard!!.setPopupOffset(xOffset, y)

                // make sure we highlight the proper key right after long pressing it, before any ACTION_MOVE event occurs
                val miniKeyboardX = if (xOffset + mMiniKeyboard!!.measuredWidth <= measuredWidth) {
                    xOffset
                } else {
                    measuredWidth - mMiniKeyboard!!.measuredWidth
                }

                val keysCnt = mMiniKeyboard!!.mKeys.size
                var selectedKeyIndex =
                    Math.floor((me.x - miniKeyboardX) / popupKeyWidth.toDouble()).toInt()
                if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                    selectedKeyIndex += MAX_KEYS_PER_MINI_ROW
                }
                selectedKeyIndex = Math.max(0, Math.min(selectedKeyIndex, keysCnt - 1))

                for (i in 0 until keysCnt) {
                    mMiniKeyboard!!.mKeys[i].focused = i == selectedKeyIndex
                }

                mMiniKeyboardSelectedKeyIndex = selectedKeyIndex
                mMiniKeyboard!!.invalidateAllKeys()

                val miniShiftStatus = if (isShifted()) ShiftState.ON_PERMANENT else ShiftState.OFF
                mMiniKeyboard!!.setShifted(miniShiftStatus)
                mPopupKeyboard.contentView = mMiniKeyboardContainer
                mPopupKeyboard.width = mMiniKeyboardContainer!!.measuredWidth
                mPopupKeyboard.height = mMiniKeyboardContainer!!.measuredHeight
                mPopupKeyboard.showAtLocation(this, Gravity.NO_GRAVITY, x, y)
                mMiniKeyboardOnScreen = true
                invalidateAllKeys()
                return true
            }
        }
        return false
    }

    override fun onTouchEvent(me: MotionEvent): Boolean {
        val action = me.action

        if (ignoreTouches) {
            if (action == MotionEvent.ACTION_UP) {
                ignoreTouches = false

                // fix a glitch with long pressing backspace, then clicking some letter
                if (mRepeatKeyIndex != NOT_A_KEY) {
                    val key = mKeys.getOrNull(mRepeatKeyIndex)
                    if (key?.code == KEYCODE_DELETE) {
                        mHandler?.removeMessages(MSG_REPEAT)
                        mRepeatKeyIndex = NOT_A_KEY
                    }
                }
            }
            return true
        }

        // handle moving between alternative popup characters by swiping
        if (mPopupKeyboard.isShowing) {
            when (action) {
                MotionEvent.ACTION_MOVE -> {
                    if (mMiniKeyboard != null) {
                        val coords = intArrayOf(0, 0)
                        mMiniKeyboard!!.getLocationOnScreen(coords)
                        val keysCnt = mMiniKeyboard!!.mKeys.size
                        val lastRowKeyCount = if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                            Math.max(keysCnt % MAX_KEYS_PER_MINI_ROW, 1)
                        } else {
                            keysCnt
                        }

                        val widthPerKey = if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                            mMiniKeyboard!!.width / MAX_KEYS_PER_MINI_ROW
                        } else {
                            mMiniKeyboard!!.width / lastRowKeyCount
                        }

                        var selectedKeyIndex =
                            Math.floor((me.x - coords[0]) / widthPerKey.toDouble()).toInt()
                        if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                            selectedKeyIndex = Math.max(0, selectedKeyIndex)
                            selectedKeyIndex += MAX_KEYS_PER_MINI_ROW
                        }

                        selectedKeyIndex = Math.max(0, Math.min(selectedKeyIndex, keysCnt - 1))
                        if (selectedKeyIndex != mMiniKeyboardSelectedKeyIndex) {
                            for (i in 0 until keysCnt) {
                                mMiniKeyboard!!.mKeys[i].focused = i == selectedKeyIndex
                            }
                            mMiniKeyboardSelectedKeyIndex = selectedKeyIndex
                            mMiniKeyboard!!.invalidateAllKeys()
                        }

                        if (coords[0] > 0 || coords[1] > 0) {
                            if (coords[0] - me.x > mPopupMaxMoveDistance ||                                         // left
                                me.x - (coords[0] + mMiniKeyboard!!.measuredWidth) > mPopupMaxMoveDistance          // right
                            ) {
                                dismissPopupKeyboard()
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mMiniKeyboard?.mKeys?.firstOrNull { it.focused }?.apply {
                        mOnKeyboardActionListener!!.onKey(code)
                    }
                    mMiniKeyboardSelectedKeyIndex = -1
                    dismissPopupKeyboard()
                }
            }
        }

        return onModifiedTouchEvent(me)
    }

    private fun onModifiedTouchEvent(me: MotionEvent): Boolean {
        var touchX = me.x.toInt()
        var touchY = me.y.toInt()
        if (touchY >= -mVerticalCorrection) {
            touchY += mVerticalCorrection
        }

        val action = me.actionMasked
        val eventTime = me.eventTime
        val keyIndex = getPressedKeyIndex(touchX, touchY)

        // Ignore all motion events until a DOWN.
        if (mAbortKey && action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL) {
            return true
        }

        // Needs to be called after the gesture detector gets a turn, as it may have displayed the mini keyboard
        if (mMiniKeyboardOnScreen && action != MotionEvent.ACTION_CANCEL) {
            return true
        }

        when (action) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                mHandler!!.removeMessages(MSG_LONGPRESS)
                dismissPopupKeyboard()
                if (keyIndex != NOT_A_KEY) {
                    detectAndSendKey(keyIndex, me.x.toInt(), me.y.toInt(), eventTime)
                }

                val pointerIdx = me.actionIndex
                val newPointerX = me.getX(pointerIdx).toInt()
                val newPointerY = me.getY(pointerIdx).toInt()
                val secondKeyIndex = getPressedKeyIndex(newPointerX, newPointerY)

                if (secondKeyIndex != NOT_A_KEY) {
                    showPreview(secondKeyIndex)
                    detectAndSendKey(secondKeyIndex, newPointerX, newPointerY, eventTime)
                    val secondKeyCode = mKeys.getOrNull(secondKeyIndex)?.code
                    if (secondKeyCode != null) {
                        mOnKeyboardActionListener!!.onPress(secondKeyCode)
                    }
                }

                showPreview(NOT_A_KEY)
                setCurrentKeyPressed(false)
                return true
            }

            MotionEvent.ACTION_DOWN -> {
                mAbortKey = false
                mLastCodeX = touchX
                mLastCodeY = touchY
                mLastKeyTime = 0
                mCurrentKeyTime = 0
                mLastKey = NOT_A_KEY
                mCurrentKey = keyIndex
                mDownTime = me.eventTime
                mLastMoveTime = mDownTime

                val onPressKey = if (keyIndex != NOT_A_KEY) {
                    mKeys[keyIndex].code
                } else {
                    0
                }
                mOnKeyboardActionListener!!.onPress(onPressKey)
                mLastKeyPressedCode = onPressKey

                var wasHandled = false
                if (mCurrentKey >= 0 && mKeys[mCurrentKey].repeatable) {
                    mRepeatKeyIndex = mCurrentKey
                    mRepeatCount = 0

                    val msg = mHandler!!.obtainMessage(MSG_REPEAT)
                    val delay = if (mKeys[mCurrentKey].code == KEYCODE_DELETE) DELETE_REPEAT_START_DELAY.toLong() else REPEAT_START_DELAY.toLong()
                    mHandler!!.sendMessageDelayed(msg, delay)
                    // if the user long presses space bar, move the cursor after swiping left/right
                    if (mKeys[mCurrentKey].code == KEYCODE_SPACE) {
                        mLastSpaceMoveX = -1
                        mCursorControlActive = false
                    } else {
                        repeatKey()
                    }

                    // Delivering the key could have caused an abort
                    if (mAbortKey) {
                        resetRepeat()
                        wasHandled = true
                    }
                }

                if (!wasHandled && mCurrentKey != NOT_A_KEY) {
                    val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                    mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                }

                if (mPopupParent.id != R.id.mini_keyboard_view) {
                    showPreview(keyIndex)
                    setCurrentKeyPressed(true)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                var continueLongPress = false
                if (keyIndex != NOT_A_KEY) {
                    if (mCurrentKey == NOT_A_KEY) {
                        mCurrentKey = keyIndex
                        mCurrentKeyTime = eventTime - mDownTime
                    } else {
                        if (keyIndex == mCurrentKey) {
                            mCurrentKeyTime += eventTime - mLastMoveTime
                            continueLongPress = true
                        } else if (mRepeatKeyIndex == NOT_A_KEY) {
                            setCurrentKeyPressed(false)
                            mLastKey = mCurrentKey
                            mLastCodeX = mLastX
                            mLastCodeY = mLastY
                            mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                            mCurrentKey = keyIndex
                            mCurrentKeyTime = 0
                            setCurrentKeyPressed(true)
                        }
                    }
                }

                // activate cursor control immediately on sufficient movement
                val currentKey = mKeys.getOrNull(mCurrentKey)
                if (currentKey?.code == KEYCODE_SPACE && mLastSpaceMoveX != 0) {
                    if (mLastSpaceMoveX == -1) {
                        mLastSpaceMoveX = mLastX
                    }

                    val diff = mLastX - mLastSpaceMoveX
                    if (diff < -mSpaceMoveThreshold) {
                        for (i in diff / mSpaceMoveThreshold until 0) {
                            mOnKeyboardActionListener?.moveCursorLeft()
                        }
                        mLastSpaceMoveX = mLastX
                        if (!mCursorControlActive) mHandler?.removeMessages(MSG_LONGPRESS)
                        mCursorControlActive = true
                    } else if (diff > mSpaceMoveThreshold) {
                        for (i in 0 until diff / mSpaceMoveThreshold) {
                            mOnKeyboardActionListener?.moveCursorRight()
                        }
                        mLastSpaceMoveX = mLastX
                        if (!mCursorControlActive) mHandler?.removeMessages(MSG_LONGPRESS)
                        mCursorControlActive = true
                    }
                } else if (!continueLongPress) {
                    // Cancel old long-press
                    mHandler!!.removeMessages(MSG_LONGPRESS)
                    // Start new long-press if key has changed
                    if (keyIndex != NOT_A_KEY) {
                        val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                        mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                    }

                    if (mPopupParent.id != R.id.mini_keyboard_view) {
                        showPreview(mCurrentKey)
                    }
                    mLastMoveTime = eventTime
                }
            }

            MotionEvent.ACTION_UP -> {
                setCurrentKeyPressed(false)

                mLastSpaceMoveX = 0
                removeMessages()
                if (keyIndex == mCurrentKey) {
                    mCurrentKeyTime += eventTime - mLastMoveTime
                } else {
                    mLastKey = mCurrentKey
                    mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                    mCurrentKey = keyIndex
                    mCurrentKeyTime = 0
                }

                showPreview(NOT_A_KEY)
                Arrays.fill(mKeyIndices, NOT_A_KEY)

                val currentKeyCode = mKeys.getOrNull(mCurrentKey)?.code

                // If we're not on a repeating key (which sends on a DOWN event)
                if (mRepeatKeyIndex == NOT_A_KEY && !mMiniKeyboardOnScreen && !mAbortKey) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                } else if (currentKeyCode == KEYCODE_SPACE && !mCursorControlActive) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                }

                resetRepeat()
                mOnKeyboardActionListener!!.onActionUp()
                mCursorControlActive = false
            }

            MotionEvent.ACTION_CANCEL -> {
                mCursorControlActive = false
                mLastSpaceMoveX = 0
                removeMessages()
                dismissPopupKeyboard()
                mAbortKey = true
                showPreview(NOT_A_KEY)
                setCurrentKeyPressed(false)
            }
        }

        mLastX = touchX
        mLastY = touchY
        return true
    }

    private fun repeatKey(): Boolean {
        if (mRepeatKeyIndex == REPEAT_KEY_DELETE_SPECIAL) {
            mRepeatCount++
            val deleteBatch = when {
                mRepeatCount > 15 -> 3 // Ultra fast delete
                mRepeatCount > 5 -> 2  // Accelerated delete
                else -> 1              // Single delete initially
            }
            for (i in 0 until deleteBatch) {
                mOnKeyboardActionListener?.onKey(KEYCODE_DELETE)
            }
            return true
        }

        if (mRepeatKeyIndex == NOT_A_KEY || mRepeatKeyIndex !in mKeys.indices) {
            return false
        }
        val key = mKeys[mRepeatKeyIndex]
        if (key.code != KEYCODE_SPACE) {
            mRepeatCount++
            if (key.code == KEYCODE_DELETE) {
                // Accelerate deletion quickly when long pressing delete
                val deleteBatch = when {
                    mRepeatCount > 15 -> 3 // Ultra fast delete (3 characters per cycle)
                    mRepeatCount > 5 -> 2  // Accelerated delete (2 characters per cycle)
                    else -> 1              // Single character delete initially
                }
                for (i in 0 until deleteBatch) {
                    mOnKeyboardActionListener?.onKey(KEYCODE_DELETE)
                }
            } else {
                detectAndSendKey(mCurrentKey, key.x, key.y, mLastTapTime)
            }
        }
        return true
    }

    private fun getNextRepeatDelay(): Long {
        if (mRepeatKeyIndex == REPEAT_KEY_DELETE_SPECIAL) {
            return when {
                mRepeatCount > 15 -> 18L // Ultra fast
                mRepeatCount > 8 -> 25L  // Very fast
                mRepeatCount > 3 -> 35L  // Accelerated
                else -> 45L              // Snappy initial repeat
            }
        }
        val key = mKeys.getOrNull(mRepeatKeyIndex)
        return if (key?.code == KEYCODE_DELETE) {
            when {
                mRepeatCount > 15 -> 18L // Ultra fast
                mRepeatCount > 8 -> 25L  // Very fast
                mRepeatCount > 3 -> 35L  // Accelerated
                else -> 45L              // Snappy initial repeat
            }
        } else {
            REPEAT_INTERVAL.toLong()
        }
    }

    private fun resetRepeat() {
        mHandler?.removeMessages(MSG_REPEAT)
        mRepeatKeyIndex = NOT_A_KEY
        mRepeatCount = 0
    }

    private fun setCurrentKeyPressed(pressed: Boolean) {
        mKeys.getOrNull(mCurrentKey)?.pressed = pressed
        invalidateKey(mCurrentKey)
    }

    private fun isAnyPanelOpen(): Boolean {
        return keyboardViewBinding?.run {
            clipboardManagerHolder.visibility == View.VISIBLE ||
                emojiPaletteHolder.visibility == View.VISIBLE ||
                toolsMenuHolder.visibility == View.VISIBLE ||
                textEditorHolder.visibility == View.VISIBLE ||
                numericKeypadHolder.visibility == View.VISIBLE
        } ?: false
    }

    fun closeAllPanels() {
        mVoiceManager.stopListening()
        keyboardViewBinding?.apply {
            clipboardManagerHolder.beGone()
            emojiPaletteHolder.beGone()
            toolsMenuHolder.beGone()
            textEditorHolder.beGone()
            numericKeypadHolder.beGone()
            suggestionsHolder.showAllInlineContentViews()
            mToolbarHolder?.beVisible()
        }
    }

    fun openResizeMode() {
        closeAllPanels()
        KeyboardResizeDialog(this) {
            mOnKeyboardActionListener?.reloadKeyboard()
        }
    }

    fun closeResizeOverlay() {
        // Obsolete overlay removed
    }

    private fun setupResizeOverlay() {
        // Obsolete overlay removed
    }

    fun ensureToolbarVisible() {
        mToolbarHolder?.beVisible()
    }

    fun openNumericKeypad() {
        closeAllPanels()
        keyboardViewBinding?.apply {
            val isArabic = context.config.keyboardLanguage == LANGUAGE_ARABIC
            numKeyAbc.text = if (isArabic) "أ ب ج" else "ABC"
            numericKeypadHolder.beVisible()
            suggestionsHolder.hideAllInlineContentViews()
        }
    }

    fun closeNumericKeypad() {
        keyboardViewBinding?.apply {
            numericKeypadHolder.beGone()
            if (!isAnyPanelOpen()) {
                suggestionsHolder.showAllInlineContentViews()
            }
        }
    }

    fun closeClipboardManager() {
        keyboardViewBinding?.apply {
            clipboardManagerHolder.beGone()
            if (!isAnyPanelOpen()) {
                suggestionsHolder.showAllInlineContentViews()
            }
        }
    }

    fun openClipboardManager() {
        closeAllPanels()
        keyboardViewBinding?.apply {
            clipboardManagerHolder.layoutDirection = View.LAYOUT_DIRECTION_LTR
            clipsList.layoutDirection = View.LAYOUT_DIRECTION_RTL
            clipboardManagerHolder.beVisible()
            suggestionsHolder.hideAllInlineContentViews()
        }
        setupStoredClips()
    }

    fun openToolsMenu() {
        closeAllPanels()
        keyboardViewBinding?.apply {
            val isArabic = context.config.keyboardLanguage == LANGUAGE_ARABIC
            toolsMenuClose.text = if (isArabic) "أ ب ج" else "ABC"
            toolsMenuHolder.beVisible()
            suggestionsHolder.hideAllInlineContentViews()
        }
    }

    fun openTextEditor() {
        closeAllPanels()
        keyboardViewBinding?.apply {
            val isArabic = context.config.keyboardLanguage == LANGUAGE_ARABIC
            cursorBtnAbc.text = if (isArabic) "أ ب ج" else "ABC"
            if (isArabic) {
                cursorBtnClipboard.text = "حافظة\nالنصوص"
                cursorBtnSelectAll.text = "الكل"
                cursorBtnCopy.text = "نسخ"
                cursorBtnCut.text = "قص"
                cursorBtnSelect.text = "تحديد"
                cursorBtnEnd.text = "إنهاء"
                cursorBtnPaste.text = "لصق"
            } else {
                cursorBtnClipboard.text = "Clipboard"
                cursorBtnSelectAll.text = "All"
                cursorBtnCopy.text = "Copy"
                cursorBtnCut.text = "Cut"
                cursorBtnSelect.text = "Select"
                cursorBtnEnd.text = "End"
                cursorBtnPaste.text = "Paste"
            }
            textEditorHolder.beVisible()
            suggestionsHolder.hideAllInlineContentViews()
        }
    }

    private fun onSpaceBarLongPressed(): Boolean {
        return if (!mCursorControlActive) {
            setCurrentKeyPressed(false)
            mRepeatKeyIndex = NOT_A_KEY
            mHandler?.removeMessages(MSG_REPEAT)
            vibrateIfNeeded()
            SwitchLanguageDialog(this) {
                mOnKeyboardActionListener?.reloadKeyboard()
            }
            true
        } else false
    }

    private fun onEmojiOrLanguageLongPressed(): Boolean {
        setCurrentKeyPressed(false)
        if (context.config.showEmojiKey) {
            openEmojiPalette()
        } else {
            SwitchLanguageDialog(this) {
                mOnKeyboardActionListener?.reloadKeyboard()
            }
        }

        return true
    }

    private var mPrimaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    fun registerClipboardListener() {
        // Lifecycle is managed centrally in SimpleKeyboardIME to avoid duplicate listeners and battery drain
    }

    fun unregisterClipboardListener() {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            mPrimaryClipChangedListener?.let { cm.removePrimaryClipChangedListener(it) }
            mPrimaryClipChangedListener = null
        } catch (ignored: Exception) {
        }
    }

    fun syncClipboard(onComplete: (() -> Unit)? = null) {
        val ime = (context as? SimpleKeyboardIME) ?: (mOnKeyboardActionListener as? SimpleKeyboardIME)
        if (ime != null && !ime.isDefaultIme()) {
            onComplete?.invoke()
            return
        }
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: run {
                onComplete?.invoke()
                return
            }
            val clip = try {
                cm.primaryClip
            } catch (e: Exception) {
                null
            } ?: run {
                onComplete?.invoke()
                return
            }
            if (clip.itemCount > 0) {
                val textList = ArrayList<String>()
                for (i in 0 until clip.itemCount) {
                    val item = clip.getItemAt(i) ?: continue
                    val clipText = item.text?.toString()?.trim()
                        ?: item.coerceToText(context).toString().trim()
                    if (clipText.isNotEmpty()) {
                        if (ime?.shouldIgnoreClip(clipText) == true) {
                            continue
                        }
                        textList.add(clipText)
                    }
                }

                if (textList.isNotEmpty()) {
                    ensureBackgroundThread {
                        for (clipText in textList) {
                            // If this text already exists in recent (unpinned) clips, delete the older copy so the new copy jumps to the top
                            context.clipsDB.deleteRecentClipWithValue(clipText)

                            // Insert the newly copied text as unpinned in Recent clips!
                            val newClip = Clip(null, clipText, isPinned = false)
                            context.clipsDB.insertOrUpdate(newClip)
                        }

                        // Keep up to 4000 unpinned items (accommodating >3000 clips)
                        context.clipsDB.trimRecentClips()

                        Handler(Looper.getMainLooper()).post {
                            handleClipboard()
                            onComplete?.invoke()
                        }
                    }
                    return
                }
            }
            onComplete?.invoke()
        } catch (ignored: Exception) {
            onComplete?.invoke()
        }
    }

    fun setupStoredClips() {
        ensureBackgroundThread {
            val clips = ArrayList<ListItem>()
            val pinnedClips = context.clipsDB.getPinnedClips()
            val recentClips = context.clipsDB.getRecentClips()

            // Recent (unpinned) clips appear at the top
            if (recentClips.isNotEmpty()) {
                val section = ClipsSectionLabel(context.getString(R.string.recent_clips), false)
                clips.add(section)
                clips.addAll(recentClips)
            }

            // Pinned clips appear at the bottom
            if (pinnedClips.isNotEmpty()) {
                val section = ClipsSectionLabel(context.getString(R.string.pinned_clips), false)
                clips.add(section)
                clips.addAll(pinnedClips)
            }

            Handler(Looper.getMainLooper()).post {
                setupClipsAdapter(clips)
            }
        }
    }

    private fun setupClipsAdapter(clips: ArrayList<ListItem>) {
        keyboardViewBinding?.apply {
            clipboardContentPlaceholder1.beVisibleIf(clips.isEmpty())
            clipboardContentPlaceholder2.beVisibleIf(clips.isEmpty())
            clipsList.beVisibleIf(clips.isNotEmpty())
        }

        val refreshClipsListener = object : RefreshClipsListener {
            override fun refreshClips() {
                setupStoredClips()
            }
        }

        val adapter = ClipsKeyboardAdapter(
            context = context,
            items = clips,
            refreshClipsListener = refreshClipsListener,
            onStartDrag = { viewHolder ->
                clipsTouchHelper?.startDrag(viewHolder)
            }
        ) { clip ->
            mOnKeyboardActionListener?.onText(clip.value)
            vibrateIfNeeded()
            closeAllPanels()
        }

        keyboardViewBinding?.clipsList?.adapter = adapter

        if (clipsTouchHelper == null) {
            var wasClipMoved = false

            val touchCallback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.START or ItemTouchHelper.END,
                ItemTouchHelper.LEFT or ItemTouchHelper.START
            ) {
                override fun isLongPressDragEnabled(): Boolean = true

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val currentAdapter = recyclerView.adapter as? ClipsKeyboardAdapter ?: return 0
                    val position = viewHolder.bindingAdapterPosition
                    if (position !in 0 until currentAdapter.items.size) return 0
                    val item = currentAdapter.items[position]

                    // Strictly pinned clips can be dragged and reordered
                    val dragFlags = if (item is Clip && item.isPinned) {
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.START or ItemTouchHelper.END
                    } else {
                        0
                    }

                    // Only unpinned recent clips can be swiped to delete
                    val swipeFlags = if (item is Clip && !item.isPinned) {
                        ItemTouchHelper.LEFT or ItemTouchHelper.START
                    } else {
                        0
                    }

                    return makeMovementFlags(dragFlags, swipeFlags)
                }

                override fun canDropOver(
                    recyclerView: RecyclerView,
                    current: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val currentAdapter = recyclerView.adapter as? ClipsKeyboardAdapter ?: return false
                    val targetPos = target.bindingAdapterPosition
                    if (targetPos !in 0 until currentAdapter.items.size) return false
                    val targetItem = currentAdapter.items[targetPos]
                    // Strict rule: Pinned items can ONLY be dropped over other pinned items
                    return targetItem is Clip && targetItem.isPinned
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val currentAdapter = recyclerView.adapter as? ClipsKeyboardAdapter ?: return false
                    val fromPos = viewHolder.bindingAdapterPosition
                    val toPos = target.bindingAdapterPosition
                    if (fromPos !in 0 until currentAdapter.items.size || toPos !in 0 until currentAdapter.items.size) return false
                    val fromItem = currentAdapter.items[fromPos]
                    val toItem = currentAdapter.items[toPos]
                    if (fromItem !is Clip || !fromItem.isPinned) return false
                    if (toItem !is Clip || !toItem.isPinned) return false

                    currentAdapter.onItemMoved(fromPos, toPos)
                    wasClipMoved = true
                    return true
                }

                override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.let { view ->
                            view.animate()
                                .scaleX(1.06f)
                                .scaleY(1.06f)
                                .alpha(0.88f)
                                .setDuration(120)
                                .start()
                            view.elevation = 20f
                        }
                    }
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.let { view ->
                        view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .alpha(1.0f)
                            .setDuration(120)
                            .start()
                        view.elevation = 0f
                    }

                    if (wasClipMoved) {
                        wasClipMoved = false
                        val currentAdapter = recyclerView.adapter as? ClipsKeyboardAdapter ?: return
                        val pinnedClips = currentAdapter.items.filterIsInstance<Clip>().filter { it.isPinned }
                        ensureBackgroundThread {
                            context.clipsDB.reorderPinnedClips(pinnedClips)
                        }
                    }
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    val currentAdapter = keyboardViewBinding?.clipsList?.adapter as? ClipsKeyboardAdapter ?: return
                    if (position !in 0 until currentAdapter.items.size) return
                    val item = currentAdapter.items[position]
                    if (item is Clip) {
                        if (item.isPinned) {
                            // Pinned texts are protected and cannot be deleted
                            currentAdapter.notifyItemChanged(position)
                            return
                        }
                        ClipsHelper.clearSystemClipboardIfMatches(context, item.value)
                        ensureBackgroundThread {
                            if (item.id != null && item.id != -1L) {
                                context.clipsDB.delete(item.id!!)
                            }
                            Handler(Looper.getMainLooper()).post {
                                setupStoredClips()
                            }
                        }
                    }
                }
            }
            clipsTouchHelper = ItemTouchHelper(touchCallback)
            keyboardViewBinding?.clipsList?.let { rv ->
                clipsTouchHelper!!.attachToRecyclerView(rv)
            }
        }
    }

    private fun setupEmojiPalette(toolbarColor: Int, backgroundColor: Int, textColor: Int) {
        keyboardViewBinding?.apply {
            emojiPaletteTopBar.background = ColorDrawable(toolbarColor)
            emojiPaletteHolder.background = ColorDrawable(backgroundColor)
            emojiPaletteClose.applyColorFilter(textColor)
            emojiPaletteLabel.setTextColor(textColor)

            emojiPaletteBottomBar.background = ColorDrawable(toolbarColor)
            emojiPaletteModeChange.apply {
                setTextColor(textColor)
                setOnClickListener {
                    vibrateIfNeeded()
                    closeEmojiPalette()
                }
            }

            emojiPaletteBackspace.apply {
                applyColorFilter(textColor)
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isPressed = true
                            mRepeatKeyIndex = REPEAT_KEY_DELETE_SPECIAL
                            mCurrentKey = NOT_A_KEY
                            mRepeatCount = 0
                            vibrateIfNeeded()
                            mOnKeyboardActionListener!!.onKey(KEYCODE_DELETE)
                            // setup repeating backspace with fast start delay
                            val msg = mHandler!!.obtainMessage(MSG_REPEAT)
                            mHandler!!.sendMessageDelayed(msg, DELETE_REPEAT_START_DELAY.toLong())
                            true
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            resetRepeat()
                            isPressed = false
                            false
                        }

                        else -> false
                    }
                }
            }
        }

        setupEmojis()
    }

    fun openEmojiPalette() {
        closeAllPanels()
        keyboardViewBinding!!.emojiPaletteHolder.beVisible()
        keyboardViewBinding!!.suggestionsHolder.beGone()
        setupEmojis()
    }

    fun closeEmojiPalette() {
        keyboardViewBinding?.apply {
            emojiPaletteHolder.beGone()
            if (!isAnyPanelOpen()) {
                suggestionsHolder.showAllInlineContentViews()
            }
        }
    }

    private fun setupEmojis() {
        ensureBackgroundThread {
            val fullEmojiList = parseRawEmojiSpecsFile(context, EMOJI_SPEC_FILE_PATH)
            Handler(Looper.getMainLooper()).post {
                setupEmojiAdapter(fullEmojiList)
            }
        }
    }

    private fun prepareEmojiCategories(emojis: List<EmojiData>): Map<String, List<EmojiData>> {
        return mapOf("smileys_emotion" to emojis)
    }

    private fun prepareEmojiItems(categories: Map<String, List<EmojiData>>): List<EmojisAdapter.Item> {
        val emojiItems = mutableListOf<EmojisAdapter.Item>()
        categories.values.forEach { emojis ->
            emojiItems.addAll(emojis.map(EmojisAdapter.Item::Emoji))
        }
        return emojiItems
    }

    private fun setupEmojiAdapter(emojis: List<EmojiData>) {
        val emojiCategories = prepareEmojiCategories(emojis)
        var emojiItems = prepareEmojiItems(emojiCategories)

        val emojiLayoutManager = AutoGridLayoutManager(
            context = context,
            itemWidth = context.resources.getDimensionPixelSize(R.dimen.emoji_item_size)
        ).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (emojiItems[position] is EmojisAdapter.Item.Category) {
                        spanCount
                    } else {
                        1
                    }
                }
            }
        }

        val emojiCategoryIds = mutableMapOf<Int, String>()
        val emojiCategoryColor = mTextColor.adjustAlpha(0.8f)
        keyboardViewBinding?.emojiCategoriesStrip?.apply {
            weightSum = emojiCategories.count().toFloat()
            val strip = this
            removeAllViews()
            emojiCategories.entries.forEach { (category, _) ->
                ItemEmojiCategoryBinding.inflate(
                    LayoutInflater.from(context),
                    this,
                    true
                ).root.apply {
                    id = generateViewId()
                    emojiCategoryIds[id] = category
                    setImageResource(getCategoryIconRes(category))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                    setOnClickListener {
                        strip.children.filterIsInstance<ImageButton>().forEach {
                            it.applyColorFilter(emojiCategoryColor)
                        }
                        applyColorFilter(mPrimaryColor)
                        keyboardViewBinding?.emojisList?.stopScroll()
                        emojiLayoutManager.scrollToPositionWithOffset(
                            emojiItems.indexOfFirst { it is EmojisAdapter.Item.Category && it.value == category },
                            0
                        )
                    }
                    applyColorFilter(emojiCategoryColor)
                }
            }
        }

        keyboardViewBinding?.emojisList?.apply {
            layoutManager = emojiLayoutManager
            adapter = EmojisAdapter(context = safeStorageContext, items = emojiItems) { emoji ->
                mOnKeyboardActionListener!!.onText(emoji.emoji)
                vibrateIfNeeded()

                context.config.addRecentEmoji(emoji.emoji)
                (adapter as? EmojisAdapter)?.apply {
                    emojiItems = prepareEmojiItems(prepareEmojiCategories(emojis))
                    updateItems(emojiItems)
                }
            }

            clearOnScrollListeners()
            onScroll { offset ->
                keyboardViewBinding!!.emojiPaletteTopBar.elevation = when {
                    offset > 4 -> context.resources.getDimensionPixelSize(R.dimen.one_dp).toFloat()
                    else -> 0f
                }

                emojiLayoutManager.findFirstCompletelyVisibleItemPosition()
                    .also { firstVisibleIndex ->
                        emojiItems
                            .withIndex()
                            .lastOrNull { it.value is EmojisAdapter.Item.Category && it.index <= firstVisibleIndex }
                            ?.also { activeCategory ->
                                val id = emojiCategoryIds.entries.first {
                                    it.value == (activeCategory.value as EmojisAdapter.Item.Category).value
                                }.key

                                keyboardViewBinding
                                    ?.emojiCategoriesStrip
                                    ?.children
                                    ?.filterIsInstance<ImageButton>()
                                    ?.forEach { button ->
                                        val selected = button.id == id
                                        button.applyColorFilter(
                                            if (selected) mPrimaryColor else emojiCategoryColor
                                        )
                                    }
                            }
                    }
            }
        }
    }

    private fun closing() {
        if (mPreviewPopup.isShowing) {
            mPreviewPopup.dismiss()
        }
        removeMessages()
        dismissPopupKeyboard()
        mBuffer?.recycle()
        mBuffer = null
        mCanvas = null
        mMiniKeyboardCache.clear()
    }

    private fun removeMessages() {
        mHandler?.apply {
            removeMessages(MSG_REPEAT)
            removeMessages(MSG_LONGPRESS)
        }
        mRepeatCount = 0
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVoiceManager.destroy()
        unregisterClipboardListener()
        removeMessages()
        closing()
    }

    fun setVoiceListeningState(isListening: Boolean) {
        keyboardViewBinding?.apply {
            if (isListening) {
                voiceInputActiveBg.visibility = View.VISIBLE
                voiceInputButton.applyColorFilter(Color.parseColor("#FF4444"))
            } else {
                voiceInputActiveBg.visibility = View.GONE
                voiceInputButton.applyColorFilter(Color.WHITE)
            }
        }
    }

    private fun dismissPopupKeyboard() {
        if (mPopupKeyboard.isShowing) {
            mPopupKeyboard.dismiss()
            mMiniKeyboardOnScreen = false
            setCurrentKeyPressed(false)
            invalidateAllKeys()
        }
    }

    private fun getKeyColor(): Int {
        return Color.parseColor("#202737")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun addToClipboardViews(it: InlineContentView, addToFront: Boolean = false) {
        if (keyboardViewBinding?.autofillSuggestionsHolder != null) {
            val newLayoutParams = LinearLayout.LayoutParams(it.layoutParams)
            newLayoutParams.updateMarginsRelative(start = resources.getDimensionPixelSize(R.dimen.normal_margin))
            it.layoutParams = newLayoutParams
            if (addToFront) {
                keyboardViewBinding?.autofillSuggestionsHolder?.addView(it, 0)
            } else {
                keyboardViewBinding?.autofillSuggestionsHolder?.addView(it)
            }
            updateSuggestionsToolbarLayout()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun clearClipboardViews() {
        keyboardViewBinding?.autofillSuggestionsHolder?.removeAllViews()
        updateSuggestionsToolbarLayout()
    }

    private fun updateSuggestionsToolbarLayout() {
        keyboardViewBinding?.apply {
            if (hasInlineViews()) {
                // make room on suggestion toolbar for inline views
                suggestionsItemsHolder.gravity = Gravity.NO_GRAVITY
                clipboardValue.maxWidth =
                    resources.getDimensionPixelSize(R.dimen.suggestion_max_width)
            } else {
                // restore original clipboard toolbar appearance
                suggestionsItemsHolder.gravity = Gravity.CENTER_HORIZONTAL
                suggestionsHolder.measuredWidth.also { maxWidth ->
                    clipboardValue.maxWidth = maxWidth
                }
            }
        }
    }

    /**
     * Returns true if there are [InlineContentView]s in [autofill_suggestions_holder]
     */
    private fun hasInlineViews() =
        (keyboardViewBinding?.autofillSuggestionsHolder?.childCount ?: 0) > 0

    /**
     * Returns: Popup Key width depends on popup keys count
     */
    private fun MyKeyboard.Key.calcKeyWidth(containerWidth: Int): Int {
        val popupKeyCount = this.popupCharacters!!.length

        return if (popupKeyCount > containerWidth / this.width) {
            containerWidth / popupKeyCount
        } else {
            this.width
        }
    }
}
