package dad.dataprovider.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import dad.core.domain.Condominio;
import dad.core.domain.Endereco;
import dad.core.domain.Torre;
import dad.dataprovider.entity.CondominioEntity;
import dad.dataprovider.entity.EnderecoEntity;
import dad.dataprovider.entity.TorreEntity;

@Component
public class CondominioPersistenceMapper {

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

  public Condominio toDomain(
      CondominioEntity entity,
      EnderecoEntity endereco,
      List<TorreEntity> torres) {
    return new Condominio(
        entity.id,
        entity.nome,
        entity.cnpj,
        entity.codigoAcesso,
        entity.ativo,
        entity.tipoId,
        entity.sindicoId,
        toDomain(endereco),
        torres.stream()
            .map(torre -> new Torre(
                torre.id,
                torre.nome))
            .toList());
  }

  public void atualizar(
      CondominioEntity entity,
      Condominio condominio) {
    entity.nome = condominio.nome();
    entity.cnpj = condominio.cnpj();
    entity.codigoAcesso = condominio.codigoAcesso();
    entity.ativo = condominio.ativo();
    entity.tipoId = condominio.tipoId();
    entity.sindicoId = condominio.sindicoId();
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