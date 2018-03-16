package com.ecjtu.qrcode;


import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.Map;

/**
 * This object renders a QR Code as a BitMatrix 2D array of greyscale values.
 * @description 因为该实现存在很大的白边（padding），因此将白边去掉
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRCodeWriter {

    private static final int QUIET_ZONE_SIZE = 4;

    public BitMatrix encode(String contents, int width, int height) throws WriterException {

        return encode(contents, width, height, null);
    }

    public BitMatrix encode(String contents, int width, int height, Map<EncodeHintType, ?> hints)
            throws WriterException {

        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        }

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' + height);
        }

        ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
        int quietZone = QUIET_ZONE_SIZE;
        if (hints != null) {
            ErrorCorrectionLevel requestedECLevel = (ErrorCorrectionLevel) hints.get(EncodeHintType.ERROR_CORRECTION);
            if (requestedECLevel != null) {
                errorCorrectionLevel = requestedECLevel;
            }
            Integer quietZoneInt = (Integer) hints.get(EncodeHintType.MARGIN);
            if (quietZoneInt != null) {
                quietZone = quietZoneInt;
            }
        }

        QRCode code = Encoder.encode(contents, errorCorrectionLevel, hints);
        return renderResult(code, width, height, quietZone);
    }

    // Note that the input matrix uses 0 == white, 1 == black, while the output
    // matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    // 修改如下代码
    private static BitMatrix renderResult(QRCode code, int width, int height, int quietZone) {
        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth;
        int qrHeight = inputHeight;
        int outputWidth = Math.max(width, qrWidth);
        int outputHeight = Math.max(height, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        // Padding includes both the quiet zone and the extra white pixels to
        // accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33
        // including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of
        // 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.

        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

        if (leftPadding >= 0) {
            outputWidth = outputWidth - 2 * leftPadding;
            leftPadding = 0;
        }
        if (topPadding >= 0) {
            outputHeight = outputHeight - 2 * topPadding;
            topPadding = 0;
        }

        BitMatrix output = new BitMatrix(outputWidth, outputHeight);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            // Write the contents of this row of the barcode
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    output.setRegion(outputX, outputY, multiple, multiple);
                }
            }
        }

        return output;
    }
}
