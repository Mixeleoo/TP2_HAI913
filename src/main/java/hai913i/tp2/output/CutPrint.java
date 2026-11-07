package hai913i.tp2.output;

import hai913i.tp2.clustering.Clustering;
import hai913i.tp2.clustering.Cut;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CutPrint {

    private CutPrint() {
    }

    public static void print(Cut cut, Clustering clustering, PrintStream out) {
        out.println("=== B3 : modules ===");
        out.printf(Locale.ROOT, "Seuil CP              : %.4f%n", cut.cp());
        out.printf(Locale.ROOT, "Modules               : %d (au plus M/2 = %d)%n",
                cut.modules().size(), cut.maxModules());
        if (cut.respectsLimit())
            out.println("Contrainte M/2        : respectée");
        else
            out.println("Contrainte M/2        : VIOLÉE, la coupe est rejetée pour ce seuil");
        out.println();

        int k = 1;
        for (Cut.Module m : cut.modules()) {
            String c = m.group().size() < 2 ? "-" : String.format(Locale.ROOT, "%.4f", m.internalCoupling());
            out.printf(Locale.ROOT, "  Module %d  [%d]  %d classe(s)  C(G) = %s%n",
                    k++, m.group().id(), m.group().size(), c);
            for (String n : clustering.names(m.group()))
                out.println("      " + n);
        }
        out.println();
    }

    public static void write(Cut cut, Clustering clustering, Path file) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        print(cut, clustering, new PrintStream(buf, true, StandardCharsets.UTF_8));
        Files.writeString(file, buf.toString(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
