# Proyecto integrador

## Spring Boot
En Spring Initializr creamos nuesto proyecto. El nombre del proyecto será proyectointegrador.
Se realizará en Java 25. Además agregamos dependencias como Spring Data JPA, PostgreSQL Driver, Validation, Spring Security, spring Data Redis, Flyway Migration, Spring Boot Actuator, Spring Boot DevTools. Todavía faltan más dependencias pero se incluirán a medida que se avance el proyecto

## PostgreSQL

Creamos una red docker llamada proyectointegrador-net
Creamos el volumen proyectointegrador-pgdata
Y creamos el contenedor. Se verifica con docker ps que todo se realizó de manera correcta

### Creamos la base de datos

Ejecutamos el archivo 00_create_database.sql
Este archivo está guardado en la raíz del proyecto proyectointegrador.
Ejecutamos el siguiente comando

Get-Content .\00_create_database.sql | docker exec -i proyectointegrador-postgres psql -U ups -d postgres

Lo que hace este comando es leer y luego ejecuta las instrucciones dentro del PostgresSQL del contenedor

Podríamos verificar que la base fue creada con:

docker exec -it proyectointegrador-postgres psql -U ups -d postgres -c "\l"

![Comprobación de base de daos creada](/assets/verificacion-base-de-datos.png)

## Flyway

Usaremos Flyway para la creación de tablas y la carga automática de los datos iniciales.

En la ruta src/main/resources/db/migration movemos nuestro otro archivo 

## application.yaml

Configuramos este archivo principalmente para que use el puerto use una varible de entorno denominada port, si no lo encuentra entonces usaráa el puerto 8080. 
Para que tenga una conexión directa con PostgreSQL, aquí no escribimos directamente ups ni ups123 ya que las credenciales se definen mediante variables de entorno: Iría así
datasource:
  url: ${DB_URL}
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}

## Módulo de Roles

Implementamos el primer módulo de la API, corresponde a los roles del sistema.

Los roles disponibles son:
- ADMIN
- ORGANIZER
- PARTICIPANT

Estos valores son datos ya cargados en PostgreSQL mediante la migración de Flyway

La estructura es la siguiente:

```text
src/main/java/ec/edu/ups/icc/proyectointegrador/roles
├── controllers
│   └── RoleController.java
├── dtos
│   └── RoleResponseDto.java
├── entities
│   └── RoleEntity.java
├── enums
│   └── RoleName.java
├── mappers
│   └── RoleMapper.java
├── repositories
│   └── RoleRepository.java
└── services
    ├── RoleService.java
    └── impl
        └── RoleServiceImpl.java
```

Entonces el módulo de roles quedo en perfecto funcionamiento. Usamos Powershell para comprobarlo debido a que por el momento Spring Security Crea automáticamente un usuario temporal. No sería comveniente usar Bruno porque habría que actualizar constantemente la contraseña en cada ejecución. Se usará Bruno cuando cuando se implemente la autenticación real con JWT.

![Verificación del módulo roles](/assets/roles-powershell-JSON.png)

## Módulo de Usuarios

Implementamos el segundo módulo de la API, corresponde a los usuarios del sistema.

Cada usuario puede tener uno o varios roles asignados mediante la tabla intermedia `user_roles`. El estado administrativo del usuario (`ACTIVE` o `BLOCKED`) se almacena en la columna `status`.

La estructura es la siguiente:

```text
src/main/java/ec/edu/ups/icc/proyectointegrador/users
├── controllers
│   └── UserController.java
├── dtos
│   └── UserResponseDto.java
├── entities
│   └── UserEntity.java
├── enums
│   └── UserStatus.java
├── mappers
│   └── UserMapper.java
├── repositories
│   └── UserRepository.java
└── services
    ├── UserService.java
    └── impl
        └── UserServiceImpl.java
```

El `UserResponseDto` no expone `passwordHash` en ningún momento, evitando filtrar la contraseña cifrada del usuario.

Se probó igual que el módulo de roles, usando PowerShell con la contraseña temporal generada por Spring Security.

![Verificación del módulo usuarios - listado](/assets/users-powershell-list.png)
![Verificación del módulo usuarios - por id](/assets/users-powershell-id.png)

## Módulo de Seguridad, autenticación y autorización

El módulo de seguridad tiene como finalidad proteger la API REST del sistema de gestión de eventos académicos. Su immplementación identifica a los usuarios, controla su acceso a los recursos y restringe su acceso a ciertas operaciones dependiendo de sus roles.

En este apartado realizamos:

Autenticació: Define quien es el usuario que intenta acceder al sistema

Autorización: determina qué operaciones puede realizar el usuario autenticado

Para la implementación del manejo de JWT se añaden:

```text
implementation("io.jsonwebtoken:jjwt-api:0.13.0")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
```

La estructura de este módelo es el siguiente:

