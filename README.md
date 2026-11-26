# Examen-practica-Segundo-Parcial
# SIGAC-UTA

El Sistema Inteligente de Gestión Académica y Conectividad de la Universidad Técnica de Ambato es una aplicación de consola desarrollada en Java para el examen práctico de Estructura de Datos del segundo parcial. Permite gestionar estudiantes, atender solicitudes de laboratorio y analizar las conexiones entre equipos y las amistades entre estudiantes.

## Requisitos y ejecución

Se requiere **JDK 17 o posterior**. Comprueba la instalación ejecutando `javac -version` y `java -version`.

Abre en VS Code la carpeta que contiene este README y `src`. Desde la terminal integrada de PowerShell, ejecuta estos comandos.

```powershell
chcp 65001
New-Item -ItemType Directory -Force build | Out-Null
$fuentes = @(Get-ChildItem src -Recurse -File -Filter *.java | ForEach-Object { $_.FullName })
javac --release 17 -encoding UTF-8 -d build @fuentes
java -cp build app.Main
```

El menú inicia con una sesión vacía. Selecciona la **opción 14** para reproducir el caso integrador. Los datos de la sesión se mantienen en memoria mientras se ejecuta el programa.

También puedes ejecutar directamente la demostración y la suite de pruebas.

```powershell
java -cp build app.Main --demo
java -cp build pruebas.PruebasSIGAC
```

## Estructura del proyecto

| Carpeta | Contenido |
| --- | --- |
| [`src/app`](src/app) | Menú principal y demostración del caso integrador. |
| [`src/modelo`](src/modelo) | Modelos de estudiante y solicitud. |
| [`src/estructuras`](src/estructuras) | Implementaciones del vector, lista enlazada, pila, cola, árboles y grafo. |
| [`src/servicios`](src/servicios) | Gestión académica, red de laboratorio y red social. |
| [`src/pruebas`](src/pruebas) | Suite de pruebas automatizadas. |

## Módulos del sistema

| Módulo | Funcionalidad |
| --- | --- |
| **M1 · Estudiantes** | Registra, lista, actualiza y elimina estudiantes con matrícula única. Compara la búsqueda secuencial en la lista enlazada con la búsqueda en el árbol binario de búsqueda (ABB). |
| **M2 · Solicitudes e historial** | Atiende las solicitudes en orden de llegada mediante una cola FIFO. La pila LIFO permite deshacer la última operación reversible. Una solicitud pendiente bloquea la baja de su titular. |
| **M3 · Organización académica** | Muestra la jerarquía Universidad → Facultad → Carrera → Estudiante. El ABB indexa por matrícula y ofrece recorridos en preorden, inorden y posorden. |
| **M4 · Red de laboratorio** | Gestiona equipos y conexiones dirigidas con pesos positivos. Incluye BFS, DFS y detección de equipos no alcanzables desde un origen. |
| **M5 · Red social** | Registra amistades bidireccionales y recomienda contactos de segundo grado. Excluye al usuario y a sus amigos directos. Ordena por cantidad de amigos en común y, en caso de empate, por matrícula ascendente. |
| **M6 · Integración y pruebas** | Reúne las operaciones en un menú y valida duplicados, referencias inválidas y estructuras vacías. |

## Caso integrador

- **Estudiantes y ABB.** Se insertan las matrículas `50, 30, 70, 20, 40, 60, 80`. El inorden inicial es `[20, 30, 40, 50, 60, 70, 80]`. Al eliminar la matrícula 30, su sucesor inorden (40) ocupa su lugar. El sucesor es el menor nodo del subárbol derecho.
- **Cola y pila.** Las solicitudes se atienden en el orden `#1, #2, #3`. Después se deshace la edición de la matrícula 80 y luego la eliminación de la matrícula 30.
- **Red de laboratorio.** Se insertan las conexiones dirigidas `PC1 → SW1` (peso 1), `SW1 → SRV1` (2), `SRV1 → PC2` (1) y `PC1 → PC3` (3), en ese orden. BFS y DFS visitan los vecinos salientes según su orden de inserción. Los pesos se almacenan como atributos de las conexiones.
- **Conectividad.** Desde PC1, BFS devuelve `[PC1, SW1, PC3, SRV1, PC2]` y DFS devuelve `[PC1, SW1, SRV1, PC2, PC3]`. Inicialmente solo PC4 es inaccesible. Después de quitar `SW1 → SRV1`, los equipos no alcanzables son `[SRV1, PC2, PC4]`.
- **Recomendaciones.** Para Ana, el sistema recomienda primero a Diego, con dos amigos en común, y luego a Elena, con uno.

## Complejidad temporal

| Operación | Complejidad |
| --- | --- |
| Agregar al final del vector dinámico | O(1) amortizado |
| Buscar por matrícula en la lista enlazada | O(n) en el peor caso |
| Buscar, insertar o eliminar en el ABB | O(h), donde `h` es la altura del árbol |
| Recorrer el ABB | O(n), donde `n` es el número de estudiantes |
| Encolar, desencolar, apilar o desapilar | O(1) por operación de la estructura |
| BFS y DFS | O(V + E), donde `V` es el número de vértices y `E` el de aristas |

En el caso integrador, buscar la matrícula 80 requiere **7 comparaciones en la lista enlazada** y **3 en el ABB**. Cada estudiante o nodo visitado cuenta como una comparación.

## Pruebas y evidencias

La suite `PruebasSIGAC` contiene **17 casos automatizados**. Incluye matrículas duplicadas, solicitudes pendientes, eliminación de nodos con dos hijos, ciclos en el grafo y empates en las recomendaciones.

La documentación de la entrega se organiza en `docs/matriz-pruebas.md`, `docs/capturas/` y `docs/contribuciones.md`. En el registro de contribuciones, cada integrante describe su aporte y enlaza los commits correspondientes en GitHub.