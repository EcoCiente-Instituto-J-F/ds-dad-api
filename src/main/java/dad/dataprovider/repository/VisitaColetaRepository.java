package dad.dataprovider.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import dad.dataprovider.entity.VisitaEntity;
import jakarta.persistence.LockModeType;

public interface VisitaColetaRepository
    extends JpaRepository<VisitaEntity, Integer> {

  List<VisitaEntity> findByColetaIdOrderByDataVisitaAsc(
      Integer coletaId);

  long countByColetaId(Integer coletaId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<VisitaEntity> findByColetaIdAndDataVisitaGreaterThanEqualAndDataVisitaLessThan(
      Integer coletaId,
      OffsetDateTime inicio,
      OffsetDateTime fim);
}