```text
security
├── config
│   ├── JwtProperties.java
│   └── SecurityConfig.java
├── controllers
│   └── AuthController.java
├── dtos
│   ├── AuthResponseDto.java
│   ├── CreatedRefreshTokenDto.java
│   ├── LoginRequestDto.java
│   ├── RefreshRequestDto.java
│   ├── RegisterRequestDto.java
│   └── RotatedRefreshTokenDto.java
├── entities
│   └── RefreshTokenEntity.java
├── filters
│   └── JwtAuthenticationFilter.java
├── repositories
│   └── RefreshTokenRepository.java
└── services
    ├── AuthService.java
    ├── CustomUserDetailsService.java
    ├── JwtService.java
    ├── RefreshTokenService.java
    └── impl
        ├── AuthServiceImpl.java
        └── RefreshTokenServiceImpl.java
```

Se separa en carpetas y varios archivos para no dejar a unos pocos archivos tener varias responsabilidades.

En este caso:

Los controladores reciben las solicitudes HTTP
Los DTO transportan y validan los datos.
Los servicios contienen la lógica de negocio.
Los repositorios acceden a la base de datos.
Los filtros interceptan las solicitudes.
Las clases de configuración definen las reglas de seguridad.

Primero hablemoos de la configuración de la clase SecurityConfig. Esta clase centraliza las reglas de seguridad de la aplicación.

Entre los principales está la desactivación de la protección CSRF porque la aplicación utiliza una API REST sin sesiones tradicionales ni formularios gestionados por el servidor.

```text
.csrf(csrf -> csrf.disable()).
```

En el proyecto los Endpoints públicos serán:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /actuator/health
```

Los demás Endpoints requerirán de autenticación

```text
.anyRequest().authenticated()
```

### Configuración de JWT

La información de la configuración se obtiene mediante JwtProperties:

```text
security:
  jwt:
    secret: ${JWT_SECRET}
    access-expiration: ${JWT_ACCESS_EXPIRATION:15m}
    refresh-expiration: ${JWT_REFRESH_EXPIRATION:7d}
```

JWT_SECRET: Clave secreta codificada en Base64
JWT_ACCESS_EXPIRATION: Duración del access token
JWT_REFRESH_EXPIRATION:	Duración del refresh token

La clave secreta no se almacena en el repositorio. Se proporciona mediante una variable de entorno. Asi podemos exponer información confidencial en GitHub

Bien, la estructura del access token es el siguiente:

correo electrónico del usuario como subject;
roles asignados
fecha de emisión
fecha de expiración
firma criptográfica

### Cifrado de contraseñas

La contraseña no se debe guardad en texto plano, por ende, se configuró un bean de BCrypt

```text
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Durante el registro se hace:
```text
passwordEncoder.encode(request.password())
```

El resultado cifrado se almacena en password_hash

Como hacemos uso de Bcrypt, incluso dos usuarios que utilizan la misma contraseña tendrán hashes diferentes

### Registro de usuarios

Endpoint:

```text
POST /api/auth/register
```

Solicitud:
```text
{
  "firstName": "Bryan",
  "lastName": "Maita",
  "email": "bryan.test@academic.test",
  "password": "Password123*"
}
```

Funciona de la siguiente manera:

1. Se elimina espacios innecesarios con ayuda de trim()
2. El correo electrónico se convierte en minúsculas
3. Se comprueba que el correo no esté ya registrado
4. La contraseña se cifra, como ya hablamos en el punto anterior
5. Se asigna un estado de ACTIVE
6. Se asigna el rol PARTICIPANT por defecto
7. Se guarda el usuario en PostgreSQL
8. Se crea la relación en user_roles

### Inicio de sesión

Endpoint:

```text
POST /api/auth/login
```

Solicitud:
```text
{
  "email": "admin@academic.test",
  "password": "Password123*"
}
```

Funcionamiento:

1. Solicitud de login
2. Normalización de correo
3. AuthenticactionManager
4. CustomUserDetailsService
5. Consulta del usuario y sus roles correspondientes.
6. Validación de la contraseña con BCrypt
7. Comprobación del estado del usuario. ACTIVE O BLOCKED
8. Generación del access token
9. Generación del fresh token
10. Respuesta al cliente

La respuesta sería similar a:

