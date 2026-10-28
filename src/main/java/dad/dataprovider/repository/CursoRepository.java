package dad.dataprovider.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.CursoEntity;
import jakarta.persistence.LockModeType;

public interface CursoRepository
    extends JpaRepository<CursoEntity, Integer> {

    @Query("""
        SELECT c
        FROM CursoEntity c
        WHERE LOWER(c.titulo) LIKE
            LOWER(CONCAT('%', :busca, '%'))
        """)
    Page<CursoEntity> buscar(
        @Param("busca") String busca,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM CursoEntity c
        WHERE c.id = :id
        """)
    Optional<CursoEntity> buscarComBloqueio(
        @Param("id") Integer id
    );
}