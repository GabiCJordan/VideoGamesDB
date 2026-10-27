package com.VideoGamesDB.application

import java.nio.file.Files
import java.nio.file.Path

class VideojuegoService(
    private val repository: VideojuegoRepository,
    private val rutaCSV: Path,
    private val rutaXML: Path,
    private val rutaJSON: Path
) {
    private val listaVideojuegos = repository.leerCSV(rutaCSV).toMutableList()

    fun listarVideojuegos(): List<Videojuego> = listaVideojuegos.toList()

    fun buscarPorId(id: Int): Videojuego? = listaVideojuegos.find { it.id == id }

    fun añadirVideojuego(titulo: String, genero: String, anio: Int, nota: Double) {
        val nuevoId = if (listaVideojuegos.isEmpty()) 1 else listaVideojuegos.maxOf { it.id } + 1
        val nuevoJuego = Videojuego(nuevoId, titulo, genero, anio, nota)
        listaVideojuegos.add(nuevoJuego)
        repository.escribirCSV(rutaCSV, listaVideojuegos)
    }

    fun modificarNota(id: Int, nuevaNota: Double): Boolean {
        val index = listaVideojuegos.indexOfFirst { it.id == id }
        if (index != -1) {
            listaVideojuegos[index] = listaVideojuegos[index].copy(nota = nuevaNota)
            repository.escribirCSV(rutaCSV, listaVideojuegos)
            return true
        }
        return false
    }

    fun eliminarVideojuego(id: Int): Boolean {
        val eliminado = listaVideojuegos.removeIf { it.id == id }
        if (eliminado) repository.escribirCSV(rutaCSV, listaVideojuegos)
        return eliminado
    }

    fun integrarCatalogo(nuevosJuegos: List<Videojuego>): Int {
        if (nuevosJuegos.isEmpty()) {
            println("No se encontraron videojuegos en el archivo.")
            return 0
        }

        nuevosJuegos.forEach {
            println("ID: ${it.id} | Título: ${it.titulo} | Género: ${it.genero} | Año: ${it.anio} | Nota: ${it.nota}")
        }

        var nuevosIntegrados = 0
        for (juego in nuevosJuegos) {
            if (listaVideojuegos.none { it.id == juego.id }) {
                listaVideojuegos.add(juego)
                nuevosIntegrados++
            }
        }

        if (nuevosIntegrados > 0) repository.escribirCSV(rutaCSV, listaVideojuegos)
        return nuevosIntegrados
    }

    fun integrarDesdeXML(): Int = integrarCatalogo(repository.leerXML(rutaXML))

    fun integrarDesdeJSON(): Int = integrarCatalogo(repository.leerJSON(rutaJSON))

    // --- FUNCIÓN GENÉRICA DE CONVERSIÓN ---
    fun convertir(
        origen: Path,
        destino: Path,
        leer: (Path) -> List<Videojuego>,
        escribir: (Path, List<Videojuego>) -> Unit
    ) {
        if (!Files.exists(origen)) {
            println("\n[Error] El fichero de origen no existe en la ruta: $origen")
            return
        }

        val registros = leer(origen)
        if (registros.isEmpty()) {
            println("\n[Error/Aviso] No se pudieron leer registros válidos desde el fichero: $origen")
            return
        }

        try {
            escribir(destino, registros)
            println("\n¡Conversión realizada con éxito!")
            println("Fichero generado: $destino")
            println("Número de registros convertidos: ${registros.size}")
        } catch (e: Exception) {
            println("\n[Error] No se pudo escribir el fichero de destino: ${e.message}")
        }
    }
}