package org.fossify.keyboard.extensions

import android.app.KeyguardManager
import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.fossify.commons.databinding.DialogTitleBinding
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getColoredDrawableWithColor
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.isBlackAndWhiteTheme
import org.fossify.commons.extensions.isDynamicTheme
import org.fossify.commons.extensions.updateTextColors
import org.fossify.commons.helpers.isNougatPlus
import org.fossify.commons.models.RadioItem
import org.fossify.commons.views.MyTextView
import org.fossify.keyboard.R
import org.fossify.keyboard.databases.ClipsDatabase
import org.fossify.keyboard.helpers.Config
import org.fossify.keyboard.helpers.INPUT_METHOD_SUBTYPE_VOICE
import org.fossify.keyboard.interfaces.ClipsDao

val Context.config: Config get() = Config.newInstance(applicationContext.safeStorageContext)

val Context.safeStorageContext: Context
    get() = if (isDeviceInDirectBootMode) {
        createDeviceProtectedStorageContext()
    } else {
        this
    }

val Context.isDeviceInDirectBootMode: Boolean
    get() {
        val userManager = getSystemService(Context.USER_SERVICE) as UserManager
        return isNougatPlus() && !userManager.isUserUnlocked
    }

val Context.isDeviceLocked: Boolean
    get() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceLocked || keyguardManager.isKeyguardLocked || isDeviceInDirectBootMode
    }

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(InputMethodService.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.clipsDB: ClipsDao
    get() = ClipsDatabase.getInstance(applicationContext.safeStorageContext).ClipsDao()

fun Context.getCurrentClip(): String? {
    return try {
        val clipboardManager = (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager) ?: return null
        val clip = clipboardManager.primaryClip ?: return null
        if (clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            item?.text?.toString()?.trim() ?: item?.coerceToText(this)?.toString()?.trim()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun Context.getKeyboardDialogBuilder() = if (safeStorageContext.isDynamicTheme()) {
    MaterialAlertDialogBuilder(this, R.style.MyKeyboard_Alert)
} else {
    AlertDialog.Builder(this, R.style.MyKeyboard_Alert)
}

fun Context.setupKeyboardDialogStuff(
    windowToken: IBinder,
    view: View,
    dialog: AlertDialog.Builder,
    titleId: Int = 0,
    titleText: String = "",
    cancelOnTouchOutside: Boolean = true,
    callback: ((alertDialog: AlertDialog) -> Unit)? = null
) {
    val textColor = getProperTextColor()
    val backgroundColor = getProperBackgroundColor()
    val primaryColor = getProperPrimaryColor()
    if (view is ViewGroup) {
        updateTextColors(view)
    } else if (view is MyTextView) {
        view.setColors(textColor, primaryColor, backgroundColor)
    }

    if (dialog is MaterialAlertDialogBuilder) {
        dialog.create().apply {
            if (titleId != 0) {
                setTitle(titleId)
            } else if (titleText.isNotEmpty()) {
                setTitle(titleText)
            }

            val lp = window?.attributes
            lp?.token = windowToken
            lp?.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            window?.attributes = lp
            window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

            setView(view)
            setCancelable(cancelOnTouchOutside)
            show()

            val bgDrawable = when {
                isBlackAndWhiteTheme() -> ResourcesCompat.getDrawable(
                    resources, R.drawable.black_dialog_background, theme
                )

                isDynamicTheme() -> ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_you_background, theme
                )

                else -> resources.getColoredDrawableWithColor(
                    drawableId = R.drawable.dialog_bg,
                    color = baseConfig.backgroundColor
                )
            }

            window?.setBackgroundDrawable(bgDrawable)
            callback?.invoke(this)
        }
    } else {
        var title: TextView? = null
        if (titleId != 0 || titleText.isNotEmpty()) {
            title =
                DialogTitleBinding
                    .inflate(LayoutInflater.from(this))
                    .dialogTitleTextview.apply {
                        if (titleText.isNotEmpty()) {
                            text = titleText
                        } else {
                            setText(titleId)
                        }
                        setTextColor(textColor)
                    }
        }

        // if we use the same primary and background color, use the text color for dialog confirmation buttons
        val dialogButtonColor = if (primaryColor == baseConfig.backgroundColor) {
            textColor
        } else {
            primaryColor
        }

        dialog.create().apply {
            setView(view)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCustomTitle(title)

            val lp = window?.attributes
            lp?.token = windowToken
            lp?.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            window?.attributes = lp
            window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

            setCanceledOnTouchOutside(cancelOnTouchOutside)
            show()
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(dialogButtonColor)

            val bgDrawable = when {
                isBlackAndWhiteTheme() -> ResourcesCompat.getDrawable(
                    resources, R.drawable.black_dialog_background, theme
                )

                isDynamicTheme() -> ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_you_background, theme
                )

                else -> resources.getColoredDrawableWithColor(
                    drawableId = R.drawable.dialog_bg,
                    color = baseConfig.backgroundColor
                )
            }

            window?.setBackgroundDrawable(bgDrawable)
            callback?.invoke(this)
        }
    }
}

fun Context.getVoiceInputMethods(
    imm: InputMethodManager = inputMethodManager
): List<Pair<InputMethodInfo, InputMethodSubtype>> {
    val result = ArrayList<Pair<InputMethodInfo, InputMethodSubtype>>()
    for (im in imm.enabledInputMethodList) {
        val subtypes = imm.getEnabledInputMethodSubtypeList(im, true)
        val voiceSubtypes = subtypes.filter { it.mode == INPUT_METHOD_SUBTYPE_VOICE }
        if (voiceSubtypes.isNotEmpty()) {
            for (st in voiceSubtypes) {
                result.add(im to st)
            }
        } else if (im.id.contains("voice", ignoreCase = true) ||
            im.packageName.contains("voice", ignoreCase = true) ||
            im.id.contains("googlequicksearchbox", ignoreCase = true)
        ) {
            val fallbackSubtype = subtypes.firstOrNull() ?: InputMethodSubtype.InputMethodSubtypeBuilder().build()
            result.add(im to fallbackSubtype)
        }
    }
    return result
}

fun Context.getCurrentVoiceInputMethod(
    inputMethods: List<Pair<InputMethodInfo, InputMethodSubtype>> = getVoiceInputMethods()
): Pair<InputMethodInfo, InputMethodSubtype>? {
    val configured = inputMethods.find { it.first.id == config.voiceInputMethod }
    if (configured != null) return configured
    val fallback = inputMethods.firstOrNull()
    if (fallback != null) {
        config.voiceInputMethod = fallback.first.id
    }
    return fallback
}

fun Context.getVoiceInputRadioItems(
    inputMethods: List<Pair<InputMethodInfo, InputMethodSubtype>> = getVoiceInputMethods()
): ArrayList<RadioItem> {
    val radioItems = arrayListOf(RadioItem(id = -1, title = getString(R.string.none)))
    for ((index, pair) in inputMethods.withIndex()) {
        radioItems += RadioItem(id = index, title = pair.first.loadLabel(packageManager).toString())
    }

    return radioItems
}
