package dad.dataprovider.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dad.core.domain.Coleta;
import dad.dataprovider.entity.ColetaEntity;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ColetaSolicitanteMapperTest {
  private final ColetaPersistenceMapper mapper = new ColetaPersistenceMapper();

  private Coleta.Dados dados(Integer status) {
    return new Coleta.Dados(
        1, 2, status, OffsetDateTime.parse("2026-11-02T12:00:00Z"), null, false, List.of());
  }

  @Test
  void mapeiaSolicitanteNaCriacao() {
    var entity = mapper.toNovaEntity(new Coleta.Criacao(dados(3), 17));
    assertEquals(17, entity.solicitadoPorUsuarioId);
    assertEquals(1, entity.condominioId);
  }

  @Test
  void atualizacaoPreservaSolicitanteOriginal() {
    var entity = mapper.toNovaEntity(new Coleta.Criacao(dados(3), 17));
    mapper.atualizar(entity, dados(4));
    assertEquals(17, entity.solicitadoPorUsuarioId);
    assertEquals(4, entity.statusId);
  }

  @Test
  void colunaObrigatoriaNaoPodeSerAtualizadaPeloHibernate() throws Exception {
    var coluna =
        ColetaEntity.class
            .getField("solicitadoPorUsuarioId")
            .getAnnotation(jakarta.persistence.Column.class);
    assertEquals("solicitado_por_usuario_id", coluna.name());
    assertFalse(coluna.nullable());
    assertFalse(coluna.updatable());
  }
}
