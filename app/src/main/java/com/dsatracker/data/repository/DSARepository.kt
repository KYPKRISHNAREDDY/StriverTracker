package com.dsatracker.data.repository

import com.dsatracker.data.local.dao.*
import com.dsatracker.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DSARepository @Inject constructor(
    private val userDao: UserDao,
    private val sheetDao: SheetDao,
    private val topicDao: TopicDao,
    private val problemDao: ProblemDao,
    private val userProblemProgressDao: UserProblemProgressDao,
    private val userProblemNoteDao: UserProblemNoteDao,
    private val revisionCardDao: RevisionCardDao,
    private val dailyStatsDao: DailyStatsDao,
    private val goalDao: GoalDao,
    private val xpEventDao: XpEventDao
) {

    // User operations
    suspend fun getCurrentUser(): UserEntity? = userDao.getCurrentUser()
    suspend fun createUser(name: String?, email: String?): Long {
        val user = UserEntity(name = name, email = email)
        return userDao.insertUser(user)
    }

    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserFlow(userId)

    // Sheet operations
    suspend fun getSheet(sheetId: String): SheetEntity? = sheetDao.getSheet(sheetId)
    suspend fun insertSheet(sheet: SheetEntity) = sheetDao.insertSheet(sheet)
    fun getAllSheets(): Flow<List<SheetEntity>> = sheetDao.getAllSheetsFlow()

    // Topic operations
    suspend fun getTopic(topicId: Long): TopicEntity? = topicDao.getTopic(topicId)

    fun getTopicsBySheet(sheetId: String): Flow<List<TopicEntity>> =
        topicDao.getTopicsBySheet(sheetId)

    fun getTopicsWithProgress(sheetId: String, userId: Long): Flow<List<TopicWithProgress>> =
        topicDao.getTopicsWithProgress(sheetId, userId)

    suspend fun insertTopics(topics: List<TopicEntity>) = topicDao.insertTopics(topics)

    // Problem operations
    suspend fun getProblem(problemId: Long): ProblemEntity? = problemDao.getProblem(problemId)
    fun getProblemsByTopic(topicId: Long): Flow<List<ProblemEntity>> =
        problemDao.getProblemsByTopic(topicId)

    fun getProblemsWithStatus(topicId: Long, userId: Long): Flow<List<ProblemWithStatus>> =
        problemDao.getProblemsWithStatus(topicId, userId)

    suspend fun insertProblems(problems: List<ProblemEntity>) = problemDao.insertProblems(problems)

    suspend fun getTotalProblemsCount(sheetId: String): Int =
        problemDao.getTotalProblemsCount(sheetId)

    // Progress operations
    suspend fun getProgress(userId: Long, problemId: Long): UserProblemProgressEntity? =
        userProblemProgressDao.getProgress(userId, problemId)

    fun getProgressFlow(userId: Long, problemId: Long): Flow<UserProblemProgressEntity?> =
        userProblemProgressDao.getProgressFlow(userId, problemId)

    suspend fun insertProgress(progress: UserProblemProgressEntity): Long =
        userProblemProgressDao.insertProgress(progress)

    suspend fun updateProgress(progress: UserProblemProgressEntity) =
        userProblemProgressDao.updateProgress(progress)

    fun getSolvedCountFlow(userId: Long): Flow<Int> =
        userProblemProgressDao.getSolvedCountFlow(userId)

    // Notes operations
    fun getNoteFlow(userId: Long, problemId: Long): Flow<UserProblemNoteEntity?> =
        userProblemNoteDao.getNoteFlow(userId, problemId)

    suspend fun saveNote(userId: Long, problemId: Long, content: String): Long {
        val existingNote = userProblemNoteDao.getNote(userId, problemId)
        return if (existingNote != null) {
            userProblemNoteDao.updateNote(
                existingNote.copy(content = content, lastUpdatedAt = System.currentTimeMillis())
            )
            existingNote.id
        } else {
            userProblemNoteDao.insertNote(
                UserProblemNoteEntity(
                    userId = userId,
                    problemId = problemId,
                    content = content
                )
            )
        }
    }

    fun getAllNotes(userId: Long): Flow<List<UserProblemNoteEntity>> =
        userProblemNoteDao.getAllNotesFlow(userId)

    // Revision cards
    fun getDueRevisionCards(userId: Long, endDate: Long, limit: Int = 15): Flow<List<RevisionCardEntity>> =
        revisionCardDao.getDueCards(userId, endDate, limit)

    suspend fun getActiveRevisionCard(userId: Long, problemId: Long): RevisionCardEntity? =
        revisionCardDao.getActiveCard(userId, problemId)

    suspend fun insertRevisionCard(card: RevisionCardEntity): Long =
        revisionCardDao.insertCard(card)

    suspend fun updateRevisionCard(card: RevisionCardEntity) =
        revisionCardDao.updateCard(card)

    suspend fun deactivateRevisionCard(userId: Long, problemId: Long) =
        revisionCardDao.deactivateCard(userId, problemId)

    fun getDueRevisionCountFlow(userId: Long, date: Long): Flow<Int> =
        revisionCardDao.getDueCountFlow(userId, date)

    // Daily stats
    suspend fun getStats(userId: Long, date: Long): DailyStatsEntity? =
        dailyStatsDao.getStats(userId, date)

    fun getStatsFlow(userId: Long, date: Long): Flow<DailyStatsEntity?> =
        dailyStatsDao.getStatsFlow(userId, date)

    suspend fun insertStats(stats: DailyStatsEntity): Long =
        dailyStatsDao.insertStats(stats)

    suspend fun updateStats(stats: DailyStatsEntity) =
        dailyStatsDao.updateStats(stats)

    fun getRecentStats(userId: Long, limit: Int = 30): Flow<List<DailyStatsEntity>> =
        dailyStatsDao.getRecentStats(userId, limit)

    // Goal operations
    suspend fun getActiveGoal(userId: Long): GoalEntity? =
        goalDao.getActiveGoal(userId)

    fun getActiveGoalFlow(userId: Long): Flow<GoalEntity?> =
        goalDao.getActiveGoalFlow(userId)

    suspend fun insertGoal(goal: GoalEntity): Long =
        goalDao.insertGoal(goal)

    suspend fun deactivateAllGoals(userId: Long) =
        goalDao.deactivateAllGoals(userId)

    // XP operations
    suspend fun addXpEvent(userId: Long, type: String, points: Int): Long =
        xpEventDao.insertEvent(XpEventEntity(userId = userId, type = type, points = points))

    fun getTotalXpFlow(userId: Long): Flow<Int?> =
        xpEventDao.getTotalXpFlow(userId)
}
