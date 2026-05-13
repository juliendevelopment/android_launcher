package dev.julien.launcher.domain.usecase

import dev.julien.launcher.data.grid.GridRepository
import dev.julien.launcher.domain.model.AppEntry
import dev.julien.launcher.domain.model.GridCoords
import javax.inject.Inject

/**
 * Combine two app shortcuts at the same cell into a new folder. The folder takes the
 * target cell; both source items are removed.
 */
class CreateFolderUseCase
    @Inject
    constructor(
        private val gridRepository: GridRepository,
    ) {
        suspend operator fun invoke(
            target: GridCoords,
            first: AppEntry,
            second: AppEntry,
        ) {
            gridRepository.createFolder(target, listOf(first, second))
        }
    }
