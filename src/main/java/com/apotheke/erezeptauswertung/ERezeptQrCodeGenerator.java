package com.apotheke.erezeptauswertung;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;

public class ERezeptQrCodeGenerator {

    public static BufferedImage generateQrCode(String taskId, String accessCode, int size) throws WriterException {
        String qrText = "Task/" + taskId + "/$accept?ac=" + accessCode;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, size, size);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static BufferedImage generateForRezept(ERezept rezept, int size) throws WriterException {
        return generateQrCode(rezept.getTaskId(), rezept.getAccessCode(), size);
    }
}
