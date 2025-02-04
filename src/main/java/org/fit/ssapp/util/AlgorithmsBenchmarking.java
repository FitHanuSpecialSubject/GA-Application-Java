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


@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
public class AlgorithmsBenchmarking {

  @SuppressWarnings({"checkstyle:CommentsIndentation", "checkstyle:LineLength",
      "checkstyle:MissingJavadocMethod"})
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

  @SuppressWarnings({"checkstyle:LineLength", "checkstyle:MissingJavadocMethod"})
  public static void main(String[] args) {
    String problemSerializedFilePath = ".data/gt_data.ser";
    StandardGameTheoryProblem problem = (StandardGameTheoryProblem) ProblemUtils.readProblemFromFile(
        problemSerializedFilePath);

    double runtime = run(problem, "OMOPSO");

    log.info("{}", runtime);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static boolean logData(String path, String[][] data, CSVFormat format) {

    if (!SimpleFileUtils.isFileExist(path)) {
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

  private static boolean isValidDelimiterType(CSVFormat format) {
    return List.of(CSVFormat.DEFAULT, CSVFormat.TDF).contains(format);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void start(Problem problem) {
    String logFileName = "log";
    FastDateFormat dateFormat = FastDateFormat.getInstance("MMddHHss");
    String currentTimestamp = dateFormat.format(System.currentTimeMillis());
    logFileName = StringUtils.join(new String[]{logFileName, currentTimestamp}, "_");
    start(problem, logFileName);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
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

  @SuppressWarnings({"checkstyle:SummaryJavadoc", "checkstyle:MissingJavadocType"})
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class AlgorithmRunResult {

    /**
     * name
     */
    String algorithmName;
    /**
     * run ability
     */
    boolean runnable;
    /**
     * runtime in seconds
     */
    double runtime;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public String[] toDataPoint() {

      String algorithmNameStr = Objects.nonNull(this.algorithmName) ? this.algorithmName : "null";
      String runnableStr = runnable ? "true" : "false";
      String runTimeStr = (runtime < 0.0) ? "0.00" : String.format("%.2f", runtime);

      return new String[]{algorithmNameStr, runnableStr, runTimeStr};
    }

  }

}
