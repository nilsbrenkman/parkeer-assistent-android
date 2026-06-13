package nl.parkeerassistent.amsterdam.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.components.SectionHeader
import nl.parkeerassistent.amsterdam.ui.components.SuccessButton
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.payment.PaymentViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import org.koin.androidx.compose.koinViewModel

private val AMOUNTS = listOf(250L, 500L, 1000L, 1500L, 2000L, 3000L, 4000L, 5000L, 10000L)
private val METHODS = listOf("IDEAL" to "iDEAL | Wero", "CARDS" to "Credit Card")

/**
 * Top up balance (iOS `PaymentView`). A single `POST payment` returns a URL we open in the
 * browser; the user pays externally and returns — the home screen refreshes the balance.
 */
@Composable
fun PaymentScreen(paymentVm: PaymentViewModel = koinViewModel()) {
    val context = LocalContext.current
    val wait by paymentVm.isPaymentInProgress.collectAsStateWithLifecycle()
    var amount by remember { mutableIntStateOf(0) }
    var method by remember { mutableStateOf("") }

    PaymentContent(
        amount = amount,
        method = method,
        wait = wait,
        onAmount = { amount = it },
        onMethod = { method = it },
        onPay = {
            paymentVm.payment(amount.toLong(), method, lang = "nl") { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
        },
    )
}

@Composable
private fun PaymentContent(
    amount: Int,
    method: String,
    wait: Boolean,
    onAmount: (Int) -> Unit,
    onMethod: (String) -> Unit,
    onPay: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.user_add_balance))
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.contentPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            SectionHeader(stringResource(R.string.payment_amount))
            AMOUNTS.chunked(3).forEach { rowAmounts ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)) {
                    rowAmounts.forEach { value ->
                        SelectableButton(
                            text = "€ ${"%.2f".format(value / 100.0)}",
                            selected = amount == value.toInt(),
                            onClick = { onAmount(value.toInt()) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            SectionHeader(stringResource(R.string.payment_bank), Modifier.padding(top = Dimens.paddingSmall))
            METHODS.forEach { (key, label) ->
                SelectableButton(
                    text = label,
                    selected = method == key,
                    onClick = { onMethod(key) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SuccessButton(
                onClick = onPay,
                enabled = amount != 0 && method.isNotEmpty() && !wait,
                wait = wait,
            ) { Text(stringResource(R.string.payment_start)) }
        }
    }
}

@Composable
private fun SelectableButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = if (selected) {
            ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.header,
                contentColor = AppTheme.colors.onHeader,
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        },
        shape = RoundedCornerShape(Dimens.radiusSmall),
        modifier = modifier
            .height(48.dp),
    ) { Text(text, style = MaterialTheme.typography.labelMedium) }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PaymentPreview() = ParkeerAssistentTheme {
    PaymentContent(amount = 500, method = "IDEAL", wait = false, {}, {}, {})
}
