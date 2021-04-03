package team.incidentservice.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.incidentservice.MongoDBContainerTest;
import team.incidentservice.model.Incident;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class IncidentRepoTest extends MongoDBContainerTest
    {
    
    @Autowired
    IncidentRepo repo;
    
    @BeforeEach
    void setUp()
        {
        Incident i1 = new Incident("i1", "incident 1", "a1", Date.from(Instant.now()), Date.from(Instant.now()), "test");
        repo.save(i1);
        Incident i2 = new Incident("i2", "incident 2", "a1", Date.from(Instant.now()), Date.from(Instant.now()),"test");
        repo.save(i2);
        Incident i3 = new Incident("i3", "incident 3", "a2", Date.from(Instant.now()), Date.from(Instant.now()),"test");
        repo.save(i3);
        Incident i4 = new Incident("i4", "incident 4", "a3", Date.from(Instant.now()), Date.from(Instant.now()),"test");
        repo.save(i4);
        }

    @AfterEach
    void tearDown()
        {
        repo.deleteAll();
        }
    
    @Test
    public void getWithIncidentId()
        {
            Optional<Incident> i1 = repo.findById("i1");
            assert(i1.isPresent());
            assertThat(i1.get().getIncidentId(), is(equalTo("i1")));
        }

    @Test
    public void getAllWithApplicationId()
        {
        List<Incident> incidents = repo.findByApplicationId("a1");
        assertThat(incidents.size(), is(equalTo(2)));
        }
    
    @Test
    public void getAllForDateRange()
        {
            LocalDateTime startDateTime = LocalDate.now().atStartOfDay();
            LocalDateTime endDateTime = LocalDate.now().plusDays(1).atStartOfDay();
            Date startDate = Date.from(startDateTime.toInstant(ZoneOffset.UTC));
            Date endDate = Date.from(endDateTime.toInstant(ZoneOffset.UTC));
            List<Incident> deploys = repo.findByApplicationIdAndCreatedBetweenOrderByCreated("a1", startDate, endDate);
            assertThat(deploys.size(), is(equalTo(2)));
        }

    @Test
    public void getAllAppsUsingIn()
        {
        List<Incident> incidents = repo.findByApplicationIdInOrderByCreatedDesc(Arrays.asList("a1", "a2"));
        assertThat(incidents.size(), is(equalTo(3)));
        }
    }