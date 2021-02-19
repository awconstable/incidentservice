package team.incidentservice.controller.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import team.incidentservice.model.Incident;
import team.incidentservice.model.MTTR;
import team.incidentservice.service.IncidentService;

import javax.validation.Valid;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/incident", produces = "application/json")
public class IncidentControllerV1
    {
    
    private final IncidentService incidentService;

    @Autowired
    public IncidentControllerV1(IncidentService incidentService)
        {
        this.incidentService = incidentService;
        }

        @PostMapping("")
        @ResponseStatus(HttpStatus.CREATED)
        public List<Incident> store(@Valid @RequestBody List<Incident> incidents){
                List<Incident> output = new ArrayList<>();
                incidents.forEach(i -> output.add(incidentService.store(i)));
                return output;
        }

        @GetMapping("")
        @ResponseStatus(HttpStatus.OK)
        public List<Incident> list(){
            return incidentService.list();
        }

        @GetMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        public Optional<Incident> show(@PathVariable String id){
            return incidentService.get(id);
        }

        @GetMapping("/application/{id}")
        @ResponseStatus(HttpStatus.OK)
        public List<Incident> listForApp(@PathVariable String id){
            return incidentService.listAllForApplication(id);
        }

        @GetMapping("/application/{id}/date/{date}")
        @ResponseStatus(HttpStatus.OK)
        public List<Incident> listForAppAndDate(@PathVariable String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
            Date reportingDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            return incidentService.listAllForApplication(id, reportingDate);
        }

        @GetMapping("/application/{id}/mttr")
        @ResponseStatus(HttpStatus.OK)
        public MTTR calculateMTTR(@PathVariable String id){
            ZonedDateTime date = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC);        
            Date reportingDate = Date.from(date.toInstant());
            return incidentService.calculateMTTR(id, reportingDate);
        }
    }
