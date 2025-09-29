# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.1.2] - 2025-09-29

### Added

* Support the data type `PIC N VARYING`. (#134)
* Improve the readability of converted SQL statements. (#136)

### Fixed

* Fix SQL statement generations for some cases. (#99)
  * With older versions, the precompiler ignores SQL statements for some cases.
* The precompiler accepts INCLUDE statements containing '\n' characters. (#121)

### Miscellaneous

* Add README_JP.md for Japanese users. (#112)
* Add Dev container configuration. (#107)

## [1.1.1] - 2024-09-30

### Added
- Implement variable length character strings (#94)
  - For example, `01 WS-VAR PIC X(10) VARYING.`

## [1.1.0] - 2024-08-30

### Added
- Support Windows with Visual Studio 2022(#82)

### Optimized
- Implemente caches for prepared statements (#58)

### Fix
- Fix prepaed statements (#53)

### Miscellaneous
- Support Ubuntu 24.04 (#56)

## [1.0.3] - 2023-08-31

### Added
- Implement the bulk fetch (#36)

### Optimized
- Refactor the runtime library entirely and remove overheads (#36)
- Implement caching for SQL statements (#36)

## [1.0.2] - 2023-04-30

### Added
- Open COBOL ESQL 4J accepts `EXEC SQL INCLUDE "filename" END EXEC`
### Fixed
- Fix the bug related to the installation

## [1.0.1] - 2022-11-22

### Added
- Implement host variables with COMP-3

### Fixed
- Fix the bug of where clause
- Fix the bug of EXEC SQL INCLUDE
- Improve SQLCA

## [1.0.0] - 2021-12-02

### Added
- Add test cases for COBOL data types, SQL data types and SQLCA
- Support additional COBOL data types (S9(n), N(n)).
- Create template documentations by Sphinx.
