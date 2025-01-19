package com.mayor.kavi.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import com.mayor.kavi.R

val AdlamFont = FontFamily(
    Font(R.font.adlam_display_regular, FontWeight.Normal)
)

val AdlamTextStyle = TextStyle(
    fontFamily = AdlamFont,
    fontSize = 16.sp // Customize font size, color, etc.
)

val DynaPuffFont = FontFamily(
    Font(R.font.dynapuff_regular, FontWeight.Normal),
    Font(R.font.dynapuff_medium, FontWeight.Medium),
    Font(R.font.dynapuff_semibold, FontWeight.SemiBold),
    Font(R.font.dynapuff_bold, FontWeight.Bold),
    Font(R.font.dynapuff_condensed_regular, FontWeight.Normal, FontStyle.Italic), // Condensed Regular
    Font(R.font.dynapuff_condensed_medium, FontWeight.Medium, FontStyle.Italic), // Condensed Medium
    Font(R.font.dynapuff_condensed_bold, FontWeight.Bold, FontStyle.Italic), // Condensed Bold
    Font(R.font.dynapuff_semi_condensed_regular, FontWeight.Normal, FontStyle.Italic), // Semi Condensed Regular
    Font(R.font.dynapuff_semi_condensed_medium, FontWeight.Medium, FontStyle.Italic), // Semi Condensed Medium
    Font(R.font.dynapuff_semi_condensed_bold, FontWeight.Bold, FontStyle.Italic), // Semi Condensed Bold
    Font(R.font.dynapuff_variablefont_wdth_wght, FontWeight.Normal) // Variable font
)

val DynaPuffTextStyleNormal = TextStyle(
    fontFamily = DynaPuffFont,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)

val DynaPuffTextStyleBold = TextStyle(
    fontFamily = DynaPuffFont,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp
)

val DynaPuffTextStyleSemiCondensed = TextStyle(
    fontFamily = DynaPuffFont,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Italic,
    fontSize = 16.sp
)

val DynaPuffTextStyleCondensed = TextStyle(
    fontFamily = DynaPuffFont,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Italic,
    fontSize = 16.sp
)