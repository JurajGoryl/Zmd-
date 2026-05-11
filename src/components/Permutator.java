package components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Permutator {

    public static byte[] permute(byte[] data, int key) {
        ArrayList<Byte> list = new ArrayList<>();
        for (byte b : data) {
            list.add(b);
        }

        Collections.shuffle(list, new Random(key));
        byte[] result = new byte[data.length];

        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static byte[] unpermute(byte[] data, int key) {
        int n = data.length;
        ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            indexList.add(i);
        }

        Collections.shuffle(indexList, new Random(key));

        byte[] result = new byte[n];
        for (int i = 0; i < n; i++) {
            result[indexList.get(i)] = data[i];
        }

        return result;
    }
}
