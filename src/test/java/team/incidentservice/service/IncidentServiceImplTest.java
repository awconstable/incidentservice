package team.incidentservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import team.incidentservice.hierarchy.repo.HierarchyClient;
import team.incidentservice.model.*;
import team.incidentservice.repo.IncidentRepo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class IncidentServiceImplTest
    {
    @Autowired
    private IncidentService incidentService;
    @Autowired
    private IncidentRepo mockincidentRepo;
    @Autowired
    private HierarchyClient mockHierarchyClient;

    @TestConfiguration
    static class DeploymentServiceImplTestContextConfiguration
        {
        @MockBean
        private IncidentRepo mockincidentRepo;
        @MockBean
        private HierarchyClient mockHierarchyClient;
        @Bean
        public IncidentService incidentService()
            {
            return new IncidentServiceImpl(mockincidentRepo, mockHierarchyClient);
            }
        }
    
    private Incident setupIncident(int startYear, int endYear, int startMonth, int endMonth, int startDay, int endDay,  int startHour, int endHour, int startMin, int endMin)
        {
        return new Incident(
            "i1", 
            "incident 1", 
            "a1", 
            dateOf(startYear, startMonth, startDay, startHour, startMin, 0),
            dateOf(endYear, endMonth, endDay, endHour, endMin, 0),
            "test");
        }
    
    static Date dateOf(int year,  int month, int dayOfMonth, int hour, int minute, int second){
        return Date.from(
            LocalDateTime.of(
                year, 
                month, 
                dayOfMonth, 
                hour, 
                minute, 
                second)
                .toInstant(ZoneOffset.UTC));
    }

    @Test
    void checkMttrTimeCalc()
        {
        Incident i1 = setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        when(mockincidentRepo.save(i1)).thenReturn(i1);
        Incident storedInc = incidentService.store(i1);
        assertThat(storedInc.getMttrSeconds(), is(equalTo(7200L)));
        }

    @Test
    void checkEliteLeadTimePerfLevel()
        {
        Incident d1 = setupIncident(2021, 2021, 2, 2, 4, 4, 8, 8, 0, 46);
        when(mockincidentRepo.save(d1)).thenReturn(d1);
        Incident storedDep = incidentService.store(d1);
        assertThat(storedDep.getMttrPerfLevel(), equalTo(DORALevel.ELITE));
        }
    
    @Test
    void checkHighLeadTimePerfLevel()
        {
        Incident d1 = setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        when(mockincidentRepo.save(d1)).thenReturn(d1);
        Incident storedDep = incidentService.store(d1);
        assertThat(storedDep.getMttrPerfLevel(), equalTo(DORALevel.HIGH));
        }
    
    @Test
    void checkMedLeadTimePerfLevel()
        {
        //MED and HIGH are categorised as the same in the State of DevOps Report
        Incident d1 = setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        when(mockincidentRepo.save(d1)).thenReturn(d1);
        Incident storedDep = incidentService.store(d1);
        assertThat(storedDep.getMttrPerfLevel(), equalTo(DORALevel.HIGH));
        }

    @Test
    void checkLowLeadTimePerfLevel()
        {
        Incident d1 = setupIncident(2020, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        when(mockincidentRepo.save(d1)).thenReturn(d1);
        Incident storedDep = incidentService.store(d1);
        assertThat(storedDep.getMttrPerfLevel(), equalTo(DORALevel.LOW));
        }

    @Test
    void checkListAll()
        {
        Incident d1 =  setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        Incident d2 =  setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        List<Incident> incidents = new ArrayList<>();
        incidents.add(d1);
        incidents.add(d2);
        String appId = "app1";
        when(mockincidentRepo.findByApplicationId
            (appId))
            .thenReturn(incidents);
        
        List<Incident> deployList = incidentService.listAllForApplication(appId);
        
        assertThat(deployList.size(), equalTo(2));
        }

    @Test
    void checkListHierarchy()
        {
        Incident d1 =  setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        Incident d2 =  setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        List<Incident> incidents = new ArrayList<>();
        incidents.add(d1);
        incidents.add(d2);
        String appId = "app1";
        when(mockHierarchyClient.findChildIds("a1")).thenReturn(Arrays.asList("a1", "a2"));
        when(mockincidentRepo.findByApplicationIdInOrderByResolvedDesc(anyCollection()))
            .thenReturn(incidents);

        List<Incident> incidentList = incidentService.listAllForHierarchy(appId);
        
        verify(mockHierarchyClient, times(1)).findChildIds("app1");
        verify(mockincidentRepo, times(1)).findByApplicationIdInOrderByResolvedDesc(anyCollection());
        assertThat(incidentList.size(), equalTo(2));
        }

    @Test
    void checkListAllWithDate()
        {
        Incident d1 =  setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        Incident d2 =  setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        List<Incident> incidents = new ArrayList<>();
        incidents.add(d1);
        incidents.add(d2);
        String appId = "app1";
        when(mockincidentRepo.findByApplicationIdAndCreatedBetweenOrderByCreated
            (appId,
                dateOf(2020, 3, 10, 0, 0, 0),
                dateOf(2020, 3, 11, 0, 0, 0)))
            .thenReturn(incidents);

        List<Incident> deployList = incidentService.listAllForApplication(appId, dateOf(2020, 3, 10, 0, 0, 0));

        assertThat(deployList.size(), equalTo(2));
        }

    @Test
    void checkDelete()
        {
        Incident i1 =  setupIncident(2021, 2021, 2, 2, 4, 4, 8, 10, 0, 0);
        when(mockincidentRepo.findById("id123"))
            .thenReturn(Optional.of(i1));
        String id = incidentService.delete("id123");
        assertThat(id, is(equalTo("id123")));
        verify(mockincidentRepo, times(1)).findById("id123");
        verify(mockincidentRepo, times(1)).delete(i1);
        }
    }
