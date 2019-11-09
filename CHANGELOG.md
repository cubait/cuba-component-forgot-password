# Changelog

All releases are documented here. This project follows *Semantic Versioning* principles.

## [4.0.0] - 2019-11-09

- Support for CUBA 7.1
- Refactored the ResetPassword screen using the new API
- Removed deprecated code and replaced with the suggested replacements
- Introduced a new login screen (NexbitLoginScreen) created with the new API, that can be used in 
place of the old one (not removed for backwards compatibility)
- Fixed a bug when updating existing tokens in the db

## [3.0.0] - 2019-08-21

- Support for CUBA 7.0 (by Sergey Butenin)

## [2.2.0] - 2019-02-16

- Added Russian translation (by Sergey Butenin)

## [2.1.0] - 2018-12-02

- Added Romanian translation (by Sorin Federiga)

## [2.0.0] - 2018-12-02

- [**BREAKING CHANGE**] Removed allowAnonymous custom attribute for REST services, now that CUBA 6.10 provides the standard anonymousAllowed attribute
- [**BREAKING CHANGE**] Small refactorings to extended login screen to support 6.10 and Hover theme (you need to update your code if you have extended the `loginWindow` screen)

## [1.0.0] - 2018-06-13

- Support for CUBA 6.9.0
- Removed unused `gui` module
- Refactored the reset link handler mechanism by using the new 6.9 support
- Updated all init db scripts to adhere to new naming conventions in 6.9
- Fixed `start*` and `stop*` gradle tasks that were using a wrong syntax
- [**BREAKING CHANGE**] Refactored the login window (you need to update your code if you have extended the `loginWindow` screen)

## [0.2.0] - 2018-04-20

- Support for CUBA 6.8.6
- New extensive usage section in README, plus Postman REST API docs and collections

## [0.1.1] - 2017-10-18

- First public version for CUBA 6.6.4
