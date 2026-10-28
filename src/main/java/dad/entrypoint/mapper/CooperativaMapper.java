package dad.entrypoint.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import dad.core.domain.Cooperativa;
import dad.core.domain.Endereco;
import dad.core.domain.Pagina;
import dad.core.domain.PontoColeta;
import dad.entrypoint.dto.request.CooperativaRequest;
import dad.entrypoint.dto.request.EnderecoRequest;
import dad.entrypoint.dto.response.CooperativaResponse;
import dad.entrypoint.dto.response.EnderecoResponse;
import dad.entrypoint.dto.response.PaginaResponse;

@Component
public class CooperativaMapper {

  public Cooperativa toDomain(
      Integer id,
      CooperativaRequest request) {
    var pontos = request.pontos()
        .stream()
        .map(ponto -> new PontoColeta(
            ponto.id(),
            ponto.nome().trim(),
            ponto.abertura(),
            ponto.fechamento(),
            ponto.ativo(),
            toDomain(ponto.endereco()),
            ponto.categoriaIds()))
        .toList();

    String email = limpar(request.email());

    if (email != null) {
      email = email.toLowerCase(Locale.ROOT);
    }

    return new Cooperativa(
        id,
        request.nome().trim(),
        request.cnpj()
            .replaceAll("[^A-Za-z0-9]", "")
            .toUpperCase(Locale.ROOT),
        email,
        limpar(request.telefone()),
        request.usuarioId(),
        null,
        toDomain(request.endereco()),
        request.categoriaIds(),
        pontos);
  }

  public CooperativaResponse toResponse(
      Cooperativa cooperativa) {
    return new CooperativaResponse(
        cooperativa.id(),
        cooperativa.nome(),
        cooperativa.cnpj(),
        cooperativa.email(),
        cooperativa.telefone(),
        cooperativa.usuarioId(),
        cooperativa.criadoEm(),
        toResponse(cooperativa.endereco()),
        cooperativa.categoriaIds(),
        cooperativa.pontos()
            .stream()
            .map(this::toResponse)
            .toList());
  }

  public PaginaResponse<CooperativaResponse> toResponse(
      Pagina<Cooperativa> pagina) {
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

  private CooperativaResponse.PontoResponse toResponse(
      PontoColeta ponto) {
    return new CooperativaResponse.PontoResponse(
        ponto.id(),
        ponto.nome(),
        ponto.abertura(),
        ponto.fechamento(),
        ponto.ativo(),
        toResponse(ponto.endereco()),
        ponto.categoriaIds());
  }

  private Endereco toDomain(
      EnderecoRequest request) {
    if (request == null) {
      return null;
    }

    return new Endereco(
        request.cep(),
        request.uf(),
        request.cidade().trim(),
        request.logradouro().trim(),
        request.numero().trim(),
        limpar(request.complemento()));
  }

  private EnderecoResponse toResponse(
      Endereco endereco) {
    if (endereco == null) {
      return null;
    }

    return new EnderecoResponse(
        endereco.cep(),
        endereco.uf(),
        endereco.cidade(),
        endereco.logradouro(),
        endereco.numero(),
        endereco.complemento());
  }

  private String limpar(String valor) {
    return valor == null || valor.isBlank()
        ? null
        : valor.trim();
  }
}