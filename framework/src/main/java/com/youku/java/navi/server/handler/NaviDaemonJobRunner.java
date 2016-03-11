package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.module.INaviModuleContext;
import com.youku.java.navi.server.module.NaviModuleContextFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.launch.support.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
public class NaviDaemonJobRunner {


    private ExitCodeMapper exitCodeMapper = new SimpleJvmExitCodeMapper();

    private JobLauncher launcher;

    private JobLocator jobLocator;

    // Package private for unit test
    private static SystemExiter systemExiter = new JvmSystemExiter();

    private static String message = "";

    private JobParametersConverter jobParametersConverter = new DefaultJobParametersConverter();

    private JobExplorer jobExplorer;

    private JobRepository jobRepository;


    /**
     * Injection setter for the {@link JobLauncher}.
     *
     * @param launcher
     *     the launcher to set
     */
    public void setLauncher(JobLauncher launcher) {
        this.launcher = launcher;
    }

    /**
     * @param jobRepository
     *     the jobRepository to set
     */
    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Injection setter for {@link JobExplorer}.
     *
     * @param jobExplorer
     *     the {@link JobExplorer} to set
     */
    public void setJobExplorer(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    /**
     * Injection setter for the {@link ExitCodeMapper}.
     *
     * @param exitCodeMapper
     *     the exitCodeMapper to set
     */
    public void setExitCodeMapper(ExitCodeMapper exitCodeMapper) {
        this.exitCodeMapper = exitCodeMapper;
    }

    /**
     * Static setter for the {@link SystemExiter} so it can be adjusted before
     * dependency injection. Typically overridden by
     * {@link #setSystemExiter(SystemExiter)}.
     */
    public static void presetSystemExiter(SystemExiter systemExiter) {
        NaviDaemonJobRunner.systemExiter = systemExiter;
    }

    /**
     * Retrieve the error message set by an instance of
     * {@link CommandLineJobRunner} as it exits. Empty if the last job launched
     * was successful.
     *
     * @return the error message
     */
    public static String getErrorMessage() {
        return message;
    }

    /**
     * Injection setter for the {@link SystemExiter}.
     */
    public void setSystemExiter(SystemExiter systemExiter) {
        NaviDaemonJobRunner.systemExiter = systemExiter;
    }

    /**
     * Injection setter for {@link JobParametersConverter}.
     *
     * @param jobParametersConverter
     */
    public void setJobParametersConverter(JobParametersConverter jobParametersConverter) {
        this.jobParametersConverter = jobParametersConverter;
    }

    /**
     * Delegate to the exiter to (possibly) exit the VM gracefully.
     *
     * @param status
     */
    public void exit(int status) {
        systemExiter.exit(status);
    }

    /**
     * {@link JobLocator} to find a job to run.
     *
     * @param jobLocator
     *     a {@link JobLocator}
     */
    public void setJobLocator(JobLocator jobLocator) {
        this.jobLocator = jobLocator;
    }

    /*
     * Start a job by obtaining a combined classpath using the job launcher and
     * job paths. If a JobLocator has been set, then use it to obtain an actual
     * job, if not ask the context for it.
     */
    public int start(String moduleNm, String jobIdentifier, String[] parameters, Set<String> opts) throws Exception {

        INaviModuleContext context = null;

        try {
            context = NaviModuleContextFactory.getInstance().getNaviModuleContext(moduleNm);
            launcher = (JobLauncher) context.getBean("jobLauncher");
            jobExplorer = (JobExplorer) context.getBean("jobExplorer");
            jobRepository = (JobRepository) context.getBean("jobRepository");

            Assert.state(launcher != null, "A JobLauncher must be provided.  Please add one to the configuration.");
            if (opts.contains("-restart") || opts.contains("-next")) {
                Assert.state(jobExplorer != null,
                    "A JobExplorer must be provided for a restart or start next operation.  Please add one to the configuration.");
            }

            String jobName = moduleNm + "_" + jobIdentifier;

            JobParameters jobParameters = jobParametersConverter.getJobParameters(StringUtils
                .splitArrayElementsIntoProperties(parameters, "="));
            Assert.isTrue(parameters == null || parameters.length == 0 || !jobParameters.isEmpty(),
                "Invalid JobParameters " + Arrays.asList(parameters)
                    + ". If parameters are provided they should be in the form name=value (no whitespace).");

            if (opts.contains("-stop")) {
                List<JobExecution> jobExecutions = getRunningJobExecutions(jobName);
                if (jobExecutions == null) {
                    throw new JobExecutionNotRunningException("No running execution found for job=" + jobName);
                }
                for (JobExecution jobExecution : jobExecutions) {
                    jobExecution.setStatus(BatchStatus.STOPPING);
                    jobRepository.update(jobExecution);
                }
                return exitCodeMapper.intValue(ExitStatus.COMPLETED.getExitCode());
            }

            if (opts.contains("-abandon")) {
                List<JobExecution> jobExecutions = getStoppedJobExecutions(jobName);
                if (jobExecutions == null) {
                    throw new JobExecutionNotStoppedException("No stopped execution found for job=" + jobName);
                }
                for (JobExecution jobExecution : jobExecutions) {
                    jobExecution.setStatus(BatchStatus.ABANDONED);
                    jobRepository.update(jobExecution);
                }
                return exitCodeMapper.intValue(ExitStatus.COMPLETED.getExitCode());
            }

            if (opts.contains("-restart")) {
                JobExecution jobExecution = getLastFailedJobExecution(jobName);
                if (jobExecution == null) {
                    throw new JobExecutionNotFailedException("No failed or stopped execution found for job="
                        + jobName);
                }
                jobParameters = jobExecution.getJobInstance().getJobParameters();
                jobName = jobExecution.getJobInstance().getJobName();
            }

            Job job;
            if (jobLocator != null) {
                job = jobLocator.getJob(jobIdentifier);
            } else {
                job = (Job) context.getBean(jobIdentifier);
                AbstractJob tmptJob = (AbstractJob) job;
                //重写jobNm
                tmptJob.setName(jobName);
            }

            if (opts.contains("-next")) {
                JobParameters nextParameters = getNextJobParameters(job);
                Map<String, JobParameter> map = new HashMap<String, JobParameter>(nextParameters.getParameters());
                map.putAll(jobParameters.getParameters());
                jobParameters = new JobParameters(map);
            }

            JobExecution jobExecution = launcher.run(job, jobParameters);
            return exitCodeMapper.intValue(jobExecution.getExitStatus().getExitCode());

        } catch (Throwable e) {
            String message = "Job Terminated in error: " + e.getMessage();
            log.error(message, e);
            NaviDaemonJobRunner.message = message;
            return exitCodeMapper.intValue(ExitStatus.FAILED.getExitCode());
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    /**
     * @param jobIdentifier
     *     a job execution id or job name
     * @param minStatus
     *     the highest status to exclude from the result
     * @return
     */
    private List<JobExecution> getJobExecutionsWithStatusGreaterThan(String jobIdentifier, BatchStatus minStatus) {

        Long executionId = getLongIdentifier(jobIdentifier);
        if (executionId != null) {
            JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
            if (jobExecution.getStatus().isGreaterThan(minStatus)) {
                return Arrays.asList(jobExecution);
            }
            return Collections.emptyList();
        }

        int start = 0;
        int count = 100;
        List<JobExecution> executions = new ArrayList<JobExecution>();
        List<JobInstance> lastInstances = jobExplorer.getJobInstances(jobIdentifier, start, count);

        while (!lastInstances.isEmpty()) {

            for (JobInstance jobInstance : lastInstances) {
                List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
                if (jobExecutions == null || jobExecutions.isEmpty()) {
                    continue;
                }
                for (JobExecution jobExecution : jobExecutions) {
                    if (jobExecution.getStatus().isGreaterThan(minStatus)) {
                        executions.add(jobExecution);
                    }
                }
            }

            start += count;
            lastInstances = jobExplorer.getJobInstances(jobIdentifier, start, count);

        }

        return executions;

    }

    private JobExecution getLastFailedJobExecution(String jobIdentifier) {
        List<JobExecution> jobExecutions = getJobExecutionsWithStatusGreaterThan(jobIdentifier, BatchStatus.STOPPING);
        if (jobExecutions.isEmpty()) {
            return null;
        }
        return jobExecutions.get(0);
    }

    private List<JobExecution> getStoppedJobExecutions(String jobIdentifier) {
        List<JobExecution> jobExecutions = getJobExecutionsWithStatusGreaterThan(jobIdentifier, BatchStatus.STARTED);
        if (jobExecutions.isEmpty()) {
            return null;
        }
        List<JobExecution> result = new ArrayList<JobExecution>();
        for (JobExecution jobExecution : jobExecutions) {
            if (jobExecution.getStatus() != BatchStatus.ABANDONED) {
                result.add(jobExecution);
            }
        }
        return result.isEmpty() ? null : result;
    }

    private List<JobExecution> getRunningJobExecutions(String jobIdentifier) {
        List<JobExecution> jobExecutions = getJobExecutionsWithStatusGreaterThan(jobIdentifier, BatchStatus.COMPLETED);
        if (jobExecutions.isEmpty()) {
            return null;
        }
        List<JobExecution> result = new ArrayList<JobExecution>();
        for (JobExecution jobExecution : jobExecutions) {
            if (jobExecution.isRunning()) {
                result.add(jobExecution);
            }
        }
        return result.isEmpty() ? null : result;
    }

    private Long getLongIdentifier(String jobIdentifier) {
        try {
            return new Long(jobIdentifier);
        } catch (NumberFormatException e) {
            // Not an ID - must be a name
            return null;
        }
    }

    /**
     * @param job
     *     the job that we need to find the next parameters for
     * @return the next job parameters if they can be located
     * @throws JobParametersNotFoundException
     *     if there is a problem
     */
    private JobParameters getNextJobParameters(Job job) throws JobParametersNotFoundException {
        String jobIdentifier = job.getName();
        JobParameters jobParameters;
        List<JobInstance> lastInstances = jobExplorer.getJobInstances(jobIdentifier, 0, 1);

        JobParametersIncrementer incrementer = job.getJobParametersIncrementer();
        if (incrementer == null) {
            throw new JobParametersNotFoundException("No job parameters incrementer found for job=" + jobIdentifier);
        }

        if (lastInstances.isEmpty()) {
            jobParameters = incrementer.getNext(new JobParameters());
            if (jobParameters == null) {
                throw new JobParametersNotFoundException("No bootstrap parameters found from incrementer for job="
                    + jobIdentifier);
            }
        } else {
            jobParameters = incrementer.getNext(lastInstances.get(0).getJobParameters());
        }
        return jobParameters;
    }


}
