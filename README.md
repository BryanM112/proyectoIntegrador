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

## Roles

