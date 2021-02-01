package de.processmining.app.web.rest;

import de.processmining.app.Process2021App;
import de.processmining.app.domain.Process;
import de.processmining.app.repository.ProcessRepository;
import de.processmining.app.service.ProcessService;
import de.processmining.app.service.dto.ProcessCriteria;
import de.processmining.app.service.ProcessQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ProcessResource} REST controller.
 */
@SpringBootTest(classes = Process2021App.class)
@AutoConfigureMockMvc
@WithMockUser
public class ProcessResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessQueryService processQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProcessMockMvc;

    private Process process;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Process createEntity(EntityManager em) {
        Process process = new Process()
            .title(DEFAULT_TITLE)
            .code(DEFAULT_CODE);
        return process;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Process createUpdatedEntity(EntityManager em) {
        Process process = new Process()
            .title(UPDATED_TITLE)
            .code(UPDATED_CODE);
        return process;
    }

    @BeforeEach
    public void initTest() {
        process = createEntity(em);
    }

    @Test
    @Transactional
    public void createProcess() throws Exception {
        int databaseSizeBeforeCreate = processRepository.findAll().size();
        // Create the Process
        restProcessMockMvc.perform(post("/api/processes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(process)))
            .andExpect(status().isCreated());

        // Validate the Process in the database
        List<Process> processList = processRepository.findAll();
        assertThat(processList).hasSize(databaseSizeBeforeCreate + 1);
        Process testProcess = processList.get(processList.size() - 1);
        assertThat(testProcess.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testProcess.getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    @Transactional
    public void createProcessWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = processRepository.findAll().size();

        // Create the Process with an existing ID
        process.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProcessMockMvc.perform(post("/api/processes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(process)))
            .andExpect(status().isBadRequest());

        // Validate the Process in the database
        List<Process> processList = processRepository.findAll();
        assertThat(processList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = processRepository.findAll().size();
        // set the field null
        process.setTitle(null);

        // Create the Process, which fails.


        restProcessMockMvc.perform(post("/api/processes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(process)))
            .andExpect(status().isBadRequest());

        List<Process> processList = processRepository.findAll();
        assertThat(processList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllProcesses() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList
        restProcessMockMvc.perform(get("/api/processes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(process.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)));
    }
    
    @Test
    @Transactional
    public void getProcess() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get the process
        restProcessMockMvc.perform(get("/api/processes/{id}", process.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(process.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE));
    }


    @Test
    @Transactional
    public void getProcessesByIdFiltering() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        Long id = process.getId();

        defaultProcessShouldBeFound("id.equals=" + id);
        defaultProcessShouldNotBeFound("id.notEquals=" + id);

        defaultProcessShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultProcessShouldNotBeFound("id.greaterThan=" + id);

        defaultProcessShouldBeFound("id.lessThanOrEqual=" + id);
        defaultProcessShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllProcessesByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where title equals to DEFAULT_TITLE
        defaultProcessShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the processList where title equals to UPDATED_TITLE
        defaultProcessShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllProcessesByTitleIsNotEqualToSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where title not equals to DEFAULT_TITLE
        defaultProcessShouldNotBeFound("title.notEquals=" + DEFAULT_TITLE);

        // Get all the processList where title not equals to UPDATED_TITLE
        defaultProcessShouldBeFound("title.notEquals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllProcessesByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultProcessShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the processList where title equals to UPDATED_TITLE
        defaultProcessShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllProcessesByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where title is not null
        defaultProcessShouldBeFound("title.specified=true");

        // Get all the processList where title is null
        defaultProcessShouldNotBeFound("title.specified=false");
    }
                @Test
    @Transactional
    public void getAllProcessesByTitleContainsSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where title contains DEFAULT_TITLE
        defaultProcessShouldBeFound("title.contains=" + DEFAULT_TITLE);

        // Get all the processList where title contains UPDATED_TITLE
        defaultProcessShouldNotBeFound("title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllProcessesByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where title does not contain DEFAULT_TITLE
        defaultProcessShouldNotBeFound("title.doesNotContain=" + DEFAULT_TITLE);

        // Get all the processList where title does not contain UPDATED_TITLE
        defaultProcessShouldBeFound("title.doesNotContain=" + UPDATED_TITLE);
    }


    @Test
    @Transactional
    public void getAllProcessesByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where code equals to DEFAULT_CODE
        defaultProcessShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the processList where code equals to UPDATED_CODE
        defaultProcessShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProcessesByCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where code not equals to DEFAULT_CODE
        defaultProcessShouldNotBeFound("code.notEquals=" + DEFAULT_CODE);

        // Get all the processList where code not equals to UPDATED_CODE
        defaultProcessShouldBeFound("code.notEquals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProcessesByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where code in DEFAULT_CODE or UPDATED_CODE
        defaultProcessShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the processList where code equals to UPDATED_CODE
        defaultProcessShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProcessesByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where code is not null
        defaultProcessShouldBeFound("code.specified=true");

        // Get all the processList where code is null
        defaultProcessShouldNotBeFound("code.specified=false");
    }
                @Test
    @Transactional
    public void getAllProcessesByCodeContainsSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where code contains DEFAULT_CODE
        defaultProcessShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the processList where code contains UPDATED_CODE
        defaultProcessShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProcessesByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        processRepository.saveAndFlush(process);

        // Get all the processList where code does not contain DEFAULT_CODE
        defaultProcessShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the processList where code does not contain UPDATED_CODE
        defaultProcessShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProcessShouldBeFound(String filter) throws Exception {
        restProcessMockMvc.perform(get("/api/processes?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(process.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)));

        // Check, that the count call also returns 1
        restProcessMockMvc.perform(get("/api/processes/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProcessShouldNotBeFound(String filter) throws Exception {
        restProcessMockMvc.perform(get("/api/processes?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProcessMockMvc.perform(get("/api/processes/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingProcess() throws Exception {
        // Get the process
        restProcessMockMvc.perform(get("/api/processes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProcess() throws Exception {
        // Initialize the database
        processService.save(process);

        int databaseSizeBeforeUpdate = processRepository.findAll().size();

        // Update the process
        Process updatedProcess = processRepository.findById(process.getId()).get();
        // Disconnect from session so that the updates on updatedProcess are not directly saved in db
        em.detach(updatedProcess);
        updatedProcess
            .title(UPDATED_TITLE)
            .code(UPDATED_CODE);

        restProcessMockMvc.perform(put("/api/processes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedProcess)))
            .andExpect(status().isOk());

        // Validate the Process in the database
        List<Process> processList = processRepository.findAll();
        assertThat(processList).hasSize(databaseSizeBeforeUpdate);
        Process testProcess = processList.get(processList.size() - 1);
        assertThat(testProcess.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testProcess.getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    @Transactional
    public void updateNonExistingProcess() throws Exception {
        int databaseSizeBeforeUpdate = processRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProcessMockMvc.perform(put("/api/processes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(process)))
            .andExpect(status().isBadRequest());

        // Validate the Process in the database
        List<Process> processList = processRepository.findAll();
        assertThat(processList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProcess() throws Exception {
        // Initialize the database
        processService.save(process);

        int databaseSizeBeforeDelete = processRepository.findAll().size();

        // Delete the process
        restProcessMockMvc.perform(delete("/api/processes/{id}", process.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Process> processList = processRepository.findAll();
        assertThat(processList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
