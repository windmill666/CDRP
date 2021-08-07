package code.model;

import code.util.ContainerMovement;
import code.util.PrepareMethod;

import java.util.List;

public class PrepareModel {

    private int containerSum;
    private int stackHeight;
    private int stackLength;
    private int[][] containerBin;
    private int[] retrieval4Model,storage4Model,restRetrieval4Model,restStorage4Model;

    public PrepareModel(int stackHeight, int stackLength) {
        this.stackHeight = stackHeight;
        this.stackLength = stackLength;
    }

    public int getContainerSum() {
        return containerSum;
    }

    public int[][] getContainerBin() {
        return containerBin;
    }

    public int[] getRetrieval4Model() {
        return retrieval4Model;
    }

    public int[] getStorage4Model() {
        return storage4Model;
    }

    public int[] getRestRetrieval4Model() {
        return restRetrieval4Model;
    }

    public int[] getRestStorage4Model() {
        return restStorage4Model;
    }

    /**
     * 为模型准备输入数据
     * 包括：初始集装箱布局，待取集装箱集合，非取集装箱集合，待存集装箱集合，非存集装箱集合
     * @param readAllLines 原始数据
     * @param containerSum 集装箱总数量
     */
    public void prepare(List<String> readAllLines, int containerSum) {
        int retrievalNum = Integer.parseInt(readAllLines.get(0).trim().split("\\s+")[1]);
        int[] containerLayout = PrepareMethod.prepareContainerLayout(readAllLines, stackHeight, stackLength);
        int[] retrievalContainer = PrepareMethod.prepareRetrievalContainer(containerLayout, stackHeight, retrievalNum);
        ContainerMovement.retrieval(containerLayout, stackHeight, retrievalContainer);
        int hasRetrieval = 0;
        for (int value : retrievalContainer) {
            if (value == -1) {
                hasRetrieval++;
            } else {
                break;
            }
        }
        this.containerSum = containerSum - hasRetrieval;
        prepareContainerBin(containerLayout, containerSum, hasRetrieval);
        prepareRetrieval4Model(retrievalNum, hasRetrieval);
        prepareRestRetrieval4Model(containerSum, hasRetrieval);
        prepareStorage4Model(readAllLines, hasRetrieval);
        prepareRestStorage4Model(containerSum, hasRetrieval);
    }

    /**
     * 解析初始集装箱布局
     * @param containerLayout 待解析数据
     * @param containerSum 集装箱总数量
     * @param hasRetrieval 不用翻倒就能取出的集装箱数量
     */
    private void prepareContainerBin(int[] containerLayout, int containerSum, int hasRetrieval) {
        containerBin = new int[containerSum - hasRetrieval][containerSum - hasRetrieval + 1];
        int floor = containerSum + 1;
        for (int i = 0; i < containerLayout.length; i++) {
            if (containerLayout[i] == 0) {
                continue;
            }
            if (i % stackHeight == 0) {
                containerBin[containerLayout[i] - hasRetrieval - 1][floor - hasRetrieval - 1] = 1;
            } else {
                containerBin[containerLayout[i] - hasRetrieval - 1][containerLayout[i - 1] - hasRetrieval - 1] = 1;
            }
        }
    }

    /**
     * 解析待取集装箱集合
     * @param retrievalNum 需要取出的集装箱数量
     * @param hasRetrieval 不用翻倒就能取出的集装箱数量
     */
    private void prepareRetrieval4Model(int retrievalNum, int hasRetrieval) {
        int num = retrievalNum - hasRetrieval;
        retrieval4Model = new int[num];
        for (int i = 0; i < num; i++) {
            retrieval4Model[i] = i;
        }
    }

    /**
     * 解析非取集装箱集合
     * @param containerSum 集装箱总数量
     * @param hasRetrieval 不用翻倒就能取出的集装箱数量
     */
    private void prepareRestRetrieval4Model(int containerSum, int hasRetrieval) {
        int num = containerSum - hasRetrieval - retrieval4Model.length;
        restRetrieval4Model = new int[num];
        for (int i = 0; i < num; i++) {
            restRetrieval4Model[i] = i + retrieval4Model.length;
        }
    }

    /**
     * 解析待存集装箱集合
     * @param readAllLines 待解析数据
     * @param hasRetrieval 不用翻倒就能取出的集装箱数量
     */
    private void prepareStorage4Model(List<String> readAllLines, int hasRetrieval) {
        String[] splitLastLine = readAllLines.get(readAllLines.size()-1).trim().split("\\s+");
        storage4Model = new int[splitLastLine.length - 1];
        for (int i = 0; i < storage4Model.length; i++) {
            storage4Model[i] = Integer.parseInt(splitLastLine[i + 1]) - hasRetrieval - 1;
        }
    }

    /**
     * 非存集装箱集合
     * @param containerSum 集装箱总数量
     * @param hasRetrieval 不用翻倒就能取出的集装箱数量
     */
    private void prepareRestStorage4Model(int containerSum, int hasRetrieval) {
        int num = containerSum - hasRetrieval - storage4Model.length;
        restStorage4Model = new int[num];
        int j = 0;
        label: for (int i = 0; i < containerSum - hasRetrieval; i++) {
            for (int value : storage4Model) {
                if (i == value) {
                    continue label;
                }
            }
            restStorage4Model[j++] = i;
        }
    }

}
