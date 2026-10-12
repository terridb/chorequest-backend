# Changelog

## [0.2.2](https://github.com/terridb/chorequest-backend/compare/v0.2.1...v0.2.2) (2026-10-12)


### Bug Fixes

* upgraded spring boot, tomcat and jackson for vulnerabilities ([a8100a3](https://github.com/terridb/chorequest-backend/commit/a8100a3f2fdfcc4d7187cb7d2508be7728d3dfbc))
* upgraded spring boot, tomcat and jackson for vulnerabilities ([53be5a6](https://github.com/terridb/chorequest-backend/commit/53be5a66e44310ebe92f61061d3219643082233a))

## [0.2.1](https://github.com/terridb/chorequest-backend/compare/v0.2.0...v0.2.1) (2026-10-12)


### Bug Fixes

* loaded auth0 configuration on first request instead of at startup ([63ce5e7](https://github.com/terridb/chorequest-backend/commit/63ce5e7b991bc9e0ba46ef8da029e2e17928823c))
* loaded auth0 configuration on first request instead of at startup ([e473224](https://github.com/terridb/chorequest-backend/commit/e473224cb78e8b40774302baf1207d25db017feb))

## [0.2.0](https://github.com/terridb/chorequest-backend/compare/v0.1.0...v0.2.0) (2026-10-12)


### Features

* added a time zone per household for due dates and assignment scheduling ([b24de90](https://github.com/terridb/chorequest-backend/commit/b24de90d19ccc9c983ca01a10ddd3fa5caaa68aa))
* added AccessDeniedException and getUserBySub for ownership checks ([02dd84c](https://github.com/terridb/chorequest-backend/commit/02dd84cb989222f43290f39daa5285ad0947497a))
* added all dtos ([44a7e68](https://github.com/terridb/chorequest-backend/commit/44a7e686c9562f8912257c7278fb22a25b14c4b0))
* added all repositories ([a304333](https://github.com/terridb/chorequest-backend/commit/a304333b7e2233a2a0b43e5b932e1533407ea95b))
* added Base and Task entity and Effort and Frequency enums ([73d011b](https://github.com/terridb/chorequest-backend/commit/73d011b95cf83aaf9b85cd3a2ce4d79f6a11fc23))
* added controller and service for tasks. Added rnf exception ([44b2e17](https://github.com/terridb/chorequest-backend/commit/44b2e1721c2327e42269f87ba0e43f8ac286a91c))
* added CorsConfig and data.sql ([2035c1a](https://github.com/terridb/chorequest-backend/commit/2035c1ae8ebe94ba3fa0cf167a36ff140ec520c2))
* added database connection and fixed taskservice ([5a26051](https://github.com/terridb/chorequest-backend/commit/5a26051b27cf82f959c03a67ea6d1dceab586990))
* added delete logic to task assignment ([a54cd14](https://github.com/terridb/chorequest-backend/commit/a54cd149098339fe1b77590bd6433964c0188001))
* added endpoint for getting completions by goal ([2c21c57](https://github.com/terridb/chorequest-backend/commit/2c21c577f62b145dbbf44b2e6fd81c5320371f38))
* added endpoints for tasks ([2722873](https://github.com/terridb/chorequest-backend/commit/2722873185990e652d54244523c9f0ac99b0b7aa))
* added error handling for validation errors, data conflicts and unexpected exceptions ([5de9e55](https://github.com/terridb/chorequest-backend/commit/5de9e550d0624fb949bfebfda9ed0c8a1fac9f69))
* added GET /api/efforts endpoint for effort-to-points mapping ([2cde388](https://github.com/terridb/chorequest-backend/commit/2cde388efb2b12042da2e493926a74574f60d7b8))
* added get and post endpoints for taskassignment and a scheduled task ([b3a2014](https://github.com/terridb/chorequest-backend/commit/b3a2014ab9f1258f31137052ae961c68864ea4a2))
* added goal crud endpoints and extracted HouseholdService ([a0047fb](https://github.com/terridb/chorequest-backend/commit/a0047fb4238d6d5a4180d1437a0aa7f28d5550fb))
* added goal dto's and repositories ([2c0a2e9](https://github.com/terridb/chorequest-backend/commit/2c0a2e97755e9e4b0acbd84542f569b6d41f982e))
* added goal entities ([71023f3](https://github.com/terridb/chorequest-backend/commit/71023f385d1d05f83a6acd6f5dc51ead237aff0c))
* added Household and User entities ([6e986da](https://github.com/terridb/chorequest-backend/commit/6e986da8f8a6e239731fbe7e2af17dca7c6ee359))
* added household invites for joining a household ([d3b009d](https://github.com/terridb/chorequest-backend/commit/d3b009d69ca6bcfa8f6e0465c3abf15977796493))
* added household member endpoints with duplicate and deletion safety checks ([63be09c](https://github.com/terridb/chorequest-backend/commit/63be09cf32187161b33e1376c0ddfe551101a92a))
* added household member roles with admin promotion and succession ([d79b007](https://github.com/terridb/chorequest-backend/commit/d79b0076131326f572313734a95c82181ac6bd8f))
* added HouseholdMember ([6f9ee4e](https://github.com/terridb/chorequest-backend/commit/6f9ee4ea69c1ab9ff22b83544fd064bf60efa2d4))
* added HouseholdMemberService ([fd18381](https://github.com/terridb/chorequest-backend/commit/fd18381b05c7892585be83fea446db53f70ae9ab))
* added HouseholdService and Controller ([52df6c7](https://github.com/terridb/chorequest-backend/commit/52df6c7d411c564c706caed1590c6bfde1e02691))
* added liveness endpoint ([4cbfd32](https://github.com/terridb/chorequest-backend/commit/4cbfd32520fe298963c3af2cf0c2c1bae98a0b0e))
* added missing UUIDS to response dto's ([0641ffd](https://github.com/terridb/chorequest-backend/commit/0641ffd069dffaf52b170492ed2a7e8daa49025e))
* added open assignments endpoint and adjusted effort points ([2bf9e99](https://github.com/terridb/chorequest-backend/commit/2bf9e9927bc5640b0dab6b1f328de96981a24384))
* added OpenAPI/Swagger documentation ([ae50d09](https://github.com/terridb/chorequest-backend/commit/ae50d097e612b7a3e122852696854dffc03f866d))
* added other mappers ([10704ba](https://github.com/terridb/chorequest-backend/commit/10704babd899da0b526cb2a324605f2bc28f362c))
* added per-user rate limiting with token buckets ([2b1013a](https://github.com/terridb/chorequest-backend/commit/2b1013a61250e002e475a6b152b734d7ad5b59e7))
* added responsible members to tasks ([715ce2a](https://github.com/terridb/chorequest-backend/commit/715ce2a3095a8b4ffeebe7da98c1cac5d3ff6a90))
* added task completion endpoints and adjusted point system ([0f2e17b](https://github.com/terridb/chorequest-backend/commit/0f2e17b52e1820edaa2111d57f67c0705384909c))
* added task management entities ([6f80c03](https://github.com/terridb/chorequest-backend/commit/6f80c033326f2b231b80ba6103172a792ed3178f))
* added TaskMapper ([629a30c](https://github.com/terridb/chorequest-backend/commit/629a30c4965c66f37d4a97b2d187545d0eae6baa))
* added update and getById endpoint for taskassignment ([632f8aa](https://github.com/terridb/chorequest-backend/commit/632f8aae053247df498218423470d1552c02c44e))
* allowed ROTATING tasks to set a starting member ([d66ab90](https://github.com/terridb/chorequest-backend/commit/d66ab9065585638dc642c9886d51465f81fc8a5a))
* auto-add creator as household member on household creation ([24afdfc](https://github.com/terridb/chorequest-backend/commit/24afdfcf89bc64f735e6c79831b39413d665a863))
* auto-contribute completed tasks to the active household goal ([ccdba0f](https://github.com/terridb/chorequest-backend/commit/ccdba0f8a94e69cb4b8ab8c32e60344b31859f56))
* auto-generate task assignments on creation and after completion ([f127f40](https://github.com/terridb/chorequest-backend/commit/f127f40cb406e8cb02092e431d22fb8d5df6ddcb))
* Feature/user management ([a0f2b14](https://github.com/terridb/chorequest-backend/commit/a0f2b14b00144b9b757c9e8549764c7c070534eb))
* integrated Auth0 as OAuth2 resource server ([cb1db84](https://github.com/terridb/chorequest-backend/commit/cb1db8471fd12d732c05df6337dc6f33ee21e6ff))
* made CORS origins and Auth0 settings configurable via environment variables ([b1117de](https://github.com/terridb/chorequest-backend/commit/b1117de2da09af8483bb83cb9bd26578fd180ed4))
* replaced Hibernate schema generation with Flyway migrations ([6e4381a](https://github.com/terridb/chorequest-backend/commit/6e4381a7e54b2ea9fae44b99baf1f51d7f95ae4e))
* soft-deleted household members to preserve their history ([d2d1ba7](https://github.com/terridb/chorequest-backend/commit/d2d1ba7683f4b83f1aeb2cf76fa7559658774148))
* updated relations ([b2863fb](https://github.com/terridb/chorequest-backend/commit/b2863fba8057963dca0fb7d251942f9dd53ecf3f))


### Bug Fixes

* adjusted release workflow ([e2a7724](https://github.com/terridb/chorequest-backend/commit/e2a7724ccef3a5ce8cd7177273f34576e5b16e69))
* enforced household membership on household and member endpoints ([949fc11](https://github.com/terridb/chorequest-backend/commit/949fc11c9e79e8b26448043f9b56de0c9a8a2282))
* enforced household membership on task, assignment, completion, and goal endpoints ([4975945](https://github.com/terridb/chorequest-backend/commit/4975945082636a9d20a2de84c1f91b22890c57b9))
* Fixes ([a71a01d](https://github.com/terridb/chorequest-backend/commit/a71a01dd302144e1cbc09d56aea6775ff4478380))
* made household and task creation transactional ([5f44066](https://github.com/terridb/chorequest-backend/commit/5f440668a49d22baa477c3ddf545a3651a6171f8))
* removed dead user-creation endpoint and enforced self-only access ([dde2174](https://github.com/terridb/chorequest-backend/commit/dde21742254238ad6d1a140768ced7120b36a1ab))
* returned 204 from GET current goal when household has no active goal ([2cc74de](https://github.com/terridb/chorequest-backend/commit/2cc74de3acc9b7e1ffc744425455dc08b129a17d))
* returned 204 from GET current invite when household has no active invite ([9d66c0e](https://github.com/terridb/chorequest-backend/commit/9d66c0ebfd5fe2f3db1eb245f4aa143f539fa78a))
