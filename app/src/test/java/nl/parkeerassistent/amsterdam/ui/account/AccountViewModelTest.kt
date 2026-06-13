package nl.parkeerassistent.amsterdam.ui.account

import nl.parkeerassistent.amsterdam.FakeCredentialStore
import nl.parkeerassistent.amsterdam.data.model.Credentials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountViewModelTest {

    private val store = FakeCredentialStore()
    private fun viewModel() = AccountViewModel(store)

    @Test fun `is not authenticated before a prompt and accounts are empty`() {
        store.store("alice", "secret", null)
        val vm = viewModel()

        assertFalse(vm.isAuthenticated())
        assertTrue(vm.accounts.value.isEmpty())
    }

    @Test fun `onAuthenticated opens the window and loads accounts`() {
        store.store("alice", "secret", "Home")
        val vm = viewModel()

        vm.onAuthenticated()

        assertTrue(vm.isAuthenticated())
        assertEquals(1, vm.accounts.value.size)
        assertEquals("alice", vm.account("alice")?.username)
        assertNull(vm.account("nobody"))
    }

    @Test fun `addAccount blanks the alias and reloads`() {
        val vm = viewModel()
        vm.onAuthenticated()

        vm.addAccount("bob", "pw", "   ")

        val account = vm.account("bob")!!
        assertEquals("bob", account.username)
        assertNull(account.alias)
    }

    @Test fun `updateAccount migrates the recent username when it was renamed`() {
        store.store("alice", "secret", null)
        store.recent = "alice"
        val vm = viewModel()
        vm.onAuthenticated()

        vm.updateAccount(Credentials(null, "alice", "secret"), "alice2", "new", "Alias")

        assertEquals("alice2", store.recent)
        assertEquals("Alias", vm.account("alice2")?.alias)
    }

    @Test fun `deleteAccount moves recent to the next account`() {
        store.store("alice", "a", null)
        store.store("bob", "b", null)
        store.recent = "alice"
        val vm = viewModel()
        vm.onAuthenticated()

        vm.deleteAccount(Credentials(null, "alice", "a"))

        assertEquals(1, vm.accounts.value.size)
        assertEquals("bob", store.recent)
    }

    @Test fun `deleteAccount clears recent when no accounts remain`() {
        store.store("alice", "a", null)
        store.recent = "alice"
        val vm = viewModel()
        vm.onAuthenticated()

        vm.deleteAccount(Credentials(null, "alice", "a"))

        assertTrue(vm.accounts.value.isEmpty())
        assertNull(store.recent)
    }

    @Test fun `autoLogin reads and writes through the store`() {
        store.autoLoginEnabled = true
        val vm = viewModel()

        assertTrue(vm.autoLogin)
        vm.autoLogin = false
        assertFalse(store.autoLoginEnabled)
    }
}
