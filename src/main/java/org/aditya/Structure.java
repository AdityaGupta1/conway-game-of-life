package org.aditya;

public class Structure {
    private int[] index;
    private int[] size;

    Structure(int[] index, int[] size) {
        this.index = index;
        this.size = size;
    }

    public int[] getIndex() {
        return index;
    }

    public int[] getSize() {
        return size;
    }
}
