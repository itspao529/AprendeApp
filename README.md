# AprendeApp

Aplicación Android (Kotlin) para gestionar recursos de aprendizaje de Ingeniería de Sistemas: libros, videos y artículos. Proyecto del curso Desarrollo de Software para Móviles, Universidad Don Bosco (Tercer Desafío Práctico).

## Funcionalidades

- **Registro y login** con validación de contraseña (mínimo 12 caracteres, mayúscula, minúscula, número y carácter especial `!@#$%^&*`). La contraseña se guarda como hash SHA-256, nunca en texto plano.
- **Roles**: Estudiante y Docente.
  - Estudiante: ver, buscar, filtrar, ordenar, marcar favoritos y calificar recursos.
  - Docente: pantalla de administración con crear (POST), editar (PUT) y eliminar (DELETE), con confirmación antes de borrar.
- **Lista de recursos** en tarjetas con imagen, título, descripción, tipo, enlace y promedio de calificación.
- **Búsqueda** por ID, título o tipo (sin distinguir mayúsculas ni tildes), filtro por tipo y orden A-Z / Z-A.
- **Favoritos** guardados localmente por usuario.
- **Calificación de 1 a 5 estrellas** guardada en el backend (`ratingSuma` y `ratingCantidad`); el promedio es visible para todos.
- **Feedback visual**: indicadores de carga, Snackbars, Toasts, diálogos de confirmación y mensajes de error con reintento.

## Arquitectura (MVC)

| Capa | Paquete | Contenido |
|---|---|---|
| Model | `model`, `network` | `Recurso`, `Usuario`, `Rol`, `ApiService`, `RetrofitClient` |
| View | `view`, `res/layout` | Activities, adapters y layouts XML |
| Controller | `controller` | `AuthController`, `RecursoController`, `PreferenciasController` |
| Utilidades | `utils` | `PasswordValidator`, `HashUtil`, `SessionManager` |

Todos los textos están centralizados en `res/values/strings.xml`.

## Tecnologías

Kotlin, ViewBinding, Retrofit + Gson + OkHttp, corrutinas, Glide, Material Components, RecyclerView, SharedPreferences y MockAPI.io como backend.

## API (MockAPI.io)

Recursos: `recursos` y `usuarios`.

```json
{
  "id": "1",
  "titulo": "Clean Code",
  "descripcion": "Guía para escribir código limpio.",
  "tipo": "libro",
  "enlace": "https://...",
  "imagen": "https://...",
  "ratingSuma": 0,
  "ratingCantidad": 0
}
```

## Cómo ejecutarlo

1. Clonar el repositorio y abrirlo en Android Studio (usar el JDK incluido, 17 o 21).
2. Ejecutar en un emulador o dispositivo (minSdk 24):

```bash
./gradlew installDebug
```

## Limitaciones conocidas

- El rol se elige al registrarse; en un sistema real lo asignaría un administrador.
- El control de "un voto por usuario" es local al dispositivo.
- El hash de contraseña se calcula en la app por tratarse de un backend simulado.

## Autora

Paola Castillo

## Instalación rápida (APK)

Descarga la APK desde la carpeta `apk/` o desde la sección **Releases** del repositorio e instálala en un dispositivo Android (hay que permitir la instalación de orígenes desconocidos).
