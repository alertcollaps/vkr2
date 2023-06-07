package com.company;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.bouncycastle.crypto.CryptoException;

import com.company.Encrypt.Gost;
import com.company.Encrypt.KeyGenerators;
import com.company.Encrypt.Utils;




public class temp {
    private static int w, h;
    private static int indImage;
    private static int[] tableFirst = {1, 2, 3};
    private static int[] tableSecond = {4, 5, 6};
    private static int twoBitsFirst = 0b00000001; //00
    private static int twoBitsSecond = 0b00000010; //11
    private static int indexImage = 0;

    
    public static void hideManager(String pathImage, String pathText, String pathResult){
        BufferedImage image = openFile.getImage(pathImage);
        byte[] text = openFile.getText(pathText);


        int width =image.getWidth();
        int heigth = image.getHeight();
        int textSize = text.length;
        Scanner in = new Scanner(System.in);
        System.out.print("Please enter your master password: ");
        String masterPassword = in.nextLine();
        System.out.println();
        
        
        

        System.out.printf("Размер сообщения равен: %d бит\n", textSize*8);

        System.out.printf("Размер контейнера равен: %d байт\n", width * heigth * 3);

        int[] imageArray = image.getRGB(0, 0, width, heigth, null, 0, width);
        int[] imageTrueArray = new int[imageArray.length * 3];
        for (int k =0; k < 3; k++){
            int indexTrue = k * imageArray.length;
            for (int i = 0; i < imageArray.length; i++){
                int color = getColor(k, new Color(imageArray[i]));
                imageTrueArray[indexTrue + i] = color;
            }
        }
        System.out.print("Choose mode:\n1. With genetic algorithm\n2. Simple\n3. Exit\n->");
        String mode = in.nextLine();
        System.out.println();
        Genetic genetic = new Genetic(text, imageTrueArray, image.getType(), width, heigth, null);
        byte[] salt = null;
        int[] imageIndexes = null;
        switch (mode){
            case "1":
                
                salt = KeyGenerators.generateSalt();
                genetic.setSeed(salt);
                byte[] AEADkey = KeyGenerators.getAEADKey(masterPassword.getBytes(), salt);
                try {
                    text = Gost.encrypt(AEADkey, text);
                } catch (RuntimeException ex){
                    ex.printStackTrace();
                } catch (CryptoException ex) {
                    ex.printStackTrace();
                }
                genetic.setData(text);
                genetic.run();
                imageIndexes = genetic.getOut();
                System.out.println("minimum: " + genetic.getMinimum());
                byte[] keyOut = genetic.getKeyOut();
                openFile.setText("keyOut.txt", Utils.bytesToHex(keyOut).getBytes());
                
                break;
            case "2":
                salt = genetic.getSeed();
                AEADkey = KeyGenerators.getAEADKey(masterPassword.getBytes(), salt);
                try {
                    text = Gost.encrypt(AEADkey, text);
                } catch (RuntimeException ex){
                    ex.printStackTrace();
                } catch (CryptoException ex) {
                    ex.printStackTrace();
                }
                
                break;
            case "3":
                in.close();
                return;
            default:
                System.out.println("Invalid value!");
        }
        
        
        
       
        
         

        
        
    

        //byte[] key = Utils.intTobytes(imageIndexes);
        
        //System.out.println("Key: " + Utils.bytesToHex(key));
        
        //openFile.setText("key.txt", Utils.bytesToHex(key).getBytes());

        
        in.close();
        System.out.println(hideImage(text, imageTrueArray, imageIndexes, image.getType(), width, heigth));

       
        for (int i = 0; i < imageArray.length; i++){ //resize
            int color = new Color(imageTrueArray[i + 1*imageArray.length], imageTrueArray[i + 2*imageArray.length], imageTrueArray[i]).getRGB();
            imageArray[i] = color;
        }

        BufferedImage img = new BufferedImage(width, heigth, image.getType());
        img.setRGB(0, 0, width, heigth, imageArray, 0, width);
        openFile.setImage(pathResult, img, "png");
        System.out.printf("Изменения записаны в файл %s\n", pathResult);
        System.out.printf("Задействовано %d ячеек\n", indexImage);
            


    }

    public static void setIndexImage(int indexImage) {
        temp.indexImage = indexImage;
    }

    public static int getIndexImage() {
        return indexImage;
    }

    public static int hideImage(byte[] text, int[] image, int[] imageIndexes, int imageType, int w, int h){

        int writeBit = 1;

        indexImage = 0;

        int k = 0;
        int len = image.length;
    

        byte[] textLine = getTextLine(text);
        int changeNumPix = 0;

        for (int i = 0; i < textLine.length - 1; i++){
            //indexImage = indImage; //add
    
           
            //
            if (k == 3){
                
            
                return changeNumPix;
            }
            int imageIndex = imageIndexes != null ? imageIndexes[indexImage] : indexImage;
            int pixel = image[imageIndex];


            int bit = checkBit(pixel); //Должен быть от 0 до 7
            
            int currBit = (textLine[i] << 1) | textLine[i+1]; //Должен быть от 0 до 3

            if (checkTable(bit, currBit)){
                int bit1 = generateColor(currBit);
                if (bit != bit1) changeNumPix++;
                setColor(imageIndex, image, bit1);
                i++;
                writeBit = writeBit + 2;
                if (++indexImage >= len){
                    break;
                }
                continue;
            }
            int bit1 = generateOneBit(bit, textLine[i]);
            if (bit != bit1) changeNumPix++;
            setColor(imageIndex, image, bit1);
            writeBit++;
            if (++indexImage >= len){
                break;
            }
        }

        return changeNumPix;
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
    public static void setColor(int indexImage, int[] image, int val){
        
        int currentColor = image[indexImage];
        currentColor = (currentColor & 0b11111000) | val;

        image[indexImage] = currentColor;
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
