package code.util;

/**
 * 集装箱的动态操作
 * @author lizongheng
 * @version 2.6
 */
public class ContainerMovement {

    /**
     * 构造器私有
     */
    private ContainerMovement() {}

    /**
     * 翻倒操作
     * 将源堆垛的顶层集装箱，移动至目标堆垛并放在最上层
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param liftUp 源堆垛
     * @param liftDown 目标堆垛
     * @return 移动的集装箱ID
     */
    public static int relocate(int[] containerLayout, int stackHeight, int liftUp, int liftDown) {
        int res = -1;
        for (int i = (liftUp + 1) * stackHeight - 1; i >= liftUp * stackHeight; i--) {
            if (containerLayout[i] != 0) {
                res = containerLayout[i];
                containerLayout[i] = 0;
                break;
            }
        }
        for (int i = liftDown * stackHeight; i < (liftDown + 1) * stackHeight; i++) {
            if (containerLayout[i] == 0) {
                containerLayout[i] = res;
                break;
            }
        }
        return res;
    }

    /**
     * 取箱操作
     * 按照取箱集合，依次取出集装箱，如果待取集装箱不在堆垛顶层，就停止取箱
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param retrievalContainer 取箱集合
     */
    public static void retrieval(int[] containerLayout, int stackHeight, int[] retrievalContainer) {
        int bottom;
        int top;
        label: for (int i = 0; i < retrievalContainer.length; i++) {
            if (retrievalContainer[i] != -1) {
                bottom = stackHeight * retrievalContainer[i];
                top = stackHeight * (retrievalContainer[i] + 1) - 1;
                for (int j = top; j >= bottom; j--) {
                    if (containerLayout[j] == 0) {// 遇到空箱位，直接跳过
                        continue;
                    }
                    if (containerLayout[j] == i + 1){// 目标箱在顶层，可以直接取出
                        // 0代表空箱位
                        containerLayout[j] = 0;
                        // -1代表已经取出
                        retrievalContainer[i] = -1;
                        break;
                    } else {// 最顶层不是目标箱，无法取出，停止取箱动作，跳出外循环
                        break label;
                    }
                }
            }
        }
    }

    /**
     * 存箱操作
     * 将待存集装箱存入指定的堆垛并放在最上层
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param storageContainer 存箱集合
     * @param liftDown 目标堆垛
     */
    public static void storage(int[] containerLayout, int stackHeight, int[] storageContainer, int liftDown) {
        label: for (int i = 0; i < storageContainer.length; i++) {
            if (storageContainer[i] != -1) {
                for (int j = liftDown * stackHeight; j < (liftDown + 1) * stackHeight; j++) {
                    if (containerLayout[j] == 0) {
                        containerLayout[j] = storageContainer[i];
                        storageContainer[i] = -1;
                        break label;
                    }
                }
            }
        }
    }
}
