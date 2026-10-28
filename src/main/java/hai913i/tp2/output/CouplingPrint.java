package hai913i.tp2.output;

import hai913i.tp2.coupling.Coupling;

import java.io.PrintStream;
import java.util.Locale;

// Restitution de l'étape A1 dans le terminal
public final class CouplingPrint {

    private CouplingPrint() {
    }

    public static void print(Coupling coupling, PrintStream out) {
        out.println("=== A1 : couplage entre classes ===");
        out.printf(Locale.ROOT, "Arcs internes écartés : %d%n", coupling.internalArcs());
        out.printf(Locale.ROOT, "|E_inter|             : %d%n", coupling.interArcs());
        out.printf(Locale.ROOT, "Couples non nuls      : %d / %d%n",
                coupling.pairs().size(), coupling.possiblePairs());
        out.printf(Locale.ROOT, "Symétrie              : %s%n", yesNo(coupling.isSymmetric()));
        out.printf(Locale.ROOT, "Diagonale nulle       : %s%n", yesNo(coupling.hasZeroDiagonal()));
        out.printf(Locale.ROOT, "Somme des couplages   : %.4f (%s)%n", coupling.sum(),
                Math.abs(coupling.sum() - 1.0) < Coupling.EPSILON ? "égale à 1" : "DIFFÉRENTE de 1");
        out.printf(Locale.ROOT, "Classes sans couplage : %s%n",
                coupling.uncoupled().isEmpty() ? "(aucune)" : String.join(", ", coupling.uncoupled()));
        out.println();

        // une ligne par couple : r, C(A, B), A -- B
        out.println("Couples par couplage décroissant :");
        out.printf(Locale.ROOT, "  %3s  %-7s  %s%n", "r", "C(A,B)", "A -- B");
        for (Coupling.Pair p : coupling.pairs()) {
            out.printf(Locale.ROOT, "  %3d  %.4f   %s -- %s%n", p.arcs(), p.coupling(), p.a(), p.b());
        }
        out.println();
    }

    private static String yesNo(boolean b) {
        return b ? "oui" : "NON";
    }
}
