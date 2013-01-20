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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class SuffixTree {
    // Reprezinta un nod in arbore. Este de tip frunza daca nu are muchii.
    public final class Node {
        // Suffix node reprezinta primul sufix al textului format prin parcurgerea
        // drumului de la radacina pana la nodul actual. Este punctul din arbore
        // in care trebuie sa se faca urmatoarea insertie.
        private Node suffixNode_;
        private HashMap<Word, Edge> edges_; // Muchiile care apartin de nod.

        /*
        * Constructori.
        */
        public Node() {
            edges_ = new HashMap<Word, Edge>(4);
        }

        /*
        * Metode publice.
        */
        public Node SuffixNode() { return suffixNode_; }
        public void SetSuffixNode(Node value) { suffixNode_ = value; }
        public Iterator<Edge> Edges() { return edges_.values().iterator(); }

        public boolean HasEdge(Word word) {
            return edges_.containsKey(word);
        }

        public void AddEdge(Word word, Edge edge) {
            edges_.put(word, edge);
        }

        public Edge GetEdge(Word word) {
            return edges_.get(word);
        }

        public boolean IsLeaf() { 
            return edges_.isEmpty(); 
        }

        @Override
        public String toString() {
            return IsLeaf() ? "Leaf" : "Edges: " + Integer.toString(edges_.size());
        }
    }

    // Reprezinta o muchie. Cuvintele asociate sunt stocate sub forma de indici.
    public final class Edge {
        private Document document_; // Documentul de care cuvintele apartin.
        private int firstIndex_;    // Primul cuvant din document aflat pe muchie.
        private int lastIndex_;     // Ultimul cuvant din document.ls
        private Node prevNode_;     // Nodurile de care apartine muchia.
        private Node nextNode_;

        /*
        * Constructori.
        */
        public Edge(Document doc, int first, int last,
                    Node previous, Node next) {
            document_ = doc;
            firstIndex_ = first;
            lastIndex_ = last;
            prevNode_ = previous;
            nextNode_ = next;
        }

        /*
        * Metode publice.
        */
        public Document Document() { return document_; }

        public int FirstIndex() { return firstIndex_; }
        public int LastIndex() { return lastIndex_; }

        public void SetFirstIndex(int value) { firstIndex_ = value; }
        public void SetLastIndex(int value) { lastIndex_ = value; }
        
        public Node PreviousNode() { return prevNode_; }
        public void SetPreviousNode(Node value) { prevNode_ = value; }

        public Node NextNode() { return nextNode_; }
        public void SetNextNode(Node value) { nextNode_ = value; }

        public int Span() {
            return lastIndex_ - firstIndex_;
        }

        @Override
        public String toString() {
            String temp = "";
            for(int i = firstIndex_; i <= lastIndex_; i++) {
                temp += tempDoc.WordAt(i).Word() + " ";
            }

            return temp;
        }
    }

    // Reprezinta un sufix (relativ la un nod de origine).
    // Folosit la adaugare.
    public final class Suffix {
        private Node origin_;
        private int firstIndex_;
        private int lastIndex_;

        /*
        * Constructori.
        */
        public Suffix() {}
        public Suffix(Node origin, int first, int last) {
            origin_ = origin;
            firstIndex_ = first;
            lastIndex_ = last;
        }

        /*
        * Metode publice.
        */
        public Node Origin() { return origin_; }
        public void SetOrigin(Node value) { origin_ = value; }

        public int FirstIndex() { return firstIndex_; }
        public void SetFirstIndex(int value) { firstIndex_ = value; }

        public int LastIndex() { return lastIndex_; }
        public void SetLastIndex(int value) { lastIndex_ = value; }

        public boolean IsExplicit() {
            return firstIndex_ > lastIndex_;
        }

        public boolean IsImplicit() {
            return firstIndex_ <= lastIndex_;
        }

        public int Span() {
            return lastIndex_ - firstIndex_;
        }
    }

    /*
    * Membrii.
    */
    private Suffix activePoint_;
    private Node root_;
    private int phreases_;
    private Document tempDoc;

    /*
    * Constructori.
    */
    public SuffixTree() {
        root_ = new Node();
        tempDoc = new Document(0);
    }

    /*
    * Metode publice.
    */
    public void AddSentence(Document document, int start, int end) {
        assert(document != null);
        assert(start >= 0 && start <= end);
        // ------------------------------------------------
        if(phreases_ == 0) {
            activePoint_ = new Suffix(root_, 0, -1);
        }

//        if(phreases_ > 0) {
//            // Una sau mai mult fraze se afla deja in arbore. Trebuie sa avem grija
//            // sa nu reintroducem prefixele aflate deja in arbore.
//            // Se avanseaza sufixul pana se intalneste primul cuvant
//            // care nu mai este in arbore (este garantat ca se opreste cautarea
//            // deoarece marcaturul de sfarsit este unic pentru fiecare fraza).
//            //activePoint_.SetLastIndex(0);
//            int pos = start;
//            boolean ok = true;
//
//            while((pos < end) && ok) {
//                Word word = document.WordAt(pos);
//
//                if(activePoint_.Origin().HasEdge(word)) {
//                    // Potriveste cuvintele de pe muchie cu fraza de adaugat.
//                    Edge edge = activePoint_.Origin().GetEdge(word);
//                    int span = edge.Span() + 1;
//                    int edgePos = 0;
//
//                    while(edgePos < span) {
//                        Word a = document.WordAt(pos + edgePos);
//                        Word b = document.WordAt(edge.FirstIndex() + edgePos);
//                        edgePos++;
//
//                        if(a.equals(b)) {
//                            activePoint_.SetFirstIndex(activePoint_.FirstIndex() + 1);
//                            start++;
//                        }
//                        else {
//                            ok = false;
//                            break;
//                        }
//                    }
//                }
//                else break;
//            }
//        }

        // Adauga fraza primita (se presupune ca include marcatorul de sfarsit).
        int oldCount = tempDoc.Count();

        for(int i = start; i < end; i++) {
            tempDoc.AddWord(document.WordAt(i));
            AddWord(tempDoc.Count() - 1, document, oldCount + (end - start));
        }

        phreases_++;
    }

    // Returneaza o lista cu toate cluster-ele de baza.
    public List<Cluster> GetBaseClusters(double minWeight) {
        ArrayList<Cluster> clusters =  new ArrayList<Cluster>();
        ArrayList<Edge> edges = new ArrayList<Edge>();

        // Cauta clustere pe toate ramurile care pornesc din radacina.
        Iterator<Edge> edgeIt = root_.Edges();
        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            edges.add(edge);
            
            if(edge.NextNode().IsLeaf() == false) {
                GetBaseClustersImpl(edge.NextNode(), clusters, edges, minWeight);
            }

            edges.remove(edges.size() - 1);
        }

        return clusters;
    }

    public Node Root() { return root_; }

    /*
    * Metode private.
    */
    private void AddWord(int wordIndex, Document document, int maxIndex) {
        Node parent = null;
        Node lastParent = null; // Folosit pentru a creea link-uri intre noduri.
        Word word = tempDoc.WordAt(wordIndex);
        
        // Se adauga o muchie (daca este necesar) la toate nodurile intre
        // cel activ si cel de sfarsit. Nodul activ este primul care nu este
        // de tip frunza (un nod care este frunza nu-si va mai schimba niciodata
        // tipul si va fi practic ignorat in pasii ulteriori).
        // Nodul de sfarsit este primul pentru care nu mai trebuie adaugata
        // muchia (si nici pentru succesori, deoarece acetia sunt sufixe pentru
        // nodul de sfarsit si contin deja muchia).
        while(true) {
            parent = activePoint_.Origin();

            // Daca nodul este explicit (are deja muchii) verificam daca
            // trebuie adaugata o muchie avand cuvantul curent.
            if(activePoint_.IsExplicit()) {
                if(parent.HasEdge(word)) {
                    break; // Muchia exista.
                }
            }
            else if(activePoint_.IsImplicit()) {
                // Muchia trebuie impartita pentru a se putea adauga cuvantul.
                Edge edge = parent.GetEdge(tempDoc.WordAt(activePoint_.firstIndex_));
                if(tempDoc.WordAt(edge.FirstIndex() + activePoint_.Span() + 1).equals(word)) {
                    // Cuvantul este deja plasat corespunzator.
                    break;
                }
                
                parent = SplitEdge(edge, activePoint_, document);
            }

            // Muchia nu a fost gasita, deci trebuie creata acuma.
            // Deasemenea, noul nod trebuie legat de ultimul nod vizitat.
            Node newNode = new Node();
            Edge newEdge = new Edge(document, wordIndex, maxIndex - 1,
                                    parent, newNode);
            parent.AddEdge(word, newEdge);

            if((lastParent != null) && (lastParent != root_)) {
                lastParent.SetSuffixNode(parent);
            }
            lastParent = parent;

            // Stabileste urmatorul sufix.
            if(activePoint_.Origin() == root_) {
                // Daca punctul activ este chiar radacina se trece
                // la urmatorul sufix in mod normal.
                activePoint_.SetFirstIndex(activePoint_.FirstIndex() + 1);
            }
            else {
                // Se foloseste un link pentru noduri interioare.
                activePoint_.SetOrigin(activePoint_.Origin().SuffixNode());
            }

            // Sufixul trebuie ajustat la fiecare schimbare.
            MakeCanonic(activePoint_);
        }

        // Leaga ultimul nod parinte.
        if((lastParent != null) && (lastParent != root_)) {
            lastParent.SetSuffixNode(parent);
        }

        // Punctul de sfarsit devine punct activ pentru pasul urmator.
        activePoint_.SetLastIndex(activePoint_.LastIndex() + 1);
        MakeCanonic(activePoint_);
    }

    // Imparte muchia in doua muchii si creeaza un nod care le leaga.
    // Se creaza o noua muche care va ramane cu cuvintele de la inceput.
    private Node SplitEdge(Edge edge, Suffix suffix, Document document) {
        Node newNode = new Node();
        Edge newEdge = new Edge(document, edge.FirstIndex(),
                                edge.FirstIndex() + suffix.Span(),
                                suffix.Origin(), newNode);

        // Inlocuieste vechea muchie cu cea noua.
        suffix.Origin().AddEdge(tempDoc.WordAt(edge.FirstIndex()), newEdge);
        newNode.SetSuffixNode(suffix.Origin());

        // Ajusteaza vechea muchie (nodul asociat ramane tot frunza).
        edge.SetFirstIndex(edge.FirstIndex() + suffix.Span() + 1);
        edge.SetPreviousNode(newNode);
        newNode.AddEdge(tempDoc.WordAt(edge.FirstIndex()), edge);

        return newNode;
    }

    // Avanseaza in arbore pana se gaseste cel mai apropiat nod
    // de sfarsitul sufixului.
    private void MakeCanonic(Suffix suffix) {
        if(suffix.IsExplicit()) return;
        
        Word word = tempDoc.WordAt(suffix.FirstIndex());
        Edge edge = suffix.Origin().GetEdge(word);
        
        while(edge.Span() <= suffix.Span()) {
            suffix.SetFirstIndex(suffix.FirstIndex() + edge.Span() + 1);
            suffix.SetOrigin(edge.NextNode());

            if(suffix.FirstIndex() <= suffix.LastIndex()) {
                // Se poate continua.
                word = tempDoc.WordAt(suffix.firstIndex_);
                edge = suffix.Origin().GetEdge(word);
            }
        }
    }

    private Phrase MakePhrase(List<Edge> edges) {
        Phrase phrase = new Phrase();

        for(int i = 0; i < edges.size(); i++) {
            SuffixTree.Edge edge = edges.get(i);

            for(int j = edge.FirstIndex(); j <= edge.LastIndex(); j++) {
                phrase.Words().add(tempDoc.WordAt(j));
            }
        }

        return phrase;
    }

    private Cluster GetBaseClustersImpl(Node node, List<Cluster> clusters,
                                        List<Edge> edges, double minWeight) {
        assert(node.IsLeaf() == false);
        assert(edges.size() > 0);
        // ------------------------------------------------
        // Creeaza un nou cluster si seteaza fraza asociata.
        Cluster cluster = new Cluster(MakePhrase(edges));

        Iterator<Edge> edgeIt = node.Edges();
        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            
            Node nextNode = edge.NextNode();
            if(nextNode.IsLeaf()) {
                // Adauga documentul la cluster.
                if(cluster.Documents().contains(edge.Document()) == false) {
                    cluster.Documents().add(edge.Document());
                }
            }
            else {
                // Muchia duce spre un nod intern; toate documentele care
                // apartin cluster-ului asociat cu nodul intern vor fi adaugate
                // si la clustet-ul curent.
                edges.add(edge);
                Cluster child = GetBaseClustersImpl(nextNode, clusters, edges, minWeight);
                edges.remove(edges.size() - 1);
                
                int count = child.Documents().size();
                for(int i = 0; i < count; i++) {
                    Document doc = child.Documents().get(i);

                    if(cluster.Documents().contains(doc) == false) {
                        cluster.Documents().add(doc);
                    }
                }
            }
        }

        // Cluster-ul este ales doar daca importanta sa depaseste pe cea minima.
        cluster.ComputeWeight();
        if(cluster.Weight() > minWeight) {
            clusters.add(cluster);
        }
        
        return cluster;
    }
}