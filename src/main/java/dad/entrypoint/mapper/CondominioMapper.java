package dad.entrypoint.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import dad.core.domain.Condominio;
import dad.core.domain.Endereco;
import dad.core.domain.Pagina;
import dad.core.domain.Torre;
import dad.entrypoint.dto.request.CondominioRequest;
import dad.entrypoint.dto.request.EnderecoRequest;
import dad.entrypoint.dto.response.CondominioResponse;
import dad.entrypoint.dto.response.EnderecoResponse;
import dad.entrypoint.dto.response.PaginaResponse;

@Component
public class CondominioMapper {

  public Condominio toDomain(
      Integer id,
      CondominioRequest request) {
    String codigo = limpar(request.codigoAcesso());

    if (codigo != null) {
      codigo = codigo.toUpperCase(Locale.ROOT);
    }

    String cnpj = request.cnpj() == null
        ? null
        : request.cnpj().replaceAll("[^0-9]", "");

    return new Condominio(
        id,
        request.nome().trim(),
        cnpj,
        codigo,
        request.ativo(),
        request.tipoId(),
        request.sindicoId(),
        toDomain(request.endereco()),
        request.torres()
            .stream()
            .map(torre -> new Torre(
                torre.id(),
                torre.nome().trim()))
            .toList());
  }

  public CondominioResponse toResponse(
      Condominio condominio) {
    return new CondominioResponse(
        condominio.id(),
        condominio.nome(),
        condominio.cnpj(),
        condominio.codigoAcesso(),
        condominio.ativo(),
        condominio.tipoId(),
        condominio.sindicoId(),
        toResponse(condominio.endereco()),
        condominio.torres()
            .stream()
            .map(torre -> new CondominioResponse.TorreResponse(
                torre.id(),
                torre.nome()))
            .toList());
  }

  public PaginaResponse<CondominioResponse> toResponse(
      Pagina<Condominio> pagina) {
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