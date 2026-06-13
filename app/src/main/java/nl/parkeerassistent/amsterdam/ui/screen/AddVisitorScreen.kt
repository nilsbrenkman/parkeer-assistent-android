package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.components.LicensePlateField
import nl.parkeerassistent.amsterdam.ui.components.SuccessButton
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.ui.visitor.VisitorViewModel
import nl.parkeerassistent.amsterdam.util.LicenseUtil

/** Add a visitor (iOS `AddVisitorView`): license (auto-formatted) + name. */
@Composable
fun AddVisitorScreen(
    visitorVm: VisitorViewModel,
    onBack: () -> Unit,
) {
    var license by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var wait by remember { mutableStateOf(false) }

    AddVisitorContent(
        license = license,
        name = name,
        onLicenseChange = { license = LicenseUtil.format(it) },
        onNameChange = { name = it },
        onAdd = {
            if (!wait) {
                wait = true
                visitorVm.addVisitor(license, name) { onBack() }
                wait = false
            }
        },
    )
}

@Composable
internal fun AddVisitorContent(
    license: String,
    name: String,
    onLicenseChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.visitor_add))
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.contentPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            Spacer(Modifier.height(Dimens.spacingSmall))

            LicensePlateField(
                value = license,
                onValueChange = onLicenseChange,
                placeholder = stringResource(R.string.visitor_license),
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.visitor_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            SuccessButton(
                onClick = onAdd,
                enabled = license.isNotEmpty() && name.isNotEmpty(),
            ) { Text(stringResource(R.string.common_add)) }
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun AddVisitorPreview() = ParkeerAssistentTheme {
    AddVisitorContent("12-ABC-3", "Jan Jansen", {}, {}, {})
}
