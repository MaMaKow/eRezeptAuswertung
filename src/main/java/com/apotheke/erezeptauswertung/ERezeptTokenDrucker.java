package com.apotheke.erezeptauswertung;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Mandelkow
 */
public class ERezeptTokenDrucker {

    public static void main(String[] args) {
        ERezeptTokenDrucker eRezeptTokenDrucker = new ERezeptTokenDrucker();
    }

    public ERezeptTokenDrucker() {
        // Define your API endpoint
        LocalDateTime now = LocalDateTime.now();
        /**
         * Heute (today) wird definiert als 22:00:00 Uhr gestern Abend:
         */
        LocalDateTime today = now.withHour(0).withMinute(0).withSecond(0).withNano(0).minusHours(2);
        String todayString = today.format(DateTimeFormatter.ISO_DATE_TIME);
        ReadPropertyFile readPropertyFile = new ReadPropertyFile();
        String apiHost = readPropertyFile.getApiHost();
        String apiPort = readPropertyFile.getApiPort();
        String apiUrl = "http://" + apiHost + ":" + apiPort + "/api/taskcompiled?filter.StartDate=" + todayString;

        // Define output CSV file path
        String csvFile = "token.csv";

        try {
            // Fetch data from API
            String jsonResponse = getApiResponse(apiUrl);
            System.out.println("apiUrl: " + apiUrl);
            // Write data to CSV
            writeDataToCSV(jsonResponse, csvFile);

            System.out.println("Data successfully written to " + csvFile);
        } catch (IOException | URISyntaxException exception) {
            System.err.println("Error: " + exception.getMessage());
        }
    }

    private static String getApiResponse(String apiUrl) throws IOException, URISyntaxException {
        StringBuilder response = new StringBuilder();
        // Verwende URI zur Validierung und Umwandlung
        URI uri = new URI(apiUrl); // validiert Syntax
        URL url = uri.toURL();     // erstelle URL nur nach erfolgreicher URI-Validierung
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Setting HTTP method
        conn.setRequestMethod("GET");

        // Reading the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        // Check if request was successful
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to get data: HTTP error code " + conn.getResponseCode());
        }

        return response.toString();
    }

    private static void writeDataToCSV(String jsonData, String csvFile) throws IOException {
        // Open CSV file for writing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {

            // Parse JSON response
            JSONArray jsonArray = new JSONArray(jsonData);

            // Write CSV headers (modify based on API response structure)
            writer.write("taskId, accessCode, created, json");
            writer.newLine();

            // Loop through each JSON object and extract fields
            for (int i = 0; i < jsonArray.length(); i++) {
                //for (int i = 0; i < 20; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // Extract fields (modify these based on the actual JSON structure)
                String taskId = jsonObject.getString("taskId");
                String accessCode;
                try {
                    accessCode = jsonObject.getString("accessCode");

                } catch (JSONException exception) {
                    accessCode = "accessCode unbekannt";
                }
                LocalDateTime createdLocalDateTime = LocalDateTime.parse(jsonObject.getString("created"), DateTimeFormatter.ISO_DATE_TIME);
                Integer taskStatus = jsonObject.getInt("taskStatus");
                if (taskStatus.equals(800)) {
                    /**
                     * Das Rezept wurde gelöscht.
                     */
                    //continue;
                }
                if (taskStatus.equals(200)) {
                    /**
                     * Das Rezept wurde zurückgegeben.
                     */
                    //continue;
                }

                // Write data to CSV
                writer.write(String.format("%s, %s, %s, %s", taskId, accessCode, createdLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME), jsonObject.toString()));
                writer.newLine();
            }
        }
    }
}
