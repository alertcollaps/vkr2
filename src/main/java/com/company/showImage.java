package com.company;
import java.awt.*;
import java.awt.image.BufferedImage;

import com.company.Encrypt.Utils;

public class showImage { //Менять переменные tableFirst и tableSecond
    private static int readCount;
    private static int twoBitsFirst = 0b00000001; //0
    private static int twoBitsSecond = 0b00000010; //3


    public static void main(String[] args) {
        showManager("result.png", "result.txt");
    }
    public static void showManager(String pathImage, String pathTextResult){
        BufferedImage image = openFile.getImage(pathImage);
        int width =image.getWidth();
        int heigth = image.getHeight();
        int[] imageArray = image.getRGB(0, 0, width, heigth, null, 0, width);
        int[] imageTrueArray = new int[imageArray.length * 3];
        for (int k =0; k < 3; k++){
            int indexTrue = k * imageArray.length;
            for (int i = 0; i < imageArray.length; i++){
                int color = getColor(k, new Color(imageArray[i]));
                imageTrueArray[indexTrue + i] = color;
            }
        }

        int[] imageIndexes = null;

        byte[] key = openFile.getText("key.txt");
        if (key != null && key.length != 0){
            imageIndexes = Utils.bytesToInt(Utils.hexStringToByteArray(new String(key)));
        }

        byte[] res = showImage(imageTrueArray, imageIndexes);

        openFile.setText(pathTextResult, res);
        System.out.printf("Изменения записаны в файл %s\n", pathTextResult);
        System.out.printf("Считано %d бит\n", readCount);

        
    }


    
    public static byte[] showImage(int[] image, int[] imageIndexes){

        int readBit = 1;
        int indexImage = 0;

        int k = 0;
        int len = image.length * 2;

       
        byte[] result = new byte[len];
        readCount = 0;
        len = image.length;
        if (imageIndexes != null){
            len = imageIndexes.length;
        }
        for (int i = 0; i < len; i++){
            int imageIndex = imageIndexes != null ? imageIndexes[indexImage] : indexImage;
            int currColor = checkBit(image[imageIndex]);
            indexImage++;
            
            
            if (validateDouble(currColor)){
                int bit = parseBit(currColor);
                result[i] = (byte) ((bit >> 1) & 1); // {0000 0011} -> And {0000 0001} & {0000 0001} (1)
                result[i+1] = (byte) (bit & 1); //And {0000 0011} & {0000 0001} (1) = 0000 0001
                i++;
                readCount = readCount + 2;
                continue;
            }
            int bit = parseBit(currColor);
            result[i] = (byte) bit;
            
            readCount++;
        }
        byte tempResult = 0;
        int lenRes = readCount/8;
        byte[] res = new byte[lenRes];
        int count = 0;

        for (int i = 0; i < lenRes; i++){
            for (int j = 0; j < 8; j++){

                tempResult = (byte) (tempResult | (result[count] << (7 - j)));
                count++;
            }
            res[i] = tempResult;
            tempResult = 0;
        }
        return res;
        
    }

    private static boolean validateDouble(int bit){
        if (bit == 5 || bit == 2){
            return true;
        }
        return false;
    }

    private static int parseBit(int bit){
        switch (bit){
            case 0:
            case 3:
            case 6:
                return 0;
            case 1:
            case 4:
            case 7:
                return 1;
            //Two bits
            case 2:
                return twoBitsFirst;
            case 5:
                return twoBitsSecond;
        }
    return 0;

    }

    public static int checkBit(int bit){
        return bit & 7;
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


}
