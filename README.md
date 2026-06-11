# Triopi Petshop Backend

API REST para una tienda de mascotas que comercializa cosmeticos, ropa,
juguetes y comida. El backend administra autenticacion, productos, categorias,
pedidos, stock y pagos en linea.

## Funcionalidades

- Inicio de sesion con JWT.
- Autorizacion por roles `ADMIN` y `CLIENT`.
- Contrasenas protegidas con BCrypt.
- Catalogo publico de productos y categorias.
- Administracion de productos con desactivacion logica.
- Administracion de categorias y validacion de nombres duplicados.
- Creacion de pedidos con validacion de stock.
- Calculo del total exclusivamente en el backend.
- Integracion de pagos con Mercado Pago.
- Confirmacion de pagos mediante webhook.
- Documentacion interactiva con Swagger/OpenAPI.
- Datos iniciales configurables.
- CORS configurable para el frontend React.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Lenguaje principal |
| Spring Boot 4.0.6 | Framework de la API |
| Spring Web MVC | Endpoints REST |
| Spring Data JPA | Persistencia |
| Spring Security | Autenticacion y autorizacion |
| JJWT 0.12.6 | Creacion y validacion de JWT |
| PostgreSQL / Supabase | Base de datos |
| Mercado Pago | Pagos en linea |
| Springdoc OpenAPI | Swagger UI |
| Maven Wrapper | Compilacion y pruebas |

## Arquitectura

El proyecto usa una arquitectura por capas:

```text
src/main/java/com/petshop/backend
|-- config       Configuracion, CORS, OpenAPI y datos iniciales
|-- controller   Endpoints REST publicos y administrativos
|-- dto          Objetos de entrada y salida
|-- entity       Entidades y enumeraciones JPA
|-- exception    Manejo centralizado de errores
|-- repository   Acceso a PostgreSQL
|-- security     Filtro, servicio y utilidades JWT
|-- service      Reglas de negocio
`-- util         Utilidades compartidas
```

## Modelo de datos

Las entidades principales son:

- `AppUser`: usuario con rol `ADMIN` o `CLIENT`.
- `Category`: categoria de productos.
- `Product`: producto, precio, stock, imagen y estado.
- `OrderEntity`: pedido del usuario, total y estado.
- `OrderItem`: producto, cantidad, precio unitario y subtotal del pedido.
- `Payment`: preferencia y resultado del pago externo.

Estados de pedido: `PENDING`, `PAID`, `CANCELLED`, `SHIPPED` y `DELIVERED`.

Estados de pago: `PENDING`, `APPROVED`, `REJECTED` y `CANCELLED`.

## Requisitos

- Java 21.
- Una base de datos PostgreSQL, recomendablemente un proyecto de Supabase.
- Una aplicacion de Mercado Pago y su access token para probar pagos.
- Git.

No es necesario instalar Maven porque el repositorio incluye Maven Wrapper.

## Configuracion local

El archivo `application.properties` importa opcionalmente un archivo `.env`
ubicado en la raiz del proyecto. Usa `.env.example` como referencia y crea un
`.env` local con tus valores reales.

```properties
DB_URL=jdbc:postgresql://db.your-project-ref.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_database_password
JWT_SECRET=replace_with_a_long_random_secret
JWT_EXPIRATION=86400000
PAYMENT_CURRENCY=PEN
FRONTEND_URL=http://localhost:5173
PAYMENT_WEBHOOK_URL=https://your-public-api-domain.com/api/payments/webhook
MERCADO_PAGO_ACCESS_TOKEN=your_mercado_pago_access_token
SEED_ENABLED=true
SEED_ADMIN_NAME=Administrador Petshop
SEED_ADMIN_EMAIL=admin@petshop.com
SEED_ADMIN_PASSWORD=replace_with_a_secure_initial_password
CORS_ALLOWED_ORIGINS=http://localhost:5173
JPA_SHOW_SQL=false
JPA_DDL_AUTO=update
SPRING_PROFILES_ACTIVE=dev
```

Variables principales:

| Variable | Descripcion |
| --- | --- |
| `DB_URL` | URL JDBC de PostgreSQL/Supabase |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contrasena de la base de datos |
| `JWT_SECRET` | Secreto largo y aleatorio para firmar tokens |
| `JWT_EXPIRATION` | Duracion del JWT en milisegundos |
| `FRONTEND_URL` | URL usada en los retornos de Mercado Pago |
| `PAYMENT_WEBHOOK_URL` | URL publica que Mercado Pago notificara |
| `MERCADO_PAGO_ACCESS_TOKEN` | Credencial privada de Mercado Pago |
| `SEED_ADMIN_PASSWORD` | Contrasena inicial del administrador |
| `CORS_ALLOWED_ORIGINS` | Origenes del frontend separados por coma |
| `JPA_DDL_AUTO` | Estrategia de Hibernate; `update` solo para desarrollo |

El archivo `.env` esta ignorado por Git. Nunca publiques contrasenas, tokens ni
secretos reales en el repositorio.

## Ejecutar el proyecto

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

La API se inicia por defecto en:

```text
http://localhost:8080
```

## Swagger

Con el backend en ejecucion, la documentacion interactiva esta disponible en:

```text
http://localhost:8080/swagger-ui.html
```

Para probar rutas protegidas:

1. Ejecuta `POST /api/auth/login`.
2. Copia el token devuelto.
3. Pulsa `Authorize` en Swagger.
4. Introduce el token en el esquema Bearer JWT.

Swagger y los documentos OpenAPI estan deshabilitados en el perfil `prod`.

## Autenticacion

Inicio de sesion:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@petshop.com",
  "password": "your_admin_password"
}
```

