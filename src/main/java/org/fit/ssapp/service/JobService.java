package org.fit.ssapp.service;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.dto.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Runs long solver requests in the background so that no single HTTP request has to stay open
 * until the computation finishes (reverse proxies such as Cloudflare drop requests that take
 * longer than about 120 seconds). Jobs are kept in memory and removed after a retention period.
 */
@Slf4j
@Service
public class JobService {

  private static final int MAX_RUNNING_JOBS = 2;
  private static final int MAX_QUEUED_JOBS = 10;
  private static final long RETENTION_MILLIS = TimeUnit.HOURS.toMillis(1);

  private final Map<String, Job> jobs = new ConcurrentHashMap<>();

  // Solvers already spread over all CPU cores, so only a few jobs may run at the same time.
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
      MAX_RUNNING_JOBS,
      MAX_RUNNING_JOBS,
      0L,
      TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(MAX_QUEUED_JOBS));

  /**
   * Job lifecycle states.
   */
  public enum Status {
    QUEUED, RUNNING, DONE, ERROR
  }

  /**
   * A background job and its outcome.
   */
  @Getter
  public static class Job {

    private final String id = UUID.randomUUID().toString();
    private final Instant createdAt = Instant.now();
    private volatile Status status = Status.QUEUED;
    private volatile Instant finishedAt;
    private volatile Response result;
  }

  /**
   * Queue a solver call.
   *
   * @param task the solver call, returns the same response the synchronous endpoint would.
   *
   * @return the created job, or empty when too many jobs are already waiting.
   */
  public Optional<Job> submit(Supplier<ResponseEntity<Response>> task) {
    purgeExpired();
    Job job = new Job();
    jobs.put(job.getId(), job);
    try {
      executor.execute(() -> run(job, task));
    } catch (RejectedExecutionException e) {
      jobs.remove(job.getId());
      log.warn("Job queue is full, rejecting new job");
      return Optional.empty();
    }
    return Optional.of(job);
  }

  /**
   * Look up a job by id.
   *
   * @param jobId job id.
   *
   * @return the job, or empty if it never existed or has expired.
   */
  public Optional<Job> find(String jobId) {
    return Optional.ofNullable(jobs.get(jobId));
  }

  private void run(Job job, Supplier<ResponseEntity<Response>> task) {
    job.status = Status.RUNNING;
    try {
      ResponseEntity<Response> response = task.get();
      job.result = response.getBody();
      job.status = Status.DONE;
    } catch (Exception e) {
      log.error("Job {} failed", job.getId(), e);
      job.result = Response.builder().status(500).message(e.getMessage()).build();
      job.status = Status.ERROR;
    } finally {
      job.finishedAt = Instant.now();
    }
  }

  private void purgeExpired() {
    long limit = System.currentTimeMillis() - RETENTION_MILLIS;
    jobs.values().removeIf(job -> job.getFinishedAt() != null
        && job.getFinishedAt().toEpochMilli() < limit);
  }

  @PreDestroy
  void shutdown() {
    executor.shutdownNow();
  }
}
