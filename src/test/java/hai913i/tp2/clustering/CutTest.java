package hai913i.tp2.clustering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hai913i.tp2.CsvFactsLoader;
import hai913i.tp2.coupling.Coupling;

class CutTest {

    private static Coupling coupling;
    private static Clustering clustering;

    private static Cut cut(double cp) throws IOException {
        if (coupling == null) {
            coupling = Coupling.of(new CsvFactsLoader(Path.of("src", "test", "resources", "facts-validation")));
            clustering = Clustering.run(coupling.classes(), coupling::coupling);
        }
        return Cut.of(clustering, coupling::coupling, cp);
    }

    private static void checkPartition(Cut cut) {
        Set<Integer> seen = new HashSet<>();
        int total = 0;
        for (Cut.Module m : cut.modules()) {
            seen.addAll(m.group().members());
            total += m.group().size();
            if (m.group().size() >= 2)
                assertTrue(m.internalCoupling() > cut.cp());
        }
        assertEquals(15, total);
        assertEquals(15, seen.size());
    }

    @Test
    void cp0015QuatreModules() throws IOException {
        Cut c = cut(0.015);
        assertEquals(4, c.modules().size());
        assertTrue(c.respectsLimit());
        assertEquals(11, c.modules().stream().mapToInt(m -> m.group().size()).max().orElseThrow());
        checkPartition(c);
    }

    @Test
    void cp0025SeptModules() throws IOException {
        Cut c = cut(0.025);
        assertEquals(7, c.modules().size());
        assertTrue(c.respectsLimit());
        checkPartition(c);
    }

    @Test
    void cp0045DixModulesViolee() throws IOException {
        Cut c = cut(0.045);
        assertEquals(10, c.modules().size());
        assertFalse(c.respectsLimit());
        checkPartition(c);
    }
}
