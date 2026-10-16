@file:Suppress("MatchingDeclarationName")

package org.fossify.keyboard.extensions

import android.content.Context
import android.graphics.Color
import org.fossify.commons.models.RadioItem
import org.fossify.keyboard.R
import org.fossify.keyboard.helpers.THEME_CALCULATOR_3D
import org.fossify.keyboard.helpers.THEME_CHOCOLATE_3D
import org.fossify.keyboard.helpers.THEME_COCKPIT_3D
import org.fossify.keyboard.helpers.THEME_EMERALD_GOLD_3D
import org.fossify.keyboard.helpers.THEME_GAMEBOY_3D
import org.fossify.keyboard.helpers.THEME_HONEY_3D
import org.fossify.keyboard.helpers.THEME_IOS_DARK
import org.fossify.keyboard.helpers.THEME_LIGHT_SWITCH_3D
import org.fossify.keyboard.helpers.THEME_NEON_CYBER
import org.fossify.keyboard.helpers.THEME_PIANO_3D
import org.fossify.keyboard.helpers.THEME_RETRO_1995
import org.fossify.keyboard.helpers.THEME_TYPEWRITER_3D

data class KeyboardThemeData(
    val id: Int,
    val nameRes: Int,
    val keyboardBgColor: Int,
    val toolbarBgColor: Int,
    val toolbarIconColor: Int,
    // Normal Keys
    val normalKeyTopColor: Int,
    val normalKeyBottomColor: Int,
    val normalKeyHighlightColor: Int,
    val normalKeyShadowColor: Int,
    val normalKeyPressedColor: Int,
    val normalTextColor: Int,
    val normalTextPressedColor: Int,
    val subTextColor: Int,
    // Accent Keys (Shift, 123, AR/EN)
    val accentKeyTopColor: Int,
    val accentKeyBottomColor: Int,
    val accentKeyHighlightColor: Int,
    val accentKeyShadowColor: Int,
    val accentKeyPressedColor: Int,
    val accentTextColor: Int,
    // Enter Key
    val enterKeyTopColor: Int,
    val enterKeyBottomColor: Int,
    val enterKeyHighlightColor: Int,
    val enterKeyShadowColor: Int,
    val enterKeyPressedColor: Int,
    val enterIconColor: Int,
    // Delete Key
    val deleteKeyTopColor: Int,
    val deleteKeyBottomColor: Int,
    val deleteKeyHighlightColor: Int,
    val deleteKeyShadowColor: Int,
    val deleteKeyPressedColor: Int,
    val deleteIconColor: Int,
    // Space Key
    val spaceKeyTopColor: Int,
    val spaceKeyBottomColor: Int,
    // Dimensions
    val cornerRadiusDp: Float,
    val elevationDp: Float
)

