package com.example.rabbittracker.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    userName: String = "",
    userPhotoPath: String = "",
    onProfileClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        actions = {
            actions()
            TopAppBarActionBox {
                ProfileAvatar(
                    userName = userName,
                    userPhotoPath = userPhotoPath,
                    size = 36.dp,
                    onClick = onProfileClick
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun TopAppBarActionBox(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(end = 16.dp)) {
        content()
    }
}
