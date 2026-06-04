# Guia de implementacion para el control de la codificacion del sistema

## Titulo del sistema

**Gestor de Ventas Streaming**

## Objetivo general

Desarrollar un sistema web para gestionar la venta de servicios de streaming, permitiendo registrar clientes, proveedores, servicios, cuentas, perfiles, ventas, renovaciones, reportes de vencimiento, gastos e ingresos. El sistema centraliza la informacion operativa del negocio y usa autenticacion para separar la informacion por usuario.

---

## 1. Arquitectura del sistema y patron de diseno

### Patron de arquitectura

El sistema aplica una arquitectura **cliente-servidor con frontend SPA y persistencia en Supabase**. La interfaz esta desarrollada como una aplicacion de pagina unica en React, mientras que Supabase cumple el rol de backend administrado para autenticacion, base de datos PostgreSQL y API de acceso a datos.

El repositorio tambien incluye un backend Spring Boot minimo, ubicado en `backend/`, pero el flujo funcional actual del sistema se concentra en `frontend/` y en los scripts SQL de Supabase.

### Flujo de capas

1. **Capa de presentacion:** componentes y paginas React.
2. **Capa de estado y rutas:** React Router, contexto de autenticacion y hooks.
3. **Capa de acceso a datos:** cliente de Supabase centralizado en `frontend/src/lib/supabaseClient.js`.
4. **Capa de persistencia:** base de datos PostgreSQL gestionada por Supabase.

Regla de implementacion: ninguna pagina debe crear conexiones directas nuevas a la base de datos. Todo acceso debe hacerse mediante el cliente compartido `supabase`, configurado con variables de entorno.

### Estructura de archivos del proyecto

```plaintext
sistema streaming/
+-- backend/
|   +-- src/main/java/com/gestorventas/backend/
|       +-- BackendApplication.java
+-- frontend/
|   +-- public/
|   +-- src/
|       +-- components/
|       |   +-- auth/
|       |   +-- common/
|       |   +-- dashboard/
|       |   +-- layout/
|       +-- context/
|       +-- hooks/
|       +-- layout/
|       +-- lib/
|       +-- pages/
|       +-- App.jsx
|       +-- main.jsx
+-- crear_tablas.sql
+-- migracion_gastos_cuentas.sql
+-- migracion_pagos_ventas.sql
+-- migracion_sistema_codigos.sql
+-- pom.xml
+-- README_FUNCIONALIDADES.md
```

### Lenguaje de programacion a usar y justificacion

| Capa | Lenguaje / tecnologia | Justificacion |
| --- | --- | --- |
| Frontend | JavaScript con React | Permite construir una interfaz SPA modular, reactiva y facil de mantener. |
| Estilos | TailwindCSS | Facilita estilos consistentes y rapidos por medio de clases utilitarias. |
| Persistencia | SQL sobre PostgreSQL | Permite integridad referencial, consultas relacionales, indices y politicas RLS. |
| Backend incluido | Java 17 con Spring Boot | Base disponible para evolucionar el sistema hacia servicios propios si se requiere. |

### Tipo de base de datos y motor

La base de datos es **relacional**, implementada en **PostgreSQL** mediante Supabase. PostgreSQL ofrece llaves foraneas, tipos `uuid`, `numeric`, `timestamptz`, indices y seguridad por filas mediante RLS.

### Nombre de la base de datos

Nombre funcional recomendado: **gestor_ventas_streaming**.

En Supabase el nombre fisico puede depender del proyecto creado, pero para documentacion, scripts y convenciones internas se usara `gestor_ventas_streaming`.

---

## 2. Estandares de base de datos (persistencia)

### Convenciones de nomenclatura

| Elemento | Convencion | Ejemplo aplicado |
| --- | --- | --- |
| Tablas | Plural, minusculas y `snake_case` | `clientes`, `cuentas_servicios`, `pagos_ventas` |
| Clave primaria | Campo fijo `id` | `id uuid primary key` |
| Claves foraneas | Nombre de tabla padre en singular + `_id` | `cliente_id`, `servicio_id`, `cuenta_servicio_id` |
| Campos de fecha | Prefijo `fecha_` o auditoria `creado_en` | `fecha_venta`, `fecha_vencimiento`, `creado_en` |
| Campos monetarios | `numeric(10,2)` o `decimal(10,2)` | `monto`, `precio`, `precio_compra` |
| Campos de estado | Booleanos o texto controlado | `liberada`, `tipo` |
| Indices | Prefijo `idx_` + tabla + campo | `idx_ventas_cliente_id` |
| Restricciones unicas | Prefijo `unique_` + descripcion | `unique_correo_servicio` |

