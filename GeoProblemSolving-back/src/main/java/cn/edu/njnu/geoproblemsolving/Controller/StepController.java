package cn.edu.njnu.geoproblemsolving.Controller;
import cn.edu.njnu.geoproblemsolving.Dao.Step.StepDaoImpl;
import cn.edu.njnu.geoproblemsolving.Entity.StepEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@CrossOrigin(origins = "*",allowCredentials = "true")
@RestController
@RequestMapping("/step")
public class StepController {
    @Resource
    private MongoTemplate mongoTemplate;

//    创建页面，带subprojectid
    @RequestMapping(value = "/create", produces = {"application/json;charset=UTF-8"},method = RequestMethod.POST)
    public String createStep(@RequestBody StepEntity step){
        StepDaoImpl stepDao = new StepDaoImpl(mongoTemplate);
        try {
            Date date=new Date();
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String stepId=UUID.randomUUID().toString();
            step.setStepId(stepId);
            step.setCreateTime(dateFormat.format(date));
//            step.setSubProjectId(subProjectId);

            return stepDao.createStep(step);
        }catch (Exception e){
            return "Fail";
        }
    }

    @RequestMapping(value = "/inquiry", method = RequestMethod.GET)
    public Object readStep(@RequestParam("key") String key,@RequestParam("value") String value){
        StepDaoImpl stepDao = new StepDaoImpl(mongoTemplate);
        try {
            return stepDao.readStep(key,value);
        }catch (Exception e){
            return "Fail";
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteStep(@RequestParam("StepId") String stepId){
        StepDaoImpl stepDao = new StepDaoImpl(mongoTemplate);
        try {
            stepDao.deleteStep("StepId",stepId);
            return "Success";
        }catch (Exception e){
            return "Fail";
        }
    }

    @RequestMapping(value = "/update", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
    public String updateModule(HttpServletRequest request){
        StepDaoImpl stepDao = new StepDaoImpl(mongoTemplate);
        return stepDao.updateStep(request);
    }
}