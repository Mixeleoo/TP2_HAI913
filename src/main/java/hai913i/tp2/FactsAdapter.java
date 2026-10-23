package hai913i.tp2;

import hai913i.tp1.extract.FilesExtractor;
import hai913i.tp1.graph.CallGraph;
import hai913i.tp1.model.MethodInfo;
import hai913i.tp1.model.ProjectInfo;
import hai913i.tp2.model.CallEdge;
import hai913i.tp2.model.ClassFact;
import hai913i.tp2.model.MethodFact;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Chemin 1 : les faits viennent du modèle du TP1 (sources Java analysées par JDT).
// C'est la seule classe du TP2 qui connaît les classes du TP1.
public class FactsAdapter implements FactsSource {
    private final ProjectInfo model;
    private final CallGraph graph;
    // signature d'une méthode -> nom qualifié de sa classe
    private final Map<String, String> owners = new HashMap<>();

    public FactsAdapter(Path source) {
        FilesExtractor filesExtractor = new FilesExtractor(source);
        this.model = filesExtractor.extractProject();
        // on construit le graphe sur le même modèle plutôt que de relancer l'extraction
        this.graph = CallGraph.build(model.types());
        model.types().forEach(type -> {
            for (MethodInfo m : type.methods)
                owners.put(m.signature(), type.qualifiedName);
        });
    }

    // Récupère les noms des classes
    @Override
    public List<ClassFact> getClasses() {
        List<ClassFact> classes = new ArrayList<>();
        model.types().forEach(type -> {
            classes.add(new ClassFact(type.qualifiedName));
        });

        return classes;
    }

    @Override
    public List<MethodFact> getMethods() {
        List<MethodFact> result = new ArrayList<>();
        model.types().forEach(type -> {
            for (MethodInfo m : type.methods)
                result.add(new MethodFact(m.signature(), m.name(), type.qualifiedName));
        });
        return result;
    }

    // Un arc par couple (appelant, appelé) : le poids du TP1 (nombre de sites) n'est pas repris
    @Override
    public List<CallEdge> getCalls() {
        List<CallEdge> result = new ArrayList<>();
        graph.arcs.forEach((caller, callees) ->
            callees.forEach((callee, weight) ->
                result.add(new CallEdge(caller, owners.get(caller), callee, owners.get(callee)))));
        return result;
    }
}
