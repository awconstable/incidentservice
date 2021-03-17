package team.incidentservice.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api
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
        @ApiOperation(value = "Store a list of incidents", notes = "Store a list of incidents", response = Incident.class, responseContainer = "List")
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
        @ApiOperation(value = "List all incidents", notes = "List all incidents", response = Incident.class, responseContainer = "List")
        public List<Incident> list(){
            return incidentService.list();
        }

        @GetMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Get a specific incident specified by it's id", response = Incident.class)
        public Optional<Incident> show(@PathVariable @ApiParam(value = "The incident id", required = true) String id){
            return incidentService.get(id);
        }

        @GetMapping("/application/{id}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Get all incidents associated to an application id", response = Incident.class)
        public List<Incident> listForApp(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            return incidentService.listAllForApplication(id);
        }

        @GetMapping("/application/{id}/date/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Get all incidents associated to an application id for a specific date", response = Incident.class)
        public List<Incident> listForAppAndDate(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The incident date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            Date reportingDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            return incidentService.listAllForApplication(id, reportingDate);
        }

        @GetMapping("/application/{id}/mttr")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate mttr over the last 90 days for an application", response = Incident.class)
        public MTTR calculateMTTR(@PathVariable @ApiParam(value = "The application id", required = true) String id){
            ZonedDateTime date = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC);        
            Date reportingDate = Date.from(date.toInstant());
            return incidentService.calculateMTTR(id, reportingDate);
        }

        @GetMapping("/application/{id}/mttr/{date}")
        @ResponseStatus(HttpStatus.OK)
        @ApiOperation(value = "Calculate mttr over 90 days for an application from a given date", response = Incident.class)
        public MTTR calculateMTTR(@PathVariable @ApiParam(value = "The application id", required = true) String id, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "The incident date in ISO Date format YYYY-MM-dd", required = true) LocalDate date){
            Date reportingDate = Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            return incidentService.calculateMTTR(id, reportingDate);
        }
    }
