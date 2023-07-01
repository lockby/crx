package com.crstlnz.komikchino.ui.components.customswipe


import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.lang.Float.min
import kotlin.math.absoluteValue

/**
 * A class to encapsulate details of different indicator sizes.
 *
 * @param size The overall size of the indicator.
 * @param arcRadius The radius of the arc.
 * @param strokeWidth The width of the arc stroke.
 * @param arrowWidth The width of the arrow.
 * @param arrowHeight The height of the arrow.
 */
@Immutable
private data class OriginalSwipeRefreshIndicatorSizes(
    val size: Dp,
    val arcRadius: Dp,
    val strokeWidth: Dp,
    val arrowWidth: Dp,
    val arrowHeight: Dp,
)

/**
 * The default/normal size values for [SwipeRefreshIndicator].
 */
private val DefaultSizes = OriginalSwipeRefreshIndicatorSizes(
    size = 40.dp,
    arcRadius = 7.5.dp,
    strokeWidth = 2.5.dp,
    arrowWidth = 10.dp,
    arrowHeight = 5.dp,
)

/**
 * The 'large' size values for [SwipeRefreshIndicator].
 */
private val LargeSizes = OriginalSwipeRefreshIndicatorSizes(
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
@Deprecated(
    """
     accompanist/swiperefresh is deprecated.
     The androidx.compose equivalent of SwipeRefreshIndicator() is PullRefreshIndicator().
     For more migration information, please visit https://google.github.io/accompanist/swiperefresh/#migration
    """
)
@Composable
fun OriginalSwipeRefreshIndicator(
    state: SwipeRefreshState,
    refreshTriggerDistance: Dp,
    modifier: Modifier = Modifier,
    fade: Boolean = true,
    scale: Boolean = false,
    arrowEnabled: Boolean = true,
    backgroundColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    refreshingOffset: Dp = 16.dp,
    largeIndication: Boolean = false,
    elevation: Dp = 6.dp,
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
        offset = -slingshot.offset.toFloat()
    } else {
        // If there's no swipe currently in progress, animate to the correct resting position
        LaunchedEffect(state.isRefreshing) {
            animate(
                initialValue = offset,
                targetValue = when {
                    state.isRefreshing -> indicatorHeight - refreshingOffsetPx
                    else -> 0f
                }
            ) { value, _ ->
                offset = value
            }
        }
    }

    val adjustedElevation = when {
        state.isRefreshing -> elevation
        offset > 0.5f -> elevation
        else -> 0.dp
    }

    Surface(
        modifier = modifier
            .size(size = sizes.size)
            .graphicsLayer {
                // Translate the indicator according to the slingshot
                translationY = offset + indicatorHeight

                val scaleFraction = if (scale && !state.isRefreshing) {
                    val progress = offset / indicatorRefreshTrigger.coerceAtLeast(1f)

                    // We use LinearOutSlowInEasing to speed up the scale in
                    LinearOutSlowInEasing
                        .transform(progress)
                        .coerceIn(0f, 1f)
                } else 1f

                scaleX = scaleFraction
                scaleY = scaleFraction
            },
        shape = shape,
        color = backgroundColor,
        elevation = adjustedElevation
    ) {
//        val painter = remember { CircularProgressPainter() }
//        painter.arcRadius = sizes.arcRadius
//        painter.strokeWidth = sizes.strokeWidth
//        painter.arrowWidth = sizes.arrowWidth
//        painter.arrowHeight = sizes.arrowHeight
//        painter.arrowEnabled = arrowEnabled && !state.isRefreshing
//        painter.color = contentColor
//        val alpha = if (fade) {
//            (state.indicatorOffset.absoluteValue / indicatorRefreshTrigger).coerceIn(0f, 1f)
//        } else {
//            1f
//        }
//        painter.alpha = alpha
//
//        painter.startTrim = slingshot.startTrim
//        painter.endTrim = slingshot.endTrim
//        painter.rotation = slingshot.rotation
//        painter.arrowScale = slingshot.arrowScale

        // This shows either an Image with CircularProgressPainter or a CircularProgressIndicator,
        // depending on refresh state
//        Crossfade(
//            targetState = state.isRefreshing,
//            animationSpec = tween(durationMillis = CrossfadeDurationMs)
//        ) { refreshing ->
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                if (refreshing) {
//                    val circleSize = (sizes.arcRadius + sizes.strokeWidth) * 2
//                    CircularProgressIndicator(
//                        color = contentColor,
//                        strokeWidth = sizes.strokeWidth,
//                        modifier = Modifier.size(circleSize),
//                    )
//                } else {
//                    Image(
//                        painter = painter,
//                        contentDescription = "Refreshing"
//                    )
//                }
//            }
//        }
    }
}

private const val CrossfadeDurationMs = 100

internal class CircularProgressPainter : Painter() {
    var color by mutableStateOf(Color.Unspecified)
    var alpha by mutableStateOf(1f)
    var arcRadius by mutableStateOf(0.dp)
    var strokeWidth by mutableStateOf(5.dp)
    var arrowEnabled by mutableStateOf(false)
    var arrowWidth by mutableStateOf(0.dp)
    var arrowHeight by mutableStateOf(0.dp)
    var arrowScale by mutableStateOf(1f)

    private val arrow: Path by lazy {
        Path().apply { fillType = PathFillType.EvenOdd }
    }

    var startTrim by mutableStateOf(0f)
    var endTrim by mutableStateOf(0f)
    var rotation by mutableStateOf(0f)

    override val intrinsicSize: Size
        get() = Size.Unspecified

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun DrawScope.onDraw() {
        rotate(degrees = rotation) {
            val arcRadius = arcRadius.toPx() + strokeWidth.toPx() / 2f
            val arcBounds = Rect(
                size.center.x - arcRadius,
                size.center.y - arcRadius,
                size.center.x + arcRadius,
                size.center.y + arcRadius
            )
            val startAngle = (startTrim + rotation) * 360
            val endAngle = (endTrim + rotation) * 360
            val sweepAngle = endAngle - startAngle
            drawArc(
                color = color,
                alpha = alpha,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcBounds.topLeft,
                size = arcBounds.size,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Square
                )
            )
            if (arrowEnabled) {
                drawArrow(startAngle, sweepAngle, arcBounds)
            }
        }
    }

    private fun DrawScope.drawArrow(startAngle: Float, sweepAngle: Float, bounds: Rect) {
        arrow.reset()
        arrow.moveTo(0f, 0f)
        arrow.lineTo(
            x = arrowWidth.toPx() * arrowScale,
            y = 0f
        )
        arrow.lineTo(
            x = arrowWidth.toPx() * arrowScale / 2,
            y = arrowHeight.toPx() * arrowScale
        )
        val radius = min(bounds.width, bounds.height) / 2f
        val inset = arrowWidth.toPx() * arrowScale / 2f
        arrow.translate(
            Offset(
                x = radius + bounds.center.x - inset,
                y = bounds.center.y + strokeWidth.toPx() / 2f
            )
        )
        arrow.close()
        rotate(degrees = startAngle + sweepAngle) {
            drawPath(
                path = arrow,
                color = color,
                alpha = alpha
            )
        }
    }
}