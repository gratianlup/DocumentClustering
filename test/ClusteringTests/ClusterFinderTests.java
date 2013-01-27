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

package ClusteringTests;
import org.junit.Test;
import static org.junit.Assert.*;
import Clustering.*;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author Gratian2
 */
public class ClusterFinderTests {
    @Test
    public void Find() throws IOException {
        java.util.List<Cluster> list = ClusterFinder.Find(new TestSource("test.txt"), 0.3, 20, 0.01);
        int ct = 0;

        for(Cluster c : list) {
            System.out.println("\n\nCluster #" + ct++);
            System.out.println("=> Documente: ");
            for(Document d : c.Documents()) {
                System.out.print(d.Index() + ", ");
            }

            System.out.println("\n=> Fraze: ");
            for(Phrase p : c.Phrases()) {
                System.out.println(p.toString());
            }
        }

        JFrame frame = new JFrame("Clustering");
        ClusterViewer viewer = new ClusterViewer(ClusterFinder.ParseSource(new TestSource("test.txt")));

        frame.setBackground(Color.WHITE);
        viewer.setBackground(Color.WHITE);
        frame.setSize(1024, 800);
        frame.setContentPane(viewer);
        frame.setVisible(true);

        while(frame.isVisible()) {
            try {
                Thread.sleep(500);
            } catch(InterruptedException ex) {}
        }
    }
}
