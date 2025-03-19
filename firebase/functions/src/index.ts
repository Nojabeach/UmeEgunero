import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

/**
 * Cloud Function para eliminar un usuario de Firebase Authentication por su email
 * Esta función debe ser llamada con un token de administrador válido
 */
export const deleteUserByEmail = functions.https.onCall(async (data, context) => {
    // Verificar si el usuario que hace la llamada tiene permisos de administrador
    if (!context.auth || !context.auth.token.admin) {
        throw new functions.https.HttpsError(
            'permission-denied', 
            'Solo administradores pueden eliminar usuarios'
        );
    }
    
    const email = data.email;
    if (!email) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Se requiere un email para eliminar el usuario'
        );
    }
    
    try {
        // Obtener el usuario por email
        const userRecord = await admin.auth().getUserByEmail(email);
        
        // Eliminar el usuario
        await admin.auth().deleteUser(userRecord.uid);
        
        // Registrar la acción en logs
        functions.logger.info(`Usuario ${email} eliminado por ${context.auth.token.email}`);
        
        return { success: true, message: `Usuario ${email} eliminado correctamente` };
    } catch (error) {
        functions.logger.error(`Error al eliminar usuario ${email}:`, error);
        throw new functions.https.HttpsError(
            'internal',
            `Error al eliminar usuario: ${error.message}`
        );
    }
});

/**
 * Cloud Function para eliminar varios usuarios de Firebase Authentication por sus emails
 * Esta función debe ser llamada con un token de administrador válido
 */
export const deleteUsersByEmails = functions.https.onCall(async (data, context) => {
    // Verificar si el usuario que hace la llamada tiene permisos de administrador
    if (!context.auth || !context.auth.token.admin) {
        throw new functions.https.HttpsError(
            'permission-denied', 
            'Solo administradores pueden eliminar usuarios'
        );
    }
    
    const emails = data.emails;
    if (!emails || !Array.isArray(emails) || emails.length === 0) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Se requiere un array de emails para eliminar usuarios'
        );
    }
    
    const results = {
        success: [],
        failed: []
    };
    
    for (const email of emails) {
        try {
            // Obtener el usuario por email
            const userRecord = await admin.auth().getUserByEmail(email);
            
            // Eliminar el usuario
            await admin.auth().deleteUser(userRecord.uid);
            
            // Registrar éxito
            results.success.push(email);
            functions.logger.info(`Usuario ${email} eliminado por ${context.auth.token.email}`);
        } catch (error) {
            // Registrar error
            results.failed.push({
                email,
                error: error.message
            });
            functions.logger.error(`Error al eliminar usuario ${email}:`, error);
        }
    }
    
    return results;
}); 