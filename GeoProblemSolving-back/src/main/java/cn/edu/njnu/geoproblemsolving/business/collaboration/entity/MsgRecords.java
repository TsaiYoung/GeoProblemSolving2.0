package cn.edu.njnu.geoproblemsolving.business.collaboration.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Data
@Document(collection = "MessageRecords")
public class MsgRecords {

    @Id
    private String recordId;


    private String aid; //查询条件Id

    private ArrayList<String> records;

    private ArrayList<String> participants;

    private Date createdTime;
}
