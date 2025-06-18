package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;

import org.json.XML;

public class XMLToJSONServer {
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9091), 0);
        
        // Runs when data is sent to server
        server.createContext("/", (HttpExchange exchange) -> {
            try {
                if (exchange.getRequestMethod().equals("POST")) {
                    
                    //Read xml
                    String xmlData = readXMLFromRequest(exchange);
                    System.out.println("Got XML: " + xmlData);
                    
                    String jsonData = convertToJSON(xmlData);
                    
                    String type = findType(xmlData);
                    
                    String date = findDate(xmlData);
                    
                    saveToFile(type, date, jsonData);
                    
                    String response = "XML received and saved!";
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    
                } else {
                    // Only allow POST reqs.
                    exchange.sendResponseHeaders(405, 0);
                    exchange.getResponseBody().close();
                }
                
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        System.out.println("Listening on http://127.0.0.1:9091");
        server.start();
    }
    
    // Read the XML data
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
    
    // Find the Type from XML (Information, Trace, Error, etc.)
    static String findType(String xml) {
        // Look for <Type>xx</Type> and extract "xx"
        //lastIndexOf is used to get the desired data type as it is the last <Type> occurence in the file. If indexOf was used, it would return the first <Type> which is not the desired data type.
        int start = xml.lastIndexOf("<Type>") + 6;  // +6 because "<Type>" is 6 characters
        int end = xml.lastIndexOf("</Type>");
        
        if (start > 5 && end > start) {
            return xml.substring(start, end).trim();
        }
        
        return "Unknown"; // fallback
    }
    
    // Find the date from XML
    static String findDate(String xml) {
        // Look for <Date>xxx...</Date> and extract "2025-06-15"
        //indexOf is used to get the desired date as it is the first <Date> occurence in the file. If lastIndexOf was used, it would return the last <Date> which is not the desired date.
        int start = xml.indexOf("<Date>") + 6;  // +6 because "<Date>" 
        int end = xml.indexOf("</Date>");
        
        if (start > 5 && end > start) {
            String fullDate = xml.substring(start, end).trim();
            // Extract just the date part (first 10 characters: YYYY-MM-DD)
            if (fullDate.length() >= 10) {
                return fullDate.substring(0, 10);
            }
        }
        
        return "2025-06-12"; // fallback 
    }
    
    // Save the JSON data to a file
    static void saveToFile(String type, String date, String jsonData) throws IOException {
        
        String filename = type + "-" + date + ".log";
        
        // If no logs folder, create
        File logsFolder = new File("logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
        
        String filepath = "logs/" + filename;
        File file = new File(filepath);
        
        // Count how many records are already in this file
        int recordCount = 0;
        String existingContent = "";
        
        // If file exists, read and count records
        if (file.exists()) {
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("Record Count: ")) {
                recordCount = Integer.parseInt(firstLine.replace("Record Count: ", ""));
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            existingContent = content.toString();
            reader.close();
        }
        
        recordCount++;
        
        //Update file
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println("Record Count: " + recordCount);
        writer.print(existingContent);
        writer.println(jsonData);
        writer.close();
        
        System.out.println("Saved to " + filename + " (Record #" + recordCount + ")");
    }
}