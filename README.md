# Práctica Preparatoria 1 — InsecurePay

IF0009 Desarrollo de Software IV · UCR Sede del Atlántico · Débora Zavala (C5L189)

Arquitectura reactiva, mitigación de inyección SQL (OWASP A03:2021) y paginación segura.

| Parte | Ubicación |
| :--- | :--- |
| Parte 1 — Teoría aplicada | [`RESPUESTAS.md`](RESPUESTAS.md) |
| 2.1.1 Repositorio JPQL paginado | `backend/src/main/java/cr/ac/ucr/if0009/insecurepay/repository/TransaccionRepository.java` |
| 2.1.2 Servicio y endpoint `buscar-seguro` | `service/TransaccionService.java`, `controller/TransaccionController.java` |
| 2.2 Debounce RxJS y paginación Angular | `frontend/src/app/components/transacciones/`, `services/banco.service.ts` |
| 2.3 Pruebas periciales | `scripts/pruebas.sh`, [`evidencias/pruebas-curl.txt`](evidencias/pruebas-curl.txt) |

## Ejecución

```bash
cd backend && ./mvnw spring-boot:run
cd frontend && npm install && npm start
bash scripts/pruebas.sh
```

El workflow `.github/workflows/verificacion.yml` compila backend y frontend, levanta la API y ejecuta `scripts/pruebas.sh` en cada push. La salida de esa ejecución está en `evidencias/pruebas-curl.txt`.
