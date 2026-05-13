package dev.julien.launcher.domain.usecase

import dev.julien.launcher.data.apps.PackageManagerRepository
import dev.julien.launcher.domain.model.AppEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Filtered + sorted view over installed launchable apps. Locale-aware case-insensitive sort.
 */
class GetInstalledAppsUseCase
    @Inject
    constructor(
        private val repo: PackageManagerRepository,
    ) {
        operator fun invoke(query: Flow<String>): Flow<List<AppEntry>> =
            combine(repo.installedApps, query) { apps, q ->
                val normalizedQuery = q.trim().lowercase()
                val filtered =
                    if (normalizedQuery.isEmpty()) {
                        apps
                    } else {
                        apps.filter { it.label.lowercase().contains(normalizedQuery) }
                    }
                filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            }
    }
