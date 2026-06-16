package nl.parkeerassistent.amsterdam.ui.session

import nl.parkeerassistent.amsterdam.FakeCredentialStore
import nl.parkeerassistent.amsterdam.FakeLoginRepository
import nl.parkeerassistent.amsterdam.FakeStatsStore
import nl.parkeerassistent.amsterdam.FakeStringProvider
import nl.parkeerassistent.amsterdam.MainDispatcherRule
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.remote.ApiException
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SessionViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val loginRepo = FakeLoginRepository()
    private val messageBus = MessageBus()
    private val strings = FakeStringProvider()
    private val errorHandler = ApiErrorHandler(messageBus, strings)
    private val credentialStore = FakeCredentialStore()
    private val stats = FakeStatsStore()

    private fun viewModel() = SessionViewModel(loginRepo, errorHandler, messageBus, credentialStore, strings, stats)

    @Test fun `successful login logs in and bumps the login count`() {
        loginRepo.loginResult = Response(true, "ok")
        val vm = viewModel()

        vm.login("u", "p", storeCredentials = false)

        assertTrue(vm.state.value.isLoggedIn)
        assertEquals(1, stats.loginCount)
        assertTrue(credentialStore.list.isEmpty())
    }

    @Test fun `remember stores the credential and recent username`() {
        val vm = viewModel()

        vm.login("alice", "secret", storeCredentials = true)

        assertEquals(1, credentialStore.list.size)
        assertEquals("alice", credentialStore.list[0].username)
        assertEquals("alice", credentialStore.recent)
    }

    @Test fun `unsuccessful login stays logged out`() {
        loginRepo.loginResult = Response(false, "wrong")
        val vm = viewModel()

        vm.login("u", "p", storeCredentials = false)

        assertFalse(vm.state.value.isLoggedIn)
        assertEquals(0, stats.loginCount)
    }

    @Test fun `login that throws Unauthorized is handled without logging in`() {
        loginRepo.throwOnLogin = ApiException.Unauthorized()
        val vm = viewModel()

        vm.login("u", "p", storeCredentials = false)

        assertFalse(vm.state.value.isLoggedIn)
    }

    @Test fun `checkLoggedIn reflects the server and clears loading`() {
        loginRepo.loggedInResult = Response(true)
        val vm = viewModel()

        vm.checkLoggedIn()

        assertTrue(vm.state.value.isLoggedIn)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun `an unauthorized event drops the session`() {
        val vm = viewModel()
        vm.login("u", "p", storeCredentials = false)
        assertTrue(vm.state.value.isLoggedIn)

        errorHandler.handle(ApiException.Unauthorized())

        assertFalse(vm.state.value.isLoggedIn)
    }

    @Test fun `consumeAutoLogin is one-shot`() {
        val vm = viewModel()
        assertTrue(vm.consumeAutoLogin())
        assertFalse(vm.consumeAutoLogin())
    }

    @Test fun `logout clears the session`() {
        val vm = viewModel()
        vm.login("u", "p", storeCredentials = false)

        vm.logout()

        assertFalse(vm.state.value.isLoggedIn)
    }
}
