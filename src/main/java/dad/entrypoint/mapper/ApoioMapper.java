package dad.entrypoint.mapper;

import org.springframework.stereotype.Component;

import dad.core.domain.Catalogos;
import dad.core.domain.Dashboard;
import dad.entrypoint.dto.response.CatalogosResponse;
import dad.entrypoint.dto.response.DashboardResponse;

@Component
public class ApoioMapper {

  public DashboardResponse toResponse(
      Dashboard dashboard) {
    return new DashboardResponse(
        dashboard.geradoEm(),
        dashboard.usuarios(),
        dashboard.usuariosAtivos(),
        dashboard.condominios(),
        dashboard.condominiosAtivos(),
        dashboard.cooperativas(),
        dashboard.cursos(),
        dashboard.quizzes(),
        dashboard.agendamentos(),
        dashboard.usuariosCadastradosHoje(),
        dashboard.postagensHoje(),

        dashboard.evolucao()
            .stream()
            .map(mes -> new DashboardResponse.MesResponse(
                mes.mes(),
                mes.usuariosNovos(),
                mes.cooperativasNovas(),
                mes.postagens()))
            .toList(),

        dashboard.usuariosRecentes()
            .stream()
            .map(usuario -> new DashboardResponse.UsuarioRecenteResponse(
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                usuario.tipo(),
                usuario.ativo(),
                usuario.registroEm()))
            .toList());
  }

  public CatalogosResponse toResponse(
      Catalogos catalogos) {
    return new CatalogosResponse(
        catalogos.tiposUsuarios()
            .stream()
            .map(this::toResponse)
            .toList(),

        catalogos.tiposCondominios()
            .stream()
            .map(this::toResponse)
            .toList(),

        catalogos.categoriasResiduos()
            .stream()
            .map(categoria -> new CatalogosResponse.CategoriaResponse(
                categoria.id(),
                categoria.nome(),
                categoria.permiteReciclagem(),
                categoria.cor(),
                categoria.pontosBase(),
                categoria.limitePontosDiario()))
            .toList(),

        catalogos.diasSemana()
            .stream()
            .map(dia -> new CatalogosResponse.DiaSemanaResponse(
                dia.id(),
                dia.nome(),
                dia.numero()))
            .toList(),

        catalogos.statusAgendamentos()
            .stream()
            .map(this::toResponse)
            .toList(),

        catalogos.sindicos()
            .stream()
            .map(sindico -> new CatalogosResponse.SindicoResponse(
                sindico.id(),
                sindico.usuarioId(),
                sindico.nome()))
            .toList());
  }

  private CatalogosResponse.OpcaoResponse toResponse(
      Catalogos.Opcao opcao) {
    return new CatalogosResponse.OpcaoResponse(
        opcao.id(),
        opcao.nome());
  }
}