package demo;

// Reproduit les arcs de facts-mini partant de A :
// f() -> B.h() (2 sites), g() -> B.h() (1 site), f() -> g() (intra-classe)
public class A {
    private final B b = new B();

    public void f() {
        b.h();
        b.h();
        g();
    }

    public void g() {
        b.h();
    }
}
