package dad.entrypoint.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dad.config.security.UsuarioAutenticado;
import dad.core.domain.Coleta;
import dad.core.service.ColetaService;
import dad.entrypoint.exception.GlobalExceptionHandler;
import dad.entrypoint.mapper.ColetaMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ColetaSolicitanteControllerTest {
  private final ColetaService service = mock(ColetaService.class);
  private MockMvc mvc;
  private static final String JSON =
      """
      {"condominioId":1,"cooperativaId":2,"statusId":3,
       "dataInicio":"2026-11-02T12:00:00Z","dataFim":null,"recorrente":false,"diasSemanaIds":[]}
      """;

  @BeforeEach
  void configurar() {
    mvc =
        MockMvcBuilders.standaloneSetup(new ColetaController(service, new ColetaMapper()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  private UsernamePasswordAuthenticationToken autenticado(Integer id) {
    return new UsernamePasswordAuthenticationToken(
        new UsuarioAutenticado(id, "Admin", "admin@teste.com", true),
        null,
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  @Test
  void encaminhaIdDoPrincipalNoComandoDeCriacao() throws Exception {
    when(service.criar(any()))
        .thenAnswer(
            inv -> {
              Coleta.Criacao c = inv.getArgument(0);
              var d = c.dados();
              return new Coleta(
                  7,
                  d.condominioId(),
                  d.cooperativaId(),
                  d.statusId(),
                  "Agendado",
                  d.dataInicio(),
                  d.dataFim(),
                  d.recorrente(),
                  d.diasSemanaIds(),
                  List.of());
            });
    mvc.perform(
            post("/api/admin/v1/coletas")
                .principal(autenticado(17))
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
        .andExpect(status().isCreated());
    verify(service).criar(argThat(c -> c.solicitadoPorUsuarioId().equals(17)));
  }

  @Test
  void semPrincipalNaoChamaService() throws Exception {
    mvc.perform(post("/api/admin/v1/coletas").contentType(MediaType.APPLICATION_JSON).content(JSON))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  void principalSemIdValidoNaoChamaService() throws Exception {
    for (Integer id : new Integer[] {null, 0, -1}) {
      mvc.perform(
              post("/api/admin/v1/coletas")
                  .principal(autenticado(id))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JSON))
          .andExpect(status().isUnauthorized());
    }
    verifyNoInteractions(service);
  }

  @Test
  void principalAnonimoNaoChamaService() throws Exception {
    var anonimo =
        new AnonymousAuthenticationToken(
            "teste", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    mvc.perform(
            post("/api/admin/v1/coletas")
                .principal(anonimo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  void autenticacaoNaoConfirmadaNaoChamaService() throws Exception {
    var auth =
        new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticado(17, "Admin", "admin@teste.com", true), null);
    mvc.perform(
            post("/api/admin/v1/coletas")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  void rejeitaTentativaDeEscolherSolicitantePeloJson() throws Exception {
    for (String campo : List.of("solicitadoPorUsuarioId", "solicitado_por_usuario_id")) {
      String adulterado =
          JSON.strip()
              .replace("\"diasSemanaIds\":[]", "\"diasSemanaIds\":[],\"" + campo + "\":999");
      mvc.perform(
              post("/api/admin/v1/coletas")
                  .principal(autenticado(17))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(adulterado))
          .andExpect(status().isBadRequest());
    }
    verifyNoInteractions(service);
  }
}
