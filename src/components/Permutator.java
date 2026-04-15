package components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Permutator {

    // we use an array[] of bytes where we store all the watermark data and we permute it using a key k.
    public static byte[] permute(byte[] data, int key) {

        ArrayList<Byte> list = new ArrayList<>(); // we will make a new list of bytes

        for (byte b : data){  // we add all the bytes from the data to the list
            list.add(b); 
        }

        Collections.shuffle(list, new Random(key)); // we shuffle the list using the key
        byte[] result = new byte[data.length];

        for (int i = 0; i < list.size(); i++){
            result[i] = list.get(i);
        }

        return result;
    }

    public static byte[] unpermute(byte[] data, int key) {

        // we create an array of integers that will store the positions of the bytes(pixels) in the original data
        int n = data.length;
        Integer[] positions = new Integer[n];

        for (int i = 0; i < n; i++){
            positions[i] = i;
        }

        // we shuffle the positions using the same key to get the original positions
        ArrayList<Integer> indexList = new ArrayList<>();
        Collections.addAll(indexList, positions);
        Collections.shuffle(indexList, new Random(key));

        byte[] result = new byte[n];

        for (int i = 0; i < n; i++){
            result[indexList.get(i)] = data[i];
        }

        return result;
    }
}
