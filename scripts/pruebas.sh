#!/usr/bin/env bash
set -euo pipefail
URL="${URL:-http://localhost:8080}"

echo "=== Paso 1: payload de inyeccion SQL neutralizado ==="
curl -sS -i -G -X GET "$URL/api/transacciones/buscar-seguro" \
  --data-urlencode "termino=' OR 1=1 --" \
  --data-urlencode "page=0" \
  --data-urlencode "size=10" | tee /tmp/paso1.txt
echo
CUERPO1=$(sed -n '/^\r\{0,1\}$/,$p' /tmp/paso1.txt | tail -n +2)
TOTAL1=$(echo "$CUERPO1" | jq '.content | length')
echo "Resultados en content: $TOTAL1"
test "$TOTAL1" -eq 0

echo
echo "=== Paso 2: busqueda legitima paginada (termino=pago, size=5) ==="
curl -sS -i -G -X GET "$URL/api/transacciones/buscar-seguro" \
  --data-urlencode "termino=pago" \
  --data-urlencode "page=0" \
  --data-urlencode "size=5" | tee /tmp/paso2.txt
echo
head -1 /tmp/paso2.txt | grep -q " 200"
CUERPO2=$(sed -n '/^\r\{0,1\}$/,$p' /tmp/paso2.txt | tail -n +2)
echo "$CUERPO2" | jq '{totalElements, totalPages, size, number, resultados: (.content | length)}'
test "$(echo "$CUERPO2" | jq '.size')" -eq 5
test "$(echo "$CUERPO2" | jq '.totalElements')" -ge 1

echo
echo "=== Extra: termino vacio retorna todas ordenadas por fecha descendente ==="
curl -sS -G "$URL/api/transacciones/buscar-seguro" --data-urlencode "page=0" --data-urlencode "size=10" \
  | jq '{totalElements, totalPages, fechas: [.content[].fecha]}'

echo
echo "=== Extra: endpoint vulnerable /buscar eliminado ==="
curl -sS -o /dev/null -w "HTTP %{http_code}\n" -G "$URL/api/transacciones/buscar" --data-urlencode "termino=' OR '1'='1"

echo
echo "RESULTADO: todas las verificaciones pasaron"
