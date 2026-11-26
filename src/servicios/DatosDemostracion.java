package servicios;

import modelo.Estudiante;

public final class DatosDemostracion {
    private DatosDemostracion() {}

    public static void cargar(Sigac sistema) {
        String facultad = "Facultad de Ingeniería en Sistemas, Electrónica e Industrial";
        String software = "Ingeniería en Software";
        sistema.registrarEstudiante(new Estudiante(50, "Ana", facultad, software));
        sistema.registrarEstudiante(new Estudiante(30, "Beto", facultad, software));
        sistema.registrarEstudiante(new Estudiante(70, "Carla", facultad, software));
        sistema.registrarEstudiante(new Estudiante(20, "Diego", facultad, software));
        sistema.registrarEstudiante(new Estudiante(40, "Elena", facultad, software));
        sistema.registrarEstudiante(new Estudiante(60, "Felipe", facultad, software));
        sistema.registrarEstudiante(new Estudiante(80, "Gabriela", facultad, software));

        RedLaboratorio red = sistema.laboratorio();
        for (String equipo : new String[]{"PC1", "SW1", "SRV1", "PC2", "PC3", "PC4"}) {
            red.agregarEquipo(equipo);
        }
        red.conectar("PC1", "SW1", 1);
        red.conectar("SW1", "SRV1", 2);
        red.conectar("SRV1", "PC2", 1);
        red.conectar("PC1", "PC3", 3);

        sistema.agregarAmistad(50, 30);
        sistema.agregarAmistad(50, 70);
        sistema.agregarAmistad(30, 20);
        sistema.agregarAmistad(70, 20);
        sistema.agregarAmistad(70, 40);
        sistema.limpiarHistorial();
    }
}
