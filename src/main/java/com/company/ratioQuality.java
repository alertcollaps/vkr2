package com.company;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ratioQuality {
    static int max = 0;
    static double sum = 0;
    public static void main(String[] args) {
        qualityManager();
    }

    public static void qualityManager(){
        String pathImageOriginal = "1024px-Lenna.png";
        String pathImageChange = "result.png";

        BufferedImage imageOriginal = openFile.getImage(pathImageOriginal);
        BufferedImage imageChange = openFile.getImage(pathImageChange);
        double nmse = NMSE(imageOriginal, imageChange);
        System.out.println("MSE: " + MSE(imageOriginal, imageChange));
        System.out.println("NMSE: " + nmse);
        System.out.println("SNR: " + 1/nmse);
        System.out.println("PSNR: " + PSNR(imageOriginal, imageChange));
    }

    public static double MSE(BufferedImage imageOriginal, BufferedImage imageChange){
        double mse = 0;
        int x = imageOriginal.getWidth();
        int y = imageOriginal.getHeight();

        double tempSum = 0;
        for (int h  = 0; h < y; h++){
            for (int w = 0; w < x; w++){
                Color colorOriginal = new Color(imageOriginal.getRGB(w,h));
                Color colorChange = new Color(imageChange.getRGB(w,h));

                tempSum = tempSum + Math.pow(colorOriginal.getGreen() - colorChange.getGreen(), 2);
                tempSum = tempSum + Math.pow(colorOriginal.getRed() - colorChange.getRed(), 2);
                tempSum = tempSum + Math.pow(colorOriginal.getBlue() - colorChange.getBlue(), 2);
            }
        }
        mse = tempSum / (3*x*y);
        return mse;
    }

    public static double NMSE(BufferedImage imageOriginal, BufferedImage imageChange){
        double mse = 0;
        int x = imageOriginal.getWidth();
        int y = imageOriginal.getHeight();

        double CSSum = 0;
        double CSum = 0;
        for (int h  = 0; h < y; h++){
            for (int w = 0; w < x; w++){
                Color colorOriginal = new Color(imageOriginal.getRGB(w,h));
                Color colorChange = new Color(imageChange.getRGB(w,h));

                int clrG, clrR, clrB;
                clrB = colorOriginal.getBlue();
                clrR = colorOriginal.getRed();
                clrG = colorOriginal.getGreen();

                if (max < clrB){
                    max = clrB;
                }
                if (max < clrG){
                    max = clrG;
                }
                if (max < clrR){
                    max = clrR;
                }

                CSum = CSum + Math.pow(colorOriginal.getGreen(), 2);
                CSum = CSum + Math.pow(colorOriginal.getRed(), 2);
                CSum = CSum + Math.pow(colorOriginal.getBlue(), 2);

                CSSum = CSSum + Math.pow(colorOriginal.getGreen() - colorChange.getGreen(), 2);
                CSSum = CSSum + Math.pow(colorOriginal.getRed() - colorChange.getRed(), 2);
                CSSum = CSSum + Math.pow(colorOriginal.getBlue() - colorChange.getBlue(), 2);
            }
            sum = CSSum;
        }

        return CSSum/CSum;
    }
    public static String PSNR(BufferedImage imageOriginal, BufferedImage imageChange){
        int x = imageOriginal.getWidth();
        int y = imageOriginal.getHeight();
        BigDecimal result = new BigDecimal(x);
        result = result.multiply(new BigDecimal(y));
        result = result.multiply(new BigDecimal(max*max));
        result = result.divide(new BigDecimal(sum),2, RoundingMode.HALF_UP);

        return result.toString();
    }

}
