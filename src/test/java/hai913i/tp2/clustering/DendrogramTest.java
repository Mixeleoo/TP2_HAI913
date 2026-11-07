package hai913i.tp2.clustering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hai913i.tp2.CsvFactsLoader;
import hai913i.tp2.coupling.Coupling;

class DendrogramTest {

    private static Clustering validation() throws IOException {
        Coupling c = Coupling.of(new CsvFactsLoader(Path.of("src", "test", "resources", "facts-validation")));
        return Clustering.run(c.classes(), c::coupling);
    }

    private static void walk(Cluster g, List<Cluster> leaves, List<Cluster> internals) {
        if (g.isLeaf()) {
            leaves.add(g);
            return;
        }
        internals.add(g);
        walk(g.left(), leaves, internals);
        walk(g.right(), leaves, internals);
    }

    @Test
    void pointDeControleB2() throws IOException {
        Clustering cl = validation();
        List<Cluster> leaves = new ArrayList<>();
        List<Cluster> internals = new ArrayList<>();
        walk(cl.root(), leaves, internals);
        assertEquals(15, leaves.size());
        assertEquals(14, internals.size());
        assertEquals(15, cl.root().size());
        assertEquals(15, new HashSet<>(cl.root().members()).size());

        for (Cluster g : internals) {
            Set<Integer> union = new HashSet<>(g.left().members());
            union.addAll(g.right().members());
            assertEquals(g.size(), g.left().size() + g.right().size());
            assertEquals(Set.copyOf(g.members()), union);
        }
        List<Clustering.Merge> m = cl.merges();
        assertEquals(0.0, m.get(m.size() - 1).similarity(), 1e-9);
        assertEquals(0.0, m.get(m.size() - 2).similarity(), 1e-9);
        assertTrue(cl.root().id() == 28);
    }
}
