package cn.edu.njnu.geoproblemsolving.business.collaboration.entity;

import cn.edu.njnu.geoproblemsolving.business.collaboration.compute.entity.TaskIO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @Author mzy
 * @Date 2021/4
 * @Description 计算记录
 * 计算任务相关信息，将可计算模型与数据方法统一
 */
@Data
public class ComputeMsg {
    //groupKey = toolId + aid;
    private String toolId;

    private String aid;

    //对应的操作 id
    private String oid;

    private boolean computeSuc = false;

    //invoke 时参与的人
    private ArrayList<CollaborationUser> receivers;

    // compute 信息时间
    private String time;

    // compute参数
    private String serviceId;
    private String serviceIp;
    private String servicePort;
    private String serviceToken;

    private JSONArray inputs;
    private JSONObject urls;
    private JSONArray params;

    /*
    工具运行状态: -2(error) 0(init) 1(running) 2(suc)
    后续做持久化还有一些用处
    */
    private Integer status;
    private String taskId;
    private Object outputs;
    private JSONObject errorInfo;

    public void setReceivers(HashMap<String, CollaborationUser> users) {
        receivers = new ArrayList<>();
        for (Map.Entry<String, CollaborationUser> receiver : users.entrySet()) {
            receivers.add(receiver.getValue());
        }
    }

    public HashMap<String, CollaborationUser> getReceivers() {
        HashMap<String, CollaborationUser> users = new HashMap<>();
        for (CollaborationUser receiver : receivers) {
            users.put(receiver.getUserId(), receiver);
        }
        return users;
    }

    public HashSet<String> getParticipants(){
        HashSet<String> userIds = new HashSet<>();
        for (CollaborationUser receiver : receivers){
            userIds.add(receiver.getUserId());
        }
        return userIds;
    }

}
