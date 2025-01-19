# Kavi - Multi-variant Dice Game

A modern Android dice game application implementing multiple classic and custom dice game variants with AI opponents. Built using Jetpack Compose and following clean architecture principles.

## Game Variants

### 1. Pig
A simple press-your-luck dice game where players:
- Roll a single die repeatedly to accumulate points
- Can bank points at any time
- Lose all accumulated points if they roll a 1
- First to reach 100 points wins

### 2. Greed
A complex dice game with multiple scoring combinations:
- Roll six dice and choose which to keep
- Score combinations like straights, sets, and single 1s/5s
- Must score at least 800 points to start banking
- First to reach 10,000 points wins

### 3. Balut
A strategic dice game similar to Yahtzee:
- Roll five dice up to three times per turn
- Score in various categories (pairs, straights, etc.)
- Each category can only be used once
- Highest total score wins after all categories are filled

## Features

- 🎲 Multiple game variants with different rule sets
- 🤖 Adaptive AI opponent with dynamic difficulty
- 📊 Comprehensive statistics tracking
- 🎮 Intuitive touch and shake controls
- 🎨 Customizable interface themes
- 📱 Modern Material Design 3 UI

## Technical Details

### Architecture
- MVVM architecture with Clean Architecture principles
- Dependency injection using Hilt
- Kotlin Coroutines and Flow for reactive programming
- Jetpack Compose for modern UI implementation

### Key Components
- GameManagers: Handle game logic and state management
- ScoreCalculator: Processes dice combinations and scoring
- StatisticsManager: Tracks game statistics and player behavior
- AI Implementation: Adaptive opponent with strategic decision making

## Setup and Installation

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on an Android device/emulator (min SDK 30)

## Development

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Material Design 3 for UI components
- Jetpack Compose for modern UI toolkit
- Android Architecture Components 