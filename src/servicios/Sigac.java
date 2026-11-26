package servicios;

import estructuras.ArbolABB;
import estructuras.ArbolAcademico;
import estructuras.Cola;
import estructuras.ListaEnlazada;
import estructuras.Pila;
import estructuras.VectorDinamico;
import modelo.Estudiante;
import modelo.Solicitud;

import java.util.ArrayList;
import java.util.List;

public final class Sigac {
    private record Operacion(String descripcion, Runnable revertir) {}

    private final VectorDinamico<Estudiante> vector = new VectorDinamico<>();
    private final ListaEnlazada<Estudiante> lista = new ListaEnlazada<>();
    private final ArbolABB indice = new ArbolABB();
    private final ArbolAcademico arbolAcademico = new ArbolAcademico();
    private final Pila<Operacion> historial = new Pila<>();
    private final Cola<Solicitud> cola = new Cola<>();
    private final RedLaboratorio laboratorio = new RedLaboratorio();
    private final RedSocial redSocial = new RedSocial();
    private int siguienteSolicitud = 1;

    public void registrarEstudiante(Estudiante estudiante) {
        validarEstudiante(estudiante);
        int matricula = estudiante.matricula();
        if (indice.buscar(matricula) != null) {
            throw new IllegalArgumentException("Ya existe la matrícula " + matricula + ".");
        }

        vector.agregar(estudiante);
        lista.agregar(estudiante);
        indice.insertar(estudiante);
        arbolAcademico.agregar(estudiante);
        redSocial.registrar(matricula);
        historial.apilar(new Operacion("Registro de matrícula " + matricula,
                () -> quitarEstudiante(matricula)));
    }

    public void actualizarEstudiante(Estudiante estudiante) {
        validarEstudiante(estudiante);
        int posicion = posicionEstudiante(estudiante.matricula());
        Estudiante anterior = vector.obtener(posicion);
        if (anterior.equals(estudiante)) {
            return;
        }

        boolean mismaUnidad = anterior.facultad().equals(estudiante.facultad())
                && anterior.carrera().equals(estudiante.carrera());
        if (mismaUnidad) {
            arbolAcademico.actualizar(estudiante);
            actualizarDatos(posicion, estudiante);
            historial.apilar(new Operacion("Actualización de matrícula " + estudiante.matricula(), () -> {
                arbolAcademico.actualizar(anterior);
                actualizarDatos(posicion, anterior);
            }));
        } else {
            Runnable restaurarUnidad = arbolAcademico.eliminarReversible(anterior);
            arbolAcademico.agregar(estudiante);
            actualizarDatos(posicion, estudiante);
            historial.apilar(new Operacion("Actualización de matrícula " + estudiante.matricula(), () -> {
                arbolAcademico.eliminar(estudiante);
                restaurarUnidad.run();
                actualizarDatos(posicion, anterior);
            }));
        }
    }

    public void eliminarEstudiante(int matricula) {
        Runnable revertir = quitarEstudiante(matricula);
        historial.apilar(new Operacion("Eliminación de matrícula " + matricula, revertir));
    }

    private Runnable quitarEstudiante(int matricula) {
        int posicion = posicionEstudiante(matricula);
        for (Solicitud solicitud : cola) {
            if (solicitud.matricula() == matricula) {
                throw new IllegalArgumentException(
                        "La matrícula " + matricula + " tiene solicitudes pendientes.");
            }
        }

        Estudiante estudiante = vector.obtener(posicion);
        List<Integer> amigos = redSocial.amigos(matricula);
        Runnable restaurarIndice = indice.eliminarReversible(matricula);
        Runnable restaurarUnidad = arbolAcademico.eliminarReversible(estudiante);
        vector.eliminar(posicion);
        lista.eliminar(posicion);
        redSocial.eliminar(matricula);

        return () -> {
            redSocial.restaurar(matricula, amigos);
            vector.insertar(posicion, estudiante);
            lista.insertar(posicion, estudiante);
            restaurarIndice.run();
            restaurarUnidad.run();
        };
    }

    private void actualizarDatos(int posicion, Estudiante estudiante) {
        vector.reemplazar(posicion, estudiante);
        lista.reemplazar(posicion, estudiante);
        indice.actualizar(estudiante);
    }

