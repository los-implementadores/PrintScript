# ADR-009: Aceptar la limitación de OCP del Visitor (expression problem)

- **Estado:** Aceptado
- **Fecha:** 2026-09-01
- **Relacionado:** ADR-002 (Visitor para recorrer el AST)

## Contexto

El AST se recorre con el patrón Visitor (ADR-002): `ASTVisitor<T>` con un método
`visitX` por tipo de nodo, y `accept(visitor)` en cada nodo. Al hacer el parser
Open/Closed (ADR-008), quedó expuesta una asimetría en el AST: el Visitor **no es
Open/Closed para agregar tipos de nodo nuevos**.

Esto es el **expression problem**: en un diseño OO clásico no se puede tener OCP
simultáneamente en los dos ejes de evolución:

- **Agregar operaciones** (una fase nueva: type-checker, optimizer, pretty-printer).
- **Agregar tipos de dato** (un nodo nuevo del AST: `IfStatement`, `WhileStatement`).

El Visitor optimiza el primer eje a costa del segundo.

## Análisis

Con el Visitor actual:

- Agregar una **operación** (un visitor nuevo) → se crea una clase que implementa
  `ASTVisitor`. **No se toca ningún nodo.** ✅ OCP cumplido.
- Agregar un **nodo** (`IfStatement`) → hay que agregar `visitIf(...)` a `ASTVisitor`,
  lo que **obliga a modificar todos los visitors** existentes (interpreter, formatter,
  analyzer). ❌ OCP violado.

Alternativas consideradas para el segundo eje:

1. **Métodos en los nodos** (`node.interpret()`, `node.format()`): agregar un nodo es
   fácil, pero agregar una operación toca todos los nodos y mezcla responsabilidades
   (viola SRP). Es el trade-off inverso, ya descartado en ADR-002.
2. **`sealed` interfaces + `switch` con patrones (Java 17)**: cada operación es un
   `switch` sobre el tipo sellado. Agregar un nodo rompe los `switch` en
   **compile-time** (el compilador exige cubrir el caso nuevo), lo cual es más seguro
   que el Visitor pero pierde el double-dispatch y dispersa la lógica en switches.
3. **Mantener el Visitor** y asumir el costo.

## Decisión

Se mantiene el patrón Visitor (ADR-002) y se **acepta explícitamente** que agregar un
tipo de nodo nuevo requiere modificar `ASTVisitor` y sus implementaciones.

Fundamento: en un compilador, **las operaciones/fases crecen más y más seguido que los
tipos de nodo**. El set de nodos de un lenguaje se estabiliza pronto; las fases
(validación, ejecución, formateo, análisis, futuras optimizaciones) se agregan durante
toda la vida del proyecto. El Visitor da OCP justo en el eje que más se mueve.

Mitigación del costo del eje débil: cuando se agrega un nodo, el compilador de Java
señala en compile-time cada visitor que quedó incompleto (no hay fallos silenciosos en
runtime). El "dolor" es acotado y verificado por el tipado.

## Consecuencias

- El AST sigue recorriéndose con `ASTVisitor<T>`; ninguna fase existente se rediseña.
- Agregar una fase nueva no toca los nodos (beneficio principal, se conserva).
- Agregar un nodo nuevo tocará `ASTVisitor` + interpreter + formatter + analyzer. Es
  una modificación conocida, guiada por errores de compilación, no un defecto oculto.
- Queda documentado que esta violación de OCP es **deliberada y localizada**, no un
  descuido: es el precio elegido del expression problem.
