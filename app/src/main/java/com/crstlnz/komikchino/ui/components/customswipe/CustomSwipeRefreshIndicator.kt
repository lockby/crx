package com.crstlnz.komikchino.ui.components.customswipe

import androidx.compose.animation.core.animate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.ui.theme.Blue
import kotlin.math.absoluteValue


private data class SwipeRefreshIndicatorSizes(
    val size: Dp,
    val arcRadius: Dp,
    val strokeWidth: Dp,
    val arrowWidth: Dp,
    val arrowHeight: Dp,
)

/**
 * The default/normal size values for [SwipeRefreshIndicator].
 */
private val DefaultSizes = SwipeRefreshIndicatorSizes(
    size = 40.dp,
    arcRadius = 7.5.dp,
    strokeWidth = 2.5.dp,
    arrowWidth = 10.dp,
    arrowHeight = 5.dp,
)

/**
 * The 'large' size values for [SwipeRefreshIndicator].
 */
private val LargeSizes = SwipeRefreshIndicatorSizes(
    size = 56.dp,
    arcRadius = 11.dp,
    strokeWidth = 3.dp,
    arrowWidth = 12.dp,
    arrowHeight = 6.dp,
)

/**
 * Indicator composable which is typically used in conjunction with [SwipeRefresh].
 *
 * @param state The [SwipeRefreshState] passed into the [SwipeRefresh] `indicator` block.
 * @param modifier The modifier to apply to this layout.
 * @param fade Whether the arrow should fade in/out as it is scrolled in. Defaults to true.
 * @param scale Whether the indicator should scale up/down as it is scrolled in. Defaults to false.
 * @param arrowEnabled Whether an arrow should be drawn on the indicator. Defaults to true.
 * @param backgroundColor The color of the indicator background surface.
 * @param contentColor The color for the indicator's contents.
 * @param shape The shape of the indicator background surface. Defaults to [CircleShape].
 * @param largeIndication Whether the indicator should be 'large' or not. Defaults to false.
 * @param elevation The size of the shadow below the indicator.
 */
@Composable
fun CustomSwipeRefreshIndicator(
    state: SwipeRefreshState,
    refreshTriggerDistance: Dp,
    modifier: Modifier = Modifier,
    fade: Boolean = true,
    scale: Boolean = false,
    arrowEnabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    refreshingOffset: Dp = 16.dp,
    largeIndication: Boolean = false,
) {
    val sizes = if (largeIndication) LargeSizes else DefaultSizes
    val indicatorRefreshTrigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val indicatorHeight = with(LocalDensity.current) { sizes.size.roundToPx() }
    val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }

    val slingshot = rememberUpdatedSlingshot(
        offsetY = state.indicatorOffset.absoluteValue,
        maxOffsetY = indicatorRefreshTrigger,
        height = indicatorHeight,
    )

    var offset by remember { mutableFloatStateOf(0f) }

    if (state.isSwipeInProgress) {
        // If the user is currently swiping, we use the 'slingshot' offset directly
        offset = slingshot.offset.toFloat()
    } else {
        // If there's no swipe currently in progress, animate to the correct resting position
        LaunchedEffect(state.isRefreshing) {
            animate(
                initialValue = offset,
                targetValue = when {
                    state.isRefreshing -> indicatorHeight + refreshingOffsetPx
                    else -> 0f
                }
            ) { value, _ ->
                offset = value
            }
        }
    }
    LaunchedEffect(state.isSwipeInProgress) {
        if (!state.isSwipeInProgress) {
            state.animateOffsetTo(0f)
        }
    }
    Box(
        modifier = modifier
            .background(backgroundColor)
            .height(with(LocalDensity.current) { offset.toDp() }),
        contentAlignment = Alignment.TopCenter
//            .height(offset.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .absoluteOffset(y = 50.dp)
                .alpha(
                    (state.indicatorOffset.absoluteValue / (indicatorRefreshTrigger / 2)).coerceIn(
                        0f,
                        1.65f
                    ) - 0.65f
                )
                .requiredSize(50.dp)
        ) {
            Icon(
                painterResource(id = R.drawable.arrow_down_material),
                modifier = Modifier
                    .requiredSize(30.dp),
                tint = Blue,
                contentDescription = null
            )
            CircularProgressIndicator(
                state.indicatorOffset.absoluteValue / indicatorRefreshTrigger,
                color = Blue,
                modifier = Modifier
                    .requiredSize(50.dp),
            )
        }
    }
}
