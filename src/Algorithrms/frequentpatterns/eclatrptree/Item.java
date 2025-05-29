package frequentpatterns.eclatrptree;

import java.util.BitSet;

public class Item implements Comparable<Item> {
    
    private int item;
    
    private BitSet bitset;
    
    private int support;
    
    private boolean isRare;
    
    public Item(int item, BitSet bitset) {
        this.item = item;
        this.bitset = bitset;
        this.support = bitset.cardinality();
        this.isRare = false;
    }
    
    public int getItem() {
        return item;
    }
    
    public BitSet getBitset() {
        return bitset;
    }
    
    public int getSupport() {
        return support;
    }
    
    public boolean isRare() {
        return isRare;
    }
    
    public void setRare(boolean isRare) {
        this.isRare = isRare;
    }
    
    @Override
    public int compareTo(Item other) {
        return Integer.compare(other.support, this.support);
    }
    
    @Override
    public String toString() {
        return "{" + item + "}: " + support + (isRare ? " (hiếm)" : " (phổ biến)");
    }
}