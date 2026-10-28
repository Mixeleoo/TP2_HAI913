package hai913i.tp2;

import hai913i.tp2.coupling.Coupling;
import hai913i.tp2.graph.CouplingGraph;
import hai913i.tp2.output.CouplingPrint;
import hai913i.tp2.output.GraphExport;
import hai913i.tp2.output.GraphPrint;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Point d'entrée du squelette : vérifie l'environnement et charge les faits.
 *
 * <p><b>Ce qu'il fait.</b> Il lit les arguments de la ligne de commande, charge
 * un jeu de faits au format canonique, et affiche les trois compteurs du
 * <b>point de contrôle A0</b> : nombre de classes, de méthodes, d'arcs du
 * graphe d'appel.</p>
 *
 * <p><b>Ce qu'il ne fait pas.</b> Tout le reste. Le couplage (A1), le graphe
 * pondéré (A2), le regroupement (B1), le dendrogramme (B2) et la coupe (B3)
 * sont l'objet du TP : leurs emplacements sont marqués {@code TODO} ci-dessous,
 * et rien n'en est fourni.</p>
 *
 * <pre>
 *   mvn clean package
 *   java -jar target/hai913i-tp2.jar CHEMIN_DU_DOSSIER_DES_FAITS
 *   java -jar target/hai913i-tp2.jar CHEMIN_DU_DOSSIER_DES_FAITS --cp 0.025
 * </pre>
 *
 * <p>Le dossier passé en argument est celui qui contient les trois fichiers
 * {@code ...-classes.csv}, {@code ...-methods.csv} et {@code ...-calls.csv}.
 * Le préfixe en est déduit : votre outil n'a donc aucune raison de connaître le
 * nom du projet de validation, et il fonctionnera tel quel sur un autre jeu de
 * faits.. Si le dossier ne contient aucun {@code *-classes.csv}, il est traité
 * comme un projet Java et ses faits sont extraits par le TP 1
 * ({@link FactsAdapter}).</p>
 *
 * <p><b>Codes de sortie</b> : 0 si les faits sont chargés, 1 si les arguments
 * sont invalides, 2 si les fichiers sont illisibles (ou si les exports ne
 * peuvent pas être écrits). Gardez cette convention
 * dans votre propre outil : elle fait partie du critère de reproductibilité.</p>
 */
public final class Main {

    /**
     * Sortie standard forcée en UTF-8.
     *
     * <p>Sans cela, une console Windows configurée sur une autre page de codes
     * afficherait les accents de travers. Le jeu de données, les sources et
     * cette sortie sont tous en UTF-8.</p>
     */
    private static final PrintStream OUT =
            new PrintStream(System.out, true, StandardCharsets.UTF_8);

    /** Sortie d'erreur, forcée en UTF-8 pour la même raison. */
    private static final PrintStream ERR =
            new PrintStream(System.err, true, StandardCharsets.UTF_8);

    /** Seuil de coupe par défaut, repris du point de contrôle B3 du sujet. */
    private static final double CP_DEFAUT = 0.025;

    /** Dossier des exports par défaut (graphe, puis dendrogramme et modules). */
    private static final String OUT_DEFAUT = "sorties";

    private Main() {
    }

