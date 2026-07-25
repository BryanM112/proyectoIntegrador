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