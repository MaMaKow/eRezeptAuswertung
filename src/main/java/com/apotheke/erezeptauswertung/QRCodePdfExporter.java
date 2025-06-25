package com.apotheke.erezeptauswertung;

import com.google.zxing.WriterException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

public class QRCodePdfExporter {

    private static final int QR_SIZE = 150;
    private static final int MARGIN = 20;
    private static final int TEXT_HEIGHT = 50;
    private static final int TEXT_FONT_SIZE = 10;

    public static void exportRezepteToPdf(List<ERezept> rezepte, File outputFile) throws IOException, WriterException {
        try (PDDocument document = new PDDocument()) {

            // TTF-Schriftart laden
            File fontFile = new File("src/main/resources/fonts/DejaVuSans.ttf");
            PDType0Font font = PDType0Font.load(document, fontFile);

            int qrPerRow = 3;
            int qrPerCol = 3;
            int qrPerPage = qrPerRow * qrPerCol;

            int totalPages = (int) Math.ceil(rezepte.size() / (double) qrPerPage);

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(font, TEXT_FONT_SIZE);

                    int startIdx = pageIndex * qrPerPage;
                    int endIdx = Math.min(startIdx + qrPerPage, rezepte.size());

                    for (int i = startIdx; i < endIdx; i++) {
                        int indexOnPage = i - startIdx;
                        int row = indexOnPage / qrPerRow;
                        int col = indexOnPage % qrPerRow;

                        float x = MARGIN + col * (QR_SIZE + MARGIN + 50);
                        float y = PDRectangle.A4.getHeight() - MARGIN - (row + 1) * (QR_SIZE + TEXT_HEIGHT + MARGIN);

                        // QR-Code erzeugen
                        BufferedImage qrImage = ERezeptQrCodeGenerator.generateForRezept(rezepte.get(i), QR_SIZE);

                        // QR-Code in PDF einfügen
                        PDFBoxImageHelper.drawImage(contentStream, document, qrImage, x, y + TEXT_HEIGHT, QR_SIZE, QR_SIZE);

                        // Text darunter
                        float textY = y + TEXT_HEIGHT - TEXT_FONT_SIZE;
                        float textX = x;

                        String patient = rezepte.get(i).getPatientFullName();
                        String med = rezepte.get(i).getMedicationPrescriptionText();

                        List<String> textLines = wrapText(med, font, TEXT_FONT_SIZE, QR_SIZE + 40);

                        contentStream.beginText();
                        contentStream.newLineAtOffset(textX, textY);

                        contentStream.showText(patient);
                        contentStream.endText();

                        textY -= TEXT_FONT_SIZE + 2;

                        for (String line : textLines) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(textX, textY);
                            contentStream.showText(line);
                            contentStream.endText();
                            textY -= TEXT_FONT_SIZE + 2;
                        }
                    }
                }
            }

            document.save(outputFile);
        }
    }

    private static List<String> wrapText(String text, PDType0Font font, int fontSize, int maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String cleanedText = text.replaceAll("[\\r\\n]", " ");

        String[] words = cleanedText.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = (line.length() == 0) ? word : line + " " + word;
            float width = font.getStringWidth(testLine) / 1000 * fontSize;

            if (width > maxWidth) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
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
