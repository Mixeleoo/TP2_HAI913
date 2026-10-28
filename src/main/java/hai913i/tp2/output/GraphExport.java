package hai913i.tp2.output;

import hai913i.tp2.graph.CouplingGraph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Export du graphe de couplage : un CSV que le programme sait relire, et un DOT pour Graphviz.
//
// Format CSV (séparateur ;, UTF-8, une ligne d'en-tête) :
//   type;classeA;classeB;arcs;poids
//   noeud;library.model.Dvd;;;                       une ligne par classe, isolées comprises
//   arete;library.model.Item;library.service.LoanService;4;0.10810810810810811
// Le poids est écrit avec Double.toString : il se relit à l'identique, au dernier bit près.
public final class GraphExport {

    public static final String HEADER = "type;classeA;classeB;arcs;poids";

    private GraphExport() {
    }

    public static void writeCsv(CouplingGraph graph, Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (String n : graph.nodes())
            lines.add("noeud;" + n + ";;;");
        for (CouplingGraph.Edge e : graph.edges())
            lines.add("arete;" + e.a() + ";" + e.b() + ";" + e.arcs() + ";" + Double.toString(e.weight()));
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public static CouplingGraph readCsv(Path file) throws IOException {
        if (!Files.isRegularFile(file))
            throw new IOException("fichier introuvable : " + file.toAbsolutePath());
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).strip().equals(HEADER))
            throw new IOException(file.getFileName() + " : en-tête attendu « " + HEADER + " »");

        List<String> nodes = new ArrayList<>();
        List<CouplingGraph.Edge> edges = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty())
                continue;
            String[] f = line.split(";", -1);
            String where = file.getFileName() + " ligne " + (i + 1) + " : ";
            if (f.length != 5)
                throw new IOException(where + "5 colonnes attendues, " + f.length + " trouvées");
            switch (f[0]) {
                case "noeud" -> nodes.add(f[1]);
                case "arete" -> {
                    int arcs;
                    double weight;
                    try {
                        arcs = Integer.parseInt(f[3]);
                        weight = Double.parseDouble(f[4]);
                    } catch (NumberFormatException e) {
                        throw new IOException(where + "nombre invalide");
                    }
                    if (arcs <= 0 || weight <= 0.0 || weight > 1.0)
                        throw new IOException(where + "une arête a au moins un arc et un poids dans ]0, 1]");
                    edges.add(new CouplingGraph.Edge(f[1], f[2], arcs, weight));
                }
                default -> throw new IOException(where + "type inconnu « " + f[0] + " »");
            }
        }
        try {
            return new CouplingGraph(nodes, edges);
        } catch (IllegalArgumentException e) {
            throw new IOException(file.getFileName() + " : " + e.getMessage());
        }
    }

    // dot -Tpng graphe-couplage.dot -o graphe-couplage.png
    public static void writeDot(CouplingGraph graph, Path file) throws IOException {
        int total = graph.totalArcs();
        Set<String> isolated = new HashSet<>(graph.isolated());
        StringBuilder sb = new StringBuilder();
        sb.append("// Graphe de couplage pondéré : poids = C(A, B) = r(A, B) / |E_inter|, |E_inter| = ")
          .append(total).append('\n');
        sb.append("graph couplage {\n");
        sb.append("  layout=neato; overlap=false; splines=true; sep=\"+25\";\n");
        sb.append("  node [shape=box, style=rounded, fontname=\"Helvetica\", fontsize=11];\n");
        sb.append("  edge [len=2.2, fontname=\"Helvetica\", fontsize=9];\n");
        for (String n : graph.nodes()) {
            sb.append("  \"").append(n).append("\" [label=\"").append(shortName(n)).append("\"");
            if (isolated.contains(n))
                sb.append(", style=\"rounded,dashed\", fontcolor=gray40, color=gray40");
            sb.append("];\n");
        }
        for (CouplingGraph.Edge e : graph.edges()) {
            sb.append(String.format(Locale.ROOT,
                    "  \"%s\" -- \"%s\" [label=\"%d/%d\\n%.4f\", penwidth=%d];%n",
                    e.a(), e.b(), e.arcs(), total, e.weight(), e.arcs()));
        }
        sb.append("}\n");
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    // library.service.LoanService.Loan -> LoanService.Loan : on retire les segments du paquetage (en minuscules)
    static String shortName(String qualified) {
        String[] parts = qualified.split("\\.");
        int i = 0;
        while (i < parts.length - 1 && !parts[i].isEmpty() && Character.isLowerCase(parts[i].charAt(0)))
            i++;
        return String.join(".", Arrays.copyOfRange(parts, i, parts.length));
    }
}
