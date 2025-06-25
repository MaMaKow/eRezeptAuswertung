package com.apotheke.erezeptauswertung;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Mandelkow
 */
public class ERezeptAuswertung {

    public ERezeptAuswertung() {
        // Define your API endpoint
        //String apiUrl = "http://localhost:65123/api/taskCompiled?startDate=2023-01-01&endDate=2023-12-31";
        String apiUrl = "http://s1-4206193-pc:65123/api/taskCompiled?startDate=2024-01-01&endDate=2024-10-31";

        // Define output CSV file path
        String csvFile = "e-rezepte.csv";

        try {
            // Fetch data from API
            String jsonResponse = getApiResponse(apiUrl);
            //System.out.println(jsonResponse);
            // Write data to CSV
            writeDataToCSV(jsonResponse, csvFile);

            System.out.println("Data successfully written to " + csvFile);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String getApiResponse(String apiUrl) throws IOException {
        StringBuilder response = new StringBuilder();
        URL url = new URL(apiUrl);
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
            writer.write("taskId,taskStatus,date");
            writer.newLine();

            // Loop through each JSON object and extract fields
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //System.out.print(jsonObject.toString());
                // Extract fields (modify these based on the actual JSON structure)
                String taskId = jsonObject.getString("taskId");
                String date;
                try {
                    /**
                     * date = jsonObject.has("acceptTimestamp") ?
                     * jsonObject.getString("acceptTimestamp") :
                     * jsonObject.has("rejectTimestamp") ?
                     * jsonObject.getString("rejectTimestamp") : "N/A"; //
                     * Fallback if neither is present
                     *
                     */
                    date = jsonObject.getString("acceptTimestamp");
                } catch (JSONException e) {
                    System.out.println("Dieses Rezept hat keinen acceptTimestamp:");
                    System.out.println(jsonObject.toString());
                    continue;
                }
                Integer taskStatus = jsonObject.getInt("taskStatus");
                if (taskStatus.equals(800)) {
                    /**
                     * Das Rezept wurde gelöscht.
                     */
                    continue;
                }
                if (taskStatus.equals(200)) {
                    /**
                     * Das Rezept wurde zurückgegeben.
                     */
                    continue;
                }
                Float invoiceTotalGross = jsonObject.getFloat("invoiceTotalGross");
                if (0 >= invoiceTotalGross) {
                    /**
                     * Es ist nichts zu bezahlen. Entweder Rezept war abgelaufen
                     * und wurde vom Patienten gezahlt. Oder Privatrezept. Oder
                     * Grünes Rezept.
                     */
                    continue;
                }

                // Write data to CSV
                writer.write(String.format("%s,%d,%s,%s", taskId, taskStatus, date, jsonObject.toString()));
                writer.newLine();
            }
        }
    }
}
