package code.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class GenerateData {

    public static void main(String[] args) {
        int[][] stackArr = new int[][]{{3,3},{3,4},{3,5},{3,6},{4,3},{4,4},{4,5},{4,6},{5,3},{5,4},{5,5},{5,6}};
        int fileNo = 1;
        int txtNum = 100;
        String outputPath = "data";
        String outputSubDir;
        for (int[] stack : stackArr) {
            outputSubDir = stack[0] + "-" + stack[1] + "-" + stack[0] * stack[1];
            File outputDir = new File(outputPath);
            boolean mk = outputDir.mkdir();
            GenerateData generateData = new GenerateData(fileNo, stack[0], stack[1]);
            try {
                generateData.start(txtNum, outputDir.getPath() + File.separator + outputSubDir);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            fileNo += txtNum;
        }
    }

    private int fileNo;
    private int stackHeight;
    private int stackLength;

    private int containerSum;
    private int retrievalSize;
    private int storageSize;
    private int divideR = 3;// 总数的1/3作为取箱集合大小
    private int divideS = 3;// 总数的1/3作为存箱集合大小

    public GenerateData(int fileNo, int stackHeight, int stackLength) {
        this.fileNo = fileNo;
        this.stackHeight = stackHeight;
        this.stackLength = stackLength;
        this.containerSum = stackHeight * stackLength;
        this.retrievalSize = containerSum / divideR;
        this.storageSize = containerSum / divideS;
    }

    public void setDivideR(int divideR) {
        this.divideR = divideR;
    }

    public void setDivideS(int divideS) {
        this.divideS = divideS;
    }

    public void start(int txtNum, String path) throws FileNotFoundException {
        File outputSubDir = new File(path);
        boolean mk = outputSubDir.mkdir();
        for (int i = 1; i <= txtNum; i++) {
            start(path);
        }
    }

    private void start(String path) throws FileNotFoundException {

        String fileName = generateFileName(fileNo++);
        PrintStream out = new PrintStream(new FileOutputStream(path + File.separator + fileName));
        System.setOut(out);

        // 输出堆垛规模和取箱数量
        System.out.print(stackLength + " " + retrievalSize);
        System.out.println();

        // 输出集装箱布局信息
        List<Integer> storageContainer = generateStorageContainer();// 选出待存集装箱
        List<Integer> allContainer = new ArrayList<>();
        for (int i = 0; i < containerSum; i++) {
            allContainer.add(i + 1);
        }
        allContainer.removeAll(storageContainer);
        Collections.shuffle(allContainer);
        generateContainerLayout(allContainer);

        // 输出待存集装箱信息
        System.out.print(" " + storageSize + "\t");
        storageContainer.forEach(v -> System.out.print(v + "\t"));
        System.out.println();

        out.close();
    }

    private List<Integer> generateStorageContainer() {
        Set<Integer> storageContainerSet = new HashSet<>();
        while (storageContainerSet.size() < storageSize) {
            storageContainerSet.add((int) (Math.random() * (containerSum - retrievalSize) + retrievalSize + 1));
        }
        List<Integer> storageContainerList = new ArrayList<>(storageContainerSet);
        Collections.shuffle(storageContainerList);
        return storageContainerList;
    }

    private void generateContainerLayout(List<Integer> restContainer) {
        // 生成堆垛容量布局
        int[] splitContainer = new int[stackLength];
        while (true){
            for (int i = 0; i < stackLength; i++) {
                splitContainer[i] = (int) (Math.random() * (stackHeight + 1));
            }
            int temp = 0;
            for (int i = 0; i < stackLength; i++) {
                temp += splitContainer[i];
            }
            if (temp == restContainer.size()){
                break;
            }
        }
        // 放入箱子，空箱位填入0
        int splitIndex = 0;
        for (int i = 0; i < stackLength; i++) {
            List<Integer> stackContainer = new ArrayList<>();
            for (int j = splitIndex; j < splitIndex + splitContainer[i]; j++) {
                stackContainer.add(restContainer.get(j));
            }
            splitIndex += splitContainer[i];
            System.out.print(" " + stackContainer.size() + "\t");
            while (stackContainer.size() < stackHeight){
                stackContainer.add(0);
            }
            for (Integer integer : stackContainer) {
                System.out.print(integer + "\t");
            }
            System.out.println();
        }
    }

    private String generateFileName(int fileNo) {
        StringBuilder stringBuilder = new StringBuilder();
        int temp = fileNo;
        int lg = 0;
        while (temp >= 10) {
            temp /= 10;
            lg++;
        }
        for (int i = lg; i < 3; i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(fileNo);
        stringBuilder.append(".txt");
        return stringBuilder.toString();
    }

}
