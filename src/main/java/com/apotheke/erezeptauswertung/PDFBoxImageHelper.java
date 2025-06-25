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

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

public class PDFBoxImageHelper {

    public static void drawImage(PDPageContentStream contentStream, PDDocument document, BufferedImage image, float x, float y, float width, float height) throws IOException {
        var pdImage = LosslessFactory.createFromImage(document, image);
        contentStream.drawImage(pdImage, x, y, width, height);
    }
}
