package com.VideoGamesDB.application

import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path

class VideojuegoBinRepository(private val rutaBin: Path) {

    companion object {
        const val TITULO_CHARS = 30
        const val GENERO_CHARS = 20
        // Tamaño total por registro: ID(4) + TITULO(60) + GENERO(40) + ANIO(4) + NOTA(8) + ACTIVO(1) = 117 bytes
        const val RECORD_SIZE = 4 + (TITULO_CHARS * 2) + (GENERO_CHARS * 2) + 4 + 8 + 1
    }

    private fun RandomAccessFile.writeFixedString(text: String, length: Int) {
        val sb = StringBuilder(text)
        sb.setLength(length)
        writeChars(sb.toString())
    }

    private fun RandomAccessFile.readFixedString(length: Int): String {
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(readChar())
        }
        return sb.toString().trim('\u0000', ' ')
    }

    private fun RandomAccessFile.escribirRegistro(juego: Videojuego, activo: Boolean = true) {
        writeInt(juego.id)
        writeFixedString(juego.titulo, TITULO_CHARS)
        writeFixedString(juego.genero, GENERO_CHARS)
        writeInt(juego.anio)
        writeDouble(juego.nota)
        writeBoolean(activo)
    }

    private fun RandomAccessFile.leerRegistro(): Pair<Videojuego, Boolean>? {
        if (filePointer + RECORD_SIZE > length()) return null
        val id = readInt()
        val titulo = readFixedString(TITULO_CHARS)
        val genero = readFixedString(GENERO_CHARS)
        val anio = readInt()
        val nota = readDouble()
        val activo = readBoolean()
        return Pair(Videojuego(id, titulo, genero, anio, nota), activo)
    }

    fun vaciarEImportar(juegos: List<Videojuego>) {
        rutaBin.parent?.let { Files.createDirectories(it) }
        RandomAccessFile(rutaBin.toFile(), "rw").use { raf ->
            raf.setLength(0) // Vacía el fichero binario
            for (juego in juegos) {
                raf.escribirRegistro(juego, activo = true)
            }
        }
    }

    fun leerTodos(): List<Videojuego> {
        if (!Files.exists(rutaBin)) return emptyList()
        val lista = mutableListOf<Videojuego>()
        RandomAccessFile(rutaBin.toFile(), "r").use { raf ->
            while (raf.filePointer < raf.length()) {
                val registro = raf.leerRegistro() ?: break
                if (registro.second) { // Solo recupera los activos
                    lista.add(registro.first)
                }
            }
        }
        return lista
    }

    fun buscarPorId(id: Int): Pair<Long, Videojuego>? {
        if (!Files.exists(rutaBin)) return null
        RandomAccessFile(rutaBin.toFile(), "r").use { raf ->
            while (raf.filePointer < raf.length()) {
                val posInicio = raf.filePointer
                val registro = raf.leerRegistro() ?: break
                if (registro.second && registro.first.id == id) {
                    return Pair(posInicio, registro.first)
                }
            }
        }
        return null
    }

    fun existeId(id: Int): Boolean {
        return buscarPorId(id) != null
    }

    fun añadirRegistro(juego: Videojuego) {
        rutaBin.parent?.let { Files.createDirectories(it) }
        RandomAccessFile(rutaBin.toFile(), "rw").use { raf ->
            raf.seek(raf.length()) // Posiciona el puntero al final del fichero
            raf.escribirRegistro(juego, activo = true)
        }
    }

    fun modificarRegistro(posicion: Long, juegoActualizado: Videojuego) {
        RandomAccessFile(rutaBin.toFile(), "rw").use { raf ->
            raf.seek(posicion)
            raf.escribirRegistro(juegoActualizado, activo = true)
        }
    }

    fun eliminarRegistro(posicion: Long) {
        RandomAccessFile(rutaBin.toFile(), "rw").use { raf ->
            // El campo booleano 'activo' se sitúa al final del registro
            raf.seek(posicion + RECORD_SIZE - 1)
            raf.writeBoolean(false)
        }
    }
}