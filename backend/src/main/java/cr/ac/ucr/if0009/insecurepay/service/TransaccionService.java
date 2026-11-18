package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.TransaccionDto;
import cr.ac.ucr.if0009.insecurepay.dto.TransferenciaDto;
import cr.ac.ucr.if0009.insecurepay.model.CuentaBancaria;
import cr.ac.ucr.if0009.insecurepay.model.Transaccion;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import cr.ac.ucr.if0009.insecurepay.repository.TransaccionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaccionService {

    private static final int TAMANO_MAXIMO = 50;
    private static final int LONGITUD_MAXIMA_TERMINO = 100;

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    public TransaccionService(TransaccionRepository transaccionRepository, CuentaRepository cuentaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public List<TransaccionDto> listarTodas() {
        return transaccionRepository.findAll().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public TransaccionDto obtenerPorId(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaccion no encontrada con ID: " + id));
        return convertirADto(transaccion);
    }

    public List<TransaccionDto> obtenerPorCuenta(Long cuentaId) {
        return transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransaccionDto> buscarTransaccionesPaginadas(String termino, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(
                Math.max(0, pagina),
                Math.min(Math.max(1, tamano), TAMANO_MAXIMO),
                Sort.by("fecha").descending()
        );

        String terminoLimpio = termino == null ? "" : termino.trim();
        if (terminoLimpio.length() > LONGITUD_MAXIMA_TERMINO) {
            terminoLimpio = terminoLimpio.substring(0, LONGITUD_MAXIMA_TERMINO);
        }

        Page<Transaccion> resultado = terminoLimpio.isEmpty()
                ? transaccionRepository.findAll(pageable)
                : transaccionRepository.buscarPorTerminoPaginado(terminoLimpio, pageable);

        return resultado.map(this::convertirADto);
    }

    @Transactional
    public TransaccionDto transferir(TransferenciaDto dto) {
        if (dto.getMonto() == null || dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto a transferir debe ser mayor a cero.");
        }

        CuentaBancaria cuentaOrigen = cuentaRepository.findById(dto.getCuentaOrigenId())
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada con ID: " + dto.getCuentaOrigenId()));

        CuentaBancaria cuentaDestino = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuentaDestino())
                .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada con numero: " + dto.getNumeroCuentaDestino()));

        if (cuentaOrigen.getSaldo().compareTo(dto.getMonto()) < 0) {
            throw new RuntimeException("Fondos insuficientes en la cuenta de origen seleccionada.");
        }

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(dto.getMonto()));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(dto.getMonto()));
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(cuentaDestino);
        transaccion.setMonto(dto.getMonto());
        transaccion.setConceptoHtml(dto.getConceptoHtml());
        transaccion.setEstado("COMPLETADA");

        Transaccion guardada = transaccionRepository.save(transaccion);
        return convertirADto(guardada);
    }

    private TransaccionDto convertirADto(Transaccion t) {
        TransaccionDto dto = new TransaccionDto();
        dto.setId(t.getId());
        dto.setCuentaOrigenId(t.getCuentaOrigen().getId());
        dto.setNumeroCuentaOrigen(t.getCuentaOrigen().getNumeroCuenta());
        dto.setTitularOrigen(t.getCuentaOrigen().getUsuario().getNombreCompleto());
        dto.setCuentaDestinoId(t.getCuentaDestino().getId());
        dto.setNumeroCuentaDestino(t.getCuentaDestino().getNumeroCuenta());
        dto.setTitularDestino(t.getCuentaDestino().getUsuario().getNombreCompleto());
        dto.setMonto(t.getMonto());
        dto.setConceptoHtml(t.getConceptoHtml());
        dto.setFecha(t.getFecha());
        dto.setEstado(t.getEstado());
        return dto;
    }
}
