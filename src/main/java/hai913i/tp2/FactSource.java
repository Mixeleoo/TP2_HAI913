package hai913i.tp2;

import java.util.List;

public interface FactSource {
    public List<String> classes();
    public List<MethodFact> methods();
    public List<CallEdge> calls();
}
