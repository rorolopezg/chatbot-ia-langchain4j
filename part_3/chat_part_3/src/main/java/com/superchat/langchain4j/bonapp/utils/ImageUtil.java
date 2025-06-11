package com.superchat.langchain4j.bonapp.utils;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtil {
    public static byte[] resizeImage(byte[] originalImageBytes, int targetWidth) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(originalImageBytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Thumbnails.of(bis)
                .width(targetWidth)
                .outputFormat("jpeg")
                .toOutputStream(bos);

        return bos.toByteArray();
    }

    public static byte[] resizeImage2(byte[] originalImageBytes, int targetWidth) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int targetHeight = (targetWidth * originalHeight) / originalWidth;

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        // Comprimir con calidad ajustada
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.5f); // Calidad entre 0 (baja) y 1 (alta)

        writer.write(null, new IIOImage(resizedImage, null, null), param);
        writer.dispose();
        ios.close();

        return outputStream.toByteArray();
    }

    public static byte[] combineImagesHorizontally(byte[] image1Bytes, byte[] image2Bytes, int targetHeight) throws IOException {
        BufferedImage img1 = ImageIO.read(new ByteArrayInputStream(image1Bytes));
        BufferedImage img2 = ImageIO.read(new ByteArrayInputStream(image2Bytes));

        // Redimensiona ambas imágenes a la misma altura
        BufferedImage resized1 = resizeToHeight(img1, targetHeight);
        BufferedImage resized2 = resizeToHeight(img2, targetHeight);

        int width = resized1.getWidth() + resized2.getWidth();
        int height = targetHeight;

        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = combined.getGraphics();
        g.drawImage(resized1, 0, 0, null);
        g.drawImage(resized2, resized1.getWidth(), 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(combined, "jpeg", baos);
        return baos.toByteArray();
    }

    private static BufferedImage resizeToHeight(BufferedImage originalImage, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int targetWidth = (targetHeight * originalWidth) / originalHeight;

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return resized;
    }

}