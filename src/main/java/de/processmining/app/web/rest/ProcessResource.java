package de.processmining.app.web.rest;

import de.processmining.app.domain.Process;
import de.processmining.app.service.ProcessService;
import de.processmining.app.web.rest.errors.BadRequestAlertException;
import de.processmining.app.service.dto.ProcessCriteria;
import de.processmining.app.service.ProcessQueryService;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.processmining.app.domain.Process}.
 */
@RestController
@RequestMapping("/api")
public class ProcessResource {

    private final Logger log = LoggerFactory.getLogger(ProcessResource.class);

    private static final String ENTITY_NAME = "process";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProcessService processService;

    private final ProcessQueryService processQueryService;

    public ProcessResource(ProcessService processService, ProcessQueryService processQueryService) {
        this.processService = processService;
        this.processQueryService = processQueryService;
    }

    /**
     * {@code POST  /processes} : Create a new process.
     *
     * @param process the process to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new process, or with status {@code 400 (Bad Request)} if the process has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/processes")
    public ResponseEntity<Process> createProcess(@Valid @RequestBody Process process) throws URISyntaxException {
        log.debug("REST request to save Process : {}", process);
        if (process.getId() != null) {
            throw new BadRequestAlertException("A new process cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Process result = processService.save(process);
        return ResponseEntity.created(new URI("/api/processes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /processes} : Updates an existing process.
     *
     * @param process the process to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated process,
     * or with status {@code 400 (Bad Request)} if the process is not valid,
     * or with status {@code 500 (Internal Server Error)} if the process couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/processes")
    public ResponseEntity<Process> updateProcess(@Valid @RequestBody Process process) throws URISyntaxException {
        log.debug("REST request to update Process : {}", process);
        if (process.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Process result = processService.save(process);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, process.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /processes} : get all the processes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of processes in body.
     */
    @GetMapping("/processes")
    public ResponseEntity<List<Process>> getAllProcesses(ProcessCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Processes by criteria: {}", criteria);
        Page<Process> page = processQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /processes/count} : count all the processes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/processes/count")
    public ResponseEntity<Long> countProcesses(ProcessCriteria criteria) {
        log.debug("REST request to count Processes by criteria: {}", criteria);
        return ResponseEntity.ok().body(processQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /processes/:id} : get the "id" process.
     *
     * @param id the id of the process to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the process, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/processes/{id}")
    public ResponseEntity<Process> getProcess(@PathVariable Long id) {
        log.debug("REST request to get Process : {}", id);
        Optional<Process> process = processService.findOne(id);
        return ResponseUtil.wrapOrNotFound(process);
    }

    /**
     * {@code DELETE  /processes/:id} : delete the "id" process.
     *
     * @param id the id of the process to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/processes/{id}")
    public ResponseEntity<Void> deleteProcess(@PathVariable Long id) {
        log.debug("REST request to delete Process : {}", id);
        processService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
