package de.processmining.app.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import de.processmining.app.domain.Process;
import de.processmining.app.domain.*; // for static metamodels
import de.processmining.app.repository.ProcessRepository;
import de.processmining.app.service.dto.ProcessCriteria;

/**
 * Service for executing complex queries for {@link Process} entities in the database.
 * The main input is a {@link ProcessCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Process} or a {@link Page} of {@link Process} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ProcessQueryService extends QueryService<Process> {

    private final Logger log = LoggerFactory.getLogger(ProcessQueryService.class);

    private final ProcessRepository processRepository;

    public ProcessQueryService(ProcessRepository processRepository) {
        this.processRepository = processRepository;
    }

    /**
     * Return a {@link List} of {@link Process} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Process> findByCriteria(ProcessCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Process> specification = createSpecification(criteria);
        return processRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Process} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Process> findByCriteria(ProcessCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Process> specification = createSpecification(criteria);
        return processRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ProcessCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Process> specification = createSpecification(criteria);
        return processRepository.count(specification);
    }

    /**
     * Function to convert {@link ProcessCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Process> createSpecification(ProcessCriteria criteria) {
        Specification<Process> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Process_.id));
            }
            if (criteria.getTitle() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTitle(), Process_.title));
            }
            if (criteria.getCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCode(), Process_.code));
            }
        }
        return specification;
    }
}
