package com.example;

import java.io.*;
import java.util.*;

public class BatchProcessor {
    
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
    
    // Process one log file
    static void processFile(File file) throws Exception {
        String filename = file.getName();
        
        // How many records did we already process?
        int processed = getProcessedCount(filename);
        
        // Read all records from file
        List<String> allRecords = readRecords(file);
        
        // Get only new records (skip the ones we already processed)
        List<String> newRecords = new ArrayList<>();
        for (int i = processed; i < allRecords.size(); i++) {
            newRecords.add(allRecords.get(i));
        }
        
        if (newRecords.size() == 0) {
            return; // No new records
        }
        
        System.out.println("Found " + newRecords.size() + " new records in " + filename);
        
        // Wait until we have at least 100 records
        if (newRecords.size() < 100) {
            System.out.println("Waiting for more records...");
            return;
        }
        
        // Group records into batches of 100
        int batchNumber = getBatchNumber(filename);
        List<String> batch = new ArrayList<>();
        
        for (String record : newRecords) {
            batch.add(record);
            
            // When we have 100 records, save them
            if (batch.size() == 100) {
                saveBatch(filename, batchNumber, batch);
                processed += 100;
                batchNumber++;
                batch.clear();
            }
        }
        
        // Save progress
        saveProgress(filename, processed, batchNumber);
    }
    
    // Read all records from a log file
    static List<String> readRecords(File file) throws Exception {
        List<String> records = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        reader.readLine(); // Skip first line (Record Count: X)
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                records.add(line.trim());
            }
        }
        reader.close();
        return records;
    }
    
    // Save 100 records to a batch file
    static void saveBatch(String originalFile, int batchNumber, List<String> records) throws Exception {
        // Create batch filename: Information-2025-06-12-0001.log
        String baseName = originalFile.replace(".log", "");
        String batchFile = String.format("%s-%04d.log", baseName, batchNumber);
        // Create batches folder if it doesn't exist
        new File("batches").mkdir();
        
        PrintWriter writer = new PrintWriter(new FileWriter("batches/" + batchFile));
        writer.println("Batch " + batchNumber + " - " + records.size() + " records");
        
        for (String record : records) {
            writer.println(record);
        }
        writer.close();
        
        System.out.println("Created: " + batchFile);
    }
    
    // Save our progress to a simple text file
    static void saveProgress(String filename, int processed, int nextBatch) throws Exception {
        String progressFile = filename.replace(".log", "_progress.txt");
        new File("progress").mkdir();
        PrintWriter writer = new PrintWriter(new FileWriter("progress/" + progressFile));
        writer.println(processed);    // How many records processed
        writer.println(nextBatch);   // Next batch number to use
        writer.close();
    }
    
    // Read how many records we already processed
    static int getProcessedCount(String filename) {
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
            return 0; // If error, start from beginning
        }
    }
    
    // Read what batch number to use next
    static int getBatchNumber(String filename) {
        try {
            String progressFile = filename.replace(".log", "_progress.txt");
            File file = new File(progressFile);
            
            if (!file.exists()) {
                return 1; // Start with batch 1
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine(); // Skip first line (processed count)
            String line = reader.readLine(); // Read batch number
            reader.close();
            
            return Integer.parseInt(line);
        } catch (Exception e) {
            return 1; // If error, start with batch 1
        }
    }
}