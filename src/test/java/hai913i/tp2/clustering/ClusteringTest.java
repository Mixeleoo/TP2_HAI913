package hai913i.tp2.clustering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hai913i.tp2.CsvFactsLoader;
import hai913i.tp2.coupling.Coupling;

class ClusteringTest {

    private static final double EPS = 1e-9;

    private static Coupling mini() throws IOException {
        return Coupling.of(new CsvFactsLoader(Path.of("src", "test", "resources"), "facts-mini"));
    }

    private static Coupling validation() throws IOException {
        return Coupling.of(new CsvFactsLoader(Path.of("src", "test", "resources", "facts-validation")));
    }

    private static Clustering run(Coupling c) {
        return Clustering.run(c.classes(), c::coupling);
    }

    @Test
    void miniCalculeALaMain() throws IOException {
        Clustering cl = run(mini());
        List<Clustering.Merge> m = cl.merges();
        assertEquals(2, m.size());
        assertEquals(0, m.get(0).a().id());
        assertEquals(1, m.get(0).b().id());
        assertEquals(2.0 / 3, m.get(0).similarity(), EPS);
        assertEquals(3, m.get(0).result().id());
        assertEquals(2, m.get(1).a().id());
        assertEquals(3, m.get(1).b().id());
        assertEquals(1.0 / 6, m.get(1).similarity(), EPS);
        assertEquals(List.of(0, 1, 2), cl.root().members());
    }

    @Test
    void validationPointDeControleB1() throws IOException {
        Clustering cl = run(validation());
        List<Clustering.Merge> m = cl.merges();
        assertEquals(14, m.size());
        assertTrue(cl.isNonIncreasing());

        assertEquals(List.of("library.model.Item"), cl.names(m.get(0).a()));
        assertEquals(List.of("library.service.LoanService"), cl.names(m.get(0).b()));
        assertEquals(4.0 / 37, m.get(0).similarity(), EPS);

        assertEquals(List.of("library.app.LibraryApp"), cl.names(m.get(1).a()));
        assertEquals(List.of("library.service.Catalog"), cl.names(m.get(1).b()));
        assertEquals(3.0 / 37, m.get(1).similarity(), EPS);
        assertEquals(Set.of("library.model.Item", "library.service.LoanService"),
                Set.copyOf(cl.names(m.get(2).a())));
        assertEquals(Set.of("library.app.LibraryApp", "library.service.Catalog"),
                Set.copyOf(cl.names(m.get(2).b())));
        assertEquals(9.0 / 148, m.get(2).similarity(), EPS);
        assertEquals(0.0, m.get(12).similarity(), EPS);
        assertEquals(0.0, m.get(13).similarity(), EPS);
        assertEquals(15, cl.root().size());
    }

    @Test
    void deuxExecutionsMemeSuiteDeFusions() throws IOException {
        List<Clustering.Merge> m1 = run(validation()).merges();
        List<Clustering.Merge> m2 = run(validation()).merges();
        assertEquals(m1.size(), m2.size());
        for (int i = 0; i < m1.size(); i++) {
            assertEquals(m1.get(i).a().id(), m2.get(i).a().id());
            assertEquals(m1.get(i).b().id(), m2.get(i).b().id());
            assertEquals(m1.get(i).similarity(), m2.get(i).similarity());
        }
    }

    @Test
    void departageSurLaMatriceDeTieBreakDemo() {
        double[][] sim = {
            {0.00, 0.10, 0.25, 0.05},
            {0.10, 0.00, 0.05, 0.25},
            {0.25, 0.05, 0.00, 0.25},
            {0.05, 0.25, 0.25, 0.00},
        };
        Clustering cl = Clustering.run(List.of("g0", "g1", "g2", "g3"), (a, b) -> sim[a][b]);
        Clustering.Merge first = cl.merges().get(0);
        assertEquals(0, first.a().id());
        assertEquals(2, first.b().id());
        assertEquals(3, first.tied());
    }
}
