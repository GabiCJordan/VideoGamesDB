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

@JacksonXmlRootElement(localName = "videojuegos")
data class VideojuegosXML(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "videojuego")
    val listaJuegos: List<Videojuego> = emptyList()
)