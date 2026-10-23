package hai913i.tp2;

import hai913i.tp2.model.CallEdge;
import hai913i.tp2.model.ClassFact;
import hai913i.tp2.model.MethodFact;

import java.util.List;

public interface FactsSource {
    List<ClassFact> getClasses();
    List<MethodFact> getMethods();
    List<CallEdge> getCalls();
}
