package servicios;

import estructuras.Grafo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RedLaboratorio {
    private final Grafo<String> grafo = new Grafo<>(true);

    public void agregarEquipo(String codigo) {
        grafo.agregarVertice(normalizar(codigo));
    }

    public void eliminarEquipo(String codigo) {
        grafo.eliminarVertice(normalizar(codigo));
    }

    public void conectar(String origen, String destino, int peso) {
        grafo.conectar(normalizar(origen), normalizar(destino), peso);
    }

    public void desconectar(String origen, String destino) {
        grafo.desconectar(normalizar(origen), normalizar(destino));
    }

    public List<String> bfs(String inicio) {
        return grafo.bfs(normalizar(inicio));
    }

    public List<String> dfs(String inicio) {
        return grafo.dfs(normalizar(inicio));
    }

    public List<String> noAlcanzables(String inicio) {
        return grafo.noAlcanzables(normalizar(inicio));
    }

    public List<String> equipos() {
        return grafo.vertices();
    }

    public List<String> conexiones() {
        List<String> resultado = new ArrayList<>();
        for (Grafo.Conexion<String> conexion : grafo.conexiones()) {
            resultado.add(conexion.origen() + " -> " + conexion.destino() + " (peso " + conexion.peso() + ")");
        }
        return List.copyOf(resultado);
    }

    private String normalizar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del equipo es obligatorio.");
        }
        return codigo.strip().toUpperCase(Locale.ROOT);
    }
}
