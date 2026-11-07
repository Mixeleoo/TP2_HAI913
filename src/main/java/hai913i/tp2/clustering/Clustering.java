package hai913i.tp2.clustering;

import java.util.ArrayList;
import java.util.List;

public final class Clustering {

    public static final double EPSILON = 1e-9;

    public record Merge(int step, Cluster a, Cluster b, double similarity, Cluster result, int tied) {}

    private final List<String> classes;
    private final List<Merge> merges;
    private final Cluster root;

    private Clustering(List<String> classes, List<Merge> merges, Cluster root) {
        this.classes = List.copyOf(classes);
        this.merges = List.copyOf(merges);
        this.root = root;
    }

    private record Candidate(Cluster a, Cluster b, double similarity) {}

    public static Clustering run(List<String> classes, Similarity c) {
        int m = classes.size();
        if (m == 0)
            throw new IllegalArgumentException("aucune classe à regrouper");

        // les groupes actifs restent triés par id, un nouveau groupe a toujours le plus grand
        List<Cluster> active = new ArrayList<>();
        for (int i = 0; i < m; i++)
            active.add(Cluster.leaf(i));

        List<Merge> merges = new ArrayList<>();
        int nextId = m;
        while (active.size() > 1) {
            List<Candidate> candidates = new ArrayList<>();
            double best = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < active.size(); i++) {
                for (int j = i + 1; j < active.size(); j++) {
                    double s = averageLink(active.get(i), active.get(j), c);
                    candidates.add(new Candidate(active.get(i), active.get(j), s));
                    best = Math.max(best, s);
                }
            }
            // le premier couple a egalité avec le max est le plus petit dans l'odre lexicographique
            Candidate chosen = null;
            int tied = 0;
            for (Candidate cand : candidates) {
                if (cand.similarity() >= best - EPSILON) {
                    tied++;
                    if (chosen == null)
                        chosen = cand;
                }
            }

            Cluster merged = Cluster.merge(nextId++, chosen.a(), chosen.b(), chosen.similarity());
            merges.add(new Merge(merges.size() + 1, chosen.a(), chosen.b(), chosen.similarity(), merged, tied));
            active.remove(chosen.a());
            active.remove(chosen.b());
            active.add(merged);
        }
        return new Clustering(classes, merges, active.get(0));
    }

    // on divise par |g1|*|g2| donc les couples nuls compten aussi
    public static double averageLink(Cluster g1, Cluster g2, Similarity c) {
        double sum = 0.0;
        for (int a : g1.members())
            for (int b : g2.members())
                sum += c.between(a, b);
        return sum / ((double) g1.size() * g2.size());
    }

    public List<String> classes() {
        return classes;
    }

    public List<Merge> merges() {
        return merges;
    }

    public Cluster root() {
        return root;
    }

    public List<String> names(Cluster g) {
        List<String> result = new ArrayList<>();
        for (int i : g.members())
            result.add(classes.get(i));
        return result;
    }

    public boolean isNonIncreasing() {
        for (int i = 1; i < merges.size(); i++)
            if (merges.get(i).similarity() > merges.get(i - 1).similarity() + EPSILON)
                return false;
        return true;
    }
}
