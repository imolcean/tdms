package de.tu_berlin.imolcean.tdm.core.repositories;

import de.tu_berlin.imolcean.tdm.core.entities.StageDataSourceParams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StageDataSourceParamsRepository extends JpaRepository<StageDataSourceParams, Integer>
{
    Optional<StageDataSourceParams> findByStageName(String name);

    boolean existsByStageName(String name);
}
