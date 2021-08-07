package code.method.core;

import java.util.List;

public interface ISearch {


    /**
     * 算法入口
     * @param readAllLines 原始数据
     * @param stackHeight 堆垛的高度限制
     * @param stackLength 堆垛的长度限制
     * @return 近似解(最小翻倒次数)
     */
    int start(List<String> readAllLines, int stackHeight, int stackLength);

    /**
     * 算法入口
     * @param containerLayout 压缩后的二维集装箱布局
     * @param retrievalContainer 取箱集合
     * @param storageContainer 存箱集合
     * @return 近似解(最小翻倒次数)
     */
    int start(int[] containerLayout, int[] retrievalContainer, int[] storageContainer);

}
