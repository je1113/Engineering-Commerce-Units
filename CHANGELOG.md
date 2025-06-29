# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release of ECU Core Library
- Basic unit conversion support for:
  - Length (m, cm, mm, km, in, ft, yd, mi)
  - Weight (kg, g, mg, t, lb, oz)
  - Volume (l, ml, m³, gal, qt, pt, fl oz)
  - Temperature (K, °C, °F)
  - Area (m², cm², mm², km², in², ft², yd², mi², ha, ac)
- Java 8 compatibility
- Fluent API for easy unit conversions
- Batch conversion support
- Unit information query system
- Java-friendly API with @JvmStatic and @JvmOverloads annotations

### Changed
- Modularized project structure:
  - `ecu-core`: Core unit conversion functionality
  - `ecu-commerce`: Commerce-specific extensions (to be implemented)
  - `ecu-engineering`: Engineering-specific extensions (to be implemented)

### Fixed
- N/A (initial release)

## [1.0.0] - TBD
- First stable release targeting Maven Central
