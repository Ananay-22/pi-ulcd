package com.nana;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

public final class uLCDInterface {
    static {
        System.loadLibrary("pi-ulcd-jni");
    }

    private static final synchronized native boolean internalWriteImageToULCD(int sectorStart, short[][] image);

    public static final boolean writeImageToULCD(int sectorStart, short[][] image) {
        if (sectorStart < 0 || image == null || image.length <= 0 || image[0].length <= 0) return false;
        int width = image[0].length;
        for (int i = 0; i < image.length; i++) {
            if (image[i].length != width) return false;
        }
        return internalWriteImageToULCD(sectorStart, image);
        // return internalWriteImageToULCD(0, null);
    }

    private static interface ImageConverter {
        public int getIntColor(int row, int col);
    }

    private static final <T> short[][] convertImage(int width, int height, T imgSrc, ImageConverter converter) {
        short[][] result = new short[height][width];

      for (int row = 0; row < height; row++) {
         for (int col = 0; col < width; col++) {
            int imgPixel = converter.getIntColor(row, col);
            result[row][col] = (short) (((imgPixel & 0xF80000) >> 8) 
                                      | ((imgPixel & 0x00FC00) >> 5) 
                                      | ((imgPixel & 0x0000F8) >> 3)
             );
         }
      }
      return result;
    }

    public static final short[][] imageToRAW(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        return convertImage(width, height, img, img::getRGB);
    }

    public static final short[][] imageToRAW(WritableImage img) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        return convertImage(width, height, img, img.getPixelReader()::getArgb);
    }

    public static final int SECTOR_SIZE_BYTES = 512;
    public static       int baseSectorAddress = 0x32;
    public static final int calculateSectorSize(short[][] image) {
        if (image == null || image.length <= 0 || image[0].length <= 0) return -1;
        int width = image[0].length;
        for (int i = 0; i < image.length; i++) {
            if (image[i].length != width) return -1;
        }
        return (int) Math.ceil(2.0 * image.length * image[0].length / 512);
    }

    public static void main(String[] args) throws IOException {
        int sectorStart = baseSectorAddress;
        for (int i = 0; i < args.length; i++) {
            BufferedImage image = ImageIO.read(new File(args[i]));
            short[][] rgb565Image = imageToRAW(image);
            boolean writeImage = writeImageToULCD(sectorStart, rgb565Image);
            System.out.printf("Writing image %s to sector %x. %s.\n", args[i], sectorStart, writeImage ? "Success" : "Failed");
            sectorStart += calculateSectorSize(rgb565Image);
        }
    }
}