val CYBER_NEON_THEME = KeyboardThemeData(
    id = THEME_NEON_CYBER,
    nameRes = R.string.theme_cyber_neon,
    keyboardBgColor = Color.parseColor("#060B14"),
    toolbarBgColor = Color.parseColor("#0A1424"),
    toolbarIconColor = Color.parseColor("#00E5FF"),
    normalKeyTopColor = Color.parseColor("#223B5E"),
    normalKeyBottomColor = Color.parseColor("#14253D"),
    normalKeyHighlightColor = Color.parseColor("#4477A8"),
    normalKeyShadowColor = Color.parseColor("#070E1A"),
    normalKeyPressedColor = Color.parseColor("#0E4F7D"),
    normalTextColor = Color.parseColor("#FFFFFF"),
    normalTextPressedColor = Color.parseColor("#00E5FF"),
    subTextColor = Color.parseColor("#00E5FF"),
    accentKeyTopColor = Color.parseColor("#1B3354"),
    accentKeyBottomColor = Color.parseColor("#102035"),
    accentKeyHighlightColor = Color.parseColor("#00E5FF"),
    accentKeyShadowColor = Color.parseColor("#060C14"),
    accentKeyPressedColor = Color.parseColor("#18426B"),
    accentTextColor = Color.parseColor("#00E5FF"),
    enterKeyTopColor = Color.parseColor("#00A3E0"),
    enterKeyBottomColor = Color.parseColor("#006B99"),
    enterKeyHighlightColor = Color.parseColor("#40E6FF"),
    enterKeyShadowColor = Color.parseColor("#003C59"),
    enterKeyPressedColor = Color.parseColor("#00BFFF"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#1B3354"),
    deleteKeyBottomColor = Color.parseColor("#102035"),
    deleteKeyHighlightColor = Color.parseColor("#00E5FF"),
    deleteKeyShadowColor = Color.parseColor("#060C14"),
    deleteKeyPressedColor = Color.parseColor("#18426B"),
    deleteIconColor = Color.parseColor("#00E5FF"),
    spaceKeyTopColor = Color.parseColor("#1C3250"),
    spaceKeyBottomColor = Color.parseColor("#122237"),
    cornerRadiusDp = 9f,
    elevationDp = 4.2f
)

val HONEY_3D_THEME = KeyboardThemeData(
    id = THEME_HONEY_3D,
    nameRes = R.string.theme_honey_3d,
    keyboardBgColor = Color.parseColor("#150B02"),
    toolbarBgColor = Color.parseColor("#221104"),
    toolbarIconColor = Color.parseColor("#F59E0B"),
    normalKeyTopColor = Color.parseColor("#EA8A0A"),
    normalKeyBottomColor = Color.parseColor("#B85B04"),
    normalKeyHighlightColor = Color.parseColor("#FDE68A"),
    normalKeyShadowColor = Color.parseColor("#381501"),
    normalKeyPressedColor = Color.parseColor("#994B02"),
    normalTextColor = Color.parseColor("#FFFBEB"),
    normalTextPressedColor = Color.parseColor("#FEF3C7"),
    subTextColor = Color.parseColor("#FDE047"),
    accentKeyTopColor = Color.parseColor("#853A04"),
    accentKeyBottomColor = Color.parseColor("#542202"),
    accentKeyHighlightColor = Color.parseColor("#F59E0B"),
    accentKeyShadowColor = Color.parseColor("#210A00"),
    accentKeyPressedColor = Color.parseColor("#3B1701"),
    accentTextColor = Color.parseColor("#FEF3C7"),
    enterKeyTopColor = Color.parseColor("#F59E0B"),
    enterKeyBottomColor = Color.parseColor("#D97706"),
    enterKeyHighlightColor = Color.parseColor("#FEF08A"),
    enterKeyShadowColor = Color.parseColor("#5C2402"),
    enterKeyPressedColor = Color.parseColor("#B45309"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#853A04"),
    deleteKeyBottomColor = Color.parseColor("#542202"),
    deleteKeyHighlightColor = Color.parseColor("#F59E0B"),
    deleteKeyShadowColor = Color.parseColor("#210A00"),
    deleteKeyPressedColor = Color.parseColor("#3B1701"),
    deleteIconColor = Color.parseColor("#FCD34D"),
    spaceKeyTopColor = Color.parseColor("#D97706"),
    spaceKeyBottomColor = Color.parseColor("#A34F03"),
    cornerRadiusDp = 9f,
    elevationDp = 4.8f
)

val RETRO_1995_THEME = KeyboardThemeData(
    id = THEME_RETRO_1995,
    nameRes = R.string.theme_retro_1995,
    keyboardBgColor = Color.parseColor("#1C1A18"),
    toolbarBgColor = Color.parseColor("#292623"),
    toolbarIconColor = Color.parseColor("#3B82F6"),
    normalKeyTopColor = Color.parseColor("#E2DDD1"),
    normalKeyBottomColor = Color.parseColor("#C8C2B3"),
    normalKeyHighlightColor = Color.parseColor("#F5F2E9"),
    normalKeyShadowColor = Color.parseColor("#141311"),
    normalKeyPressedColor = Color.parseColor("#B3ACA0"),
    normalTextColor = Color.parseColor("#1A1816"),
    normalTextPressedColor = Color.parseColor("#0F0E0D"),
    subTextColor = Color.parseColor("#1D4ED8"),
    accentKeyTopColor = Color.parseColor("#AFA89B"),
    accentKeyBottomColor = Color.parseColor("#948E82"),
    accentKeyHighlightColor = Color.parseColor("#CCC6B9"),
    accentKeyShadowColor = Color.parseColor("#11100F"),
    accentKeyPressedColor = Color.parseColor("#7E796F"),
    accentTextColor = Color.parseColor("#1A1816"),
    enterKeyTopColor = Color.parseColor("#1D4ED8"),
    enterKeyBottomColor = Color.parseColor("#1E40AF"),
    enterKeyHighlightColor = Color.parseColor("#60A5FA"),
    enterKeyShadowColor = Color.parseColor("#0F172A"),
    enterKeyPressedColor = Color.parseColor("#172554"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#AFA89B"),
    deleteKeyBottomColor = Color.parseColor("#948E82"),
    deleteKeyHighlightColor = Color.parseColor("#CCC6B9"),
    deleteKeyShadowColor = Color.parseColor("#11100F"),
    deleteKeyPressedColor = Color.parseColor("#7E796F"),
    deleteIconColor = Color.parseColor("#1A1816"),
    spaceKeyTopColor = Color.parseColor("#DDD8CB"),
    spaceKeyBottomColor = Color.parseColor("#C2BCAD"),
    cornerRadiusDp = 5f,
    elevationDp = 5.2f
)

val IOS_DARK_THEME = KeyboardThemeData(
    id = THEME_IOS_DARK,
    nameRes = R.string.theme_ios_dark,
    keyboardBgColor = Color.parseColor("#0E0E10"),
    toolbarBgColor = Color.parseColor("#151518"),
    toolbarIconColor = Color.parseColor("#0A84FF"),
    normalKeyTopColor = Color.parseColor("#424247"),
    normalKeyBottomColor = Color.parseColor("#2E2E33"),
    normalKeyHighlightColor = Color.parseColor("#62626B"),
    normalKeyShadowColor = Color.parseColor("#0A0A0C"),
    normalKeyPressedColor = Color.parseColor("#56565C"),
    normalTextColor = Color.parseColor("#FFFFFF"),
    normalTextPressedColor = Color.parseColor("#D1D1D6"),
    subTextColor = Color.parseColor("#98989D"),
    accentKeyTopColor = Color.parseColor("#2A2A2E"),
    accentKeyBottomColor = Color.parseColor("#1C1C20"),
    accentKeyHighlightColor = Color.parseColor("#48484E"),
    accentKeyShadowColor = Color.parseColor("#08080A"),
    accentKeyPressedColor = Color.parseColor("#3A3A40"),
    accentTextColor = Color.parseColor("#0A84FF"),
    enterKeyTopColor = Color.parseColor("#007AFF"),
    enterKeyBottomColor = Color.parseColor("#0056B3"),
    enterKeyHighlightColor = Color.parseColor("#54A6FF"),
    enterKeyShadowColor = Color.parseColor("#003870"),
    enterKeyPressedColor = Color.parseColor("#0A84FF"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#2A2A2E"),
    deleteKeyBottomColor = Color.parseColor("#1C1C20"),
    deleteKeyHighlightColor = Color.parseColor("#48484E"),
    deleteKeyShadowColor = Color.parseColor("#08080A"),
    deleteKeyPressedColor = Color.parseColor("#3A3A40"),
    deleteIconColor = Color.parseColor("#D1D1D6"),
    spaceKeyTopColor = Color.parseColor("#3E3E43"),
    spaceKeyBottomColor = Color.parseColor("#2B2B2F"),
    cornerRadiusDp = 8f,
    elevationDp = 4f
)

val PIANO_3D_THEME = KeyboardThemeData(
    id = THEME_PIANO_3D,
    nameRes = R.string.theme_piano_3d,
    keyboardBgColor = Color.parseColor("#070709"),
    toolbarBgColor = Color.parseColor("#111115"),
    toolbarIconColor = Color.parseColor("#EAB308"),
    normalKeyTopColor = Color.parseColor("#F5EFE0"),
    normalKeyBottomColor = Color.parseColor("#DED5C0"),
    normalKeyHighlightColor = Color.parseColor("#FFFDF7"),
    normalKeyShadowColor = Color.parseColor("#080706"),
    normalKeyPressedColor = Color.parseColor("#CDC2A8"),
    normalTextColor = Color.parseColor("#1C1814"),
    normalTextPressedColor = Color.parseColor("#0F0C0A"),
    subTextColor = Color.parseColor("#B45309"),
    accentKeyTopColor = Color.parseColor("#2A2A32"),
    accentKeyBottomColor = Color.parseColor("#16161B"),
    accentKeyHighlightColor = Color.parseColor("#484856"),
    accentKeyShadowColor = Color.parseColor("#040405"),
    accentKeyPressedColor = Color.parseColor("#101014"),
    accentTextColor = Color.parseColor("#F5EFE0"),
    enterKeyTopColor = Color.parseColor("#D97706"),
    enterKeyBottomColor = Color.parseColor("#92400E"),
    enterKeyHighlightColor = Color.parseColor("#FCD34D"),
    enterKeyShadowColor = Color.parseColor("#451A03"),
    enterKeyPressedColor = Color.parseColor("#78350F"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#2A2A32"),
    deleteKeyBottomColor = Color.parseColor("#16161B"),
    deleteKeyHighlightColor = Color.parseColor("#484856"),
    deleteKeyShadowColor = Color.parseColor("#040405"),
    deleteKeyPressedColor = Color.parseColor("#101014"),
    deleteIconColor = Color.parseColor("#EAB308"),
    spaceKeyTopColor = Color.parseColor("#ECE4D3"),
    spaceKeyBottomColor = Color.parseColor("#D3C7AE"),
    cornerRadiusDp = 7f,
    elevationDp = 5.0f
)

val LIGHT_SWITCH_3D_THEME = KeyboardThemeData(
    id = THEME_LIGHT_SWITCH_3D,
    nameRes = R.string.theme_light_switch_3d,
    keyboardBgColor = Color.parseColor("#181C22"),
    toolbarBgColor = Color.parseColor("#20252D"),
    toolbarIconColor = Color.parseColor("#FF9500"),
    normalKeyTopColor = Color.parseColor("#C5CCD5"),
    normalKeyBottomColor = Color.parseColor("#A7B0BD"),
    normalKeyHighlightColor = Color.parseColor("#E2E7ED"),
    normalKeyShadowColor = Color.parseColor("#0D1117"),
    normalKeyPressedColor = Color.parseColor("#959EA9"),
    normalTextColor = Color.parseColor("#151B26"),
    normalTextPressedColor = Color.parseColor("#0C1017"),
    subTextColor = Color.parseColor("#FF9500"),
    accentKeyTopColor = Color.parseColor("#485260"),
    accentKeyBottomColor = Color.parseColor("#343C47"),
    accentKeyHighlightColor = Color.parseColor("#677384"),
    accentKeyShadowColor = Color.parseColor("#0D1117"),
    accentKeyPressedColor = Color.parseColor("#262C34"),
    accentTextColor = Color.parseColor("#F1F5F9"),
    enterKeyTopColor = Color.parseColor("#FF7A00"),
    enterKeyBottomColor = Color.parseColor("#D95F00"),
    enterKeyHighlightColor = Color.parseColor("#FFB066"),
    enterKeyShadowColor = Color.parseColor("#662C00"),
    enterKeyPressedColor = Color.parseColor("#B34E00"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#485260"),
    deleteKeyBottomColor = Color.parseColor("#343C47"),
    deleteKeyHighlightColor = Color.parseColor("#677384"),
    deleteKeyShadowColor = Color.parseColor("#0D1117"),
    deleteKeyPressedColor = Color.parseColor("#262C34"),
    deleteIconColor = Color.parseColor("#FF9500"),
    spaceKeyTopColor = Color.parseColor("#BAC1CC"),
    spaceKeyBottomColor = Color.parseColor("#9DA7B4"),
    cornerRadiusDp = 6f,
    elevationDp = 5.0f
)

val TYPEWRITER_3D_THEME = KeyboardThemeData(
    id = THEME_TYPEWRITER_3D,
    nameRes = R.string.theme_typewriter_3d,
    keyboardBgColor = Color.parseColor("#121110"),
    toolbarBgColor = Color.parseColor("#1B1917"),
    toolbarIconColor = Color.parseColor("#E7E5E4"),
    normalKeyTopColor = Color.parseColor("#FAF7F0"),
    normalKeyBottomColor = Color.parseColor("#DDD6C6"),
    normalKeyHighlightColor = Color.parseColor("#D4D4D8"),
    normalKeyShadowColor = Color.parseColor("#0A0908"),
    normalKeyPressedColor = Color.parseColor("#C8BFAC"),
    normalTextColor = Color.parseColor("#1C1917"),
    normalTextPressedColor = Color.parseColor("#0C0A09"),
    subTextColor = Color.parseColor("#8A382A"),
    accentKeyTopColor = Color.parseColor("#262320"),
    accentKeyBottomColor = Color.parseColor("#161412"),
    accentKeyHighlightColor = Color.parseColor("#A1A1AA"),
    accentKeyShadowColor = Color.parseColor("#0A0908"),
    accentKeyPressedColor = Color.parseColor("#38332E"),
    accentTextColor = Color.parseColor("#FAF7F0"),
    enterKeyTopColor = Color.parseColor("#991B1B"),
    enterKeyBottomColor = Color.parseColor("#6B1111"),
    enterKeyHighlightColor = Color.parseColor("#EF4444"),
    enterKeyShadowColor = Color.parseColor("#380606"),
    enterKeyPressedColor = Color.parseColor("#7F1D1D"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#262320"),
    deleteKeyBottomColor = Color.parseColor("#161412"),
    deleteKeyHighlightColor = Color.parseColor("#A1A1AA"),
    deleteKeyShadowColor = Color.parseColor("#0A0908"),
    deleteKeyPressedColor = Color.parseColor("#38332E"),
    deleteIconColor = Color.parseColor("#FAF7F0"),
    spaceKeyTopColor = Color.parseColor("#EAE4D3"),
    spaceKeyBottomColor = Color.parseColor("#CDC3AD"),
    cornerRadiusDp = 14f,
    elevationDp = 5.2f
)

val CALCULATOR_3D_THEME = KeyboardThemeData(
    id = THEME_CALCULATOR_3D,
    nameRes = R.string.theme_calculator_3d,
    keyboardBgColor = Color.parseColor("#1A202C"),
    toolbarBgColor = Color.parseColor("#222B38"),
    toolbarIconColor = Color.parseColor("#38BDF8"),
    normalKeyTopColor = Color.parseColor("#4A5568"),
    normalKeyBottomColor = Color.parseColor("#323B4A"),
    normalKeyHighlightColor = Color.parseColor("#718096"),
    normalKeyShadowColor = Color.parseColor("#0F141C"),
    normalKeyPressedColor = Color.parseColor("#2D3748"),
    normalTextColor = Color.parseColor("#F7FAFC"),
    normalTextPressedColor = Color.parseColor("#CBD5E0"),
    subTextColor = Color.parseColor("#FBBF24"),
    accentKeyTopColor = Color.parseColor("#1E3A8A"),
    accentKeyBottomColor = Color.parseColor("#172554"),
    accentKeyHighlightColor = Color.parseColor("#60A5FA"),
    accentKeyShadowColor = Color.parseColor("#0A1128"),
    accentKeyPressedColor = Color.parseColor("#1E293B"),
    accentTextColor = Color.parseColor("#93C5FD"),
    enterKeyTopColor = Color.parseColor("#EA580C"),
    enterKeyBottomColor = Color.parseColor("#9A3412"),
    enterKeyHighlightColor = Color.parseColor("#FB923C"),
    enterKeyShadowColor = Color.parseColor("#431407"),
    enterKeyPressedColor = Color.parseColor("#C2410C"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#B91C1C"),
    deleteKeyBottomColor = Color.parseColor("#7F1D1D"),
    deleteKeyHighlightColor = Color.parseColor("#F87171"),
    deleteKeyShadowColor = Color.parseColor("#450A0A"),
    deleteKeyPressedColor = Color.parseColor("#991B1B"),
    deleteIconColor = Color.parseColor("#FFFFFF"),
    spaceKeyTopColor = Color.parseColor("#3F4958"),
    spaceKeyBottomColor = Color.parseColor("#2A323D"),
    cornerRadiusDp = 5f,
    elevationDp = 4.8f
)

val CHOCOLATE_3D_THEME = KeyboardThemeData(
    id = THEME_CHOCOLATE_3D,
    nameRes = R.string.theme_chocolate_3d,
    keyboardBgColor = Color.parseColor("#150A05"),
    toolbarBgColor = Color.parseColor("#221008"),
    toolbarIconColor = Color.parseColor("#FBBF24"),
    normalKeyTopColor = Color.parseColor("#452313"),
    normalKeyBottomColor = Color.parseColor("#2B150A"),
    normalKeyHighlightColor = Color.parseColor("#783D21"),
    normalKeyShadowColor = Color.parseColor("#0D0603"),
    normalKeyPressedColor = Color.parseColor("#210F07"),
    normalTextColor = Color.parseColor("#FFFBEB"),
    normalTextPressedColor = Color.parseColor("#FDE68A"),
    subTextColor = Color.parseColor("#F59E0B"),
    accentKeyTopColor = Color.parseColor("#2E160B"),
    accentKeyBottomColor = Color.parseColor("#1B0C06"),
    accentKeyHighlightColor = Color.parseColor("#5A2D17"),
    accentKeyShadowColor = Color.parseColor("#080302"),
    accentKeyPressedColor = Color.parseColor("#150904"),
    accentTextColor = Color.parseColor("#FCD34D"),
    enterKeyTopColor = Color.parseColor("#B45309"),
    enterKeyBottomColor = Color.parseColor("#78350F"),
    enterKeyHighlightColor = Color.parseColor("#FBBF24"),
    enterKeyShadowColor = Color.parseColor("#351503"),
    enterKeyPressedColor = Color.parseColor("#92400E"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#9F1239"),
    deleteKeyBottomColor = Color.parseColor("#640722"),
    deleteKeyHighlightColor = Color.parseColor("#F43F5E"),
    deleteKeyShadowColor = Color.parseColor("#2F020E"),
    deleteKeyPressedColor = Color.parseColor("#7D0C2A"),
    deleteIconColor = Color.parseColor("#FFF1F2"),
    spaceKeyTopColor = Color.parseColor("#3C1E10"),
    spaceKeyBottomColor = Color.parseColor("#241108"),
    cornerRadiusDp = 8f,
    elevationDp = 5.0f
)

val GAMEBOY_3D_THEME = KeyboardThemeData(
    id = THEME_GAMEBOY_3D,
    nameRes = R.string.theme_gameboy_3d,
    keyboardBgColor = Color.parseColor("#1E1F24"),
    toolbarBgColor = Color.parseColor("#292A30"),
    toolbarIconColor = Color.parseColor("#B82662"),
    normalKeyTopColor = Color.parseColor("#4A4C56"),
    normalKeyBottomColor = Color.parseColor("#31333B"),
    normalKeyHighlightColor = Color.parseColor("#6E717F"),
    normalKeyShadowColor = Color.parseColor("#101114"),
    normalKeyPressedColor = Color.parseColor("#27282F"),
    normalTextColor = Color.parseColor("#F1F2F6"),
    normalTextPressedColor = Color.parseColor("#C2C5D1"),
    subTextColor = Color.parseColor("#80A0C0"),
    accentKeyTopColor = Color.parseColor("#363842"),
    accentKeyBottomColor = Color.parseColor("#202127"),
    accentKeyHighlightColor = Color.parseColor("#525563"),
    accentKeyShadowColor = Color.parseColor("#0C0D0F"),
    accentKeyPressedColor = Color.parseColor("#1A1B20"),
    accentTextColor = Color.parseColor("#E0E3EC"),
    enterKeyTopColor = Color.parseColor("#B82662"),
    enterKeyBottomColor = Color.parseColor("#801440"),
    enterKeyHighlightColor = Color.parseColor("#E54388"),
    enterKeyShadowColor = Color.parseColor("#420820"),
    enterKeyPressedColor = Color.parseColor("#941B4D"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#8E1F4D"),
    deleteKeyBottomColor = Color.parseColor("#601032"),
    deleteKeyHighlightColor = Color.parseColor("#C93874"),
    deleteKeyShadowColor = Color.parseColor("#300518"),
    deleteKeyPressedColor = Color.parseColor("#6F153B"),
    deleteIconColor = Color.parseColor("#FFFFFF"),
    spaceKeyTopColor = Color.parseColor("#42444D"),
    spaceKeyBottomColor = Color.parseColor("#2A2B32"),
    cornerRadiusDp = 9f,
    elevationDp = 4.8f
)

val COCKPIT_3D_THEME = KeyboardThemeData(
    id = THEME_COCKPIT_3D,
    nameRes = R.string.theme_cockpit_3d,
    keyboardBgColor = Color.parseColor("#0D1117"),
    toolbarBgColor = Color.parseColor("#161B22"),
    toolbarIconColor = Color.parseColor("#22C55E"),
    normalKeyTopColor = Color.parseColor("#272E38"),
    normalKeyBottomColor = Color.parseColor("#181D24"),
    normalKeyHighlightColor = Color.parseColor("#434F5F"),
    normalKeyShadowColor = Color.parseColor("#080B0F"),
    normalKeyPressedColor = Color.parseColor("#141920"),
    normalTextColor = Color.parseColor("#F0F6FC"),
    normalTextPressedColor = Color.parseColor("#22C55E"),
    subTextColor = Color.parseColor("#22C55E"),
    accentKeyTopColor = Color.parseColor("#1F242C"),
    accentKeyBottomColor = Color.parseColor("#13161B"),
    accentKeyHighlightColor = Color.parseColor("#3A4350"),
    accentKeyShadowColor = Color.parseColor("#06080B"),
    accentKeyPressedColor = Color.parseColor("#0E1115"),
    accentTextColor = Color.parseColor("#38BDF8"),
    enterKeyTopColor = Color.parseColor("#DC2626"),
    enterKeyBottomColor = Color.parseColor("#991B1B"),
    enterKeyHighlightColor = Color.parseColor("#F87171"),
    enterKeyShadowColor = Color.parseColor("#450A0A"),
    enterKeyPressedColor = Color.parseColor("#B91C1C"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#CA8A04"),
    deleteKeyBottomColor = Color.parseColor("#854D0E"),
    deleteKeyHighlightColor = Color.parseColor("#FDE047"),
    deleteKeyShadowColor = Color.parseColor("#3D2204"),
    deleteKeyPressedColor = Color.parseColor("#A16207"),
    deleteIconColor = Color.parseColor("#FFFFFF"),
    spaceKeyTopColor = Color.parseColor("#242A33"),
    spaceKeyBottomColor = Color.parseColor("#15191F"),
    cornerRadiusDp = 5f,
    elevationDp = 5.2f
)

val EMERALD_GOLD_3D_THEME = KeyboardThemeData(
    id = THEME_EMERALD_GOLD_3D,
    nameRes = R.string.theme_emerald_gold_3d,
    keyboardBgColor = Color.parseColor("#04120A"),
    toolbarBgColor = Color.parseColor("#071E10"),
    toolbarIconColor = Color.parseColor("#F59E0B"),
    normalKeyTopColor = Color.parseColor("#0B4628"),
    normalKeyBottomColor = Color.parseColor("#062917"),
    normalKeyHighlightColor = Color.parseColor("#1FB86E"),
    normalKeyShadowColor = Color.parseColor("#020B06"),
    normalKeyPressedColor = Color.parseColor("#042012"),
    normalTextColor = Color.parseColor("#FEF3C7"),
    normalTextPressedColor = Color.parseColor("#FDE68A"),
    subTextColor = Color.parseColor("#F59E0B"),
    accentKeyTopColor = Color.parseColor("#07301B"),
    accentKeyBottomColor = Color.parseColor("#041C0F"),
    accentKeyHighlightColor = Color.parseColor("#157849"),
    accentKeyShadowColor = Color.parseColor("#020905"),
    accentKeyPressedColor = Color.parseColor("#03140B"),
    accentTextColor = Color.parseColor("#FDE68A"),
    enterKeyTopColor = Color.parseColor("#D97706"),
    enterKeyBottomColor = Color.parseColor("#92400E"),
    enterKeyHighlightColor = Color.parseColor("#FDE047"),
    enterKeyShadowColor = Color.parseColor("#381501"),
    enterKeyPressedColor = Color.parseColor("#B45309"),
    enterIconColor = Color.parseColor("#FFFFFF"),
    deleteKeyTopColor = Color.parseColor("#07301B"),
    deleteKeyBottomColor = Color.parseColor("#041C0F"),
    deleteKeyHighlightColor = Color.parseColor("#157849"),
    deleteKeyShadowColor = Color.parseColor("#020905"),
    deleteKeyPressedColor = Color.parseColor("#03140B"),
    deleteIconColor = Color.parseColor("#FCD34D"),
    spaceKeyTopColor = Color.parseColor("#093C22"),
    spaceKeyBottomColor = Color.parseColor("#052213"),
    cornerRadiusDp = 8f,
    elevationDp = 5.0f
)

fun Context.getCurrentKeyboardTheme(): KeyboardThemeData {
    return when (config.keyboardTheme) {
        THEME_HONEY_3D -> HONEY_3D_THEME
        THEME_RETRO_1995 -> RETRO_1995_THEME
        THEME_IOS_DARK -> IOS_DARK_THEME
        THEME_PIANO_3D -> PIANO_3D_THEME
        THEME_LIGHT_SWITCH_3D -> LIGHT_SWITCH_3D_THEME
        THEME_TYPEWRITER_3D -> TYPEWRITER_3D_THEME
        THEME_CALCULATOR_3D -> CALCULATOR_3D_THEME
        THEME_CHOCOLATE_3D -> CHOCOLATE_3D_THEME
        THEME_GAMEBOY_3D -> GAMEBOY_3D_THEME
        THEME_COCKPIT_3D -> COCKPIT_3D_THEME
        THEME_EMERALD_GOLD_3D -> EMERALD_GOLD_3D_THEME
        else -> CYBER_NEON_THEME
    }
}

fun Context.getKeyboardBackgroundColor(): Int {
    return getCurrentKeyboardTheme().keyboardBgColor
}

fun Context.getStrokeColor(): Int {
    return getCurrentKeyboardTheme().normalKeyHighlightColor
}

fun Context.getKeyboardThemesRadioItems(): ArrayList<RadioItem> {
    return arrayListOf(
        RadioItem(THEME_NEON_CYBER, getString(R.string.theme_cyber_neon)),
        RadioItem(THEME_HONEY_3D, getString(R.string.theme_honey_3d)),
        RadioItem(THEME_RETRO_1995, getString(R.string.theme_retro_1995)),
        RadioItem(THEME_IOS_DARK, getString(R.string.theme_ios_dark)),
        RadioItem(THEME_PIANO_3D, getString(R.string.theme_piano_3d)),
        RadioItem(THEME_LIGHT_SWITCH_3D, getString(R.string.theme_light_switch_3d)),
        RadioItem(THEME_TYPEWRITER_3D, getString(R.string.theme_typewriter_3d)),
        RadioItem(THEME_CALCULATOR_3D, getString(R.string.theme_calculator_3d)),
        RadioItem(THEME_CHOCOLATE_3D, getString(R.string.theme_chocolate_3d)),
        RadioItem(THEME_GAMEBOY_3D, getString(R.string.theme_gameboy_3d)),
        RadioItem(THEME_COCKPIT_3D, getString(R.string.theme_cockpit_3d)),
        RadioItem(THEME_EMERALD_GOLD_3D, getString(R.string.theme_emerald_gold_3d))
    )
}

fun Context.getKeyboardThemeName(themeId: Int): String {
    return when (themeId) {
        THEME_HONEY_3D -> getString(R.string.theme_honey_3d)
        THEME_RETRO_1995 -> getString(R.string.theme_retro_1995)
        THEME_IOS_DARK -> getString(R.string.theme_ios_dark)
        THEME_PIANO_3D -> getString(R.string.theme_piano_3d)
        THEME_LIGHT_SWITCH_3D -> getString(R.string.theme_light_switch_3d)
        THEME_TYPEWRITER_3D -> getString(R.string.theme_typewriter_3d)
        THEME_CALCULATOR_3D -> getString(R.string.theme_calculator_3d)
        THEME_CHOCOLATE_3D -> getString(R.string.theme_chocolate_3d)
        THEME_GAMEBOY_3D -> getString(R.string.theme_gameboy_3d)
        THEME_COCKPIT_3D -> getString(R.string.theme_cockpit_3d)
        THEME_EMERALD_GOLD_3D -> getString(R.string.theme_emerald_gold_3d)
        else -> getString(R.string.theme_cyber_neon)
    }
}
