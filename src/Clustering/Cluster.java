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
    * Constructori.
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
    * Metode publice.
    */
    // Calculeaza importanta unui cluster pe baza documentelor ce il definesc.
    public void ComputeWeight() {
        // Importanta unui cluster este data de produsul dintre numarul de documente,
        // lungimea (ajustata) a frazei si suma importantei cuvintelor ce compun frazele.
        double wordWeight = 0; // Importanta tuturor cuvintelor.

        
        int count = phrases_.size();
        for(int i = 0; i < count; i++) {
            wordWeight += phrases_.get(i).Weight();
        }

        weight_ = documents_.size() * PhrasesWeight() * wordWeight;
    }

    // Verifica daca cluster-ul curent si cel dat sunt similare.
    // (au o parte dintre documente in comun).
    public boolean IsSimilarTo(Cluster other, double overlapDegree) {
        assert(other != null);
        // ------------------------------------------------
        // Determina documentele comune.
        int common = 0;

        if((documents_.size() <= 3) &&
            (other.documents_.size() <= 3)) {
            // Foloseste o cautare simpla pentru putine documente.
            for(int i = 0; i < documents_.size(); i++) {
                Document doc = documents_.get(i);
                if(other.documents_.contains(doc)) {
                    common++;
                }
            }
        }
        else {
            // Foloseste o tabela hash si introduce documentele dintr-un cluster.
            Hashtable<Document, Document> hash = new Hashtable<Document, Document>();
            
            int count = documents_.size();
            for(int i = 0; i < count; i++) {
                Document doc = documents_.get(i);
                hash.put(doc, doc);
            }

            // Verifica care dintre documentele celuilalt cluster se afla in tabela.
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

    // Uneste toate cluster-ele date.
    public static Cluster Merge(List<Cluster> clusters) {
        assert(clusters != null);
        // ------------------------------------------------
        ArrayList<Phrase> allPhrases = new ArrayList<Phrase>();
        Cluster newCluster = new Cluster(clusters.size() * 2,
                                         clusters.size());
        Hashtable<Document, Document> hash = new Hashtable<Document, Document>();

        // Fiecare document apare o singura data in noul cluster.
        for(int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            List<Document> docs = cluster.Documents();

            for(int j = 0; j < docs.size(); j++) {
                Document doc = docs.get(j);
                hash.put(doc, doc);
            }

            // Toate frazele din clusterele primite vor aparea in cel nou.
            List<Phrase> phrases = cluster.Phrases();
            for(int j = 0; j < phrases.size(); j++) {
                allPhrases.add(phrases.get(j));
            }
        }

        // Adauga documentele la noul cluster.
        Iterator<Document> docIt = hash.keySet().iterator();
        while(docIt.hasNext()) {
            newCluster.documents_.add(docIt.next());
        }

        // Seteaza lista cu fraze.
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
    * Metode private.
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

    // Folosit la sortarea cluster-elor.
    public int compareTo(Object obj) {
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