package dad.core.service;

import java.time.OffsetDateTime;
import java.util.HashSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Coleta;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.ColetaGateway;

@Service
@Transactional
public class ColetaService {

  private final ColetaGateway gateway;

  public ColetaService(ColetaGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Coleta> listar(
      String busca,
      Integer statusId,
      OffsetDateTime inicio,
      OffsetDateTime fim,
      int pagina,
      int tamanho) {
    if (pagina < 0 || tamanho < 1 || tamanho > 100) {
      throw new RegraNegocioException(
          "Página deve ser maior ou igual a zero "
              + "e tamanho deve estar entre 1 e 100");
    }

    if (statusId != null && statusId <= 0) {
      throw new RegraNegocioException(
          "Status inválido");
    }

    validarPeriodo(inicio, fim);

    return gateway.listar(
        busca,
        statusId,
        inicio,
        fim,
        pagina,
        tamanho);
  }

  @Transactional(readOnly = true)
  public Coleta buscar(Integer id) {
    return gateway.buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Agendamento não encontrado"));
  }

  public Coleta salvar(
      Integer id,
      Coleta.Dados dados) {
    if (dados.condominioId() == null
        || dados.cooperativaId() == null
        || dados.statusId() == null
        || dados.dataInicio() == null) {
      throw new RegraNegocioException(
          "Informe condomínio, cooperativa, status e início");
    }

    validarPeriodo(
        dados.dataInicio(),
        dados.dataFim());

    var dias = dados.diasSemanaIds();

    if (dias == null
        || dias.stream().anyMatch(
            dia -> dia == null || dia <= 0)
        || dias.size() > 7) {
      throw new RegraNegocioException(
          "Lista de dias inválida");
    }

    if (new HashSet<>(dias).size() != dias.size()) {
      throw new RegraNegocioException(
          "Não informe dias repetidos");
    }

    if (dados.recorrente() && dias.isEmpty()) {
      throw new RegraNegocioException(
          "Informe os dias da recorrência");
    }

    if (!dados.recorrente() && !dias.isEmpty()) {
      throw new RegraNegocioException(
          "Agendamento sem recorrência deve enviar diasSemanaIds vazio");
    }

    return gateway.salvar(id, dados);
  }

  public Coleta cancelar(Integer id) {
    return gateway.cancelar(id);
  }

  public Coleta registrarVisita(
      Integer id,
      Coleta.RegistroVisita registro) {
    if (registro.data() == null) {
      throw new RegraNegocioException(
          "Informe a data da visita");
    }

    if (registro.confirmada() && !registro.realizada()) {
      throw new RegraNegocioException(
          "Não confirme uma visita que não foi realizada");
    }

    if (!registro.realizada()
        && (registro.observacao() == null
            || registro.observacao().isBlank())) {
      throw new RegraNegocioException(
          "Informe uma observação quando a visita não for realizada");
    }

    return gateway.registrarVisita(id, registro);
  }

  private void validarPeriodo(
      OffsetDateTime inicio,
      OffsetDateTime fim) {
    if (inicio != null
        && fim != null
        && fim.isBefore(inicio)) {
      throw new RegraNegocioException(
          "O fim não pode ser anterior ao início");
    }
  }
}