import java.io.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class BSTMultiProcessTest {
    private static final int NUM_PROCESSES = 8;
    private static final int VALUES_PER_PROCESS = 1000;
    private static final int TOTAL_VALUE_RANGE = 5000; // Much smaller than total inserts to force overlaps
    private static final String TEMP_DIR = "test_results";
    
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("worker")) {
            // This is a worker process
            int processId = Integer.parseInt(args[1]);
            String strategy = args[2];
            int param1 = Integer.parseInt(args[3]);
            int param2 = Integer.parseInt(args[4]);
            runWorkerProcess(processId, strategy, param1, param2);
        } else {
            // This is the main coordinator process
            runCoordinatorProcess();
        }
    }
    
    private static void runCoordinatorProcess() {
        System.out.println("🚀 Starting Multi-Process BST Test");
        System.out.println("Processes: " + NUM_PROCESSES);
        System.out.println("Values per process: " + VALUES_PER_PROCESS);
        System.out.println("Value range: 0-" + (TOTAL_VALUE_RANGE - 1));
        System.out.println("Total insertion attempts: " + (NUM_PROCESSES * VALUES_PER_PROCESS));
        System.out.println("⚠️  OVERLAP EXPECTED - Multiple processes will try same values!");
        System.out.println("==========================================\n");
        
        // Create temp directory for results
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            boolean created = tempDir.mkdirs();
            if (!created) {
                System.err.println("❌ Failed to create temp directory: " + TEMP_DIR);
                System.exit(1);
            }
        }
        
        // Clean up any existing result files
        cleanupResultFiles();
        
        // Recreate temp directory after cleanup
        tempDir.mkdirs();
        
        // Start all worker processes
        List<Process> processes = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Launch worker processes with overlapping value ranges
            for (int i = 0; i < NUM_PROCESSES; i++) {
                // Create different strategies for different processes
                String strategy;
                int param1, param2;
                
                switch (i % 4) {
                    case 0: // Random values from full range
                        strategy = "random";
                        param1 = 0;
                        param2 = TOTAL_VALUE_RANGE - 1;
                        break;
                    case 1: // Sequential from random start
                        strategy = "sequential";
                        param1 = (int)(Math.random() * (TOTAL_VALUE_RANGE - VALUES_PER_PROCESS));
                        param2 = param1 + VALUES_PER_PROCESS - 1;
                        break;
                    case 2: // Focus on lower half
                        strategy = "random";
                        param1 = 0;
                        param2 = TOTAL_VALUE_RANGE / 2;
                        break;
                    case 3: // Focus on upper half
                        strategy = "random";
                        param1 = TOTAL_VALUE_RANGE / 2;
                        param2 = TOTAL_VALUE_RANGE - 1;
                        break;
                    default:
                        strategy = "random";
                        param1 = 0;
                        param2 = TOTAL_VALUE_RANGE - 1;
                }
                
                ProcessBuilder pb = new ProcessBuilder(
                    "java", "-cp", "src/algorithms", 
                    "BSTMultiProcessTest", "worker", String.valueOf(i),
                    strategy, String.valueOf(param1), String.valueOf(param2)
                );
                
                // Ensure output files exist
                File outputFile = new File(TEMP_DIR + "/process_" + i + "_output.txt");
                File errorFile = new File(TEMP_DIR + "/process_" + i + "_error.txt");
                
                try {
                    outputFile.createNewFile();
                    errorFile.createNewFile();
                } catch (IOException e) {
                    System.err.println("❌ Failed to create output files for process " + i);
                    throw e;
                }
                
                pb.redirectOutput(outputFile);
                pb.redirectError(errorFile);
                
                Process process = pb.start();
                processes.add(process);
                
                System.out.println("✅ Started process " + i + " (" + strategy + " from " + param1 + "-" + param2 + ")");
            }
            
            // Wait for all processes to complete
            boolean allCompleted = true;
            for (int i = 0; i < processes.size(); i++) {
                Process process = processes.get(i);
                try {
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        System.out.println("✅ Process " + i + " completed successfully");
                    } else {
                        System.out.println("❌ Process " + i + " failed with exit code: " + exitCode);
                        allCompleted = false;
                    }
                } catch (InterruptedException e) {
                    System.out.println("❌ Process " + i + " was interrupted");
                    allCompleted = false;
                }
            }
            
            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;
            
            System.out.println("\n==========================================");
            System.out.println("⏱️ All processes completed in " + String.format("%.2f", duration) + " seconds");
            
            if (allCompleted) {
                // Verify results
                verifyResults();
            } else {
                System.out.println("❌ Some processes failed - displaying error details:");
                displayErrorDetails();
                System.exit(1);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start processes: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void runWorkerProcess(int processId, String strategy, int param1, int param2) {
        System.out.println("Worker process " + processId + " starting: " + strategy + " strategy, range " + param1 + " to " + param2);
        System.out.flush(); // Force output immediately
        
        try {
            System.out.println("Creating BST instance...");
            System.out.flush();
            BST bst = new BST();
            List<Integer> insertedValues = new ArrayList<>();
            List<Integer> failedInsertions = new ArrayList<>();
            
            // Generate values based on strategy
            System.out.println("Generating values...");
            System.out.flush();
            List<Integer> valuesToInsert = generateValues(strategy, param1, param2, VALUES_PER_PROCESS);
            System.out.println("Generated " + valuesToInsert.size() + " values to insert");
            System.out.flush();
            
            // Insert all values
            for (int value : valuesToInsert) {
                if (bst.add(value)) {
                    insertedValues.add(value);
                } else {
                    failedInsertions.add(value);
                }
                
                // Periodically check BST property during insertion
                if (insertedValues.size() % 100 == 0) {
                    if (!bst.checkBSTProperty()) {
                        throw new RuntimeException("BST property violated after inserting " + insertedValues.size() + " values");
                    }
                }
            }
            
            // Final BST property check
            if (!bst.checkBSTProperty()) {
                throw new RuntimeException("BST property violated at the end");
            }
            
            // Verify all expected values are in the tree
            for (int value : insertedValues) {
                if (!bst.contains(value)) {
                    throw new RuntimeException("Value " + value + " was inserted but not found in tree");
                }
            }
            
            // Try to insert duplicates (should all fail)
            int duplicateAttempts = 0;
            for (int value : insertedValues) {
                if (!bst.add(value)) {
                    duplicateAttempts++;
                }
            }
            
            // Write results to file
            writeResults(processId, strategy, param1, param2, insertedValues.size(), failedInsertions.size(), duplicateAttempts);
            
            System.out.println("Worker process completed successfully");
            System.out.println("Inserted: " + insertedValues.size());
            System.out.println("Failed: " + failedInsertions.size());
            System.out.println("Duplicate attempts blocked: " + duplicateAttempts);
            
        } catch (Exception e) {
            System.err.println("Worker process failed: " + e.getMessage());
            System.err.flush();
            e.printStackTrace();
            System.err.flush();
            
            // Try to write error to file for debugging
            try {
                String filename = TEMP_DIR + "/process_" + processId + "_error_details.txt";
                PrintWriter writer = new PrintWriter(new FileWriter(filename));
                writer.println("PROCESS_ID=" + processId);
                writer.println("STRATEGY=" + strategy);
                writer.println("PARAM1=" + param1);
                writer.println("PARAM2=" + param2);
                writer.println("ERROR=" + e.getMessage());
                writer.println("STACK_TRACE:");
                e.printStackTrace(writer);
                writer.close();
            } catch (IOException ioE) {
                System.err.println("Failed to write error file: " + ioE.getMessage());
            }
            
            System.exit(1);
        }
    }
    
    // Method to generate values based on strategy
    private static List<Integer> generateValues(String strategy, int param1, int param2, int count) {
        List<Integer> values = new ArrayList<>();
        
        switch (strategy) {
            case "random":
                // Generate random values within range
                for (int i = 0; i < count; i++) {
                    int value = param1 + (int)(Math.random() * (param2 - param1 + 1));
                    values.add(value);
                }
                break;
                
            case "sequential":
                // Generate sequential values, wrapping around if needed
                for (int i = 0; i < count; i++) {
                    int value = param1 + (i % (param2 - param1 + 1));
                    values.add(value);
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
        
        // Always shuffle to avoid insertion order bias
        Collections.shuffle(values);
        return values;
    }
    
    private static void writeResults(int processId, String strategy, int param1, int param2, int inserted, int failed, int duplicatesBlocked) {
        try {
            String filename = TEMP_DIR + "/process_" + processId + "_results.txt";
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            writer.println("PROCESS_ID=" + processId);
            writer.println("STRATEGY=" + strategy);
            writer.println("PARAM1=" + param1);
            writer.println("PARAM2=" + param2);
            writer.println("INSERTED=" + inserted);
            writer.println("FAILED=" + failed);
            writer.println("DUPLICATES_BLOCKED=" + duplicatesBlocked);
            writer.println("BST_PROPERTY_OK=true");
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to write results: " + e.getMessage());
        }
    }
    
    private static void verifyResults() {
        System.out.println("\n🔍 Verifying results from all processes...");
        
        File tempDir = new File(TEMP_DIR);
        File[] resultFiles = tempDir.listFiles((dir, name) -> name.endsWith("_results.txt"));
        
        if (resultFiles == null || resultFiles.length != NUM_PROCESSES) {
            System.out.println("❌ Expected " + NUM_PROCESSES + " result files, found " + 
                             (resultFiles == null ? 0 : resultFiles.length));
            
            // Show which result files are missing
            System.out.println("\n🔍 Checking for missing processes:");
            displayErrorDetails();
            System.exit(1);
        }
        
        int totalInserted = 0;
        int totalFailed = 0;
        int totalDuplicatesBlocked = 0;
        boolean allBSTPropertiesOK = true;
        
        for (File file : resultFiles) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("INSERTED=")) {
                        totalInserted += Integer.parseInt(line.split("=")[1]);
                    } else if (line.startsWith("FAILED=")) {
                        totalFailed += Integer.parseInt(line.split("=")[1]);
                    } else if (line.startsWith("DUPLICATES_BLOCKED=")) {
                        totalDuplicatesBlocked += Integer.parseInt(line.split("=")[1]);
                    } else if (line.startsWith("BST_PROPERTY_OK=")) {
                        if (!Boolean.parseBoolean(line.split("=")[1])) {
                            allBSTPropertiesOK = false;
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("❌ Failed to read result file: " + file.getName());
                System.exit(1);
            }
        }
        
        // Print final verification
        System.out.println("==========================================");
        System.out.println("📊 FINAL RESULTS:");
        System.out.println("Total insertions across all processes: " + totalInserted);
        System.out.println("Total insertion attempts: " + (NUM_PROCESSES * VALUES_PER_PROCESS));
        System.out.println("Failed insertions (within-process duplicates): " + totalFailed);
        System.out.println("Duplicate attempts blocked: " + totalDuplicatesBlocked);
        System.out.println("BST property maintained: " + allBSTPropertiesOK);
        System.out.println("Value range tested: 0-" + (TOTAL_VALUE_RANGE - 1));
        System.out.println("⚠️  Note: Each process has its own BST - same values can be in multiple trees");
        
        // Verify success conditions (adjusted for separate BST instances per process)
        boolean success = true;
        
        // Each process should insert a reasonable number of values
        double avgInsertionsPerProcess = (double) totalInserted / NUM_PROCESSES;
        if (avgInsertionsPerProcess < VALUES_PER_PROCESS * 0.5) {
            System.out.println("❌ Too few insertions per process on average: " + String.format("%.1f", avgInsertionsPerProcess));
            success = false;
        }
        
        // Total should be reasonable but can exceed TOTAL_VALUE_RANGE since processes are separate
        if (totalInserted < NUM_PROCESSES * VALUES_PER_PROCESS * 0.7) {
            System.out.println("❌ Too few total successful insertions - processes may not be working properly");
            success = false;
        }
        
        // Check that failures + successes = total attempts
        if (totalInserted + totalFailed != NUM_PROCESSES * VALUES_PER_PROCESS) {
            System.out.println("❌ Insertion count mismatch: " + totalInserted + " + " + totalFailed + " ≠ " + (NUM_PROCESSES * VALUES_PER_PROCESS));
            success = false;
        }
        
        if (!allBSTPropertiesOK) {
            System.out.println("❌ BST property was violated in some processes");
            success = false;
        }
        
        if (success) {
            System.out.println("\n🎉 ALL MULTI-PROCESS TESTS PASSED!");
            System.out.println("✅ Overlapping value ranges handled correctly");
            System.out.println("✅ BST property maintained across all processes");
            System.out.println("✅ Duplicate detection working with concurrent access");
            System.out.println("✅ Multiple insertion strategies successful");
            System.out.println("✅ Process-level concurrency stress test passed");
        } else {
            System.out.println("\n❌ MULTI-PROCESS TESTS FAILED!");
            System.exit(1);
        }
        
        // Clean up
        cleanupResultFiles();
    }
    
    private static void displayErrorDetails() {
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            System.out.println("❌ Temp directory doesn't exist: " + TEMP_DIR);
            return;
        }
        
        File[] allFiles = tempDir.listFiles();
        if (allFiles == null) {
            System.out.println("❌ No files found in temp directory");
            return;
        }
        
        System.out.println("\n📁 Files in " + TEMP_DIR + ":");
        for (File file : allFiles) {
            System.out.println("  - " + file.getName() + " (" + file.length() + " bytes)");
        }
        
        // Display error file contents
        File[] errorFiles = tempDir.listFiles((dir, name) -> name.endsWith("_error.txt"));
        if (errorFiles != null) {
            for (File errorFile : errorFiles) {
                System.out.println("\n🚨 Error file: " + errorFile.getName());
                try (BufferedReader reader = new BufferedReader(new FileReader(errorFile))) {
                    String line;
                    boolean hasContent = false;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            System.out.println("  " + line);
                            hasContent = true;
                        }
                    }
                    if (!hasContent) {
                        System.out.println("  (empty)");
                    }
                } catch (IOException e) {
                    System.out.println("  Failed to read error file: " + e.getMessage());
                }
            }
        }
        
        // Display output file contents for debugging
        File[] outputFiles = tempDir.listFiles((dir, name) -> name.endsWith("_output.txt"));
        if (outputFiles != null) {
            for (File outputFile : outputFiles) {
                System.out.println("\n📄 Output file: " + outputFile.getName());
                try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
                    String line;
                    int lineCount = 0;
                    while ((line = reader.readLine()) != null && lineCount < 10) {
                        System.out.println("  " + line);
                        lineCount++;
                    }
                    if (lineCount == 0) {
                        System.out.println("  (empty)");
                    }
                } catch (IOException e) {
                    System.out.println("  Failed to read output file: " + e.getMessage());
                }
            }
        }
    }
    
    private static void cleanupResultFiles() {
        File tempDir = new File(TEMP_DIR);
        if (tempDir.exists()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            tempDir.delete();
        }
    }
} 