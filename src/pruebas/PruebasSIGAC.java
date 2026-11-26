package pruebas;

import java.util.List;
import java.util.Objects;
import modelo.Estudiante;
import modelo.Solicitud;
import servicios.DatosDemostracion;
import servicios.RedLaboratorio;
import servicios.RedSocial.Recomendacion;
import servicios.Sigac;

public final class PruebasSIGAC {
    private int aprobadas;
    private int fallidas;

    public static void main(String[] args) {
        new PruebasSIGAC().ejecutar();
    }

    private void ejecutar() {
        probar("P01", "Carga y búsqueda por matrícula", this::cargaYBusqueda);
        probar("P02", "Alta, edición, baja y consistencia", this::crud);
        probar("P03", "Crecimiento y eliminación en los extremos", this::crecimiento);
        probar("P04", "Entradas de estudiantes inválidas", this::erroresEstudiantes);
        probar("P05", "Cola FIFO de tres solicitudes", this::colaFifo);
        probar("P06", "Dos operaciones deshechas en orden LIFO", this::pilaLifo);
        probar("P07", "Validación de solicitudes y de operaciones pendientes", this::solicitudesInvalidas);
        probar("P08", "Jerarquía y tres recorridos del ABB", this::jerarquiaYRecorridos);
        probar("P09", "Eliminación con dos hijos e integridad del ABB", this::eliminarDosHijos);
        probar("P10", "Eliminación de la raíz con un hijo y del último nodo", this::eliminarRaiz);
        probar("P11", "Red del examen, BFS, DFS y desconexión", this::redDelExamen);
        probar("P12", "Ciclo y eliminación de aristas incidentes", this::redConCiclo);
        probar("P13", "Entradas de red inválidas", this::erroresRed);
        probar("P14", "Recomendación de Diego y Elena", this::recomendacionDelExamen);
        probar("P15", "Exclusión, desempate y amistades inválidas", this::reglasSociales);
        probar("P16", "Baja y recuperación de un estudiante con amistades", this::bajaYRecuperacion);
        probar("P17", "Consultas en un sistema vacío", this::sistemaVacio);
        System.out.printf("%nResultado: %d de %d pruebas aprobadas (%d fallidas).%n",
                aprobadas, aprobadas + fallidas, fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private void cargaYBusqueda() {
        Sigac sigac = muestra();
        List<Integer> matriculas = List.of(50, 30, 70, 20, 40, 60, 80);
        igual(matriculas, ids(sigac.estudiantes()));
        igual(matriculas, ids(sigac.listaEstudiantes()));
        igual("Gabriela", sigac.buscarSecuencial(80).nombre());
        igual(sigac.buscarSecuencial(80), sigac.buscarABB(80));
        igual(7, sigac.comparacionesSecuencial(80));
        igual(3, sigac.comparacionesABB(80));
        igual(null, sigac.buscarSecuencial(99));
        igual(null, sigac.buscarABB(99));
        igual(0, sigac.operacionesPendientes());
    }

    private void crud() {
        Sigac sigac = new Sigac();
        Estudiante original = alumno(11, "Sofía");
        Estudiante editado = new Estudiante(11, "Lucía", "Facultad C", "Redes");
        sigac.registrarEstudiante(original);
        List<String> jerarquiaOriginal = sigac.jerarquia();
        sigac.actualizarEstudiante(editado);
        igual(List.of(editado), sigac.estudiantes());
        igual(List.of(editado), sigac.listaEstudiantes());
        igual(editado, sigac.buscarABB(11));
        igual(List.of("Universidad Técnica de Ambato", "  Facultad C", "    Redes", "      11 - Lucía"),
                sigac.jerarquia());
        sigac.deshacer();
        igual(original, sigac.buscarABB(11));
        igual(jerarquiaOriginal, sigac.jerarquia());
        sigac.actualizarEstudiante(editado);
        sigac.eliminarEstudiante(11);
        igual(List.of(), sigac.estudiantes());
        igual(List.of(), sigac.listaEstudiantes());
        igual(null, sigac.buscarABB(11));
        igual(List.of("Universidad Técnica de Ambato"), sigac.jerarquia());
    }

    private void crecimiento() {
        Sigac sigac = new Sigac();
        for (int matricula = 1; matricula <= 35; matricula++) {
            sigac.registrarEstudiante(alumno(matricula, "Estudiante " + matricula));
        }
        List<Estudiante> originales = sigac.estudiantes();
        igual(35, originales.size());
        igual(originales, sigac.listaEstudiantes());
        sigac.eliminarEstudiante(1);
        sigac.eliminarEstudiante(35);
        igual(originales.subList(1, 34), sigac.estudiantes());
        igual(originales.subList(1, 34), sigac.listaEstudiantes());
        sigac.deshacer();
        sigac.deshacer();
        igual(originales, sigac.estudiantes());
        igual(originales, sigac.listaEstudiantes());
    }

    private void erroresEstudiantes() {
        Sigac sigac = new Sigac();
        Estudiante original = alumno(11, "Sofía");
        sigac.registrarEstudiante(original);
        invalida(() -> sigac.registrarEstudiante(alumno(11, "Otra persona")));
        invalida(() -> sigac.registrarEstudiante(null));
        invalida(() -> sigac.registrarEstudiante(alumno(0, "Cero")));
        invalida(() -> sigac.registrarEstudiante(alumno(22, "   ")));
        invalida(() -> sigac.actualizarEstudiante(alumno(99, "Ausente")));
        invalida(() -> sigac.eliminarEstudiante(99));
        invalida(() -> sigac.buscarABB(-1));
        igual(List.of(original), sigac.estudiantes());
        igual(List.of(original), sigac.listaEstudiantes());
        igual(1, sigac.operacionesPendientes());
    }

    private void colaFifo() {
        Sigac sigac = muestra();
        Solicitud primera = sigac.solicitarLaboratorio(50, "Impresión");
        Solicitud segunda = sigac.solicitarLaboratorio(30, "Red");
        Solicitud tercera = sigac.solicitarLaboratorio(50, "Equipo");
        igual(List.of(1, 2, 3), List.of(primera.numero(), segunda.numero(), tercera.numero()));
        igual(List.of(primera, segunda, tercera), sigac.solicitudes());
        igual(primera, sigac.atenderSolicitud());
        igual(segunda, sigac.atenderSolicitud());
        igual(tercera, sigac.atenderSolicitud());
        igual(null, sigac.atenderSolicitud());
        igual(List.of(), sigac.solicitudes());
    }

    private void pilaLifo() {
        Sigac sigac = muestra();
        Estudiante nuevo = alumno(90, "Luis");
        sigac.registrarEstudiante(nuevo);
        sigac.actualizarEstudiante(alumno(90, "Luis Andrés"));
        igual(2, sigac.operacionesPendientes());
        verdadero(sigac.deshacer() != null, "Falta deshacer la edición");
        igual(nuevo, sigac.buscarABB(90));
        igual(1, sigac.operacionesPendientes());
        verdadero(sigac.deshacer() != null, "Falta deshacer el registro");
        igual(null, sigac.buscarABB(90));
        igual(0, sigac.operacionesPendientes());
        igual(null, sigac.deshacer());
    }

    private void solicitudesInvalidas() {
        Sigac sigac = new Sigac();
        Estudiante estudiante = alumno(11, "Sofía");
        sigac.registrarEstudiante(estudiante);
        invalida(() -> sigac.solicitarLaboratorio(99, "Equipo"));
        invalida(() -> sigac.solicitarLaboratorio(11, "  "));
        invalida(() -> sigac.solicitarLaboratorio(11, null));
        Solicitud solicitud = sigac.solicitarLaboratorio(11, "Equipo");
        igual(1, solicitud.numero());
        invalida(() -> sigac.eliminarEstudiante(11));
        invalida(sigac::deshacer);
        igual(estudiante, sigac.buscarABB(11));
        igual(1, sigac.operacionesPendientes());
        igual(List.of(solicitud), sigac.solicitudes());
        igual(solicitud, sigac.atenderSolicitud());
        sigac.deshacer();
        igual(null, sigac.buscarABB(11));
    }

    private void jerarquiaYRecorridos() {
        Sigac sigac = muestra();
        Estudiante ana = sigac.buscarABB(50);
        igual(List.of("Universidad Técnica de Ambato", "  " + ana.facultad(), "    " + ana.carrera(),
                "      50 - Ana"), sigac.jerarquia().subList(0, 4));
        igual(List.of(50, 30, 20, 40, 70, 60, 80), sigac.preorden());
        igual(List.of(20, 30, 40, 50, 60, 70, 80), sigac.inorden());
        igual(List.of(20, 40, 30, 60, 80, 70, 50), sigac.posorden());
    }

    private void eliminarDosHijos() {
        Sigac sigac = muestra();
        List<Integer> preordenOriginal = sigac.preorden();
        List<Integer> posordenOriginal = sigac.posorden();
        sigac.eliminarEstudiante(30);
        igual(null, sigac.buscarABB(30));
        igual(List.of(50, 40, 20, 70, 60, 80), sigac.preorden());
        igual(List.of(20, 40, 50, 60, 70, 80), sigac.inorden());
        for (Estudiante estudiante : sigac.estudiantes()) {
            igual(estudiante, sigac.buscarABB(estudiante.matricula()));
        }
        sigac.eliminarEstudiante(50);
        igual(List.of(60, 40, 20, 70, 80), sigac.preorden());
        igual(List.of(20, 40, 60, 70, 80), sigac.inorden());
        sigac.deshacer();
        igual(List.of(50, 40, 20, 70, 60, 80), sigac.preorden());
        sigac.deshacer();
        igual(preordenOriginal, sigac.preorden());
        igual(posordenOriginal, sigac.posorden());
    }

    private void eliminarRaiz() {
        Sigac sigac = new Sigac();
        sigac.registrarEstudiante(alumno(10, "Luis"));
        sigac.registrarEstudiante(alumno(4, "Beatriz"));
        sigac.eliminarEstudiante(10);
        igual(List.of(4), sigac.preorden());
        sigac.eliminarEstudiante(4);
        igual(List.of(), sigac.inorden());
        igual(null, sigac.buscarABB(4));
    }

    private void redDelExamen() {
        RedLaboratorio laboratorio = muestra().laboratorio();
        igual(List.of("PC1", "SW1", "SRV1", "PC2", "PC3", "PC4"), laboratorio.equipos());
        igual(List.of("PC1", "SW1", "PC3", "SRV1", "PC2"), laboratorio.bfs("PC1"));
        igual(List.of("PC1", "SW1", "SRV1", "PC2", "PC3"), laboratorio.dfs("PC1"));
        igual(List.of("PC4"), laboratorio.noAlcanzables("PC1"));
        igual(List.of("PC2"), laboratorio.bfs("PC2"));
        igual(List.of("PC4"), laboratorio.dfs("PC4"));
        igual(4, laboratorio.conexiones().size());
        verdadero(laboratorio.conexiones().contains("SW1 -> SRV1 (peso 2)"),
                "La arista SW1 a SRV1 debe mostrar su peso");
        laboratorio.desconectar("SW1", "SRV1");
        igual(List.of("PC1", "SW1", "PC3"), laboratorio.bfs("PC1"));
        igual(List.of("SRV1", "PC2", "PC4"), laboratorio.noAlcanzables("PC1"));
        igual(List.of("SRV1", "PC2"), laboratorio.bfs("SRV1"));
    }

    private void redConCiclo() {
        RedLaboratorio laboratorio = new Sigac().laboratorio();
        for (String equipo : List.of("A", "B", "C", "D")) {
            laboratorio.agregarEquipo(equipo);
        }
        laboratorio.conectar("A", "B", 2);
        laboratorio.conectar("A", "C", 3);
        laboratorio.conectar("B", "D", 1);
        laboratorio.conectar("C", "D", 7);
        laboratorio.conectar("D", "A", 9);
        igual(List.of("A", "B", "C", "D"), laboratorio.bfs("A"));
        igual(List.of("A", "B", "D", "C"), laboratorio.dfs("A"));
        igual(List.of(), laboratorio.noAlcanzables("A"));
        laboratorio.eliminarEquipo("B");
        igual(List.of("A", "C", "D"), laboratorio.equipos());
        igual(List.of("A", "C", "D"), laboratorio.bfs("A"));
        igual(3, laboratorio.conexiones().size());
        laboratorio.agregarEquipo("B");
        igual(List.of("B"), laboratorio.noAlcanzables("A"));
        igual(List.of("B"), laboratorio.bfs("B"));
    }

    private void erroresRed() {
        RedLaboratorio laboratorio = muestra().laboratorio();
        invalida(() -> laboratorio.agregarEquipo("PC1"));
        invalida(() -> laboratorio.agregarEquipo(" pc1 "));
        invalida(() -> laboratorio.agregarEquipo(null));
        invalida(() -> laboratorio.agregarEquipo(" "));
        invalida(() -> laboratorio.conectar("PC1", "SW1", 5));
        invalida(() -> laboratorio.conectar("PC4", "PC3", 0));
        invalida(() -> laboratorio.conectar("PC1", "XX", 3));
        invalida(() -> laboratorio.desconectar("SW1", "PC1"));
        invalida(() -> laboratorio.bfs("XX"));
        invalida(() -> laboratorio.eliminarEquipo("XX"));
        igual(4, laboratorio.conexiones().size());
    }

    private void recomendacionDelExamen() {
        Sigac sigac = muestra();
        igual(List.of(30, 70), sigac.amigos(50));
        verdadero(sigac.amigos(30).contains(50), "La amistad debe existir en ambos sentidos");
        igual(List.of(new Recomendacion(20, 2), new Recomendacion(40, 1)), sigac.recomendar(50));
        igual("Diego", sigac.buscarABB(20).nombre());
        igual("Elena", sigac.buscarABB(40).nombre());
        igual(List.of(), sigac.recomendar(60));
    }

    private void reglasSociales() {
        Sigac sigac = new Sigac();
        for (int matricula = 1; matricula <= 5; matricula++) {
            sigac.registrarEstudiante(alumno(matricula, "Estudiante " + matricula));
        }
        sigac.agregarAmistad(1, 2);
        sigac.agregarAmistad(1, 3);
        sigac.agregarAmistad(2, 3);
        sigac.agregarAmistad(2, 5);
        sigac.agregarAmistad(3, 4);
        igual(List.of(new Recomendacion(4, 1), new Recomendacion(5, 1)), sigac.recomendar(1));
        invalida(() -> sigac.agregarAmistad(2, 1));
        invalida(() -> sigac.agregarAmistad(1, 1));
        invalida(() -> sigac.agregarAmistad(1, 99));
        invalida(() -> sigac.recomendar(99));
        invalida(() -> sigac.eliminarAmistad(1, 4));
        sigac.eliminarAmistad(3, 4);
        igual(List.of(new Recomendacion(5, 1)), sigac.recomendar(1));
        sigac.deshacer();
        igual(List.of(new Recomendacion(4, 1), new Recomendacion(5, 1)), sigac.recomendar(1));
    }

    private void bajaYRecuperacion() {
        Sigac sigac = muestra();
        List<Estudiante> originales = sigac.estudiantes();
        List<String> jerarquiaOriginal = sigac.jerarquia();
        sigac.eliminarEstudiante(30);
        igual(List.of(70), sigac.amigos(50));
        igual(List.of(70), sigac.amigos(20));
        igual(List.of(new Recomendacion(20, 1), new Recomendacion(40, 1)), sigac.recomendar(50));
        invalida(() -> sigac.amigos(30));
        sigac.deshacer();
        igual(originales, sigac.estudiantes());
        igual(originales, sigac.listaEstudiantes());
        igual(jerarquiaOriginal, sigac.jerarquia());
        igual(List.of(20, 30, 40, 50, 60, 70, 80), sigac.inorden());
        igual(List.of(new Recomendacion(20, 2), new Recomendacion(40, 1)), sigac.recomendar(50));
    }

    private void sistemaVacio() {
        Sigac sigac = new Sigac();
        igual(List.of(), sigac.estudiantes());
        igual(List.of(), sigac.listaEstudiantes());
        igual(List.of(), sigac.inorden());
        igual(0, sigac.comparacionesSecuencial(1));
        igual(0, sigac.comparacionesABB(1));
        igual(null, sigac.buscarSecuencial(1));
        igual(null, sigac.buscarABB(1));
        igual(null, sigac.atenderSolicitud());
        igual(null, sigac.deshacer());
        igual(List.of(), sigac.laboratorio().equipos());
        invalida(() -> sigac.laboratorio().dfs("A"));
    }

    private void probar(String id, String nombre, Runnable caso) {
        try {
            caso.run();
            aprobadas++;
            System.out.printf("[OK] %s %s%n", id, nombre);
        } catch (RuntimeException | AssertionError error) {
            fallidas++;
            System.out.printf("[FALLO] %s %s. %s%n", id, nombre, error.getMessage());
        }
    }

    private static Sigac muestra() {
        Sigac sigac = new Sigac();
        DatosDemostracion.cargar(sigac);
        return sigac;
    }

    private static Estudiante alumno(int matricula, String nombre) {
        return new Estudiante(matricula, nombre, "Facultad de Prueba", "Software");
    }

    private static List<Integer> ids(List<Estudiante> estudiantes) {
        return estudiantes.stream().map(Estudiante::matricula).toList();
    }

    private static void igual(Object esperado, Object recibido) {
        if (!Objects.equals(esperado, recibido)) {
            throw new AssertionError("Se esperaba " + esperado + " y se obtuvo " + recibido);
        }
    }

    private static void verdadero(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void invalida(Runnable operacion) {
        try {
            operacion.run();
        } catch (IllegalArgumentException esperada) {
            return;
        }
        throw new AssertionError("La operación debía rechazar la entrada");
    }
}
