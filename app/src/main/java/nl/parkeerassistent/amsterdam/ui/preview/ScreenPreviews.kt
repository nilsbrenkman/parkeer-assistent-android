package nl.parkeerassistent.amsterdam.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import nl.parkeerassistent.amsterdam.ui.screen.HeaderView
import nl.parkeerassistent.amsterdam.ui.screen.InfoScreen
import nl.parkeerassistent.amsterdam.ui.screen.LoadingScreen
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme

@Preview(showBackground = true)
@Composable
private fun HeaderLoggedOutPreview() = ParkeerAssistentTheme { HeaderView(loggedIn = false) }

@Preview(showBackground = true)
@Composable
private fun HeaderLoggedInPreview() = ParkeerAssistentTheme { HeaderView(loggedIn = true, balance = "20.27") }

@Preview(showBackground = true, heightDp = 300)
@Composable
private fun LoadingScreenPreview() = ParkeerAssistentTheme { LoadingScreen() }

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun InfoScreenPreview() = ParkeerAssistentTheme { InfoScreen() }
