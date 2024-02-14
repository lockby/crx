package com.crstlnz.komikchino.ui.util

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.launch


fun Modifier.customZoomable(): Modifier = composed {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    pointerInput(Unit) {
        detectTransformGestures(onGesture = { centroid, pan, gestureZoom, _ ->
            val oldScale = zoom
            val newScale = zoom * gestureZoom
            // For natural zooming and rotating, the centroid of the gesture should
            // be the fixed point where zooming and rotating occurs.
            // We compute where the centroid was (in the pre-transformed coordinate
            // space), and then compute where it will be after this delta.
            // We then compute what the new offset should be to keep the centroid
            // visually stationary for rotating and zooming, and also apply the pan.
            offset = (offset + centroid / oldScale) - (centroid / newScale + pan / oldScale)
            zoom = newScale
        })

        graphicsLayer {
            translationX = -offset.x * zoom
            translationY = -offset.y * zoom
            scaleX = zoom
            scaleY = zoom
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
    clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
}

@Composable
fun Modifier.tapToZoomVertical(
    constraints: Constraints, lazyState: LazyListState
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    var mScale by remember {
        mutableStateOf(1f)
    }

    var offset by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    return this then Modifier
        .pointerInput(Unit) {
//            detectTapGestures(onDoubleTap = { tapCenter ->
//                Log.d("ZOOMABLE", "DOUBLE TAP")
////                    if (!state.isZoomEnable) return@detectTapGestures
//                if (mScale > 1.0f) {
//                    mScale = 1.0f
//                    offset = Offset(0f, 0f)
//                } else {
//                    mScale = 3.0f
//                    val center = Pair(constraints.maxWidth / 2, constraints.maxHeight / 2)
//                    val xDiff = (tapCenter.x - center.first) * mScale
//                    val yDiff = ((tapCenter.y - center.second) * mScale).coerceIn(
//                        minimumValue = -(center.second * 2f), maximumValue = (center.second * 2f)
//                    )
//                    offset = Offset(-xDiff, -yDiff)
//                }
//            })
            detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                Log.d("TRANSFORM", zoom.toString())
                mScale *= zoom
                val pair = if (pan.y > 0) {
                    if (lazyState.canScrollBackward) {
                        Pair(0f, pan.y)
                    } else {
                        Pair(pan.y, 0f)
                    }
                } else {
                    if (lazyState.canScrollForward) {
                        Pair(0f, pan.y)
                    } else {
                        Pair(pan.y, 0f)
                    }
                }
                val nOffset = if (mScale > 1f) {
                    val maxT = (constraints.maxWidth * mScale) - constraints.maxWidth
                    val maxY = (constraints.maxHeight * mScale) - constraints.maxHeight
                    Offset(
                        x = (offset.x + pan.x).coerceIn(
                            minimumValue = (-maxT / 2) * 1.3f, maximumValue = (maxT / 2) * 1.3f
                        ), y = (offset.y + pair.first).coerceIn(
                            minimumValue = (-maxY / 2), maximumValue = (maxY / 2)
                        )
                    )
                } else {
                    Offset(0f, 0f)
                }
                offset = nOffset
                coroutineScope.launch {
                    lazyState.scrollBy((-pair.second / mScale))
                }
            }
        }
        .graphicsLayer {
            scaleX = mScale
            scaleY = mScale
            translationX = offset.x
            translationY = offset.y
        }
}