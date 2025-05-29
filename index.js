// Importar las funciones de custom claims
const customClaimsFunctions = require("./setUserCustomClaims");

// Exportar las funciones de custom claims
exports.syncUserCustomClaims = customClaimsFunctions.syncUserCustomClaims;
exports.setUserClaimsById = customClaimsFunctions.setUserClaimsById;
exports.syncClaimsOnUserUpdate = customClaimsFunctions.syncClaimsOnUserUpdate;
exports.setClaimsOnNewUser = customClaimsFunctions.setClaimsOnNewUser; 