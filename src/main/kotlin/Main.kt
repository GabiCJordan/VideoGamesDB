package com.VideoGamesDB.application

import java.nio.file.Path

fun main() {
    val dirIni = Path.of("datos", "datos_ini")
    val dirFin = Path.of("datos", "datos_fin")

    val rutaCSV = dirIni.resolve("videojuegos.csv")
    val rutaXML = dirIni.resolve("videojuegos.xml")
    val rutaJSON = dirIni.resolve("videojuegos.json")
    val rutaBIN = dirIni.resolve("videojuegos.dat")

    val repository = VideojuegoRepository(rutaCSV, rutaXML, rutaJSON)
    val service = VideojuegoService(repository, rutaCSV, rutaXML, rutaJSON)

    val binRepository = VideojuegoBinRepository(rutaBIN)
    val binService = VideojuegoBinService(binRepository, repository, rutaCSV)

    var opcion: Int

    do {
        println("\n===== CATÁLOGO DE VIDEOJUEGOS =====")
        println("1. Gestión CSV")
        println("2. Leer catálogo desde XML")
        println("3. Leer catálogo desde JSON")
        println("4. Conversiones entre formatos")
        println("5. Gestión fichero BIN")
        println("0. Salir")
        println("===================================")
        print("Elige una opción: ")

        opcion = readlnOrNull()?.toIntOrNull() ?: -1

        when (opcion) {
            1 -> submenuCSV(service)
            2 -> {
                val cantidad = service.integrarDesdeXML()
                println("\nIntegración completada: Se han añadido $cantidad videojuegos nuevos (XML).")
            }
            3 -> {
                val cantidad = service.integrarDesdeJSON()
                println("\nIntegración completada: Se han añadido $cantidad videojuegos nuevos (JSON).")
            }
            4 -> submenuConversiones(service, repository, dirIni, dirFin)
            5 -> submenuBIN(binService)
            0 -> println("Saliendo del programa...")
            else -> println("Opción no válida. Inténtalo de nuevo.")
        }
    } while (opcion != 0)
}

