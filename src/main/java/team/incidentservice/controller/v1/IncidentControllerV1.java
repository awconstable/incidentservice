package team.incidentservice.controller.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import team.incidentservice.model.Incident;
import team.incidentservice.model.MTTR;
import team.incidentservice.service.IncidentService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.*;
import java.util.*;

@Validated
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
        public List<Incident> store(@RequestBody @NotEmpty(message = "Input incident list cannot be empty.") List<@Valid Incident> incidents){
                List<Incident> output = new ArrayList<>();
                incidents.forEach(i -> output.add(incidentService.store(i)));
                return output;
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity handle(ConstraintViolationException constraintViolationException) {
            Set<ConstraintViolation<?>> violations = constraintViolationException.getConstraintViolations();
            String errorMessage = "";
            if (!violations.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                violations.forEach(violation -> builder.append(" " + violation.getMessage()));
                errorMessage = builder.toString();
            } else {
                errorMessage = "ConstraintViolationException occurred.";
            }
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
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

        @GetMapping("/application/{id}/mttr/{date}")
        @ResponseStatus(HttpStatus.OK)
        public MTTR calculateMTTR(@PathVariable String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
            Date reportingDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            return incidentService.calculateMTTR(id, reportingDate);
        }
    }
