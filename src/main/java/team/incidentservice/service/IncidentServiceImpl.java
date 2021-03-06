package team.incidentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team.incidentservice.model.DORALevel;
import team.incidentservice.model.Incident;
import team.incidentservice.model.MTTR;
import team.incidentservice.repo.IncidentRepo;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class IncidentServiceImpl implements IncidentService
    {

    private final IncidentRepo incidentRepo;

    @Autowired
    public IncidentServiceImpl(IncidentRepo incidentRepo)
        {
        this.incidentRepo = incidentRepo;
        }

    private static long findAverageUsingStream(Long[] array) {
        return Math.round(Arrays.stream(array).mapToLong(Long::longValue).average().orElse(Double.NaN));
    }
    
    private DORALevel findDORAPerfLevel(long mttrSecs){
        if(mttrSecs == 0){
            return DORALevel.LOW;
        }
        if(mttrSecs < DORALevel.HOUR){
            return DORALevel.ELITE;
        } else if(mttrSecs < DORALevel.DAY){
            return DORALevel.HIGH;
        } else {
            return DORALevel.LOW;
        }
    }
    
    @Override
    public Incident store(Incident incident)
        {
        ZonedDateTime createdDate = ZonedDateTime.ofInstant(incident.getCreated().toInstant(), ZoneOffset.UTC);
        ZonedDateTime resolvedDate = ZonedDateTime.ofInstant(incident.getResolved().toInstant(), ZoneOffset.UTC);
        long mttrSecs = resolvedDate.toEpochSecond() - createdDate.toEpochSecond();
        incident.setMttrSeconds(mttrSecs);
        DORALevel perfLevel = findDORAPerfLevel(mttrSecs);
        incident.setMttrPerfLevel(perfLevel);
        return incidentRepo.save(incident);
        }

    @Override
    public Optional<Incident> get(String id)
        {
        return incidentRepo.findById(id);
        }

    @Override
    public List<Incident> list()
        {
        return incidentRepo.findAll();
        }

    @Override
    public List<Incident> listAllForApplication(String applicationId)
        {
        return incidentRepo.findByApplicationId(applicationId);
        }

    @Override
    public List<Incident> listAllForApplication(String applicationId, Date reportingDate)
        {
        return incidentRepo.findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, getStartDate(reportingDate, 0), getEndDate(reportingDate));
        }

    @Override
    public MTTR calculateMTTR(String applicationId, Date reportingDate)
        {
        //TODO implement
        return null;
        }

    private Date getStartDate(Date reportingDate, Integer minusDays){
        ZonedDateTime startDate = ZonedDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).minusDays(minusDays);
        return Date.from(startDate.toInstant());
    }
    
    private Date getEndDate(Date reportingDate){
        return Date.from(ZonedDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).plusDays(1).toInstant());
    }
    }
