package com.example.speccyose5ultrav021b

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

object SystemMetadataManager {
    data class SystemMetadata(
        val name: String = "",
        val description: String = "",
        val manufacturer: String = "",
        val releaseYear: String = ""
    )

    fun getMetadataForSystem(context: Context, systemId: String, lang: String): SystemMetadata {
        val assetPath = "contentimg/system-metadata/${systemId.lowercase()}.xml"
        return try {
            val inputStream = context.assets.open(assetPath)
            parseXml(inputStream, lang)
        } catch (e: Exception) {
            SystemMetadata(name = systemId.uppercase())
        }
    }

    private fun parseXml(inputStream: InputStream, targetLang: String): SystemMetadata {
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, null)
        
        var name = ""
        var description = ""
        var manufacturer = ""
        var releaseYear = ""
        
        var currentLang: String? = null
        var inVariables = false
        
        // Primero guardamos los valores por defecto (fuera de etiquetas <language>)
        var defaultName = ""
        var defaultDesc = ""
        var defaultMan = ""
        var defaultYear = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "language") {
                        currentLang = parser.getAttributeValue(null, "name")
                    } else if (tagName == "variables") {
                        inVariables = true
                    } else if (inVariables) {
                        val text = parser.nextText()
                        if (currentLang == null) {
                            when (tagName) {
                                "systemName" -> defaultName = text
                                "systemDescription" -> defaultDesc = text
                                "systemManufacturer" -> defaultMan = text
                                "systemReleaseYear" -> defaultYear = text
                            }
                        } else if (currentLang.startsWith(targetLang)) {
                            when (tagName) {
                                "systemName" -> name = text
                                "systemDescription" -> description = text
                                "systemManufacturer" -> manufacturer = text
                                "systemReleaseYear" -> releaseYear = text
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "language") currentLang = null
                    else if (tagName == "variables") inVariables = false
                }
            }
            eventType = parser.next()
        }
        
        return SystemMetadata(
            name = name.ifEmpty { defaultName },
            description = description.ifEmpty { defaultDesc },
            manufacturer = manufacturer.ifEmpty { defaultMan },
            releaseYear = releaseYear.ifEmpty { defaultYear }
        )
    }
}
