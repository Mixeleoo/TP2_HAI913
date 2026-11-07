package hai913i.tp2.clustering;

import java.util.ArrayList;
import java.util.List;

public final class Cut {

    public record Module(Cluster group, double internalCoupling) {}

    private final double cp;
    private final List<Module> modules;
    private final int maxModules;

    private Cut(double cp, List<Module> modules, int maxModules) {
        this.cp = cp;
        this.modules = List.copyOf(modules);
        this.maxModules = maxModules;
    }

    public static Cut of(Clustering clustering, Similarity c, double cp) {
        List<Module> modules = new ArrayList<>();
        visit(clustering.root(), c, cp, modules);
        return new Cut(cp, modules, clustering.classes().size() / 2);
    }

    private static void visit(Cluster g, Similarity c, double cp, List<Module> modules) {
        if (g.isLeaf()) {
            modules.add(new Module(g, Double.NaN));
            return;
        }
        double internal = internalCoupling(g, c);
        // strictement superieur, pas >=
        if (internal > cp) {
            modules.add(new Module(g, internal));
            return;
        }
        visit(g.left(), c, cp, modules);
        visit(g.right(), c, cp, modules);
    }

    // moyenne sur tous les couples du groupe, ce n'est pas la similarité de la fusoin
    public static double internalCoupling(Cluster g, Similarity c) {
        List<Integer> m = g.members();
        if (m.size() < 2)
            return Double.NaN;
        double sum = 0.0;
        for (int i = 0; i < m.size(); i++)
            for (int j = i + 1; j < m.size(); j++)
                sum += c.between(m.get(i), m.get(j));
        return sum / (m.size() * (m.size() - 1) / 2.0);
    }

    public double cp() {
        return cp;
    }

    public List<Module> modules() {
        return modules;
    }

    public int maxModules() {
        return maxModules;
    }

    public boolean respectsLimit() {
        return modules.size() <= maxModules;
    }
}
