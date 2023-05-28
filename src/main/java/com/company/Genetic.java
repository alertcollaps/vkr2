package com.company;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
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
    private int w;
    private int h;
    private int minimum;
    private HashMap<byte[], int[]> idealPopul;
    private HashMap<byte[], int[]> population;
    private HashMap<Short, String> populationHistory;
    private int sizePopulation;
    final int countPopulations = 18;
    final int countIdealPopulations = 5;
    private ArrayList<Integer> imageArrayIndexes;
    private double propCrossover = 0.001;
    final double persentageMutation = 1;
    final double persentageMutationSize = 0.2;
    final int limitErrors = 2;
    final int sizeSeed = 14;
    private long seed = 123274692783460312L;
    final int sizeKeyMap = 2;

    private byte[] ch;
    Genetic(){

    }


    public Genetic(byte[] data, int[] image, int imageType, int w, int h){
        this.data = data;
        this.image = new int[image.length];
        System.arraycopy(image, 0, this.image, 0, image.length);
        this.imageType = imageType;
        this.w = w;
        this.h = h;
        
        sizePopulation = data.length * 8;
        keyOut = new byte[0];
        out = new int[sizePopulation];
        populationHistory = new HashMap<>();
        imageArrayIndexes = new ArrayList<>();
        for (int i = 0; i < image.length; i++){
            imageArrayIndexes.add(i, i);
        }

        minimum = sizePopulation;
    
        
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

    public void run(){
        createPopulation();
        start();
    }


    public void start(){
        int min = minimum;
        
        int count = 0;
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

    public void selection(){ //TODO
        ArrayList<Integer> count = new ArrayList<>();
        List<byte[]> keys = new ArrayList<>(population.keySet());
        for (int i = 0; i < keys.size(); i++){
            int[] imageIndexes = population.get(keys.get(i));
            int[] img = new int[image.length];
            System.arraycopy(image, 0, img, 0, image.length);

            int min = temp.hideImage(data, img, imageIndexes, imageType, w, h); //Вызов оракула
            if (min < minimum){
                minimum = min;
                keyOut = Utils.concatArrays(longToBytes(seed), intTobytes(sizePopulation), keys.get(i));
                System.out.println(Utils.bytesToHex(keyOut));
                System.arraycopy(imageIndexes, 0, out, 0, sizePopulation);
            }
            count.add(i, min); 
        }

        
        idealPopul = new HashMap<byte[], int[]>();
        int min = Collections.min(count);
        int max = Collections.max(count) + 1;
        System.out.println("Min: " + min);
        int indexIdeal = count.indexOf(min);

        

        for (int i = 0; i < countPopulations; i++){
            min = Collections.min(count);
            indexIdeal = count.indexOf(min);
            count.set(indexIdeal, max);
            idealPopul.put(keys.get(indexIdeal), population.remove(keys.get(indexIdeal)));
        }
        population.clear();
        population.putAll(idealPopul);
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

            List list1 = Arrays.stream(population.get(k1)).boxed().collect(Collectors.toList());
            List list2 = Arrays.stream(population.get(k2)).boxed().collect(Collectors.toList());

            Arrays.sort(pop1, 0, sizePopulation);
            Arrays.sort(pop2, 0, sizePopulation);
            HashSet<Integer> ss = new HashSet<>();
         
            int[] ii1 = new int[(int) (sizePopulation * propCrossover)];
            int[] ii2 = new int[(int) (sizePopulation * propCrossover)];

            for (int j = 0; j < (int) (sizePopulation * propCrossover); j += 1){
                
                int index = random.nextInt(sizePopulation - 1);
                
                while (Arrays.binarySearch(pop2, 0, sizePopulation, pop1[index]) >= 0){
                    index = random.nextInt(sizePopulation - 1);
                }
                
                int index1 = random.nextInt(sizePopulation - 1);
                while (Arrays.binarySearch(pop1, 0, sizePopulation, pop2[index1]) >= 0){
                    index1 = random.nextInt(sizePopulation - 1);
                }

                
                
                ii1[j] = list1.indexOf(pop1[index]);
                ii2[j] = list2.indexOf(pop2[index1]);
              
            }
            int[] temp1 = population.get(k1);
            int[] temp2 = population.get(k2);

            int[] child1 = new int[temp1.length];
            System.arraycopy(temp1, 0, child1, 0, child1.length);
            int[] child2 = new int[temp2.length];
            System.arraycopy(temp2, 0, child2, 0, child2.length);
            for (int j = 0; j < ii1.length; j++){
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
        Collections.shuffle(keys, rnd);
        for (int i = 0; i < keys.size() * persentageMutationSize; i++){
            Random random = new Random(getLongFromByte(keys.get(i), keys.get(i)));
            int[] temp = population.get(keys.get(i));
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

            population.remove(keys.get(i));
            byte[] mt = Utils.concatArrays(new byte[]{3} ,keys.get(i));
            population.put(mt, temp);
        }

        
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
        seed = rnd.nextLong();
        short number = 0;
        
        for (int i = 0; i < countPopulations; i++){
            number = (short) rnd.nextInt();
            byte[] num = new byte[2];
            num[0] = (byte)((number >> 8) & 0b11111111);
            num[1] = (byte)(number & 0b11111111);
            while (num[0] == 0 || num[1] == 0 || num[0] == 1 || num[1] == 1 || num[0] == 2 || num[1] == 2 || num[0] == 3 || num[1] == 3){
                number = (short) rnd.nextInt();
                num[0] = (byte)((number >> 8) & 0b11111111);
                num[1] = (byte)(number & 0b11111111);
            }
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

    public int[] getMutation(int[] pop, byte[] key, int count){
        if (count == 0){
            mutKey = key;
            return pop;
        }
        ArrayList<Integer> ArrayIndex = new ArrayList<>(imageArrayIndexes);
        int[] temp = new int[pop.length];
        System.arraycopy(pop, 0, temp, 0, temp.length);

        for (int i = 0; i < count; i++){
            Random random = new Random(getLongFromByte(key, key));
       
            
            ArrayList<Integer> temp1 = new ArrayList<>(temp.length);
            for (int j : temp){
                temp1.add(j);
            }
            HashSet<Integer> ss = new HashSet<>(ArrayIndex);

            ss.removeAll(temp1);
            Integer[] temp2 = ss.toArray(new Integer[ss.size()]);
            ss.clear();
    
            for (int j = 0; j < (int) (sizePopulation * persentageMutation/100); j++){
                
                int indRnd1 = random.nextInt(temp.length);

                int indRnd = random.nextInt(temp2.length);
                

                int tmp = temp[indRnd1];
                
                temp[indRnd1] = temp2[indRnd];
                
                temp2[indRnd] = tmp;
                
            }
            key = Utils.concatArrays(new byte[]{3}, key);
        }
        mutKey = key;
        return temp;
    }

    public int[] getCrossover(int[] popul1, int[] popul2, byte[] key1, byte[] key2, int countZero){
        byte[] k1 = key1;
        byte[] k2 = key2;
        Random random = new Random(getLongFromByte(k1, k2));
    
        int[] pop1 = new int[popul1.length];
        int[] pop2 = new int[popul2.length];
        
        System.arraycopy(popul1, 0, pop1, 0, pop1.length);
        System.arraycopy(popul2, 0, pop2, 0, pop2.length);

        List list1 = Arrays.stream(popul1).boxed().collect(Collectors.toList());
        List list2 = Arrays.stream(popul2).boxed().collect(Collectors.toList());

        Arrays.sort(pop1, 0, sizePopulation);
        Arrays.sort(pop2, 0, sizePopulation);
        HashSet<Integer> ss = new HashSet<>();
     
        int[] ii1 = new int[(int) (sizePopulation * propCrossover)];
        int[] ii2 = new int[(int) (sizePopulation * propCrossover)];

        for (int j = 0; j < (int) (sizePopulation * propCrossover); j += 1){
            
            int index = random.nextInt(sizePopulation - 1);
            
            while (Arrays.binarySearch(pop2, 0, sizePopulation, pop1[index]) >= 0){
                
                index = random.nextInt(sizePopulation - 1);
            }
            
            int index1 = random.nextInt(sizePopulation - 1);
            while (Arrays.binarySearch(pop1, 0, sizePopulation, pop2[index1]) >= 0){
                index1 = random.nextInt(sizePopulation - 1);
            }

            ii1[j] = list1.indexOf(pop1[index]);
            ii2[j] = list2.indexOf(pop2[index1]);
            
        }
        int[] temp1 = popul1;
        int[] temp2 = popul2;

        int[] child1 = new int[temp1.length];
        System.arraycopy(temp1, 0, child1, 0, child1.length);
        int[] child2 = new int[temp2.length];
        System.arraycopy(temp2, 0, child2, 0, child2.length);
        for (int j = 0; j < ii1.length; j++){
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
        byte[] keyArr = new byte[key.length - 12];
        byte[] keyArr1 = new byte[key.length - 12];
        System.arraycopy(key, 12, keyArr, 0, keyArr.length);
        System.arraycopy(key, 12, keyArr1, 0, keyArr1.length);

        byte[] sizePopulationBytes = new byte[4];
        System.arraycopy(key, 8, sizePopulationBytes, 0, 4);
        sizePopulation = bytesToInt(sizePopulationBytes);
        
        byte[] seedByte = new byte[8];
        System.arraycopy(key, 0, seedByte, 0, 8);

        seed = bytesToLong(seedByte);

        int countOnes = 0;
        int[] arrOnes = new int[key.length];


        int countShorts = 0;
        int[] arrShorts = new int[key.length];
        HashMap<Integer, int[]> position = new HashMap<>();
        HashMap<Integer, byte[]> seedPosition = new HashMap<>();
        HashMap<byte[], Integer> seedPositionREs = new HashMap<>();
        HashMap<Short, Integer> shortPosition = new HashMap<>();
        for (int i = 0; i < keyArr.length; i++){
            position.put(i, null);
            seedPosition.put(i, null);
            if (keyArr[i] == 1){
                arrOnes[countOnes] = i;
                countOnes++;
            } else if (keyArr[i] != 0 && keyArr[i] != 2 && keyArr[i] != 3){
                position.remove(i);
                seedPosition.remove(i);
                byte[] shrt = new byte[]{keyArr[i], keyArr[i+1]};
                short numb = (short)(((shrt[0]&0b11111111) << 8) | (shrt[1]&0b11111111));
                int[] pop = null;
                if (shortPosition.containsKey(numb)){
                    int y = shortPosition.get(numb);
                    pop = position.get(y);
                } else {
                    pop = getPopulation(seed, numb);
                    shortPosition.put(numb, i);
                }
                
                
                position.put(i, pop);
                seedPosition.put(i, shrt);
                
                keyArr[i+1] = 1;
                keyArr[i] = 1;
                position.put(i+1, null);
                seedPosition.put(i+1, null);
                i++;
            }       
            
            
        }      

        for (int i = countOnes - 1; i >= 0; i--){
            int k = arrOnes[i];
            
            int currValue = keyArr[k];
           
            int countThree = 0;
            LinkedHashMap<byte[], int[]> currentSum = new LinkedHashMap<>();
            
            int countCurrentSum = 0;
            int countZero = 0;
            while (currValue != 2){
                if (currValue == 3){
                    countThree++;
                    k++;
                    currValue = keyArr[k];
                    continue;
                }
                if (currValue == 0){
                    k++;
                    currValue = keyArr[k];
                    System.out.println("Error! Zero!!");
                    continue;
                }
                if (position.get(k) == null){
                    System.out.println("Warning! Pass value!");
                    k++;
                    currValue = keyArr[k];
                    continue;
                }
                byte[] kk = seedPosition.get(k);
                int[] curr = getMutation(position.get(k), seedPosition.get(k), countThree);
                kk = getMutKey();
                currentSum.put(kk, curr);
                while (k < keyArr.length){
                    k++;
                    currValue = keyArr[k];
                    if (currValue == 2){
                        k--;
                        break;
                    }
                    if (currValue == 0){
                        if (keyArr[k+1] == 0){
                            countZero = 2;
                            keyArr[k+1] = 1;
                            keyArr[k] = 1;
                            k++;
                        } else{
                            keyArr[k] = 1;
                            countZero = 1;
                        }
                        
                        break;
                    }
                    
                }
                countThree = 0;
                countCurrentSum++;
                k++;
                currValue = keyArr[k];
                continue;
            }
            List<byte[]> lstKeys = new ArrayList<>(currentSum.keySet());
            if (lstKeys.size() != 2){
                System.out.println("Error!! lstKeys != 2");
            }
            int[] cross = getCrossover(currentSum.get(lstKeys.get(0)), currentSum.get(lstKeys.get(1)), lstKeys.get(0), lstKeys.get(1), countZero);
            byte[] newKey = ch;
            position.remove(arrOnes[i]);
            seedPosition.remove(arrOnes[i]);
            position.put(arrOnes[i], cross);
            seedPosition.put(arrOnes[i], newKey);
            if (keyArr[k] != 2){
                System.out.println("Error. k error != 2");
            } else {
                keyArr[k] = 1;
            }

        }
        int currValue = keyArr[0];
        int countThree = 0;
        while (currValue == 3){
            countThree++;
            currValue = keyArr[countThree]; 
        }
        byte[] kk = seedPosition.get(countThree);
        int[] out = getMutation(position.get(countThree), kk, countThree);
        kk = getMutKey();
        byte[] r = kk;
        for (byte y : r){
            System.out.print(y + " ");
        }
        System.out.println();
        return out;
    }

}
