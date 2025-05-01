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
            <style>
                body {
                    font-family: 'Segoe UI', Arial, sans-serif;
                    line-height: 1.6;
                    color: #333333;
                    margin: 0;
                    padding: 0;
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: #ffffff;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                .header {
                    background-color: #0D47A1;
                    color: white;
                    padding: 20px;
                    text-align: center;
                    border-radius: 8px 8px 0 0;
                }
                .content {
                    padding: 20px;
                }
                .footer {
                    text-align: center;
                    padding: 20px;
                    color: #666666;
                    font-size: 12px;
                }
                .button {
                    display: inline-block;
                    padding: 12px 24px;
                    background-color: #4CAF50;
                    color: white;
                    text-decoration: none;
                    border-radius: 4px;
                    margin-top: 20px;
                }
                .status {
                    background-color: #4CAF50;
                    color: white;
                    padding: 8px 16px;
                    border-radius: 20px;
                    display: inline-block;
                    margin: 10px 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>UmeEgunero</h1>
                </div>
                <div class="content">
                    <h2>¡Solicitud Aprobada!</h2>
                    <div class="status">APROBADA</div>
                    <p>Estimado/a $nombre,</p>
                    <p>Nos complace informarle que su solicitud ha sido <strong>aprobada</strong> por el centro.</p>
                    <p>Ya puede acceder a la plataforma para ver la información de su hijo/a.</p>
                    <a href="https://umeegunero.com/login" class="button">Acceder a la plataforma</a>
                </div>
                <div class="footer">
                    <p>Este es un mensaje automático, por favor no responda a este email.</p>
                    <p>© 2024 UmeEgunero. Todos los derechos reservados.</p>
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
            <style>
                body {
                    font-family: 'Segoe UI', Arial, sans-serif;
                    line-height: 1.6;
                    color: #333333;
                    margin: 0;
                    padding: 0;
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: #ffffff;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                .header {
                    background-color: #0D47A1;
                    color: white;
                    padding: 20px;
                    text-align: center;
                    border-radius: 8px 8px 0 0;
                }
                .content {
                    padding: 20px;
                }
                .footer {
                    text-align: center;
                    padding: 20px;
                    color: #666666;
                    font-size: 12px;
                }
                .status {
                    background-color: #F44336;
                    color: white;
                    padding: 8px 16px;
                    border-radius: 20px;
                    display: inline-block;
                    margin: 10px 0;
                }
                .contact-info {
                    background-color: #f8f9fa;
                    padding: 15px;
                    border-radius: 4px;
                    margin: 20px 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>UmeEgunero</h1>
                </div>
                <div class="content">
                    <h2>Estado de su Solicitud</h2>
                    <div class="status">RECHAZADA</div>
                    <p>Estimado/a $nombre,</p>
                    <p>Lamentamos informarle que su solicitud ha sido <strong>rechazada</strong> por el centro.</p>
                    <div class="contact-info">
                        <h3>¿Necesita más información?</h3>
                        <p>Por favor, póngase en contacto con la secretaría del centro para obtener más detalles sobre esta decisión.</p>
                    </div>
                </div>
                <div class="footer">
                    <p>Este es un mensaje automático, por favor no responda a este email.</p>
                    <p>© 2024 UmeEgunero. Todos los derechos reservados.</p>
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
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <div style="background-color: #0D47A1; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h1 style="margin: 0; font-size: 24px;">¡Bienvenido/a a UmeEgunero!</h1>
                </div>
                <div style="padding: 20px;">
                    <h2 style="color: #0D47A1; font-size: 20px;">Hola $nombre,</h2>
                    <p>¡Bienvenido/a a la familia UmeEgunero! Nos alegra tenerte con nosotros.</p>
                    
                    <div style="background-color: #FFF3E0; border-left: 4px solid #FF9800; padding: 15px; margin: 20px 0; border-radius: 4px;">
                        <h3 style="margin-top: 0; color: #FF9800;">Información importante</h3>
                        <p>Su solicitud está siendo procesada. <strong>Recuerde que hasta que no reciba la aprobación del centro no podrá acceder a la plataforma</strong>. Le notificaremos por correo electrónico cuando su solicitud haya sido aprobada.</p>
                    </div>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 4px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #0D47A1;">Con UmeEgunero podrás:</h3>
                        <p style="margin: 10px 0; padding-left: 20px;"><span style="display: inline-block; margin-right: 5px; color: #0D47A1;">•</span> Ver el progreso diario de tu hijo/a</p>
                        <p style="margin: 10px 0; padding-left: 20px;"><span style="display: inline-block; margin-right: 5px; color: #0D47A1;">•</span> Comunicarte directamente con los profesores</p>
                        <p style="margin: 10px 0; padding-left: 20px;"><span style="display: inline-block; margin-right: 5px; color: #0D47A1;">•</span> Recibir notificaciones importantes</p>
                        <p style="margin: 10px 0; padding-left: 20px;"><span style="display: inline-block; margin-right: 5px; color: #0D47A1;">•</span> Acceder al calendario de actividades</p>
                    </div>
                    
                    <p>Una vez aprobada su solicitud, podrá acceder a la plataforma:</p>
                    <p style="text-align: center; margin: 25px 0;">
                        <a href="https://umeegunero.com/login" style="display: inline-block; padding: 12px 24px; background-color: #0D47A1; color: white; text-decoration: none; border-radius: 4px; font-weight: bold;">Acceder a UmeEgunero</a>
                    </p>
                </div>
                <div style="text-align: center; padding: 20px; color: #666666; font-size: 12px; border-top: 1px solid #eeeeee;">
                    <p>Este es un mensaje automático, por favor no responda a este email.</p>
                    <p>© 2024 UmeEgunero. Todos los derechos reservados.</p>
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
            <style>
                body {
                    font-family: 'Segoe UI', Arial, sans-serif;
                    line-height: 1.6;
                    color: #333333;
                    margin: 0;
                    padding: 0;
                    background-color: #f5f5f5;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: #ffffff;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                .header {
                    background-color: #0D47A1;
                    color: white;
                    padding: 20px;
                    text-align: center;
                    border-radius: 8px 8px 0 0;
                }
                .content {
                    padding: 20px;
                }
                .footer {
                    text-align: center;
                    padding: 20px;
                    color: #666666;
                    font-size: 12px;
                }
                .reminder-box {
                    background-color: #FFF3E0;
                    border-left: 4px solid #FF9800;
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 4px;
                }
                .button {
                    display: inline-block;
                    padding: 12px 24px;
                    background-color: #FF9800;
                    color: white;
                    text-decoration: none;
                    border-radius: 4px;
                    margin-top: 20px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Recordatorio UmeEgunero</h1>
                </div>
                <div class="content">
                    <h2>Hola $nombre,</h2>
                    <div class="reminder-box">
                        <h3>Recordatorio Importante</h3>
                        <p>Te recordamos que tienes pendiente revisar la información diaria de tu hijo/a en la aplicación.</p>
                    </div>
                    <p>Mantente al día con las actividades y el progreso de tu hijo/a accediendo regularmente a la aplicación.</p>
                    <a href="https://umeegunero.com/login" class="button">Acceder ahora</a>
                </div>
                <div class="footer">
                    <p>Este es un mensaje automático, por favor no responda a este email.</p>
                    <p>© 2024 UmeEgunero. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
} 