### Tabla de nomenclatura de la base de datos

| Tabla | Proposito | Campos principales |
| --- | --- | --- |
| `clientes` | Registra clientes del usuario autenticado. | `id`, `user_id`, `nombre`, `apellido`, `telefono`, `correo`, `clave_acceso`, `creado_en` |
| `proveedores` | Registra proveedores de cuentas. | `id`, `user_id`, `usuario`, `telefono`, `correo`, `creado_en` |
| `servicios` | Registra plataformas de streaming. | `id`, `user_id`, `nombre`, `service_key`, `creado_en` |
| `cuentas_servicios` | Registra cuentas compradas por servicio y proveedor. | `id`, `servicio_id`, `proveedor_id`, `correo`, `contrasena`, `precio`, `fecha_vencimiento`, `creado_en` |
| `ventas` | Registra ventas o alquileres de cuentas/perfiles. | `id`, `user_id`, `cliente_id`, `cuenta_servicio_id`, `perfil_id`, `fecha_venta`, `fecha_inicio`, `fecha_vencimiento`, `monto`, `liberada` |
| `pagos_ventas` | Historial de ingresos por ventas y renovaciones. | `id`, `user_id`, `venta_id`, `monto`, `fecha_pago`, `tipo`, `creado_en` |
| `gastos_cuentas` | Historial de gastos por compra o renovacion de cuentas. | `id`, `user_id`, `cuenta_servicio_id`, `monto`, `fecha_gasto`, `tipo`, `creado_en` |
| `mensajes_cuentas` | Mensajes/codigos asociados a cuentas de streaming. | `id`, `cuenta_servicio_id`, `service_key`, `mailbox`, `subject`, `body`, `received_at` |

### Ejemplo practico de 4 tablas

```sql
CREATE TABLE IF NOT EXISTS clientes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  telefono VARCHAR(20),
  correo VARCHAR(255),
  clave_acceso TEXT,
  creado_en TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS servicios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID,
  nombre VARCHAR(100) NOT NULL,
  service_key TEXT,
  creado_en TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cuentas_servicios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID,
  servicio_id UUID NOT NULL REFERENCES servicios(id) ON DELETE CASCADE,
  proveedor_id UUID NOT NULL REFERENCES proveedores(id) ON DELETE RESTRICT,
  correo VARCHAR(255) NOT NULL,
  contrasena VARCHAR(255) NOT NULL,
  precio DECIMAL(10, 2) NOT NULL,
  fecha_vencimiento DATE,
  creado_en TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  CONSTRAINT unique_correo_servicio UNIQUE (servicio_id, correo)
);

CREATE TABLE IF NOT EXISTS ventas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
  cuenta_servicio_id UUID REFERENCES cuentas_servicios(id) ON DELETE RESTRICT,
  perfil_id UUID,
  fecha_venta TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  fecha_inicio DATE,
  fecha_vencimiento DATE,
  monto DECIMAL(10, 2) NOT NULL,
  liberada BOOLEAN DEFAULT FALSE,
  creado_en TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## 3. Estandares de codificacion (Clean Code)

### Reglas de nomenclatura

| Elemento | Convencion | Ejemplo del sistema |
| --- | --- | --- |
| Componentes React | `PascalCase` | `ProtectedRoute`, `MainLayout`, `Clientes` |
| Hooks | `camelCase` con prefijo `use` | `useAuth`, `useCurrency`, `useCurrentPage` |
| Funciones | `camelCase`, iniciar con verbo | `handleSubmit`, `fetchClientes`, `openEditModal` |
| Variables de estado | `camelCase` descriptivo | `openNewCliente`, `formError`, `fechaVencimientoVenta` |
| Constantes | `UPPER_SNAKE_CASE` si son globales | `VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY` |
| Tablas/campos BD | `snake_case` | `cuentas_servicios`, `fecha_vencimiento` |

Nota: el PDF menciona `strict_types=1`, propio de PHP. En este sistema no aplica porque el frontend esta hecho en JavaScript/React. Como equivalente recomendado, se deben usar validaciones explicitas, JSDoc cuando una funcion reciba estructuras complejas y ESLint para mantener consistencia.

### Comentarios estructurados

Los comentarios deben explicar decisiones de negocio o reglas no obvias. No deben repetir lo evidente.

Ejemplo recomendado con JSDoc:

```javascript
/**
 * Calcula el estado visual de una venta segun su fecha de vencimiento.
 *
 * @param {string | Date | null} fechaVencimiento Fecha en formato ISO o valor compatible.
 * @returns {{ label: string, color: string }} Etiqueta de estado y clases visuales.
 */
