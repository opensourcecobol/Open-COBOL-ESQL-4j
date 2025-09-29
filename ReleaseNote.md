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