#!/bin/bash

# Buscar archivos que importan Result de repository
echo "Buscando archivos que necesitan actualización..."
files=$(grep -r "import com.tfg.umeegunero.data.repository.Result" --include="*.kt" .)

# Procesar cada archivo
echo "Actualizando imports..."
for file in $files; do
  file=$(echo $file | cut -d: -f1)
  echo "Actualizando $file"
  # Reemplazar el import
  sed -i '' 's/import com.tfg.umeegunero.data.repository.Result/import com.tfg.umeegunero.data.model.Result/g' "$file"
done

echo "¡Actualización completada!" 