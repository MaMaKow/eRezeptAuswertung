/*
 * Copyright (C) 2024 Mandelkow
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
package com.apotheke.erezeptauswertung;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author S3000
 */
public class MissingTaskFinder {

    public static void main(String[] args) {
        MissingTaskFinder finder = new MissingTaskFinder();

        // Paths to your files
        String taskIdsFilePath = "e-rezepte.csv"; // e.g., "e-rezepte.csv"
        String rezeptIdsFilePath = "N:\\Rezeptsuche_07112024.csv"; // The second file

        try {
            // Load task IDs from the first file into a set
            Set<String> taskIdsSet = finder.loadTaskIds(rezeptIdsFilePath);

            // Find missing Rezept-IDs from the second file and display in GUI
            List<String> missingRezeptIds = finder.findMissingRezeptIds(taskIdsSet, taskIdsFilePath);
            finder.displayMissingRezeptIdsInGUI(missingRezeptIds);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private Set<String> loadTaskIds(String filePath) throws IOException {
        Set<String> taskIds = new HashSet<>();

        // Read the first file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                //System.out.println(Arrays.toString(values));
                String taskId = values[5].trim(); // (e)Rezept-ID is the 6th column (index 5)
                taskIds.add(taskId);
            }
        }

        return taskIds;
    }

    private List<String> findMissingRezeptIds(Set<String> taskIdsSet, String filePath) throws IOException {
        List<String> missingRezeptIds = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String rezeptId = values[0].trim();

                // Check if Rezept-ID is missing in taskIdsSet
                if (!taskIdsSet.contains(rezeptId)) {
                    missingRezeptIds.add(rezeptId);
                }
            }
        }

        return missingRezeptIds;
    }

    private void displayMissingRezeptIdsInGUI(List<String> missingRezeptIds) {
        // Create a JFrame for displaying the missing Rezept-IDs
        JFrame frame = new JFrame("Missing Rezept-IDs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);

        // Convert List to an array for JList
        String[] missingIdsArray = missingRezeptIds.toArray(new String[0]);

        // Create a JList and add it to a JScrollPane for scrolling
        JList<String> list = new JList<>(missingIdsArray);
        JScrollPane scrollPane = new JScrollPane(list);

        // Add the JScrollPane to the frame's content pane
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Display the frame
        frame.setVisible(true);
    }
}
