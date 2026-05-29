package nl.parkeerassistent.amsterdam.ui.screen

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens

/**
 * About / info (iOS `InfoView`). Copy is placeholder Dutch pending i18n (Phase 7); the external
 * links match the iOS app.
 */
@Composable
fun InfoScreen() {
    val context = LocalContext.current
    val open: (String) -> Unit = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
    val pkg = remember { context.packageManager.getPackageInfo(context.packageName, 0) }

    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.info_header))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingNormal),
        ) {
            Text(stringResource(R.string.info_text1))
            Text(stringResource(R.string.info_text2))
            Link(stringResource(R.string.info_website), "https://aanmeldenparkeren.amsterdam.nl/", open)
            Text(stringResource(R.string.info_text3))
            Text(stringResource(R.string.info_text4))
            Text(stringResource(R.string.info_text5))
            Link(stringResource(R.string.info_source_code), "https://github.com/nilsbrenkman/parkeer-assistent", open)
            Text(stringResource(R.string.info_text6))
            Link(stringResource(R.string.info_feedback), "https://parkeerassistent.nl/feedback", open)
            Text(
                "${stringResource(R.string.info_version)}: ${pkg.versionName} (${pkg.longVersionCode})",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun Link(label: String, url: String, open: (String) -> Unit) {
    Text(
        text = label,
        color = AppTheme.colors.header,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable { open(url) },
    )
}
