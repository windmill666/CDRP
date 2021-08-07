package code.util;

import java.util.List;

public class PrepareMethod {

    /**
     * 构造器私有
     */
    private PrepareMethod() {}

    /**
     * 解析集装箱布局数据并压缩为一维数组
     * @param readAllLines 原始数据
     * @param stackHeight 堆垛的高度限制
     * @param stackLength 堆垛的长度限制
     * @return 解析后的数据
     */
    public static int[] prepareContainerLayout(List<String> readAllLines, int stackHeight, int stackLength){
        int[] containerLayout = new int[stackLength * stackHeight];
        int j;
        int index;
        for (int i = 1; i < readAllLines.size() - 1; i++) {
            String[] split = readAllLines.get(i).trim().split("\\s+");
            j = 1;
            for (; j < split.length; j++) {
                index = (i - 1) * stackHeight + j - 1;
                containerLayout[index] = Integer.parseInt(split[j]);
            }
            for (; j < stackHeight; j++) {
                index = (i - 1) * stackHeight + j - 1;
                containerLayout[index] = 0;
            }
        }
        return containerLayout;
    }

    /**
     * 解析存箱数据
     * @param readAllLines 原始数据
     * @return 解析后的数据
     */
    public static int[] prepareStorageContainer(List<String> readAllLines) {
        String[] split = readAllLines.get(readAllLines.size() - 1).trim().split("\\s+");
        int[] storageContainer = new int[split.length - 1];
        for (int i = 0; i < storageContainer.length; i++) {
            storageContainer[i] = Integer.parseInt(split[i + 1]);
        }
        return storageContainer;
    }

    /**
     * 解析取箱数据
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param retrievalNum 集装箱的总数目
     * @return 解析后的数据
     */
    public static int[] prepareRetrievalContainer(int[] containerLayout, int stackHeight, int retrievalNum) {
        int[] retrievalContainer = new int[retrievalNum];
        for (int i = 0; i < containerLayout.length; i++) {
            if (containerLayout[i] > 0 && containerLayout[i] <= retrievalNum) {
                retrievalContainer[containerLayout[i] - 1] = i / stackHeight;
            }
        }
        return retrievalContainer;
    }

}
