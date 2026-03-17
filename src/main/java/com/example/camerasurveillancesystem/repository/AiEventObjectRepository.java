package com.example.camerasurveillancesystem.repository;

import com.example.camerasurveillancesystem.domain.AiEventObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiEventObjectRepository extends JpaRepository<AiEventObject, Long> {

    List<AiEventObject> findByAiEventId(Long aiEventId);

    List<AiEventObject> findByObjectType(String objectType);

    List<AiEventObject> findByConfidenceGreaterThanEqual(Double minConfidence);

    List<AiEventObject> findByAiEventIdAndObjectType(Long aiEventId, String objectType);

    long countByAiEventId(Long aiEventId);

    @Query("SELECT DISTINCT o.objectType FROM AiEventObject o")
    List<String> findDistinctObjectTypes();
}
