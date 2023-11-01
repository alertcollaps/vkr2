package com.company;


import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bouncycastle.jce.provider.BrokenPBE.Util;

import com.company.Encrypt.Utils;

public class Genetic {
    private byte[] data;
    private int[] image;
    private int[] out;
    private byte[] keyOut;
    private int imageType;
    private byte[] mutKey;
    private int sotes;
    private int w;
    private int h;
    private int minimum;
    private HashMap<byte[], int[]> idealPopul;
    private HashMap<byte[], int[]> population;
    private HashMap<byte[], HashSet<Integer>> populationChanges;
    private HashMap<byte[], HashSet<Integer>> idealPopulationChanges;
    HashMap<Integer, Integer> lst1 = new HashMap<>();
    HashMap<Integer, Integer> lst2 = new HashMap<>();
    StringBuffer str = new StringBuffer();
    private int sizePopulation;
    final int countPopulations = 10;
    final int countCopyies = 10;
    final int countInvalidPopulations = 2;
    private ArrayList<Integer> imageArrayIndexes;
    private double propCrossover = 1;
    final double persentageMutation = 0.3;
    final double persentageMutationSize = 0.4;
    final int maxMutation = 2000;
    final int minMutation = 200;
    int mutationSize = 20000;
    final int limitErrors = 3;
    final int sizeSeed = 14;
    private long seed = 123274692783460312L;
    final int sizeKeyMap = 2;

    private byte[] ch;
    Genetic(){

    }


    public Genetic(byte[] data, int[] image, int imageType, int w, int h, byte[] seedByte){
        this.data = data;
        this.image = new int[image.length];
        System.arraycopy(image, 0, this.image, 0, image.length);
        this.imageType = imageType;
        this.w = w;
        this.h = h;
        populationChanges = new HashMap<>();
       
        sizePopulation = data.length * 8;
        keyOut = new byte[0];
        out = new int[sizePopulation];
        
        imageArrayIndexes = new ArrayList<>();
        for (int i = 0; i < image.length; i++){
            imageArrayIndexes.add(i, i);
        }

        minimum = sizePopulation;
        if (seedByte != null){
            seed = bytesToLong(seedByte);
        }
        
        
    }

    public byte[] getSeed() {
        return longToBytes(seed);
    }

    public byte[] getMutKey() {
        return mutKey;
    }

    public byte[] getKeyOut() {
        return keyOut;
    }

