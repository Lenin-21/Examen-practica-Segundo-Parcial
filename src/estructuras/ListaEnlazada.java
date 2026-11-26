package estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Lista simplemente enlazada con referencias al primero y al último nodo. */
public final class ListaEnlazada<T> implements Iterable<T> {
    private static final class Nodo<T> {
        private T elemento;
        private Nodo<T> siguiente;

        private Nodo(T elemento) {
            this.elemento = elemento;
        }
    }

    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int tamanio;

    public void agregar(T elemento) {
        insertar(tamanio, elemento);
    }

    public void insertar(int indice, T elemento) {
        Objects.checkIndex(indice, tamanio + 1);
        Nodo<T> nuevo = new Nodo<>(Objects.requireNonNull(elemento, "El elemento es obligatorio."));
        if (indice == 0) {
            nuevo.siguiente = primero;
            primero = nuevo;
        } else {
            Nodo<T> anterior = indice == tamanio ? ultimo : nodoEn(indice - 1);
            nuevo.siguiente = anterior.siguiente;
            anterior.siguiente = nuevo;
        }
        if (indice == tamanio) {
            ultimo = nuevo;
        }
        tamanio++;
    }

    public T obtener(int indice) {
        Objects.checkIndex(indice, tamanio);
        return nodoEn(indice).elemento;
    }

    public void reemplazar(int indice, T elemento) {
        Objects.checkIndex(indice, tamanio);
        nodoEn(indice).elemento = Objects.requireNonNull(elemento, "El elemento es obligatorio.");
    }

    public T eliminar(int indice) {
        Objects.checkIndex(indice, tamanio);
        Nodo<T> anterior = indice == 0 ? null : nodoEn(indice - 1);
        Nodo<T> eliminado;
        if (anterior == null) {
            eliminado = primero;
            primero = eliminado.siguiente;
        } else {
            eliminado = anterior.siguiente;
            anterior.siguiente = eliminado.siguiente;
        }
        if (eliminado == ultimo) {
            ultimo = anterior;
        }
        tamanio--;
        return eliminado.elemento;
    }

    public int tamanio() {
        return tamanio;
    }

    private Nodo<T> nodoEn(int indice) {
        Nodo<T> actual = primero;
        for (int posicion = 0; posicion < indice; posicion++) {
            actual = actual.siguiente;
        }
        return actual;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> siguiente = primero;

            @Override
            public boolean hasNext() {
                return siguiente != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T elemento = siguiente.elemento;
                siguiente = siguiente.siguiente;
                return elemento;
            }
        };
    }
}
