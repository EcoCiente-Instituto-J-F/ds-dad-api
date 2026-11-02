package dad.core.service;

import dad.core.domain.Catalogos;
import dad.core.domain.Dashboard;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.ApoioGateway;
import org.springframework.stereotype.Service;

@Service
public class ApoioService {

  private final ApoioGateway gateway;

  public ApoioService(ApoioGateway gateway) {
    this.gateway = gateway;
  }

  public Dashboard buscarDashboard(int meses) {
    if (meses < 1 || meses > 24) {
      throw new RegraNegocioException("Informe um período entre 1 e 24 meses");
    }

    return gateway.buscarDashboard(meses);
  }

  public Catalogos buscarCatalogos() {
    return gateway.buscarCatalogos();
  }
}
