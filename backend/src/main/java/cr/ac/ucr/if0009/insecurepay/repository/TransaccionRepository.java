package cr.ac.ucr.if0009.insecurepay.repository;

import cr.ac.ucr.if0009.insecurepay.model.Transaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(Long cuentaOrigenId, Long cuentaDestinoId);

    @Query(value = "SELECT t FROM Transaccion t " +
                   "WHERE LOWER(t.conceptoHtml) LIKE LOWER(CONCAT('%', :termino, '%')) " +
                   "   OR LOWER(t.estado) LIKE LOWER(CONCAT('%', :termino, '%'))",
           countQuery = "SELECT COUNT(t) FROM Transaccion t " +
                        "WHERE LOWER(t.conceptoHtml) LIKE LOWER(CONCAT('%', :termino, '%')) " +
                        "   OR LOWER(t.estado) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<Transaccion> buscarPorTerminoPaginado(@Param("termino") String termino, Pageable pageable);
}
