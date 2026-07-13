package com.havos.lubricerp.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NetworkMonitorTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkMonitor: NetworkMonitor

    @Before
    fun setUp() {
        context = mock()
        connectivityManager = mock()
        whenever(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE)))
            .thenReturn(connectivityManager)
    }

    private fun stubConnectedNetwork() {
        val network: Network = mock()
        val capabilities: NetworkCapabilities = mock()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(capabilities)
        whenever(capabilities.hasCapability(eq(NetworkCapabilities.NET_CAPABILITY_INTERNET)))
            .thenReturn(true)
    }

    private fun stubDisconnectedNetwork() {
        whenever(connectivityManager.activeNetwork).thenReturn(null)
    }

    @Test
    fun `isCurrentlyOnline returns true when network has internet capability`() {
        stubConnectedNetwork()
        networkMonitor = NetworkMonitor(context)

        assertTrue(networkMonitor.isCurrentlyOnline())
    }

    @Test
    fun `isCurrentlyOnline returns false when activeNetwork is null`() {
        stubDisconnectedNetwork()
        networkMonitor = NetworkMonitor(context)

        assertFalse(networkMonitor.isCurrentlyOnline())
    }

    @Test
    fun `isCurrentlyOnline returns false when network capabilities is null`() {
        val network: Network = mock()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)
        networkMonitor = NetworkMonitor(context)

        assertFalse(networkMonitor.isCurrentlyOnline())
    }

    @Test
    fun `isCurrentlyOnline returns false when network lacks internet capability`() {
        val network: Network = mock()
        val capabilities: NetworkCapabilities = mock()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(capabilities)
        whenever(capabilities.hasCapability(eq(NetworkCapabilities.NET_CAPABILITY_INTERNET)))
            .thenReturn(false)
        networkMonitor = NetworkMonitor(context)

        assertFalse(networkMonitor.isCurrentlyOnline())
    }
}
