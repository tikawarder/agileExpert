package hu.agilexpert;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test: launches the full application as a subprocess and verifies
 * console output. This is NOT a component integration test — it tests the
 * complete user-facing flow from stdin to stdout.
 *
 * For component-level integration tests (service + real DB), see PersistenceTest.
 *
 * Run with: mvn test -Dgroups=e2e
 * Excluded from default mvn test run (see Surefire config in pom.xml).
 */
@Tag("e2e")
class SystemIntegrationTest {

    @Test
    void applicationStartsAndAcceptsUserCreation() throws Exception {
        String inputs = String.join("\n",
            "1", "E2E Test User",   // Create user
            "3",                    // Show state (no user selected yet, shows message)
            "0"                     // Exit
        ) + "\n";

        ProcessBuilder pb = new ProcessBuilder("mvn", "-q", "exec:java");
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (OutputStream os = process.getOutputStream()) {
            os.write(inputs.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }

        boolean finished = process.waitFor(90, TimeUnit.SECONDS);
        if (!finished) process.destroy();

        String fullOutput = String.join("\n", outputLines);
        assertThat(finished).as("Application timed out").isTrue();
        assertThat(process.exitValue()).as("Application exited with error.\nOutput:\n" + fullOutput).isEqualTo(0);
        assertThat(fullOutput).contains("Welcome to the AgileXpert OS simulator!");
        assertThat(fullOutput).contains("Created User: E2E Test User");
        assertThat(fullOutput).contains("Goodbye!");
    }
}
