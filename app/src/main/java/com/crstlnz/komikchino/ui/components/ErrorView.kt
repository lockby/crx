package com.crstlnz.komikchino.ui.components

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.ui.util.ViewModelBase
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.crstlnz.komikchino.ui.theme.WhiteGray

@Composable
fun ErrorView(
    @DrawableRes resId: Int,
    message: String,
    buttonName: String = stringResource(R.string.refresh),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), contentAlignment = Alignment.Center
    ) {
//        val drawable = AppCompatResources.getDrawable(LocalContext.current, resId)

        val drawable = ContextCompat.getDrawable(LocalContext.current, resId)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                painter = rememberDrawablePainter(drawable = drawable),
                contentDescription = message
            )
            Spacer(modifier = Modifier.height(25.dp))
            Text(
                message,
                modifier = Modifier.padding(horizontal = 10.dp),
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(35.dp))
            Button(onClick = { onClick() }) {
                Text(buttonName)
            }
        }
    }
}
