package dad.dataprovider.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_aulas")
public class AulaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_aula")
  public Integer id;

  @Column(name = "curso_id")
  public Integer cursoId;

  @Column(name = "titulo_aula")
  public String titulo;

  @Column(name = "conteudo_aula", columnDefinition = "text")
  public String conteudo;

  @Column(name = "ordem")
  public Integer ordem;
}