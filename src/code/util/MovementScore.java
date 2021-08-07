package code.util;

public class MovementScore {

    private static int[] param = new int[]{4, 4, 3, 3, 2, 2, 1, 1, 1, 1,
            4, 4, 2, 2, 3, 3, 1, 1, 1, 1,
            2, 2, 1, 1, 1, 1};

    /**
     * 构造器私有
     */
    private MovementScore() {}

    /**
     * 阶段一打分策略
     * 为阶段一(取箱阶段，从任务开始到取箱任务结束)的翻倒操作打分
     * @param containerLayout 压缩后的二维集装箱布局
     * @param retrievalContainer 取箱集合
     * @param stackHeight 堆垛的高度限制
     * @param liftUp 源堆垛
     * @param liftDown 目标堆垛
     * @return 得分
     */
    public static double scoreOfStageOne(int[] containerLayout, int[] retrievalContainer, int stackHeight, int liftUp, int liftDown) {
        int targetStack = 0;
        for (int value : retrievalContainer) {
            if (value != -1) {
                targetStack = value;
                break;
            }
        }
        int topContainer = topContainer(containerLayout, stackHeight, liftUp);
        int liftDownStackPriority = getPriorityOfStack(containerLayout, stackHeight, liftDown);
        int liftUpStackPriority;
        if (liftUp == targetStack) {
            if (topContainer < liftDownStackPriority) {// target BG move
                return (Math.pow(Math.E, topContainer - liftDownStackPriority) + param[0]) * param[1];
            } else {// target BB move
                return (Math.pow(Math.E, liftDownStackPriority - topContainer) + param[2]) * param[3];
            }
        } else {
            liftUpStackPriority = getPriorityOfStack(containerLayout, stackHeight, liftUp);
            if (topContainer > liftUpStackPriority) {
                if (topContainer < liftDownStackPriority) {// BG move
                    return (Math.pow(Math.E, topContainer - liftDownStackPriority) + param[4]) * param[5];
                } else {// BB move
                    return (Math.pow(Math.E, liftDownStackPriority - topContainer) + param[6]) * param[7];
                }
            } else {
                if (topContainer < liftDownStackPriority) {// GG move
                    return (Math.pow(Math.E, topContainer - liftDownStackPriority) + param[8]) * param[9];
                } else {// GB move
                    return 0;
                }
            }
        }
    }

    /**
     * 阶段二打分策略
     * 为阶段二(存箱阶段，从取箱任务结束到存箱任务结束)的翻倒操作打分
     * @param containerLayout 压缩后的二维集装箱布局
     * @param storageContainer 存箱集合
     * @param stackHeight 堆垛的高度限制
     * @param liftUp 源堆垛
     * @param liftDown 目标堆垛
     * @return 得分
     */
    public static double scoreOfStageTwo(int[] containerLayout, int[] storageContainer, int stackHeight, int liftUp, int liftDown) {
        int liftDownStackPriority = getPriorityOfStack(containerLayout, stackHeight, liftDown);
        int container,topContainer,liftUpStackPriority;
        if (liftUp == -1) {
            container = nextStorageContainer(storageContainer);
            if (container < liftDownStackPriority) {// OG move
                return (Math.pow(Math.E, container - liftDownStackPriority) + param[10]) * param[11];
            } else {// OB move
                return (Math.pow(Math.E, liftDownStackPriority - container) + param[12]) * param[13];
            }
        } else {
            topContainer = topContainer(containerLayout, stackHeight, liftUp);
            liftUpStackPriority = getPriorityOfStack(containerLayout, stackHeight, liftUp);
            if (topContainer > liftUpStackPriority) {
                if (topContainer < liftDownStackPriority) {// BG move
                    return (Math.pow(Math.E, topContainer - liftDownStackPriority) + param[14]) * param[15];
                } else {// BB move
                    return (Math.pow(Math.E, liftDownStackPriority - topContainer) + param[16]) * param[17];
                }
            } else {
                if (topContainer < liftDownStackPriority) {// GG move
                    return (Math.pow(Math.E, topContainer - liftDownStackPriority) + param[18]) * param[19];
                } else {// GB move
                    return 0;
                }
            }
        }

    }

    /**
     * 阶段三打分策略
     * 为阶段三(预翻倒阶段，从存箱任务结束到任务结束)的翻倒操作打分
     * @param containerLayout 压缩后的二维集装箱布局
     * @param stackHeight 堆垛的高度限制
     * @param liftUp 源堆垛
     * @param liftDown 目标堆垛
     * @return 得分
     */
    public static double scoreOfStageThree(int[] containerLayout, int stackHeight, int liftUp, int liftDown) {
        int topContainer = topContainer(containerLayout, stackHeight, liftUp);
        int liftDownStackPriority = getPriorityOfStack(containerLayout, stackHeight, liftDown);
        int liftUpStackPriority = getPriorityOfStack(containerLayout, stackHeight, liftUp);
        if (topContainer > liftUpStackPriority) {
            if (topContainer < liftDownStackPriority) {// BG move
                return (Math.pow(Math.E, topContainer - liftDownStackPriority) + param[20]) * param[21];
            } else {// BB move
                return (Math.pow(Math.E, liftDownStackPriority - topContainer) + param[22]) * param[23];
            }
        } else {
            if (topContainer < liftDownStackPriority) {// GG move
                return (Math.pow(Math.E, topContainer - liftDownStackPriority) + param[24]) * param[25];
            } else {// GB move
                return 0;
            }
        }
    }

    /**
     * 轮盘赌
     * @param p 概率数组
     * @return 选择的index
     */
    public static int roulette(Double[] p) {
        // 归一化概率数组
        double normalization = 0;
        for (double v : p) {
            normalization += v;
        }
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i] / normalization;
        }
        double random = Math.random();
        int chooseIndex = 0;
        double sum = 0;
        while (chooseIndex < p.length) {
            sum += p[chooseIndex];
            if (random < sum) {
                break;
            }
            chooseIndex++;
        }
        return chooseIndex;
    }

    private static int topContainer(int[] containerLayout, int stackHeight, int liftUp) {
        int topContainer = -1;
        for (int i = (liftUp + 1) * stackHeight - 1; i >= liftUp * stackHeight; i--) {
            if (containerLayout[i] != 0) {
                topContainer = containerLayout[i];
                break;
            }
        }
        return topContainer;
    }

    private static int getPriorityOfStack(int[] containerLayout, int stackHeight, int stackNo) {
        int stackPriority = containerLayout.length;
        for (int i = stackNo * stackHeight; i < (stackNo + 1) * stackHeight; i++) {
            if (containerLayout[i] == 0) {
                break;
            } else if (containerLayout[i] < stackPriority){
                stackPriority = containerLayout[i];
            }
        }
        return stackPriority;
    }

    private static int nextStorageContainer(int[] storageContainer) {
        for (int sc : storageContainer) {
            if (sc != -1) {
                return sc;
            }
        }
        return -1;
    }

}
