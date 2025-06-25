/*
 * Copyright (C) 2025 S3000
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author S3000
 */
package com.apotheke.erezeptauswertung;

import com.google.zxing.WriterException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ERezeptTokenGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ERezeptTokenGUI();
        });
    }
    private JFrame loadingFrame;
    private ReadPropertyFile readPropertyFile;

    public ERezeptTokenGUI() {
        readPropertyFile = new ReadPropertyFile();
        createAndShowLoadingFrame();
        fetchDataAsync();
    }

    private void createAndShowLoadingFrame() {
        loadingFrame = new JFrame("Lade Daten...");
        loadingFrame.setSize(300, 100);
        loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loadingFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Daten werden abgerufen..."));
        loadingFrame.add(panel);

        loadingFrame.setVisible(true);
        loadingFrame.setAlwaysOnTop(true);
    }

    private void fetchDataAsync() {
        new SwingWorker<List<ERezept>, Void>() {
            @Override
            protected List<ERezept> doInBackground() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime today = now.withHour(0).withMinute(0).withSecond(0).withNano(0).minusHours(2);
                String todayString = today.format(DateTimeFormatter.ISO_DATE_TIME);

                String apiHost = readPropertyFile.getApiHost();
                String apiPort = readPropertyFile.getApiPort();
                String apiUrl = "http://" + apiHost + ":" + apiPort + "/api/taskcompiled?filter.StartDate=" + todayString;
                return fetchData(apiUrl);
            }

            @Override
            protected void done() {
                try {
                    List<ERezept> tokenList = get();
                    loadingFrame.dispose(); // <--- Ladefenster schließen
                    showMainFrame(tokenList);
                } catch (InterruptedException | ExecutionException exception) {
                    loadingFrame.dispose(); // <--- Auch im Fehlerfall schließen
                    showErrorDialog("Fehler beim Abrufen der Daten: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private List<ERezept> fetchData(String apiUrl) throws IOException, URISyntaxException, JSONException {
        String jsonResponse = getApiResponse(apiUrl);
        return parseJsonData(jsonResponse);
    }

    private void showMainFrame(List<ERezept> rezeptList) {
        JFrame mainFrame = new JFrame("eRezept Übersicht");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 600);
        mainFrame.setLocationRelativeTo(null);

        String[] columnNames = {"Patient", "Versicherung", "Verordnung", "Erstellt am"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (ERezept rezept : rezeptList) {
            ZonedDateTime berlinTime = rezept.getCreated().withZoneSameInstant(ZoneId.of("Europe/Berlin"));
            String createdFormatted = berlinTime.format(formatter);
            model.addRow(new Object[]{
                rezept.getPatientFullName(),
                rezept.getInsuranceName(),
                rezept.getMedicationPrescriptionText(),
                createdFormatted
            });
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Patient
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Versicherung
        table.getColumnModel().getColumn(2).setPreferredWidth(400); // Verordnung
        table.getColumnModel().getColumn(3).setPreferredWidth(130); // Erstellt am

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.rowAtPoint(evt.getPoint());
                    showQRTokenDialog(rezeptList.get(row)); // oder rezept.getOriginalJson()
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        mainFrame.add(scrollPane);
        mainFrame.setVisible(true);
    }

    private void showQRTokenDialog(ERezept rezept) {
        JTextArea textArea = new JTextArea(25, 80);
        textArea.setText("Token");
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        BufferedImage qrImage;
        try {
            qrImage = ERezeptQrCodeGenerator.generateForRezept(rezept, 300);
            JLabel qrLabel = new JLabel(new ImageIcon(qrImage));

            JOptionPane.showMessageDialog(
                    null,
                    qrLabel,
                    "QR-Code für Rezept",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (WriterException ex) {
            textArea.setText("Fehler bei der Erstellung des QR Codes");
            Logger.getLogger(ERezeptTokenGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Fehler",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static String getApiResponse(String apiUrl) throws IOException, URISyntaxException {
        StringBuilder response = new StringBuilder();
        URI uri = new URI(apiUrl);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP Fehlercode: " + conn.getResponseCode());
        }

        return response.toString();
    }

    private static List<ERezept> parseJsonData(String jsonData) throws JSONException {
        List<ERezept> rezeptList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonData);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            int taskStatus = jsonObject.optInt("taskStatus", -1);
            if (taskStatus != 200 && taskStatus != 800) {
                ERezept rezept = new ERezept(jsonObject.toString());
                rezeptList.add(rezept);
            }
        }

        // Sortiere nach erstellt (neueste zuerst)
        rezeptList.sort((a, b) -> b.getCreated().compareTo(a.getCreated()));

        return rezeptList;
    }

    private static String getStatusText(int statusCode) {
        switch (statusCode) {
            case 100:
                return "Neu";
            case 300:
                return "In Bearbeitung";
            case 400:
                return "Abgeschlossen";
            case 406:
                return "Abgegeben";
            case 500:
                return "Fehler";
            default:
                return "Unbekannt (" + statusCode + ")";
        }
    }

}
