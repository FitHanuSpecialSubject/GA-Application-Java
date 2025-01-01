package com.example.SS2_Backend.util;

import com.example.SS2_Backend.constants.AppConst;
import com.example.SS2_Backend.ss.gt.implement.StandardGameTheoryProblem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;

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


@Slf4j
public class AlgorithmsBenchmarking {

    public static double run(Problem problem, String algo) {
        List<String> algorithms = Arrays.asList(AppConst.SUPPORTED_ALGOS);
        if (!algorithms.contains(algo)) {
            throw new IllegalArgumentException("Algorithm not supported: " + algo);
        }

//        System.out.println("\n[ Randomly Generated Population ]\n");
//        problem.printIndividuals();
//
//        System.out.println("Number Of Individuals: " + problem.getIndividuals().getNumberOfIndividual());
//
//        System.out.println("\n[ Algorithm Output Solution ]\n");

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

//        // Process and print the results
//        for (Solution solution : result) {
//            Matches matches = (Matches) solution.getAttribute("matches");
////            System.out.println("Output Matches (by Gale Shapley):\n" + matches.toString());
////            System.out.println("Randomized Individuals Input Order (by MOEA): " + solution.getVariable(0).toString());
//            System.out.println("Fitness Score: " + -solution.getObjective(0));
//            Testing tester = new Testing(matches,
//                    problem.getIndividuals().getNumberOfIndividual(),
//                    problem.getIndividuals().getCapacities());
//            System.out.println("Solution has duplicate individual? : " + tester.hasDuplicate());
//        }
        return runtime;
    }

    public static void main(String[] args) {

//        SampleDataGenerator generator = new SampleDataGenerator(10, 10);
//        String[] propNames = {"Prop1", "Prop2", "Prop3", "Prop4"};
//        generator.setPropNames(propNames);
//        generator.setSet1Cap(20);
//        generator.setSet2Cap(1000);
//        generator.setRandCapSet1(false);
//        generator.setRandCapSet2(false);
//        generator.setF1("none");
//        generator.setF2("none");
//        generator.setFnf("none");
//        // Generate the StableMatchingProblem instance
//        StableMatchingProblem problem = generator.generate();
//        String logFileName = "smt_log";

        String problemSerializedFilePath = ".data/gt_data.ser";
        String logFileName = "gt_log";
        StandardGameTheoryProblem problem = (StandardGameTheoryProblem) ProblemUtils.readProblemFromFile(
                problemSerializedFilePath);


        double runtime = run(problem, "OMOPSO");

        log.info("{}", runtime);
//        AlgorithmsBenchmarking algo = new AlgorithmsBenchmarking();
//        algo.start(problem, logFileName);

    }

    public void start(Problem problem) {
        String logFileName = "log";
        FastDateFormat dateFormat = FastDateFormat.getInstance("MMddHHss");
        String currentTimestamp = dateFormat.format(System.currentTimeMillis());
        logFileName = StringUtils.join(new String[]{logFileName, currentTimestamp}, "_");
        start(problem, logFileName);
    }

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
                    log.info("Execution time: {} Second(s) with Algorithm: {}",
                            runtime,
                            algorithms[i]);
                    return null;
                };
                Future<Void> future = threadPool.submit(callable);
                future.get(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed, Timed out for {}", algorithms[i]);
                runResults.add(new AlgorithmRunResult(algorithms[i], false, 0.0));
            } catch (Exception e) {
                log.error("Failed, Could not run with {}, message: {}",
                        algorithms[i],
                        e.getMessage());
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

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class AlgorithmRunResult {

        /** name */
        String algorithmName;
        /** run ability */
        boolean runnable;
        /** runtime in seconds */
        double runtime;

        public String[] toDataPoint() {

            String algorithmNameStr =
                    Objects.nonNull(this.algorithmName) ? this.algorithmName : "null";
            String runnableStr = runnable ? "true" : "false";
            String runTimeStr = (runtime < 0.0) ? "0.00" : String.format("%.2f", runtime);

            return new String[]{algorithmNameStr, runnableStr, runTimeStr};
        }

    }

}
