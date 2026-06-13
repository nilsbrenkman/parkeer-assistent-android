package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme

/**
 * Top brand header (iOS `HeaderView`). Shows the app name and, when logged in, a tappable
 * balance bar and an overflow menu. The `Image-logo` asset is not ported yet, so the app name
 * stands in for the logo.
 */
@Composable
fun HeaderView(
    loggedIn: Boolean,
    balance: String? = null,
    onBalanceTap: () -> Unit = {},
    onInfo: () -> Unit = {},
    onHistory: () -> Unit = {},
    onPayment: () -> Unit = {},
    onAccounts: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    Column(Modifier.fillMaxWidth().background(AppTheme.colors.header)) {
        Box(Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp)) {
            Image(
                painter = painterResource(R.drawable.logo_transparent),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(48.dp)
                    .clickable(onClick = onInfo),
            )
            if (loggedIn) {
                Box(Modifier.align(Alignment.CenterEnd)) {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.2.dp, Color.White, RoundedCornerShape(4.dp)),
                    ) {
                        Text(
                            "≡",
                            color = Color.White,
                            fontSize = 48.sp,
                            lineHeight = 48.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = (-10).dp),
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.parking_history)) },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            onClick = { expanded = false; onHistory() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.user_add_balance)) },
                            leadingIcon = { Icon(Icons.Default.EuroSymbol, contentDescription = null) },
                            onClick = { expanded = false; onPayment() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.account_header)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            onClick = { expanded = false; onAccounts() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_header)) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = { expanded = false; onSettings() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.login_logout)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, contentDescription = null) },
                            onClick = { expanded = false; onLogout() },
                        )
                    }
                }
            }
        }

        if (loggedIn) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.light)
                    .clickable(onClick = onBalanceTap)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text("${stringResource(R.string.user_balance)}:", color = AppTheme.colors.header)
                Text(
                    text = "  € ${balance ?: "--"}",
                    color = AppTheme.colors.header,
                    fontWeight = FontWeight.Bold,
                )
            }
            HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.header)
        }
    }
}
