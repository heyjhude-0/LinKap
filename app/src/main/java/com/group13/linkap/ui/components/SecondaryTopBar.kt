package com.group13.linkap.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.Nunito

@Composable
fun SecondaryTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    val primary = if (isSystemInDarkTheme()) GreenPrimaryDark else GreenPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go back",
                tint = primary
            )
        }

        Text(
            text = title,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}