    /**
     * Charge les faits et affiche les compteurs de l'étape A0.
     *
     * @param args {@code <dossier-des-faits> [--cp <valeur>] [--prefix <préfixe>] [--out <dossier>]}
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            usage("chemin du dossier des faits manquant");
            return;
        }
        Path dir = Path.of(args[0]);
        double cp = CP_DEFAUT;
        String prefix = null;
        Path outDir = Path.of(OUT_DEFAUT);
        for (int i = 1; i < args.length; i++) {
            if ("--cp".equals(args[i]) && i + 1 < args.length) {
                try {
                    cp = Double.parseDouble(args[++i]);
                } catch (NumberFormatException e) {
                    usage("valeur de --cp invalide : " + args[i]);
                    return;
                }
            } else if ("--prefix".equals(args[i]) && i + 1 < args.length) {
                prefix = args[++i];
            } else if ("--out".equals(args[i]) && i + 1 < args.length) {
                outDir = Path.of(args[++i]);
            } else {
                usage("argument inconnu : " + args[i]);
                return;
            }
        }
        if (cp < 0.0) {
            usage("--cp doit être positif ou nul");
            return;
        }

        if (!Files.isDirectory(dir)) {
            ERR.println("Erreur : dossier introuvable : " + dir.toAbsolutePath());
            System.exit(2);
            return;
        }

        FactsSource facts;
        String origine;
        try {
            if (prefix != null || containsCsvFacts(dir)) {
                facts = prefix != null ? new CsvFactsLoader(dir, prefix)
                                       : new CsvFactsLoader(dir);
                origine = "CSV " + (prefix != null ? prefix : Facts.discoverPrefix(dir));
            } else {
                facts = new FactsAdapter(dir);
                origine = "sources Java (modèle du TP 1)";
            }
        } catch (IOException e) {
            ERR.println("Erreur : " + e.getMessage());
            System.exit(2);
            return;
        }

        OUT.printf(Locale.ROOT, "Dossier des faits   : %s%n", dir);
        OUT.printf(Locale.ROOT, "Origine des faits   : %s%n", origine);
        OUT.printf(Locale.ROOT, "Classes             : %d%n",
                facts.getClasses().size());
        OUT.printf(Locale.ROOT, "Méthodes            : %d%n",
                facts.getMethods().size());
        OUT.printf(Locale.ROOT, "Arcs du graphe      : %d%n",
                facts.getCalls().size());
        OUT.printf(Locale.ROOT, "Seuil CP retenu     : %.4f%n", cp);
        OUT.println();

        // étape A1 : à partir d'ici, plus rien ne lit les arcs d'appel
        Coupling coupling;
        try {
            coupling = Coupling.of(facts);
        } catch (IllegalArgumentException e) {
            ERR.println("Erreur : faits incohérents : " + e.getMessage());
            System.exit(2);
            return;
        }
        CouplingPrint.print(coupling, OUT);

        // étape A2 : graphe pondéré, exporté en CSV (relu aussitôt) et en DOT
        CouplingGraph graph = CouplingGraph.of(coupling);
        GraphPrint.print(graph, OUT);
        Path graphCsv = outDir.resolve("graphe-couplage.csv");
        Path graphDot = outDir.resolve("graphe-couplage.dot");
        try {
            if (Files.exists(outDir) && !Files.isDirectory(outDir))
                throw new IOException(outDir.toAbsolutePath() + " existe et n'est pas un dossier");
            Files.createDirectories(outDir);
            GraphExport.writeCsv(graph, graphCsv);
            GraphExport.writeDot(graph, graphDot);
            boolean same = GraphExport.readCsv(graphCsv).equals(graph);
            OUT.printf(Locale.ROOT, "Export CSV            : %s%n", graphCsv);
            OUT.printf(Locale.ROOT, "Export DOT            : %s%n", graphDot);
            OUT.printf(Locale.ROOT, "Rechargement du CSV   : %s%n",
                    same ? "mêmes nœuds, mêmes arêtes, mêmes poids" : "DIFFÉRENT du graphe calculé");
        } catch (IOException e) {
            ERR.println("Erreur : export du graphe impossible : " + e.getMessage());
            System.exit(2);
            return;
        }

        // TODO étape B1 : regroupement hiérarchique ascendant, lien moyen,
        //                 égalités départagées comme le sujet l'impose.
        // TODO étape B2 : représenter et exporter le dendrogramme.
        // TODO étape B3 : couper l'arbre sous les trois contraintes, puis
        //                 vérifier la contrainte M/2 et signaler sa violation.
        OUT.println();
    }

    /**
     * Choix du chemin d'entrée : un dossier qui contient un {@code *-classes.csv}
     * est un jeu de faits au format canonique (chemin 2, ou facts-mini) ; sinon
     * c'est un projet Java, analysé par le TP 1 (chemin 1).
     *
     * @param dir dossier passé en argument
     * @return vrai si le dossier contient au moins un jeu de faits CSV
     * @throws IOException si le dossier ne peut pas être listé
     */
    private static boolean containsCsvFacts(Path dir) throws IOException {
        try (var entries = Files.list(dir)) {
            return entries.anyMatch(p -> p.getFileName().toString().endsWith("-classes.csv"));
        }
    }

    /**
     * Affiche un message d'erreur, le mode d'emploi, et sort avec le code 1.
     *
     * @param message cause de l'échec, en une ligne
     */
    private static void usage(String message) {
        ERR.println("Erreur : " + message);
        ERR.println();
        ERR.println("Usage : java -jar hai913i-tp2.jar <dossier-des-faits>"
                + " [--cp <valeur>] [--prefix <préfixe>] [--out <dossier>]");
        ERR.println();
        ERR.println("  <dossier-des-faits>  dossier contenant les trois fichiers");
        ERR.println("                       <préfixe>-classes.csv,");
        ERR.println("                       <préfixe>-methods.csv et");
        ERR.println("                       <préfixe>-calls.csv ;");
        ERR.println("                       sinon, projet Java analysé par le TP 1");
        ERR.println("  --cp <valeur>        seuil de couplage interne moyen"
                + " (défaut : " + CP_DEFAUT + ")");
        ERR.println("  --prefix <préfixe>   force le préfixe ; par défaut il est");
        ERR.println("                       déduit du dossier");
        ERR.println("  --out <dossier>      dossier des exports (défaut : "
                + OUT_DEFAUT + ")");
        System.exit(1);
    }
}
