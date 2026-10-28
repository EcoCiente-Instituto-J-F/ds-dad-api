package dad.core.gateway;

import dad.core.domain.Catalogos;
import dad.core.domain.Dashboard;

public interface ApoioGateway {

  Dashboard buscarDashboard(int meses);

  Catalogos buscarCatalogos();
}