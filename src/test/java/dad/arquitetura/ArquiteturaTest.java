package dad.arquitetura;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ArquiteturaTest {
  @Test
  void coreNaoDependeDeAdaptersOuTecnologiasDeEntradaEPersistencia() throws Exception {
    verificar(
        "dad/core",
        Set.of(
            "dad/dataprovider",
            "dad/entrypoint",
            "dad/config",
            "org/springframework/data",
            "org/springframework/web",
            "org/springframework/security",
            "jakarta/persistence",
            "jakarta/servlet",
            "java/sql"));
  }

  @Test
  void dominioERegrasSaoIndependentesDoSpring() throws Exception {
    verificar("dad/core/domain", Set.of("org/springframework", "jakarta/", "dad/core/service"));
    verificar("dad/core/regra", Set.of("org/springframework", "jakarta/", "dad/core/service"));
  }

  @Test
  void entrypointNaoAcessaPersistenciaDiretamente() throws Exception {
    verificar("dad/entrypoint", Set.of("dad/dataprovider"));
  }

  @Test
  void persistenciaNaoDependeDeDtosOuControllers() throws Exception {
    verificar("dad/dataprovider", Set.of("dad/entrypoint"));
  }

  private void verificar(String pacote, Set<String> proibidos) throws Exception {
    var recurso = getClass().getClassLoader().getResource(pacote);
    if (recurso == null) throw new IllegalStateException("Pacote não encontrado: " + pacote);
    var falhas = new ArrayList<String>();
    try (var arquivos = Files.walk(Path.of(recurso.toURI()))) {
      for (var arquivo : arquivos.filter(p -> p.toString().endsWith(".class")).toList()) {
        for (var referencia : referencias(arquivo)) {
          for (var proibido : proibidos) {
            String prefixo = proibido.endsWith("/") ? proibido : proibido + "/";
            if (referencia.startsWith(prefixo) || referencia.contains("L" + prefixo))
              falhas.add(arquivo.getFileName() + " -> " + referencia);
          }
        }
      }
    }
    assertTrue(falhas.isEmpty(), () -> String.join("\n", falhas));
  }

  private List<String> referencias(Path arquivo) throws Exception {
    try (var in = new DataInputStream(Files.newInputStream(arquivo))) {
      if (in.readInt() != 0xCAFEBABE) throw new IllegalStateException("Bytecode inválido");
      in.readUnsignedShort();
      in.readUnsignedShort();
      int quantidade = in.readUnsignedShort();
      List<String> textos = new ArrayList<>();
      for (int i = 1; i < quantidade; i++) {
        switch (in.readUnsignedByte()) {
          case 1 -> textos.add(in.readUTF());
          case 3, 4 -> in.skipNBytes(4);
          case 5, 6 -> {
            in.skipNBytes(8);
            i++;
          }
          case 7, 8, 16, 19, 20 -> in.skipNBytes(2);
          case 9, 10, 11, 12, 17, 18 -> in.skipNBytes(4);
          case 15 -> in.skipNBytes(3);
          default -> throw new IllegalStateException("Constant pool inválido");
        }
      }
      return textos;
    }
  }
}
