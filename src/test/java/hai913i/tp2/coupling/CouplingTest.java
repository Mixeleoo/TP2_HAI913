package hai913i.tp2.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import hai913i.tp2.CsvFactsLoader;
import hai913i.tp2.FactsSource;
import hai913i.tp2.model.CallEdge;
import hai913i.tp2.model.ClassFact;
import hai913i.tp2.model.MethodFact;

/**
 * Point de contrôle A1, sur le micro jeu de données (valeurs calculées à la main
 * dans examples/README.txt) puis sur le projet de validation.
 */
class CouplingTest {

    private static final double EPS = 1e-9;

    private static Coupling mini() throws IOException {
        return Coupling.of(new CsvFactsLoader(Path.of("src", "test", "resources"), "facts-mini"));
    }

    private static Coupling validation() throws IOException {
        return Coupling.of(new CsvFactsLoader(Path.of("src", "test", "resources", "facts-validation")));
    }

    @Test
    void miniUnArcInterneTroisInterClasses() throws IOException {
        Coupling c = mini();
        assertEquals(1, c.internalArcs());
        assertEquals(3, c.interArcs());
    }

    @Test
    void miniValeursCalculeesALaMain() throws IOException {
        Coupling c = mini();
        assertEquals(2, c.arcs("demo.A", "demo.B"));
        assertEquals(2.0 / 3, c.coupling("demo.A", "demo.B"), EPS);
        assertEquals(1.0 / 3, c.coupling("demo.B", "demo.C"), EPS);
        assertEquals(0.0, c.coupling("demo.A", "demo.C"), EPS);
    }

    @Test
    void miniSymetrieDiagonaleSomme() throws IOException {
        Coupling c = mini();
        assertTrue(c.isSymmetric());
        assertTrue(c.hasZeroDiagonal());
        assertEquals(c.coupling("demo.B", "demo.A"), c.coupling("demo.A", "demo.B"));
        assertEquals(1.0, c.sum(), EPS);
    }

    @Test
    void miniCouplesParCouplageDecroissant() throws IOException {
        List<Coupling.Pair> pairs = mini().pairs();
        assertEquals(2, pairs.size());
        assertEquals(new Coupling.Pair("demo.A", "demo.B", 2, 2.0 / 3), pairs.get(0));
        assertEquals("demo.C", pairs.get(1).b());
    }

    @Test
    void validationPointDeControleA1() throws IOException {
        Coupling c = validation();
        assertEquals(15, c.size());
        assertEquals(7, c.internalArcs());
        assertEquals(37, c.interArcs());
        assertEquals(19, c.pairs().size());
        assertEquals(105, c.possiblePairs());
        assertTrue(c.isSymmetric());
        assertTrue(c.hasZeroDiagonal());
        assertEquals(1.0, c.sum(), EPS);

        Coupling.Pair top = c.pairs().get(0);
        assertEquals("library.model.Item", top.a());
        assertEquals("library.service.LoanService", top.b());
        assertEquals(4, top.arcs());
        assertEquals(4.0 / 37, top.coupling(), EPS);

        assertEquals(List.of("library.model.Dvd", "library.service.LoanException"), c.uncoupled());
    }

    @Test
    void aucuneValeurHorsDeZeroUn() throws IOException {
        Coupling c = validation();
        for (int i = 0; i < c.size(); i++)
            for (int j = 0; j < c.size(); j++)
                assertTrue(c.coupling(i, j) >= 0.0 && c.coupling(i, j) <= 1.0);
    }

    @Test
    void unArcDansChaqueSensCompteDeuxFoisPourLeMemeCouple() {
        // le piège de l'indice A1 : A -> B et B -> A donnent r(A, B) = 2, et non deux couples
        FactsSource f = source(List.of("x.A", "x.B"), List.of(
                new CallEdge("x.A#f()", "x.A", "x.B#g()", "x.B"),
                new CallEdge("x.B#g()", "x.B", "x.A#f()", "x.A")));
        Coupling c = Coupling.of(f);
        assertEquals(2, c.arcs("x.A", "x.B"));
        assertEquals(1, c.pairs().size());
        assertEquals(1.0, c.sum(), EPS);
    }

    @Test
    void sansArcInterClassesToutVautZero() {
        FactsSource f = source(List.of("x.A"), List.of(
                new CallEdge("x.A#f()", "x.A", "x.A#g()", "x.A")));
        Coupling c = Coupling.of(f);
        assertEquals(0, c.interArcs());
        assertEquals(0.0, c.sum(), EPS);
    }

    @Test
    void arcVersUneClasseInconnueRefuse() {
        FactsSource f = source(List.of("x.A"), List.of(
                new CallEdge("x.A#f()", "x.A", "y.Z#g()", "y.Z")));
        assertThrows(IllegalArgumentException.class, () -> Coupling.of(f));
    }

    // petite source de faits écrite à la main, sans méthodes (le couplage n'en a pas besoin)
    private static FactsSource source(List<String> classes, List<CallEdge> calls) {
        return new FactsSource() {
            @Override
            public List<ClassFact> getClasses() {
                return classes.stream().map(ClassFact::new).toList();
            }

            @Override
            public List<MethodFact> getMethods() {
                return List.of();
            }

            @Override
            public List<CallEdge> getCalls() {
                return calls;
            }
        };
    }
}
