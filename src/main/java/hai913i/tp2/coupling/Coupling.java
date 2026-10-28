package hai913i.tp2.coupling;

import hai913i.tp2.FactsSource;
import hai913i.tp2.model.CallEdge;
import hai913i.tp2.model.ClassFact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Étape A1 : C(A, B) = r(A, B) / |E_inter| pour tout couple de classes distinctes.
// Seule cette classe lit les arcs d'appel ; la suite (graphe, regroupement) ne voit que les couplages.
public final class Coupling {

    // Tolérance pour comparer deux couplages (§ 5.4 du sujet)
    public static final double EPSILON = 1e-9;

    // Un couple de couplage non nul, a avant b dans l'ordre alphabétique
    public record Pair(String a, String b, int arcs, double coupling) {}

    // Classes triées par nom qualifié : l'indice est l'identifiant de la règle de départage
    private final List<String> classes;
    private final Map<String, Integer> index = new HashMap<>();
    // r(A, B), symétrique, diagonale nulle
    private final int[][] arcs;
    private final int internalArcs;
    private final int interArcs;

    private Coupling(List<String> classes, int[][] arcs, int internalArcs, int interArcs) {
        this.classes = List.copyOf(classes);
        for (int i = 0; i < classes.size(); i++)
            index.put(classes.get(i), i);
        this.arcs = arcs;
        this.internalArcs = internalArcs;
        this.interArcs = interArcs;
    }

    public static Coupling of(FactsSource facts) {
        // la liste des classes vient du fichier des classes, pas des appels (Dvd et LoanException n'y sont pas)
        List<String> classes = new ArrayList<>();
        for (ClassFact c : facts.getClasses())
            classes.add(c.name());
        classes.sort(Comparator.naturalOrder());
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < classes.size(); i++) {
            if (idx.put(classes.get(i), i) != null)
                throw new IllegalArgumentException("classe en double : " + classes.get(i));
        }

        int[][] r = new int[classes.size()][classes.size()];
        int internal = 0;
        int inter = 0;
        for (CallEdge e : facts.getCalls()) {
            Integer i = idx.get(e.callerClass());
            Integer j = idx.get(e.calleeClass());
            if (i == null || j == null)
                throw new IllegalArgumentException("arc vers une classe inconnue : "
                        + e.callerMethod() + " -> " + e.calleeMethod());
            if (i.equals(j)) {
                // arc interne : il relève de la cohésion, il ne compte ni dans r ni dans |E_inter|
                internal++;
                continue;
            }
            // un arc A -> B et un arc B -> A comptent tous les deux pour le même couple
            r[i][j]++;
            r[j][i]++;
            inter++;
        }
        return new Coupling(classes, r, internal, inter);
    }

    public List<String> classes() {
        return classes;
    }

    public int size() {
        return classes.size();
    }

    public int internalArcs() {
        return internalArcs;
    }

    // |E_inter|, le dénominateur
    public int interArcs() {
        return interArcs;
    }

    public int arcs(int i, int j) {
        return arcs[i][j];
    }

    public int arcs(String a, String b) {
        return arcs(indexOf(a), indexOf(b));
    }

    public double coupling(int i, int j) {
        return interArcs == 0 ? 0.0 : (double) arcs[i][j] / interArcs;
    }

    public double coupling(String a, String b) {
        return coupling(indexOf(a), indexOf(b));
    }

    public int indexOf(String className) {
        Integer i = index.get(className);
        if (i == null)
            throw new IllegalArgumentException("classe inconnue : " + className);
        return i;
    }

    // Couples de couplage non nul, par couplage décroissant puis par identifiants croissants
    public List<Pair> pairs() {
        List<Pair> result = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            for (int j = i + 1; j < size(); j++) {
                if (arcs[i][j] > 0)
                    result.add(new Pair(classes.get(i), classes.get(j), arcs[i][j], coupling(i, j)));
            }
        }
        // on trie sur r (entier) plutôt que sur C : même ordre, sans souci d'arrondi
        result.sort(Comparator.comparingInt(Pair::arcs).reversed());
        return result;
    }

    // Nombre de couples de classes distinctes, M(M-1)/2
    public int possiblePairs() {
        return size() * (size() - 1) / 2;
    }

    public double sum() {
        double s = 0.0;
        for (int i = 0; i < size(); i++)
            for (int j = i + 1; j < size(); j++)
                s += coupling(i, j);
        return s;
    }

    public boolean isSymmetric() {
        for (int i = 0; i < size(); i++)
            for (int j = 0; j < size(); j++)
                if (coupling(i, j) != coupling(j, i))
                    return false;
        return true;
    }

    public boolean hasZeroDiagonal() {
        for (int i = 0; i < size(); i++)
            if (coupling(i, i) != 0.0)
                return false;
        return true;
    }

    // Classes qui n'apparaissent dans aucun couple non nul
    public List<String> uncoupled() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            boolean coupled = false;
            for (int j = 0; j < size() && !coupled; j++)
                coupled = arcs[i][j] > 0;
            if (!coupled)
                result.add(classes.get(i));
        }
        return result;
    }
}
