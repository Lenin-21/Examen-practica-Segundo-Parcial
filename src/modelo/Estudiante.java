package modelo;

public record Estudiante(int matricula, String nombre, String facultad, String carrera) {
    public Estudiante {
        if (matricula <= 0) {
            throw new IllegalArgumentException("La matrícula debe ser un entero positivo.");
        }
        nombre = textoObligatorio(nombre, "El nombre");
        facultad = textoObligatorio(facultad, "La facultad");
        carrera = textoObligatorio(carrera, "La carrera");
    }

    private static String textoObligatorio(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return texto.strip();
    }
}
