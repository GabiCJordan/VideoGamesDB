package com.VideoGamesDB.application

import java.nio.file.Path

class VideojuegoBinService(
    private val repositoryBin: VideojuegoBinRepository,
    private val repositoryCSV: VideojuegoRepository,
    private val rutaCSV: Path
) {

    fun importarDesdeCSV(): Int {
        val juegos = repositoryCSV.leerCSV(rutaCSV)
        if (juegos.isEmpty()) return 0
        repositoryBin.vaciarEImportar(juegos)
        return juegos.size
    }

    fun leerTodos(): List<Videojuego> = repositoryBin.leerTodos()

    fun existeId(id: Int): Boolean = repositoryBin.existeId(id)

    fun buscarPorId(id: Int): Pair<Long, Videojuego>? = repositoryBin.buscarPorId(id)

    fun añadirRegistro(id: Int, titulo: String, genero: String, anio: Int, nota: Double) {
        val juego = Videojuego(id, titulo, genero, anio, nota)
        repositoryBin.añadirRegistro(juego)
    }

    fun modificarRegistro(posicion: Long, juegoActualizado: Videojuego) {
        repositoryBin.modificarRegistro(posicion, juegoActualizado)
    }

    fun eliminarRegistro(posicion: Long) {
        repositoryBin.eliminarRegistro(posicion)
    }
}