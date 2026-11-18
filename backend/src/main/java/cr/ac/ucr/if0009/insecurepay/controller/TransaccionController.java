package cr.ac.ucr.if0009.insecurepay.controller;

import cr.ac.ucr.if0009.insecurepay.dto.TransaccionDto;
import cr.ac.ucr.if0009.insecurepay.dto.TransferenciaDto;
import cr.ac.ucr.if0009.insecurepay.service.TransaccionService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @GetMapping
    public ResponseEntity<List<TransaccionDto>> listarTodas() {
        return ResponseEntity.ok(transaccionService.listarTodas());
    }

    @GetMapping("/buscar-seguro")
    public ResponseEntity<Page<TransaccionDto>> buscarSeguro(
            @RequestParam(required = false) String termino,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transaccionService.buscarTransaccionesPaginadas(termino, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerPorId(id));
    }

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<TransaccionDto>> obtenerPorCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(transaccionService.obtenerPorCuenta(cuentaId));
    }

    @PostMapping("/transferir")
    public ResponseEntity<TransaccionDto> transferir(@RequestBody TransferenciaDto dto) {
        TransaccionDto resultado = transaccionService.transferir(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
