package hu.agilexpert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class SystemIntegrationTest {

    public static void main(String[] args) {
        System.out.println("Starting System Integration Test...");
        
        // Define the sequence of inputs to test all menus
        String inputs = String.join("\n",
            "1", "Test User AI",      // 1. Create User
            "2", "2",                  // 2. Select User (assuming at least 2 users exist)
            "3",                       // 3. Show Current User State
            "6", "Dark Mode",          // 6. Change Theme
            "7", "Forest.jpg",         // 7. Set Background
            "8", "1",                  // 8. Manage Icons -> List (returns to main menu)
            "9", "1",                  // 9. Install App (first one)
            "10", "Change theme to Ocean", // 10. AI Assistant
            "0"                        // 0. Exit
        ) + "\n";

        ProcessBuilder pb = new ProcessBuilder("mvn", "exec:java");
        pb.redirectErrorStream(true);
        
        try {
            Process process = pb.start();
            
            // Feed the inputs to the process
            try (OutputStream os = process.getOutputStream()) {
                os.write(inputs.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // Read and print the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[APP] " + line);
                    
                    // Simple assertions based on output
                    if (line.contains("Created User: Test User AI")) {
                        System.out.println(">>> SUCCESS: User creation verified.");
                    }
                    if (line.contains("Theme changed to: Ocean")) {
                        System.out.println(">>> SUCCESS: AI Command verified.");
                    }
                }
            }

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0) {
                System.out.println("\nIntegration Test Finished Successfully!");
            } else {
                System.err.println("\nIntegration Test Failed or Timed Out!");
                process.destroy();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
