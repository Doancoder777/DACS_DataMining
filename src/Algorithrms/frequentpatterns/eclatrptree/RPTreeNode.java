package Algorithrms.frequentpatterns.eclatrptree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RPTreeNode implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int[] itemset;
    
    private BitSet bitset;
    
    private int support;
    
    private boolean isValid;
    
    private List<RPTreeNode> children;
    
    public RPTreeNode(int[] itemset, BitSet bitset) {
        this.itemset = itemset;
        this.bitset = bitset;
        this.support = bitset != null ? bitset.cardinality() : 0;
        this.isValid = true;
        this.children = new ArrayList<>();
    }
    
    public int[] getItemset() {
        return itemset;
    }
    
    public void setItemset(int[] itemset) {
        this.itemset = itemset;
    }
    
    public BitSet getBitset() {
        return bitset;
    }
    
    public void setBitset(BitSet bitset) {
        this.bitset = bitset;
        this.support = bitset != null ? bitset.cardinality() : 0;
    }
    
    public int getSupport() {
        return support;
    }
    
    public void setSupport(int support) {
        this.support = support;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }
    
    public List<RPTreeNode> getChildren() {
        return children;
    }
    
    public void addChild(RPTreeNode child) {
        this.children.add(child);
    }
    
    public boolean removeChild(RPTreeNode child) {
        return this.children.remove(child);
    }
    
    public String toString(String indent) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(indent);
        if (itemset == null) {
            sb.append("{}");
        } else {
            sb.append("{");
            for (int i = 0; i < itemset.length; i++) {
                sb.append(itemset[i]);
                if (i < itemset.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
        }
        sb.append(": ").append(support).append("\n");
        
        for (RPTreeNode child : children) {
            sb.append(child.toString(indent + "  "));
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return toString("");
    }
}