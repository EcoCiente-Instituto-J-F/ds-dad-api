package dad.dataprovider.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_cursos")
public class CursoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_curso")
  public Integer id;

  @Column(name = "titulo_curso")
  public String titulo;

  @Column(name = "descricao_curso", columnDefinition = "text")
  public String descricao;

  @Column(name = "esta_ativo")
  public boolean ativo;
}