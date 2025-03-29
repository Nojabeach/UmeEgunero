package com.tfg.umeegunero.util

import com.tfg.umeegunero.data.model.Usuario

/**
 * Clase de utilidades para exportar datos de usuarios a diferentes formatos.
 */
object ExportUtils {
    
    /**
     * Genera contenido CSV a partir de una lista de usuarios.
     * 
     * @param usuarios Lista de usuarios a exportar
     * @return String con el contenido CSV
     */
    fun generateCSVContent(usuarios: List<Usuario>): String {
        val csvBuilder = StringBuilder()
        
        // Cabecera
        csvBuilder.append("DNI,Nombre,Apellidos,Email,Teléfono,Activo,Fecha Registro,Perfiles\n")
        
        // Datos
        usuarios.forEach { usuario ->
            val perfiles = usuario.perfiles.joinToString("|") { 
                "${it.tipo.name}:${it.centroId ?: ""}" 
            }
            
            csvBuilder.append("\"${usuario.dni}\",")
            csvBuilder.append("\"${usuario.nombre}\",")
            csvBuilder.append("\"${usuario.apellidos}\",")
            csvBuilder.append("\"${usuario.email}\",")
            csvBuilder.append("\"${usuario.telefono}\",")
            csvBuilder.append("\"${if (usuario.activo) "Sí" else "No"}\",")
            csvBuilder.append("\"${formatDate(usuario.fechaRegistro)}\",")
            csvBuilder.append("\"$perfiles\"\n")
        }
        
        return csvBuilder.toString()
    }
    
    /**
     * Genera contenido para un PDF a partir de una lista de usuarios.
     * Esta es una representación simplificada, en una implementación real
     * se utilizaría una biblioteca como iText o PDFBox para generar el PDF.
     * 
     * @param usuarios Lista de usuarios a exportar
     * @return String con una representación del contenido para PDF
     */
    fun generatePDFContent(usuarios: List<Usuario>): String {
        // En una implementación real, aquí se generaría el PDF con una biblioteca adecuada
        // Por ahora, sólo generaremos un texto representativo con formato similar a HTML
        
        val content = StringBuilder()
        content.append("<h1>Listado de Usuarios</h1>\n")
        content.append("<p>Total: ${usuarios.size} usuarios</p>\n")
        content.append("<table>\n")
        
        // Cabecera de la tabla
        content.append("<tr>")
        content.append("<th>DNI</th>")
        content.append("<th>Nombre</th>")
        content.append("<th>Apellidos</th>")
        content.append("<th>Email</th>")
        content.append("<th>Teléfono</th>")
        content.append("<th>Estado</th>")
        content.append("<th>Fecha Registro</th>")
        content.append("<th>Perfiles</th>")
        content.append("</tr>\n")
        
        // Datos
        usuarios.forEach { usuario ->
            content.append("<tr>")
            content.append("<td>${usuario.dni}</td>")
            content.append("<td>${usuario.nombre}</td>")
            content.append("<td>${usuario.apellidos}</td>")
            content.append("<td>${usuario.email}</td>")
            content.append("<td>${usuario.telefono}</td>")
            content.append("<td>${if (usuario.activo) "Activo" else "Inactivo"}</td>")
            content.append("<td>${formatDate(usuario.fechaRegistro)}</td>")
            
            // Perfiles
            content.append("<td>")
            if (usuario.perfiles.isEmpty()) {
                content.append("Sin perfiles")
            } else {
                usuario.perfiles.forEachIndexed { index, perfil ->
                    if (index > 0) content.append("<br>")
                    content.append("${perfil.tipo.name}")
                    if (perfil.centroId != null) {
                        content.append(" (Centro: ${perfil.centroId})")
                    }
                }
            }
            content.append("</td>")
            
            content.append("</tr>\n")
        }
        
        content.append("</table>")
        
        return content.toString()
    }
} 