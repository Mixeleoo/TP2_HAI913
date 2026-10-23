package demo;

// Reproduit l'arc de facts-mini partant de B : i() -> C.j()
public class B {
    private final C c = new C();

    public void h() {
    }

    public void i() {
        c.j();
    }
}