    public byte[] getCh() {
        return ch;
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
    public void setSeed(byte[] seed) {
        this.seed = bytesToLong(seed);
    }

    public void run(){
        createPopulation();
        start();
    }


    public void start(){
        int min = minimum;
        
        
        int count = 0;
        selection();
        while (count < limitErrors){
            System.out.println("------------------------------------------------------");
            str.append("------------------------------------------------------\n");
            mutation();
            selection();
            
            List<byte[]> keys = new ArrayList<>(population.keySet());
            str.append("Liders:\n");
            for (byte[] k : keys){
                str.append(Utils.bytesToHex(k));
                str.append('\n');
            }
            
            if (min != minimum){
                min = minimum;
                System.out.println("New minimum: " + min);
                System.out.println("Зайдействовано ячеек: " + sotes);

                str.append("New minimum: " + min + '\n' + "Зайдействовано ячеек: " + sotes + '\n');
                
                int leng = keyOut.length;
                str.append("mutation size: ");
                if (leng > 8){
                    long lng = bytesToLong(new byte[]{keyOut[leng-8], keyOut[leng-7], keyOut[leng-6], keyOut[leng-5], keyOut[leng-4], keyOut[leng-3], keyOut[leng-2], keyOut[leng-1]});
                    Random random = new Random(lng);
                    int mutationSize = random.nextInt();
                    mutationSize = mutationSize >= 0 ? (mutationSize % maxMutation) + minMutation : (-mutationSize % maxMutation) + minMutation;
                    
                    str.append(mutationSize + '\n');
                } else {
                    str.append("Default: " + Utils.bytesToHex(keyOut) + '\n');
                }
                count = 0;
            
            } else {
                count++;
            }
            str.append("Errors:" + count + '\n');
            System.out.println("Errors:" + count);

            openFile.addToFile("log.log", str.toString());
            str.setLength(0);
            
            //crossover();
            
            
            

            
        }
        temp.setIndexImage(sotes);
        
    
    }

    public void selection(){ //TODO
        ArrayList<Integer> count = new ArrayList<>();
        List<byte[]> keys = new ArrayList<>(population.keySet());
        for (int i = 0; i < keys.size(); i++){
            int[] imageIndexes = population.get(keys.get(i));
            int[] img = new int[image.length];
            System.arraycopy(image, 0, img, 0, image.length);

            resultInserting res = temp.hideImage(data, img, imageIndexes, imageType, w, h); //Вызов оракула
            populationChanges.put(keys.get(i), res.matrixChanges);
            int min = res.countChanges;
            if (min < minimum){
                str.append("MIN:" + Utils.bytesToHex(keys.get(i)) + '\n');
                minimum = min;
                sotes = temp.getIndexImage();
                
                keyOut = Utils.concatArrays(intTobytes(sotes), longToBytes(seed), intTobytes(sizePopulation), keys.get(i));
                
                //System.out.println(sotes);
                System.arraycopy(imageIndexes, 0, out, 0, sizePopulation);
            }
            count.add(i, min); 
        }
        
        
        idealPopul = new HashMap<byte[], int[]>();
        idealPopulationChanges = new HashMap<byte[], HashSet<Integer>>();
        int min = Collections.min(count);
        int max = Collections.max(count) + 1;
        System.out.println("Min: " + min);
        int indexIdeal = count.indexOf(min);
        ArrayList<Integer> count1 = new ArrayList<>();
        for (int i = 0; i < count.size(); i++){
            count1.add(count.get(i));
        }

        

        for (int i = 0; i < countPopulations/*  - countInvalidPopulations*/; i++){
            min = Collections.min(count);
            indexIdeal = count.indexOf(min);
            count.set(indexIdeal, max);
            idealPopul.put(keys.get(indexIdeal), population.remove(keys.get(indexIdeal)));
            idealPopulationChanges.put(keys.get(indexIdeal), populationChanges.remove(keys.get(indexIdeal)));
            
        }
        /* 
        for (int i = 0; i < countInvalidPopulations; i++){
            max = Collections.max(count1);
            indexIdeal = count1.indexOf(max);
            count1.set(indexIdeal, min);
            idealPopul.put(keys.get(indexIdeal), population.remove(keys.get(indexIdeal)));
            idealPopulationChanges.put(keys.get(indexIdeal), populationChanges.remove(keys.get(indexIdeal)));
            
        }
        */
        population.clear();
        population.putAll(idealPopul);

        populationChanges.clear();
        populationChanges.putAll(idealPopulationChanges);
    }

    public void crossover(){
        SecureRandom rnd = new SecureRandom();

        
        
        

        List<byte[]> keys = new ArrayList<byte[]>(population.keySet());

        Collections.shuffle(keys, rnd);
        HashMap<byte[], int[]> tempik = new HashMap<byte[], int[]>();
    
        
        for (int i = 0; i < keys.size(); i += 2){
            byte[] k1 = keys.get(i);
            byte[] k2 = keys.get(i + 1);
            Random random = new Random(getLongFromByte(k1, k2));
            int[] pop1 = new int[population.get(k1).length];
            int[] pop2 = new int[population.get(k2).length];
            
            System.arraycopy(population.get(k1), 0, pop1, 0, pop1.length);
            System.arraycopy(population.get(k2), 0, pop2, 0, pop2.length);

            HashMap<Integer, Integer> lst1 = new HashMap<>();
            HashMap<Integer, Integer> lst2 = new HashMap<>();
            for (int j = 0; j < pop1.length; j++){
                lst1.put(pop1[j], j);
                lst2.put(pop2[j], j);
            }

            Arrays.sort(pop1, 0, sizePopulation);
            Arrays.sort(pop2, 0, sizePopulation);
            HashSet<Integer> ss = new HashSet<>();
         
            int[] ii1 = new int[(int) (sizePopulation * propCrossover)];
            int[] ii2 = new int[(int) (sizePopulation * propCrossover)];

            for (int j = 0; j < (int) (sizePopulation * propCrossover); j += 1){
                
                int index = random.nextInt(sizePopulation - 1);
                int count = 0;
                while (Arrays.binarySearch(pop2, 0, sizePopulation, pop1[index]) >= 0 && count < 10){
                    index = random.nextInt(sizePopulation - 1);
                    count++;
                }
                
                int index1 = random.nextInt(sizePopulation - 1);
                while (Arrays.binarySearch(pop1, 0, sizePopulation, pop2[index1]) >= 0 && count < 10){
                    index1 = random.nextInt(sizePopulation - 1);
                    count++;
                }
                if (count >= 10){
                    ii1[j] = -1;
                    ii2[j] = -1;
                    continue;
                }

                
                
                ii1[j] = lst1.get(pop1[index]);
                ii2[j] = lst2.get(pop2[index1]);
              
            }
            int[] temp1 = population.get(k1);
            int[] temp2 = population.get(k2);

            int[] child1 = new int[temp1.length];
            System.arraycopy(temp1, 0, child1, 0, child1.length);
            int[] child2 = new int[temp2.length];
            System.arraycopy(temp2, 0, child2, 0, child2.length);
            for (int j = 0; j < ii1.length; j++){
                if (ii1[j] == -1){
                    continue;
                }
                int temp = child1[ii1[j]];
                
                child1[ii1[j]] = child2[ii2[j]]; //
                child2[ii2[j]] = temp;
            }

            byte[] ch1 ;
            byte[] ch2 ;
            ch1 = Utils.concatArrays(new byte[]{1}, k1, new byte[]{0}, k2, new byte[]{2});
            ch2 = Utils.concatArrays(new byte[]{1}, k1, new byte[]{0, 0}, k2, new byte[]{2});
            tempik.put(ch1, child1);
            tempik.put(ch2, child2);
        }
        population.putAll(tempik);
    }
    public long getLongFromByte(byte[] a, byte[] b){
        long l1 = 0;
        l1 ^= Arrays.hashCode(a);
        l1 <<= 32;
        l1 ^= Arrays.hashCode(b);
        return l1;
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
        SecureRandom rnd = new SecureRandom();
        List<byte[]> keys = new ArrayList<>(population.keySet());
        //Collections.shuffle(keys, rnd);
        ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
        HashSet<Integer> arrIND = new HashSet<>(ArrayIndex);
        for (int i = 0; i < keys.size(); i++){
            
            long lng = rnd.nextLong();
            Random random = new Random(lng);
            int[] temp = population.get(keys.get(i));
            
            ArrayList<Integer> temp1 = new ArrayList<>(temp.length);
            int[] temp11 = new int[temp.length];
            ArrayList<Integer> arrayChangeNow = new ArrayList<Integer>();

            
            
            
            for (int cc = 0; cc < countCopyies; cc++){
                temp11 = new int[temp.length];
                temp1 = new ArrayList<>(temp.length);
                int iter = 0;
                for (int j : temp) {
                    temp1.add(j);
                    temp11[iter] = j;
                    iter++;
                }

                int[] arrayChange = new int[mutationSize];
                HashSet<Integer> matrixChanges = populationChanges.get(keys.get(i));
                do {

                    for (int c = 0; c < temp1.size(); c++){
                        arrayChangeNow.add(c);
                    }
                    lng = rnd.nextLong();
                    random = new Random(lng);
                    mutationSize = random.nextInt();
                    mutationSize = mutationSize >= 0 ? (mutationSize % maxMutation) + minMutation : (-mutationSize % maxMutation) + minMutation;
                    arrayChange = new int[mutationSize];

                    Collections.shuffle(arrayChangeNow, random);
                
                    
                    for (int c = 0; c < mutationSize; c++){
                        arrayChange[c] = arrayChangeNow.get(c);
                    }
                    arrayChangeNow.clear();

                    
                } while (!compareMutation(matrixChanges, arrayChange));
                str.append("mutation size: " + mutationSize + "\n");
                //Если temp2 меньше по размеру temp1?
                
                //System.out.println("Find matrix!");

                arrIND.removeAll(temp1);
                Integer[] temp2 = arrIND.toArray(new Integer[arrIND.size()]);
                Arrays.sort(temp2);
                arrIND.addAll(temp1);
                temp1.clear();;

                
                

                
                for (int j = 0; j < mutationSize; j++){
                    
                    int indRnd1 = arrayChange[j];

                    int indRnd = random.nextInt(temp2.length);
                    

                    int tmp = temp11[indRnd1];
                    
                    temp11[indRnd1] = temp2[indRnd];
                    
                    temp2[indRnd] = tmp;
                    
                }

                //population.remove(keys.get(i)); //Analysis
                
                byte[] mt = Utils.concatArrays(keys.get(i), new byte[]{3}, longToBytes(lng));
                
                
                population.put(mt, temp11);
                
            }
            
        }

        
    }

    public boolean compareMutation(HashSet<Integer> mut1, int[] mut2){
        

        int res = 0;
        
        for (int val : mut2){
            if (mut1.contains(val)){
                res++;
                continue;
            }
            res--;
        }
        
        //System.out.println(res);
        return res > 0.05*mutationSize;
    
    }

    public byte[] intTobytes(int a){
        return ByteBuffer.allocate(4).putInt(a).array();
    }

    public int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip 
        return buffer.getInt();
    }

