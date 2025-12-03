package com.dsatracker.data.local.entity

enum class ProblemStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SOLVED,
    SKIPPED
}

enum class ProblemDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class Platform {
    LEETCODE,
    GFG,
    TUF,
    OTHER
}

enum class GoalMode {
    PROBLEMS_PER_DAY,
    MINUTES_PER_DAY
}

enum class RevisionRating {
    HARD,    // Review in 1 day
    OKAY,    // Review in 3 days
    EASY     // Review in 7 days
}
