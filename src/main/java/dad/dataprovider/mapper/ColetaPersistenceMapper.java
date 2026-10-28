package dad.dataprovider.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import dad.core.domain.Coleta;
import dad.dataprovider.entity.ColetaEntity;
import dad.dataprovider.entity.VisitaEntity;

@Component
public class ColetaPersistenceMapper {

  public Coleta toDomain(
      ColetaEntity entity,
      String status,
      List<Integer> dias,
      List<VisitaEntity> visitas) {
    return new Coleta(
        entity.id,
        entity.condominioId,
        entity.cooperativaId,
        entity.statusId,
        status,
        entity.dataInicio,
        entity.dataFim,
        entity.recorrente,
        dias,
        visitas.stream()
            .map(this::toDomain)
            .toList());
  }

  public Coleta.Visita toDomain(VisitaEntity entity) {
    return new Coleta.Visita(
        entity.id,
        entity.dataVisita,
        entity.realizada,
        entity.confirmada,
        entity.confirmadoEm,
        entity.observacao);
  }

  public void atualizar(
      ColetaEntity entity,
      Coleta.Dados dados) {
    entity.condominioId = dados.condominioId();
    entity.cooperativaId = dados.cooperativaId();
    entity.statusId = dados.statusId();
    entity.dataInicio = dados.dataInicio();
    entity.dataFim = dados.dataFim();
    entity.recorrente = dados.recorrente();
  }
}