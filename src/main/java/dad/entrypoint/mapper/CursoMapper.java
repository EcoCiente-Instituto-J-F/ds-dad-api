package dad.entrypoint.mapper;

import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import dad.core.domain.Aula;
import dad.core.domain.Curso;
import dad.core.domain.Pagina;
import dad.entrypoint.dto.request.CursoRequest;
import dad.entrypoint.dto.response.CursoResponse;
import dad.entrypoint.dto.response.PaginaResponse;

@Component
public class CursoMapper {

  public Curso toDomain(
      Integer id,
      CursoRequest request) {
    var aulas = IntStream
        .range(0, request.aulas().size())
        .mapToObj(indice -> {
          var aula = request.aulas().get(indice);

          return new Aula(
              aula.id(),
              id,
              aula.titulo().trim(),
              aula.conteudo(),
              indice + 1);
        })
        .toList();

    return new Curso(
        id,
        request.titulo().trim(),
        request.descricao().trim(),
        request.ativo(),
        aulas);
  }

  public CursoResponse toResponse(Curso curso) {
    return new CursoResponse(
        curso.id(),
        curso.titulo(),
        curso.descricao(),
        curso.ativo(),
        curso.aulas()
            .stream()
            .map(this::toResponse)
            .toList());
  }

  public PaginaResponse<CursoResponse> toResponse(
      Pagina<Curso> pagina) {
    return new PaginaResponse<>(
        pagina.itens()
            .stream()
            .map(this::toResponse)
            .toList(),
        pagina.pagina(),
        pagina.tamanho(),
        pagina.totalItens(),
        pagina.totalPaginas());
  }

  private CursoResponse.AulaResponse toResponse(
      Aula aula) {
    return new CursoResponse.AulaResponse(
        aula.id(),
        aula.cursoId(),
        aula.titulo(),
        aula.conteudo(),
        aula.ordem());
  }
}