Las rutas protegidas esperan la cabecera:

```http
Authorization: Bearer <jwt>
```

El endpoint `POST /api/auth/register-admin` permite crear otro administrador,
pero requiere un JWT con rol `ADMIN`.

## Endpoints

### Publicos

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Iniciar sesion |
| `GET` | `/api/products` | Listar productos activos |
| `GET` | `/api/products/{id}` | Consultar un producto |
| `GET` | `/api/categories` | Listar categorias activas |
| `POST` | `/api/payments/webhook` | Recibir notificaciones de Mercado Pago |

### Pedidos y pagos autenticados

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/orders` | Crear un pedido |
| `GET` | `/api/orders/my-orders` | Listar pedidos del usuario |
| `GET` | `/api/orders/{id}` | Consultar un pedido accesible |
| `POST` | `/api/payments/create-preference` | Crear preferencia de pago |
| `GET` | `/api/payments/{id}` | Consultar un pago |

Ejemplo para crear un pedido:

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

Ejemplo para crear la preferencia de pago:

```json
{
  "orderId": 1
}
```

### Administracion

Todas estas rutas requieren el rol `ADMIN`.

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/auth/register-admin` | Crear otro administrador |
| `GET` | `/api/admin/products` | Listar todos los productos |
| `POST` | `/api/admin/products` | Crear un producto |
| `PUT` | `/api/admin/products/{id}` | Actualizar un producto |
| `DELETE` | `/api/admin/products/{id}` | Desactivar un producto |
| `GET` | `/api/admin/categories` | Listar todas las categorias |
| `GET` | `/api/admin/categories/{id}` | Consultar una categoria |
| `POST` | `/api/admin/categories` | Crear una categoria |
| `PUT` | `/api/admin/categories/{id}` | Actualizar una categoria |
| `DELETE` | `/api/admin/categories/{id}` | Eliminar o desactivar una categoria |
| `PUT` | `/api/admin/categories/{id}/deactivate` | Desactivar una categoria |
| `GET` | `/api/admin/orders` | Listar todos los pedidos |
| `GET` | `/api/admin/orders/{id}` | Consultar cualquier pedido |
| `PUT` | `/api/admin/orders/{id}/status` | Actualizar el estado operativo |

Ejemplo para crear o actualizar un producto:

```json
{
  "name": "Shampoo de avena",
  "description": "Producto para piel sensible",
  "price": 24.90,
  "stock": 25,
  "imageUrl": "https://example.com/product.jpg",
  "categoryId": 1,
  "active": true
}
```

## Flujo de pedidos

1. El cliente envia productos y cantidades.
2. El backend consulta precios y stock desde la base de datos.
3. El backend calcula subtotales y total.
4. El pedido se crea con estado `PENDING`.
5. El stock se descuenta cuando el pago es confirmado.

React no debe enviar ni calcular el precio final como fuente de verdad.

## Flujo de pagos

```text
React crea pedido
  -> Backend crea preferencia de Mercado Pago
  -> Cliente completa el checkout
  -> Mercado Pago llama al webhook
  -> Backend valida el pago con Mercado Pago
  -> Pago pasa a APPROVED
  -> Pedido pasa a PAID y se descuenta el stock
```

El frontend no puede marcar pedidos como pagados. El endpoint administrativo de
estado tampoco permite asignar `PAID`; esa transicion pertenece exclusivamente
al flujo confirmado por el proveedor de pagos.

Para desarrollo local, el webhook necesita una URL HTTPS publica que redirija a:

```text
POST /api/payments/webhook
```

## Datos iniciales

Cuando `SEED_ENABLED=true`, el inicio de la aplicacion crea si no existen:

- Categorias: Cosmeticos, Ropa, Juguetes y Comida.
- Un producto de prueba por categoria.
- El usuario administrador, solo si `SEED_ADMIN_PASSWORD` tiene un valor.

Las contrasenas iniciales no estan escritas en el codigo y se almacenan con
BCrypt.

## Pruebas

Ejecutar todas las pruebas:

```powershell
.\mvnw.cmd test
```

La suite cubre, entre otros casos:

- Servicios de productos.
- Calculo de pedidos.
- Stock insuficiente.
- Confirmacion de pago y descuento de stock.
- Inicio de sesion.
- Acceso a endpoints administrativos.
- Reglas basicas de seguridad y CORS.

## Empaquetado

```powershell
.\mvnw.cmd clean package
```

El archivo ejecutable se genera dentro de `target/`.

## Produccion

El perfil de produccion se activa con:

```properties
SPRING_PROFILES_ACTIVE=prod
```

Este perfil:

- Usa `spring.jpa.hibernate.ddl-auto=validate`.
- Desactiva la carga de datos iniciales.
- Desactiva Swagger y OpenAPI.
- Desactiva la impresion de SQL.
- Exige configurar explicitamente `CORS_ALLOWED_ORIGINS`.

Antes de desplegar:

- Aplica migraciones de base de datos de manera controlada.
- Usa secretos distintos a los de desarrollo.
- Configura una URL HTTPS real para el webhook.
- Limita CORS al dominio real del frontend.
- Mantiene `.env` y credenciales fuera de Git.

## Frontend esperado

El backend esta preparado para un frontend React ejecutado durante desarrollo
en:

```text
http://localhost:5173
```

Si React usa otro origen, actualiza `CORS_ALLOWED_ORIGINS` y `FRONTEND_URL`.
