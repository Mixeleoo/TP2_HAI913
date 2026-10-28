# HAI913I — TP 2 : Compréhension et architectures

Outil en ligne de commande qui part des **faits extraits au TP 1** (classes, méthodes, graphe d'appel) et en tire une **relation pondérée entre classes** : couplage `C(A, B)`, puis graphe de couplage pondéré exporté en CSV et en DOT.

Université de Montpellier — Faculté des Sciences · HAI913I Évolution et restructuration des logiciels · 2026–2027
Wissam El Hage, Léo Faucon

> **État actuel** : partie A (A0 → A2) terminée. Le regroupement, le dendrogramme et la coupe (partie B) ne sont pas encore implémentés ; l'option `--cp` est déjà lue et vérifiée mais ne sert qu'à partir de B3.

## Sommaire

- [Projet](#projet)
- [Environnement](#environnement)
- [Construction](#construction)
- [Exécution](#exécution)
- [Validation](#validation)
- [Dépannage](#dépannage)
- [Liens](#liens)

## Projet

### Chemin d'entrée retenu

Nous utilisons le **chemin 1** du sujet (§ 4) : notre propre modèle de faits du TP 1, derrière l'interface `FactsSource`. Le format canonique CSV (chemin 2) est aussi accepté, ce qui permet de charger le micro jeu de données `facts-mini`, qui n'existe qu'en CSV. Le type d'entrée est détecté automatiquement :

| Le dossier passé en argument…                  | Implémentation   | Origine des faits                        |
|------------------------------------------------|------------------|------------------------------------------|
| contient un `*-classes.csv` (ou `--prefix`)    | `CsvFactsLoader` | les trois CSV `<préfixe>-classes/methods/calls.csv` |
| sinon                                          | `FactsAdapter`   | les sources Java, analysées par notre TP 1 (JDT) |

### Architecture

```
src/main/java/hai913i/tp2/
├── FactsSource.java        interface d'entrée : classes, méthodes, arcs d'appel
├── FactsAdapter.java       chemin 1 : modèle du TP 1 (seule classe qui importe hai913i.tp1)
├── CsvFactsLoader.java     format canonique CSV (réutilise Facts.java du starter)
├── Facts.java              lecteur CSV fourni par le starter
├── model/                  ClassFact, MethodFact, CallEdge (records neutres)
├── coupling/Coupling.java  A1 : C(A,B) = r(A,B) / |E_inter| (seule classe qui lit les arcs)
├── graph/CouplingGraph.java A2 : graphe pondéré, arêtes non nulles seulement
├── output/                 restitution : CouplingPrint, GraphPrint, GraphExport (CSV + DOT)
└── Main.java               lecture des arguments, enchaînement des étapes
```

### Entrées et sorties

- **Entrée** : un dossier de faits CSV, ou un projet Java (dossier contenant `src/` ou des `.java`).
- **Terminal** : compteurs A0, couplages A1 (contrôles de symétrie, diagonale, somme), graphe A2 (cinq arêtes les plus lourdes, classes isolées).
- **Fichiers** (dossier `sorties/` par défaut) :
  - `graphe-couplage.csv` — format relisible : `type;classeA;classeB;arcs;poids`, une ligne `noeud` par classe puis une ligne `arete` par couple non nul ; le programme le recharge et vérifie qu'il redonne le même graphe ;
  - `graphe-couplage.dot` — à convertir en image avec Graphviz.

### Dépendances

- **TP 1** (`fr.umontpellier.hai913i:hai913i-tp1-analyzer:0.1.0`), nécessaire au chemin 1. Il apporte Eclipse JDT Core 3.46.0 de façon transitive ; c'est le TP 1 qui relit les `.java`, pas le code du TP 2.
- JUnit Jupiter 5.14.4 (tests uniquement).

### Limites connues

- Seul l'appel de méthode crée un couplage (§ 5.1 du sujet) : héritage, attributs, paramètres et `new` ne comptent pas.
- Sur le chemin 1, une erreur d'extraction est signalée par le TP 1 avec ses propres codes de sortie (3 ou 4).
- Partie B non encore implémentée.

## Environnement

| Élément    | Version utilisée                                                         |
|------------|--------------------------------------------------------------------------|
| Système    | Ubuntu 24.04.5 LTS (x86-64)                                              |
| JDK        | OpenJDK 21.0.12.1 — compilé pour Java 17, **JDK 17 minimum**             |
| Maven      | Apache Maven 3.8.7                                                       |
| Graphviz   | 2.43.0 (seulement pour convertir le DOT en image)                        |
| Outils     | IntelliJ IDEA 2025.2, et ligne de commande (`mvn`, `java`) ; aucun IDE n'est nécessaire |
| Bibliothèques | TP 1 0.1.0 (→ Eclipse JDT Core 3.46.0), JUnit Jupiter 5.14.4          |

## Construction

Le TP 2 dépend du jar du TP 1 : il faut d'abord **installer le TP 1 dans le dépôt Maven local**, puis construire le TP 2. Depuis une copie fraîche :

```sh
git clone https://github.com/Mixeleoo/TP1_HAI913.git
cd TP1_HAI913
mvn clean install          # installe hai913i-tp1-analyzer 0.1.0 dans ~/.m2
cd ..

git clone https://github.com/Mixeleoo/TP2_HAI913.git
cd TP2_HAI913
mvn clean package          # compile, lance les tests, produit le jar
```

Le jar exécutable autonome est `target/hai913i-tp2.jar`. `mvn clean test` lance seulement les tests.

Cette procédure a été vérifiée dans un dossier vide avec un dépôt Maven local vide (`-Dmaven.repo.local=...`).

## Exécution

Depuis la racine du dépôt :

```sh
java -jar target/hai913i-tp2.jar <dossier-des-faits> [--cp <valeur>] [--prefix <préfixe>] [--out <dossier>]
```

| Argument              | Rôle                                                                                       |
|-----------------------|--------------------------------------------------------------------------------------------|
| `<dossier-des-faits>` | dossier des trois CSV `<préfixe>-classes.csv`, `-methods.csv`, `-calls.csv`, ou projet Java |
| `--cp <valeur>`       | seuil de couplage interne moyen, nombre ≥ 0 (défaut : `0.025`) — utilisé à partir de B3    |
| `--prefix <préfixe>`  | choisit le jeu de données quand le dossier en contient plusieurs                            |
| `--out <dossier>`     | dossier des exports (défaut : `sorties`, créé s'il n'existe pas)                            |

### Exemples

Les deux premiers jeux de données sont inclus dans le dépôt :

```sh
# Jeu de validation, format canonique CSV
java -jar target/hai913i-tp2.jar src/test/resources/facts-validation

# Micro jeu de données facts-mini (le dossier contient aussi d'autres fichiers : préfixe explicite)
java -jar target/hai913i-tp2.jar src/test/resources --prefix facts-mini --out sorties-mini

# Chemin 1 : projet Java analysé par le TP 1 (facts-mini réécrit en Java, ou projet de validation du TP 1)
java -jar target/hai913i-tp2.jar src/test/resources/facts-mini-sources --out sorties-mini
java -jar target/hai913i-tp2.jar ../validation

# Image du graphe de couplage
dot -Tpng sorties/graphe-couplage.dot -o sorties/graphe-couplage.png
```

`../validation` désigne le projet de validation du TP 1 (Moodle), placé à côté du dépôt. Les chemins contenant des espaces doivent être entre guillemets.

Sous Windows (`cmd.exe` ou PowerShell) :

```powershell
java -jar target\hai913i-tp2.jar src\test\resources\facts-validation
java -jar target\hai913i-tp2.jar "C:\Mes cours\HAI913I\validation" --out "C:\Mes cours\sorties"
```

### Codes de sortie

| Code | Signification                                                                                   |
|------|-------------------------------------------------------------------------------------------------|
| 0    | faits chargés, calculs et exports effectués                                                     |
| 1    | arguments invalides (argument manquant ou inconnu, `--cp` non numérique ou négatif)             |
| 2    | dossier introuvable, dossier sans aucune classe (ni CSV, ni `.java`), CSV absents ou mal formés, plusieurs jeux sans `--prefix`, faits incohérents, export impossible |
| 3, 4 | chemin 1 seulement : erreur de lecture signalée par le TP 1                                     |

## Validation

Sur le jeu de validation (`src/test/resources/facts-validation`, identique à `resources/` de Moodle) et sur le projet de validation du TP 1 (chemin 1), la sortie est la même, à part les lignes « Dossier des faits » et « Origine des faits ».

| Étape | Sortie attendue                                                                                                   |
|-------|-------------------------------------------------------------------------------------------------------------------|
| A0    | `Classes : 15`, `Méthodes : 59`, `Arcs du graphe : 44` ; sur `facts-mini` : 3 / 5 / 4                                |
| A1    | `Arcs internes écartés : 7`, `\|E_inter\| : 37`, `Couples non nuls : 19 / 105`, symétrie et diagonale nulle `oui`, `Somme des couplages : 1.0000` ; premier couple `Item -- LoanService` à `0.1081` ; `Dvd` et `LoanException` sans couplage |
| A2    | `Nœuds : 15`, `Arêtes : 19` ; cinq arêtes : `Item -- LoanService`, `LoanService -- LoanService.Loan` (4/37), `LibraryApp -- Item`, `LibraryApp -- Catalog`, `LibraryApp -- LoanService` (3/37) ; isolées : `Dvd`, `LoanException` ; `Rechargement du CSV : mêmes nœuds, mêmes arêtes, mêmes poids` |

Sur `facts-mini` : `C(A,B) = 0.6667`, `C(B,C) = 0.3333`, `C(A,C) = 0` (valeurs calculées à la main dans `examples/README.txt`).

Tests automatiques (`mvn clean test`) : 25 tests — `FactsTest` (starter), `FactsSourceTest` (A0, deux chemins), `CouplingTest` (A1), `CouplingGraphTest` (A2, rechargement de l'export).

Deux exécutions successives produisent exactement la même sortie et le même CSV.

## Dépannage

| Symptôme | Cause et remède |
|----------|-----------------|
| `Could not find artifact fr.umontpellier.hai913i:hai913i-tp1-analyzer:jar:0.1.0` | Le TP 1 n'est pas installé dans `~/.m2` : lancer `mvn clean install` dans le dépôt du TP 1, puis reconstruire le TP 2. |
| `release version 17 not supported` ou erreur de compilation liée à Java | Utiliser un JDK 17 ou plus ; vérifier `java -version`, `mvn -v` et `JAVA_HOME`. |
| `Unable to access jarfile target/hai913i-tp2.jar` | Le projet n'est pas construit, ou la commande n'est pas lancée depuis la racine du dépôt. |
| `dossier introuvable` (code 2) | Vérifier le chemin ; le mettre entre guillemets s'il contient des espaces. |
| `plusieurs jeux de données dans …` (code 2) | Le dossier contient plusieurs `*-classes.csv` : ajouter `--prefix facts-mini` (par exemple). |
| `… colonnes attendues` (code 2) | Un CSV ne suit pas le format canonique (séparateur `;`, une ligne d'en-tête, UTF-8). |
| `aucune classe trouvée dans …` (code 2) | Le dossier ne contient ni CSV ni sources Java, ou le CSV des classes est vide : donner le dossier des faits ou la racine du projet Java. |
| `export du graphe impossible` (code 2) | Le dossier de `--out` n'est pas accessible en écriture, ou c'est un fichier existant. |
| `argument inconnu : --cp` | Une option a été donnée sans sa valeur. |
| `dot: command not found` | Installer Graphviz (`sudo apt install graphviz`) ; il ne sert qu'à produire l'image. |
| Accents mal affichés sous Windows | La sortie est en UTF-8 : lancer `chcp 65001` avant la commande. |

## Liens

- Dépôt du TP 2 : <https://github.com/Mixeleoo/TP2_HAI913>
- Dépôt du TP 1 (dépendance) : <https://github.com/Mixeleoo/TP1_HAI913>
