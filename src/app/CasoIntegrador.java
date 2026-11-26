package app;

import modelo.Estudiante;
import modelo.Solicitud;
import servicios.DatosDemostracion;
import servicios.RedLaboratorio;
import servicios.RedSocial;
import servicios.Sigac;

import java.util.ArrayList;
import java.util.List;

final class CasoIntegrador {
    private CasoIntegrador() {}

    static void ejecutar() {
        Sigac sistema = new Sigac();
        DatosDemostracion.cargar(sistema);
        System.out.println("\n=== Caso integrador SIGAC-UTA ===");
        System.out.println("Matrículas ingresadas  "
                + sistema.estudiantes().stream().map(Estudiante::matricula).toList());
        System.out.println("ABB inorden inicial    " + sistema.inorden());
        int ejemplo = 80;
        System.out.printf("Búsqueda de %d          %d comparaciones en la lista, %d en el ABB%n",
                ejemplo, sistema.comparacionesSecuencial(ejemplo), sistema.comparacionesABB(ejemplo));

        sistema.eliminarEstudiante(30);
        System.out.println("ABB tras eliminar 30   " + sistema.inorden());
        Estudiante gabriela = sistema.buscarABB(80);
        sistema.actualizarEstudiante(new Estudiante(80, "Gabriela Pérez", gabriela.facultad(), gabriela.carrera()));
        sistema.solicitarLaboratorio(50, "Práctica de redes");
        sistema.solicitarLaboratorio(70, "Reserva de equipo");
        sistema.solicitarLaboratorio(20, "Soporte de laboratorio");
        List<String> atendidas = new ArrayList<>();
        Solicitud solicitud;
        while ((solicitud = sistema.atenderSolicitud()) != null) {
            atendidas.add("#" + solicitud.numero() + " (" + solicitud.matricula() + ")");
        }
        System.out.println("Atención FIFO          " + String.join(" → ", atendidas));
        System.out.println("Primer deshacer LIFO   " + sistema.deshacer());
        System.out.println("Segundo deshacer LIFO  " + sistema.deshacer());
        System.out.println("ABB tras deshacer      " + sistema.inorden());

        RedLaboratorio red = sistema.laboratorio();
        System.out.println("\nConexiones             " + red.conexiones());
        System.out.println("BFS desde PC1          " + red.bfs("PC1"));
        System.out.println("DFS desde PC1          " + red.dfs("PC1"));
        System.out.println("No alcanzables         " + red.noAlcanzables("PC1"));
        System.out.println("Se elimina SW1 → SRV1");
        red.desconectar("SW1", "SRV1");
        System.out.println("BFS actualizado        " + red.bfs("PC1"));
        System.out.println("No alcanzables         " + red.noAlcanzables("PC1"));

        List<String> recomendaciones = new ArrayList<>();
        for (RedSocial.Recomendacion recomendacion : sistema.recomendar(50)) {
            recomendaciones.add(sistema.buscarABB(recomendacion.matricula()).nombre()
                    + " (" + recomendacion.amigosEnComun() + ")");
        }
        System.out.println("\nContactos para Ana     " + String.join(", ", recomendaciones));
        System.out.println("Los números indican la cantidad de amistades en común.");
    }
}

