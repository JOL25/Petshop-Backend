# Guia Backend - Petshop

Esta guia organiza la creacion del backend de la Petshop en tareas ordenadas para reducir errores y evitar rehacer partes importantes.

## 1. Definir alcance inicial

- Confirmar que el backend sera una API REST con Spring Boot.
- Confirmar que la base de datos sera Supabase PostgreSQL.
- Confirmar que el frontend sera React y consumira la API.
- Definir roles iniciales:
  - `ADMIN`: gestiona productos, categorias, stock y pedidos.
  - `CLIENTE`: compra productos y consulta sus pedidos.
- Definir categorias iniciales:
  - Cosmeticos
  - Ropa
  - Juguetes
  - Comida

## 2. Crear proyecto Spring Boot

- Crear proyecto con Java 17 o Java 21.
- Usar Maven como gestor de dependencias.
- Definir nombre base del proyecto: `petshopbackend`.
- Definir paquete principal recomendado:
  - `com.petshop.backend`

Dependencias iniciales recomendadas:

- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- Validation
- Lombok
- Spring Boot DevTools

Dependencias para etapas posteriores:

- JWT
- Flyway o Liquibase
- Swagger/OpenAPI
- SDK o cliente HTTP para pasarela de pagos

## 3. Configurar estructura de carpetas

Crear una estructura clara antes de programar funcionalidades:

```text
src/main/java/com/petshop/backend
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── exception
├── repository
├── security
├── service
└── util
```

Objetivo:

- `controller`: endpoints REST.
- `service`: logica de negocio.
- `repository`: acceso a base de datos.
- `entity`: modelos JPA.
- `dto`: datos de entrada y salida.
- `security`: autenticacion, autorizacion y JWT.
- `config`: configuraciones generales.
- `exception`: manejo centralizado de errores.

## 4. Conectar con Supabase

- Crear un proyecto en Supabase.
- Obtener los datos de conexion PostgreSQL.
- Configurar variables de entorno para no exponer credenciales.

