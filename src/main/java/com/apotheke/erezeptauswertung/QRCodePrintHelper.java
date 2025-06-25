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
package com.apotheke.erezeptauswertung;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.List;

public class QRCodePrintHelper implements Printable {

    private final List<ERezept> rezepte;
    private final int qrSize = 150;
    private final int margin = 20;
    private final int textHeight = 40;  // Platz für 2 Textzeilen unter jedem QR

    public QRCodePrintHelper(List<ERezept> rezepte) {
        this.rezepte = rezepte;
    }

    public static void printRezepte(List<ERezept> rezepte) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new QRCodePrintHelper(rezepte));

        if (job.printDialog()) {
            job.print();
        }
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        int qrPerRow = 3;
        int qrPerCol = 3;
        int qrPerPage = qrPerRow * qrPerCol;

        int totalPages = (int) Math.ceil(rezepte.size() / (double) qrPerPage);
        if (pageIndex >= totalPages) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));

        int startIdx = pageIndex * qrPerPage;
        int endIdx = Math.min(startIdx + qrPerPage, rezepte.size());

        for (int i = startIdx; i < endIdx; i++) {
            int indexOnPage = i - startIdx;
            int row = indexOnPage / qrPerRow;
            int col = indexOnPage % qrPerRow;

            int x = col * (qrSize + margin + 50);  // Extra Platz für Text
            int y = row * (qrSize + textHeight + margin);

            ERezept rezept = rezepte.get(i);

            // QR-Code erzeugen
            BufferedImage qrImage;
            try {
                qrImage = ERezeptQrCodeGenerator.generateForRezept(rezept, qrSize);
            } catch (Exception ex) {
                throw new PrinterException("QR-Code konnte nicht erzeugt werden: " + ex.getMessage());
            }

            g2d.drawImage(qrImage, x, y, qrSize, qrSize, null);

            // Text darunter
            int textX = x;

            String patientName = rezept.getPatientFullName();
            String arzneimittel = rezept.getMedicationPrescriptionText();

            int textYPos = y + qrSize + 12;

            // Erste Zeile: Patient
            g2d.drawString(patientName, textX, textYPos);
            textYPos += 12;

            // Danach: Arzneimittelbeschreibung mit Umbruch
            List<String> wrappedLines = wrapText(g2d, arzneimittel, qrSize);
            int maxLines = 4;
            int lineCount = 0;

            for (String line : wrappedLines) {
                if (lineCount >= maxLines) {
                    g2d.drawString("...", textX, textYPos);
                    break;
                }
                g2d.drawString(line, textX, textYPos);
                textYPos += 12;
                lineCount++;
            }
        }

        return PAGE_EXISTS;
    }

    private List<String> wrapText(Graphics2D g2d, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        FontMetrics fm = g2d.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            int lineWidth = fm.stringWidth(testLine);

            if (lineWidth > maxWidth) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    // Wort allein schon zu lang → trotzdem übernehmen (kein harter Cut)
                    lines.add(word);
                    line = new StringBuilder();
                }
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines;
    }
}
