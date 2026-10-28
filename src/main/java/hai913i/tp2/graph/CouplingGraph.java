package hai913i.tp2.graph;

import hai913i.tp2.coupling.Coupling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Étape A2 : un nœud par classe, une arête non orientée par couple de couplage non nul, de poids C(A, B).
// Seuls les couples non nuls sont stockés : une arête absente a un poids de 0.
public final class CouplingGraph {

    // Arête non orientée, a avant b dans l'ordre alphabétique ; arcs = r(A, B), weight = C(A, B)
    public record Edge(String a, String b, int arcs, double weight) {}

    // poids décroissant, puis noms croissants : le même ordre à chaque exécution
    private static final Comparator<Edge> ORDER = Comparator
            .comparingDouble(Edge::weight).reversed()
            .thenComparing(Edge::a)
            .thenComparing(Edge::b);

    private final List<String> nodes;
    private final List<Edge> edges;
    // clé "a|b" avec a < b -> arête
    private final Map<String, Edge> byPair = new HashMap<>();

    public CouplingGraph(List<String> nodes, List<Edge> edges) {
        List<String> sortedNodes = new ArrayList<>(nodes);
        sortedNodes.sort(Comparator.naturalOrder());
        this.nodes = List.copyOf(sortedNodes);
        Set<String> known = new HashSet<>(nodes);
        if (known.size() != nodes.size())
            throw new IllegalArgumentException("nœud en double");

        List<Edge> normalized = new ArrayList<>();
        for (Edge e : edges) {
            if (!known.contains(e.a()) || !known.contains(e.b()))
                throw new IllegalArgumentException("arête vers un nœud inconnu : " + e.a() + " -- " + e.b());
            if (e.a().equals(e.b()))
                throw new IllegalArgumentException("boucle sur " + e.a() + " : le couplage d'une classe avec elle-même vaut 0");
            // on remet toujours le couple dans le même sens
            Edge n = e.a().compareTo(e.b()) < 0 ? e : new Edge(e.b(), e.a(), e.arcs(), e.weight());
            if (byPair.put(key(n.a(), n.b()), n) != null)
                throw new IllegalArgumentException("arête en double : " + n.a() + " -- " + n.b());
            normalized.add(n);
        }
        normalized.sort(ORDER);
        this.edges = List.copyOf(normalized);
    }

    public static CouplingGraph of(Coupling coupling) {
        List<Edge> edges = new ArrayList<>();
        for (Coupling.Pair p : coupling.pairs())
            edges.add(new Edge(p.a(), p.b(), p.arcs(), p.coupling()));
        return new CouplingGraph(coupling.classes(), edges);
    }

    public List<String> nodes() {
        return nodes;
    }

    // par poids décroissant
    public List<Edge> edges() {
        return edges;
    }

    public double weight(String a, String b) {
        Edge e = byPair.get(a.compareTo(b) < 0 ? key(a, b) : key(b, a));
        return e == null ? 0.0 : e.weight();
    }

    // les k arêtes les plus lourdes (moins s'il y en a moins)
    public List<Edge> heaviest(int k) {
        return edges.subList(0, Math.min(k, edges.size()));
    }

    // nœuds sans aucune arête
    public List<String> isolated() {
        Set<String> touched = new HashSet<>();
        for (Edge e : edges) {
            touched.add(e.a());
            touched.add(e.b());
        }
        List<String> result = new ArrayList<>();
        for (String n : nodes)
            if (!touched.contains(n))
                result.add(n);
        return result;
    }

    // somme des r sur les arêtes, c'est-à-dire |E_inter|
    public int totalArcs() {
        int s = 0;
        for (Edge e : edges)
            s += e.arcs();
        return s;
    }

    private static String key(String a, String b) {
        return a + "|" + b;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CouplingGraph g && nodes.equals(g.nodes) && edges.equals(g.edges);
    }

    @Override
    public int hashCode() {
        return 31 * nodes.hashCode() + edges.hashCode();
    }
}
