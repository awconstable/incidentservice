package team.incidentservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team.incidentservice.hierarchy.repo.HierarchyClient;
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
    private static final Logger log = LoggerFactory.getLogger(IncidentServiceImpl.class);

    private final IncidentRepo incidentRepo;
    private final HierarchyClient hierarchyClient;

    @Autowired
    public IncidentServiceImpl(IncidentRepo incidentRepo, HierarchyClient hierarchyClient)
        {
        this.incidentRepo = incidentRepo;
        this.hierarchyClient = hierarchyClient;
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
    public String delete(String id)
        {
        Optional<Incident> incident = get(id);
        incident.ifPresent(incidentRepo::delete);
        return id;
        }

    @Override
    public List<Incident> listAllForApplication(String applicationId)
        {
        return incidentRepo.findByApplicationId(applicationId);
        }

    @Override
    public List<Incident> listAllForHierarchy(String applicationId)
        {
        log.info("Loading all incidents in the hierarchy starting at applicationId {}", applicationId);
        Collection<String> appIds = hierarchyClient.findChildIds(applicationId);
        return incidentRepo.findByApplicationIdInOrderByResolvedDesc(appIds);
        }

    @Override
    public List<Incident> listAllForApplication(String applicationId, Date reportingDate)
        {
        return incidentRepo.findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, getStartDate(reportingDate, 0), getEndDate(reportingDate));
        }

    @Override
    public MTTR calculateMTTR(String applicationId, Date reportingDate)
        {
        Date startDate = getStartDate(reportingDate, 89);
        Date endDate = getEndDate(reportingDate);
        List<Incident> incidents = incidentRepo
            .findByApplicationIdAndCreatedBetweenOrderByCreated(applicationId, startDate, endDate);
        //No data, return unknown performance level
        if(incidents.size() == 0){
        return new MTTR(applicationId, reportingDate, 0,0, DORALevel.UNKNOWN);
        }
        ArrayList<Long> cMttrTimes = new ArrayList<>();
        incidents.forEach( 
            incident -> cMttrTimes.add(incident.getMttrSeconds())
        );
        long mttrSecs = findAverageUsingStream(cMttrTimes.toArray(new Long[0]));
        DORALevel mttrPerfLevel = findDORAPerfLevel(mttrSecs);
        return new MTTR(applicationId, reportingDate, mttrSecs, incidents.size(), mttrPerfLevel);
        }

    private Date getStartDate(Date reportingDate, Integer minusDays)
        {
            ZonedDateTime startDate = ZonedDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).minusDays(minusDays);
            return Date.from(startDate.toInstant());
        }
    
    private Date getEndDate(Date reportingDate)
        {
            return Date.from(ZonedDateTime.ofInstant(reportingDate.toInstant(), ZoneOffset.UTC).plusDays(1).toInstant());
        }
    }
