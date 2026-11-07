package hai913i.tp2.output;

import hai913i.tp2.clustering.Cluster;
import hai913i.tp2.clustering.Clustering;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class DendrogramExport {

    private DendrogramExport() {
    }

    public static void writeText(Clustering clustering, Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# fusions : etape;similarite;groupeA;groupeB;nouveau\n");
        for (Clustering.Merge m : clustering.merges()) {
            sb.append(String.format(Locale.ROOT, "%d;%.4f;%s;%s;%d%n", m.step(), m.similarity(),
                    ClusteringPrint.members(clustering, m.a()), ClusteringPrint.members(clustering, m.b()),
                    m.result().id()));
        }
        sb.append("\n# arbre\n");
        indent(clustering, clustering.root(), 0, sb);
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private static void indent(Clustering clustering, Cluster g, int depth, StringBuilder sb) {
        sb.append("  ".repeat(depth));
        if (g.isLeaf()) {
            sb.append(String.format(Locale.ROOT, "[%d] %s%n", g.id(), clustering.classes().get(g.id())));
            return;
        }
        sb.append(String.format(Locale.ROOT, "[%d] %.4f %s%n", g.id(), g.similarity(),
                ClusteringPrint.members(clustering, g)));
        indent(clustering, g.left(), depth + 1, sb);
        indent(clustering, g.right(), depth + 1, sb);
    }

    // dot -Tpng dendrogramme.dot -o dendrogramme.png
    public static void writeDot(Clustering clustering, Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph dendrogramme {\n");
        sb.append("  rankdir=BT;\n");
        sb.append("  node [fontname=\"Helvetica\", fontsize=10];\n");
        for (int i = 0; i < clustering.classes().size(); i++) {
            sb.append(String.format("  n%d [shape=box, label=\"%s\"];%n", i,
                    GraphExport.shortName(clustering.classes().get(i))));
        }
        for (Clustering.Merge m : clustering.merges()) {
            Cluster g = m.result();
            sb.append(String.format(Locale.ROOT, "  n%d [shape=ellipse, label=\"%d. %.4f\"];%n",
                    g.id(), m.step(), m.similarity()));
            sb.append(String.format("  n%d -> n%d [dir=none];%n", m.a().id(), g.id()));
            sb.append(String.format("  n%d -> n%d [dir=none];%n", m.b().id(), g.id()));
        }
        sb.append("}\n");
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}
