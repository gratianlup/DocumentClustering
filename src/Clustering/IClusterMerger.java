package Clustering;

import java.util.Set;

public interface IClusterMerger {

       /**
        * Merges clusters together based on in implementation of a
        * distance function. Examples are {@link IClusterMerger} and
        * {@link MSTMerger}. 
        * @return
        */
       Set<Cluster> MergeClusters(Set<Cluster> baseClustersToMerge);
}