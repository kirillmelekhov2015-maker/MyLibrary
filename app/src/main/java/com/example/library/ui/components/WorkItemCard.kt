package com.example.library.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.library.ui.theme.IconTextColor
import com.example.library.ui.theme.MainBackgroundColor
import com.example.library.ui.theme.WorkTitleColor

data class WorkItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val episodes: String,
    val description: String
)

@Composable
fun WorkItemCard(
    workItem: WorkItem,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val mainBackgroundColor = MainBackgroundColor()
    val iconTextColor = IconTextColor()
    val workTitleColor = WorkTitleColor()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = mainBackgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cover image (or placeholder)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                if (!workItem.imageUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(workItem.imageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "📖",
                        fontSize = 32.sp
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = workItem.title,
                    color = workTitleColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                // Episodes
                Text(
                    text = workItem.episodes,
                    color = iconTextColor,
                    fontSize = 12.sp
                )

                // Description
                Text(
                    text = workItem.description,
                    color = iconTextColor,
                    fontSize = 12.sp,
                    maxLines = 3,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
