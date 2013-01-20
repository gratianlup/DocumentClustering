// Copyright (c) Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
//       copyright notice, this list of conditions and the following
//       disclaimer in the documentation and/or other materials provided
//       with the distribution.
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

public class ClusterMerger {
    private class ClusterInfo {
        private Cluster cluster_;
        private boolean discovered_;
        private ArrayList<ClusterInfo> edges_;

        public ClusterInfo(Cluster cluster) {
            cluster_ = cluster;
            edges_ = new ArrayList<ClusterInfo>();
        }

        public Cluster Cluster() { return cluster_; }
        public boolean Discovered() { return discovered_; }
        public void SetDiscovered(boolean value) { discovered_ = value; }
        public List<ClusterInfo> Edges() { return edges_; }
    }

    /*
    * Membrii.
    */
    private ArrayList<ClusterInfo> clusters_;
    private double overlapDegree_;

    /*
    * Constructori.
    */
    public ClusterMerger(List<Cluster> clusters, double overlapDegree) {
        clusters_ = new ArrayList<ClusterInfo>();
        overlapDegree_ = overlapDegree;

        int count = clusters.size();
        for(int i = 0; i < count; i++) {
            clusters_.add(new ClusterInfo(clusters.get(i)));
        }
    }

    /*
    * Metode publice.
    */
    // Determina grupuri de clustere cu aceleasi documente.
    // Foloseste o parcurgere in latime pentru a gasi componentele conexe.
    public List<Cluster> MergeClusters() {
        // Identifica clusterele care au documente comune.
        ConnectClusters();

        // Fiecare grup de clustere similare se afla intr-o componenta conexa.
        // Identifica componentele conexe folosind cautarea in latime.
        Queue<ClusterInfo> queue = new LinkedList<ClusterInfo>();
        ArrayList<ArrayList<Cluster>> components =
                new ArrayList<ArrayList<Cluster>>();
        
        int count = clusters_.size();
        for(int i = 0; i < count; i++) {
            ClusterInfo cluster = clusters_.get(i);
            if(cluster.Discovered()) {
                // Cluster-ul a fost deja descoperit si face parte dintr-o componenta.
                continue;
            }

            // Se incepe o noua componenta conexa.
            ArrayList<Cluster> component = new ArrayList<Cluster>();
            components.add(component);

            queue.clear();
            queue.add(cluster);
            cluster.SetDiscovered(true);
            FindComponent(queue, component);
        }

        // Uneste cluster-ele din fiecare componenta.
        ArrayList<Cluster> clusters = new ArrayList<Cluster>(components.size());

        for(int i = 0; i < components.size(); i++) {
            ArrayList<Cluster> component = components.get(i);
            clusters.add(Cluster.Merge(component));
        }

        return clusters;
    }

    /*
    * Metode private.
    */
    // "Leaga" cluster-ele cu (aproximativ) aceleasi documente.
    private void ConnectClusters() {
        // Compara fiecare cluster cu toate celelalte si adauga
        // o muchie intre cele care sunt asemanatoare (creeaza un graf).
        int count = clusters_.size();
        for(int i = 0; i < count; i++) {
            for(int j = 0; j < count; j++) {
                if(i == j) {
                    continue;
                }

                ClusterInfo a = clusters_.get(i);
                ClusterInfo b = clusters_.get(j);
                if(a.Cluster().IsSimilarTo(b.Cluster(), overlapDegree_)) {
                    // Documentele sunt similare, leaga-le.
                    a.Edges().add(b);
                }
            }
        }
    }

    // Gaseste o componenta conexa. Se presupune ca cluster-ul de start
    // se afla deja adaugat in coada.
    private void FindComponent(Queue<ClusterInfo> queue,
                               ArrayList<Cluster> component) {
        while(queue.size() > 0) {
            ClusterInfo info = queue.poll();
            component.add(info.Cluster());

            // Adauga toate nodurile vecine.
            for(int i = 0; i < info.Edges().size(); i++) {
                ClusterInfo next = info.Edges().get(i);
                
                if(next.Discovered() == false) {
                    next.SetDiscovered(true);
                    queue.add(next);
                }
            }
        }
    }
}