fun submenuBIN(binService: VideojuegoBinService) {
    var opcion: Int

    do {
        println("\n===== CRUD fichero BIN =====")
        println("1. Importar información de un fichero de intercambio")
        println("2. Leer información del fichero binario")
        println("3. Añadir un registro nuevo")
        println("4. Modificar un registro existente (por ID)")
        println("5. Eliminar un registro existente (por ID)")
        println("0. Volver")
        print("Elige una opción: ")

        opcion = readlnOrNull()?.toIntOrNull() ?: -1

        when (opcion) {
            1 -> {
                val importados = binService.importarDesdeCSV()
                if (importados > 0) {
                    println("\nImportación correcta. Se han cargado $importados registros en el fichero binario.")
                } else {
                    println("\nNo se pudo importar información desde el fichero de intercambio CSV.")
                }
            }
            2 -> {
                val juegos = binService.leerTodos()
                println("\n--- Contenido del Fichero Binario ---")
                if (juegos.isEmpty()) {
                    println("El fichero binario está vacío o no se ha creado aún.")
                } else {
                    juegos.forEach {
                        println("ID: ${it.id} | Título: ${it.titulo} | Género: ${it.genero} | Año: ${it.anio} | Nota: ${it.nota}")
                    }
                }
            }
            3 -> {
                var idNuevo: Int?
                do {
                    print("Introduce el nuevo ID (entero positivo): ")
                    idNuevo = readlnOrNull()?.toIntOrNull()
                    if (idNuevo == null || idNuevo <= 0) {
                        println("El ID debe ser un número entero válido.")
                        idNuevo = null
                    } else if (binService.existeId(idNuevo)) {
                        println("Error: El ID $idNuevo ya existe en el fichero binario.")
                        idNuevo = null
                    }
                } while (idNuevo == null)

                print("Título: ")
                val titulo = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "Desconocido"

                print("Género: ")
                val genero = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "Desconocido"

                var anio: Int?
                do {
                    print("Año de lanzamiento: ")
                    anio = readlnOrNull()?.toIntOrNull()
                    if (anio == null) println("Año inválido. Debe ser un número entero.")
                } while (anio == null)

                var nota: Double?
                do {
                    print("Nota [0.0 - 10.0]: ")
                    nota = readlnOrNull()?.toDoubleOrNull()
                    if (nota == null || nota !in 0.0..10.0) {
                        println("Nota inválida. Debe ser un número entre 0.0 y 10.0.")
                        nota = null
                    }
                } while (nota == null)

                binService.añadirRegistro(idNuevo, titulo, genero, anio, nota)
                println("\nRegistro añadido con éxito al final del fichero binario.")
            }
            4 -> {
                var idBuscar: Int?
                do {
                    print("Introduce el ID del registro a modificar: ")
                    idBuscar = readlnOrNull()?.toIntOrNull()
                    if (idBuscar == null) println("Error: Debe ser un número entero.")
                } while (idBuscar == null)

                val resultado = binService.buscarPorId(idBuscar)
                if (resultado == null) {
                    println("\n[Aviso] No existe ningún registro activo con el ID $idBuscar en el fichero binario.")
                } else {
                    val (posicion, juegoExistente) = resultado
                    println("\nRegistro localizado: [ID: ${juegoExistente.id}] Título actual: '${juegoExistente.titulo}'")

                    print("Nuevo título (Enter para mantener '${juegoExistente.titulo}'): ")
                    val nuevoTituloInput = readlnOrNull()
                    val nuevoTitulo = if (nuevoTituloInput.isNullOrBlank()) juegoExistente.titulo else nuevoTituloInput

                    print("Nuevo género (Enter para mantener '${juegoExistente.genero}'): ")
                    val nuevoGeneroInput = readlnOrNull()
                    val nuevoGenero = if (nuevoGeneroInput.isNullOrBlank()) juegoExistente.genero else nuevoGeneroInput

                    print("Nuevo año (Enter para mantener '${juegoExistente.anio}'): ")
                    val nuevoAnioInput = readlnOrNull()?.toIntOrNull()
                    val nuevoAnio = nuevoAnioInput ?: juegoExistente.anio

                    var nuevaNota: Double?
                    do {
                        print("Nueva nota [0.0 - 10.0] (Enter para mantener '${juegoExistente.nota}'): ")
                        val inputNota = readlnOrNull()
                        if (inputNota.isNullOrBlank()) {
                            nuevaNota = juegoExistente.nota
                        } else {
                            nuevaNota = inputNota.toDoubleOrNull()
                            if (nuevaNota == null || nuevaNota !in 0.0..10.0) {
                                println("Nota inválida.")
                                nuevaNota = null
                            }
                        }
                    } while (nuevaNota == null)

                    val juegoActualizado = Videojuego(idBuscar, nuevoTitulo, nuevoGenero, nuevoAnio, nuevaNota)
                    binService.modificarRegistro(posicion, juegoActualizado)
                    println("\nRegistro con ID $idBuscar modificado correctamente en el fichero binario.")
                }
            }
            5 -> {
                var idEliminar: Int?
                do {
                    print("Introduce el ID del registro a eliminar: ")
                    idEliminar = readlnOrNull()?.toIntOrNull()
                    if (idEliminar == null) println("Error: Debe ser un número entero.")
                } while (idEliminar == null)

                val resultado = binService.buscarPorId(idEliminar)
                if (resultado == null) {
                    println("\n[Aviso] No se encontró ningún registro activo con el ID $idEliminar en el fichero binario.")
                } else {
                    val (posicion, juegoExistente) = resultado
                    print("¿Confirma la eliminación del videojuego '${juegoExistente.titulo}' (ID: $idEliminar)? (s/n): ")
                    val confirmacion = readlnOrNull()?.lowercase()

                    if (confirmacion == "s") {
                        binService.eliminarRegistro(posicion)
                        println("\nRegistro con ID $idEliminar eliminado correctamente del fichero binario.")
                    } else {
                        println("\nOperación de eliminación cancelada por el usuario.")
                    }
                }
            }
            0 -> println("Volviendo al menú principal...")
            else -> println("Opción no válida.")
        }
    } while (opcion != 0)
}

