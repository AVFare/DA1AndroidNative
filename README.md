# XploreNow Argentina - Android Native

Este proyecto es una aplicación nativa de Android desarrollada para la materia **Desarrollo de Aplicaciones 1**. La aplicación, denominada **XploreNow**, permite a los usuarios explorar, reservar y gestionar actividades turísticas y experiencias en Argentina.

## 🏛️ Arquitectura del Proyecto

La aplicación sigue el patrón de diseño **MVVM (Model-View-ViewModel)** y se organiza en capas para asegurar la separación de responsabilidades, facilitar el mantenimiento y permitir la escalabilidad:

*   **Capa de UI (Vista)**: Implementada con Fragmentos encargados de la visualización y la interacción directa con el usuario. Utiliza **Jetpack Navigation Component** para el flujo entre pantallas y **View Binding** para un acceso seguro y eficiente a los componentes de la interfaz.
*   **Capa de ViewModel**: Actúa como puente entre la capa de datos y la UI. Expone estados mediante **LiveData** que los fragmentos observan para reaccionar a los cambios de forma reactiva, garantizando que la interfaz esté siempre sincronizada con los datos.
*   **Capa de Datos (Data)**:
    *   **Repository**: Actúa como la única fuente de verdad (Single Source of Truth), coordinando el flujo de información entre las fuentes remotas y locales.
    *   **Remote (Network)**: Gestión de peticiones a la API REST mediante **Retrofit 2** y **OkHttp**, utilizando **Gson** para la serialización de datos.
    *   **Local (Persistence)**: Almacenamiento persistente mediante **SharedPreferences**. Se utiliza para la gestión de sesiones (tokens), favoritos y un sistema de caché que permite el funcionamiento del modo offline.
*   **Inyección de Dependencias**: Se utiliza **Dagger-Hilt** para proveer dependencias de forma centralizada, reduciendo el acoplamiento y mejorando la testabilidad del código.

## 📂 Estructura del Proyecto

El código fuente se organiza siguiendo las mejores prácticas de modularización por capas dentro del paquete principal `com.example.da1androidnative`:

```text
app/src/main/java/com/example/da1androidnative/
├── data/
│   ├── local/          # Gestión de persistencia (TokenManager, OfflineStorage)
│   ├── model/          # Modelos de datos y DTOs para la API
│   ├── network/        # Interfaces de Retrofit y utilidades de conexión
│   └── repository/     # Implementación de repositorios (Single Source of Truth)
├── ui/
│   ├── auth/           # Fragmentos de Login, Registro y validación OTP
│   ├── home/           # Pantallas principales (Catálogo, Detalles, Reservas, Historial)
│   │   └── adapter/    # Adaptadores para Listados (RecyclerView)
│   ├── profile/        # Pantalla de Mi Perfil y configuración de usuario
│   ├── util/           # Clases de apoyo y utilidades de UI
│   └── MainActivity    # Actividad contenedora y configuración de Navegación
└── XploreNowApp        # Inicialización de Dagger-Hilt y contexto global
```

## 🚀 Características Principales

### 🔐 Autenticación y Seguridad
*   **Login y Registro**: Acceso seguro para usuarios.
*   **Acceso OTP**: Inicio de sesión mediante código de un solo uso enviado por email.
*   **Biometría**: Soporte para inicio de sesión con huella dactilar o reconocimiento facial.

### 📅 Gestión de Reservas
*   **Exploración**: Catálogo de actividades con detalles completos (itinerarios, puntos de encuentro, guías e idiomas).
*   **Creación de Reservas**: Proceso fluido para elegir fechas, horarios y cantidad de participantes.
*   **Mis Actividades**: Listado de reservas activas con acceso a vouchers y códigos de confirmación.
*   **Cancelación**: Posibilidad de cancelar reservas según la política vigente.

### 📜 Historial y Filtros
*   **Historial Completo**: Registro de actividades finalizadas y canceladas.
*   **Filtrado Avanzado**: Búsqueda por rango de fechas, estado y destino (mediante selección dinámica).
*   **Paginación**: Visualización optimizada de 6 tarjetas por página para un mejor rendimiento.

### 📶 Modo Offline y Sincronización
*   **Persistencia Local**: Acceso a "Mis Actividades" e historial sin conexión a internet mediante `SharedPreferences`.
*   **Banner de Estado**: Indicador visual claro cuando la app opera en modo offline.
*   **Sincronización Diferida**: Las cancelaciones realizadas sin internet se guardan localmente y se ejecutan automáticamente al recuperar la conexión.

### 📍 Mapas e Itinerarios
*   **Google Maps**: Integración para visualizar puntos de encuentro y recorridos de las actividades.
*   **Navegación**: Botón "Cómo llegar" que enlaza con aplicaciones de mapas externas.

## 🛠️ Stack Tecnológico

*   **Lenguaje**: Java
*   **Arquitectura**: MVVM (Model-View-ViewModel)
*   **Inyección de Dependencias**: Dagger-Hilt
*   **Networking**: Retrofit 2 + OkHttp + Gson
*   **Navegación**: Jetpack Navigation Component
*   **Imágenes**: Glide
*   **UI**: Material Design 3 (Material Components)
*   **Persistencia**: SharedPreferences con serialización JSON (Gson)

## 📋 Requisitos y Configuración

*   **Android Studio**: Hedgehog o superior.
*   **SDK Mínimo**: API 24 (Android 7.0)
*   **SDK Objetivo**: API 34
*   **Google Maps API Key**: Es necesaria una clave válida en el `AndroidManifest.xml` o `local.properties` para visualizar los mapas.

### Instalación
1. Clonar el repositorio.
2. Sincronizar el proyecto con archivos Gradle.
3. Ejecutar en un emulador o dispositivo físico con Google Play Services.

---
Desarrollado para la materia **Desarrollo de Aplicaciones 1 - UADE**.
