package cn.edu.njnu.geoproblemsolving.business.behaviorTrack.enums;

/*
第二级：两个级别共同确定一个交互行为
交互行为类型的枚举，具体的交互行为
 */
public enum InteractiveBehavior {
    createActivity,
    editActivity,
    delActivity,
    crateTask,
    editTask,
    delTask,
    assignTask,
    setLink,
    editLink,
    delLink,

    addTool,
    addResource,
    delTool,
    delResource,
    editTool,
    editResource,
    shareTool,
    shareResource,
    downloadResource,

    paramInput,
    dataInput
}
