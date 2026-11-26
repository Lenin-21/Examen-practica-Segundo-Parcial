package estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import modelo.Estudiante;

/** Árbol binario de búsqueda cuya clave es la matrícula. */
public final class ArbolABB {
    private static final class Nodo {
        private Estudiante estudiante;
        private Nodo izquierdo;
        private Nodo derecho;

        private Nodo(Estudiante estudiante) {
            this.estudiante = estudiante;
        }
    }

    private record Busqueda(Nodo nodo, int comparaciones) {
    }

    private Nodo raiz;

    public boolean insertar(Estudiante estudiante) {
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio.");
        Nodo padre = null;
        Nodo actual = raiz;
        boolean aLaIzquierda = false;
        while (actual != null) {
            if (estudiante.matricula() == actual.estudiante.matricula()) {
                return false;
            }
            padre = actual;
            aLaIzquierda = estudiante.matricula() < actual.estudiante.matricula();
            actual = aLaIzquierda ? actual.izquierdo : actual.derecho;
        }
        enlazar(padre, aLaIzquierda, new Nodo(estudiante));
        return true;
    }

    public Estudiante buscar(int matricula) {
        Nodo encontrado = localizar(matricula).nodo();
        return encontrado == null ? null : encontrado.estudiante;
    }

    public int comparacionesBusqueda(int matricula) {
        return localizar(matricula).comparaciones();
    }

    public boolean actualizar(Estudiante estudiante) {
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio.");
        Nodo encontrado = localizar(estudiante.matricula()).nodo();
        if (encontrado == null) {
            return false;
        }
        encontrado.estudiante = estudiante;
        return true;
    }

    private Busqueda localizar(int matricula) {
        int comparaciones = 0;
        Nodo actual = raiz;
        while (actual != null) {
            comparaciones++;
            if (matricula == actual.estudiante.matricula()) {
                break;
            }
            actual = matricula < actual.estudiante.matricula() ? actual.izquierdo : actual.derecho;
        }
        return new Busqueda(actual, comparaciones);
    }

    public boolean eliminar(int matricula) {
        return eliminarReversible(matricula) != null;
    }

    /** Retira la clave y devuelve una acción para restaurarla en orden LIFO. */
    public Runnable eliminarReversible(int matricula) {
        Nodo padre = null;
        Nodo actual = raiz;
        while (actual != null && actual.estudiante.matricula() != matricula) {
            padre = actual;
            actual = matricula < actual.estudiante.matricula() ? actual.izquierdo : actual.derecho;
        }
        if (actual == null) {
            return null;
        }
        if (actual.izquierdo != null && actual.derecho != null) {
            return sustituirPorSucesor(actual);
        }

        Nodo eliminado = actual;
        Nodo padreDelEliminado = padre;
        boolean esHijoIzquierdo = padre != null && padre.izquierdo == eliminado;
        Nodo sustituto = eliminado.izquierdo != null ? eliminado.izquierdo : eliminado.derecho;
        enlazar(padreDelEliminado, esHijoIzquierdo, sustituto);
        return () -> enlazar(padreDelEliminado, esHijoIzquierdo, eliminado);
    }

    private Runnable sustituirPorSucesor(Nodo eliminado) {
        Nodo padre = eliminado;
        Nodo minimo = eliminado.derecho;
        while (minimo.izquierdo != null) {
            padre = minimo;
            minimo = minimo.izquierdo;
        }

        Nodo sucesor = minimo;
        Nodo padreDelSucesor = padre;
        boolean esHijoIzquierdo = padreDelSucesor != eliminado;
        Estudiante original = eliminado.estudiante;
        eliminado.estudiante = sucesor.estudiante;
        enlazar(padreDelSucesor, esHijoIzquierdo, sucesor.derecho);
        return () -> {
            eliminado.estudiante = original;
            enlazar(padreDelSucesor, esHijoIzquierdo, sucesor);
        };
    }

    private void enlazar(Nodo padre, boolean aLaIzquierda, Nodo hijo) {
        if (padre == null) {
            raiz = hijo;
        } else if (aLaIzquierda) {
            padre.izquierdo = hijo;
        } else {
            padre.derecho = hijo;
        }
    }

    public List<Integer> preorden() {
        List<Integer> matriculas = new ArrayList<>();
        if (raiz == null) {
            return matriculas;
        }
        Pila<Nodo> pendientes = new Pila<>();
        pendientes.apilar(raiz);
        while (pendientes.tamanio() > 0) {
            Nodo actual = pendientes.desapilar();
            matriculas.add(actual.estudiante.matricula());
            if (actual.derecho != null) {
                pendientes.apilar(actual.derecho);
            }
            if (actual.izquierdo != null) {
                pendientes.apilar(actual.izquierdo);
            }
        }
        return matriculas;
    }

    public List<Integer> inorden() {
        List<Integer> matriculas = new ArrayList<>();
        Pila<Nodo> pendientes = new Pila<>();
        Nodo actual = raiz;
        while (actual != null || pendientes.tamanio() > 0) {
            if (actual != null) {
                pendientes.apilar(actual);
                actual = actual.izquierdo;
            } else {
                actual = pendientes.desapilar();
                matriculas.add(actual.estudiante.matricula());
                actual = actual.derecho;
            }
        }
        return matriculas;
    }

    public List<Integer> posorden() {
        List<Integer> matriculas = new ArrayList<>();
        Pila<Nodo> pendientes = new Pila<>();
        Nodo actual = raiz;
        Nodo ultimoVisitado = null;
        while (actual != null || pendientes.tamanio() > 0) {
            if (actual != null) {
                pendientes.apilar(actual);
                actual = actual.izquierdo;
            } else {
                Nodo cima = pendientes.cima();
                if (cima.derecho != null && cima.derecho != ultimoVisitado) {
                    actual = cima.derecho;
                } else {
                    ultimoVisitado = pendientes.desapilar();
                    matriculas.add(ultimoVisitado.estudiante.matricula());
                }
            }
        }
        return matriculas;
    }
}
