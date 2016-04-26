package Clustering;

import java.util.List;

public interface IClusterMerger {

       /**
        * Merges clusters together based on in implementation of a
        * distance function. Examples are {@link IClusterMerger} and
        * {@link MSTMerger}. 
        * @return
        */
       List<Cluster> MergeClusters(List<Cluster> baseClustersToMerge);
}