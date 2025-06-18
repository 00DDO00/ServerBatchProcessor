package com.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class xmlSender {
    public static void main(String[] args) {

      int messageNumber = 1; 
      while (true) {

       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");
        String currentDateTime = dateFormat.format(new Date());
        String currentDate = currentDateTime.substring(0, 10); // Just the date part
        
        // Create different types of messages
        String[] messageTypes = {"Information", "Trace", "Error", "Warning"};
        String[] processNames = {"scheduler.exe", "service.exe", "monitor.exe", "worker.exe"};
        
        // Pick a random type and process name
       String messageType = messageTypes[messageNumber % messageTypes.length];
        System.out.println("Message Type: " + messageType);

        String processName = processNames[messageNumber % processNames.length];
        System.out.println("Process Name: " + processName);

        int processId = 100000 + (messageNumber * 123); // Simple way to generate different IDs
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
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
        
        String serverAddress = "localhost";
        int port = 9091;

        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send XML content
            out.writeBytes("POST /convert HTTP/1.1\r\n");
            out.writeBytes("Host: " + serverAddress + "\r\n");
            out.writeBytes("Content-Type: application/xml\r\n");
            out.writeBytes("Content-Length: " + xmlContent.length() + "\r\n");
            out.writeBytes("\r\n");
            out.writeBytes(xmlContent);

            // Read response
            /*
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine);
            }
                */

            messageNumber++;

            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
            

        }
      }
    }
    
}
