package com.company;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;

import com.company.Encrypt.Gost;
import com.company.Encrypt.KeyGenerators;
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
        /* 
        byte[] key = openFile.getText("key.txt");
        if (key != null && key.length != 0){
            imageIndexes = Utils.bytesToInt(Utils.hexStringToByteArray(new String(key)));
        }
        */
        Scanner in = new Scanner(System.in);
        System.out.print("Please enter your master password: ");
        String masterPassword = in.nextLine();
        

        System.out.print("Choose mode:\n1. With genetic algorithm\n2. Simple\n3. Exit\n->");
        String mode = in.nextLine();
        System.out.println();
        
        Genetic genetic = new Genetic(new byte[]{}, imageTrueArray, image.getType(), width, heigth, null);
        byte[] salt = null;
        int[] sequence = null;
        
        int cc = 0;
        switch (mode){
            case "1":
                
                byte[] keyGen = Utils.hexStringToByteArray(new String(openFile.getText("keyOut.txt")));
                byte[] sotes = new byte[4];
                System.arraycopy(keyGen, 0, sotes, 0, 4);
                cc = bytesToInt(sotes);
                sequence = genetic.reSequence(keyGen);
                System.out.println("Hashcode check: " + Arrays.hashCode(sequence));
                salt = genetic.getSeed();
                break;
            case "2":
                salt = genetic.getSeed();
                System.out.print("Please enter count of sotes: ");
                String count = in.nextLine();
                cc = Integer.parseInt(count);
                sequence = new int[cc];
                
                for (int i = 0; i < cc; i++){
                    sequence[i] = i;
                }
                
                break;
            case "3":
                in.close();
                return;
            default:
                System.out.println("Invalid value!");
        }

        imageIndexes = sequence;
        in.close();
        byte[] res = showImage(imageTrueArray, imageIndexes, cc);
        byte[] keyGost = KeyGenerators.getAEADKey(masterPassword.getBytes(), salt);
        try {
            res = Gost.decrypt(keyGost, res);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("May be Bad password!");
            System.out.println("Try correct error");
            res[res.length-1]++;
            try {
                res = Gost.decrypt(keyGost, res);
            } catch (Exception e1){
                e1.printStackTrace();
                System.out.println("May be Bad password!");
                System.out.println("Try correct error");
                res[res.length-1] -= 2;
                try {
                    res = Gost.decrypt(keyGost, res);
                }catch (Exception e2){
                    e2.printStackTrace();
                    System.out.println("Bad password!");
                    return;
                }
                
            }
            
        }

        openFile.setText(pathTextResult, res);
        System.out.printf("Изменения записаны в файл %s\n", pathTextResult);
        System.out.printf("Считано %d бит\n", readCount);

        
    }
    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip 
        return buffer.getInt();
    }

    
    public static byte[] showImage(int[] image, int[] imageIndexes, int len){

        int readBit = 1;
        int indexImage = 0;

        int k = 0;
        

       
        byte[] result = new byte[len*2];
        readCount = 0;
        //len = image.length;
        
        
        for (int i = 0; i < len; i++){
            int imageIndex = imageIndexes != null ? imageIndexes[indexImage] : indexImage;
            int currColor = checkBit(image[imageIndex]);
            indexImage++;
            
            
            if (validateDouble(currColor)){
                int bit = parseBit(currColor);
                result[readCount] = (byte) ((bit >> 1) & 1); // {0000 0011} -> And {0000 0001} & {0000 0001} (1)
                result[readCount+1] = (byte) (bit & 1); //And {0000 0011} & {0000 0001} (1) = 0000 0001
                //i++;
                readCount = readCount + 2;
                continue;
            }
            int bit = parseBit(currColor);
            result[readCount] = (byte) bit;
            
            readCount++;
        }
        byte tempResult = 0;
        int lenRes = (readCount+1)/8;
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
