package dad.dataprovider.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_quizzes")
public class QuizEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_quiz")
  public Integer id;

  @Column(name = "curso_id")
  public Integer cursoId;

  @Column(name = "aula_id")
  public Integer aulaId;

  @Column(name = "titulo_quiz")
  public String titulo;

  @Column(name = "nota_minima_aprovacao")
  public BigDecimal notaMinima;

  @Column(name = "pontos_recompensa")
  public Integer pontos;

  @Column(name = "ativo")
  public boolean ativo;

  @Column(name = "criado_em")
  public OffsetDateTime criadoEm;
}
