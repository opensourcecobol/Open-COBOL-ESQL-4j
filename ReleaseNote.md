### Added

* Support the `PIC N VARYING` data type. (#134)
* Improve the readability of converted SQL statements. (#136)

### Fixed

* Fix SQL statement generations in some cases. (#99)
  * With older versions, the precompiler ignores SQL statements in some cases.
* The precompiler accepts INCLUDE statements containing '\n' characters. (#121)

### Miscellaneous

* Add README_JP.md for Japanese users. (#112)
* Add a Dev container configuration. (#107)