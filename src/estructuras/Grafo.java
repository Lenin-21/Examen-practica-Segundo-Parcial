package estructuras;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Grafo dinámico con listas de adyacencia en orden de inserción. */
public final class Grafo<T> {
    public record Conexion<T>(T origen, T destino, int peso) { }

    public record Contacto<T>(T valor, int amigosEnComun) { }

    private static final class Vertice<T> {
        private final T valor;
        private final ListaEnlazada<Arista<T>> salidas = new ListaEnlazada<>();
        private int indice;

        private Vertice(T valor, int indice) {
            this.valor = valor;
            this.indice = indice;
        }
    }

    private record Arista<T>(Vertice<T> destino, int peso) { }

    private final boolean dirigido;
    private final ListaEnlazada<Vertice<T>> vertices = new ListaEnlazada<>();

    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
    }

    public boolean contiene(T valor) {
        return buscarVertice(valor) != null;
    }

    public void agregarVertice(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El identificador del vértice es obligatorio.");
        }
        if (contiene(valor)) {
            throw new IllegalArgumentException("El vértice ya existe: " + valor + ".");
        }
        vertices.agregar(new Vertice<>(valor, vertices.tamanio()));
    }

    public void eliminarVertice(T valor) {
        Vertice<T> eliminado = exigirVertice(valor);
        vertices.eliminar(eliminado.indice);
        // Cada arista saliente se pierde con el vértice. Las entrantes viven en otras listas.
        int indice = 0;
        for (Vertice<T> vertice : vertices) {
            eliminarSalidaSiExiste(vertice, eliminado);
            vertice.indice = indice++;
        }
    }

    public void conectar(T origen, T destino, int peso) {
        Vertice<T> desde = exigirVertice(origen);
        Vertice<T> hacia = exigirVertice(destino);
        if (desde == hacia) {
            throw new IllegalArgumentException("El origen y el destino deben ser distintos.");
        }
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser un entero positivo.");
        }
        if (indiceSalida(desde, hacia) >= 0) {
            throw new IllegalArgumentException("La conexión ya existe.");
        }
        desde.salidas.agregar(new Arista<>(hacia, peso));
        if (!dirigido) {
            hacia.salidas.agregar(new Arista<>(desde, peso));
        }
    }

    public void desconectar(T origen, T destino) {
        Vertice<T> desde = exigirVertice(origen);
        Vertice<T> hacia = exigirVertice(destino);
        if (!eliminarSalidaSiExiste(desde, hacia)) {
            throw new IllegalArgumentException("La conexión indicada no existe.");
        }
        if (!dirigido) {
            eliminarSalidaSiExiste(hacia, desde);
        }
    }

    public List<T> vertices() {
        List<T> valores = new ArrayList<>();
        for (Vertice<T> vertice : vertices) {
            valores.add(vertice.valor);
        }
        return List.copyOf(valores);
    }

    public List<Conexion<T>> conexiones() {
        List<Conexion<T>> resultado = new ArrayList<>();
        for (Vertice<T> vertice : vertices) {
            for (Arista<T> arista : vertice.salidas) {
                if (dirigido || vertice.indice < arista.destino().indice) {
                    resultado.add(new Conexion<>(vertice.valor, arista.destino().valor, arista.peso()));
                }
            }
        }
        return List.copyOf(resultado);
    }

    public List<T> vecinos(T origen) {
        List<T> resultado = new ArrayList<>();
        for (Arista<T> arista : exigirVertice(origen).salidas) {
            resultado.add(arista.destino().valor);
        }
        return List.copyOf(resultado);
    }

    public List<T> bfs(T inicio) {
        return List.copyOf(recorrerEnAnchura(inicio, new boolean[vertices.tamanio()]));
    }

    public List<T> dfs(T inicio) {
        Vertice<T> origen = exigirVertice(inicio);
        List<T> orden = new ArrayList<>();
        boolean[] visitados = new boolean[vertices.tamanio()];
        Pila<Iterator<Arista<T>>> pila = new Pila<>();
        visitados[origen.indice] = true;
        orden.add(origen.valor);
        pila.apilar(origen.salidas.iterator());

        // Cada iterador guarda el siguiente vecino cuando el recorrido baja otro nivel.
        while (pila.tamanio() > 0) {
            Iterator<Arista<T>> pendientes = pila.cima();
            if (!pendientes.hasNext()) {
                pila.desapilar();
                continue;
            }
            Vertice<T> vecino = pendientes.next().destino();
            if (!visitados[vecino.indice]) {
                visitados[vecino.indice] = true;
                orden.add(vecino.valor);
                pila.apilar(vecino.salidas.iterator());
            }
        }
        return List.copyOf(orden);
    }

    public List<T> noAlcanzables(T inicio) {
        boolean[] visitados = new boolean[vertices.tamanio()];
        recorrerEnAnchura(inicio, visitados);
        List<T> resultado = new ArrayList<>();
        for (Vertice<T> vertice : vertices) {
            if (!visitados[vertice.indice]) {
                resultado.add(vertice.valor);
            }
        }
        return List.copyOf(resultado);
    }

    public List<Contacto<T>> contactosSegundoGrado(T valor) {
        Vertice<T> origen = exigirVertice(valor);
        boolean[] excluidos = new boolean[vertices.tamanio()];
        int[] conteos = new int[vertices.tamanio()];
        excluidos[origen.indice] = true;
        for (Arista<T> amistad : origen.salidas) {
            excluidos[amistad.destino().indice] = true;
        }
        for (Arista<T> amistad : origen.salidas) {
            for (Arista<T> posibleContacto : amistad.destino().salidas) {
                int indice = posibleContacto.destino().indice;
                if (!excluidos[indice]) {
                    conteos[indice]++;
                }
            }
        }
        List<Contacto<T>> resultado = new ArrayList<>();
        for (Vertice<T> vertice : vertices) {
            if (conteos[vertice.indice] > 0) {
                resultado.add(new Contacto<>(vertice.valor, conteos[vertice.indice]));
            }
        }
        return List.copyOf(resultado);
    }

    private List<T> recorrerEnAnchura(T inicio, boolean[] visitados) {
        Vertice<T> origen = exigirVertice(inicio);
        List<T> orden = new ArrayList<>();
        Cola<Vertice<T>> pendientes = new Cola<>();
        visitados[origen.indice] = true;
        pendientes.encolar(origen);

        while (pendientes.tamanio() > 0) {
            Vertice<T> actual = pendientes.desencolar();
            orden.add(actual.valor);
            for (Arista<T> arista : actual.salidas) {
                Vertice<T> vecino = arista.destino();
                if (!visitados[vecino.indice]) {
                    visitados[vecino.indice] = true;
                    pendientes.encolar(vecino);
                }
            }
        }
        return orden;
    }

    private Vertice<T> buscarVertice(T valor) {
        for (Vertice<T> vertice : vertices) {
            if (vertice.valor.equals(valor)) {
                return vertice;
            }
        }
        return null;
    }

    private Vertice<T> exigirVertice(T valor) {
        Vertice<T> vertice = buscarVertice(valor);
        if (vertice == null) {
            throw new IllegalArgumentException("No existe el vértice: " + valor + ".");
        }
        return vertice;
    }

    private int indiceSalida(Vertice<T> origen, Vertice<T> destino) {
        int indice = 0;
        for (Arista<T> arista : origen.salidas) {
            if (arista.destino() == destino) {
                return indice;
            }
            indice++;
        }
        return -1;
    }

    private boolean eliminarSalidaSiExiste(Vertice<T> origen, Vertice<T> destino) {
        int indice = indiceSalida(origen, destino);
        if (indice < 0) {
            return false;
        }
        origen.salidas.eliminar(indice);
        return true;
    }
}
