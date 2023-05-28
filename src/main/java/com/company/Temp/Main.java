package com.company.Temp;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.zip.Deflater;

import org.bouncycastle.util.Arrays;

import com.company.Genetic;
import com.company.openFile;
import com.company.Encrypt.Utils;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException{
        BufferedImage image = openFile.getImage("1024px-Lenna.png");
        

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
        Genetic genetic = new Genetic(new byte[]{}, imageTrueArray, image.getType(), width, heigth);
        byte[] key = Utils.hexStringToByteArray(new String(openFile.getText("keyOut.txt")));
        int[] sequence = genetic.reSequence(key);
        System.out.println();
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