    public void createPopulation(){
        population = new HashMap<byte[], int[]>();
      
        SecureRandom rnd = new SecureRandom();
        //seed = rnd.nextLong();
        short number = 0;
        
        str.append("seed: " + seed + "\n");
        for (int i = 0; i < countPopulations*2; i++){
            number = (short) rnd.nextInt();
            byte[] num = new byte[2];
            num[0] = (byte)((number >> 8) & 0b11111111);
            num[1] = (byte)(number & 0b11111111);
            while (num[0] == 0 || num[1] == 0 || num[0] == 1 || num[1] == 1 || num[0] == 2 || num[1] == 2 || num[0] == 3 || num[1] == 3){
                number = (short) rnd.nextInt();
                num[0] = (byte)((number >> 8) & 0b11111111);
                num[1] = (byte)(number & 0b11111111);
            }
            str.append("number: " + number + "\n");
            Random random = new Random(seed ^ number);

            ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
            int[] temp = new int[sizePopulation];
            
            Collections.shuffle(ArrayIndex, random);
            for (int j = 0; j < sizePopulation; j++){

                temp[j] = ArrayIndex.get(j);
            }
            
            //0 short(two bytes) 0
            //(short)(((num[1]&0b11111111) << 8) | (num[2]&0b11111111))
            
            population.put(num, temp);
           

        }

       
        
    }

    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
    