    public Estudiante buscarSecuencial(int matricula) {
        validarMatricula(matricula);
        for (Estudiante estudiante : lista) {
            if (estudiante.matricula() == matricula) {
                return estudiante;
            }
        }
        return null;
    }

    public int comparacionesSecuencial(int matricula) {
        validarMatricula(matricula);
        int comparaciones = 0;
        for (Estudiante estudiante : lista) {
            comparaciones++;
            if (estudiante.matricula() == matricula) {
                break;
            }
        }
        return comparaciones;
    }

    public Estudiante buscarABB(int matricula) {
        validarMatricula(matricula);
        return indice.buscar(matricula);
    }

    public int comparacionesABB(int matricula) {
        validarMatricula(matricula);
        return indice.comparacionesBusqueda(matricula);
    }

    public List<Estudiante> estudiantes() {
        List<Estudiante> resultado = new ArrayList<>(vector.tamanio());
        for (int i = 0; i < vector.tamanio(); i++) {
            resultado.add(vector.obtener(i));
        }
        return List.copyOf(resultado);
    }

    public List<Estudiante> listaEstudiantes() {
        List<Estudiante> resultado = new ArrayList<>(lista.tamanio());
        for (Estudiante estudiante : lista) {
            resultado.add(estudiante);
        }
        return List.copyOf(resultado);
    }

    public List<String> jerarquia() {
        return arbolAcademico.jerarquia();
    }

    public List<Integer> preorden() {
        return indice.preorden();
    }

    public List<Integer> inorden() {
        return indice.inorden();
    }

    public List<Integer> posorden() {
        return indice.posorden();
    }

    public Solicitud solicitarLaboratorio(int matricula, String detalle) {
        if (buscarABB(matricula) == null) {
            throw new IllegalArgumentException("No existe la matrícula " + matricula + ".");
        }
        Solicitud solicitud = new Solicitud(siguienteSolicitud, matricula, detalle);
        cola.encolar(solicitud);
        siguienteSolicitud++;
        return solicitud;
    }

    public Solicitud atenderSolicitud() {
        return cola.desencolar();
    }

    public List<Solicitud> solicitudes() {
        List<Solicitud> resultado = new ArrayList<>(cola.tamanio());
        for (Solicitud solicitud : cola) {
            resultado.add(solicitud);
        }
        return List.copyOf(resultado);
    }

    public String deshacer() {
        Operacion operacion = historial.cima();
        if (operacion == null) {
            return null;
        }
        // La operación se retira solo si pudo revertirse.
        operacion.revertir().run();
        historial.desapilar();
        return operacion.descripcion();
    }

    public int operacionesPendientes() {
        return historial.tamanio();
    }

    void limpiarHistorial() {
        while (historial.tamanio() > 0) {
            historial.desapilar();
        }
    }

    public RedLaboratorio laboratorio() {
        return laboratorio;
    }

    public List<Integer> amigos(int matricula) {
        return redSocial.amigos(matricula).stream().sorted().toList();
    }

    public List<RedSocial.Recomendacion> recomendar(int matricula) {
        return redSocial.recomendar(matricula);
    }

    public void agregarAmistad(int primera, int segunda) {
        redSocial.agregarAmistad(primera, segunda);
        historial.apilar(new Operacion("Registro de amistad " + primera + " - " + segunda,
                () -> redSocial.eliminarAmistad(primera, segunda)));
    }

    public void eliminarAmistad(int primera, int segunda) {
        redSocial.eliminarAmistad(primera, segunda);
        historial.apilar(new Operacion("Eliminación de amistad " + primera + " - " + segunda,
                () -> redSocial.agregarAmistad(primera, segunda)));
    }

    private int posicionEstudiante(int matricula) {
        validarMatricula(matricula);
        for (int i = 0; i < vector.tamanio(); i++) {
            if (vector.obtener(i).matricula() == matricula) {
                return i;
            }
        }
        throw new IllegalArgumentException("No existe la matrícula " + matricula + ".");
    }

    private static void validarMatricula(int matricula) {
        if (matricula <= 0) {
            throw new IllegalArgumentException("La matrícula debe ser un entero positivo.");
        }
    }

    private static void validarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante es obligatorio.");
        }
    }
}
