package com.gtbr.gtbrpg.repository;

import com.gtbr.gtbrpg.domain.entity.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends CrudRepository<Configuration, String> {
}
