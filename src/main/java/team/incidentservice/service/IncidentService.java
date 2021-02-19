package team.incidentservice.service;

import team.incidentservice.model.Incident;
import team.incidentservice.model.MTTR;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IncidentService
    {
        Incident store(Incident incident);

        Optional<Incident> get(String id);

        List<Incident> list();
        
        List<Incident> listAllForApplication(String applicationId);

        List<Incident> listAllForApplication(String applicationId, Date reportingDate);
        
        MTTR calculateMTTR(String applicationId, Date reportingDate);
    }
