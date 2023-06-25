package com.crstlnz.komikchino.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

fun Modifier.defaultPlaceholder(
    color: Color? = null,
    highlightColor: Color? = null,
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier = composed {
    placeholder(
        visible = true,
        color = color ?: MaterialTheme.colors.surface,
        shape = shape,
        highlight = PlaceholderHighlight.shimmer(
            highlightColor = highlightColor ?: lightenColor(
                MaterialTheme.colors.surface, 20
            ),
        ),
    )
}