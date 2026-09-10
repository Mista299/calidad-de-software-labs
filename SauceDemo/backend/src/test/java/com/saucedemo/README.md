# Tests Unitarios — SauceDemo Backend

Documentación de las pruebas unitarias del backend de SauceDemo.

## Integrantes

- **Michael Stiven Tabares Tobón** (autor del setup inicial, interfaces y happy path)

<!-- Agregar aquí los nombres de los compañeros que completan los tests pendientes:

- 
- 
- 

-->

## Stack de pruebas

- **JUnit 5** (`jupiter`) — motor de tests
- **Mockito** — mocks y spies
- **AssertJ** — aserciones fluidas (`assertThat`)
- Sin Spring Context (tests unitarios puros, arrancan en milisegundos)

## Estructura

```
src/test/java/com/saucedemo/
├── service/
│   ├── ProductServiceTest.java       (2 tests)
│   └── CartServiceTest.java          (4 tests)
└── controller/
    ├── ProductControllerTest.java    (2 tests)
    └── CartControllerTest.java       (4 tests)
```

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

Resultado esperado: **12 tests pasan, 0 fallan**, en menos de 5 segundos, sin levantar Spring ni conectar a la BD.

## Convención de los tests

- **AAA (Arrange, Act, Assert)** en cada test
- Nombres descriptivos con `@DisplayName`
- Solo se testean métodos públicos
- Cada test es independiente (sin orden)

---

## ✅ Lo que está hecho (Happy Path)

Estos 12 tests cubren los flujos donde **todo funciona como se espera**:

### `ProductServiceTest` (2)

| Test | Qué verifica |
|---|---|
| `getAllProducts_debeRetornarProductosDelRepositorio` | El service devuelve la lista completa del repository |
| `getProductById_debeRetornarProductoCuandoExiste` | El service devuelve `Optional<Product>` con el producto encontrado |

### `CartServiceTest` (4)

| Test | Qué verifica |
|---|---|
| `getCart_deveRetornarItemsDeLaSesion` | Devuelve los items del sessionId solicitado |
| `addToCart_creaNuevoItemCuandoProductoExisteYNoHayItemPrevio` | Crea un `CartItem` nuevo cuando el producto existe y no había item previo en el carrito |
| `updateQuantity_actualizaCantidadCuandoItemExiste` | Actualiza la cantidad del item existente |
| `removeItem_llamaDeleteByIdDelRepositorio` | Llama a `deleteById` del repository |

### `ProductControllerTest` (2)

| Test | Qué verifica |
|---|---|
| `getAllProducts_retornaListaDelService` | Devuelve la lista retornada por el service |
| `getProductById_retorna200ConProductoCuandoExiste` | Retorna `ResponseEntity.ok` con el producto |

### `CartControllerTest` (4)

| Test | Qué verifica |
|---|---|
| `getCart_retornaItemsDelService` | Devuelve la lista retornada por el service |
| `addToCart_retorna200ConItemCuandoExitoso` | Retorna `ResponseEntity.ok` con el item agregado |
| `updateQuantity_retorna200ConItemCuandoExitoso` | Retorna `ResponseEntity.ok` con el item actualizado |
| `removeItem_retorna204` | Retorna `ResponseEntity.noContent()` |

---

## 🚧 Lo que falta por hacer (para los compañeros)

Quedan **11 escenarios pendientes** que complementan la cobertura. Distribúyanse libremente:

### `ProductServiceTest` — faltan 2

| # | Test sugerido | Qué cubre |
|---|---|---|
| 1 | `getAllProducts_debeRetornarListaVaciaCuandoNoHayProductos` | Borde: lista vacía |
| 2 | `getProductById_debeRetornarVacioCuandoNoExiste` | Borde: `Optional.empty()` |

### `CartServiceTest` — faltan 5

| # | Test sugerido | Qué cubre |
|---|---|---|
| 3 | `getCart_debeRetornarListaVaciaCuandoSesionNoTieneItems` | Borde: lista vacía |
| 4 | `addToCart_incrementaCantidadCuandoItemYaExisteEnCarrito` | Rama: cuando ya existe el item, suma cantidades en lugar de crear uno nuevo |
| 5 | `addToCart_lanzaExcepcionCuandoProductoNoExiste` | Excepción: `NoSuchElementException` cuando el `productId` no existe |
| 6 | `updateQuantity_lanzaExcepcionCuandoItemNoExiste` | Excepción: `NoSuchElementException` cuando el `itemId` no existe |
| 7 | `removeItem_propagaExcepcionSiRepositorioFalla` | Excepción: propaga fallos del repository |

### `ProductControllerTest` — faltan 2

| # | Test sugerido | Qué cubre |
|---|---|---|
| 8 | `getAllProducts_retornaListaVacia` | Borde: lista vacía |
| 9 | `getProductById_retorna404CuandoNoExiste` | Status 404 cuando service retorna `Optional.empty()` |

### `CartControllerTest` — faltan 2

| # | Test sugerido | Qué cubre |
|---|---|---|
| 10 | `addToCart_retorna404CuandoServiceLanzaExcepcion` | Status 404 cuando service lanza `NoSuchElementException` (producto no existe) |
| 11 | `updateQuantity_retorna404CuandoServiceLanzaExcepcion` | Status 404 cuando service lanza `NoSuchElementException` (item no existe) |

---

## 🧪 Pistas para los compañeros

### Para testear excepciones

```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

when(productRepository.findById(99L)).thenReturn(Optional.empty());

assertThatThrownBy(() -> cartService.addToCart("session-1", 99L, 1))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("99");

verify(cartItemRepository, never()).save(any(CartItem.class));
```

### Para testear un 404 en el Controller

```java
when(cartService.addToCart("session-1", 99L, 1))
        .thenThrow(new NoSuchElementException("Producto no encontrado: 99"));

ResponseEntity<CartItem> respuesta = cartController.addToCart(request);

assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
assertThat(respuesta.getBody()).isNull();
```

### Para usar captores (verificar lo que se guardó)

```java
@Captor
private ArgumentCaptor<CartItem> captor;

when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

cartService.addToCart("session-1", 1L, 3);

verify(cartItemRepository).save(captor.capture());
assertThat(captor.getValue().getQuantity()).isEqualTo(5); // 2 previos + 3 nuevos
```

---

## 📊 Cobertura final esperada

Al completar los 11 tests pendientes, la cobertura debería ser:

- **100%** de las líneas en `ProductService` y `ProductController`
- **100%** de las ramas en `CartService` y `CartController` (incluyendo el `orElseThrow`, `map.orElseGet`, etc.)

Verificar con:

```bash
mvn test
```

(Todos los tests deben pasar.)

```bash
mvn test -Djacoco.skip=false
```

(Para ver el reporte visual de cobertura con JaCoCo si está configurado.)
