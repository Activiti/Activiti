package org.activiti.cycle.service;

import java.util.List;

import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.ProcessSolutionTemplate;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.engine.identity.User;

/**
 * Cycle-serivce for managing {@link ProcessSolution}s and
 * {@link VirtualRepositoryFolder}s.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface CycleProcessSolutionService {

  /**
   * @param the
   *          id of the {@link ProcessSolution} to be returned.
   * @return the {@link ProcessSolution} with the given id.
   */
  public ProcessSolution getProcessSolutionById(String id);

  /**
   * 
   * @return a {@link List} of all available {@link ProcessSolution}s
   */
  public List<ProcessSolution> getProcessSolutions();

  /**
   * Create a new process solution based on the default configuration
   * 
   * @param the
   *          name of the new process solution
   * @return the id of the new processSolution
   */
  public String createNewProcessSolution(String name);

  /**
   * @param the
   *          id of the {@link ProcessSolution} to return the list of
   *          {@link VirtualRepositoryFolder}s for.
   * 
   * @return a {@link List} of {@link VirtualRepositoryFolder}s for the given
   *         {@link ProcessSolution}.
   */
  public List<VirtualRepositoryFolder> getFoldersForProcessSolution(String id);

  /**
   * @return the {@link VirtualRepositoryFolder} corresponding to the provided
   *         id.
   */
  public VirtualRepositoryFolder getVirtualRepositoryFolderById(String id);

  /**
   * @return the default {@link ProcessSolutionTemplate}.
   */
  public ProcessSolutionTemplate getDefaultProcessSolutionTemplate();

  /**
   * Update a {@link ProcessSolution}
   */
  public ProcessSolution updateProcessSolution(ProcessSolution processSolution);

  public List<User> getProcessSolutionCollaborators(String processSolutionId, String processSolutionCollaboratorRole);
}
