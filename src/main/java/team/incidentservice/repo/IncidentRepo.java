package team.incidentservice.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import team.incidentservice.model.Incident;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepo extends MongoRepository<Incident, String>
    {
        Optional<Incident> findByIncidentId(String deploymentId);
        
        List<Incident> findByApplicationId(String applicationId);

        List<Incident> findByApplicationIdAndCreatedBetweenOrderByCreated(String applicationId, Date start, Date end);
    }
