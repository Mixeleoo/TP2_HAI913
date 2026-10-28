package hai913i.tp2.output;

import hai913i.tp2.graph.CouplingGraph;

import java.io.PrintStream;
import java.util.Locale;

// Restitution de l'étape A2 dans le terminal
public final class GraphPrint {

    private GraphPrint() {
    }

    public static void print(CouplingGraph graph, PrintStream out) {
        out.println("=== A2 : graphe de couplage pondéré ===");
        out.printf(Locale.ROOT, "Nœuds                 : %d%n", graph.nodes().size());
        out.printf(Locale.ROOT, "Arêtes                : %d%n", graph.edges().size());
        out.println();

        int total = graph.totalArcs();
        out.println("Les cinq arêtes les plus lourdes :");
        for (CouplingGraph.Edge e : graph.heaviest(5)) {
            out.printf(Locale.ROOT, "  %d/%d = %.4f   %s -- %s%n",
                    e.arcs(), total, e.weight(), e.a(), e.b());
        }
        out.println();

        out.printf(Locale.ROOT, "Classes isolées (%d) :%n", graph.isolated().size());
        if (graph.isolated().isEmpty())
            out.println("  (aucune)");
        for (String n : graph.isolated())
            out.println("  " + n);
        out.println();
    }
}
