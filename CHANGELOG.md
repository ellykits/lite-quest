# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-11-07

### Added
- Initial release of LiteQuest
- Core data models with kotlinx.serialization support
- Custom JsonLogic expression evaluator with 15+ operators
- Validation engine with custom rules and error reporting
- Visibility engine for conditional item display
- Calculated values engine for dynamic computations
- Template-based data extraction engine
- Decoupled internationalization system with remote translation loading
- Platform-specific translation loaders (Android, iOS, Desktop)
- Reactive state management with Kotlin StateFlow
- QuestionnaireManager for orchestrating questionnaire lifecycle
- Support for 13 item types (string, text, boolean, decimal, integer, date, time, dateTime, choice, openChoice, display, group, quantity)
- Hierarchical item structure with nested groups
- Required fields and repeatable items support
- Comprehensive test suite with vitals example
- Documentation and usage examples

### Supported Platforms
- Android (API 24+)
- iOS (iOS 13+)
- Desktop (JVM)

### Dependencies
- Kotlin 2.0.21
- kotlinx-serialization-json 1.7.3
- kotlinx-coroutines-core 1.9.0
- kotlinx-datetime 0.6.1
- ktor-client 3.0.1

[1.0.0]: https://github.com/yourusername/lite-quest/releases/tag/v1.0.0
