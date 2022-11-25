package cn.edu.njnu.geoproblemsolving.business.activity.entity.Template.workflow;

import lombok.Data;
import netscape.javascript.JSObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * @ClassName WorkflowTemplate
 * @Description Todo
 * @Author zhngzhng
 * @Date 2021/12/9
 **/
@Document(collection = "TemplateWorkflow")
@Data
public class WorkflowTemplate {
    @Id
    String id;
    boolean resVisible = false;
    String name;
    ProjectInfo projectInfo;
    HashSet<Operation> operations;
    HashSet<HashMap<String, String>> resBehavior;
}
