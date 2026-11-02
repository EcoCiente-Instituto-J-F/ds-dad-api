package dad.dataprovider.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_agendamentos_coletas")
public class ColetaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_agendamento_coleta")
  public Integer id;

  @Column(name = "condominio_id")
  public Integer condominioId;

  @Column(name = "cooperativa_id")
  public Integer cooperativaId;

  @Column(name = "status_agendamento_id")
  public Integer statusId;

  @Column(name = "data_inicio")
  public OffsetDateTime dataInicio;

  @Column(name = "data_fim")
  public OffsetDateTime dataFim;

  @Column(name = "possui_recorrencia")
  public boolean recorrente;
}
