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