function getEstadoVenta(fechaVencimiento) {
  const fechaNorm = normalizeDateString(fechaVencimiento)
  if (!fechaNorm) {
    return { label: 'Sin fecha', color: 'text-slate-600 bg-slate-100 border-slate-200' }
  }

  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)

  const [y, m, d] = fechaNorm.split('-')
  const vencimiento = new Date(Number(y), Number(m) - 1, Number(d))
  vencimiento.setHours(0, 0, 0, 0)

  const diffDays = Math.ceil((vencimiento - hoy) / (1000 * 60 * 60 * 24))

  if (diffDays < 0) return { label: 'Vencida', color: 'text-rose-700 bg-rose-100 border-rose-300' }
  if (diffDays === 0) return { label: 'Vencido', color: 'text-rose-700 bg-rose-100 border-rose-300' }
  if (diffDays <= 2) return { label: 'Por Vencer', color: 'text-amber-700 bg-amber-100 border-amber-300' }
  return { label: 'Vigente', color: 'text-emerald-700 bg-emerald-100 border-emerald-300' }
}
```

### Flujo de trabajo de los modulos

Para agregar o modificar un modulo se debe seguir este orden:

1. **Diseno de datos:** crear o ajustar tablas en scripts SQL, respetando `snake_case`, llaves foraneas, indices y campos de auditoria.
2. **Acceso a datos:** usar `supabase` desde `frontend/src/lib/supabaseClient.js`.
3. **Logica de modulo:** crear funciones claras para carga, validacion, guardado, actualizacion y eliminacion.
4. **Interfaz:** crear o modificar pagina en `frontend/src/pages/` y componentes reutilizables en `frontend/src/components/`.
5. **Rutas y proteccion:** registrar rutas en `App.jsx` y proteger pantallas con `ProtectedRoute` si requieren sesion.
6. **Validacion manual:** probar creacion, listado, edicion, eliminacion y escenarios de error.

### Tabla de nomenclatura de codigo

| Tipo | Ubicacion | Ejemplo |
| --- | --- | --- |
| Pagina principal de modulo | `frontend/src/pages/` | `Clientes.jsx`, `Ventas.jsx`, `Reportes.jsx` |
| Componente comun | `frontend/src/components/common/` | `Modal.jsx`, `ConfirmModal.jsx`, `FilterableSelect.jsx` |
| Componente de layout | `frontend/src/components/layout/` | `Sidebar.jsx`, `TopBar.jsx` |
| Contexto global | `frontend/src/context/` | `AuthContext.jsx` |
| Funciones utilitarias | `frontend/src/lib/` | `money.js`, `dateUtils.js`, `whatsapp.js` |
| Hooks | `frontend/src/hooks/` | `useCurrency.jsx`, `useCurrentPage.jsx` |

### Ejemplo practico de codigo integrado

```javascript
import { useState } from 'react'
import { supabase } from '../lib/supabaseClient'
import { useAuth } from '../context/AuthContext'
import { capitalize } from '../lib/textUtils'

/**
 * Registra un cliente validando campos obligatorios y asociandolo al usuario actual.
 *
 * @param {object} values Datos capturados desde el formulario.
 * @param {string} values.nombre Nombre del cliente.
 * @param {string} values.apellido Apellido del cliente.
 * @param {string} values.telefono Telefono opcional.
 * @returns {Promise<object>} Cliente creado en Supabase.
 */
