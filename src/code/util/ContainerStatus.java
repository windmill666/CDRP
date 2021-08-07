package code.util;

import java.util.List;

public class ContainerStatus {

    /**
     * 构造器私有
     */
    private ContainerStatus() {}

    /**
     * 打印集装箱布局
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param stackLength 堆垛的长度限制
     */
    public static void printContainerLayout(int[] containerLayout, int stackHeight, int stackLength) {
        for (int i = 0; i < stackHeight; i++) {
            for (int j = 0; j < stackLength; j++) {
                System.out.printf("%d\t", containerLayout[stackHeight * (j + 1) - (i + 1)]);
            }
            System.out.println();
        }
    }

    /**
     * 计算全局的BP数量
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param stackLength 堆垛的长度限制
     * @return 全局的BP数量
     */
    public static int calGlobalBP(int[] containerLayout, int stackHeight, int stackLength) {
        int BP = 0;
        int curContainer, bottom, top;
        for (int i = 0; i < stackLength; i++) {
            bottom = i * stackHeight;
            top = (i + 1) * stackHeight - 1;
            curContainer = containerLayout[bottom];
            bottom++;
            while (bottom <= top) {
                if (curContainer == 0) {
                    break;
                }
                if (containerLayout[bottom] > curContainer) {
                    BP++;
                } else {
                    curContainer = containerLayout[bottom];
                }
                bottom++;
            }
        }
        return BP;
    }

    /**
     * 计算全局的BP数量
     * @param readAllLines 原始数据
     * @param stackHeight 堆垛的高度限制
     * @param stackLength 堆垛的长度限制
     * @return 全局的BP数量
     */
    public static int calGlobalBP(List<String> readAllLines, int stackHeight, int stackLength) {
        int[] containerLayout = PrepareMethod.prepareContainerLayout(readAllLines, stackHeight, stackLength);
        return calGlobalBP(containerLayout, stackHeight, stackLength);
    }

    /**
     * 判断取箱集合或者存箱集合是否为空
     * @param containerArr 压缩后的二维集装箱布局
     * @return 集装箱集合
     */
    public static boolean isEmptyIn(int[] containerArr) {
        return containerArr[containerArr.length - 1] == -1;
    }

    /**
     * 判断堆垛内是否为空
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param stackNo 指定堆垛
     * @return true：空堆垛；false：非空堆垛
     */
    public static boolean isEmptyIn(int[] containerLayout, int stackHeight, int stackNo) {
        return containerLayout[stackNo * stackHeight] == 0;
    }

    /**
     * 判断堆垛内是否已满
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param stackNo 指定堆垛
     * @return true：堆垛已满；false：堆垛未满
     */
    public static boolean isFullIn(int[] containerLayout, int stackHeight, int stackNo) {
        return containerLayout[(stackNo + 1) * stackHeight - 1] != 0;
    }

    public static int[] copyArr(int[] source) {
        int[] destination = new int[source.length];
        System.arraycopy(source, 0, destination, 0, source.length);
        return destination;
    }

}
