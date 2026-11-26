package estructuras;

import java.util.Iterator;

/** Cola FIFO. Las altas se hacen al final y las salidas por el frente. */
public final class Cola<T> implements Iterable<T> {
    private final ListaEnlazada<T> elementos = new ListaEnlazada<>();

    public void encolar(T elemento) {
        elementos.agregar(elemento);
    }

    public T desencolar() {
        return tamanio() == 0 ? null : elementos.eliminar(0);
    }

    public T frente() {
        return tamanio() == 0 ? null : elementos.obtener(0);
    }

    public int tamanio() {
        return elementos.tamanio();
    }

    @Override
    public Iterator<T> iterator() {
        return elementos.iterator();
    }
}
