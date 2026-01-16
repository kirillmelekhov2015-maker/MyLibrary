package com.example.library.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Theme-aware colors
data class AppColors(
    val panelColor: Color,
    val iconBackgroundColor: Color,
    val iconTextColor: Color,
    val mainBackgroundColor: Color,
    val searchBarColor: Color,
    val titleColorBetween: Color,
    val titleColorOuter: Color,
    val titleColorInner: Color,
    val inactiveTabBorderInner: Color,
    val inactiveTabBorder2: Color,
    val inactiveTabBorder3: Color,
    val inactiveTabBorder4: Color,
    val inactiveTabBorderOuter: Color,
    val selectedTabColor: Color,
    val workTitleColor: Color,
    val bottomPanelLabelColor: Color,
    val bottomPanelIconColor: Color
)

val DarkColors = AppColors(
    panelColor = Color(0xFF252525),
    iconBackgroundColor = Color(0xFF252525),
    iconTextColor = Color(0xFF757575),
    mainBackgroundColor = Color(0xFF121212),
    searchBarColor = Color(0xFF232323),
    titleColorBetween = Color(0xFFFFFFFF),
    titleColorOuter = Color(0xFFFFFFFF),
    titleColorInner = Color(0xFFFFFFFF),
    inactiveTabBorderInner = Color(0xFF3A3A3C), // Темные градиентные границы
    inactiveTabBorder2 = Color(0xFF48484A),
    inactiveTabBorder3 = Color(0xFF636366),
    inactiveTabBorder4 = Color(0xFF8E8E93),
    inactiveTabBorderOuter = Color(0xFFAEAEB2),
    selectedTabColor = Color(0xFFe8def7),
    workTitleColor = Color(0xFFFFFFFF), // Белый для заголовков
    bottomPanelLabelColor = Color(0xFF626262),
    bottomPanelIconColor = Color(0xFF616161)
)

val LightColors = AppColors(
    panelColor = Color(0xFFFFFFFF),
    iconBackgroundColor = Color(0xFFFFFFFF),
    iconTextColor = Color(0xFF757575),
    mainBackgroundColor = Color(0xFFFFFFFF),
    searchBarColor = Color(0xFFeeeeee),
    titleColorBetween = Color(0xFF212121),
    titleColorOuter = Color(0xFF151515),
    titleColorInner = Color(0xFF373737),
    inactiveTabBorderInner = Color(0xFFD1D1D6),
    inactiveTabBorder2 = Color(0xFFC7C7CC),
    inactiveTabBorder3 = Color(0xFFAEAEB2),
    inactiveTabBorder4 = Color(0xFF8E8E93),
    inactiveTabBorderOuter = Color(0xFF6E6E6E),
    selectedTabColor = Color(0xFFde595a),
    workTitleColor = Color(0xFF171717),
    bottomPanelLabelColor = Color(0xFF747474),
    bottomPanelIconColor = Color(0xFF747474)
)

val LocalAppColors = compositionLocalOf<AppColors> { DarkColors }

@Composable
fun appColors(): AppColors = LocalAppColors.current

// Composable accessors for colors
@Composable
fun PanelColor(): Color = appColors().panelColor
@Composable
fun IconBackgroundColor(): Color = appColors().iconBackgroundColor
@Composable
fun IconTextColor(): Color = appColors().iconTextColor
@Composable
fun MainBackgroundColor(): Color = appColors().mainBackgroundColor
@Composable
fun SearchBarColor(): Color = appColors().searchBarColor
@Composable
fun TitleColorBetween(): Color = appColors().titleColorBetween
@Composable
fun TitleColorOuter(): Color = appColors().titleColorOuter
@Composable
fun TitleColorInner(): Color = appColors().titleColorInner
@Composable
fun InactiveTabBorderInner(): Color = appColors().inactiveTabBorderInner
@Composable
fun InactiveTabBorder2(): Color = appColors().inactiveTabBorder2
@Composable
fun InactiveTabBorder3(): Color = appColors().inactiveTabBorder3
@Composable
fun InactiveTabBorder4(): Color = appColors().inactiveTabBorder4
@Composable
fun InactiveTabBorderOuter(): Color = appColors().inactiveTabBorderOuter
@Composable
fun SelectedTabColor(): Color = appColors().selectedTabColor
@Composable
fun WorkTitleColor(): Color = appColors().workTitleColor
@Composable
fun BottomPanelLabelColor(): Color = appColors().bottomPanelLabelColor
@Composable
fun BottomPanelIconColor(): Color = appColors().bottomPanelIconColor