package dad.dataprovider.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import dad.core.domain.Aula;
import dad.core.domain.Curso;
import dad.dataprovider.entity.AulaEntity;
import dad.dataprovider.entity.CursoEntity;

@Component
public class CursoPersistenceMapper {

  public Aula toDomain(AulaEntity entity) {
    return new Aula(
        entity.id,
        entity.cursoId,
        entity.titulo,
        entity.conteudo,
        entity.ordem);
  }

  public Curso toDomain(
      CursoEntity entity,
      List<AulaEntity> aulas) {
    return new Curso(
        entity.id,
        entity.titulo,
        entity.descricao,
        entity.ativo,
        aulas.stream()
            .map(this::toDomain)
            .toList());
  }

  public void atualizar(
      CursoEntity entity,
      Curso curso) {
    entity.titulo = curso.titulo();
    entity.descricao = curso.descricao();
    entity.ativo = curso.ativo();
  }

  public void atualizar(
      AulaEntity entity,
      Aula aula,
      Integer cursoId,
      int ordem) {
    entity.cursoId = cursoId;
    entity.titulo = aula.titulo();
    entity.conteudo = aula.conteudo();
    entity.ordem = ordem;
  }
}