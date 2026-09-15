# Tests Unitarios — SauceDemo Backend

Documentación de las pruebas unitarias del backend de SauceDemo.

## Integrantes

- **Michael Stiven Tabares Tobón**
- **Adrian Espinosa Montoya**
- **Jose Manuel Bernal Aguilar**

## Stack de pruebas

- **JUnit 5** (`jupiter`) — motor de tests
- **Mockito** — mocks y spies
- **AssertJ** — aserciones fluidas (`assertThat`)
- Sin Spring Context (tests unitarios puros, arrancan en milisegundos)

## Estructura

```
src/test/java/com/saucedemo/
├── service/
│   ├── ProductServiceTest.java       (4 tests)
│   └── CartServiceTest.java          (9 tests)
└── controller/
    ├── ProductControllerTest.java    (4 tests)
    └── CartControllerTest.java       (6 tests)
```

**Total: 23 tests unitarios.**

## Cambios al código de producción

Para poder mockear las dependencias de los Controllers, fue necesario aplicar el **principio de Inversión de Dependencias (DIP)**:

1. **Nuevas interfaces de service** (contratos que el Controller consume):
   - `service/ProductServiceInterface.java`
   - `service/CartServiceInterface.java`

2. **Services implementan las interfaces**:
   - `service/ProductService.java` ahora `implements ProductServiceInterface` con `@Override`
   - `service/CartService.java` ahora `implements CartServiceInterface` con `@Override`

3. **Controllers dependen de las interfaces** (en lugar de las clases concretas):
   - `controller/ProductController.java` — campo y parámetro del constructor son `ProductServiceInterface`
   - `controller/CartController.java` — campo y parámetro del constructor son `CartServiceInterface`

Spring Boot resuelve la inyección en tiempo de arranque: encuentra `ProductService` como implementación de `ProductServiceInterface` y la inyecta automáticamente.

## Cómo ejecutar los tests

```bash
mvn test
```

Resultado esperado: **23 tests pasan, 0 fallan**, sin levantar Spring ni conectar a la BD.

Para ver el reporte de cobertura con JaCoCo:

```bash
mvn test -Djacoco.skip=false
```

## Convención de los tests

- **AAA (Arrange, Act, Assert)** en cada test
- Nombres descriptivos con `@DisplayName`
- Solo se testean métodos públicos
- Cada test es independiente

---

## Tests implementados

### `ProductServiceTest` (4 tests)

| Test | Qué verifica |
|---|---|
| `getAllProducts_debeRetornarProductosDelRepositorio` | El service devuelve la lista completa del repository |
| `getAllProducts_debeRetornarListaVaciaCuandoNoHayProductos` | Borde: el service devuelve lista vacía cuando el repository no tiene productos |
| `getProductById_debeRetornarProductoCuandoExiste` | El service devuelve `Optional<Product>` con el producto encontrado |
| `getProductById_debeRetornarVacioCuandoNoExiste` | Borde: el service devuelve `Optional.empty()` cuando el id no existe |

### `CartServiceTest` (9 tests)

| Test | Qué verifica |
|---|---|
| `getCart_deveRetornarItemsDeLaSesion` | Devuelve los items del `sessionId` solicitado desde el repository |
| `addToCart_creaNuevoItemCuandoProductoExisteYNoHayItemPrevio` | Crea un `CartItem` nuevo cuando el producto existe y no había item previo en el carrito |
| `updateQuantity_actualizaCantidadCuandoItemExiste` | Actualiza la cantidad del item existente y lo persiste con `save` |
| `removeItem_llamaDeleteByIdDelRepositorio` | Llama a `deleteById` del repository con el id indicado |
| `getCart_debeRetornarListaVaciaCuandoSesionNoTieneItems` | Borde: el service devuelve lista vacía cuando la sesión no tiene items |
| `addToCart_incrementaCantidadCuandoItemYaExisteEnCarrito` | Suma la cantidad nueva a la existente cuando ya hay un item para ese producto en la sesión |
| `addToCart_lanzaExcepcionCuandoProductoNoExiste` | Excepción: lanza `NoSuchElementException` cuando el `productId` no existe |
| `updateQuantity_lanzaExcepcionCuandoItemNoExiste` | Excepción: lanza `NoSuchElementException` cuando el `itemId` no existe |
| `removeItem_propagaExcepcionSiRepositorioFalla` | Excepción: propaga el fallo del repository al llamar `removeItem` |

### `ProductControllerTest` (4 tests)

| Test | Qué verifica |
|---|---|
| `getAllProducts_retornaListaDelService` | Devuelve la lista retornada por el service |
| `getProductById_retorna200ConProductoCuandoExiste` | Retorna `ResponseEntity.ok` con el producto en el body |
| `getAllProducts_retornaListaVacia` | Borde: devuelve lista vacía cuando el service no retorna productos |
| `getProductById_retorna404CuandoNoExiste` | Retorna `ResponseEntity.notFound()` cuando el service devuelve `Optional.empty()` |

### `CartControllerTest` (6 tests)

| Test | Qué verifica |
|---|---|
| `getCart_retornaItemsDelService` | Devuelve la lista retornada por el service |
| `addToCart_retorna200ConItemCuandoExitoso` | Retorna `ResponseEntity.ok` con el item agregado en el body |
| `updateQuantity_retorna200ConItemCuandoExitoso` | Retorna `ResponseEntity.ok` con el item actualizado en el body |
| `removeItem_retorna204` | Retorna `ResponseEntity.noContent()` sin body |
| `addToCart_retorna404CuandoServiceLanzaExcepcion` | Retorna `ResponseEntity.notFound()` cuando el service lanza `NoSuchElementException` (producto no existe) |
| `updateQuantity_retorna404CuandoServiceLanzaExcepcion` | Retorna `ResponseEntity.notFound()` cuando el service lanza `NoSuchElementException` (item no existe) |

---

## Cobertura

Con los 23 tests implementados se obtiene **cobertura al 100%** de las líneas y ramas en:

- `ProductService` y `ProductController`
- `CartService` y `CartController` (incluyendo `orElseThrow`, `map.orElseGet`, etc.)
