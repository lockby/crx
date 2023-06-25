package com.crstlnz.komikchino.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.crstlnz.komikchino.R
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun ErrorView(
    @DrawableRes resId: Int,
    message: String,
    buttonName: String = stringResource(R.string.refresh),
    showButton: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), contentAlignment = Alignment.Center
    ) {
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
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(35.dp))
            if (showButton)
                Button(onClick = { onClick() }) {
                    Text(buttonName)
                }
        }
    }
}
