package com.example.rabbittracker.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun ProfileAvatar(
    userName: String,
    userPhotoPath: String,
    size: Dp = 36.dp,
    onClick: (() -> Unit)? = null
) {
    val bitmap = remember(userPhotoPath) {
        if (userPhotoPath.isNotBlank()) {
            try {
                val file = File(userPhotoPath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(primaryColor)
        .let { if (onClick != null) it.clickable { onClick() } else it }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile Photo",
                modifier = Modifier.size(size)
            )
        } else if (userName.isNotBlank()) {
            Text(
                text = userName.trim().take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = onPrimaryColor
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Profile",
                tint = onPrimaryColor,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
