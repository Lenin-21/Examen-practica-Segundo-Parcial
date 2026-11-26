package modelo;

public record Solicitud(int numero, int matricula, String detalle) {
    public Solicitud {
        if (numero <= 0 || matricula <= 0) {
            throw new IllegalArgumentException("El número y la matrícula deben ser positivos.");
        }
        if (detalle == null || detalle.isBlank()) {
            throw new IllegalArgumentException("El detalle de la solicitud es obligatorio.");
        }
        detalle = detalle.strip();
    }
}