```text
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "valor-aleatorio-seguro",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

CustomUserDetailService sirve principalmente para la integración entre los usuarios del sistema y Spring Security. 

### Filtro de autenticación JWT
Este filtro (JwtAuthenticationFilter) se ejecuta una vez por cada solicitud HTTP

Básicamente:

1. Comprueba que exista el encabezado Authorization.
2. Verifica que comience con Bearer.
3. Extrae el JWT.
4. Obtiene el correo electrónico.
5. Carga al usuario desde la base de datos.
6. Valida la firma y expiración.
7. Recupera las autoridades del usuario.
8. establece la autenticación en SecurityContext.

Si el token es válido entonces los controladores pueden obtener la identidad del usuario mediante Principal.

### Consulta del usuario autenticado 

Endpoint:

```text
GET /api/auth/me
```

Solicitud:

```text
Authorization: Bearer ACCESS_TOKEN
```

La respuesta debe ser el estado completo del usuario, sin mostrar las contraseñas claro:

```text
{
  "id": 1,
  "firstName": "Administrador",
  "lastName": "Sistema",
  "email": "admin@academic.test",
  "status": "ACTIVE",
  "roles": [
    "ADMIN"
  ]
}
```

### Refresh tokens

Como bien sabemos los access token expiran muy rápido y cuando lo hacen, el usuario no debería de introducir sus credenciales en una sesión válida

Para resolver este problema se usan los Refresh tokens. Que tienen una duración mayor que el access token.

Se genera:

```text
byte[] randomBytes = new byte[64];
secureRandom.nextBytes(randomBytes);
```

Después se convierte en texto
```text
Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(randomBytes);
```

En PostgreSQL no se guarda el refresh token original. Se caldula un hash mediante SHA-256. La base de datos conserva únicamente un resultado similar a:
```text
be1bb4cf3ef9f39eb76d35d72d22369c...
```
Así evitamos revelar los refresh tokens utilizables en caso de que suceda una filtración.


### Renovación de sesión

Endpoint:

```text
POST /api/auth/refresh
```

Solicitud:

```text
{
  "refreshToken": "REFRESH_TOKEN_ACTUAL"
}
```
Hay que tener en cuenta que:

1. El token no debe existir
2. No debe estar revocado
3. No debe estar expirado
4. Por último, el usuario debe continuar activo.

Si se cumplen estos requerimentos entonces se genera el nuevo access tokenn junto al nuevo refresh token

Para entender mejor el funcionamiento, explicamos que se implemento una rotación de refresh tokens.

La aplicación aplica rotación de refresh tokens. Osea que el refresh token se puede utilizar una vez.

Funciona de la siguiente manera

1. Refresh token A activo
2. Se utiliza para renovar la sesión
3. Entonces se genera refresh token B
4. Refresh token A queda revocad
5. Y A registra que fue remplazado por B

Token A
```text
revoked_at = fecha de la rotación
replaced_by_token_id = UUID del nuevo token
```

Token B
```text
revoked_at = null
replaced_by_token_id = null
```

### Cierre de sesión

Endpoint:

```text
POST /api/auth/logout
```

Solicitud:

```text
{
  "refreshToken": "REFRESH_TOKEN_ACTIVO"
}
```

Se realizan las siguientes operaciones:

1. Calcula el hash del token.
2. Busca el registro correspondiente.
3. Comprueba si ya está revocado.
4. Registra la fecha actual en revoked_at.
5. Finaliza sin generar nuevos tokens.

Como respuesta obtendremos "204 No Content"

Después del logout, el refresh token ya no puede utilizarse en /auth/refresh.

### Cambio de estado de usuario

```text
PATCH /api/users/{id}/status
```

Solicitud:

```text
{
  "status": "****"
}
```

Debe ser BLOCKED o ACTIVE

El endpoint está protegido. Haicendo uso de @PreAuthorize("hasRole('ADMIN')"). Por lo que solo el ADMIN puede realizar este movimiento.

### Asignación de roles

```text
PUT /api/users/{id}/roles
```

Solicitud:

```text
{
  "roles": [
    "ORGANIZER",
    "PARTICIPANT"
  ]
}
```

Es este módulo la implementación permitió establecer un sistema de seguridad completo para la API. Los usuarios pueden registrarse e iniciar sesión, mientras que el servidor genera access tokens de corta duración y refresh tokens renovables.

El almacenamiento del hash de los refresh tokens mejora la protección de las sesiones. Además, la rotación impide que un token utilizado previamente pueda volver a renovar la sesión.

## Módulo categorías

Esta parte implementa las operaciones CRUD. La elimianción lógica, que evita borrar físicamente registros que podrían estar relacionados con eventos existentes.

Las operaciones de consulta están disponibles para cualquier usuario autenticado, mientras que la creación, actualización y eliminación de categorías están restringidas al rol ADMIN.

Estructura
```text
categories
├── controllers
│   └── CategoryController.java
├── dtos
│   ├── CategoryResponseDto.java
│   ├── CreateCategoryDto.java
│   └── UpdateCategoryDto.java
├── entities
│   └── CategoryEntity.java
├── mappers
│   └── CategoryMapper.java
├── repositories
│   └── CategoryRepository.java
└── services
    ├── CategoryService.java
    └── impl
        └── CategoryServiceImpl.java
