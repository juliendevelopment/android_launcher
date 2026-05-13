package dev.julien.launcher.domain.usecase

import dev.julien.launcher.data.grid.GridRepository
import javax.inject.Inject

class RemoveItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke(itemId: Long) = gridRepository.remove(itemId)
}
