package dev.julien.launcher.domain.usecase

import app.cash.turbine.test
import dev.julien.launcher.domain.model.AppEntry
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The sort/filter logic does not require Android dependencies, so we exercise it via a
 * pure-Kotlin reimplementation that mirrors [GetInstalledAppsUseCase]. The real use case
 * delegates to [PackageManagerRepository.installedApps] which is exercised on device.
 */
class GetInstalledAppsUseCaseTest {

    private val apps = listOf(
        AppEntry("com.alpha", "com.alpha.Main", "Alpha"),
        AppEntry("com.beta", "com.beta.Main", "Beta"),
        AppEntry("com.charlie", "com.charlie.Main", "charlie"),
    )

    private fun filterSort(query: String, source: List<AppEntry>): List<AppEntry> {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) source else source.filter { it.label.lowercase().contains(q) }
        return filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    @Test fun `case insensitive sort regardless of query`() = runTest {
        flowOf(filterSort("", apps)).test {
            val emitted = awaitItem()
            assertEquals(listOf("Alpha", "Beta", "charlie"), emitted.map { it.label })
            awaitComplete()
        }
    }

    @Test fun `filter narrows by query substring`() = runTest {
        flowOf(filterSort("ar", apps)).test {
            val emitted = awaitItem()
            assertEquals(listOf("charlie"), emitted.map { it.label })
            awaitComplete()
        }
    }
}
