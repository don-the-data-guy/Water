package water.api;

import water.*;
import water.api.schemas3.JobV3;
import water.api.schemas3.JobsV3;
import water.api.schemas4.input.JobIV4;
import water.api.schemas4.output.JobV4;
import water.exceptions.H2ONotFoundArgumentException;
import water.server.ServletUtils;
import water.util.Log;

public class JobsHandler extends Handler {
  /** Impl class for a collection of jobs; only used in the API to make it easier to cons up the jobs array via the magic of PojoUtils.copyProperties.  */

  @SuppressWarnings("unused") // called through reflection by RequestServer
  public JobsV3 list(int version, JobsV3 s) {
    Job[] jobs = Job.jobs();
    // Jobs j = new Jobs();
    // j._jobs = Job.jobs();
    // PojoUtils.copyProperties(s, j, PojoUtils.FieldNaming.ORIGIN_HAS_UNDERSCORES);
    s.jobs = new JobV3[jobs.length];

    int i = 0;
    for (Job j : jobs) {
      try { s.jobs[i] = (JobV3) SchemaServer.schema(version, j).fillFromImpl(j); }
      // no special schema for this job subclass, so fall back to JobV3
      catch (H2ONotFoundArgumentException e) { s.jobs[i] = new JobV3().fillFromImpl(j); }
      i++; // Java does the increment before the function call which throws?!
    }
    return s;
  }

  @SuppressWarnings("unused") // called through reflection by RequestServer
  public JobsV3 fetch(int version, JobsV3 s) {
    Key<Job> key = s.job_id.key();

    long waitingStartedAt = System.currentTimeMillis();
    long waitMs = fetchJobTimeoutMs();
    Job<?> j = Job.tryGetDoneJob(key, waitMs);
    long waitingEndedAt = System.currentTimeMillis();
    if (Log.isLoggingFor(Log.TRACE)) {
      Log.trace("Waited for job result for " + (waitingEndedAt - waitingStartedAt) + "ms.");
    }

    JobV3 job;
    try {
      job = (JobV3) SchemaServer.schema(version, j);
    } catch (H2ONotFoundArgumentException e) { // no special schema for this job subclass, so fall back to JobV3
      job = new JobV3().fillFromImpl(j);
    }
    job.fillFromImpl(j);

    s.jobs = new JobV3[1];
    s.jobs[0] = job;
    return s;
  }

  static long fetchJobTimeoutMs() {
    String timeoutSpec = ServletUtils.getSessionProperty("job.fetch_timeout_ms", null);
    if (timeoutSpec == null) {
      return -1;
    }
    try {
      return Long.parseLong(timeoutSpec);
    } catch (Exception e) {
      Log.trace(e);
      return -1;
    }
  }

  public JobsV3 cancel(int version, JobsV3 c) {
    Job j = DKV.getGet(c.job_id.key());
    if (j == null) {
      throw new IllegalArgumentException("No job with key " + c.job_id.key());
    }
    j.stop(); // Request Job stop
    long start = System.currentTimeMillis();
    Log.info("Waiting for job " + c.job_id.key() + " to finish execution.");
    try {
      j.get(); // Wait for Job to complete 
    } catch (Exception e) {
      if (! Job.isCancelledException(e)) {
        Log.warn("Job was cancelled with exception", e);
      }
    }
    long took = System.currentTimeMillis() - start;
    Log.info("Job " + c.job_id.key() + " cancelled (waiting took=" + took + "ms).");
    return c;
  }


  public static class FetchJob extends RestApiHandler<JobIV4, JobV4> {

    @Override public String name() {
      return "getJob4";
    }

    @Override public String help() {
      return "Retrieve information about the current state of a job.";
    }

    @Override
    public JobV4 exec(int ignored, JobIV4 input) {
      Key<Job> key = Key.make(input.job_id);
      Value val = DKV.get(key);
      if (val == null)
        throw new IllegalArgumentException("Job " + input.job_id + " is missing");
      Iced iced = val.get();
      if (!(iced instanceof Job))
        throw new IllegalArgumentException("Id " + input.job_id + " references a " + iced.getClass() + " not a Job");

      Job job = (Job) iced;
      JobV4 out = new JobV4();
      out.fillFromImpl(job);
      return out;
    }
  }
}
