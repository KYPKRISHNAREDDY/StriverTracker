package com.dsatracker.di

import android.content.Context
import com.dsatracker.data.local.dao.*
import com.dsatracker.data.local.database.DSADatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DSADatabase {
        return DSADatabase.getInstance(context)
    }

    @Provides
    fun provideUserDao(database: DSADatabase): UserDao = database.userDao()

    @Provides
    fun provideSheetDao(database: DSADatabase): SheetDao = database.sheetDao()

    @Provides
    fun provideTopicDao(database: DSADatabase): TopicDao = database.topicDao()

    @Provides
    fun provideProblemDao(database: DSADatabase): ProblemDao = database.problemDao()

    @Provides
    fun provideUserProblemProgressDao(database: DSADatabase): UserProblemProgressDao =
        database.userProblemProgressDao()

    @Provides
    fun provideUserProblemNoteDao(database: DSADatabase): UserProblemNoteDao =
        database.userProblemNoteDao()

    @Provides
    fun provideRevisionCardDao(database: DSADatabase): RevisionCardDao =
        database.revisionCardDao()

    @Provides
    fun provideDailyStatsDao(database: DSADatabase): DailyStatsDao =
        database.dailyStatsDao()

    @Provides
    fun provideGoalDao(database: DSADatabase): GoalDao = database.goalDao()

    @Provides
    fun provideXpEventDao(database: DSADatabase): XpEventDao = database.xpEventDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()
}
