package com.company;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class openFile {
    public static BufferedImage getImage(String path){
        File file = new File(path);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    public static void setImage(String path, BufferedImage image, String format){
        File file = new File(path);
        
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getText(String path){
        byte[] mass = null;
        int k = 0;
        try(FileInputStream fin=new FileInputStream(path))
        {
            System.out.printf("File size: %d bytes \n", fin.available());
            mass = new byte[fin.available()];
            byte i=-1;
            while((i= (byte) fin.read())!=-1){
                mass[k] = i;
                k++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mass;
    }

    public static void setText(String path, byte[] text){
        try(FileOutputStream fos=new FileOutputStream(path))
        {
            fos.write(text, 0, text.length);
            System.out.println("The file has been written " + path);
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    public static void addToFile(String path, String text){
        try (FileWriter writer = new FileWriter(path, true)){
            writer.append(text);
            writer.flush();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
