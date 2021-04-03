package team.incidentservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import team.incidentservice.model.Incident;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface IncidentRepo extends MongoRepository<Incident, String>
    {
        List<Incident> findByApplicationId(String applicationId);

        List<Incident> findByApplicationIdInOrderByResolvedDesc(Collection<String> applicationIds);
        
        List<Incident> findByApplicationIdAndCreatedBetweenOrderByCreated(String applicationId, Date start, Date end);
    }
