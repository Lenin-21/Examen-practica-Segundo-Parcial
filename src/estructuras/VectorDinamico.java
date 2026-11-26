package estructuras;

import java.util.Objects;

/** Vector con almacenamiento contiguo y crecimiento por duplicación. */
public final class VectorDinamico<T> {
    private Object[] elementos = new Object[8];
    private int tamanio;

    public void agregar(T elemento) {
        insertar(tamanio, elemento);
    }

    public void insertar(int indice, T elemento) {
        Objects.checkIndex(indice, tamanio + 1);
        Objects.requireNonNull(elemento, "El elemento es obligatorio.");
        if (tamanio == elementos.length) {
            Object[] ampliado = new Object[elementos.length * 2];
            System.arraycopy(elementos, 0, ampliado, 0, tamanio);
            elementos = ampliado;
        }
        System.arraycopy(elementos, indice, elementos, indice + 1, tamanio - indice);
        elementos[indice] = elemento;
        tamanio++;
    }

    @SuppressWarnings("unchecked")
    public T obtener(int indice) {
        Objects.checkIndex(indice, tamanio);
        return (T) elementos[indice];
    }

    public void reemplazar(int indice, T elemento) {
        Objects.checkIndex(indice, tamanio);
        elementos[indice] = Objects.requireNonNull(elemento, "El elemento es obligatorio.");
    }

    public T eliminar(int indice) {
        T eliminado = obtener(indice);
        System.arraycopy(elementos, indice + 1, elementos, indice, tamanio - indice - 1);
        elementos[--tamanio] = null;
        return eliminado;
    }

    public int tamanio() {
        return tamanio;
    }
}
