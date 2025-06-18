package com.example;

import java.io.*;
import java.util.*;

public class BatchFileProcessor_v2 {
    
    public static void main(String[] args) throws Exception {
        
        while (true) {
            try {
                checkForLogFiles();
                Thread.sleep(5000); 
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    // Check for log files and process them
    static void checkForLogFiles() throws Exception {
        File logsFolder = new File("logs");
        if (!logsFolder.exists()) return;
        
        File[] files = logsFolder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.getName().endsWith(".log")) {
                processFile(file);
            }
        }
    }
    
    // Process one log file separated by file type
    static void processFile(File file) throws Exception {
        String filename = file.getName();
        
        // "Record count: x""
        int totalRecordCount = getTotalRecordCount(file);
        
        // from this specific file type only. i.e. Information
        List<String> allRecords = readRecords(file);
        
        // Save position for this file type
        int lastProcessed = getLastProcessedRecord(filename);
        int currentBatch = getCurrentBatchNumber(filename);

        // Get count from the batch file
        int recordsInCurrentBatch = getBatchSize(filename, currentBatch);
        
        System.out.println("File: " + filename + " - Total record count from file: " + totalRecordCount + "/n" + " - Actual records: " + allRecords.size() + "/n" + " - Last processed: " + lastProcessed + "/n" + " - Current batch: " + currentBatch + "/n" + " - Records in current batch: " + recordsInCurrentBatch);
        
        // Process records ONE BY ONE from where the program was stopped
        for (int i = lastProcessed; i < allRecords.size(); i++) {
            String record = allRecords.get(i);
            
            // Check if current batch is full (100 records)
            if (recordsInCurrentBatch >= 100) {
                currentBatch++; //Move to next batch
                recordsInCurrentBatch = 0; //Reset count in the current next batch
            }
            
            // Add this record to the current batch for respective file type
            addRecordToBatch(filename, currentBatch, record);
            recordsInCurrentBatch++;
            
            // Update progress for this file type (position and batch number)
            saveProgress(filename, i + 1, currentBatch);
            
            System.out.println("Record " + recordsInCurrentBatch + " in batch");
        }
    }
    
    //Read the total record count from the first line of the log file
    static int getTotalRecordCount(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String firstLine = reader.readLine();
        reader.close();
        
        if (firstLine != null && firstLine.startsWith("Record Count:")) {
            // Extract the number from "Record Count:"
            String countStr = firstLine.substring("Record Count:".length()).trim();
            return Integer.parseInt(countStr);
        }
        
        return 0; // If no record count found
    }
    
    // Function adds one record to a batch file with ---RECORD--- header for easier record counting
    static void addRecordToBatch(String originalFile, int batchNumber, String record) throws Exception {
        String baseName = originalFile.replace(".log", "");
        String batchFile = String.format("%s-%04d.log", baseName, batchNumber);
        
        new File("batches").mkdir();
        String batchPath = "batches/" + batchFile;
        
        // Check if this is the first record in the batch
        File batch = new File(batchPath);
        if (!batch.exists()) {
            // Create new batch file with ---RECORD--- header
            PrintWriter writer = new PrintWriter(new FileWriter(batchPath));
            writer.println("Batch " + batchNumber + " for " + originalFile + " - Records:");
            writer.println("---RECORD---");
            writer.println(record);
            writer.close();
        } else {
            // Append to existing batch file with a record separator
            PrintWriter writer = new PrintWriter(new FileWriter(batchPath, true)); // true = append
            writer.println("---RECORD---");
            writer.println(record);
            writer.close();
        }
    }
    
    // Count how many records are in a batch file by counting ---RECORD--- separators
    static int getBatchSize(String originalFile, int batchNumber) {
        try {
            String baseName = originalFile.replace(".log", "");
            String batchFile = String.format("%s-%04d.log", baseName, batchNumber);
            String batchPath = "batches/" + batchFile;
            
            File batch = new File(batchPath);
            if (!batch.exists()) {
                return 0;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(batch));
            int count = 0;
            String line;

            //Counts each ---RECORD--- instance to determine how many records are in the batch.
            
            while ((line = reader.readLine()) != null) {
                if (line.equals("---RECORD---")) {
                    count++;
                }
            }
            reader.close();
            return count;
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Read all records from a log file
    static List<String> readRecords(File file) throws Exception {
        List<String> records = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        reader.readLine(); // Skip first line (Record Count: X)
        
        // Read the entire remaining content
        StringBuilder currentRecord = new StringBuilder();
        String line;
        int braceCount = 0;
        boolean inRecord = false;
        

        //Counts each JSON object.
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            
            // Count braces to track JSON object boundaries
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    braceCount++;
                    inRecord = true;
                } else if (c == '}') {
                    braceCount--;
                }
            }
            
            // Add line to current record
            if (inRecord) {
                currentRecord.append(line).append("\n");
            }
            
            // When braces balance back to 0, we've completed a JSON object
            if (inRecord && braceCount == 0) {
                records.add(currentRecord.toString().trim());
                currentRecord = new StringBuilder();
                inRecord = false;
            }
        }
        
        reader.close();
        return records;
    }
    
    // Save where we are in the file and which batch we're currently processing
    static void saveProgress(String filename, int recordsProcessed, int currentBatch) throws Exception {
        String progressFile = filename.replace(".log", "_progress.txt");
        
        PrintWriter writer = new PrintWriter(new FileWriter(progressFile));
        writer.println(recordsProcessed);  // Line 1: Position in the source file
        writer.println(currentBatch);      // Line 2: Current batch number
        writer.close();
    }
    
    // Read how many records we already processed from this file
    static int getLastProcessedRecord(String filename) {
        try {
            String progressFile = filename.replace(".log", "_progress.txt");
            File file = new File(progressFile);
            
            if (!file.exists()) {
                return 0; // Start from beginning
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            reader.close();
            
            return Integer.parseInt(line);
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Read what batch number is currently being worked on
    static int getCurrentBatchNumber(String filename) {
        try {
            String progressFile = filename.replace(".log", "_progress.txt");
            File file = new File(progressFile);
            
            if (!file.exists()) {
                return 1; // Start with batch 1
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine(); // Skip first line
            String line = reader.readLine();
            reader.close();
            
            return Integer.parseInt(line);
        } catch (Exception e) {
            return 1;
        }
    }
}