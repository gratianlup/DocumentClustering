// Copyright (c) 2010 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "DocumentClustering" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "DocumentClustering" nor
// may "DocumentClustering" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package Clustering;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Merges base clusters to produce final clusters using a minimum overlap threshold.
 */
public class MinDegreeClusterMerger extends AbstractOverlappingClusterMerger {

	public MinDegreeClusterMerger(double overlapDegree_) {
		this.minOverlapDegree = overlapDegree_;
	}

	public List<Cluster> MergeClusters(List<Cluster> baseClustersToMerge) {
		// Build a graph of similar base clusters.
		List<ClusterInfo> baseClusterInfos = generateClusterInfo(baseClustersToMerge);
		ConnectClusters(baseClusterInfos);

		// Find the connected components within the graph produced above.
		Queue<ClusterInfo> queue = new LinkedList<ClusterInfo>();
		ArrayList<ArrayList<Cluster>> components = new ArrayList<ArrayList<Cluster>>();

		for (int i = 0; i < baseClusterInfos.size(); i++) {
			ClusterInfo ci = baseClusterInfos.get(i);

			if (ci.Discovered()) {
				// The cluster has already been discovered
				// and is part of a connected component.
				continue;
			}

			// Start a new connect component.
			ArrayList<Cluster> component = new ArrayList<Cluster>();
			components.add(component);

			queue.clear();
			queue.add(ci);
			ci.SetDiscovered(true);
			FindComponent(queue, component);
		}

		// Unify the clusters from each connected component
		// into a single one and add them to the resulting list.
		ArrayList<Cluster> clusters = new ArrayList<Cluster>(components.size());

		for (int i = 0; i < components.size(); i++) {
			ArrayList<Cluster> component = components.get(i);
			clusters.add(Cluster.Merge(component));
		}

		return clusters;
	}

	// Finds a connected component. It is presumed that the start node
	// is already found in the specified queue.
	private void FindComponent(Queue<ClusterInfo> queue, ArrayList<Cluster> component) {
		while (queue.size() > 0) {
			ClusterInfo info = queue.poll();
			component.add(info.Cluster());

			// Add all neighbor nodes to the component.
			for (int i = 0; i < info.Edges().size(); i++) {
				ClusterInfo next = info.Edges().get(i).getOther();

				if (!next.Discovered()) {
					next.SetDiscovered(true);
					queue.add(next);
				}
			}
		}
	}
}
