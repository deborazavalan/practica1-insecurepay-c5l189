# Práctica Preparatoria 1 — Parte 1: Teoría Aplicada

## 1.1 Ciclo de vida y gestión de recursos en Flutter

1. **Defecto:** `_BuscadorTransaccionesWidgetState` crea un `TextEditingController`, un `FocusNode` y un listener en `initState()`, pero nunca los libera. Se omitió la sobrescritura de `dispose()`.
2. **Impacto:** cuando el widget sale del Element Tree, el `State` se desmonta pero el controlador, su listener y el `FocusNode` siguen vivos. El listener mantiene una referencia a `widget.onBusqueda` (y por ende al `State`), lo que impide que el recolector de basura libere la memoria. Cada visita a la pantalla acumula objetos huérfanos y listeners activos; el `FocusNode` queda registrado en el `FocusManager`. El resultado es un crecimiento de memoria y callbacks ejecutándose sobre estados desmontados.
3. **Corrección:**

```dart
@override
void dispose() {
  _controlador.dispose();
  _focoTexto.dispose();
  super.dispose();
}
```

## 1.2 Árboles del motor gráfico de Flutter

| Árbol | Responsabilidad | ¿Mutable o inmutable? | Costo de creación |
| :--- | :--- | :--- | :--- |
| Widget Tree | Configuración declarativa de la UI | Inmutable | Muy bajo |
| Element Tree | Vinculación y ciclo de vida (`BuildContext`) | Mutable | Moderado |
| RenderObject Tree | Layout, pintado y hit-test | Mutable | Costoso / pesado |

Los widgets se descartan y recrean en cada `build()`; los elementos se reutilizan comparando `runtimeType` y `key`, y los RenderObjects solo se actualizan cuando cambian sus propiedades, lo que evita recalcular layout y pintado innecesariamente.

## 1.3 OWASP A03:2021 — Inyección SQL

1. **Payload:** `' OR '1'='1` (o `' OR 1=1 --`). La consulta resultante es `... WHERE CONCEPTO = '' OR '1'='1'`, una tautología que retorna todas las filas.
2. **Clasificación:** OWASP Top 10:2021 **A03 – Injection** (CWE-89). Compromete la confidencialidad: un atacante obtiene montos, cuentas, titulares y conceptos de todos los clientes; con `UNION SELECT` puede leer otras tablas (usuarios, contraseñas) y, según los privilegios, modificar o borrar datos.
3. **Mitigación:** con *Prepared Statements* o parámetros nombrados JPQL (`:termino`), la plantilla SQL se envía y compila primero con un marcador de posición; el plan de ejecución queda fijado antes de recibir los datos. El valor del parámetro se vincula después como literal tipado, por lo que comillas, `OR` o `--` nunca se interpretan como gramática SQL.

## 1.4 OWASP Mobile Top 10 — Persistencia insegura

1. **Categoría:** OWASP Mobile Top 10 (2024) **M9 – Insecure Data Storage** (M2 en la edición 2016). También afecta M1 – Improper Credential Usage.
2. **Diagnóstico:** `SharedPreferences` guarda un archivo XML en texto plano en `/data/data/<package_name>/shared_prefs/FlutterSharedPreferences.xml`. No está cifrado: un dispositivo con root lee el archivo directamente y, si la app permite respaldos (`android:allowBackup="true"`), `adb backup` o herramientas forenses extraen el contenido sin root. El JWT permite secuestrar la sesión y el PIN autoriza transferencias.
3. **Alternativa segura:** paquete **`flutter_secure_storage`**, que en Android usa **Android Keystore** + **EncryptedSharedPreferences** (AES-256-GCM) y en iOS el **Keychain**. Además, nunca persistir el PIN: validarlo en el servidor o protegerlo con biometría (`local_auth`).

## 1.5 Optimización de bundles y caché HTTP

1. **Tree-shaking:** análisis estático de los imports ES que elimina del bundle el código y las exportaciones que nunca se usan. **Minificación / uglificación:** elimina espacios, saltos de línea y comentarios y acorta identificadores, reduciendo el tamaño de los archivos JS/CSS en `dist/`.
2. **Cache busting:** el hash del nombre (`main-CMPDLICE.js`) se calcula a partir del contenido. Si el código cambia, cambia el hash y el nombre; el nuevo `index.html` referencia el archivo nuevo, que el navegador nunca ha cacheado y por tanto descarga. Los archivos sin cambios conservan su nombre y se siguen sirviendo desde caché.
3. **Cabeceras:**
   - JS/CSS con hash: `Cache-Control: public, max-age=31536000, immutable`
   - `index.html`: `Cache-Control: no-cache, no-store, must-revalidate`

```nginx
location ~* \.(?:js|css|woff2?|png|jpg|svg|ico)$ {
    add_header Cache-Control "public, max-age=31536000, immutable";
}
location = /index.html {
    add_header Cache-Control "no-cache, no-store, must-revalidate";
}
```
