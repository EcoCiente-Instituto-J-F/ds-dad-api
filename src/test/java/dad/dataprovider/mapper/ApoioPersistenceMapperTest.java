package dad.dataprovider.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import dad.core.exception.RegraNegocioException;

import static org.junit.jupiter.api.Assertions.*;

class ApoioPersistenceMapperTest {

  private final ApoioPersistenceMapper mapper = new ApoioPersistenceMapper();

  @ParameterizedTest
  @CsvSource({
      "Domingo, 0",
      "Segunda-feira, 1",
      "Terça-feira, 2",
      "Quarta-feira, 3",
      "Quinta-feira, 4",
      "Sexta-feira, 5",
      "Sábado, 6",
      "SEGUNDA_FEIRA, 1",
      "Terça, 2"
  })
  void deveAssociarNomeAoNumeroSemUsarIdDoBanco(
      String nome,
      int numero) {
    var dia = mapper.toDia(
        new Object[] { 997, nome });

    assertEquals(
        997,
        dia.id().intValue());

    assertEquals(
        numero,
        dia.numero().intValue());
  }

  @Test
  void deveInformarDiaNaoReconhecido() {
    assertThrows(
        RegraNegocioException.class,
        () -> mapper.toDia(
            new Object[] { 997, "Dia inexistente" }));
  }

  @Test
  void devePreservarLimiteDiarioAusente() {
    var categoria = mapper.toCategoria(
        new Object[] {
            1,
            "Papel",
            true,
            "#0000FF",
            10,
            null
        });

    assertNull(categoria.limitePontosDiario());

    assertEquals(
        10,
        categoria.pontosBase().intValue());
  }
}