#!/bin/bash

# Script para corregir los errores comunes en la migración de Resultado a Result

# 1. Corregir accesos inseguros a exception.message
find app/src/main/java -name "*.kt" -exec sed -i '"' -e "s/exception.message/exception?.message/g" {} \;

# 2. Corregir uso de One type argument expected en Success
find app/src/main/java -name "*.kt" -exec sed -i '"' -e "s/Result.Success(/Result.Success<Any>(/g" {} \;

# 3. Corregir uso de One type argument expected en Error
find app/src/main/java -name "*.kt" -exec sed -i '"' -e "s/Result.Error(/Result.Error(/g" {} \;

# 4. Corregir uso de One type argument expected en Loading
find app/src/main/java -name "*.kt" -exec sed -i '"' -e "s/Result.Loading(/Result.Loading<Any>(/g" {} \;

echo "Correcciones automáticas aplicadas"
