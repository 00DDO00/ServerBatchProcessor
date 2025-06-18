package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.json.XML;
import org.json.JSONException;

public class App {

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9091), 0);
            server.createContext("/convert", (HttpExchange exchange) -> {
                if ("POST".equals(exchange.getRequestMethod())) {
                    InputStream inputStream = exchange.getRequestBody();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String xmlContent = reader.lines().collect(Collectors.joining("\n"));

                   

                    int typeStart = xmlContent.lastIndexOf("<Type>") + 6;  
                    int typeEnd = xmlContent.lastIndexOf("</Type>");
                    String type = xmlContent.substring(typeStart, typeEnd);

                    System.out.println("herType");

                    int dateStart = xmlContent.indexOf("<Date>") + 6;  
                    int dateEnd = xmlContent.indexOf("</Date>");
                    String fullDate = xmlContent.substring(dateStart, dateEnd);
                    String date = fullDate.substring(0, 10);

                    System.out.println("hereDate");

                    String jsonContent = convertXmlToJson(xmlContent);


                    String filename = type + "-" + date + ".log";

                    System.out.println("Filename: " + filename);

                    try {
                    File jsonFile = new File(filename);
                    if (jsonFile.createNewFile()) {
                        System.out.println("File created: " + jsonFile.getName());
                    } else {
                        System.out.println("File already exists.");
                    }
                    } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    }

                    try {
                    FileWriter myWriter = new FileWriter(filename, true);
                    myWriter.append(jsonContent);
                    myWriter.close();
                    System.out.println("Successfully wrote to the file.");
                    } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                    }

                    exchange.sendResponseHeaders(200, jsonContent.length());
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(jsonContent.getBytes());
                    outputStream.close();


                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            });
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Server started on port 9091");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertXmlToJson(String xmlContent) {
        String convertedJson = XML.toJSONObject(xmlContent).toString();
        return convertedJson;
       } 
}
