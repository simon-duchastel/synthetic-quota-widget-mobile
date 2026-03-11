package com.duchastel.simon.syntheticwidget.ui.selector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duchastel.simon.syntheticwidget.ui.preview.WidgetPreviewData
import com.duchastel.simon.syntheticwidget.ui.theme.SyntheticWidgetTheme
import com.duchastel.simon.syntheticwidget.ui.widget.QuotaWidgetScreen
import com.duchastel.simon.syntheticwidget.widget.QuotaWidgetState

data class WidgetPreviewItem(
    val id: String,
    val name: String,
    val description: String,
    val previewState: QuotaWidgetState,
    val sizeLabel: String = "2x1"
)

@Composable
fun WidgetSelectorScreen(
    modifier: Modifier = Modifier,
    onWidgetSelected: (WidgetPreviewItem) -> Unit = {},
    onAddWidgetClick: () -> Unit = {}
) {
    val widgetOptions = listOf(
        WidgetPreviewItem(
            id = "quota_normal",
            name = "Quota Widget",
            description = "Normal usage view",
            previewState = WidgetPreviewData.FAKE_WIDGET_STATE_NORMAL,
            sizeLabel = "2x1"
        ),
        WidgetPreviewItem(
            id = "quota_compact",
            name = "Compact Quota",
            description = "Minimal size widget",
            previewState = WidgetPreviewData.FAKE_WIDGET_STATE_LOW_USAGE,
            sizeLabel = "2x1"
        ),
        WidgetPreviewItem(
            id = "quota_detailed",
            name = "Detailed Quota",
            description = "High usage alert view",
            previewState = WidgetPreviewData.FAKE_WIDGET_STATE_HIGH_USAGE,
            sizeLabel = "2x2"
        ),
        WidgetPreviewItem(
            id = "quota_alert",
            name = "Alert Widget",
            description = "Near limit warning",
            previewState = WidgetPreviewData.FAKE_WIDGET_STATE_NEAR_LIMIT,
            sizeLabel = "2x1"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Widget Gallery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a widget to add to your home screen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(widgetOptions) { widgetItem ->
                WidgetPreviewCard(
                    widgetItem = widgetItem,
                    onClick = { onWidgetSelected(widgetItem) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            onClick = onAddWidgetClick,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Widget",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun WidgetPreviewCard(
    widgetItem: WidgetPreviewItem,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Widget preview area with fixed aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Show the actual widget preview with fake data
                QuotaWidgetScreen(
                    quotaWidgetState = widgetItem.previewState,
                    onRefreshClick = {}
                )

                // Size label overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = widgetItem.sizeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Widget info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = widgetItem.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = widgetItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetSelectorDialogContent(
    onWidgetSelected: (WidgetPreviewItem) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        WidgetSelectorScreen(
            onWidgetSelected = onWidgetSelected,
            onAddWidgetClick = onDismiss
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WidgetSelectorScreenPreview() {
    SyntheticWidgetTheme {
        WidgetSelectorScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun WidgetPreviewCardPreview() {
    SyntheticWidgetTheme {
        WidgetPreviewCard(
            widgetItem = WidgetPreviewItem(
                id = "preview",
                name = "Quota Widget",
                description = "Normal usage view",
                previewState = WidgetPreviewData.FAKE_WIDGET_STATE_NORMAL,
                sizeLabel = "2x1"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WidgetPreviewCardSelectedPreview() {
    SyntheticWidgetTheme {
        WidgetPreviewCard(
            widgetItem = WidgetPreviewItem(
                id = "preview",
                name = "Quota Widget",
                description = "Selected widget",
                previewState = WidgetPreviewData.FAKE_WIDGET_STATE_HIGH_USAGE,
                sizeLabel = "2x1"
            ),
            onClick = {},
            isSelected = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WidgetSelectorDialogPreview() {
    SyntheticWidgetTheme {
        WidgetSelectorDialogContent()
    }
}
