package com.tfg.umeegunero.data.model

/**
 * Clase que gestiona las plantillas de email HTML para la aplicación.
 *
 * Esta clase proporciona plantillas HTML predefinidas para diferentes tipos
 * de comunicaciones por email en la aplicación UmeEgunero. Las plantillas
 * están diseñadas siguiendo las mejores prácticas de diseño de email y
 * utilizan los colores corporativos de la aplicación.
 *
 * @see com.tfg.umeegunero.ui.theme.AppColors
 *
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.0
 */
object PlantillaEmail {

    /**
     * Obtiene la plantilla HTML para emails de aprobación de solicitud.
     *
     * @param nombre Nombre del destinatario
     * @return String con el contenido HTML de la plantilla
     */
    fun obtenerPlantillaAprobacion(nombre: String): String = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Solicitud Aprobada</title>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333333; margin: 0; padding: 0; background-color: #f5f5f5; }
                .container { max-width: 600px; margin: 20px auto; padding: 0; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden; }
                .header { background-color: #0D47A1; color: white; padding: 20px; text-align: center; }
                .content { padding: 25px; }
                .footer { text-align: center; padding: 20px; color: #666666; font-size: 12px; border-top: 1px solid #eeeeee; }
                .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white !important; text-decoration: none; border-radius: 4px; margin-top: 20px; font-weight: bold; }
                .status { background-color: #4CAF50; color: white; padding: 8px 16px; border-radius: 20px; display: inline-block; margin: 10px 0; font-weight: bold; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1 style="margin:0; font-size: 24px;">UmeEgunero</h1>
                </div>
                <div class="content">
                    <h2 style="color: #0D47A1; margin-top:0;">¡Solicitud Aprobada!</h2>
                    <div class="status">APROBADA</div>
                    <p>Estimado/a $nombre,</p>
                    <p>Nos complace informarle que su solicitud ha sido <strong>aprobada</strong> por el centro.</p>
                    <p>Ya puede acceder a la plataforma para ver la información de su hijo/a.</p>
                    <p style="text-align:center; margin-top: 25px;">
                      <a href="https://umeegunero.com/login" class="button">Acceder a la plataforma</a>
                    </p>
                </div>
                <div class="footer">
                    <p style="margin: 0 0 5px 0;">Este es un mensaje automático, por favor no responda a este email.</p>
                    <p style="margin: 0;">© ${java.time.Year.now().value} UmeEgunero. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    /**
     * Obtiene la plantilla HTML para emails de rechazo de solicitud.
     *
     * @param nombre Nombre del destinatario
     * @return String con el contenido HTML de la plantilla
     */
    fun obtenerPlantillaRechazo(nombre: String): String = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Estado de Solicitud</title>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333333; margin: 0; padding: 0; background-color: #f5f5f5; }
                .container { max-width: 600px; margin: 20px auto; padding: 0; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden; }
                .header { background-color: #0D47A1; color: white; padding: 20px; text-align: center; }
                .content { padding: 25px; }
                .footer { text-align: center; padding: 20px; color: #666666; font-size: 12px; border-top: 1px solid #eeeeee; }
                .status { background-color: #F44336; color: white; padding: 8px 16px; border-radius: 20px; display: inline-block; margin: 10px 0; font-weight: bold; }
                .contact-info { background-color: #f8f9fa; padding: 15px; border-radius: 4px; margin: 25px 0; border-left: 4px solid #F44336; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                     <h1 style="margin:0; font-size: 24px;">UmeEgunero</h1>
                </div>
                <div class="content">
                    <h2 style="color: #0D47A1; margin-top:0;">Estado de su Solicitud</h2>
                    <div class="status">RECHAZADA</div>
                    <p>Estimado/a $nombre,</p>
                    <p>Lamentamos informarle que su solicitud ha sido <strong>rechazada</strong> por el centro.</p>
                    <div class="contact-info">
                        <h3 style="margin-top: 0; color: #F44336; font-size: 16px;">¿Necesita más información?</h3>
                        <p style="margin-bottom: 0;">Por favor, póngase en contacto con la secretaría del centro para obtener más detalles sobre esta decisión.</p>
                    </div>
                </div>
                <div class="footer">
                    <p style="margin: 0 0 5px 0;">Este es un mensaje automático, por favor no responda a este email.</p>
                    <p style="margin: 0;">© ${java.time.Year.now().value} UmeEgunero. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    /**
     * Obtiene la plantilla HTML para emails de bienvenida.
     *
     * @param nombre Nombre del destinatario
     * @return String con el contenido HTML de la plantilla
     */
    fun obtenerPlantillaBienvenida(nombre: String): String = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Bienvenido a UmeEgunero</title>
        </head>
        <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333333; margin: 0; padding: 0; background-color: #f5f5f5;">
            <div style="max-width: 600px; margin: 20px auto; padding: 0; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden;">
                <div style="background-color: #0D47A1; color: white; padding: 20px; text-align: center;">
                    <h1 style="margin: 0; font-size: 24px;">¡Bienvenido/a a UmeEgunero!</h1>
                </div>
                <div style="padding: 25px;">
                    <h2 style="color: #0D47A1; font-size: 20px; margin-top:0;">Hola $nombre,</h2>
                    <p>¡Bienvenido/a a la familia UmeEgunero! Nos alegra tenerte con nosotros.</p>

                    <div style="background-color: #FFF3E0; border-left: 4px solid #FF9800; padding: 15px; margin: 25px 0; border-radius: 4px;">
                        <h3 style="margin-top: 0; color: #FF9800; font-size: 16px;">Información importante</h3>
                        <p style="margin-bottom: 0;">Su solicitud está siendo procesada. <strong>Recuerde que hasta que no reciba la aprobación del centro no podrá acceder a la plataforma</strong>. Le notificaremos por correo electrónico cuando su solicitud haya sido aprobada.</p>
                    </div>

                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 4px; margin: 25px 0;">
                        <h3 style="margin-top: 0; color: #0D47A1; font-size: 16px;">Con UmeEgunero podrás:</h3>
                        <ul style="margin: 10px 0 0 0; padding-left: 20px; list-style: none;">
                          <li style="margin-bottom: 8px;"><span style="color: #0D47A1; margin-right: 8px;">•</span> Ver el progreso diario de tu hijo/a</li>
                          <li style="margin-bottom: 8px;"><span style="color: #0D47A1; margin-right: 8px;">•</span> Comunicarte directamente con los profesores</li>
                          <li style="margin-bottom: 8px;"><span style="color: #0D47A1; margin-right: 8px;">•</span> Recibir notificaciones importantes</li>
                          <li style="margin-bottom: 0;"><span style="color: #0D47A1; margin-right: 8px;">•</span> Acceder al calendario de actividades</li>
                        </ul>
                    </div>

                    <p>Una vez aprobada su solicitud, podrá acceder a la plataforma:</p>
                    <p style="text-align: center; margin: 25px 0;">
                        <a href="https://umeegunero.com/login" style="display: inline-block; padding: 12px 24px; background-color: #0D47A1; color: white; text-decoration: none; border-radius: 4px; font-weight: bold;">Acceder a UmeEgunero</a>
                    </p>
                </div>
                <div style="text-align: center; padding: 20px; color: #666666; font-size: 12px; border-top: 1px solid #eeeeee;">
                    <p style="margin: 0 0 5px 0;">Este es un mensaje automático, por favor no responda a este email.</p>
                    <p style="margin: 0;">© ${java.time.Year.now().value} UmeEgunero. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    /**
     * Obtiene la plantilla HTML para emails de recordatorio.
     *
     * @param nombre Nombre del destinatario
     * @return String con el contenido HTML de la plantilla
     */
    fun obtenerPlantillaRecordatorio(nombre: String): String = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
             <title>Recordatorio UmeEgunero</title>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333333; margin: 0; padding: 0; background-color: #f5f5f5; }
                .container { max-width: 600px; margin: 20px auto; padding: 0; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden;}
                .header { background-color: #0D47A1; color: white; padding: 20px; text-align: center; }
                .content { padding: 25px; }
                .footer { text-align: center; padding: 20px; color: #666666; font-size: 12px; border-top: 1px solid #eeeeee; }
                .reminder-box { background-color: #FFF3E0; border-left: 4px solid #FF9800; padding: 15px; margin: 25px 0; border-radius: 4px; }
                .button { display: inline-block; padding: 12px 24px; background-color: #FF9800; color: white !important; text-decoration: none; border-radius: 4px; margin-top: 20px; font-weight: bold;}
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1 style="margin:0; font-size: 24px;">Recordatorio UmeEgunero</h1>
                </div>
                <div class="content">
                    <h2 style="color: #0D47A1; margin-top:0;">Hola $nombre,</h2>
                    <div class="reminder-box">
                        <h3 style="margin-top: 0; color: #FF9800; font-size: 16px;">Recordatorio Importante</h3>
                        <p style="margin-bottom: 0;">Te recordamos que tienes pendiente revisar la información diaria de tu hijo/a en la aplicación.</p>
                    </div>
                    <p>Mantente al día con las actividades y el progreso de tu hijo/a accediendo regularmente a la aplicación.</p>
                    
                    <p style="text-align: center; margin: 25px 0;">
                        <a href="https://umeegunero.com/login" class="button">Acceder ahora</a>
                    </p>
                </div>
                <div class="footer">
                    <p style="margin: 0 0 5px 0;">Este es un mensaje automático, por favor no responda a este email.</p>
                    <p style="margin: 0;">© ${java.time.Year.now().value} UmeEgunero. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    /**
     * Obtiene la plantilla HTML para emails de soporte técnico.
     *
     * @param nombre Nombre del destinatario (usuario que reporta el problema)
     * @return String con el contenido HTML de la plantilla
     */
    fun obtenerPlantillaSoporte(nombre: String): String = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Soporte Técnico UmeEgunero</title>
            <style>
                body { 
                    font-family: "Segoe UI", Arial, sans-serif; 
                    line-height: 1.6; 
                    color: #333333; 
                    margin: 0; 
                    padding: 0; 
                    background-color: #f5f5f5; 
                }
                .container { 
                    max-width: 700px; 
                    margin: 20px auto; 
                    padding: 0; 
                    background-color: #ffffff; 
                    border-radius: 8px; 
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
                    overflow: hidden; 
                }
                .header { 
                    background-color: #E91E63; 
                    color: white; 
                    padding: 20px; 
                    text-align: center; 
                }
                .content { 
                    padding: 25px; 
                }
                .footer { 
                    text-align: center; 
                    padding: 15px; 
                    color: #666666; 
                    font-size: 12px; 
                    border-top: 1px solid #eeeeee; 
                }
                .info-section { 
                    background-color: #f8f9fa; 
                    padding: 15px; 
                    border-radius: 4px; 
                    margin-bottom: 20px; 
                    border-left: 4px solid #E91E63; 
                }
                .info-row { 
                    margin-bottom: 8px; 
                }
                .info-label { 
                    font-weight: bold; 
                    color: #555; 
                    width: 80px; 
                    display: inline-block; 
                }
                .message-box { 
                    background-color: #ffffff; 
                    padding: 15px; 
                    border-radius: 4px; 
                    margin: 20px 0; 
                    border: 1px solid #dddddd; 
                    white-space: pre-wrap;
                }
                .priority-high { 
                    color: #D32F2F; 
                    font-weight: bold; 
                }
                .ticket-info { 
                    background-color: #E8F5E9; 
                    padding: 10px 15px; 
                    border-radius: 4px; 
                    margin-top: 20px; 
                    font-size: 0.9em; 
                    border-left: 4px solid #4CAF50; 
                }
                .ticket-id { 
                    text-align: right; 
                    color: #777; 
                    font-size: 0.8em; 
                    margin-top: 10px; 
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1 style="margin:0; font-size: 24px;">Soporte Técnico UmeEgunero</h1>
                </div>
                <div class="content">
                    <h2 style="color: #E91E63; margin-top:0;">Nueva consulta de soporte</h2>
                    
                    <div class="info-section">
                        <div class="info-row">
                            <span class="info-label">De:</span> $nombre
                        </div>
                        <div class="info-row">
                            <span class="info-label">Email:</span> <!-- Se insertará desde el script -->
                        </div>
                        <div class="info-row">
                            <span class="info-label">Asunto:</span> <span class="priority-high"><!-- Se insertará desde el script --></span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Fecha:</span> ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}
                        </div>
                    </div>
                    
                    <h3>Mensaje del usuario:</h3>
                    <div class="message-box">
                        <!-- El mensaje se insertará desde el script -->
                    </div>
                    
                    <div class="ticket-info">
                        <p style="margin-top: 0;">Este ticket ha sido generado automáticamente desde la aplicación UmeEgunero.</p>
                        <p style="margin-bottom: 0;">Por favor, responda directamente al correo del usuario para atender su consulta.</p>
                    </div>
                    
                    <div class="ticket-id">
                        ID: UME-${java.util.Random().nextInt(900000) + 100000}
                    </div>
                </div>
                <div class="footer">
                    <p style="margin: 0 0 5px 0;">Sistema de soporte técnico - UmeEgunero</p>
                    <p style="margin: 0;">© ${java.time.Year.now().value} UmeEgunero. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
} 