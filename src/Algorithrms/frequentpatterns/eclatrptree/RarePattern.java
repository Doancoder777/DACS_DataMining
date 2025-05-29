package frequentpatterns.eclatrptree;

import java.util.Arrays;

public class RarePattern implements Comparable<RarePattern> {
    
    private int[] itemset;
    
    private int support;
    
    public RarePattern(int[] itemset, int support) {
        this.itemset = itemset;
        this.support = support;
    }
    
    public int[] getItemset() {
        return itemset;
    }
    
    public int getSupport() {
        return support;
    }
    
    @Override
    public int compareTo(RarePattern other) {
        int sizeCompare = Integer.compare(this.itemset.length, other.itemset.length);
        if (sizeCompare != 0) return sizeCompare;
        
        return Integer.compare(this.support, other.support);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RarePattern that = (RarePattern) obj;
        return support == that.support && Arrays.equals(itemset, that.itemset);
    }
    
    @Override
    public int hashCode() {
        int result = support;
        result = 31 * result + Arrays.hashCode(itemset);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < itemset.length; i++) {
            sb.append(itemset[i]);
            if (i < itemset.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("}: ");
        sb.append(support);
        return sb.toString();
    }
}