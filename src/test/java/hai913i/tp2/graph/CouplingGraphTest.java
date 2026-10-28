package hai913i.tp2.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import hai913i.tp2.CsvFactsLoader;
import hai913i.tp2.coupling.Coupling;
import hai913i.tp2.output.GraphExport;

/**
 * Point de contrôle A2, sur le micro jeu de données puis sur le projet de
 * validation, et rechargement de l'export CSV.
 */
class CouplingGraphTest {

    private static final double EPS = 1e-9;

    private static CouplingGraph mini() throws IOException {
        return CouplingGraph.of(Coupling.of(
                new CsvFactsLoader(Path.of("src", "test", "resources"), "facts-mini")));
    }

    private static CouplingGraph validation() throws IOException {
        return CouplingGraph.of(Coupling.of(
                new CsvFactsLoader(Path.of("src", "test", "resources", "facts-validation"))));
    }

    @Test
    void miniTroisNoeudsDeuxAretes() throws IOException {
        CouplingGraph g = mini();
        assertEquals(List.of("demo.A", "demo.B", "demo.C"), g.nodes());
        assertEquals(2, g.edges().size());
        assertEquals(2.0 / 3, g.weight("demo.B", "demo.A"), EPS);
        // couple sans arête : 0, pas une erreur
        assertEquals(0.0, g.weight("demo.A", "demo.C"));
        assertTrue(g.isolated().isEmpty());
    }

    @Test
    void validationPointDeControleA2() throws IOException {
        CouplingGraph g = validation();
        assertEquals(15, g.nodes().size());
        assertEquals(19, g.edges().size());
        assertEquals(37, g.totalArcs());

        List<CouplingGraph.Edge> top = g.heaviest(5);
        assertEquals("library.model.Item", top.get(0).a());
        assertEquals("library.service.LoanService", top.get(0).b());
        assertEquals("library.service.LoanService", top.get(1).a());
        assertEquals("library.service.LoanService.Loan", top.get(1).b());
        assertEquals(4.0 / 37, top.get(1).weight(), EPS);
        for (int i = 2; i < 5; i++) {
            assertEquals("library.app.LibraryApp", top.get(i).a());
            assertEquals(3, top.get(i).arcs());
        }
        assertEquals("library.model.Item", top.get(2).b());
        assertEquals("library.service.Catalog", top.get(3).b());
        assertEquals("library.service.LoanService", top.get(4).b());

        assertEquals(List.of("library.model.Dvd", "library.service.LoanException"), g.isolated());
    }

    @Test
    void lExportCsvSeRechargeALIdentique(@TempDir Path tmp) throws IOException {
        CouplingGraph g = validation();
        Path csv = tmp.resolve("graphe.csv");
        GraphExport.writeCsv(g, csv);
        CouplingGraph back = GraphExport.readCsv(csv);
        assertEquals(g, back);
        // les classes isolées sont dans le fichier, même sans arête
        assertEquals(g.isolated(), back.isolated());
    }

    @Test
    void lExportDotContientTousLesNoeudsEtAretes(@TempDir Path tmp) throws IOException {
        Path dot = tmp.resolve("graphe.dot");
        GraphExport.writeDot(validation(), dot);
        String text = Files.readString(dot);
        assertTrue(text.contains("graph couplage {"));
        assertEquals(19, text.lines().filter(l -> l.contains(" -- ")).count());
        assertTrue(text.contains("\"library.model.Dvd\" [label=\"Dvd\""));
    }

    @Test
    void unFichierMalFormeEstRefuse(@TempDir Path tmp) throws IOException {
        Path csv = tmp.resolve("faux.csv");
        Files.writeString(csv, GraphExport.HEADER + "\nnoeud;x.A;;;\narete;x.A;x.B;1;0.5\n");
        // x.B n'est pas déclaré comme nœud
        assertThrows(IOException.class, () -> GraphExport.readCsv(csv));

        Files.writeString(csv, "pas;le;bon;en;tete\n");
        assertThrows(IOException.class, () -> GraphExport.readCsv(csv));

        assertThrows(IOException.class, () -> GraphExport.readCsv(tmp.resolve("absent.csv")));
    }
}
