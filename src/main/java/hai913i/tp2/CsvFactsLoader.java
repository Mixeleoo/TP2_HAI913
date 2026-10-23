package hai913i.tp2;

import hai913i.tp2.model.CallEdge;
import hai913i.tp2.model.ClassFact;
import hai913i.tp2.model.MethodFact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Faits au format canonique (trois CSV de même préfixe), lus par la classe Facts du starter.
// Sert au micro jeu de données facts-mini, qui n'existe qu'en CSV.
public class CsvFactsLoader implements FactsSource {
    private final Facts facts;

    // le préfixe est déduit du fichier *-classes.csv du dossier
    public CsvFactsLoader(Path dir) throws IOException {
        this.facts = Facts.load(dir);
    }

    public CsvFactsLoader(Path dir, String prefix) throws IOException {
        this.facts = Facts.load(dir, prefix);
    }

    @Override
    public List<ClassFact> getClasses() {
        List<ClassFact> classes = new ArrayList<>();
        facts.classes().forEach(c -> classes.add(new ClassFact(c.name())));
        return classes;
    }

    @Override
    public List<MethodFact> getMethods() {
        List<MethodFact> result = new ArrayList<>();
        facts.methodOwners().forEach((id, owner) ->
            result.add(new MethodFact(id, nameOf(id), owner)));
        return result;
    }

    @Override
    public List<CallEdge> getCalls() {
        List<CallEdge> result = new ArrayList<>();
        facts.calls().forEach(a ->
            result.add(new CallEdge(a.caller(), a.callerClass(), a.callee(), a.calleeClass())));
        return result;
    }

    // "demo.A#f(int)" -> "f"
    private static String nameOf(String id) {
        return id.substring(id.indexOf('#') + 1, id.indexOf('('));
    }
}
