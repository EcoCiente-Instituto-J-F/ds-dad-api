package dad.dataprovider.gateway;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dad.core.domain.Coleta;
import dad.core.exception.UsuarioNaoAutenticadoException;
import dad.dataprovider.entity.UsuarioEntity;
import dad.dataprovider.mapper.ColetaPersistenceMapper;
import dad.dataprovider.repository.ColetaRepository;
import dad.dataprovider.repository.CondominioRepository;
import dad.dataprovider.repository.CooperativaRepository;
import dad.dataprovider.repository.UsuarioRepository;
import dad.dataprovider.repository.VisitaColetaRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ColetaSolicitanteGatewayTest {
  private final ColetaRepository coletas = mock(ColetaRepository.class);
  private final VisitaColetaRepository visitas = mock(VisitaColetaRepository.class);
  private final CondominioRepository condominios = mock(CondominioRepository.class);
  private final CooperativaRepository cooperativas = mock(CooperativaRepository.class);
  private final ColetaPersistenceMapper mapper = mock(ColetaPersistenceMapper.class);
  private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
  private final ColetaGatewayImpl gateway =
      new ColetaGatewayImpl(coletas, visitas, condominios, cooperativas, mapper, usuarios);

  @Test
  void usuarioInexistenteInterrompeAntesDePersistir() {
    when(usuarios.buscarComBloqueio(17)).thenReturn(Optional.empty());
    assertThrows(
        UsuarioNaoAutenticadoException.class, () -> gateway.criar(new Coleta.Criacao(null, 17)));
    verifyNoInteractions(coletas, visitas, condominios, cooperativas, mapper);
  }

  @Test
  void usuarioInativoInterrompeAntesDePersistir() {
    var usuario = new UsuarioEntity();
    usuario.id = 17;
    usuario.ativo = false;
    when(usuarios.buscarComBloqueio(17)).thenReturn(Optional.of(usuario));
    assertThrows(
        UsuarioNaoAutenticadoException.class, () -> gateway.criar(new Coleta.Criacao(null, 17)));
    verifyNoInteractions(coletas, visitas, condominios, cooperativas, mapper);
  }

  @Test
  void naoExisteCriacaoAlternativaSemSolicitante() {
    assertThrows(UsuarioNaoAutenticadoException.class, () -> gateway.salvar(null, null));
    verifyNoInteractions(usuarios, coletas, visitas, condominios, cooperativas, mapper);
  }
}
