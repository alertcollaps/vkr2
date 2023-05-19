package com.company;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Set;

public class hideImage { //twoBitsFirst/Second меняют раскладку
    private static int w, h, k;
    private static int[] tableFirst = {1, 2, 3};
    private static int[] tableSecond = {4, 5, 6};
    private static int twoBitsFirst = 0b00000001; //00
    private static int twoBitsSecond = 0b00000010; //11


    public static void hideManager(String pathImage, String pathText, String pathResult){
        BufferedImage image = openFile.getImage(pathImage);
        byte[] text = openFile.getText(pathText);


        int width =image.getWidth();
        int heigth = image.getHeight();
        int textSize = text.length;


        System.out.printf("Размер сообщения равен: %d бит\n", textSize*8);

        System.out.printf("Размер контейнера равен: %d байт\n", width * heigth * 3);

        hideImage(text, image, pathResult);


    }

    public static void hideImage(byte[] text, BufferedImage image, String pathResult){

        int writeBit = 1;

        int ww, hh, kk;
        int mask = 0b10000000;

        byte[] textLine = getTextLine(text);

        for (int i = 0; i < textLine.length - 1; i++){
            ww = w;
            hh = h;
            kk = k;

            int bit = checkBit(image); //Должен быть от 0 до 7
            if (bit == 10){
                openFile.setImage(pathResult, image, "png");
                System.out.printf("Изменения записаны в файл %s\n", pathResult);
                System.out.printf("Записано %d бит\n", writeBit);
                return;
            }
            int currBit = (textLine[i] << 1) | textLine[i+1]; //Должен быть от 0 до 3

            if (checkTable(bit, currBit)){
                bit = generateColor(currBit);
                setColor(ww, hh, kk, image, bit);
                i++;
                writeBit = writeBit + 2;
                continue;
            }
            bit = generateOneBit(bit, textLine[i]);
            setColor(ww, hh, kk, image, bit);
            writeBit++;
        }

        openFile.setImage(pathResult, image, "png");
        System.out.printf("Изменения записаны в файл %s\n", pathResult);
        System.out.printf("Записано %d бит\n", writeBit);
    }

    private static boolean checkTable(int bitImage, int bitText){

        if (bitText == twoBitsFirst){
            for (int i : tableFirst){
                if (bitImage == i){
                    return true;
                }
            }
        } else if (bitText == twoBitsSecond){
            for (int i : tableSecond){
                if (bitImage == i){
                    return true;
                }
            }
        }
        return false;
    }

    public static int generateOneBit(int bitImage, int bitText){

        if (bitText == 1){
            return generateUnit(bitImage);
        } else {
            return generateZero(bitImage);
        }

    }

    private static int generateUnit(int bitImage){

        if (bitImage == 7)
            return bitImage;
        if (bitImage == 0){
            return bitImage + 1;
        }
        if (bitImage == 1){
            return bitImage;
        }
        if (bitImage == 2){
            return bitImage - 1;
        }
        if (bitImage == 3){
            return bitImage + 1;
        }
        if (bitImage == 4){
            return bitImage;
        }
        if (bitImage == 5){
            return bitImage - 1;
        }
        if (bitImage == 6){
            return bitImage + 1;
        }
        return bitImage;
    }



    private static int generateZero(int bitImage){
        if (bitImage == 0)
            return bitImage;

        if (bitImage == 1){
            return bitImage - 1;
        }
        if (bitImage == 2){
            return bitImage + 1;
        }
        if (bitImage == 3){
            return bitImage;
        }
        if (bitImage == 4){
            return bitImage - 1;
        }
        if (bitImage == 5){
            return bitImage + 1;
        }
        if (bitImage == 6){
            return bitImage;
        }
        if (bitImage == 7){
            return bitImage - 1;
        }

        return bitImage;

    }

    public static int generateColor(int bitText){
        if (bitText == twoBitsFirst){
            return 2;
        }
        return 5;
    }

    public static byte[] getTextLine(byte[] text){
        int len = text.length *8;
        int mask = 0b10000000;
        byte[] res = new byte[len];
        int i = 0;
        for (byte tt : text){
            for (int j = 0; j < 8; j++) {
                int currBit = tt & mask;
                if (currBit > 0) {
                    currBit = 1;
                }
                res[i] = (byte) currBit;
                i++;
                mask >>>= 1;
            }
            mask = 0b10000000;
        }
        return res;
    }

    public static int checkBit(BufferedImage image){
        int width =image.getWidth();
        int heigth = image.getHeight();

        int count = 0;
        Color color = new Color(image.getRGB(w, h));
        int currentColor = getColor(k, color);
        currentColor = currentColor & 7;
        for (; k < 3; k++) {
            for (; h < heigth; h++) {
                for (; w < width; w++) {
                    if (count == 1){
                        return currentColor;
                    }
                    count++;
                }
                w = 0;
            }
            h = 0;
        }
        return 10;
    }
    private static int getColor(int k, Color color){
        switch (k){
            case 0:
                return color.getBlue();
            case 1:
                return color.getRed();
            case 2:
                return color.getGreen();
            default:
                return 0;
        }
    }
    public static void setColor(int w, int h, int k, BufferedImage image, int val){
        int heigthImage = image.getHeight();
        int widthImage = image.getWidth();

        for (; k < 3; k++) {
            for (; h < heigthImage; h++) {
                for (; w < widthImage; w++) {
                    Color color = new Color(image.getRGB(w,h), true);
                    int currentColor = getColor(k, color);
                    currentColor = (currentColor & 0b11111000) | val;

                    color = preSetColor(k, currentColor, color);
                    image.setRGB(w, h, color.getRGB());
                    return;
                }
                w = 0;
            }
            h = 0;
        }
    }

    private static Color preSetColor(int k, int val, Color color){
        switch (k){
            case 0:
                return new Color(color.getRed(), color.getGreen(), val, color.getAlpha());
            case 1:
                return new Color(val, color.getGreen(), color.getBlue(), color.getAlpha());
            case 2:
                return new Color(color.getRed(), val, color.getBlue(), color.getAlpha());
            default:
                return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }
}
