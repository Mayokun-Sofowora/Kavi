package com.mayor.kavi.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mayor.kavi.R

/**
 * Composable to display a count of objects detected in the current frame, which includes an icon followed by a text field.
 * This UI component is intended to overlay other content, with a semi-transparent background for better visibility.
 *
 * @param objectCount The number of detected objects to display.
 */
@Composable
fun ObjectCounter(objectCount: Int) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .background(
                color = Color.Transparent.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Image to display Icon
        Image(
            painter = painterResource(id = R.drawable.ic_object_count),
            contentDescription = stringResource(id = R.string.object_icon_description),
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp),
        )
        // Text
        Text(
            modifier = Modifier
                .padding(end = 16.dp, start = 8.dp),
            text = "$objectCount",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colorResource(id = R.color.gray_50)
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewObjectCountDisplay() {
    ObjectCounter(objectCount = 5)
}