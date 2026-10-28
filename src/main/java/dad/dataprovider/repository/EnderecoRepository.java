package dad.dataprovider.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dad.dataprovider.entity.EnderecoEntity;

public interface EnderecoRepository
    extends JpaRepository<EnderecoEntity, Integer> {
}