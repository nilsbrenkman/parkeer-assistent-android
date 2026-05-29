package nl.parkeerassistent.amsterdam.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.ui.visitor.VisitorViewModel
import nl.parkeerassistent.amsterdam.util.License

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
        onLicenseChange = { license = License.format(it) },
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
private fun AddVisitorContent(
    license: String,
    name: String,
    onLicenseChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.visitor_add))
        Column(Modifier.padding(Dimens.paddingNormal)) {
            OutlinedTextField(
                value = license,
                onValueChange = onLicenseChange,
                label = { Text(stringResource(R.string.visitor_license)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.visitor_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingSmall),
            )
            Button(
                onClick = onAdd,
                enabled = license.isNotEmpty() && name.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.success,
                    contentColor = AppTheme.colors.enabled,
                ),
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.paddingNormal),
            ) { Text(stringResource(R.string.common_add)) }
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun AddVisitorPreview() = ParkeerAssistentTheme {
    AddVisitorContent("12-ABC-3", "Jan Jansen", {}, {}, {})
}
