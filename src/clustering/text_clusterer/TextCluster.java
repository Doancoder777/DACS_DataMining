package clustering.text_clusterer;
import java.util.ArrayList;
import java.util.HashSet;
public class TextCluster {
	public ArrayList<Integer> cluster; //consists of indices of records in a cluster
	public ArrayList<Integer> getCluster() {
		return cluster;
	}
	public void setCluster(ArrayList<Integer> cluster) {
		this.cluster = cluster;
	}
	@Override
	public boolean equals(Object o1)
	{
		ArrayList<Integer> cluster1List=this.getCluster();
		TextCluster cluster2=(TextCluster)o1;
		ArrayList<Integer> cluster2List=cluster2.getCluster();
		HashSet<Integer> cluster=new HashSet<Integer>();
		int flag=0;
		for(int x:cluster1List)
		{
		for(int y:cluster2List)	
		{	if(cluster2List.contains(x)||cluster1List.contains(y)||x==y)
			{
				flag=1;
			}
		}
		}
		if(flag==0)
		{
		return false;
		}
		else
		{
			cluster.addAll(cluster1List);
			cluster.addAll(cluster2List);
			ArrayList<Integer> clusterList=new ArrayList<Integer>(cluster);
			cluster2.setCluster(clusterList);
	return true;
		}
	}
	public int hashCode()
	{
		return 31;
	}
}

