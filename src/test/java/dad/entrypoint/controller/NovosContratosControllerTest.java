package dad.entrypoint.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dad.core.domain.Pagina;
import dad.core.gateway.CalendarioColetaGateway;
import dad.core.gateway.ConsultaCadastroUsuariosGateway;
import dad.core.gateway.UsuarioGateway;
import dad.core.service.CalendarioColetaService;
import dad.core.service.ImportacaoUsuariosService;
import dad.core.service.UsuarioService;
import dad.entrypoint.exception.GlobalExceptionHandler;
import dad.entrypoint.mapper.ColetaMapper;
import dad.entrypoint.mapper.UsuarioMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NovosContratosControllerTest {
  private final UsuarioGateway usuarios = mock(UsuarioGateway.class);
  private final ConsultaCadastroUsuariosGateway validacao =
      mock(ConsultaCadastroUsuariosGateway.class);
  private final CalendarioColetaGateway calendario = mock(CalendarioColetaGateway.class);
  private MockMvc mvc;
  private static final String USUARIO =
      """
        {"nome":"Pessoa","email":"pessoa@teste.com","senha":"Teste@123","tipoUsuarioId":1,
         "endereco":{"cep":"01001000","uf":"SP","cidade":"São Paulo","logradouro":"Rua","numero":"1"}}
        """;

  @BeforeEach
  void configurar() {
    mvc =
        MockMvcBuilders.standaloneSetup(
                new ImportacaoUsuariosController(
                    new ImportacaoUsuariosService(new UsuarioService(usuarios), validacao),
                    new UsuarioMapper()),
                new CalendarioColetaController(
                    new CalendarioColetaService(calendario), new ColetaMapper()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void validaDtoAninhadoComCaminhoIndexado() throws Exception {
    mvc.perform(
            post("/api/admin/v1/usuarios/importacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"usuarios\":[" + USUARIO.replace("pessoa@teste.com", "invalido") + "]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos['usuarios.0.email']").exists());
    verifyNoInteractions(usuarios, validacao);
  }

  @Test
  void importaJsonComContratoIndividual() throws Exception {
    when(validacao.consultar(anyList()))
        .thenAnswer(
            inv ->
                inv.<java.util.List<dad.core.domain.CriarUsuario>>getArgument(0).stream()
                    .map(
                        u ->
                            new dad.core.domain.ReferenciasCadastroUsuario(
                                false, false, "Comum", null, true))
                    .toList());
    mvc.perform(
            post("/api/admin/v1/usuarios/importacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarios\":[" + USUARIO + "]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.importados").value(1));
    verify(usuarios).criar(argThat(u -> u.email().equals("pessoa@teste.com")));
  }

  @Test
  void rejeitaUsuarioNuloESemChamarCadastro() throws Exception {
    mvc.perform(
            post("/api/admin/v1/usuarios/importacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarios\":[null]}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(usuarios);
  }

  @Test
  void exigeTimestampComFusoNoCalendario() throws Exception {
    mvc.perform(
            get("/api/admin/v1/coletas/calendario")
                .param("inicio", "2026-11-02T12:00:00")
                .param("fim", "2026-11-03T12:00:00"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.mensagem").exists());
    verifyNoInteractions(calendario);
  }

  @Test
  void usaPaginacaoPadraoComCinquentaOcorrencias() throws Exception {
    when(calendario.listar(any(), any(), isNull(), isNull(), isNull(), eq(0), eq(50)))
        .thenReturn(new Pagina<>(List.of(), 0, 50, 0, 0));
    mvc.perform(
            get("/api/admin/v1/coletas/calendario")
                .param("inicio", "2026-11-01T00:00:00Z")
                .param("fim", "2026-12-01T00:00:00Z"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pagina").value(0))
        .andExpect(jsonPath("$.tamanho").value(50))
        .andExpect(jsonPath("$.itens").isArray());
  }
}
