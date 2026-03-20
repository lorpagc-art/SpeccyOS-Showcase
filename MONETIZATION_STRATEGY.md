# Estrategia de Monetización y Crecimiento - Speccy OS

Este documento resume la propuesta para transformar Speccy OS en un modelo de negocio sostenible en la Play Store, equilibrando la retención de usuarios con la cobertura de costes operativos (IA, Nube, etc.).

## 1. El Modelo "Speccy Core" (Gratis)
Permite que el usuario experimente la interfaz y la potencia del frontend con límites controlados:

*   **Biblioteca Limitada:** Escaneo completo, pero con un límite de lanzamiento (ej. los primeros 20 juegos por sistema o un total de 100 lanzamientos).
*   **IA "El Arquitecto" Limitada:** 3 a 5 consultas diarias gratuitas. Suficiente para probar su utilidad sin disparar el consumo de tokens de Vertex AI.
*   **Rendimiento Estándar:** Acceso a perfiles de CPU "ECO" y "BALANCED". Los modos de alto rendimiento quedan reservados para la versión Pro.
*   **Scraper Básico:** Descarga de carátulas frontales (imágenes), pero sin soporte para videos o manuales técnicos.

## 2. El "Imperial Pass" (Suscripción Premium)
Modelo mensual para costear infraestructura y desarrollo continuo:

*   **IA Ilimitada:** Consultas infinitas al Arquitecto para optimización técnica, historia de juegos y recomendaciones personalizadas.
*   **Cloud Save Imperial:** Sincronización automática de partidas guardadas en la nube (Google Drive/Dropbox) en tiempo real.
*   **Modo Overclock/Extreme:** Desbloqueo de los perfiles de hardware más potentes vía Root o Shizuku.
*   **Personalización ADN:** Desbloqueo de temas "Inmersivos" y variantes de color neón exclusivas.
*   **Acceso Prioritario:** Mayor velocidad en el scraping y acceso a metadatos extendidos.

## 3. Estrategias de Adquisición (Play Store)
*   **Prueba Gratuita (Free Trial):** Implementar una semana de prueba gratuita de la suscripción mensual. Es la mejor forma de que el usuario vincule su biblioteca y se acostumbre a las funciones Pro.
*   **Marketing de Hardware:** Resaltar en la descripción que es la única capa diseñada específicamente para chips Unisoc (T620, T820) y dispositivos de nicho como Anbernic, Retroid y AYN.
*   **Visuales Neón:** Uso intensivo de vídeos de alta calidad mostrando la fluidez de los menús y el chat con la IA.

## 4. Implementación Técnica Sugerida
*   **Upgrade Contextual:** Si el usuario intenta acceder a una función bloqueada (ej. pulsar "Extreme" en ajustes de hardware), mostrar un diálogo elegante que ofrezca el Imperial Pass con un botón de compra directa.
*   **Sistema de Créditos:** Regalar un paquete inicial de "Créditos de IA" al primer inicio para fomentar el engagement con el Arquitecto.
*   **Validación Pro:** Al arrancar la app, el `BillingManager` debe verificar el estado de la suscripción y actualizar el `SettingsManager.isAuthorized`.

## 5. Precios Recomendados
*   **Mensual:** 1.99€ - 2.99€ (Micro-suscripción, bajo impacto psicológico).
*   **Anual:** 19.99€ (Oferta de "2 meses gratis" para asegurar la retención anual).
*   **Lifetime (Opcional):** Un pago único elevado (ej. 49.99€) para usuarios que odian las suscripciones.

---
*Nota: Este plan busca cubrir el coste de la IA de Google Cloud mientras se mantiene una base de usuarios gratuita que actúe como motor de marketing.*
