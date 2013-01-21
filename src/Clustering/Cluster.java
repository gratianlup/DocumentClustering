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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public final class Cluster implements Comparable {
    private ArrayList<Document> documents_;
    private List<Phrase> phrases_;
    private double weight_;
    private String label_;

    /*
    * Constructors.
    */
    public Cluster(int docCapacity, int phraseCapacity) {
        documents_ = new ArrayList<Document>(docCapacity);
        phrases_ = new ArrayList<Phrase>(phraseCapacity);
    }

    public Cluster(Phrase phrase) {
        this(4, 1);
        phrases_.add(phrase);
    }

    /*
    * Public methods.
    */
    // Computes the weight of the clusters based on the contained documents.
    public void ComputeWeight() {
        // The weight is equal to the producet between the number of documents,
        // the (adjusted) length of the senteces and the sum of the weight
        // of each words part of the sentences.
        double wordWeight = 0;        
        int count = phrases_.size();
        
        for(int i = 0; i < count; i++) {
            wordWeight += phrases_.get(i).Weight();
        }

        weight_ = documents_.size() * PhrasesWeight() * wordWeight;
    }

    // Verifies if the cluster and the specified one are similar
    // (they haev some documents in common).
    public boolean IsSimilarTo(Cluster other, double overlapDegree) {
        assert(other != null);
        // ------------------------------------------------
        // Find the common documents. If there are only a few documents,
        // a linear search is used; otherwise the search uses a hash table.
        int common = 0;

        if((documents_.size() <= 3) &&
           (other.documents_.size() <= 3)) {
            // Few documents case, do a linear search.
            for(int i = 0; i < documents_.size(); i++) {
                Document doc = documents_.get(i);
                if(other.documents_.contains(doc)) {
                    common++;
                }
            }
        }
        else {
            // Many documents case, use a hash table.
            Hashtable<Document, Document> hash = new Hashtable<Document, Document>();
            int count = documents_.size();
            
            for(int i = 0; i < count; i++) {
                Document doc = documents_.get(i);
                hash.put(doc, doc);
            }

            // Check which of the documents from the other clusters
            // are found in the hash table.
            count = other.documents_.size();
            
            for(int i = 0; i < count; i++) {
                if(hash.containsKey(other.documents_.get(i))) {
                    common++;
                }
            }
        }

        return ((double)common / (double)documents_.size()) > overlapDegree &&
               ((double)common / (double)other.documents_.size()) > overlapDegree;
    }

    // Unifies all clusters from the specified list
    // into a single cluster containing the union of the documents.
    public static Cluster Merge(List<Cluster> clusters) {
        assert(clusters != null);
        // ------------------------------------------------
        ArrayList<Phrase> allPhrases = new ArrayList<Phrase>();
        Cluster newCluster = new Cluster(clusters.size() * 2,
                                         clusters.size());
        Hashtable<Document, Document> hash = new Hashtable<Document, Document>();

        // Each document must appear a single time in the new cluster
        // (the list must behave as a mathematical set).
        for(int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            List<Document> docs = cluster.Documents();

            for(int j = 0; j < docs.size(); j++) {
                Document doc = docs.get(j);
                hash.put(doc, doc);
            }

            // All sentences from the clusters must appear in the new one.
            List<Phrase> phrases = cluster.Phrases();
            
            for(int j = 0; j < phrases.size(); j++) {
                allPhrases.add(phrases.get(j));
            }
        }

        // Add the documents to the new cluster.
        Iterator<Document> docIt = hash.keySet().iterator();
        
        while(docIt.hasNext()) {
            newCluster.documents_.add(docIt.next());
        }

        // Associated the sentences with the new cluster.
        newCluster.SetPhrases(allPhrases);
        return newCluster;
    }

    public double Weight() { return weight_; }
    public List<Document> Documents() { return documents_; }

    public List<Phrase> Phrases() { return phrases_; }
    public void SetPhrases(List<Phrase> value) { phrases_ = value; }
    
    public String Label() { return label_; }
    public void SetLabel(String value) { label_ = value; }

    /*
    * Private methods.
    */
    private double PhrasesWeight() {
        double sum = 0;
        int count = phrases_.size();
        
        for(int i = 0; i < count; i++) {
            sum += phrases_.get(i).WordCount();            
        }

        if(sum < 2) {
            return 0.5;
        }
        else {
            return Math.min(6, sum);
        }
    }

    public int compareTo(Object obj) {
        // Used when sorting clusters based on their weight.
        if(this == obj) {
            return 0;
        }

        Cluster other = (Cluster)obj;
        
        if(weight_ < other.weight_) {
            return 1;
        }
        else if(weight_ > other.weight_) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
