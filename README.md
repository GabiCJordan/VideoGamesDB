# Catálogo de Videojuegos (VideoGamesDB)

Programa de consola desarrollado en Kotlin para la gestión y persistencia multi-formato de un catálogo de videojuegos. El sistema permite realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar), importaciones e integraciones desde archivos **CSV**, **XML** y **JSON**, así como conversiones bidireccionales de formatos y la gestión de almacenamiento eficiente mediante un **fichero binario de acceso aleatorio** (`RandomAccessFile`).

---

## 1. Estructura de Datos

### **Data Class Principal:**
```kotlin
package com.VideoGamesDB.application

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Videojuego(
    @JacksonXmlProperty(localName = "id") @SerialName("id") val id: Int,
    @JacksonXmlProperty(localName = "titulo") @SerialName("titulo") val titulo: String,
    @JacksonXmlProperty(localName = "genero") @SerialName("genero") val genero: String,
    @JacksonXmlProperty(localName = "anio") @SerialName("anio") val anio: Int,
    @JacksonXmlProperty(localName = "nota") @SerialName("nota") val nota: Double
)
```

---

### **Estructura del Registro Binario (`.dat`):**
Para posibilitar el acceso aleatorio directo mediante desplazamientos de puntero (`seek`), se utiliza una longitud fija de registro de **117 bytes**, desglosada de la siguiente manera:

* **`id`**: `Int` — **4 bytes**
* **`titulo`**: `String` de longitud fija (30 caracteres en UTF-16, 2 bytes/carácter) — **60 bytes**
* **`genero`**: `String` de longitud fija (20 caracteres en UTF-16, 2 bytes/carácter) — **40 bytes**
* **`anio`**: `Int` — **4 bytes**
* **`nota`**: `Double` — **8 bytes**
* **`activo`**: `Boolean` (bandera para borrado lógico) — **1 byte**

> **Cálculo Total por Registro:** $4 + 60 + 40 + 4 + 8 + 1 = 117\text{ bytes}$

---

## 2. Estructura del Proyecto

```text
VideoGamesDB/
├── datos/
│   ├── datos_ini/          ← Ficheros fuente de entrada
│   │   ├── videojuegos.csv
│   │   ├── videojuegos.xml
│   │   ├── videojuegos.json
│   │   └── videojuegos.dat
│   └── datos_fin/          ← Ficheros de salida de conversiones
├── src/
│   └── main/
│       └── kotlin/
│           ├── Main.kt                  ← Menús interactivos y consolas
│           ├── Videojuego.kt            ← Data class y modelos XML/JSON
│           ├── VideojuegoRepository.kt  ← Persistencia CSV, XML y JSON
│           ├── VideojuegoService.kt     ← Lógica de negocio e integración
│           ├── VideojuegoBinRepository.kt ← Acceso aleatorio binario
│           └── VideojuegoBinService.kt  ← Operaciones CRUD binarias
├── build.gradle.kts                     ← Configuración de Gradle y dependencias
└── README.md
```

---

## 3. Instrucciones de Ejecución

1. **Requisitos Previos:**
   * Java Development Kit (JDK) 17 o superior.
   * IntelliJ IDEA u otro IDE compatible con proyectos Gradle/Kotlin.

2. **Preparación de Archivos:**
   * Comprueba que los archivos iniciales (`videojuegos.csv`, `videojuegos.xml`, `videojuegos.json`) estén ubicados en la ruta `datos/datos_ini/`.

3. **Compilación y Construcción:**
   * Abre la carpeta raíz del proyecto en IntelliJ IDEA.
   * Deja que Gradle descargue las dependencias requeridas (`kotlin-csv`, `jackson-dataformat-xml`, `kotlinx-serialization-json`).
   * Si experimentas problemas de caché, ejecuta en la terminal del proyecto:
     ```bash
     ./gradlew clean
     ```

4. **Ejecución:**
   * Ubica el archivo `src/main/kotlin/Main.kt` y pulsa sobre la flecha verde de ejecución (`Run Main.kt`).

---

## 4. Decisiones de Diseño

* **Arquitectura en Capas (N-Tier):** Se separaron las responsabilidades en **Repository** (acceso a datos/ficheros), **Service** (lógica de negocio y validaciones) y **Main** (interfaz de usuario e interacciones por consola). Esto facilita el mantenimiento e impide acoplar la lectura de ficheros a la consola.
* **Borrado Lógico en Fichero Binario:** Se añadió un byte extra al final de cada registro binario (`activo: Boolean`). Esto permite marcar registros como eliminados sin necesidad de reescribir todo el fichero físico, optimizando los tiempos de escritura.
* **Tamaños Fijos para Cadenas:**
  * **Título (30 caracteres / 60 bytes):** Permite almacenar títulos largos como *"The Legend of Zelda: Breath of..."* sin desperdiciar almacenamiento excesivo en memoria.
  * **Género (20 caracteres / 40 bytes):** Cubre con margen géneros extensos como *"Acción-Aventura"*.
* **Función Genérica de Conversión:** Para evitar duplicar código en las conversiones entre formatos (CSV, XML, JSON), se definió la función de orden superior `convertir`, que recibe lambdas de lectura y escritura:
  ```kotlin
  fun convertir(
      origen: Path,
      destino: Path,
      leer: (Path) -> List<Videojuego>,
      escribir: (Path, List<Videojuego>) -> Unit
  )
  ```