export async function crearCliente(values) {
  const { user } = useAuth()
  const nombre = values.nombre?.trim()
  const apellido = values.apellido?.trim()

  if (!nombre || !apellido) {
    throw new Error('Nombre y apellido son obligatorios')
  }

  const { data, error } = await supabase
    .from('clientes')
    .insert({
      user_id: user.id,
      nombre: capitalize(nombre),
      apellido: capitalize(apellido),
      telefono: values.telefono?.trim() || null,
    })
    .select('id, nombre, apellido, telefono, creado_en')
    .single()

  if (error) {
    throw new Error('No se pudo registrar el cliente')
  }

  return data
}
```

Observacion tecnica: el ejemplo muestra la regla de limpieza y nombres, pero en React los hooks como `useAuth()` deben usarse dentro de componentes u otros hooks. En implementacion real del proyecto, la funcion de guardado vive dentro del componente `Clientes`, donde el hook ya esta disponible.

---

## 4. Seguridad del sistema y control de vulnerabilidades

### Inyeccion SQL

El frontend no concatena SQL manualmente. Todas las operaciones se realizan mediante el cliente de Supabase:

```javascript
const { data, error } = await supabase
  .from('clientes')
  .select('id, nombre, apellido, telefono')
  .eq('user_id', user.id)
```

Reglas obligatorias:

- No construir consultas SQL concatenando texto ingresado por el usuario.
- Usar los metodos del cliente Supabase: `.select()`, `.insert()`, `.update()`, `.delete()`, `.eq()`, `.lt()`, `.order()`.
- Si se agregan funciones RPC o SQL manual, deben usar parametros y no interpolacion directa.
- Toda tabla con datos por usuario debe incluir `user_id` y politicas RLS.

### Cross-Site Scripting (XSS)

React escapa por defecto los valores renderizados dentro de JSX, por lo que se debe evitar `dangerouslySetInnerHTML`.

Reglas obligatorias:

- No usar `dangerouslySetInnerHTML` salvo necesidad documentada y sanitizada.
- Validar entradas antes de guardarlas.
- Mostrar mensajes controlados en la interfaz.
- No renderizar HTML recibido desde campos como `body`, `subject`, `nombre`, `correo` o `contrasena` sin sanitizacion.

### Hashing seguro de contrasenas

La autenticacion de usuarios se maneja con **Supabase Auth**, que internamente gestiona credenciales de acceso del usuario.

Riesgo actual detectado: las contrasenas de cuentas de streaming (`cuentas_servicios.contrasena`) y la clave de acceso del cliente (`clientes.clave_acceso`) se almacenan en texto plano. Esto aparece incluso indicado en el script `migracion_sistema_codigos.sql`.

Politica obligatoria:

- Las contrasenas de usuarios del sistema deben gestionarse exclusivamente por Supabase Auth.
- Las claves de acceso de clientes deben migrarse a hash seguro, preferentemente Argon2id o Bcrypt mediante una funcion backend o Edge Function.
- Las contrasenas de cuentas de streaming son datos operativos que el vendedor necesita consultar; si deben permanecer recuperables, se recomienda cifrado reversible del lado servidor, no texto plano.
- No registrar contrasenas en consola, logs ni mensajes de error.

### Control de acceso basado en roles (RBAC)

El sistema ya usa rutas protegidas:

```javascript
if (!user) {
  return <Navigate to="/login" replace state={{ from: location }} />
}
```

Politicas obligatorias:

- Toda pantalla interna debe estar envuelta por `ProtectedRoute`.
- Toda consulta de datos operativos debe filtrar por `user_id = user.id`.
- En Supabase se deben activar politicas RLS por tabla:

```sql
ALTER TABLE public.ventas ENABLE ROW LEVEL SECURITY;

