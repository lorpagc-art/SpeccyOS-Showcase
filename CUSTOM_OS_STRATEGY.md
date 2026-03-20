# Estrategia para generar el Speccy OS (Custom ROM)

Basado en los datos extraídos de la GameMT E5 Ultra (Unisoc T620).

## 1. Integración como "Lanzador de Sistema"
El primer paso para que Speccy OS se sienta como un sistema operativo es que Android lo trate como el "Home" predeterminado y le de privilegios máximos.

### Pasos:
1. **Mover a /system/priv-app/**: Al colocar nuestra APK en esta carpeta, el sistema le otorga permisos de escritura en hardware (ventilador) automáticamente sin preguntar.
2. **Eliminar Bloatware**: Mediante scripts, podemos "congelar" el launcher original y las apps innecesarias del fabricante que consumen RAM del T620.

## 2. Inyección de ADN en el Kernel (boot.img)
Ya sabemos que el ventilador se controla en `/sys/class/backlight/sprd_backlight_fan/brightness`. 

### Optimización:
Podemos modificar el archivo `init.rc` dentro del `boot.img` para:
- Establecer un perfil de ventilador base desde que sale el logo de inicio.
- Configurar los gobernadores de la CPU Unisoc a `schedutil` para un balance perfecto entre batería y potencia.

## 3. Identidad de Marca (Boot Animation)
Podemos sustituir el logo de inicio del fabricante por una animación de **Nano Banana** y el texto "Speccy OS Loading...". 
- **Ruta**: `/system/media/bootanimation.zip`

## 4. Herramientas Necesarias
Para generar el archivo `.PAC` (formato oficial de Unisoc) y flashearlo:
1. **SPD Research Tool**: Para flashear el nuevo OS.
2. **Android Image Kitchen**: Para desempaquetar y editar el `boot.img`.
3. **Firmware original**: Necesitamos el volcado completo que intentamos hacer.

## 5. Próximo Hito: Speccy OS V0.3
En la siguiente versión, la app debería incluir un **"Modo Kiosko"** que bloquee el acceso al Android normal, convirtiendo la consola en una máquina de juegos pura.
