package com.group13.linkap.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.R
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.GreenSecondary
import com.group13.linkap.ui.theme.Nunito

@Composable
fun LinKapTopBar(
    onNotificationClick: () -> Unit = {}
) {
    // "Lin" switches to the lighter green in dark mode so it stays readable
    val linColor = if (isSystemInDarkTheme()) GreenPrimaryDark else GreenPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.linkap_logo),
                contentDescription = "LinKap Logo",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            val logoTextStyle = TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            )

            Row {
                Text("Lin", style = logoTextStyle, color = linColor)
                Text("Kap", style = logoTextStyle, color = GreenSecondary)
            }
        }
    }
}