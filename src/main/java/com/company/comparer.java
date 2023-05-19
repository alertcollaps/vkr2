package com.company;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;

public class comparer {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static void main(String[] args) {

        System.out.println("\t\t\t\tOriginal\t\t\t\t\t\t\t\t\t\t\tModified");
        compare("1024px-Lenna.png", "result.png");

    }

    public static void compare(String pathImage1, String pathImage2){
        BufferedImage image1 = openFile.getImage(pathImage1);
        BufferedImage image2 = openFile.getImage(pathImage2);
        int x1 = image1.getHeight();
        int y1 = image1.getWidth();

        int x2 = image2.getHeight();
        int y2 = image2.getWidth();

        if (x1 != x2 && y1 != y2){
            throw new RuntimeException("Images dont size");
        }
        StringBuffer image1Str = new StringBuffer();
        StringBuffer image2Str = new StringBuffer();
        int count = 0;
        for (int k = 0; k < 3; k++){
            for (int y = 0; y < y1; y++){
                for (int x = 0; x <  x1; x++){
                    int color1 = getColour(new Color(image1.getRGB(x, y)), k);
                    int color2 = getColour(new Color(image2.getRGB(x, y)), k);


                    if (color1 == color2){
                        image1Str.append(ANSI_GREEN+ String.format("%8s", Integer.toBinaryString((byte)color1 & 0xFF)).replace(' ', '0') + ANSI_RESET + " ");
                        image2Str.append(ANSI_GREEN + String.format("%8s", Integer.toBinaryString((byte)color2 & 0xFF)).replace(' ', '0') + ANSI_RESET + " ");
                    } else {
                        image1Str.append(ANSI_RED+ String.format("%8s", Integer.toBinaryString((byte)color1 & 0xFF)).replace(' ', '0') + ANSI_RESET + " ");
                        image2Str.append(ANSI_RED + String.format("%8s", Integer.toBinaryString((byte)color2 & 0xFF)).replace(' ', '0') + ANSI_RESET + " ");
                    }


                    count++;

                    if (count % 5 == 0){
                        count = 0;
                        System.out.println(image1Str + "\t" + image2Str);
                        image2Str= new StringBuffer();
                        image1Str = new StringBuffer();
                    }
                }
                return;
            }
        }

    }

    public static int getColour(Color color, int k){
        return switch (k) {
            case 0 -> color.getBlue();
            case 1 -> color.getRed();
            case 2 -> color.getGreen();
            default -> 0;
        };
    }
}