```

Tenemos este método para revisar si ya existe una categoría con el mismo nombre

```text
boolean existsByNameIgnoreCase(String name);
```

También comprobar duplicados al actualizar
```text
boolean existsByNameIgnoreCaseAndIdNot(
        String name,
        Long id
);
```
Esta consulta busca otra categoría con el mismo nombre, una categoría puede conservar su propio nombre, pero no puede adoptar el nombre de otra categoría.

También el servicio implementa métodos internos para normalizar texto
Así evitamos valores como "      " y los convierte en null

### Listado de categorías

Endpoint:
```text
GET /api/categories
```

Respuesta:

![get categories](/assets/getcategories.png)

### Categoría por id

Endpoint:
```text
GET /api/categories/{i}
```

Respuesta:

![get categories por id](/assets/getcategoriesid.png)

### Registro de una categoría

Permiso: ROLE_ADMIN

Endpoint:
```text
GET /api/categories/{i}
```

Respuesta:

![post de una categoría](/assets/postcategories.png)

### Modificación de una categoría

Permiso: ROLE_ADMIN

Endpoint:
```text
PUT /api/categories/{id}
```

Respuesta:

![put de una categoría](/assets/putcategories.png)

### Eliminación de una categoría

Permiso: ROLE_ADMIN

Endpoint:
```text
DELETE /api/categories/{id}
```

Respuesta:

![delete de una categoría](/assets/deletecategories.png)

Eliminación lógica = false

![delete false](/assets/deletecategoriesfalse.png)


La aplicación permite consultar, crear, actualizar y desactivar categorías. También evita nombres duplicados, normaliza los valores recibidos y mantiene las fechas en UTC.

La eliminación lógica garantiza la conservación de la información y evita afectar futuras relaciones con eventos académicos.

### Gestión de eventos académicos

Este módulo de eventos es uno de los componenetes principales del sistema ya que se nor permite registrar, consultar y administrar eventos académicos disponibles para los usuarios

Cada evento contiene una información con

```text
título y descripción;
modalidad;
ubicación física o enlace virtual;
capacidad total y capacidad disponible;
periodo de inscripciones;
fecha de inicio y finalización;
estado;
organizador responsable;
categoría;
eliminación lógica;
versión para control de concurrencia.
```

También se implementó autorización basada en roles y propiedad del recurso. Un administrador puede gestionar cualquier evento, mientras que un organizador solamente puede modificar los eventos que le pertenecen.

Estructura del módulo:
```text
events
├── controllers
│   └── EventController.java
├── dtos
│   ├── CreateEventDto.java
│   ├── EventResponseDto.java
│   ├── UpdateEventDto.java
│   └── UpdateEventStatusDto.java
├── entities
│   └── EventEntity.java
├── enums
│   ├── EventModality.java
│   └── EventStatus.java
├── mappers
│   └── EventMapper.java
├── repositories
│   └── EventRepository.java
├── services
│   ├── EventService.java
│   └── impl
│       └── EventServiceImpl.java
└── specifications
    └── EventSpecifications.java
```

### Restricciones de la base de datos

La base de datos protege varias reglas

como la capacidad positiva capacity > 0

La capacidad disponible debe cumplir 0 <= available_capacity <= capacity

Las fechas:

registrationStartAt < registrationEndAt
registrationEndAt <= startAt
startAt < endAt

Modalidades:

PRESENTIAL → necesita ubicación y no admite URL virtual
VIRTUAL    → necesita URL virtual y no admite ubicación física
HYBRID     → necesita ubicación y URL virtual

Estas reglas se validan tanto en Java como en PostgreSQL.

### Ciclo de vida de los estados

Las transacciones deben: 
```text
DRAFT → PUBLISHED
DRAFT → CANCELLED

PUBLISHED → FINISHED
PUBLISHED → CANCELLED
```

No se debe, ni se puede:
```text
FINISHED → PUBLISHED
FINISHED → DRAFT
CANCELLED → PUBLISHED
CANCELLED → DRAFT
```

### Condiciones para publicar un evento

Para pasar a: DRAFT → PUBLISHED

Debemos comprobar:

```text
el evento todavía no haya comenzado;
el periodo de inscripciones no haya finalizado;
la categoría siga activa;
la capacidad sea mayor que cero.
```

### Finalización del evento

Un evento publicado puede cambiar a Finished cuando su fecha de finalización ya haya llegado.

### Relaciones

Relación con el organizador

```text
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
        name = "organizer_id",
        nullable = false
)
private UserEntity organizer;
```

Cada evento pertenece a un usuario organizador

Relación con categoría
```text
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
        name = "category_id",
        nullable = false
)
private CategoryEntity category;
```

Cada evento debe estar asociado con una categoría activa

Aquí también se elimina lógicamente.


### Filtros creados


Modalidad

```text
GET /api/events?modality=VIRTUAL
```

Categoría

```text
GET /api/events?categoryId=2
```

Organizador

```text
GET /api/events?organizerId=2
```

Intervalo de fechas

```text
GET /api/events?organizerId=2
```

### Actualización de capacidad

La capacidad disponible no puede reemplazarse directamente con la capacidad total.

Primero se calcula cuántas personas ya están inscritas:
```text
int registeredParticipants =
        event.getCapacity()
        - event.getAvailableCapacity();
```

Después se valida la nueva capacidad:

```text
if (dto.capacity() < registeredParticipants) {
    throw new IllegalStateException(
            "La capacidad no puede ser menor que el número de participantes inscritos"
    );
}
```

Finalmente:

```text
int newAvailableCapacity =
        dto.capacity() - registeredParticipants;
