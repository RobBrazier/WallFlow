package com.ammar.wallflow.ui.common

import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.Parameters
import com.ammar.wallflow.R
import com.ammar.wallflow.extensions.TAG
import com.ammar.wallflow.extensions.aspectRatio
import com.ammar.wallflow.model.WallhavenWallpaper
import com.ammar.wallflow.model.wallhavenWallpaper1
import com.ammar.wallflow.ui.theme.WallFlowTheme
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import kotlin.math.roundToInt

private val cardHeight = 300.dp

@Composable
fun WallpaperCard(
    modifier: Modifier = Modifier,
    wallhavenWallpaper: WallhavenWallpaper,
    blur: Boolean = false,
    fixedHeight: Boolean = false,
    roundedCorners: Boolean = true,
    isSelected: Boolean = false,
    isFavorite: Boolean = false,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val request = remember(
        context,
        wallhavenWallpaper,
    ) {
        ImageRequest.Builder(context).apply {
            data(wallhavenWallpaper.thumbs.original)
            crossfade(true)
            parameters(
                Parameters.Builder().apply {
                    set("fallback_url", wallhavenWallpaper.path)
                }.build(),
            )
        }.build()
    }
    val transition = updateTransition(isSelected, label = "selection state")
    val selectionColor by transition.animateColor(label = "selection color") {
        if (it) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
    }
    val selectionCircleColor by transition.animateColor(label = "circle color") {
        if (it) MaterialTheme.colorScheme.onPrimary else Color.Transparent
    }
    val checkImageColor by transition.animateColor(label = "check image color") {
        if (it) MaterialTheme.colorScheme.primary else Color.Transparent
    }
    val checkImageAlpha by transition.animateFloat(label = "check image alpha") {
        if (it) 1f else 0f
    }
    val checkImage = remember(context) {
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.baseline_check_24,
            null,
        )?.toBitmap()?.asImageBitmap()
    }

    Box(
        modifier = modifier
            .let {
                if (fixedHeight) {
                    it.height(200.dp)
                } else {
                    it
                        .aspectRatio(wallhavenWallpaper.resolution.aspectRatio)
                        .heightIn(min = 60.dp)
                }
            }
            .clip(if (roundedCorners) CardDefaults.shape else RectangleShape)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            modifier = Modifier
                .blur(if (blur) 16.dp else 0.dp)
                .clip(RectangleShape)
                .fillMaxHeight()
                .drawWithContent {
                    drawContent()
                    drawRect(selectionColor)
                    val radius = minOf(size.minDimension / 2f, 20.dp.toPx())
                    drawCircle(
                        color = selectionCircleColor,
                        radius = radius,
                    )
                    if (checkImage == null) return@drawWithContent
                    val imageSize = (radius * 1.5).roundToInt()
                    drawImage(
                        image = checkImage,
                        dstOffset = IntOffset(
                            x = (size.width / 2 - imageSize / 2).roundToInt(),
                            y = (size.height / 2 - imageSize / 2).roundToInt(),
                        ),
                        dstSize = IntSize(imageSize, imageSize),
                        colorFilter = ColorFilter.tint(checkImageColor),
                        alpha = checkImageAlpha,
                    )
                },
            model = request,
            placeholder = ColorPainter(wallhavenWallpaper.colors.firstOrNull() ?: Color.White),
            fallback = ColorPainter(wallhavenWallpaper.colors.firstOrNull() ?: Color.White),
            error = ColorPainter(wallhavenWallpaper.colors.firstOrNull() ?: Color.White),
            contentDescription = stringResource(R.string.wallpaper),
            contentScale = ContentScale.Crop,
            onError = {
                Log.e(
                    TAG,
                    "Error loading: ${wallhavenWallpaper.thumbs.original}",
                    it.result.throwable,
                )
            },
        )
        FilledIconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = 4.dp,
                    end = 4.dp,
                ),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.5f),
                contentColor = if (isFavorite) Color.Red else Color.White,
            ),
            onClick = onFavoriteClick,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(
                    if (isFavorite) {
                        R.drawable.baseline_favorite_24
                    } else {
                        R.drawable.outline_favorite_border_24
                    },
                ),
                contentDescription = stringResource(R.string.favorite),
            )
        }
    }
}

@Composable
fun PlaceholderWallpaperCard(
    modifier: Modifier = Modifier,
    loading: Boolean = true,
) {
    Column(
        modifier = modifier
            .height(cardHeight)
            .clip(RectangleShape)
            .placeholder(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                visible = loading,
                highlight = PlaceholderHighlight.fade(),
            ),
    ) {}
}

@Preview
@Composable
private fun PreviewWallpaperCard() {
    WallFlowTheme {
        WallpaperCard(
            modifier = Modifier.width(200.dp),
            wallhavenWallpaper = wallhavenWallpaper1,
        )
    }
}

@Preview
@Composable
private fun PreviewWallpaperCardSelected() {
    WallFlowTheme {
        WallpaperCard(
            modifier = Modifier.width(200.dp),
            wallhavenWallpaper = wallhavenWallpaper1,
            isSelected = true,
        )
    }
}