    public long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip 
        return buffer.getLong();
    }

    public int[] getPopulation(long seed, short key){ 
            Random random = new Random(seed ^ key);

            ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
            int[] temp = new int[sizePopulation];
            
            Collections.shuffle(ArrayIndex, random);
            for (int j = 0; j < sizePopulation; j++){

                temp[j] = ArrayIndex.get(j);
            }
            
            //0 short(two bytes) 0
            //(short)(((num[1]&0b11111111) << 8) | (num[2]&0b11111111))
            
            return temp;
    }

    public int[] getMutation(int[] pop, long key){
        
        ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
        HashSet<Integer> arrIND = new HashSet<>(ArrayIndex);
        
        int[] temp = new int[pop.length];
        System.arraycopy(pop, 0, temp, 0, temp.length);

        long lng = key;
        int[] temp11 = new int[temp.length];
        
        Random random = new Random(lng);
        mutationSize = random.nextInt();
        mutationSize = mutationSize >= 0 ? (mutationSize % maxMutation) + minMutation : (-mutationSize % maxMutation) + minMutation;
        

        
            
       
            ArrayList<Integer> temp1 = new ArrayList<>(temp.length);
            ArrayList<Integer> arrayChangeNow = new ArrayList<>(temp.length);
            int iter = 0;
            for (int c = 0; c < temp.length; c++){
                arrayChangeNow.add(c);
            }
            for (int j : temp){
                temp1.add(j);
                temp11[iter] = j; 
                //arrayChangeNow.add(iter);
                iter++;
            }
            int[] arrayChange = new int[mutationSize];

            Collections.shuffle(arrayChangeNow, random);
        
            
            for (int c = 0; c < mutationSize; c++){
                arrayChange[c] = arrayChangeNow.get(c);
            }
            arrayChangeNow.clear();

            

            arrIND.removeAll(temp1);
            Integer[] temp2 = arrIND.toArray(new Integer[arrIND.size()]);
            Arrays.sort(temp2);
            arrIND.addAll(temp1);
            temp1.clear();;

            
            

            
            for (int j = 0; j < mutationSize; j++){
                
                int indRnd1 = arrayChange[j];

                int indRnd = random.nextInt(temp2.length);
                

                int tmp = temp11[indRnd1];
                
                temp11[indRnd1] = temp2[indRnd];
                
                temp2[indRnd] = tmp;
                
            }
            
        
        
        
        return temp11;
    }

    public int[] getCrossover(int[] popul1, int[] popul2, byte[] key1, byte[] key2, int countZero){
        byte[] k1 = key1;
        byte[] k2 = key2;
        Random random = new Random(getLongFromByte(k1, k2));
    
        int[] pop1 = new int[popul1.length];
        int[] pop2 = new int[popul2.length];
        
        System.arraycopy(popul1, 0, pop1, 0, pop1.length);
        System.arraycopy(popul2, 0, pop2, 0, pop2.length);
        //Long time = System.currentTimeMillis();
        lst1.clear();
        lst2.clear();
        for (int i = 0; i < popul1.length; i++){
            lst1.put(popul1[i], i);
            lst2.put(popul2[i], i);
        }
        //System.out.println("LST part: " + Long.toString(System.currentTimeMillis() - time));
        
        
        Arrays.sort(pop1, 0, sizePopulation);
        Arrays.sort(pop2, 0, sizePopulation);
        HashSet<Integer> ss = new HashSet<>();
     
        int[] ii1 = new int[(int) (sizePopulation * propCrossover)];
        int[] ii2 = new int[(int) (sizePopulation * propCrossover)];

        for (int j = 0; j < (int) (sizePopulation * propCrossover); j += 1){
            
            int index = random.nextInt(sizePopulation - 1);
            int count = 0;
            while (Arrays.binarySearch(pop2, 0, sizePopulation, pop1[index]) >= 0 && count < 10){
                
                index = random.nextInt(sizePopulation - 1);
                count++;
            }
            
            int index1 = random.nextInt(sizePopulation - 1);
            while (Arrays.binarySearch(pop1, 0, sizePopulation, pop2[index1]) >= 0 && count < 10){
                index1 = random.nextInt(sizePopulation - 1);
                count++;
            }
            if (count >= 10){
                ii1[j] = -1;
                ii2[j] = -1;
                continue;
            }

            ii1[j] = lst1.get(pop1[index]);
            ii2[j] = lst2.get(pop2[index1]);
            
        }
        int[] temp1 = popul1;
        int[] temp2 = popul2;

        int[] child1 = new int[temp1.length];
        System.arraycopy(temp1, 0, child1, 0, child1.length);
        int[] child2 = new int[temp2.length];
        System.arraycopy(temp2, 0, child2, 0, child2.length);
        for (int j = 0; j < ii1.length; j++){
            if (ii1[j] == -1){
                continue;
            }
            int temp = child1[ii1[j]];
            
            child1[ii1[j]] = child2[ii2[j]]; //
            child2[ii2[j]] = temp;
        }

        byte[] ch1 ;

        byte[] ch2 ;
        ch1 = Utils.concatArrays(new byte[]{1}, k1, new byte[]{0}, k2, new byte[]{2});
        ch2 = Utils.concatArrays(new byte[]{1}, k1, new byte[]{0, 0}, k2, new byte[]{2});
        if (countZero == 1){
            ch = ch1;
            return child1;
        } else if (countZero == 2){
            ch = ch2;
            return child2;
        }
        System.out.println("Zero no 1 or 2!!!!");;
        return null;
    }

    public int[] reSequence(byte[] key){
        
        byte[] keyArr = new byte[key.length - 16];

        System.arraycopy(key, 16, keyArr, 0, keyArr.length);


        byte[] sizePopulationBytes = new byte[4];
        System.arraycopy(key, 12, sizePopulationBytes, 0, 4);
        sizePopulation = bytesToInt(sizePopulationBytes);
        
        byte[] seedByte = new byte[8];
        System.arraycopy(key, 4, seedByte, 0, 8);
        
        seed = bytesToLong(seedByte);

        short number = (short)(((keyArr[0]&0b11111111) << 8) | ((keyArr[1])&0b11111111));
        int[] population = getPopulation(seed, number);
        for (int i = 2; i < keyArr.length; i++){
            if (keyArr[i] == 3){
                byte[] bLong = new byte[8];
                System.arraycopy(keyArr, i + 1, bLong, 0, bLong.length);
                long lng = bytesToLong(bLong);
                population = getMutation(population, lng);
                i += 8;
            }
        }      

       
        System.out.println();
        return population;
    }

}