```

Ejemplo: 
```text
Capacidad actual: 100
Disponibles: 70
Inscritos: 30
Nueva capacidad: 120
Nuevos disponibles: 90
```

### Actualización del evento

El endpoint actualiza:

```text
título;
descripción;
modalidad;
ubicación;
URL virtual;
capacidad;
fechas;
categoría;
fecha de modificación.
```

Pero conserva:

```text
identificador;
organizador;
estado;
fecha de creación;
indicador de eliminación.
```

### Cambio de estado

El cambio de estado utiliza un endpoint independiente:

```text
PATCH /api/events/{id}/status
```

Ejemplo:
```text
{
  "status": "PUBLISHED",
  "version": 0
}
```

### Visibilidad según el rol

El listado y la consulta individual aplican diferentes reglas.

#### Administrador

Puede consultar todos los eventos que no estén eliminados: DRAFT, PUBLISHED, FINISHED, CANCELLED

#### Organizador

Puede consultar: todos los eventos publicados, sus propios eventos en borrador, sus propios eventos cancelados, sus propios eventos finalizados.

#### Participante

Solo puede consultar: PUBLISHED

### Ocultamiento de recursos

Cuando un usuario intenta consultar un evento que existe, pero no tiene permiso para verlo, el sistema utiliza provisionalmente:
```text
Evento no encontrado
```

en lugar de revelar:

```text
No tiene permisos para ver este evento
```

Esto evita que un usuario descubra la existencia de borradores privados probando diferentes identificadores.

### Ejemplo de consulta paginada

```text
GET /api/events?page=0&size=5&sort=startAt,asc
Authorization: Bearer ACCESS_TOKEN
```

La respuesta contiene una página con los eventos visibles para el usuario.

La estructura puede incluir:

```text
{
  "content": [
    {
      "id": 4,
      "title": "Evento académico",
      "status": "PUBLISHED",
      "modality": "VIRTUAL",
      "organizerId": 2,
      "categoryId": 1,
      "version": 1
    }
  ],
  "page": {
    "size": 5,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

La implementación permitió construir un módulo completo para administrar eventos académicos.

El sistema permite crear, consultar, actualizar, publicar, cancelar, finalizar y eliminar lógicamente eventos, manteniendo la integridad de las fechas, modalidades, capacidades y relaciones.

Los filtros dinámicos permiten encontrar eventos mediante diferentes criterios sin crear un endpoint independiente para cada combinación.

La autorización protege las operaciones administrativas y garantiza que los organizadores solo puedan modificar sus propios recursos.

El control de concurrencia mediante @Version evita sobrescribir cambios realizados por otros usuarios.

## Redis, Rate Limiting y CORS

Se implementó rate limiting distribuido usando Redis, con contadores atómicos asociados a IP, usuario autenticado o combinación IP+correo, según el tipo de endpoint:

| Endpoint | Clave | Límite |
|---|---|---|
| `POST /auth/login` | IP + correo | 5 solicitudes/minuto |
| `POST /auth/register` | IP | 3 solicitudes/hora |
| Endpoints públicos | IP | 60 solicitudes/minuto |
| Endpoints autenticados | Usuario autenticado | 120 solicitudes/minuto |
| Generación de reportes | Usuario autenticado | 5 solicitudes/minuto |

Al superar el límite, la API responde `429 Too Many Requests` con el encabezado `Retry-After`, indicando en segundos cuándo puede volver a intentarse.

Adicionalmente, se implementó bloqueo temporal tras varios intentos fallidos de login (5 intentos en 15 minutos), bloqueando tanto la IP como el correo involucrados durante 15 minutos, usando claves con prefijo `blocked-user:` y `blocked-ip:`.

Todas las claves temporales en Redis tienen TTL definido, sin usarse como almacenamiento permanente.

La estructura es la siguiente:

```text
src/main/java/ec/edu/ups/icc/proyectointegrador/security/ratelimit
├── CachedBodyHttpServletRequest.java
├── LoginAttemptService.java
├── RateLimitFilter.java
├── RateLimiterService.java
└── RateLimitResult.java
```

También se restringió CORS mediante `core/config/CorsConfig.java`, permitiendo únicamente los orígenes definidos en la variable de entorno `ALLOWED_ORIGINS`, los métodos `GET, POST, PUT, PATCH, DELETE, OPTIONS`, y los headers `Authorization` y `Content-Type`, sin usar `*` ni habilitar credenciales.

Se probó enviando 6 solicitudes seguidas de login: las primeras 5 respondieron `200 OK`, y la sexta respondió `429 Too Many Requests`.

![Rate limiting probado con PowerShell](/assets/ratelimit-powershell.png)


## Manejo Centralizado de Excepciones

Se implementó `GlobalExceptionHandler` (`@RestControllerAdvice`) para capturar y transformar excepciones en respuestas JSON uniformes, evitando exponer errores 500 sin control.

Estructura de la respuesta de error:

```json
{
  "timestamp": "...",
  "status": 401,
  "code": "INVALID_CREDENTIALS",
  "message": "Credenciales inválidas.",
  "path": "/api/auth/login",
  "errors": null
}
```

Casos manejados:

| Excepción | Status | Código |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` (incluye errores por campo) |
| `BadCredentialsException` | 401 | `INVALID_CREDENTIALS` |
| `AuthenticationException` | 401 | `UNAUTHENTICATED` |
| `AccessDeniedException` | 403 | `ACCESS_DENIED` |
| `DataIntegrityViolationException` | 409 | `DATA_CONFLICT` |
| `IllegalStateException` | 409 | `BUSINESS_RULE_VIOLATION` |
| `NoSuchElementException` | 404 | `RESOURCE_NOT_FOUND` |
| Cualquier otra excepción | 500 | `INTERNAL_ERROR` |

Se probó enviando credenciales inválidas a `/auth/login`: la API respondió `401` con `code: "INVALID_CREDENTIALS"` en lugar de un error 500 sin controlar.

![Manejo de excepciones probado con PowerShell](/assets/exceptions-powershell.png)


## Swagger Protegido, Actuator y Observabilidad

Se integró Swagger UI mediante `springdoc-openapi`, protegido con autenticación HTTP Basic independiente del JWT usado por el resto de la API (`SwaggerSecurityConfig`), con credenciales configurables por variables de entorno (`SWAGGER_USERNAME`, `SWAGGER_PASSWORD`).

Se configuró `OpenApiConfig` con el esquema de seguridad Bearer JWT, permitiendo probar endpoints protegidos directamente desde Swagger UI.

Actuator expone únicamente `/actuator/health`, sin detalles internos, accesible sin autenticación.

Se probó accediendo a `/swagger-ui/index.html` con Basic Auth: el servidor respondió correctamente con el documento HTML de Swagger UI, confirmando que la protección y la documentación funcionan.

![Swagger UI accedido con Basic Auth desde PowerShell](/assets/swagger-powershell.png)

## Manejo centralizado de errores completo

Se complemento en el sistema agregando excepciones personailizadas. Es decir, se crearon excepciones específicas para evitar el uso general de IllegalStateException y diferenciar correctamente cada tipo de error.

```text
ResourceNotFoundException: Se utiliza cuando un recurso no existe

404 Not Found
RESOURCE_NOT_FOUND
```

```text
ConflictException: Se utiliza cuando una operación entra en un conflicto con los datos actuales

409 Conflict
CONFLICT
```

```text
BusinessRuleException: Se utiliza cuando se incumple alguna regla de negocio

409 Conflict
BUSINESS_RULE_VIOLATION
```

```text
InvalidRefreshTokenException Se utiliza cuando un refresh token no es válido para generar una nueva sesión

401 Unauthorized
UNAUTHENTICATED
```

```text
InternalServerException: se utiliza para fallos internos que no se corresponden a una regla de negocio o error enviado por el usaurio

500 Internal Server Error
INTERNAL_ERROR

```

```text
La validaciones como:
@NotBlank
@NotNull
@Positive
@Email
@Size

son procesadas mediante

MethodArgumentNotValidException

```

```text
IllegalArgumentException: Se utiliza cuando el refresh token no fue enviado


400 Bad Request
INVALID_ARGUMENT
```

```text
BadCredentialsException: Cuando las credenciales son incorrectas

{
  "timestamp": "2026-07-26T12:30:00-05:00",
  "status": 401,
  "code": "INVALID_CREDENTIALS",
  "message": "Credenciales inválidas.",
  "path": "/api/auth/login",
  "errors": null
}
```

### Errores generados dentro de Spring Security

Las excepciones generadas dentro d ela caden ade filtros de Spring Security ocurren antes de llegar a los controladores. Esa es la razón del porque GlobalExceptionHandler no puede capturar siempre estos errores.

```text
JwtAuthenticationEntryPoint: Se ejecuta cuando un usuario intenta acceder a un endpoint protegido sin autenticarse
```

```text
JwtAccessDeniedHandler: Se ejecuta cuando el usuario está autenticado, pero no tiene el rol o permiso requerido.

```

El manejo global de errores permite que la API responda de forma consistente ante errores de validadción, autenticación, recursos inexistentes, conflictos y reglas de negocio

## Módulo de Sesiones

Se implementaron las sesiones. Que están asociadas a un evento y debe respetar tanto el horario general del evento como las reglas de solapamiento.

### 

Reglas de negocio

La fecha de inicio debe ser anterior a la fecha de finalización
es decir. startAt < endAt

La sesión debe estar completamente contenida dentro del horario del evento. 

No se permite crear, ni actualizar una sesión que coincide con otra sesión del mismo evento. Además no se puede repetir dentro del mismo evento la sesión. Solo se puede crear o modificar o eliminar sesiones si se tiene rol de Admiin o si es organizador, dueño del evento.

Sin embargo, si se puede consultar las sesiones. Los administradores, propietarios o usuarios autenticados mientras el evento sea publico.

Es un CRUD sencillo así que los endpoints son los mismos.

```text
GET	/api/sessions/event/{eventId}
GET /api/sessions/{id}
POST	/api/sessions	Crea una sesión
PUT	/api/sessions/{id}	Actualiza una sesión
DELETE	/api/sessions/{id}
```

### Casos de error probados

Sesión inexistente
```text
404 Not Found
RESOURCE_NOT_FOUND
```

Sesión duplicada
```text
409 Conflict
CONFLICT
```

Sesión solapada
```text
409 Conflict
CONFLICT
Mensaje: La sesión se superpone con otra sesión del evento 
```

Sesión fuera del horario del evento
```text
409 Conflict
BUSINESS_RULE_VIOLATION
Mensaje:
La sesión debe desarrollarse dentro del horario del evento
```

Usuario sin permisos
```text
403 Forbidden
ACCESS_DENIED
```

## Inscripciones

Este apartado nos permite que los usuarios autenticados se registren en eventos académicos publicados

Cada inscripción relaciona:
Un evento, un usuario participantes, un código público UUID, un estado, fechas de creación y actualización, control de concurrencia mediante versión.

Las inscripciones pueden tener los siguientes estados:

| Estado | Descripción |
|---|---|
| `PENDING` | La inscripción fue creada y espera revisión. |
| `CONFIRMED` | La inscripción fue aprobada. |
| `REJECTED` | La inscripción fue rechazada. |
| `CANCELLED` | La inscripción fue cancelada por el participante o un administrador. |

Una inscripción rechazada o cancelada no puede volver a otro estado

### Reglas de negocio

Para crear una inscripción se validan las siguientes condiciones:
```text
El usuario autenticado debe existir y estar activo.
El evento debe existir y no estar eliminado.
El evento debe estar en estado PUBLISHED.
La fecha actual debe encontrarse dentro del periodo de inscripción.
El evento debe tener cupos disponibles.
El participante no debe tener otra inscripción en el mismo evento.
```

Cuando se crea una inscripción:
```text
se genera un código UUID;
el estado inicial es PENDING;
se disminuye en uno la capacidad disponible del evento;
la inscripción y la actualización del cupo se guardan dentro de la misma transacción.
```

Para evitar que dos participantes ocupen simultáneamente el último cupo, se utiliza un bloqueo pesimista sobre el evento. Es decir, durante la creación o cancelación de una inscripción, la fila del evento permanece bloqueada hasta finalizar la transacción.


### Permisos

vamos con una parte primordia. Ahora los permisoso que tiene cada usuario dependiendo de su rol.

#### Participante autenticado

Puede:
```text
crear una inscripción;
consultar sus propias inscripciones;
consultar una inscripción propia por ID o UUID;
cancelar una inscripción propia pendiente o confirmada.
```

#### Organizador

Puede:
```text
listar las inscripciones de sus propios eventos;
consultar inscripciones relacionadas con sus eventos;
confirmar inscripciones pendientes;
rechazar inscripciones pendientes.

No puede administrar inscripciones de eventos pertenecientes a otro organizador.
```

#### Administrador

Puede:
```
consultar cualquier inscripción;
listar inscripciones de cualquier evento;
confirmar o rechazar inscripciones;
cancelar inscripciones.
```

### Endpoints

#### Crear una inscripción.

Endpoint

```text
POST /api/registrations
```

Cuerpo:
```text
{
  "eventId": 10
}
```

#### Listar mis inscripciones.

Endpoint

```text
GET /api/registrations/me
```

#### Consultar una inscripción por ID.

Endpoint

```text
GET /api/registrations/{id}
```

#### Consultar una inscripción por código UUID.

Endpoint

```text
GET /api/registrations/code/{registrationCode}
```

#### Listar inscripciones de un evento.

Endpoint

```text
GET /api/registrations/event/{eventId}
```

#### Actualizar el estado.

Endpoint

```text
PATCH /api/registrations/{id}/status
```

Cuerpo:
```text
{
  "status": "CONFIRMED",
  "version": 0
}
```

#### Cancelar una inscripción.

Endpoint

```text
DELETE /api/registrations/{id}
```

Cuerpo:
```text
{
  "version": 1
}
```
### Respuesta de error

#### 400 Bad Request
Reglas de negocio inválidas.

```text
{
  "message": "Solo es posible inscribirse en eventos publicados"
}
```

```text
{
  "message": "El periodo de inscripciones ya finalizó"
}
```

```text
{
  "message": "La inscripción ya está cancelada"
}
```

#### 401 Unauthorized
No dio un token válido

#### 403 Forbidden
El usuario está autenticado, pero no tiene el rol para ejecutar la operación.

#### 404 Not Found
El evento, usuario o inscripción no existe

#### 409 Conflict
Se utiliza para inscripciones duplicadas

```text
{
  "message": "El participante ya tiene una inscripción en este evento"
}
```

```text
{
  "message": "La inscripción fue modificada por otro usuario. Actualice la información e intente nuevamente"
}
```


## Documentación Swagger

Se documento la API Swagger/OpenAPI, para visualizar los enpoints disponibles.

Cada endpoint incluye una descripción de su función, los permisos requeridos y respuestas HTTP que es capaz de generar. Usualmente 200, 201, 400, 401, 402, 404 y 409


## Auditoría

Se implementó registro automático de operaciones críticas mediante un aspecto de Spring AOP (`AuditAspect`), que intercepta toda petición `POST`, `PUT`, `PATCH` y `DELETE` que llega a cualquier `@RestController` del sistema, sin requerir cambios en los controladores existentes de otros módulos.

### Qué se registra

Por cada operación se guarda:

| Campo | Origen |
|---|---|
| `actor_id` | Usuario autenticado, resuelto desde el JWT vía `UserRepository`. `null` si la petición es anónima |
| `action` | Derivada automáticamente del método HTTP + recurso (ej. `POST_CATEGORIES`, `PATCH_REGISTRATIONS`) |
| `resource_type` | Nombre del controlador sin el sufijo `Controller` (ej. `CategoriesController` → `CATEGORIES`) |
| `resource_id` | Extraído del `@PathVariable` de la ruta si existe, o del campo `id` de la respuesta cuando la operación es una creación |
| `new_value` | Cuerpo de la petición (`@RequestBody`), serializado a JSON, con campos sensibles (`password`, `passwordHash`, `token`, `accessToken`, `refreshToken`) enmascarados como `***` |
| `result` | `SUCCESS` o `FAILED`, según si la operación lanzó una excepción |
| `ip_address`, `http_method`, `endpoint` | Extraídos directamente de la petición HTTP |
| `correlation_id` | UUID generado por cada petición, para poder correlacionar con logs técnicos |

### Diseño no invasivo

El registro se implementó completamente con AOP, interceptando los controladores desde fuera, sin necesidad de modificar ningún servicio existente de los módulos de usuarios, categorías, eventos, sesiones o inscripciones.

El guardado corre en una transacción independiente (`REQUIRES_NEW`) con manejo de errores propio: si el registro de auditoría falla por cualquier motivo, nunca afecta ni interrumpe la petición real del usuario.

### Estructura

```text
src/main/java/ec/edu/ups/icc/proyectointegrador/audit
├── aspects
│   └── AuditAspect.java
├── entities
│   └── AuditLogEntity.java
├── enums
│   └── AuditResult.java
├── repositories
│   └── AuditLogRepository.java
└── services
    ├── AuditService.java
    └── impl
        └── AuditServiceImpl.java
```

### Prueba realizada

Se inició sesión como usuario `ADMIN` y se creó una categoría nueva mediante `POST /api/categories`. Se verificó directamente en PostgreSQL que el registro de auditoría se guardó correctamente, con el `actor_id` del usuario autenticado, `action: POST_CATEGORIES`, `result: SUCCESS`, y sin exponer ningún dato sensible.

### Limitación conocida

El campo `previous_value` no se completa automáticamente para operaciones de actualización, ya que requeriría cargar el estado anterior del recurso antes de cada operación de forma genérica para cualquier entidad del sistema. Actualmente solo se registra `new_value` (el cuerpo de la petición). Esto podría extenderse en una siguiente iteración agregando una anotación personalizada (`@Audited`) por endpoint que indique explícitamente cómo obtener el estado previo.

## Reportes, Estadísticas y Archivos Descargables

Se implementó el módulo de reportes, que permite consultar y descargar archivos generados bajo demanda a partir de los datos de inscripciones, respetando los roles y la propiedad de los eventos.

### Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/reports/events/{eventId}/registrations.pdf` | Organizador propietario o ADMIN | Listado de inscritos en formato PDF |
| `GET` | `/api/reports/events/{eventId}/registrations.xlsx` | Organizador propietario o ADMIN | Listado de inscritos en formato Excel |
| `GET` | `/api/registrations/{id}/certificate.pdf` | Participante propietario | Comprobante de una inscripción confirmada |

Ambos endpoints de listado aceptan filtros opcionales por rango de fechas (`from`, `to`, formato `yyyy-MM-dd`).

### Reglas de negocio aplicadas

- Los reportes de un evento solo pueden generarlos el organizador dueño del evento o un usuario con rol `ADMIN`.
- El comprobante de inscripción solo puede descargarlo el participante propietario de esa inscripción.
- El comprobante solo se genera si la inscripción está en estado `CONFIRMED`; en cualquier otro caso, se responde `409 Conflict`.
- Los archivos se generan bajo demanda (no se almacenan en disco ni en base de datos).

### Encabezados de respuesta

Ambos tipos de reporte responden con `Content-Type` y `Content-Disposition` correctos:

Content-Type: application/pdf
Content-Disposition: attachment; filename="registrations-event-1.pdf"

### Librerías utilizadas

- **Apache POI** (`poi-ooxml`), para la generación de archivos Excel (`.xlsx`).
- **OpenPDF**, para la generación de archivos PDF.

### Estructura

```text
src/main/java/ec/edu/ups/icc/proyectointegrador/reports
├── controllers
│   └── ReportController.java
└── services
    ├── ReportService.java
    └── impl
        └── ReportServiceImpl.java
```

### Pruebas realizadas

Se generaron ambos reportes (PDF y Excel) de un evento con inscripciones reales, autenticado como usuario `ADMIN`. Ambos archivos se descargaron correctamente, con las fechas de inscripción mostradas en hora de Ecuador (ver sección de Zona Horaria).

### Documentación capturas

#### Roles

![Roles Swagger](/assets/rolesSwagger.png)

#### Autenticación

![Autenticación Swagger](/assets/autenticacionSwagger.png)

#### Usuarios

![Usuarios Swagger](/assets/usuariosSwagger.png)

#### Eventos

![Eventos Swagger](/assets/eventosSwagger.png)

#### Sesiones

![Sesiones Swagger](/assets/sesionesSwagger.png)

#### Inscripciones 

![Inscripciones Swagger](/assets/inscripcionesSwagger.png)

#### Categorias

![Categorias Swagger](/assets/categoriasSwagger.png)

### Auditorias
![Registro de auditoría verificado en PostgreSQL](/assets/audit-postgres.png)

### Reportes
![Reporte PDF de inscritos por evento](/assets/report-pdf.png)
![Reporte Excel de inscritos por evento](/assets/report-excel.png)