Variables recomendadas:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
```

Configurar `application.properties` o `application.yml`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Importante:

- Usar `ddl-auto=update` solo durante desarrollo.
- Para produccion, usar migraciones con Flyway o Liquibase.

## 5. Disenar modelo de base de datos

Crear primero las entidades principales:

### Usuario

Campos sugeridos:

- `id`
- `name`
- `email`
- `password`
- `role`
- `enabled`
- `createdAt`
- `updatedAt`

### Producto

Campos sugeridos:

- `id`
- `name`
- `description`
- `price`
- `stock`
- `imageUrl`
- `category`
- `active`
- `createdAt`
- `updatedAt`

### Categoria

Campos sugeridos:

- `id`
- `name`
- `description`
- `active`

### Pedido

Campos sugeridos:

- `id`
- `user`
- `status`
- `total`
- `createdAt`
- `updatedAt`

Estados sugeridos:

- `PENDING`
- `PAID`
- `CANCELLED`
- `SHIPPED`
- `DELIVERED`

### Detalle de pedido

Campos sugeridos:

- `id`
- `order`
- `product`
- `quantity`
- `unitPrice`
- `subtotal`

### Pago

Campos sugeridos:

- `id`
- `order`
- `provider`
- `providerPaymentId`
- `status`
- `amount`
- `createdAt`
- `updatedAt`

## 6. Crear repositorios

Crear repositorios JPA para:

- `UserRepository`
- `ProductRepository`
- `CategoryRepository`
- `OrderRepository`
- `OrderItemRepository`
- `PaymentRepository`

Validar busquedas necesarias:

- Buscar usuario por email.
- Listar productos activos.
- Filtrar productos por categoria.
- Buscar pedidos por usuario.
- Buscar pago por id del proveedor.

## 7. Implementar manejo de errores

Crear errores personalizados antes de exponer muchos endpoints:

- `ResourceNotFoundException`
- `BadRequestException`
- `UnauthorizedException`
- `ForbiddenException`

Crear un `GlobalExceptionHandler` con `@RestControllerAdvice`.

Objetivo:

- Responder errores con formato uniforme.
- Evitar stack traces en respuestas al cliente.
- Devolver codigos HTTP correctos.

## 8. Implementar autenticacion

Primera meta:

- Login de administrador.
- Generacion de JWT.
- Proteccion de rutas privadas.

Endpoints sugeridos:

```text
POST /api/auth/login
POST /api/auth/register-admin
```

Nota:

- `register-admin` deberia usarse solo en desarrollo o protegerse despues.
- En produccion, el primer admin puede crearse por seed o manualmente en base de datos.

## 9. Configurar autorizacion por roles

Rutas publicas:

```text
GET /api/products
GET /api/products/{id}
GET /api/categories
POST /api/auth/login
```

Rutas de administrador:

```text
POST /api/admin/products
PUT /api/admin/products/{id}
DELETE /api/admin/products/{id}
POST /api/admin/categories
PUT /api/admin/categories/{id}
GET /api/admin/orders
PUT /api/admin/orders/{id}/status
```

Rutas de cliente:

```text
POST /api/orders
GET /api/orders/my-orders
GET /api/orders/{id}
```

## 10. Implementar modulo de productos

Orden recomendado:

- Crear entidad `Product`.
- Crear DTOs de request y response.
- Crear repository.
- Crear service.
- Crear controller publico.
- Crear controller admin.
- Validar campos obligatorios.
- Agregar borrado logico con `active=false`.

Reglas importantes:

- No permitir precio negativo.
- No permitir stock negativo.
- No mostrar productos inactivos en el catalogo publico.
- No eliminar fisicamente productos con pedidos asociados.

## 11. Implementar modulo de categorias

Orden recomendado:

- Crear entidad `Category`.
- Crear repository.
- Crear service.
- Crear controller publico.
- Crear controller admin.
- Crear categorias iniciales.

Reglas importantes:

- No permitir nombres duplicados.
- No eliminar categorias con productos asociados.
- Usar borrado logico si ya existen relaciones.

## 12. Implementar pedidos

Orden recomendado:

- Crear entidad `Order`.
- Crear entidad `OrderItem`.
- Crear DTO para crear pedido.
- Validar stock disponible.
- Calcular total en backend.
- Descontar stock cuando el pedido se confirme o pague, segun la regla elegida.

Recomendacion:

- Crear pedido con estado `PENDING`.
- Confirmar pago mediante webhook.
- Cambiar pedido a `PAID` solo cuando el proveedor confirme el pago.

## 13. Integrar pagos en linea

Elegir proveedor:

- Mercado Pago para una opcion comun en Latinoamerica.
- Stripe si la cuenta y el pais lo permiten.

Flujo recomendado:

```text
React crea pedido -> Backend crea preferencia de pago -> Cliente paga -> Proveedor llama webhook -> Backend confirma pago -> Backend actualiza pedido
```

Endpoints sugeridos:

```text
POST /api/payments/create-preference
POST /api/payments/webhook
GET /api/payments/{id}
```

Reglas importantes:

- No confiar en el frontend para marcar un pedido como pagado.
- Validar siempre la notificacion o webhook del proveedor.
- Guardar el id externo del pago.
- Manejar pagos rechazados, pendientes y aprobados.

## 14. Documentar API

Agregar Swagger/OpenAPI para probar endpoints:

Dependencia recomendada:

```text
springdoc-openapi-starter-webmvc-ui
```

Ruta esperada:

```text
/swagger-ui.html
```

Documentar:

- Auth
- Productos
- Categorias
- Pedidos
- Pagos
- Admin

## 15. Crear datos iniciales

Crear seed o carga inicial para:

- Roles.
- Usuario administrador inicial.
- Categorias iniciales.
- Productos de prueba.

Ejemplo de admin inicial:

```text
email: admin@petshop.com
role: ADMIN
```

No guardar contrasenas reales en el codigo.

## 16. Configurar CORS

Permitir llamadas desde React durante desarrollo:

```text
http://localhost:5173
```

Para produccion, reemplazar por el dominio real del frontend.

## 17. Validar seguridad basica

Revisar antes de avanzar:

- Las contrasenas se guardan encriptadas con BCrypt.
- Las rutas admin requieren rol `ADMIN`.
- El JWT tiene expiracion.
- Las credenciales no estan en GitHub.
- Los errores no exponen informacion sensible.

## 18. Crear pruebas

Pruebas recomendadas:

- Test de servicios para productos.
- Test de servicios para pedidos.
- Test de login.
- Test de endpoints admin protegidos.
- Test de calculo de total.
- Test de stock insuficiente.

## 19. Preparar variables de entorno

Crear archivo de ejemplo:

```text
.env.example
```

Contenido sugerido:

```text
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION=
PAYMENT_PROVIDER=
PAYMENT_ACCESS_TOKEN=
FRONTEND_URL=
```

No subir `.env` real al repositorio.

## 20. Checklist antes de conectar con React

Antes de iniciar el frontend, verificar:

- El backend levanta sin errores.
- La conexion con Supabase funciona.
- Swagger abre correctamente.
- Login admin devuelve JWT.
- Productos se pueden crear, editar, listar y desactivar.
- Categorias se pueden crear y listar.
- Pedidos calculan total desde backend.
- CORS permite peticiones desde React.

## 21. Orden final recomendado de implementacion

1. Crear proyecto Spring Boot.
2. Agregar dependencias.
3. Configurar Supabase.
4. Crear entidades base.
5. Crear repositorios.
6. Crear manejo global de errores.
7. Crear modulo de autenticacion.
8. Crear seguridad con JWT y roles.
9. Crear categorias.
10. Crear productos.
11. Crear pedidos.
12. Crear pagos.
13. Agregar Swagger.
14. Agregar seeds iniciales.
15. Agregar pruebas.
16. Preparar documentacion para frontend.

## 22. Reglas para evitar fallos comunes

- No crear endpoints admin sin seguridad.
- No calcular precios finales en React.
- No guardar contrasenas sin BCrypt.
- No subir credenciales al repositorio.
- No eliminar productos fisicamente si ya tienen pedidos.
- No marcar pedidos como pagados desde el frontend.
- No mezclar entidades JPA directamente como respuesta si contienen relaciones complejas; usar DTOs.
- No usar `ddl-auto=update` en produccion.
- No dejar CORS abierto con `*` en produccion.