CREATE POLICY ventas_select_own
ON public.ventas
FOR SELECT
TO authenticated
USING (user_id = auth.uid());
```

Roles sugeridos:

| Rol | Permisos |
| --- | --- |
| `admin` | Acceso total a configuracion, usuarios, reportes y mantenimiento. |
| `vendedor` | Gestion de clientes, ventas, renovaciones y reportes operativos. |
| `consulta` | Solo lectura de dashboard y reportes. |

---

## 5. Manejo de errores, excepciones y logs

### Estandar de captura de errores

El sistema debe capturar errores de Supabase y mostrar mensajes comprensibles al usuario, evitando exponer informacion tecnica sensible.

Reglas obligatorias:

- Usar `try-catch` cuando una accion tenga pasos encadenados.
- Revisar siempre `error` devuelto por Supabase.
- En produccion, no mostrar mensajes internos de PostgreSQL ni detalles de politicas RLS.
- Registrar detalles tecnicos en consola solo durante desarrollo.
- Usar mensajes de interfaz como: "No se pudo guardar el registro. Intenta nuevamente."

### Ejemplo de manejo correcto

```javascript
async function handleSubmit(e) {
  e.preventDefault()
  setFormError(null)

  if (!nombre.trim() || !apellido.trim()) {
    setFormError('Nombre y apellido son obligatorios')
    return
  }

  try {
    const { data, error } = await supabase
      .from('clientes')
      .insert({
        user_id: user.id,
        nombre: capitalize(nombre.trim()),
        apellido: capitalize(apellido.trim()),
      })
      .select('id, nombre, apellido, creado_en')
      .single()

    if (error) {
      console.error('Error al crear cliente:', error)
      setFormError('No se pudo registrar el cliente.')
      return
    }

    setClientes((prev) => [data, ...prev])
  } catch (err) {
    console.error('Excepcion inesperada en handleSubmit:', err)
    setFormError('Ocurrio un error inesperado. Intenta nuevamente.')
  }
}
```

### Politica de logs

| Entorno | Politica |
| --- | --- |
| Desarrollo | Se permite `console.warn` y `console.error` para diagnostico. |
| Pruebas | Los logs deben facilitar reproduccion del error sin datos sensibles. |
| Produccion | Los errores tecnicos deben enviarse a logs del servidor/servicio externo y la UI debe mostrar mensajes controlados. |

Datos que no deben registrarse:

- Contrasenas de cuentas.
- Claves de acceso de clientes.
- Tokens de Supabase.
- Variables `VITE_SUPABASE_ANON_KEY` o credenciales privadas.

---

## 6. Flujo de trabajo en el control de versiones (Git)

### Ramas principales

| Rama | Uso |
| --- | --- |
| `main` | Rama estable del proyecto. Debe contener codigo funcional. |
| `feature/*` | Nuevas funcionalidades. |
| `bugfix/*` | Correccion de errores. |
| `hotfix/*` | Correcciones urgentes sobre produccion. |
| `docs/*` | Cambios de documentacion. |
| `chore/*` | Tareas tecnicas sin cambio funcional directo. |

Ejemplos:

```bash
git checkout -b feature/gestion-renovaciones
git checkout -b bugfix/filtro-ventas-vencidas
git checkout -b docs/guia-codificacion
```

### Mensajes de confirmacion semanticos

Formato:

```plaintext
tipo(modulo): descripcion breve en imperativo
```

Tipos permitidos:

| Tipo | Uso |
| --- | --- |
| `feat` | Nueva funcionalidad. |
| `fix` | Correccion de error. |
| `docs` | Documentacion. |
| `style` | Cambios visuales o formato sin cambiar logica. |
| `refactor` | Reorganizacion interna sin cambio funcional. |
| `test` | Pruebas. |
| `chore` | Mantenimiento, configuracion o dependencias. |

Ejemplos:

```bash
git commit -m "feat(ventas): agregar renovacion de cuentas vencidas"
git commit -m "fix(clientes): validar telefono antes de guardar"
git commit -m "docs(guia): documentar estandares hasta flujo git"
```

### Pull Requests y Code Review

Toda rama debe integrarse mediante Pull Request.

Checklist minimo antes de solicitar revision:

- El codigo compila o la aplicacion inicia correctamente.
- No se incluyen credenciales reales ni archivos `.env`.
- Las consultas nuevas filtran por `user_id` cuando corresponde.
- Las tablas nuevas usan `snake_case`, `id`, `creado_en` y llaves foraneas.
- La interfaz muestra errores controlados.
- El cambio fue probado manualmente en el flujo afectado.

### Flujo recomendado

```bash
git checkout main
git pull origin main
git checkout -b feature/nombre-del-cambio

# editar archivos
git status
git add archivo1 archivo2
git commit -m "feat(modulo): describir cambio"
git push origin feature/nombre-del-cambio
```

Luego se abre un Pull Request hacia `main`, se solicita revision por pares y se corrigen observaciones antes de fusionar.

### Reglas de control de version para este proyecto

- No trabajar cambios grandes directamente en `main`.
- No subir `frontend/.env`.
- No subir archivos generados innecesarios si pueden regenerarse.
- No mezclar cambios de base de datos, interfaz y documentacion en commits sin relacion.
- Cada migracion SQL debe tener nombre descriptivo y ser idempotente cuando sea posible.
- Antes de fusionar, ejecutar al menos:

```bash
cd frontend
npm run lint
npm run build
```

Si se modifica el backend:

```bash
mvnw.cmd test
```
