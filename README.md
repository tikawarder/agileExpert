# AgileXpert OS Simulator

AgileXpert OS Simulator is a Java-based simulation of an operating system designed for smart devices. The project demonstrates a client-server architecture where data storage and complex computations (including AI) are handled in the "cloud" (database and LLM).

## Key Features
- **Multi-user Management**: Individual user profiles with their own apps and settings.
- **Dynamic Menu System**: Customizable main menu and submenus.
- **Personalization**: Selectable themes, wallpapers, and icons.
- **AI Assistant**: Natural language command execution (via OpenAI / LangChain4j).
- **System Simulation**: Automated test data generation using Artificial Intelligence.

---

## Prerequisites
To run this project, you will need:
- **Java 17** or higher
- **Maven 3.6** or higher
- (Optional) **OpenAI API Key** for AI features

---

## Installation and Execution

### 1. Clone the Repository
```bash
git clone https://github.com/tikawarder/agileExpert.git
cd agileExpert
```

### 2. Build and Package
The project uses Maven to compile and package everything into a single executable JAR file:
```bash
mvn clean package
```

### 3. Run the Application
After a successful build, you can start the application with the following command:
```bash
java -jar target/agilexpert-os-simulator-1.0-SNAPSHOT.jar
```

---

## Setting up AI Features (Optional)
To use the AI Assistant and Simulation data generator, an OpenAI API key is required.
1. Create a `.env` file in the project root directory.
2. Add your key:
   ```text
   OPENAI_API_KEY=your_actual_api_key_here
   ```

---

## Developer Mode
If you are modifying the code and want to see results quickly without re-packaging, use the developer execution mode:
```bash
mvn compile exec:java
```

---

## Changelog

### Batch 1 – Model layer refactor
- Added Lombok (`provided` scope) to eliminate getter/setter/toString boilerplate across all entity classes.
- Added `@EqualsAndHashCode(of = "id")` to `BaseEntity` so entity equality is based on database ID, fixing silent deduplication failures in collections after a DB reload.
- Added `@ToString.Exclude` on all relationship fields to prevent `LazyInitializationException` and circular reference loops in `toString()`.
- Removed `CascadeType.ALL` from `@ManyToOne` associations in `MenuItem` — cascading deletes through a many-to-one relation could silently delete shared `App` or `Menu` entities.
- `OsService.addSubMenu()` now explicitly calls `em.persist(sub)` since the unsafe cascade no longer handles it.
- Fixed `PersistenceTest` to explicitly persist the `subMenu` entity before associating it with a `MenuItem`.

### Batch 6 – Fix SystemIntegrationTest: proper JUnit E2E test
- Converted `SystemIntegrationTest` from a `main()` method to a proper `@Test` method with JUnit 5 assertions (AssertJ).
- Added `@Tag("e2e")` to clearly mark it as an end-to-end test, distinct from component integration tests (`PersistenceTest`).
- Added a class-level Javadoc comment explaining the difference: E2E tests verify the full console flow via subprocess, integration tests verify service + DB interaction directly.
- Added `maven-surefire-plugin` configuration to `pom.xml` to exclude `e2e`-tagged tests from the default `mvn test` run. Run E2E tests with: `mvn test -Dgroups=e2e`.
- Added `scanner.hasNextLine()` guard in `ConsoleUi.start()` to handle stdin EOF gracefully instead of throwing `NoSuchElementException`.

### Batch 5 – AI integration improvements
- Added `AiAction` enum (`START_APP`, `CHANGE_THEME`, `CREATE_USER`, `UNKNOWN`) to replace fragile free-text String comparisons.
- Extracted `COMMAND_SYSTEM_PROMPT` and `SIMULATION_SYSTEM_PROMPT` as `public static final` constants in `AiService` — prompts are now easy to find, modify, and reuse.
- `AiService` now uses LangChain4j's `SystemMessage` / `UserMessage` separation instead of string concatenation, giving the model proper role context.
- Model name is now loaded from `.env` (`OPENAI_MODEL_NAME`), defaulting to `gpt-4o-mini` if not set.
- JSON response cleanup replaced with a single regex (`replaceAll("(?s)\`\`\`json?\\s*|\`\`\`", "")`) instead of multiple `startsWith` / `endsWith` checks.
- `catch (Exception e)` replaced with `catch (JsonProcessingException e)` for precise JSON parse error handling.
- `SimulationService` now receives `AiService` via constructor injection — no duplicate HTTP client instantiation.
- `ConsoleUi.executeAiCommand()` split into `parseAiAction()` and `handleAiAction()` to reduce nesting depth.
- `SimulationService.runSimulation()` inner loop extracted into `persistUserFromDto()`.

### Batch 4 – Implement startApp()
- `startApp()` now lists the current user's installed apps and lets the user select one by number.
- Selecting an app prints a simulated launch sequence: `[OS] Launching application: <name>...` followed by `[OS] <name> is running.`
- If no apps are installed, the user is directed to option 9 (Install App) instead of silently doing nothing.

### Batch 3 – Complete user CRUD (rename and delete)
- Added `renameUser()` and `deleteUser()` to `UserService` — the task description explicitly required create, modify, and delete for user accounts.
- `deleteUser()` re-attaches the entity via `em.find()` before calling `em.remove()` to ensure it is managed within the transaction.
- `renameUser()` updates the name in-memory and merges the entity in a single transaction.
- Added menu options 12 (Rename Current User) and 13 (Delete Current User) to `ConsoleUi`.
- `deleteUser()` prompts for confirmation and resets `currentUser` to `null` after deletion.
- Replaced generic `catch (Exception e)` with `catch (NumberFormatException e)` in `selectUser()`, `installApp()`, and `modifyMenu()`.

### Batch 2 – EntityManager lifecycle fix
- `DbService` no longer holds a singleton `EntityManager`. The `EntityManagerFactory` remains a singleton, but each operation now opens and closes its own `EntityManager`.
- `inTransaction()` creates a fresh `EntityManager`, begins a transaction, commits or rolls back on error, and always closes the EM in a `finally` block.
- New `inQuery()` helper method for read-only operations that do not require a transaction.
- All `dbService.getEm().createQuery(...)` calls in `OsService` and `UserService` replaced with `dbService.inQuery(...)`.
- `installApp()` and `addAppToMenu()` now re-attach entities via `em.find()` within the transaction instead of relying on a shared persistent context.

---
**Author**: Tamás Biró (tikawarder@gmail.com)
**Technologies**: Java 17, JPA/Hibernate, H2 Database, LangChain4j, Lombok, Maven.
