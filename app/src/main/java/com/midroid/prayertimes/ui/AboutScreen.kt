package com.midroid.prayertimes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.midroid.prayertimes.R

const val ADHAN_URL = "https://www.youtube.com/watch?v=rp1FyMSxehE"
const val CHANNEL_URL = "https://www.youtube.com/channel/UCox8SYPdFsv-ClZoyzIJR1w"
const val PAYPAL_URL = "https://paypal.me/Midroid5G"
const val DEVELOPER_X_URL = "https://x.com/midroid5G"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(version: String, onBack: () -> Unit) {
    val uri = LocalUriHandler.current
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.about_title)) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_qibla), null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.version_fmt, version), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Head(stringResource(R.string.support_title)); Body(stringResource(R.string.support_body))
            Link("PayPal — https://paypal.me/Midroid5G (@Midroid5G)") { runCatching { uri.openUri(PAYPAL_URL) } }
            Link("X (Twitter) — @midroid5G — https://x.com/midroid5G") { runCatching { uri.openUri(DEVELOPER_X_URL) } }
            Head(stringResource(R.string.privacy_title)); Body(stringResource(R.string.privacy_body))
            Head(stringResource(R.string.adhan_attr_title)); Body(stringResource(R.string.adhan_attr_body))
            Link(stringResource(R.string.source_link)) { uri.openUri(ADHAN_URL) }
            Link("بيت المسلم - Muslim House — $CHANNEL_URL") { uri.openUri(CHANNEL_URL) }
            Head(stringResource(R.string.other_credits)); Body(stringResource(R.string.other_credits_body))
            Head(stringResource(R.string.sources_licences)); Body(stringResource(R.string.hisn_credit)); Body(stringResource(R.string.quran_credit)); Body(stringResource(R.string.font_credit))
            Link("adhan-java — MIT — github.com/batoulapps/adhan-java") { uri.openUri("https://github.com/batoulapps/adhan-java") }
            Link("GeoNames — CC BY 4.0 — geonames.org") { uri.openUri("https://www.geonames.org/") }
        }
    }
}

@Composable private fun Head(t: String) = Text(t, Modifier.padding(top = 20.dp, bottom = 6.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
@Composable private fun Body(t: String) = Text(t, style = MaterialTheme.typography.bodyMedium)
@Composable private fun Link(t: String, onClick: () -> Unit) = TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
    Text(t, style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content))
}
