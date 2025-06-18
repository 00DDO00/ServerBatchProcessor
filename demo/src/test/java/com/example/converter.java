package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;

import org.json.XML;

public class converter {
    
    public static void main(String[] args) throws Exception {
        // Create a simple HTTP server on port 9091
        HttpServer server = HttpServer.create(new InetSocketAddress(9091), 0);
        
        // When someone sends data to our server, run this code
        server.createContext("/", (HttpExchange exchange) -> {
            try {
                // Only accept POST requests (when someone sends us XML)
                if (exchange.getRequestMethod().equals("POST")) {
                    
                    // Step 1: Read the XML that was sent to us
                    String xmlData = readXMLFromRequest(exchange);
                    System.out.println("Got XML: " + xmlData);
                    
                    // Step 2: Convert XML to JSON (super simple way)
                    String jsonData = convertToJSON(xmlData);
                    
                    // Step 3: Find out what type this is (Information, Trace, etc.)
                    String type = findType(xmlData);
                    
                    // Step 4: Find out what date this is
                    String date = findDate(xmlData);
                    
                    // Step 5: Save to a file
                    saveToFile(type, date, jsonData);
                    
                    // Step 6: Tell the sender "OK, got it!"
                    String response = "XML received and saved!";
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    
                } else {
                    // If it's not a POST request, say "method not allowed"
                    exchange.sendResponseHeaders(405, 0);
                    exchange.getResponseBody().close();
                }
                
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        System.out.println("Server started! Listening on http://127.0.0.1:9091");
        server.start();
    }
    
    // Read the XML data that someone sent to our server
    static String readXMLFromRequest(HttpExchange exchange) throws IOException {
        StringBuilder xmlBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            xmlBuilder.append(line).append("\n");
        }
        reader.close();
        
        return xmlBuilder.toString();
    }
    
    // Convert XML to JSON in the simplest way possible
    static String convertToJSON(String xml) {
        String convertedJson = XML.toJSONObject(xml).toString();
        return convertedJson;
    }
    
    static String findType(String xml) {
        int start = xml.lastIndexOf("<Type>") + 6;  
        int end = xml.lastIndexOf("</Type>");

        return xml.substring(start, end).trim();
        
    }
    
    // Find the date from XML
    static String findDate(String xml) {
        // Look for <Date>2016-05-31T12:07:42...</Date> and extract just "2016-05-31"
      int dateStart = xml.indexOf("<Date>") + 6;  
      int dateEnd = xml.indexOf("</Date>");
      String fullDate = xml.substring(dateStart, dateEnd);
      String date = fullDate.substring(0, 10);
      return date;
        
    }
    
    // Save the JSON data to a file
    static void saveToFile(String type, String date, String jsonData) throws IOException {
        String filename = type + "-" + date + ".log";
        
        // Create logs folder if it doesn't exist
        File logsFolder = new File("logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
        
        String filepath = "logs/" + filename;
        File file = new File(filepath);
        
        // Count how many records are already in this file
        int recordCount = 0;
        String existingContent = "";
        
        // If file already exists, read it and count records
        if (file.exists()) {
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("Record Count: ")) {
                recordCount = Integer.parseInt(firstLine.replace("Record Count: ", "")); //Trimming the "Record Count: " part in order to be left with the number to be parsed.
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            existingContent = content.toString();
            reader.close();
        }
        
        // Increase record count by 1
        recordCount++;
        
        // Write everything back to the file
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println("Record Count: " + recordCount);
        writer.print(existingContent);
        writer.println(jsonData);
        writer.close();
        
        System.out.println("Saved to " + filename + " (Record #" + recordCount + ")");
    }
}