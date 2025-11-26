# Favourite Service

Favourite Service para sistema de ecommerce. Gestiona los productos favoritos de los usuarios, permitiendo que los usuarios marquen productos como favoritos.

## Características

- Spring Boot 2.5.7 con Java 11
- Base de datos: H2 (dev) / MySQL (stage/prod)
- Service Discovery: Eureka Client
- Circuit Breaker: Resilience4j para tolerancia a fallos
- Actuator para health checks
- Clave primaria compuesta: Favourite identificado por userId, productId y likeDate
- Integración con User Service y Product Service

## Endpoints

Prefijo: `/favourite-service`

### Favourite API

```
GET    /api/favourites                                    - Listar todos los favoritos
GET    /api/favourites/{userId}/{productId}/{likeDate}    - Obtener favorito por ID compuesto
GET    /api/favourites/find                               - Obtener favorito por ID (POST con body)
POST   /api/favourites                                    - Crear favorito
PUT    /api/favourites                                    - Actualizar favorito
DELETE /api/favourites/{userId}/{productId}/{likeDate}    - Eliminar favorito por ID compuesto
DELETE /api/favourites/delete                             - Eliminar favorito (POST con body)
```

**Ejemplo de payload para crear favorito:**

```json
{
  "userId": 1,
  "productId": 1,
  "likeDate": "10-06-2025__13:12:22:606444"
}
```

**Formato de fecha:**

La fecha debe seguir el formato: `dd-MM-yyyy__HH:mm:ss:SSSSSS` (día-mes-año__hora:minuto:segundo:microsegundos)

**Nota sobre ID compuesto:**

El Favourite tiene una clave primaria compuesta por `userId`, `productId` y `likeDate`. Para obtener o eliminar un favorito específico, se deben proporcionar los tres valores en la URL:
- `GET /api/favourites/1/5/10-06-2025__13:12:22:606444` - Obtiene el favorito con userId=1, productId=5 y likeDate específico
- `DELETE /api/favourites/1/5/10-06-2025__13:12:22:606444` - Elimina el favorito con esos valores

**Nota sobre codificación de URL:**

Cuando se usa el endpoint con parámetros en la URL, la fecha debe estar codificada (URL encoded) si contiene caracteres especiales. El servicio maneja automáticamente la decodificación.

## Testing

### Unit Tests

El servicio incluye pruebas unitarias para validar la lógica de negocio de favoritos.

### Integration Tests

El servicio incluye pruebas de integración para validar la comunicación con la base de datos y otros servicios.

```bash
./mvnw test
```

## Ejecutar

```bash
# Opción 1: Directamente
./mvnw spring-boot:run

# Opción 2: Compilar y ejecutar
./mvnw clean package
java -jar target/favourite-service-v0.1.0.jar
```

Service corre en: `http://localhost:8800/favourite-service`

## Configuración

### Circuit Breaker (Resilience4j)

El servicio está configurado con circuit breaker para tolerancia a fallos:

- Failure rate threshold: 50%
- Minimum number of calls: 5
- Sliding window size: 10
- Wait duration in open state: 5s
- Sliding window type: COUNT_BASED

### Service Discovery

El servicio se registra automáticamente en Eureka Server con el nombre `FAVOURITE-SERVICE`.

### Health Checks

El servicio expone endpoints de health check a través de Spring Boot Actuator:

```
GET /favourite-service/actuator/health
```

## Funcionalidades Implementadas

- Gestión completa de favoritos (CRUD)
- Clave primaria compuesta (userId + productId + likeDate)
- Integración con User Service para obtener información de usuarios
- Integración con Product Service para obtener información de productos
- Validaciones de campos requeridos
- Manejo de excepciones personalizado
- Circuit breaker para resiliencia
- Integración con Service Discovery (Eureka)
- Manejo de errores al obtener información de otros servicios (continúa sin userDto/productDto si falla)

## Comunicación con Otros Servicios

El Favourite Service se comunica con otros microservicios a través del API Gateway:

- **User Service**: Para obtener información completa del usuario que marcó el favorito
- **Product Service**: Para obtener información completa del producto marcado como favorito

Todas las comunicaciones se realizan a través del API Gateway y el Service Discovery (Eureka).

## Notas Importantes

### Favoritos

- Un favorito representa que un usuario ha marcado un producto como favorito en una fecha específica
- La clave primaria es compuesta: `(userId, productId, likeDate)`
- El campo `likeDate` permite que un usuario marque el mismo producto como favorito múltiples veces (en diferentes fechas)
- Al obtener un favorito, el servicio automáticamente obtiene la información completa del usuario y del producto desde sus respectivos servicios

### Integración con User Service y Product Service

El servicio utiliza RestTemplate para obtener información de usuarios y productos desde sus respectivos servicios. Esto permite que los favoritos incluyan información completa sin necesidad de duplicar datos.

**Manejo de errores:**

- Si falla la obtención del usuario, el servicio registra el error pero continúa (el favorito se retorna sin userDto)
- Si falla la obtención del producto, el servicio registra el error pero continúa (el favorito se retorna sin productDto)
- Si falla al obtener un favorito específico por ID, se lanza una excepción

### Formato de Fecha

El formato de fecha utilizado es: `dd-MM-yyyy__HH:mm:ss:SSSSSS`

Ejemplo: `10-06-2025__13:12:22:606444` representa el 10 de junio de 2025 a las 13:12:22.606444

Este formato permite identificar de manera única cada favorito, incluso si un usuario marca el mismo producto como favorito múltiples veces en el mismo día.
