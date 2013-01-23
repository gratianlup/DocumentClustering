// Copyright (c) Gratian Lup. All rights reserved.
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
import java.util.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.DecimalFormat;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class ClusterViewer extends JPanel {
    class NodeInfo {
        public int Width;
        public int X;
        public int Y;
        public SuffixTree.Node Node;
        public Color Color;
        public ArrayList<NodeInfo> Children;
        public String Text;

        public NodeInfo() {
            Children = new ArrayList<NodeInfo>();
        }
    }

    private static final int Y_DISTANCE = 100;
    private static final int X_DISTANCE = 50;
    private static final int SIZE = 24;

    private static final Color LEAF_COLOR = new Color(200, 200, 200);
    private static final Color COLORS[] = new Color[] {
        new Color(255, 170, 85),
        new Color(255, 130, 140),
        new Color(226, 130, 210),
        new Color(173, 158, 218),
        new Color(141, 201, 232),
        new Color(108, 219, 214),
        new Color(201, 227, 156),
        new Color(250, 224, 78)
    };

    private NodeInfo root;


    public ClusterViewer(SuffixTree.Node root) {
        LayoutNodes(root, 0);
    }

    private NodeInfo LayoutNodes(SuffixTree.Node node, int level) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.Node = node;
        nodeInfo.Color = COLORS[level];

        if(node.IsLeaf()) {
            nodeInfo.Color = LEAF_COLOR;
        }

        if(level == 0) {
            root = nodeInfo;
        }

        int width = 0;
        Iterator<SuffixTree.Edge> edgeIt = node.Edges();
        
        while(edgeIt.hasNext()) {
            SuffixTree.Edge edge = edgeIt.next();
            NodeInfo child = LayoutNodes(edge.NextNode(), level + 1);
            nodeInfo.Children.add(child);
            
            if(edge.NextNode().IsLeaf()) {
                width += SIZE;
            }
            else {
                width += child.Width;
            }

            width += X_DISTANCE;
            child.Text = edge.toString();
        }

        nodeInfo.Width = node.IsLeaf() ? SIZE : width / 2;
        int x = SIZE / 2;
        
        for(int i = 0; i < nodeInfo.Children.size(); i++) {
            NodeInfo child = nodeInfo.Children.get(i);
            child.Y = SIZE;
            child.X = x - (width / 2) + child.Width / 2;
            x += child.Width + X_DISTANCE;
        }
        
        return nodeInfo;
    }

    private void DrawNodes(NodeInfo node, int nodeX, int nodeY, Graphics2D g) {
        for(int i = 0; i < node.Children.size(); i++) {
            g.setColor(Color.BLACK);
            NodeInfo child = node.Children.get(i);
            int childX = nodeX + child.X;
            int childY = nodeY + child.Y + Y_DISTANCE;
            
            g.drawLine(nodeX, nodeY, childX, childY);
            DrawNodes(child, childX, childY, g);
        }

        g.setColor(node.Color);
        g.fillOval(nodeX - SIZE / 2, nodeY - SIZE / 2, SIZE, SIZE);

        for(int i = 0; i < node.Children.size(); i++) {
            g.setColor(Color.BLACK);
            NodeInfo child = node.Children.get(i);

            int childX = nodeX + child.X;
            int childY = nodeY + child.Y + Y_DISTANCE;

            int middleX = (int)(nodeX +(childX - nodeX) * 0.5);
            int middleY = (int)(nodeY +(childY - nodeY) * 0.5);
            double angle = Math.atan2(childY - nodeY, childX - nodeX);
            
            if(Math.abs(angle) > 1.68) {
                middleY = (int)(nodeY +(childY - nodeY) * 0.5);
                angle += Math.PI;
            }

            FontMetrics metrics = g.getFontMetrics(this.getFont());
            AffineTransform prevTransf = g.getTransform();
            g.translate(middleX, middleY);
            g.rotate(angle);

            g.drawString(child.Text, -metrics.stringWidth(child.Text) / 2 - 15, -5);
            g.setTransform(prevTransf);
        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.clearRect(0, 0, getSize().width, getSize().height);

        int startX = getSize().width / 2;
        int startY = SIZE * 2;
        DrawNodes(root, startX, startY, g);
    }
}
