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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class SuffixTree {
    // Represents a node in the tree.
    // It is a leaf if it has no child nodes.
    public final class Node {
        // The suffix node is the last node of the suffix obtained
        // by considering all nodes from the root to it.
        // It is the point where the next insertion must be made.
        private Node suffixNode_;
        private HashMap<Word, Edge> edges_; // The edges to the child nodes.

        /*
        * Constructors.
        */
        public Node() {
            edges_ = new HashMap<Word, Edge>(4);
        }

        /*
        * Public methods.
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

    // Represents an edge connecting two tree nodes.
    // The associated words are stored as indices in the associated document.
    public final class Edge {
        private Document document_; // The document containing the words.
        private int firstIndex_;    // The index of the first word found on the edge.
        private int lastIndex_;     // The index of the last word found on the edge.
        private Node prevNode_;     // The first node connected by the edge.
        private Node nextNode_;     // The second node connected by the edge.

        /*
        * Constructors.
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
        * Public methods.
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
                temp += tempDoc.WordAt(i).GetWord() + " ";
            }

            return temp;
        }
    }

    // Represents a suffix. Used while building the suffix tree.
    public final class Suffix {
        private Node origin_;
        private int firstIndex_;
        private int lastIndex_;

        /*
        * Constructors.
        */
        public Suffix() {}
        public Suffix(Node origin, int first, int last) {
            origin_ = origin;
            firstIndex_ = first;
            lastIndex_ = last;
        }

        /*
        * Public methods.
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
    * Private members.
    */
    private Suffix activePoint_;
    private Node root_;
    private int phreases_;
    private Document tempDoc;

    /*
    * Constructors.
    */
    public SuffixTree() {
        root_ = new Node();
        tempDoc = new Document(0);
    }

    /*
    * Public methods.
    */
    public void AddSentence(Document document, int start, int end) {
        assert(document != null);
        assert(start >= 0 && start <= end);
        // ------------------------------------------------
        if(phreases_ == 0) {
            activePoint_ = new Suffix(root_, 0, -1);
        }

        // Add the sentence (it is presumed that it includes the terminator).
        int oldCount = tempDoc.Count();

        for(int i = start; i < end; i++) {
            tempDoc.AddWord(document.WordAt(i));
            AddWord(tempDoc.Count() - 1, document, oldCount + (end - start));
        }

        phreases_++;
    }

    // Returns a list with all base clusters
    // having a weight at lest equal to the specified one.
    public List<Cluster> GetBaseClusters(double minWeight) {
        ArrayList<Cluster> clusters =  new ArrayList<Cluster>();
        ArrayList<Edge> edges = new ArrayList<Edge>();

        // Search the clusters on all edges originating from the root.
        Iterator<Edge> edgeIt = root_.Edges();
        
        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            edges.add(edge);
            
            if(!edge.NextNode().IsLeaf()) {
                GetBaseClustersImpl(edge.NextNode(), clusters, edges, minWeight);
            }

            edges.remove(edges.size() - 1);
        }

        return clusters;
    }

    public Node Root() { return root_; }

    /*
    * Private methods.
    */
    private void AddWord(int wordIndex, Document document, int maxIndex) {
        Node parent = null;
        Node lastParent = null; // Used to create links between the nodes.
        Word word = tempDoc.WordAt(wordIndex);
        
        // An edge is added (if necessary) for all nodes found
        // between the active one and the last one. The active node
        // is the first node which is not a leaf (a leaf node will never
        // change its type again and will be ignored in the next steps).
        // The end node is the first node for which an edge must not be added
        // (and the same for its successors, because they are suffixes for
        //  the end node and already have the required edges).
        while(true) {
            parent = activePoint_.Origin();

            // If the node is explicit (already has edges) check if
            // an edge labeled with the current word must be added.
            if(activePoint_.IsExplicit()) {
                if(parent.HasEdge(word)) {
                    break; // The word is already added to an edge.
                }
            }
            else if(activePoint_.IsImplicit()) {
                // The edge must be split before the word can be added.
                Edge edge = parent.GetEdge(tempDoc.WordAt(activePoint_.firstIndex_));
                
                if(tempDoc.WordAt(edge.FirstIndex() + activePoint_.Span() + 1).equals(word)) {
                    // The word is already in the right place.
                    break;
                }
                
                parent = SplitEdge(edge, activePoint_, document);
            }

            // The edge could not be found, it must be created now.
            // At the same time, the new node must be connected to the last visited one.
            Node newNode = new Node();
            Edge newEdge = new Edge(document, wordIndex, maxIndex - 1,
                                    parent, newNode);
            parent.AddEdge(word, newEdge);

            if((lastParent != null) && (lastParent != root_)) {
                lastParent.SetSuffixNode(parent);
            }
            lastParent = parent;

            // Figure out the next suffix.
            if(activePoint_.Origin() == root_) {
                // If the active node is the root of the tree
                // the next suffix follows the natural order.
                activePoint_.SetFirstIndex(activePoint_.FirstIndex() + 1);
            }
            else {
                // For internal nodes a link is used.
                activePoint_.SetOrigin(activePoint_.Origin().SuffixNode());
            }

            // The suffix must be adjusted at each update.
            MakeCanonic(activePoint_);
        }

        // Connect the last node to its parent.
        if((lastParent != null) && (lastParent != root_)) {
            lastParent.SetSuffixNode(parent);
        }

        // The end point becomes the active point for the next step.
        activePoint_.SetLastIndex(activePoint_.LastIndex() + 1);
        MakeCanonic(activePoint_);
    }

    // Splits the edge in two and creates a node that connects them.
    // A new edge which remains with the prefix is created.
    private Node SplitEdge(Edge edge, Suffix suffix, Document document) {
        Node newNode = new Node();
        Edge newEdge = new Edge(document, edge.FirstIndex(),
                                edge.FirstIndex() + suffix.Span(),
                                suffix.Origin(), newNode);

        // Replace the old edge with the new one.
        suffix.Origin().AddEdge(tempDoc.WordAt(edge.FirstIndex()), newEdge);
        newNode.SetSuffixNode(suffix.Origin());

        // Adjust the new edge (the associated node remains a leaf).
        edge.SetFirstIndex(edge.FirstIndex() + suffix.Span() + 1);
        edge.SetPreviousNode(newNode);
        newNode.AddEdge(tempDoc.WordAt(edge.FirstIndex()), edge);
        return newNode;
    }

    // Advance in the suffix tree until the closest node
    // to the end of the suffix is found.
    private void MakeCanonic(Suffix suffix) {
        if(suffix.IsExplicit()) {
            return; 
        }
        
        Word word = tempDoc.WordAt(suffix.FirstIndex());
        Edge edge = suffix.Origin().GetEdge(word);
        
        while(edge.Span() <= suffix.Span()) {
            suffix.SetFirstIndex(suffix.FirstIndex() + edge.Span() + 1);
            suffix.SetOrigin(edge.NextNode());

            if(suffix.FirstIndex() <= suffix.LastIndex()) {
                // Search can continue at the next level.
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
        assert(!node.IsLeaf());
        assert(edges.size() > 0);
        // ------------------------------------------------
        // Create a new cluster and set the associated sentence.
        Cluster cluster = new Cluster(MakePhrase(edges));
        Iterator<Edge> edgeIt = node.Edges();
        
        while(edgeIt.hasNext()) {
            Edge edge = edgeIt.next();
            Node nextNode = edge.NextNode();
            
            if(nextNode.IsLeaf()) {
                // Add the document to the cluster.
                if(!cluster.Documents().contains(edge.Document())) {
                    cluster.Documents().add(edge.Document());
                }
            }
            else {
                // The edge leads to an internal node.
                // All documents that belong to the cluster associated
                // with this internal node must be added to the current cluster.
                edges.add(edge);
                Cluster child = GetBaseClustersImpl(nextNode, clusters, edges, minWeight);
                edges.remove(edges.size() - 1);
                int count = child.Documents().size();
                
                for(int i = 0; i < count; i++) {
                    Document doc = child.Documents().get(i);

                    if(!cluster.Documents().contains(doc)) {
                        cluster.Documents().add(doc);
                    }
                }
            }
        }

        // The cluster is selected only if its weight
        // is at least equal to the minimum requested weight.
        cluster.ComputeWeight();
        
        if(cluster.Weight() > minWeight) {
            clusters.add(cluster);
        }
        
        return cluster;
    }
}
