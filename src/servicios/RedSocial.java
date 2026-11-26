package servicios;

import estructuras.Grafo;
import java.util.ArrayList;
import java.util.List;

public final class RedSocial {
    public record Recomendacion(int matricula, int amigosEnComun) { }

    private final Grafo<Integer> grafo = new Grafo<>(false);

    public void registrar(int matricula) {
        validarMatricula(matricula);
        grafo.agregarVertice(matricula);
    }

    public void eliminar(int matricula) {
        validarMatricula(matricula);
        grafo.eliminarVertice(matricula);
    }

    public void agregarAmistad(int primera, int segunda) {
        validarMatricula(primera);
        validarMatricula(segunda);
        grafo.conectar(primera, segunda, 1);
    }

    public void eliminarAmistad(int primera, int segunda) {
        validarMatricula(primera);
        validarMatricula(segunda);
        grafo.desconectar(primera, segunda);
    }

    public List<Integer> amigos(int matricula) {
        validarMatricula(matricula);
        return grafo.vecinos(matricula);
    }

    public void restaurar(int matricula, List<Integer> amistades) {
        validarMatricula(matricula);
        if (amistades == null) {
            throw new IllegalArgumentException("La lista de amistades es obligatoria.");
        }
        List<Integer> validadas = new ArrayList<>();
        for (Integer amistad : amistades) {
            if (amistad == null || amistad == matricula || !grafo.contiene(amistad)) {
                throw new IllegalArgumentException("Amistad inválida al restaurar: " + amistad + ".");
            }
            if (validadas.contains(amistad)) {
                throw new IllegalArgumentException("Amistad repetida al restaurar: " + amistad + ".");
            }
            validadas.add(amistad);
        }
        registrar(matricula);
        for (int amistad : validadas) {
            agregarAmistad(matricula, amistad);
        }
    }

    public List<Recomendacion> recomendar(int matricula) {
        validarMatricula(matricula);
        List<Recomendacion> resultado = new ArrayList<>();
        for (Grafo.Contacto<Integer> contacto : grafo.contactosSegundoGrado(matricula)) {
            resultado.add(new Recomendacion(contacto.valor(), contacto.amigosEnComun()));
        }

        // Ordenamiento por inserción sobre el resultado del conteo.
        for (int indice = 1; indice < resultado.size(); indice++) {
            Recomendacion actual = resultado.get(indice);
            int anterior = indice - 1;
            while (anterior >= 0 && precede(actual, resultado.get(anterior))) {
                resultado.set(anterior + 1, resultado.get(anterior));
                anterior--;
            }
            resultado.set(anterior + 1, actual);
        }
        return List.copyOf(resultado);
    }

    private boolean precede(Recomendacion primera, Recomendacion segunda) {
        if (primera.amigosEnComun() != segunda.amigosEnComun()) {
            return primera.amigosEnComun() > segunda.amigosEnComun();
        }
        return primera.matricula() < segunda.matricula();
    }

    private void validarMatricula(int matricula) {
        if (matricula <= 0) {
            throw new IllegalArgumentException("La matrícula debe ser un entero positivo.");
        }
    }
}
