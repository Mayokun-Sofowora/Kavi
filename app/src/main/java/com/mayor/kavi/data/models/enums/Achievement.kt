package com.mayor.kavi.data.models.enums

/**
 * Represents different achievements that a player can earn.
 * Each achievement has specific criteria and thresholds for completion.
 */
enum class Achievement(val description: String) {
    // Winning Streaks
    STREAK_MASTER("Win 15 games in a row"),
    WINNING_SPREE("Win 25 games in a row"),
    
    // Comeback Achievements
    COMEBACK_KING("Win 10 games after being significantly behind"),
    CLUTCH_MASTER("Win 5 games with very close scores"),
    
    // Consistency Achievements
    CONSISTENT_PLAYER("Maintain a high average score across 20+ games"),
    PERFECTIONIST("Score maximum points in any category 10 times"),
    
    // Risk Taking
    RISK_TAKER("Consistently make high-risk decisions"),
    HIGH_ROLLER("Bank a score over 2000 in Greed"),
    
    // Speed Achievements
    SPEED_STAR("Make decisions quickly"),
    LIGHTNING_FAST("Complete a game in under 2 minutes"),
    
    // Experience Achievements
    VETERAN_PLAYER("Play for 20 hours total"),
    DICE_MASTER("Win 100 games total"),
    
    // Game-Specific Achievements
    BALUT_EXPERT("Score over 300 in Balut"),
    GREED_GURU("Win a Greed game with over 3000 points"),
    PIG_PRODIGY("Win a Pig game without the opponent scoring")
}
