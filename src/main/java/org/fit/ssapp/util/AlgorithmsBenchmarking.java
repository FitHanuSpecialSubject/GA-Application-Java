package org.fit.ssapp.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.ss.gt.implement.StandardGameTheoryProblem;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;

/**
 * This class benchmarks various algorithms for solving game theory problems, logs their execution
 * time, and outputs the results in CSV format. It also handles the configuration and management of
 * multiple algorithms and their execution in parallel.
 */
@Slf4j
public class AlgorithmsBenchmarking {

  /**
   * Runs the specified algorithm on a given problem and returns the execution time.
   *
   * @param problem the problem to solve
   * @param algo    the algorithm to use
   * @return the runtime of the algorithm in seconds
   * @throws IllegalArgumentException if the algorithm is not supported
   */
  public static double run(Problem problem, String algo) {
    List<String> algorithms = Arrays.asList(AppConst.SUPPORTED_ALGOS);
    if (!algorithms.contains(algo)) {
      throw new IllegalArgumentException("Algorithm not supported: " + algo);
    }

    // Run the algorithm
    long startTime = System.currentTimeMillis();
    NondominatedPopulation result = new Executor()
        .withProblem(problem)
        .withAlgorithm(algo)
        .withMaxEvaluations(100)
        .withProperty("populationSize", 1000)
        .distributeOnAllCores()
        .run();
    long endTime = System.currentTimeMillis();
    double runtime = ((double) (endTime - startTime) / 1000);
    runtime = Math.round(runtime * 100.0) / 100.0;

    log.info(result.iterator().next().toString());
    return runtime;
  }

  /**
   * The main method to initialize and run the benchmarking process for the algorithms. It reads a
   * serialized game theory problem, runs the specified algorithm, and logs the runtime.
   *
   * @param args the command-line arguments
   */
  public static void main(String[] args) {
    String problemSerializedFilePath = ".data/gt_data.ser";
    StandardGameTheoryProblem problem = (StandardGameTheoryProblem) ProblemUtils
        .readProblemFromFile(
            problemSerializedFilePath);

    double runtime = run(problem, "OMOPSO");

    log.info("{}", runtime);
  }

  /**
   * Logs the given data to a file in CSV format.
   *
   * @param path   the file path where the data should be saved
   * @param data   the data to be written to the file
   * @param format the CSV format to use
   * @return true if the data was written successfully, false otherwise
   */
  public static boolean logData(String path, String[][] data, CSVFormat format) {

    if (!SimpleFileUtils.isFileNotExist(path)) {
      try {
        FileUtils.touch(new File(path));
      } catch (IOException e) {
        log.error("e: ", e);
        return false;
      }
    }
    try (FileWriter writer = new FileWriter(path); CSVPrinter printer = new CSVPrinter(writer,
        format)) {

      if (!isValidDelimiterType(format)) {
        throw new IllegalArgumentException("Format not supported: " + format);
      }

      for (String[] row : data) {
        printer.printRecord((Object[]) row);
      }
      return true;

    } catch (IOException e) {
      log.error("e: ", e);
      return false;
    }
  }

  /**
   * Checks whether the given CSV format is supported.
   *
   * @param format the CSV format to check
   * @return true if the format is supported, false otherwise
   */
  private static boolean isValidDelimiterType(CSVFormat format) {
    return List.of(CSVFormat.DEFAULT, CSVFormat.TDF).contains(format);
  }

  /**
   * Starts the benchmarking process with a default log file name.
   *
   * @param problem the problem to solve
   */
  public void start(Problem problem) {
    String logFileName = "log";
    FastDateFormat dateFormat = FastDateFormat.getInstance("MMddHHss");
    String currentTimestamp = dateFormat.format(System.currentTimeMillis());
    logFileName = StringUtils.join(new String[]{logFileName, currentTimestamp}, "_");
    start(problem, logFileName);
  }

  /**
   * Starts the benchmarking process and logs the results to a specified log file.
   *
   * @param problem     the problem to solve
   * @param logFileName the name of the log file
   */
  public void start(Problem problem, String logFileName) {
    String[] algorithms = AppConst.SUPPORTED_ALGOS;
    List<AlgorithmRunResult> runResults = new ArrayList<>();

    ExecutorService threadPool = Executors.newCachedThreadPool();

    IntStream.range(0, algorithms.length).forEach(i -> {
      try {
        log.info("Start running with algorithm {}", algorithms[i]);
        Callable<Void> callable = () -> {
          double runtime = run(problem, algorithms[i]);
          runResults.add(new AlgorithmRunResult(algorithms[i], true, runtime));
          log.info("Execution time: {} Second(s) with Algorithm: {}", runtime, algorithms[i]);
          return null;
        };
        Future<Void> future = threadPool.submit(callable);
        future.get(20, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Failed, Timed out for {}", algorithms[i]);
        runResults.add(new AlgorithmRunResult(algorithms[i], false, 0.0));
      } catch (Exception e) {
        log.error("Failed, Could not run with {}, message: {}", algorithms[i], e.getMessage());
        runResults.add(new AlgorithmRunResult(algorithms[i], false, 0.0));
      }
    });

    threadPool.shutdown();
    System.out.println("algo " + algorithms.length + ", results" + runResults.size());
    System.out.println("benchmark complete");
    System.out.println(runResults);

    String[][] data = runResults
        .stream()
        .map(AlgorithmRunResult::toDataPoint)
        .toArray(String[][]::new);

    String logFilePath = SimpleFileUtils.getFilePath(AppConst.LOG_DIR,
        logFileName,
        AppConst.TSV_EXT);
    if (logData(logFilePath, data, CSVFormat.TDF)) {
      log.info("write log to {} success", logFilePath);
    } else {
      log.error("write log to {} failed", logFilePath);
    }
    System.exit(0);
  }

  /**
   * Class representing the results of an algorithm run.
   */
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class AlgorithmRunResult {

    /**
     * The name of the algorithm.
     */
    String algorithmName;
    /**
     * Indicates whether the algorithm was successfully executed.
     */
    boolean runnable;
    /**
     * The runtime of the algorithm in seconds.
     */
    double runtime;

    /**
     * Converts the algorithm run result to a data point for logging.
     *
     * @return an array of strings representing the algorithm's result
     */
    public String[] toDataPoint() {

      String algorithmNameStr = Objects.nonNull(this.algorithmName) ? this.algorithmName : "null";
      String runnableStr = runnable ? "true" : "false";
      String runTimeStr = (runtime < 0.0) ? "0.00" : String.format("%.2f", runtime);

      return new String[]{algorithmNameStr, runnableStr, runTimeStr};
    }

  }

}
