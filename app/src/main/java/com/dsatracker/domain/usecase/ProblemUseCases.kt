package com.dsatracker.domain.usecase

import com.dsatracker.data.local.entity.ProblemStatus
import com.dsatracker.data.local.entity.RevisionRating
import com.dsatracker.data.local.entity.UserProblemProgressEntity
import com.dsatracker.data.local.entity.RevisionCardEntity
import com.dsatracker.data.local.entity.DailyStatsEntity
import com.dsatracker.data.repository.DSARepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class UpdateProblemStatusUseCase @Inject constructor(
    private val repository: DSARepository,
    private val recordProblemSolvedUseCase: RecordProblemSolvedUseCase
) {
    suspend operator fun invoke(
        userId: Long,
        problemId: Long,
        newStatus: ProblemStatus,
        timeTakenSeconds: Int? = null,
        neededHelp: Boolean? = null
    ) {
        val now = System.currentTimeMillis()
        val existingProgress = repository.getProgress(userId, problemId)

        if (existingProgress == null) {
            // Create new progress
            val newProgress = UserProblemProgressEntity(
                userId = userId,
                problemId = problemId,
                status = newStatus,
                timesSolved = if (newStatus == ProblemStatus.SOLVED) 1 else 0,
                lastSolvedAt = if (newStatus == ProblemStatus.SOLVED) now else null,
                timeTakenSeconds = timeTakenSeconds,
                neededHelp = neededHelp ?: false,
                createdAt = now,
                updatedAt = now
            )
            repository.insertProgress(newProgress)

            // Record stats if solved
            if (newStatus == ProblemStatus.SOLVED) {
                recordProblemSolvedUseCase(userId)
                // Award XP
                repository.addXpEvent(userId, "PROBLEM_SOLVED", 10)
            }
        } else {
            // Update existing progress
            val wasSolvedBefore = existingProgress.status == ProblemStatus.SOLVED
            val isSolvedNow = newStatus == ProblemStatus.SOLVED

            val updatedProgress = existingProgress.copy(
                status = newStatus,
                timesSolved = if (isSolvedNow && !wasSolvedBefore) {
                    existingProgress.timesSolved + 1
                } else {
                    existingProgress.timesSolved
                },
                lastSolvedAt = if (isSolvedNow) now else existingProgress.lastSolvedAt,
                timeTakenSeconds = timeTakenSeconds ?: existingProgress.timeTakenSeconds,
                neededHelp = neededHelp ?: existingProgress.neededHelp,
                updatedAt = now
            )

            repository.updateProgress(updatedProgress)

            // Record stats if newly solved
            if (isSolvedNow && !wasSolvedBefore) {
                recordProblemSolvedUseCase(userId)
                // Award XP
                repository.addXpEvent(userId, "PROBLEM_SOLVED", 10)
            }

            // Deactivate revision card if status is no longer SOLVED
            if (wasSolvedBefore && !isSolvedNow) {
                repository.deactivateRevisionCard(userId, problemId)
            }
        }
    }
}

class ScheduleRevisionCardUseCase @Inject constructor(
    private val repository: DSARepository,
    private val recordRevisionDoneUseCase: RecordRevisionDoneUseCase
) {
    suspend operator fun invoke(
        userId: Long,
        problemId: Long,
        rating: RevisionRating
    ) {
        val now = System.currentTimeMillis()
        val existingCard = repository.getActiveRevisionCard(userId, problemId)

        // Determine next review date based on rating
        val daysToAdd = when (rating) {
            RevisionRating.HARD -> 1
            RevisionRating.OKAY -> 3
            RevisionRating.EASY -> 7
        }

        val nextReviewDate = LocalDate.now().plusDays(daysToAdd.toLong())
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        if (existingCard == null) {
            // Create new card
            val newCard = RevisionCardEntity(
                userId = userId,
                problemId = problemId,
                nextRevisionDateUtc = nextReviewDate,
                difficultyRating = rating.ordinal + 1,
                reviewCount = 1,
                lastReviewedAtUtc = now,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            repository.insertRevisionCard(newCard)
        } else {
            // Update existing card
            val updatedCard = existingCard.copy(
                nextRevisionDateUtc = nextReviewDate,
                difficultyRating = rating.ordinal + 1,
                reviewCount = existingCard.reviewCount + 1,
                lastReviewedAtUtc = now,
                updatedAt = now
            )
            repository.updateRevisionCard(updatedCard)
        }

        // Record revision in stats
        recordRevisionDoneUseCase(userId)

        // Award XP for revision
        repository.addXpEvent(userId, "REVISION_DONE", 5)
    }
}

class RecordProblemSolvedUseCase @Inject constructor(
    private val repository: DSARepository
) {
    suspend operator fun invoke(userId: Long) {
        val today = LocalDate.now().toEpochDay()
        val existingStats = repository.getStats(userId, today)

        if (existingStats == null) {
            val newStats = DailyStatsEntity(
                userId = userId,
                dateLocalEpochDay = today,
                problemsSolvedCount = 1,
                revisionsCount = 0,
                minutesStudied = 0
            )
            repository.insertStats(newStats)
        } else {
            val updatedStats = existingStats.copy(
                problemsSolvedCount = existingStats.problemsSolvedCount + 1,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateStats(updatedStats)
        }
    }
}

class RecordRevisionDoneUseCase @Inject constructor(
    private val repository: DSARepository
) {
    suspend operator fun invoke(userId: Long) {
        val today = LocalDate.now().toEpochDay()
        val existingStats = repository.getStats(userId, today)

        if (existingStats == null) {
            val newStats = DailyStatsEntity(
                userId = userId,
                dateLocalEpochDay = today,
                problemsSolvedCount = 0,
                revisionsCount = 1,
                minutesStudied = 0
            )
            repository.insertStats(newStats)
        } else {
            val updatedStats = existingStats.copy(
                revisionsCount = existingStats.revisionsCount + 1,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateStats(updatedStats)
        }
    }
}

class CalculateStreakUseCase @Inject constructor(
    private val repository: DSARepository
) {
    suspend operator fun invoke(userId: Long): Int {
        // Fetch recent stats
        var streak = 0
        var currentDate = LocalDate.now().toEpochDay()

        while (true) {
            val stats = repository.getStats(userId, currentDate)

            if (stats != null && isActiveDay(stats)) {
                streak++
                currentDate--
            } else {
                break
            }
        }

        return streak
    }

    private fun isActiveDay(stats: DailyStatsEntity): Boolean {
        return stats.problemsSolvedCount >= 1 ||
                stats.revisionsCount >= 1 ||
                stats.minutesStudied >= 20
    }
}
