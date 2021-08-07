package code.method.core;

import code.util.ContainerMovement;
import code.util.ContainerStatus;
import code.util.MovementScore;
import code.util.PrepareMethod;

import java.util.ArrayList;
import java.util.List;

public class RandomSearch implements ISearch {
    private static final int GEN = 10000;

    private int stackHeight;
    private int stackLength;
    private int countMin;
    private int upperBound;

    public RandomSearch(int stackHeight, int stackLength) {
        this.stackHeight = stackHeight;
        this.stackLength = stackLength;
    }

    /**
     * 随机搜索算法
     * @param readAllLines 原始数据
     * @param stackHeight 堆垛的高度限制
     * @param stackLength 堆垛的长度限制
     * @return 近似解(最小翻倒次数)
     */
    @Override
    public int start(List<String> readAllLines, int stackHeight, int stackLength) {
        int retrievalNum = Integer.parseInt(readAllLines.get(0).trim().split("\\s+")[1]);
        int[] containerLayout = PrepareMethod.prepareContainerLayout(readAllLines, stackHeight, stackLength);
        int[] retrievalContainer = PrepareMethod.prepareRetrievalContainer(containerLayout, stackHeight, retrievalNum);
        int[] storageContainer = PrepareMethod.prepareStorageContainer(readAllLines);
        return start(containerLayout, retrievalContainer, storageContainer);
    }

    /**
     * 随机搜索算法
     * @param containerLayout 压缩后的二维集装箱布局
     * @param retrievalContainer 取箱集合
     * @param storageContainer 存箱集合
     * @return 近似解(最小翻倒次数)
     */
    @Override
    public int start(int[] containerLayout, int[] retrievalContainer, int[] storageContainer) {
        countMin = Integer.MAX_VALUE;
        upperBound = ContainerStatus.calGlobalBP(containerLayout, stackHeight, stackLength) * 2 + 10;
        int count,generation;
        generation = 0;
        while (generation < GEN || countMin == Integer.MAX_VALUE) {
            count = run(containerLayout, retrievalContainer, storageContainer);
            if (count < countMin) {
                countMin = count;
                generation = 0;
            } else {
                generation++;
            }
        }
        return countMin;
    }

    private int run(int[] containerLayoutSource, int[] retrievalContainerSource, int[] storageContainerSource) {
        int count = 0;
        int[] containerLayout = ContainerStatus.copyArr(containerLayoutSource);
        int[] retrievalContainer = ContainerStatus.copyArr(retrievalContainerSource);
        int[] storageContainer = ContainerStatus.copyArr(storageContainerSource);
        int index,liftUpRandom,liftDownRandom;

        ContainerMovement.retrieval(containerLayout, stackHeight, retrievalContainer);

        // 阶段一：执行取箱任务
        while (!ContainerStatus.isEmptyIn(retrievalContainer)) {
            List<Integer> record = new ArrayList<>();
            List<Double> score = new ArrayList<>();
            for (int liftUp = 0; liftUp < stackLength; liftUp++) {
                if (ContainerStatus.isEmptyIn(containerLayout, stackHeight, liftUp)) {
                    continue;
                }
                for (int liftDown = 0; liftDown < stackLength; liftDown++) {
                    if (liftDown == liftUp || ContainerStatus.isFullIn(containerLayout, stackHeight, liftDown)) {
                        continue;
                    }
                    record.add(liftUp);
                    record.add(liftDown);
                    score.add(MovementScore.scoreOfStageOne(containerLayout, retrievalContainer, stackHeight, liftUp, liftDown));
                }
            }
            index = MovementScore.roulette(score.toArray(Double[]::new));
            liftUpRandom = record.get(index*2);
            liftDownRandom = record.get(index*2+1);
            int relocateContainer = ContainerMovement.relocate(containerLayout, stackHeight, liftUpRandom, liftDownRandom);
            if (relocateContainer - 1 < retrievalContainer.length) {
                retrievalContainer[relocateContainer - 1] = liftDownRandom;
            }
            ContainerMovement.retrieval(containerLayout, stackHeight, retrievalContainer);
            count++;
            if (count > upperBound || count + ContainerStatus.calGlobalBP(containerLayout, stackHeight, stackLength) > countMin) {
                return Integer.MAX_VALUE;
            }
        }

        // 阶段二：执行存箱任务
        while (!ContainerStatus.isEmptyIn(storageContainer)) {
            List<Integer> record = new ArrayList<>();
            List<Double> score = new ArrayList<>();
            for (int liftUp = 0; liftUp < stackLength; liftUp++) {
                if (ContainerStatus.isEmptyIn(containerLayout, stackHeight, liftUp)) {
                    continue;
                }
                for (int liftDown = 0; liftDown < stackLength; liftDown++) {
                    if (liftDown == liftUp || ContainerStatus.isFullIn(containerLayout, stackHeight, liftDown)) {
                        continue;
                    }
                    record.add(liftUp);
                    record.add(liftDown);
                    score.add(MovementScore.scoreOfStageTwo(containerLayout, storageContainer, stackHeight, liftUp, liftDown));
                }
            }
            for (int liftDown = 0; liftDown < stackLength; liftDown++) {
                if (ContainerStatus.isFullIn(containerLayout, stackHeight, liftDown)) {
                    continue;
                }
                record.add(-1);
                record.add(liftDown);
                score.add(MovementScore.scoreOfStageTwo(containerLayout, storageContainer, stackHeight, -1, liftDown));
            }
            index = MovementScore.roulette(score.toArray(Double[]::new));
            liftUpRandom = record.get(index*2);
            liftDownRandom = record.get(index*2+1);
            if (liftUpRandom == -1) {
                ContainerMovement.storage(containerLayout, stackHeight, storageContainer, liftDownRandom);
            } else {
                ContainerMovement.relocate(containerLayout, stackHeight, liftUpRandom, liftDownRandom);
                count++;
            }
            if (count > upperBound || count + ContainerStatus.calGlobalBP(containerLayout, stackHeight, stackLength) > countMin) {
                return Integer.MAX_VALUE;
            }
        }

        // 阶段三：执行预翻倒任务
        while (ContainerStatus.calGlobalBP(containerLayout, stackHeight, stackLength) > 0) {
            List<Integer> record = new ArrayList<>();
            List<Double> score = new ArrayList<>();
            for (int liftUp = 0; liftUp < stackLength; liftUp++) {
                if (ContainerStatus.isEmptyIn(containerLayout, stackHeight, liftUp)) {
                    continue;
                }
                for (int liftDown = 0; liftDown < stackLength; liftDown++) {
                    if (liftDown == liftUp || ContainerStatus.isFullIn(containerLayout, stackHeight, liftDown)) {
                        continue;
                    }
                    record.add(liftUp);
                    record.add(liftDown);
                    score.add(MovementScore.scoreOfStageThree(containerLayout, stackHeight, liftUp, liftDown));
                }
            }
            index = MovementScore.roulette(score.toArray(Double[]::new));
            liftUpRandom = record.get(index*2);
            liftDownRandom = record.get(index*2+1);
            ContainerMovement.relocate(containerLayout, stackHeight, liftUpRandom, liftDownRandom);
            count++;
            if (count > upperBound || count + ContainerStatus.calGlobalBP(containerLayout, stackHeight, stackLength) > countMin) {
                return Integer.MAX_VALUE;
            }
        }

        return count;
    }

}
