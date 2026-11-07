package hai913i.tp2.output;

import hai913i.tp2.clustering.Cluster;
import hai913i.tp2.clustering.Clustering;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClusteringPrint {

    private ClusteringPrint() {
    }

    public static void print(Clustering clustering, PrintStream out) {
        List<Clustering.Merge> merges = clustering.merges();
        long withTies = merges.stream().filter(m -> m.tied() > 1).count();

        out.println("=== B1 : regroupement hiérarchique ascendant (lien moyen) ===");
        out.printf(Locale.ROOT, "Classes (M)           : %d%n", clustering.classes().size());
        out.printf(Locale.ROOT, "Fusions               : %d (M - 1 = %d)%n",
                merges.size(), clustering.classes().size() - 1);
        out.printf(Locale.ROOT, "Similarités           : %s%n",
                clustering.isNonIncreasing() ? "jamais croissantes" : "CROISSANTES à une étape");
        out.printf(Locale.ROOT, "Étapes avec égalité   : %d%n", withTies);
        out.println();

        out.println("Fusions (identifiants entre crochets ; ex aequo = couples à égalité au maximum) :");
        for (Clustering.Merge m : merges) {
            out.printf(Locale.ROOT, "  %2d.  %.4f  [%d] + [%d] -> [%d]%s%n",
                    m.step(), m.similarity(), m.a().id(), m.b().id(), m.result().id(),
                    m.tied() > 1 ? "   (" + m.tied() + " ex aequo)" : "");
            out.println("         " + members(clustering, m.a()));
            out.println("       + " + members(clustering, m.b()));
        }
        out.println();
    }

    static String members(Clustering clustering, Cluster g) {
        List<String> names = new ArrayList<>();
        for (String n : clustering.names(g))
            names.add(GraphExport.shortName(n));
        return "{" + String.join(", ", names) + "}";
    }
}
