package com.company;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Genetic {
    private byte[] data;
    private int[] image;
    private int[] out;
    private String pathResult; 
    private int imageType;
    private int w;
    private int h;
    private int minimum;
    private ArrayList<int[]> idealPopul;
    private ArrayList<int[]> population;
    private int sizePopulation;
    final int countPopulations = 18;
    final int countIdealPopulations = 5;
    private ArrayList<Integer> imageArrayIndexes;
    private double propCrossover = 0.02;
    final double persentageMutation = 1;
    final double persentageMutationSize = 0.2;
    final int limitErrors = 5;
    Genetic(){

    }


    Genetic(byte[] data, int[] image, String pathResult, int imageType, int w, int h){
        this.data = data;
        this.image = new int[image.length];
        System.arraycopy(image, 0, this.image, 0, image.length);
        this.pathResult = pathResult;
        this.imageType = imageType;
        this.w = w;
        this.h = h;
        
        sizePopulation = data.length * 8;
        out = new int[sizePopulation];

        imageArrayIndexes = new ArrayList<>();
        for (int i = 0; i < image.length; i++){
            imageArrayIndexes.add(i, i);
        }

        minimum = sizePopulation;
    
        
    }

    public int[] getOut(){
        return out;
    }

    public int getMinimum(){
        return minimum;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void run(){
        createPopulation();
        start();
    }


    public void start(){
        int min = minimum;
        
        int count = 2;
        while (count < limitErrors){
            selection();
            if (min != minimum){
                min = minimum;
                System.out.println("New minimum:" + min);
                count = 0;
            
            } else {
                count++;
            }
            System.out.println("Errors:" + count);
            
            crossover();
            
            mutation();
            

            
        }
        
    
    }

    public void selection(){
        ArrayList<Integer> count = new ArrayList<>();
        for (int i = 0; i < population.size(); i++){
            int[] imageIndexes = population.get(i);
            int[] img = new int[image.length];
            System.arraycopy(image, 0, img, 0, image.length);

            int min = temp.hideImage(data, img, imageIndexes, imageType, w, h); //Вызов оракула
            if (min < minimum){
                minimum = min;

                System.arraycopy(imageIndexes, 0, out, 0, sizePopulation);
            }
            count.add(i, min); 
        }

        
        idealPopul = new ArrayList<int[]>();
        int min = Collections.min(count);
        System.out.println("Min: " + min);
        int indexIdeal = count.indexOf(min);

        

        for (int i = 0; i < countPopulations; i++){
            min = Collections.min(count);
            indexIdeal = count.indexOf(min);
            count.remove(indexIdeal);
            idealPopul.add(i, population.remove(indexIdeal));
        }
        population.clear();
        population.addAll(idealPopul);
    }

    public void crossover(){
       
        SecureRandom random = new SecureRandom();

        Collections.shuffle(population, random);
        ArrayList<int[]> tempik = new ArrayList<>();
    
        
        for (int i = 0; i < population.size(); i += 2){
            Arrays.sort(population.get(i), 0, sizePopulation);
            Arrays.sort(population.get(i+1), 0, sizePopulation);
            HashSet<Integer> ss = new HashSet<>();
         
            int[] ii1 = new int[(int) (sizePopulation * propCrossover)];
            int[] ii2 = new int[(int) (sizePopulation * propCrossover)];

            for (int j = 0; j < (int) (sizePopulation * propCrossover); j += 1){
                
                int index = random.nextInt(sizePopulation - 1);
                
                while (Arrays.binarySearch(population.get(i+1), 0, sizePopulation, population.get(i)[index]) >= 0){
                    
                    index = random.nextInt(sizePopulation - 1);
                }
                
                int index1 = random.nextInt(sizePopulation - 1);
                while (Arrays.binarySearch(population.get(i), 0, sizePopulation, population.get(i + 1)[index1]) >= 0){
                    index1 = random.nextInt(sizePopulation - 1);
                }
                ii1[j] = index;
                ii2[j] = index1;
            }
            int[] child1 = population.get(i).clone();
            int[] child2 = population.get(i + 1).clone();
            for (int j = 0; j < ii1.length; j++){
                int temp = child1[ii1[j]];
                
                child1[ii1[j]] = child2[ii2[j]]; //
                child2[ii2[j]] = temp;
            }
            tempik.add(child2);
            tempik.add(child1);
        }
        population.addAll(tempik);
    }

    public boolean contains(int[] array, int key) {
        for (int i : array) {
            if (i == key) {
                System.out.println("Alert!:");
                return true;
            }
        }
        return false;
    }

    public void mutation(){
        SecureRandom random = new SecureRandom();
        Collections.shuffle(population, random);
        for (int i = 0; i < population.size() * persentageMutationSize; i++){
            int[] temp = population.get(i);
            ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
            ArrayList<Integer> temp1 = new ArrayList<>(temp.length);
            for (int j : temp){
                temp1.add(j);
            }
            HashSet<Integer> ss = new HashSet<>(ArrayIndex);

            ss.removeAll(temp1);
            Integer[] temp2 = ss.toArray(new Integer[ss.size()]);


            
            for (int j = 0; j < (int) (sizePopulation * persentageMutation/100); j++){
                
                int indRnd1 = random.nextInt(temp.length);

                int indRnd = random.nextInt(temp2.length);
                

                int tmp = temp[indRnd1];
                
                temp[indRnd1] = temp2[indRnd];
                
                temp2[indRnd] = tmp;
                
            }
        }

        
    }

    public void createPopulation(){
        population = new ArrayList<int[]>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < countPopulations; i++){
            HashSet<Integer> ss = new HashSet<>(imageArrayIndexes);
            ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
            int[] temp = new int[sizePopulation];
            
            Collections.shuffle(ArrayIndex, random);
            for (int j = 0; j < sizePopulation; j++){
                
                
                temp[j] = ArrayIndex.get(j);
            }
            population.add(i, temp);

        }
        
    }

}
