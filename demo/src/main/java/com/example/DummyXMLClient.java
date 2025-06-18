package com.example;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DummyXMLClient {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Sending XML messages to http://127.0.0.1:9091");
        
        int messageNumber = 1;
        
        while (true) {
            try {
                String xml = createXMLMessage(messageNumber);
                
                boolean success = sendXMLToServer(xml);
                
                if (success) {
                    System.out.println("Message #" + messageNumber + " sent successfully!");
                } else {
                    System.out.println("Failed to send message #" + messageNumber);
                }
                
                messageNumber++;
                
                // Wait 1 second before sending next message
                Thread.sleep(1000);
                
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                Thread.sleep(1000); 
            }
        }
    }
    
    static String createXMLMessage(int messageNumber) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");
        String currentDateTime = dateFormat.format(new Date());
        String currentDate = currentDateTime.substring(0, 10); // Just the date part
        
        String[] messageTypes = {"Information", "Trace", "Error", "Warning"};
        String[] processNames = {"scheduler.exe", "service.exe", "monitor.exe", "worker.exe"};
        
        // Pick a random type and process name
        String messageType = messageTypes[messageNumber % messageTypes.length]; //Randomized type picker
        String processName = processNames[messageNumber % processNames.length]; //Randomized process name picker
        int processId = 100000 + (messageNumber * 123); // ID generation
        
        // XML Format
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<Data>\n" +
                    "  <Method>\n" +
                    "    <Name>Order</Name>\n" +
                    "    <Type>Services</Type>\n" +
                    "    <Assembly>ServiceRepository, Version=1.0.0.1, Culture=neutral, PublicKeyToken=null</Assembly>\n" +
                    "  </Method>\n" +
                    "  <Process>\n" +
                    "    <Name>" + processName + "</Name>\n" +
                    "    <Id>" + processId + "</Id>\n" +
                    "    <Start>\n" +
                    "      <Epoch>1464709722277</Epoch>\n" +
                    "      <Date>" + currentDateTime + "</Date>\n" +
                    "    </Start>\n" +
                    "  </Process>\n" +
                    "  <Layer>DailyScheduler</Layer>\n" +
                    "  <Creation>\n" +
                    "    <Epoch>1464709728500</Epoch>\n" +
                    "    <Date>" + currentDateTime + "</Date>\n" +
                    "  </Creation>\n" +
                    "  <Type>" + messageType + "</Type>\n" +
                    "</Data>";
        
        return xml;
    }
    
    static boolean sendXMLToServer(String xml) {
        try {
            URL url = new URL("http://127.0.0.1:9091");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setDoOutput(true);
            
            // Send data
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(xml.getBytes());
            outputStream.flush();
            outputStream.close();
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                // Server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();
                
                return true;
            } else {
                System.out.println("Server returned error code: " + responseCode);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Couldn't connect to server: " + e.getMessage());
            return false;
        }
    }
}