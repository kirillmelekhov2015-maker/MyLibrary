package com.example.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.library.ui.LocalStrings
import com.example.library.ui.screens.AppTheme
import com.example.library.ui.theme.*

sealed class NavigationItem(
    val labelKey: (com.example.library.ui.Strings) -> String,
    val icon: ImageVector
) {
    object Books : NavigationItem({ it.tabBooks }, Icons.AutoMirrored.Filled.MenuBook)
    object Anime : NavigationItem({ it.tabAnime }, Icons.Default.Movie)
    // Manga: more thematic "book" icon
    object Manga : NavigationItem({ it.tabManga }, Icons.Default.AutoStories)
    // TV series: revert to TV icon
    object TVSeries : NavigationItem({ it.tabSeries }, Icons.Default.Tv)
    object Notes : NavigationItem({ "Заметки" }, Icons.Default.Edit) // Temporary label
    object Profile : NavigationItem({ it.profile }, Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    currentTheme: AppTheme = AppTheme.DARK,
    notesEnabled: Boolean = false,
    booksEnabled: Boolean = true,
    animeEnabled: Boolean = true,
    mangaEnabled: Boolean = true,
    tvSeriesEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val panelColor = PanelColor()

    val baseItems = buildList {
        if (booksEnabled) add(NavigationItem.Books)
        if (animeEnabled) add(NavigationItem.Anime)
        if (mangaEnabled) add(NavigationItem.Manga)
        if (tvSeriesEnabled) add(NavigationItem.TVSeries)
    }

    val items = buildList {
        addAll(baseItems)
        if (notesEnabled) add(NavigationItem.Notes)
        add(NavigationItem.Profile)
    }

    // Bottom panel size is controlled here - height is 154.dp (line 56)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(panelColor)
            .padding(bottom = 30.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            NavigationButton(
                item = item,
                label = item.labelKey(strings),
                isSelected = item == selectedItem,
                onClick = { onItemSelected(item) },
                currentTheme = currentTheme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NavigationButton(
    item: NavigationItem,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    currentTheme: AppTheme = AppTheme.DARK,
    modifier: Modifier = Modifier
) {
    val selectedTabColor = SelectedTabColor()
    val bottomPanelLabelColor = BottomPanelLabelColor()
    val bottomPanelIconColor = BottomPanelIconColor()
    val interactionSource = remember { MutableInteractionSource() }
    
    // Circle color based on theme: dark - rgb(73, 68, 88), light - rgb(255, 218, 215)
    val circleColor = if (currentTheme == AppTheme.DARK) {
        Color(0xFF494458) // rgb(73, 68, 88)
    } else {
        Color(0xFFFFDAD7) // rgb(255, 218, 215)
    }

    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                )
            }
            Icon(
                imageVector = item.icon,
                contentDescription = label,
                tint = if (isSelected) selectedTabColor else bottomPanelIconColor,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) selectedTabColor else bottomPanelLabelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
