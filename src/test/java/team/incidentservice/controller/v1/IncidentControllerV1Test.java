package team.incidentservice.controller.v1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import team.incidentservice.model.*;
import team.incidentservice.service.IncidentService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(IncidentControllerV1.class)
class IncidentControllerV1Test
    {
    
    @Autowired private MockMvc mockMvc;
    
    @MockBean private IncidentService mockIncidentService;
    
    @Test
    void store() throws Exception
        {
        mockMvc.perform(post("/api/v1/incident").contentType(MediaType.APPLICATION_JSON)
            .content(
            "[" +
            "{\n" +
            "    \"incidentId\": \"i1\",\n" + 
            "    \"incidentDesc\": \"a description\",\n" +
            "    \"applicationId\": \"a1\",\n" +
            "    \"created\": \"2020-11-30T22:00:00.000+00:00\",\n" + 
            "    \"resolved\": \"2020-11-30T23:00:00.000+00:00\",\n" +
            "    \"source\": \"test\"\n" +
            "}" +
            "]"    
            ))
            .andExpect(status().isCreated());
        verify(mockIncidentService, times(1)).store(any(Incident.class));
        }
    
    @Test
    void storeCheckValidationEmptyList() throws Exception
        {
        mockMvc.perform(post("/api/v1/incident").contentType(MediaType.APPLICATION_JSON)
            .content(
                "[]"
            ))
            .andExpect(status().is4xxClientError());
        verify(mockIncidentService, never()).store(any(Incident.class));
        }
    
    @Test
    void storeCheckValidationAppId() throws Exception
        {
        mockMvc.perform(post("/api/v1/incident").contentType(MediaType.APPLICATION_JSON)
            .content(
                "[" +
                "{\n" +
                "    \"incidentId\": \"i1\",\n" +
                "    \"incidentDesc\": \"a description\",\n" +
                "    \"created\": \"2020-11-30T22:00:00.000+00:00\",\n" +
                "    \"resolved\": \"2020-11-30T23:00:00.000+00:00\",\n" +
                "    \"source\": \"test\"\n" +
                "}" +
                "]"
            ))
            .andExpect(status().is4xxClientError());
        verify(mockIncidentService, never()).store(any(Incident.class));
        }

    @Test
    void storeCheckValidationCreated() throws Exception
        {
        mockMvc.perform(post("/api/v1/incident").contentType(MediaType.APPLICATION_JSON)
            .content(
                "[" +
                "{\n" +
                "    \"incidentId\": \"i1\",\n" +
                "    \"incidentDesc\": \"a description\",\n" +
                "    \"applicationId\": \"a1\",\n" +
                "    \"resolved\": \"2020-11-30T23:00:00.000+00:00\",\n" +
                "    \"source\": \"test\"\n" +
                "}" +
                "]"
            ))
            .andExpect(status().is4xxClientError());
        verify(mockIncidentService, never()).store(any(Incident.class));
        }

    @Test
    void storeCheckValidationResolved() throws Exception
        {
        mockMvc.perform(post("/api/v1/incident").contentType(MediaType.APPLICATION_JSON)
            .content(
                "[" +
                    "{\n" +
                    "    \"incidentId\": \"i1\",\n" +
                    "    \"incidentDesc\": \"a description\",\n" +
                    "    \"applicationId\": \"a1\",\n" +
                    "    \"created\": \"2020-11-30T22:00:00.000+00:00\",\n" +
                    "    \"source\": \"test\"\n" +
                    "}" +
                    "]"
            ))
            .andExpect(status().is4xxClientError());
        verify(mockIncidentService, never()).store(any(Incident.class));
        }
    
    @Test
    void list() throws Exception
        {
        ZonedDateTime reportingDate = LocalDate.of(2020, 10, 10).atStartOfDay(ZoneId.of("UTC"));
        
        List<Incident> incidents = new ArrayList<>();
        Incident i1 = new Incident("i1", "incident 1", "a1", Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");
        Incident i2 = new Incident("i2", "incident 2", "a1", Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");
        i1.setMttrPerfLevel(DORALevel.ELITE);
        i1.setMttrSeconds(120L);
        i2.setMttrPerfLevel(DORALevel.LOW);
        i2.setMttrSeconds(12000L);
        incidents.add(i1);
        incidents.add(i2);
        when(mockIncidentService.list()).thenReturn(incidents);
        MvcResult result = mockMvc.perform(get("/api/v1/incident")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("[{\"incidentId\":\"i1\",\"incidentDesc\":\"incident 1\",\"applicationId\":\"a1\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":120,\"mttrPerfLevel\":\"ELITE\"},{\"incidentId\":\"i2\",\"incidentDesc\":\"incident 2\",\"applicationId\":\"a1\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":12000,\"mttrPerfLevel\":\"LOW\"}]")));
        verify(mockIncidentService, times(1)).list();
        }

    @Test
    void show() throws Exception
        {
        ZonedDateTime reportingDate = LocalDate.of(2020, 10, 10).atStartOfDay(ZoneId.of("UTC"));
        
        String id = "testId";
        Incident i1 = new Incident("i1", "incident 1", "a1", Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");

        when(mockIncidentService.get(id)).thenReturn(Optional.of(i1));
        MvcResult result = mockMvc.perform(get("/api/v1/incident/" + id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("{\"incidentId\":\"i1\",\"incidentDesc\":\"incident 1\",\"applicationId\":\"a1\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":0,\"mttrPerfLevel\":null}")));
        verify(mockIncidentService, times(1)).get(id);
        }

    @Test
    void delete() throws Exception
        {
        when(mockIncidentService.delete("id123")).thenReturn("id123");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/incident/{id}", "id123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("id123")));
        verify(mockIncidentService, times(1)).delete("id123");
        }

    @Test
    void listByApp() throws Exception
        {
        ZonedDateTime reportingDate = LocalDate.of(2020, 10, 10).atStartOfDay(ZoneId.of("UTC"));
        String appId = "id123";
        Incident i1 = new Incident("i1", "incident 1", appId, Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");
        Incident i2 = new Incident("i2", "incident 2", appId, Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");

        List<Incident> incidents = new ArrayList<>();
        incidents.add(i1);
        incidents.add(i2);

        when(mockIncidentService.listAllForApplication(appId)).thenReturn(incidents);

        MvcResult result = mockMvc.perform(get("/api/v1/incident/application/" + appId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("[{\"incidentId\":\"i1\",\"incidentDesc\":\"incident 1\",\"applicationId\":\"id123\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":0,\"mttrPerfLevel\":null},{\"incidentId\":\"i2\",\"incidentDesc\":\"incident 2\",\"applicationId\":\"id123\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":0,\"mttrPerfLevel\":null}]")));
        verify(mockIncidentService, times(1)).listAllForApplication(appId);
        }

    @Test
    void listForHierarchy() throws Exception
        {
        ZonedDateTime reportingDate = LocalDate.of(2020, 10, 10).atStartOfDay(ZoneId.of("UTC"));
        String appId = "id123";
        Incident i1 = new Incident("i1", "incident 1", appId, Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");
        Incident i2 = new Incident("i2", "incident 2", appId, Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");

        List<Incident> incidents = new ArrayList<>();
        incidents.add(i1);
        incidents.add(i2);

        when(mockIncidentService.listAllForHierarchy(appId)).thenReturn(incidents);

        MvcResult result = mockMvc.perform(get("/api/v1/incident/hierarchy/" + appId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("[{\"incidentId\":\"i1\",\"incidentDesc\":\"incident 1\",\"applicationId\":\"id123\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":0,\"mttrPerfLevel\":null},{\"incidentId\":\"i2\",\"incidentDesc\":\"incident 2\",\"applicationId\":\"id123\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":0,\"mttrPerfLevel\":null}]")));
        verify(mockIncidentService, times(1)).listAllForHierarchy(appId);
        }
    
    @Test
    void listByAppAndDate() throws Exception
        {
        ZonedDateTime reportingDate = LocalDate.of(2020, 10, 10).atStartOfDay(ZoneId.of("UTC"));
        String dateIn = DateTimeFormatter.ISO_LOCAL_DATE.format(reportingDate);
        String appId = "id123";
        Incident i1 = new Incident("i1", "incident 1", appId, Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");
        Incident i2 = new Incident("i2", "incident 2", appId, Date.from(reportingDate.toInstant()), Date.from(reportingDate.toInstant()), "test");

        List<Incident> incidents = new ArrayList<>();
        incidents.add(i1);
        incidents.add(i2);
        
        when(mockIncidentService.listAllForApplication(appId, Date.from(reportingDate.toInstant()))).thenReturn(incidents);
        
        MvcResult result = mockMvc.perform(get("/api/v1/incident/application/" + appId + "/date/" + dateIn)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        
        String content = result.getResponse().getContentAsString();
        verify(mockIncidentService, times(1)).listAllForApplication(appId, Date.from(reportingDate.toInstant()));
        assertThat(content, is(equalTo("[{\"incidentId\":\"i1\",\"incidentDesc\":\"incident 1\",\"applicationId\":\"id123\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":0,\"mttrPerfLevel\":null},{\"incidentId\":\"i2\",\"incidentDesc\":\"incident 2\",\"applicationId\":\"id123\",\"created\":\"2020-10-10T00:00:00.000+00:00\",\"resolved\":\"2020-10-10T00:00:00.000+00:00\",\"source\":\"test\",\"mttrSeconds\":0,\"mttrPerfLevel\":null}]")));

        }
    
    @Test
    void calcMttr() throws Exception
        {
        LocalDateTime date = LocalDate.now().minusDays(1).atStartOfDay();
        Date reportingDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        MTTR mttr = new MTTR(appId, reportingDate, 1200, 4, DORALevel.ELITE);
        when(mockIncidentService.calculateMTTR(appId, reportingDate)).thenReturn(mttr);
        
        MvcResult result = mockMvc.perform(get("/api/v1/incident/application/" + appId + "/mttr")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();
        
        String content = result.getResponse().getContentAsString();
        String dateOut = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        assertThat(content, is(equalTo("{\"applicationId\":\"" + appId + "\",\"reportingDate\":\"" + dateOut + "\",\"meanTimeToRecoverSeconds\":1200,\"incidentCount\":4,\"doraLevel\":\"ELITE\"}")));
        verify(mockIncidentService, times(1)).calculateMTTR(appId, reportingDate);
        }
    
    @Test
    void calcMttrByDate() throws Exception
        {
        LocalDateTime date = LocalDate.of(2020, Month.OCTOBER, 3).atStartOfDay();
        Date reportingDate = Date.from(date.toInstant(ZoneOffset.UTC));
        String appId = "a1";
        MTTR mttr = new MTTR(appId, reportingDate, 1200, 4, DORALevel.ELITE);
        when(mockIncidentService.calculateMTTR(appId, reportingDate)).thenReturn(mttr);
        String dateOut = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        assertThat(dateOut, is(equalTo("2020-10-03")));

        MvcResult result = mockMvc.perform(get("/api/v1/incident/application/" + appId + "/mttr/" + dateOut)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, is(equalTo("{\"applicationId\":\"" + appId + "\",\"reportingDate\":\"" + dateOut + "\",\"meanTimeToRecoverSeconds\":1200,\"incidentCount\":4,\"doraLevel\":\"ELITE\"}")));
        verify(mockIncidentService, times(1)).calculateMTTR(appId, reportingDate);
        }
    
    }