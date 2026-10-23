package hai913i.tp2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hai913i.tp2.model.CallEdge;
import hai913i.tp2.model.MethodFact;

/**
 * Point de contrôle A0 sur le micro jeu de données, par les deux implémentations
 * de {@link FactsSource}.
 *
 * <p>{@link CsvFactsLoader} lit {@code facts-mini-*.csv} ; {@link FactsAdapter}
 * analyse, avec le TP 1, les sources {@code demo.A}, {@code demo.B} et
 * {@code demo.C} écrites pour reproduire exactement ces faits. Les deux doivent
 * annoncer 3 classes, 5 méthodes, 4 arcs, et donner les mêmes faits.</p>
 */
class FactsSourceTest {

    private static FactsSource csv() throws IOException {
        return new CsvFactsLoader(Path.of("src", "test", "resources"), "facts-mini");
    }

    private static FactsSource tp1() {
        return new FactsAdapter(Path.of("src", "test", "resources", "facts-mini-sources"));
    }

    private static void troisCinqQuatre(FactsSource f) {
        assertEquals(3, f.getClasses().size());
        assertEquals(5, f.getMethods().size());
        assertEquals(4, f.getCalls().size());
    }

    @Test
    void leCsvAnnonceTroisCinqQuatre() throws IOException {
        troisCinqQuatre(csv());
    }

    @Test
    void leTp1AnnonceTroisCinqQuatre() {
        troisCinqQuatre(tp1());
    }

    @Test
    void lesDeuxCheminsDonnentLesMemesFaits() throws IOException {
        FactsSource a = csv();
        FactsSource b = tp1();
        assertEquals(new HashSet<>(a.getClasses()), new HashSet<>(b.getClasses()));
        assertEquals(new HashSet<>(a.getMethods()), new HashSet<>(b.getMethods()));
        assertEquals(new HashSet<>(a.getCalls()), new HashSet<>(b.getCalls()));
    }

    @Test
    void chaqueMethodeConnaitSaClasse() throws IOException {
        for (FactsSource f : List.of(csv(), tp1())) {
            Set<MethodFact> methods = new HashSet<>(f.getMethods());
            assertTrue(methods.contains(new MethodFact("demo.A#f()", "f", "demo.A")));
            assertTrue(methods.contains(new MethodFact("demo.C#j()", "j", "demo.C")));
        }
    }

    @Test
    void unArcSurQuatreEstInterneAUneClasse() throws IOException {
        for (FactsSource f : List.of(csv(), tp1())) {
            long intra = f.getCalls().stream()
                    .filter(e -> e.callerClass().equals(e.calleeClass()))
                    .count();
            assertEquals(1, intra);
            assertTrue(f.getCalls().contains(
                    new CallEdge("demo.A#f()", "demo.A", "demo.A#g()", "demo.A")));
        }
    }
}
