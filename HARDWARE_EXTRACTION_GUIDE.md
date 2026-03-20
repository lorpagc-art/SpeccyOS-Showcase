# Guía de Extracción de Hardware - Speccy OS (GameMT E5 Ultra)

Este documento contiene el script automatizado para extraer las rutas críticas de hardware del SoC Unisoc T620.

## 🚀 Solución al error "adb no se reconoce"

Si estás en Windows, la forma más fácil de ejecutar esto es abrir la pestaña **"Terminal"** que tienes justo aquí abajo en **Android Studio**. Esa terminal ya suele tener configurado el acceso a ADB.

Si aun así falla, usa este script que busca automáticamente tu carpeta de usuario:

### 🪟 Script para Windows (Copia y guarda como `extraer.bat`)

```batch
@echo off
set "ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if not exist "%ADB_PATH%" (
    echo [!] No se encontro ADB en la ruta por defecto.
    echo [?] Por favor, ejecuta este comando en la terminal de Android Studio.
    pause
    exit
)

echo --- Speccy OS: Buscador de ADN de Hardware (E5 Ultra) ---
"%ADB_PATH%" wait-for-device
echo [+] Consola detectada.

echo [?] Buscando sensores de temperatura...
"%ADB_PATH%" shell "for zone in /sys/class/thermal/thermal_zone*; do type=$(cat $zone/type); temp=$(cat $zone/temp); echo Zone $zone: $type -> $temp; done" > hardware_dump.txt

echo [?] Buscando rutas de ventilador...
"%ADB_PATH%" shell "find /sys -name '*fan*' -o -name '*pwm*'" >> hardware_dump.txt

echo [?] Listando gobernadores...
"%ADB_PATH%" shell "cat /sys/devices/system/cpu/cpufreq/policy0/scaling_available_governors" >> hardware_dump.txt

echo [?] Extrayendo build.prop...
"%ADB_PATH%" pull /system/build.prop .

echo --- LISTO! Revisa el archivo hardware_dump.txt ---
pause
```

## 🛠️ Cómo aplicar los resultados al código

Una vez que tengas el `hardware_dump.txt`, busca las siguientes líneas y actualiza `HardwareControlManager.kt`:

1. **Temperatura**: Busca la zona térmica que diga `soc-thermal` o `cpu-thermal`.
2. **Ventilador**: Busca una ruta que termine en `level` o `duty_cycle`. 

## 📦 Archivos Críticos a Conservar
Si vas a crear una imagen de sistema (ROM) personalizada, asegúrate de haber extraído:
- `/vendor/etc/init/` (Configuración de arranque).
- `/vendor/lib/hw/` (Drivers HAL).
