package estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import modelo.Estudiante;

/** Árbol general con niveles de universidad, facultad, carrera y estudiante. */
public final class ArbolAcademico {
    private static final class Nodo {
        private final String nombre;
        private final ListaEnlazada<Nodo> hijos = new ListaEnlazada<>();
        private Estudiante estudiante;

        private Nodo(String nombre) {
            this.nombre = nombre;
        }

        private Nodo(Estudiante estudiante) {
            this.nombre = null;
            this.estudiante = estudiante;
        }

        private String etiqueta() {
            if (estudiante == null) {
                return nombre;
            }
            return estudiante.matricula() + " - " + estudiante.nombre();
        }
    }

    private final Nodo raiz = new Nodo("Universidad Técnica de Ambato");

    public void agregar(Estudiante estudiante) {
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio.");
        Nodo facultad = obtenerOCrearGrupo(raiz, estudiante.facultad());
        Nodo carrera = obtenerOCrearGrupo(facultad, estudiante.carrera());
        if (indiceEstudiante(carrera, estudiante.matricula()) < 0) {
            carrera.hijos.agregar(new Nodo(estudiante));
        }
    }

    /** Reemplaza la información de la hoja sin alterar su posición en la carrera. */
    public void actualizar(Estudiante estudiante) {
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio.");
        int posicionFacultad = indiceGrupo(raiz, estudiante.facultad());
        if (posicionFacultad < 0) {
            return;
        }
        Nodo facultad = raiz.hijos.obtener(posicionFacultad);
        int posicionCarrera = indiceGrupo(facultad, estudiante.carrera());
        if (posicionCarrera < 0) {
            return;
        }
        Nodo carrera = facultad.hijos.obtener(posicionCarrera);
        int posicionEstudiante = indiceEstudiante(carrera, estudiante.matricula());
        if (posicionEstudiante >= 0) {
            carrera.hijos.obtener(posicionEstudiante).estudiante = estudiante;
        }
    }

    public void eliminar(Estudiante estudiante) {
        eliminarReversible(estudiante);
    }

    /** Retira la hoja, poda los grupos vacíos y permite restaurar sus posiciones en orden LIFO. */
    public Runnable eliminarReversible(Estudiante estudiante) {
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio.");
        int posicionFacultad = indiceGrupo(raiz, estudiante.facultad());
        if (posicionFacultad < 0) {
            return null;
        }
        Nodo facultad = raiz.hijos.obtener(posicionFacultad);
        int posicionCarrera = indiceGrupo(facultad, estudiante.carrera());
        if (posicionCarrera < 0) {
            return null;
        }
        Nodo carrera = facultad.hijos.obtener(posicionCarrera);
        int posicionEstudiante = indiceEstudiante(carrera, estudiante.matricula());
        if (posicionEstudiante < 0) {
            return null;
        }

        Nodo hoja = carrera.hijos.eliminar(posicionEstudiante);
        boolean podarCarrera = carrera.hijos.tamanio() == 0;
        if (podarCarrera) {
            facultad.hijos.eliminar(posicionCarrera);
        }
        boolean podarFacultad = facultad.hijos.tamanio() == 0;
        if (podarFacultad) {
            raiz.hijos.eliminar(posicionFacultad);
        }

        return () -> {
            if (podarFacultad) {
                raiz.hijos.insertar(posicionFacultad, facultad);
            }
            if (podarCarrera) {
                facultad.hijos.insertar(posicionCarrera, carrera);
            }
            carrera.hijos.insertar(posicionEstudiante, hoja);
        };
    }

    public List<String> jerarquia() {
        List<String> lineas = new ArrayList<>();
        recorrer(raiz, 0, lineas);
        return lineas;
    }

    private void recorrer(Nodo nodo, int nivel, List<String> lineas) {
        lineas.add("  ".repeat(nivel) + nodo.etiqueta());
        for (Nodo hijo : nodo.hijos) {
            recorrer(hijo, nivel + 1, lineas);
        }
    }

    private Nodo obtenerOCrearGrupo(Nodo padre, String nombre) {
        int posicion = indiceGrupo(padre, nombre);
        if (posicion >= 0) {
            return padre.hijos.obtener(posicion);
        }
        Nodo grupo = new Nodo(nombre);
        padre.hijos.agregar(grupo);
        return grupo;
    }

    private int indiceGrupo(Nodo padre, String nombre) {
        int posicion = 0;
        for (Nodo hijo : padre.hijos) {
            if (hijo.nombre.equals(nombre)) {
                return posicion;
            }
            posicion++;
        }
        return -1;
    }

    private int indiceEstudiante(Nodo carrera, int matricula) {
        int posicion = 0;
        for (Nodo hijo : carrera.hijos) {
            if (hijo.estudiante.matricula() == matricula) {
                return posicion;
            }
            posicion++;
        }
        return -1;
    }
}
