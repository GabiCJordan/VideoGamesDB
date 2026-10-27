package com.VideoGamesDB.application

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

class VideojuegoRepository(
    private val rutaCSV: Path,
    private val rutaXML: Path,
    private val rutaJSON: Path
) {

    // ==================== LECTURA ====================

    fun leerCSV(ruta: Path = rutaCSV): List<Videojuego> {
        if (!Files.exists(ruta) || !Files.isReadable(ruta)) {
            println("Error: No se puede leer el fichero en la ruta: $ruta")
            return emptyList()
        }

        return csvReader { delimiter = ';' }.readAll(ruta.toFile()).mapNotNull { columnas ->
            if (columnas.size >= 5) {
                try {
                    Videojuego(
                        columnas[0].toInt(),
                        columnas[1],
                        columnas[2],
                        columnas[3].toInt(),
                        columnas[4].toDouble()
                    )
                } catch (e: Exception) { null }
            } else null
        }
    }

    fun leerXML(ruta: Path = rutaXML): List<Videojuego> {
        if (!Files.exists(ruta) || !Files.isReadable(ruta)) {
            println("Aviso: No se encuentra o no se puede leer el fichero XML en $ruta")
            return emptyList()
        }

        return try {
            val xmlMapper = XmlMapper().registerModule(KotlinModule.Builder().build())
            val contenedor: VideojuegosXML = xmlMapper.readValue(ruta.toFile())
            contenedor.listaJuegos
        } catch (e: Exception) {
            println("Error al leer el XML: ${e.message}")
            emptyList()
        }
    }

    fun leerJSON(ruta: Path = rutaJSON): List<Videojuego> {
        if (!Files.exists(ruta) || !Files.isReadable(ruta)) {
            println("Aviso: No se encuentra o no se puede leer el fichero JSON en $ruta")
            return emptyList()
        }

        return try {
            val jsonString = Files.readString(ruta)
            Json.decodeFromString<List<Videojuego>>(jsonString)
        } catch (e: Exception) {
            println("Error al leer el JSON: ${e.message}")
            emptyList()
        }
    }

    // ==================== ESCRITURA ====================

    // Guarda por defecto en la ruta principal del CSV (datos/datos_ini/videojuegos.csv)
    fun guardarCSV(lista: List<Videojuego>) {
        escribirCSV(rutaCSV, lista)
    }

    // Escribe el CSV en la ruta especificada por parámetro (útil para datos_fin/)
    fun escribirCSV(ruta: Path, lista: List<Videojuego>) {
        try {
            ruta.parent?.let { Files.createDirectories(it) }
            csvWriter { delimiter = ';' }.writeAll(
                lista.map { listOf(it.id.toString(), it.titulo, it.genero, it.anio.toString(), it.nota.toString()) },
                ruta.toFile()
            )
        } catch (e: Exception) {
            println("Error al escribir el fichero CSV: ${e.message}")
        }
    }

    fun escribirXML(ruta: Path, lista: List<Videojuego>) {
        try {
            ruta.parent?.let { Files.createDirectories(it) }
            val xmlMapper = XmlMapper().registerModule(KotlinModule.Builder().build())
            val contenedor = VideojuegosXML(lista)
            val xmlString = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contenedor)
            ruta.toFile().writeText(xmlString)
        } catch (e: Exception) {
            println("Error al escribir el fichero XML: ${e.message}")
        }
    }

    fun escribirJSON(ruta: Path, lista: List<Videojuego>) {
        try {
            ruta.parent?.let { Files.createDirectories(it) }
            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(lista)
            ruta.toFile().writeText(jsonString)
        } catch (e: Exception) {
            println("Error al escribir el fichero JSON: ${e.message}")
        }
    }
}