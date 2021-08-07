package code.method;

import code.method.core.RandomSearch;
import code.method.core.ISearch;
import code.util.ContainerStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SearchStarter {

    public static void main(String[] args) {
        String inputPath = "data";
        String outputPath = "method";
        try {
            resolveProblem(inputPath, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void resolveProblem(String inputPath, String outputPath) throws IOException {
        int stackHeight,stackLength,lowerBound,upperBound;

        makeDir(outputPath);
        PrintStream out = new PrintStream(new FileOutputStream(outputPath + "/" + "handleMethod.txt"));
        System.setOut(out);
        String[] inputDirectories = (new File(inputPath)).list();
        assert inputDirectories != null;

        for (String inputDirectory : inputDirectories) {
            StringBuilder lowerBoundRow = new StringBuilder();
            StringBuilder upperBoundRow = new StringBuilder();
            StringBuilder timeRow = new StringBuilder();

            String[] split = inputDirectory.split("-");
            stackHeight = Integer.parseInt(split[0]);
            stackLength = Integer.parseInt(split[1]);

            ISearch search = new RandomSearch(stackHeight, stackLength);
            String[] files = (new File(inputPath + "/" + inputDirectory)).list();
            assert files != null;

            for (String file : files) {
                List<String> readAllLines = Files.readAllLines(Paths.get(inputPath, inputDirectory, file));
                lowerBound = ContainerStatus.calGlobalBP(readAllLines, stackHeight, stackLength);
                long start = System.currentTimeMillis();
                upperBound = search.start(readAllLines, stackHeight, stackLength);
                long end = System.currentTimeMillis();
                lowerBoundRow.append(" ").append(lowerBound);
                upperBoundRow.append(" ").append(upperBound);
                timeRow.append(" ").append((end - start) / 1000.0);
            }
            System.out.println(inputDirectory + "lowerBound" + lowerBoundRow);
            System.out.println(inputDirectory + "upperBound" + upperBoundRow);
            System.out.println(inputDirectory + "time/s" + timeRow);
        }

        out.close();
    }

    private static void makeDir(String path) {
        File fileOrDir = new File(path);
        if (!fileOrDir.exists()) {
            fileOrDir.mkdir();
        }
    }
}
