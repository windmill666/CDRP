package code;

import code.method.core.RandomSearch;
import code.method.core.ISearch;
import code.model.MIP;
import code.model.PrepareModel;
import code.util.ContainerStatus;
import ilog.concert.IloException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String inputPath = "data";
        String outputPath = "result";
        try {
            resolveProblem(inputPath, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void resolveProblem(String inputPath, String outputPath) throws IOException {
        int containerSum,stackHeight,stackLength,lowerBound,upperBound;

        int[][] containerBin;
        int[] retrieval4Model,storage4Model,restRetrieval4Model,restStorage4Model;

        makeDir(outputPath);
        String[] inputDirectories = (new File(inputPath)).list();
        assert inputDirectories != null;

        for (String inputDirectory : inputDirectories) {
            String[] split = inputDirectory.split("-");
            stackHeight = Integer.parseInt(split[0]);
            stackLength = Integer.parseInt(split[1]);
            containerSum = Integer.parseInt(split[2]);

            ISearch search = new RandomSearch(stackHeight, stackLength);
            PrepareModel prepareModel = new PrepareModel(stackHeight, stackLength);

            makeDir(outputPath + "/" + inputDirectory);
            String[] files = (new File(inputPath + "/" + inputDirectory)).list();
            assert files != null;

            for (String file : files) {
                PrintStream out = new PrintStream(new FileOutputStream(outputPath + "/" + inputDirectory + "/" + file));
                System.setOut(out);

                List<String> readAllLines = Files.readAllLines(Paths.get(inputPath, inputDirectory, file));

                prepareModel.prepare(readAllLines, containerSum);

                containerBin = prepareModel.getContainerBin();
                retrieval4Model = prepareModel.getRetrieval4Model();
                restRetrieval4Model = prepareModel.getRestRetrieval4Model();
                storage4Model = prepareModel.getStorage4Model();
                restStorage4Model = prepareModel.getRestStorage4Model();

                lowerBound = ContainerStatus.calGlobalBP(readAllLines, stackHeight, stackLength);
                upperBound = search.start(readAllLines, stackHeight, stackLength);
                System.out.println("下界：" + lowerBound);
                System.out.println("启发式算法求解的上界：" + upperBound);
                if (upperBound == 0) {
                    System.out.println("Total (root+branch&cut) =    0.00 sec. (0 ticks)\n" + "obj = 0.0");
                    continue;
                }
                if (upperBound == Integer.MAX_VALUE) {
                    System.out.println("Total (root+branch&cut) =    0.00 sec. (0 ticks)\n" + "MIP model not solved");
                    continue;
                }
                MIP model = new MIP(prepareModel.getContainerSum(), stackHeight, stackLength, 5, lowerBound, containerBin, retrieval4Model, restRetrieval4Model, storage4Model, restStorage4Model);

                try {
                    model.start();
                } catch (IloException e) {
                    e.printStackTrace();
                }
                out.close();
            }
        }

    }

    private static void makeDir(String path) {
        File fileOrDir = new File(path);
        if (!fileOrDir.exists()) {
            fileOrDir.mkdir();
        }
    }
}
