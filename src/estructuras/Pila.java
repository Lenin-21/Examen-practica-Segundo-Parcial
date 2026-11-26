package estructuras;

/** Pila LIFO. La cima se almacena al inicio de la lista enlazada. */
public final class Pila<T> {
    private final ListaEnlazada<T> elementos = new ListaEnlazada<>();

    public void apilar(T elemento) {
        elementos.insertar(0, elemento);
    }

    public T desapilar() {
        return tamanio() == 0 ? null : elementos.eliminar(0);
    }

    public T cima() {
        return tamanio() == 0 ? null : elementos.obtener(0);
    }

    public int tamanio() {
        return elementos.tamanio();
    }
}
