package br.com.searchjob.job.model;

import java.io.Serializable;

/**
 * Chave composta da {@link JobTechnology}: a mesma tecnologia não entra duas vezes na
 * mesma vaga.
 */
public record JobTechnologyId(Long jobId, Long technologyId) implements Serializable {

}
