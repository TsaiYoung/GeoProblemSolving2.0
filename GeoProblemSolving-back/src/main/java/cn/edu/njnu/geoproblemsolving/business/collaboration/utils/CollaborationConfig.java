package cn.edu.njnu.geoproblemsolving.business.collaboration.utils;

import cn.edu.njnu.geoproblemsolving.business.activity.dao.Impl.TaskDaoImpl;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.CollaborationUser;
import cn.edu.njnu.geoproblemsolving.business.collaboration.enums.CollaborationMode;
import lombok.Data;

import java.util.*;

/**
 * @ author: mzy
 * @ date: 2021/4
 * @ description: the mode, origin, scope and destination of collaboration
 */
@Data
public class CollaborationConfig {

    /**
     * Basic
     */
    private String aid;

    /**
     * mode: Free / Semi-free(Occupy/Apply)
     * operator: current person who can send the message
     * applyQueue: people who are waiting for operation - for semi-free mode
     */
    private CollaborationMode mode;
    private String operator;
    private List<String> applyQueue;
    private HashMap<String, CollaborationUser> participants;
    // for SemiFree_Occupy mode; the time start to operate
    private Long startTime;

    public CollaborationConfig(String aid){
        this.aid = aid;
    }


    public void resetTime() {
        startTime = System.currentTimeMillis();
    }

    public void setOperator(String user) {
        operator = user;
        startTime = System.currentTimeMillis();
    }

    public Boolean resetOperator(String user) {
        Long current = System.currentTimeMillis();
        if((current - startTime) > (20 * 1000)) {
            operator = user;
            startTime = current;
            return true;
        } else {
            return false;
        }
    }
}
