package app;

import modelo.Estudiante;
import modelo.Solicitud;
import servicios.RedLaboratorio;
import servicios.RedSocial;
import servicios.Sigac;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public final class Main {
    private final Scanner entrada;
    private final Sigac sistema = new Sigac();

    private Main(Scanner entrada) {
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--demo")) {
            CasoIntegrador.ejecutar();
            return;
        }
        if (args.length != 0) {
            System.err.println("Uso: java -cp build app.Main [--demo]");
            System.exit(2);
        }
        try (Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new Main(entrada).ejecutar();
        }
    }

    private void ejecutar() {
        try {
            menuPrincipal();
        } catch (FinEntrada fin) {
            System.out.println("\nFin de la sesión.");
        }
    }

    private void menuPrincipal() {
        while (true) {
            System.out.println("""

                    === SIGAC-UTA ===
                     1. Registrar estudiante
                     2. Actualizar estudiante
                     3. Eliminar estudiante
                     4. Listar estudiantes
                     5. Comparar búsquedas
                     6. Ver jerarquía académica
                     7. Mostrar recorridos ABB
                     8. Solicitar laboratorio
                     9. Atender solicitud
                    10. Ver cola de solicitudes
                    11. Deshacer operación
                    12. Red de laboratorio
                    13. Red social
                    14. Ejecutar caso integrador
                     0. Salir
                    """);
            try {
                switch (entero("Opción")) {
                    case 0 -> {
                        System.out.println("Fin de la sesión.");
                        return;
                    }
                    case 1 -> {
                        sistema.registrarEstudiante(leerEstudiante(entero("Matrícula")));
                        System.out.println("Estudiante registrado.");
                    }
                    case 2 -> actualizarEstudiante();
                    case 3 -> {
                        sistema.eliminarEstudiante(entero("Matrícula"));
                        System.out.println("Estudiante eliminado.");
                    }
                    case 4 -> listarEstudiantes();
                    case 5 -> compararBusquedas();
                    case 6 -> sistema.jerarquia().forEach(System.out::println);
                    case 7 -> {
                        System.out.println("Preorden  " + sistema.preorden());
                        System.out.println("Inorden   " + sistema.inorden());
                        System.out.println("Posorden  " + sistema.posorden());
                    }
                    case 8 -> {
                        Solicitud solicitud = sistema.solicitarLaboratorio(
                                entero("Matrícula"), texto("Detalle"));
                        System.out.println("Encolada " + formatoSolicitud(solicitud));
                    }
                    case 9 -> atenderSolicitud();
                    case 10 -> verSolicitudes();
                    case 11 -> deshacer();
                    case 12 -> menuLaboratorio();
                    case 13 -> menuSocial();
                    case 14 -> CasoIntegrador.ejecutar();
                    default -> System.out.println("Opción fuera del menú.");
                }
            } catch (IllegalArgumentException error) {
                mostrarError(error);
            }
        }
    }

    private void actualizarEstudiante() {
        int matricula = entero("Matrícula");
        if (sistema.buscarABB(matricula) == null) {
            throw new IllegalArgumentException("No existe la matrícula " + matricula + ".");
        }
        sistema.actualizarEstudiante(leerEstudiante(matricula));
        System.out.println("Datos guardados.");
    }

    private Estudiante leerEstudiante(int matricula) {
        if (matricula <= 0) {
            throw new IllegalArgumentException("La matrícula debe ser un entero positivo.");
        }
        return new Estudiante(matricula, texto("Nombre"), texto("Facultad"), texto("Carrera"));
    }

    private void listarEstudiantes() {
        List<Estudiante> estudiantes = sistema.estudiantes();
        if (estudiantes.isEmpty()) {
            System.out.println("No hay estudiantes registrados.");
            return;
        }
        for (Estudiante estudiante : estudiantes) {
            System.out.printf("%d | %s | %s | %s%n", estudiante.matricula(), estudiante.nombre(),
                    estudiante.facultad(), estudiante.carrera());
        }
    }

    private void compararBusquedas() {
        int matricula = entero("Matrícula");
        Estudiante secuencial = sistema.buscarSecuencial(matricula);
        Estudiante abb = sistema.buscarABB(matricula);
        System.out.printf("Lista enlazada  %s (%d comparaciones)%n",
                resultadoBusqueda(secuencial), sistema.comparacionesSecuencial(matricula));
        System.out.printf("ABB             %s (%d comparaciones)%n",
                resultadoBusqueda(abb), sistema.comparacionesABB(matricula));
    }

    private static String resultadoBusqueda(Estudiante estudiante) {
        return estudiante == null ? "Sin coincidencias" : estudiante.nombre();
    }

    private void atenderSolicitud() {
        Solicitud solicitud = sistema.atenderSolicitud();
        if (solicitud == null) {
            System.out.println("No hay solicitudes pendientes.");
        } else {
            System.out.println("Atendida " + formatoSolicitud(solicitud));
        }
    }

    private void verSolicitudes() {
        List<Solicitud> solicitudes = sistema.solicitudes();
        if (solicitudes.isEmpty()) {
            System.out.println("No hay solicitudes pendientes.");
            return;
        }
        solicitudes.forEach(solicitud -> System.out.println(formatoSolicitud(solicitud)));
    }

    private static String formatoSolicitud(Solicitud solicitud) {
        return "#" + solicitud.numero() + " | matrícula " + solicitud.matricula()
                + " | " + solicitud.detalle();
    }

    private void deshacer() {
        String operacion = sistema.deshacer();
        System.out.println(operacion == null ? "El historial está vacío." : "Se deshizo " + operacion + ".");
    }

    private void menuLaboratorio() {
        RedLaboratorio red = sistema.laboratorio();
        while (true) {
            System.out.println("""

                    === Red de laboratorio ===
                    1. Agregar equipo
                    2. Eliminar equipo
                    3. Conectar equipos
                    4. Eliminar conexión
                    5. BFS
                    6. DFS
                    7. Equipos inaccesibles desde un origen
                    8. Mostrar equipos y conexiones
                    0. Volver
                    """);
            try {
                switch (entero("Opción")) {
                    case 0 -> {
                        return;
                    }
                    case 1 -> {
                        red.agregarEquipo(texto("Equipo"));
                        System.out.println("Equipo agregado.");
                    }
                    case 2 -> {
                        red.eliminarEquipo(texto("Equipo"));
                        System.out.println("Equipo y conexiones incidentes eliminados.");
                    }
                    case 3 -> {
                        red.conectar(texto("Origen"), texto("Destino"), entero("Peso"));
                        System.out.println("Conexión agregada.");
                    }
                    case 4 -> {
                        red.desconectar(texto("Origen"), texto("Destino"));
                        System.out.println("Conexión eliminada.");
                    }
                    case 5 -> System.out.println("BFS  " + red.bfs(texto("Origen")));
                    case 6 -> System.out.println("DFS  " + red.dfs(texto("Origen")));
                    case 7 -> System.out.println("No alcanzables  " + red.noAlcanzables(texto("Origen")));
                    case 8 -> {
                        System.out.println("Equipos  " + red.equipos());
                        System.out.println("Conexiones  " + red.conexiones());
                    }
                    default -> System.out.println("Opción fuera del menú.");
                }
            } catch (IllegalArgumentException error) {
                mostrarError(error);
            }
        }
    }

    private void menuSocial() {
        while (true) {
            System.out.println("""

                    === Red social ===
                    1. Registrar amistad
                    2. Eliminar amistad
                    3. Mostrar amistades
                    4. Recomendar contactos
                    0. Volver
                    """);
            try {
                switch (entero("Opción")) {
                    case 0 -> {
                        return;
                    }
                    case 1 -> {
                        sistema.agregarAmistad(entero("Primera matrícula"), entero("Segunda matrícula"));
                        System.out.println("Amistad registrada.");
                    }
                    case 2 -> {
                        sistema.eliminarAmistad(entero("Primera matrícula"), entero("Segunda matrícula"));
                        System.out.println("Amistad eliminada.");
                    }
                    case 3 -> mostrarAmigos(entero("Matrícula"));
                    case 4 -> mostrarRecomendaciones(entero("Matrícula"));
                    default -> System.out.println("Opción fuera del menú.");
                }
            } catch (IllegalArgumentException error) {
                mostrarError(error);
            }
        }
    }

    private void mostrarAmigos(int matricula) {
        List<Integer> amigos = sistema.amigos(matricula);
        if (amigos.isEmpty()) {
            System.out.println("El estudiante aún no tiene amistades.");
            return;
        }
        for (int id : amigos) {
            System.out.printf("%d | %s%n", id, sistema.buscarABB(id).nombre());
        }
    }

    private void mostrarRecomendaciones(int matricula) {
        List<RedSocial.Recomendacion> recomendaciones = sistema.recomendar(matricula);
        if (recomendaciones.isEmpty()) {
            System.out.println("No hay contactos de segundo grado para recomendar.");
            return;
        }
        for (RedSocial.Recomendacion recomendacion : recomendaciones) {
            Estudiante candidato = sistema.buscarABB(recomendacion.matricula());
            System.out.printf("%d | %s | %d en común%n",
                    candidato.matricula(), candidato.nombre(), recomendacion.amigosEnComun());
        }
    }

    private int entero(String etiqueta) {
        try {
            return Integer.parseInt(texto(etiqueta));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Introduce un entero válido.");
        }
    }

    private String texto(String etiqueta) {
        System.out.print(etiqueta + " > ");
        if (!entrada.hasNextLine()) {
            throw new FinEntrada();
        }
        return entrada.nextLine().strip();
    }

    private static void mostrarError(IllegalArgumentException error) {
        System.out.println("Entrada rechazada. " + error.getMessage());
    }

    private static final class FinEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
