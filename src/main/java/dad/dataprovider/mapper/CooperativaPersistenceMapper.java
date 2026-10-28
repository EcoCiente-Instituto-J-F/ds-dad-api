package dad.dataprovider.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import dad.core.domain.Cooperativa;
import dad.core.domain.Endereco;
import dad.core.domain.PontoColeta;
import dad.dataprovider.entity.CooperativaEntity;
import dad.dataprovider.entity.EnderecoEntity;
import dad.dataprovider.entity.PontoEntity;

@Component
public class CooperativaPersistenceMapper {

  public Endereco toDomain(EnderecoEntity entity) {
    if (entity == null) {
      return null;
    }

    return new Endereco(
        entity.cep,
        entity.uf,
        entity.cidade,
        entity.logradouro,
        entity.numero,
        entity.complemento);
  }

  public Cooperativa toDomain(
      CooperativaEntity entity,
      EnderecoEntity endereco,
      List<Integer> categorias,
      List<PontoColeta> pontos) {
    return new Cooperativa(
        entity.id,
        entity.nome,
        entity.cnpj,
        entity.email,
        entity.telefone,
        entity.usuarioId,
        entity.criadoEm,
        toDomain(endereco),
        categorias,
        pontos);
  }

  public PontoColeta toDomain(
      PontoEntity entity,
      EnderecoEntity endereco,
      List<Integer> categorias) {
    return new PontoColeta(
        entity.id,
        entity.nome,
        entity.abertura,
        entity.fechamento,
        entity.ativo,
        toDomain(endereco),
        categorias);
  }

  public void atualizar(
      CooperativaEntity entity,
      Cooperativa cooperativa) {
    entity.nome = cooperativa.nome();
    entity.cnpj = cooperativa.cnpj();
    entity.email = cooperativa.email();
    entity.telefone = cooperativa.telefone();
    entity.usuarioId = cooperativa.usuarioId();
  }

  public void atualizar(
      PontoEntity entity,
      PontoColeta ponto,
      Integer cooperativaId) {
    entity.nome = ponto.nome();
    entity.abertura = ponto.abertura();
    entity.fechamento = ponto.fechamento();
    entity.ativo = ponto.ativo();
    entity.cooperativaId = cooperativaId;
  }

  public EnderecoEntity toEntity(Endereco endereco) {
    var entity = new EnderecoEntity();

    entity.cep = endereco.cep();
    entity.uf = endereco.uf();
    entity.cidade = endereco.cidade();
    entity.logradouro = endereco.logradouro();
    entity.numero = endereco.numero();
    entity.complemento = endereco.complemento();

    return entity;
  }
}