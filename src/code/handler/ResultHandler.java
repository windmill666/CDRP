package code.handler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ResultHandler {

    public static void main(String[] args) {
        String resultPath = "result";
        String outputPath = "handle";
        try {
            handle(resultPath, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    private static void handle(String resultPath, String outputPath) throws IOException {

        File dataDirectory = new File(resultPath);
        String[] subDirectories = dataDirectory.list();
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        PrintStream out = new PrintStream(new FileOutputStream(outputPath + "/" + "handleData.txt"));
        System.setOut(out);
        assert subDirectories != null;
        for (String directory : subDirectories) {
            File subDirectory = new File(resultPath + "/" + directory);
            String[] txtFiles = subDirectory.list();
            assert txtFiles != null;
            handle(txtFiles, resultPath, directory);
        }
        out.close();
    }

    private static void handle(String[] txtFiles, String resultPath, String directory) throws IOException {
        StringBuilder lowerBoundRow = new StringBuilder();
        StringBuilder upperBoundRow = new StringBuilder();
        StringBuilder countRow = new StringBuilder();
        StringBuilder timeRow = new StringBuilder();

        for (String file : txtFiles) {
            Path path = Paths.get(resultPath, directory, file);
            List<String> txtFile = Files.readAllLines(path);
            String lowerBound = txtFile.get(0).substring(3);
            String upperBound = txtFile.get(1).substring(11);
            String count = txtFile.get(txtFile.size() - 1).split("\\s+")[2];
            String time = txtFile.get(txtFile.size() - 2).split("\\s+")[3];

            lowerBoundRow.append(" ").append(lowerBound);
            upperBoundRow.append(" ").append(upperBound);
            countRow.append(" ").append(count);
            timeRow.append(" ").append(time);

        }

        System.out.println(directory + "lowerBound" + lowerBoundRow);
        System.out.println(directory + "upperBound" + upperBoundRow);
        System.out.println(directory + "count" + countRow);
        System.out.println(directory + "time/s" + timeRow);
    }
}
