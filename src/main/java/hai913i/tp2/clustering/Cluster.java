package hai913i.tp2.clustering;

import java.util.ArrayList;
import java.util.List;

public final class Cluster {

    private final int id;
    private final Cluster left;
    private final Cluster right;
    // NaN pour une feuile
    private final double similarity;
    private final List<Integer> members;

    private Cluster(int id, Cluster left, Cluster right, double similarity, List<Integer> members) {
        this.id = id;
        this.left = left;
        this.right = right;
        this.similarity = similarity;
        this.members = List.copyOf(members);
    }

    public static Cluster leaf(int classIndex) {
        return new Cluster(classIndex, null, null, Double.NaN, List.of(classIndex));
    }

    public static Cluster merge(int id, Cluster left, Cluster right, double similarity) {
        List<Integer> m = new ArrayList<>(left.members);
        m.addAll(right.members);
        m.sort(null);
        return new Cluster(id, left, right, similarity, m);
    }

    public int id() {
        return id;
    }

    public boolean isLeaf() {
        return left == null;
    }

    public Cluster left() {
        return left;
    }

    public Cluster right() {
        return right;
    }

    public double similarity() {
        return similarity;
    }

    public List<Integer> members() {
        return members;
    }

    public int size() {
        return members.size();
    }
}