fun submenuConversiones(
    service: VideojuegoService,
    repository: VideojuegoRepository,
    dirIni: Path,
    dirFin: Path
) {
    var opcion: Int

    do {
        println("\n--- Conversiones ---")
        println("1. CSV  -> JSON")
        println("2. CSV  -> XML")
        println("3. JSON -> CSV")
        println("4. JSON -> XML")
        println("5. XML  -> CSV")
        println("6. XML  -> JSON")
        println("0. Volver")
        print("Elige una opción: ")

        opcion = readlnOrNull()?.toIntOrNull() ?: -1

        when (opcion) {
            1 -> service.convertir(
                dirIni.resolve("videojuegos.csv"),
                dirFin.resolve("videojuegos.json"),
                repository::leerCSV,
                repository::escribirJSON
            )
            2 -> service.convertir(
                dirIni.resolve("videojuegos.csv"),
                dirFin.resolve("videojuegos.xml"),
                repository::leerCSV,
                repository::escribirXML
            )
            3 -> service.convertir(
                dirIni.resolve("videojuegos.json"),
                dirFin.resolve("videojuegos.csv"),
                repository::leerJSON,
                repository::escribirCSV
            )
            4 -> service.convertir(
                dirIni.resolve("videojuegos.json"),
                dirFin.resolve("videojuegos.xml"),
                repository::leerJSON,
                repository::escribirXML
            )
            5 -> service.convertir(
                dirIni.resolve("videojuegos.xml"),
                dirFin.resolve("videojuegos.csv"),
                repository::leerXML,
                repository::escribirCSV
            )
            6 -> service.convertir(
                dirIni.resolve("videojuegos.xml"),
                dirFin.resolve("videojuegos.json"),
                repository::leerXML,
                repository::escribirJSON
            )
            0 -> println("Volviendo al menú principal...")
            else -> println("Opción no válida.")
        }
    } while (opcion != 0)
}

fun submenuCSV(service: VideojuegoService) {
    var opcion: Int

    do {
        println("\n--- Gestión CSV ---")
        println("1. Listar todos los videojuegos")
        println("2. Buscar videojuego por ID")
        println("3. Añadir nuevo videojuego")
        println("4. Modificar nota de un videojuego")
        println("5. Eliminar videojuego")
        println("0. Volver al menú principal")
        print("Elige una opción: ")

        opcion = readlnOrNull()?.toIntOrNull() ?: -1

        when (opcion) {
            1 -> {
                val juegos = service.listarVideojuegos()
                if (juegos.isEmpty()) println("El catálogo está vacío.")
                else juegos.forEach { println("ID: ${it.id} | Título: ${it.titulo} | Género: ${it.genero} | Año: ${it.anio} | Nota: ${it.nota}") }
            }
            2 -> {
                print("Introduce el ID a buscar: ")
                val id = readlnOrNull()?.toIntOrNull() ?: -1
                val juego = service.buscarPorId(id)
                if (juego != null) println("Encontrado: ID: ${juego.id} | Título: ${juego.titulo} | Género: ${juego.genero} | Año: ${juego.anio} | Nota: ${juego.nota}")
                else println("Error: No existe ningún videojuego con el ID $id.")
            }
            3 -> {
                print("Título: ")
                val titulo = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "Desconocido"

                print("Género: ")
                val genero = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "Desconocido"

                print("Año de lanzamiento: ")
                val anio = readlnOrNull()?.toIntOrNull() ?: 2024

                var nota: Double?
                do {
                    print("Nota [0.0 - 10.0]: ")
                    nota = readlnOrNull()?.toDoubleOrNull()
                    if (nota == null || nota !in 0.0..10.0) {
                        println("Nota inválida. Debe ser un número entre 0.0 y 10.0.")
                        nota = null
                    }
                } while (nota == null)

                service.añadirVideojuego(titulo, genero, anio, nota)
                println("Videojuego añadido correctamente.")
            }
            4 -> {
                print("Introduce el ID del videojuego: ")
                val id = readlnOrNull()?.toIntOrNull() ?: -1
                print("Introduce la nueva nota [0.0 - 10.0]: ")
                val nuevaNota = readlnOrNull()?.toDoubleOrNull() ?: -1.0

                if (nuevaNota in 0.0..10.0) {
                    if (service.modificarNota(id, nuevaNota)) println("Nota modificada correctamente.")
                    else println("Error: No existe ningún videojuego con el ID $id.")
                } else {
                    println("Error: La nota debe estar entre 0.0 y 10.0.")
                }
            }
            5 -> {
                print("Introduce el ID del videojuego a eliminar: ")
                val id = readlnOrNull()?.toIntOrNull() ?: -1
                val juego = service.buscarPorId(id)

                if (juego != null) {
                    print("¿Estás seguro de que deseas eliminar '${juego.titulo}'? (s/n): ")
                    if (readlnOrNull()?.lowercase() == "s") {
                        service.eliminarVideojuego(id)
                        println("Videojuego eliminado correctamente.")
                    } else println("Eliminación cancelada.")
                } else {
                    println("Error: No existe ningún videojuego con el ID $id.")
                }
            }
            0 -> println("Volviendo al menú principal...")
            else -> println("Opción no válida.")
        }
    } while (opcion != 0)
}