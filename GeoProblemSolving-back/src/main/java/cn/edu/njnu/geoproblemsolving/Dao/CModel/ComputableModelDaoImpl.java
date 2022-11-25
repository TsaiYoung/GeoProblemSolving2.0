package cn.edu.njnu.geoproblemsolving.Dao.CModel;

import cn.edu.njnu.geoproblemsolving.Entity.ModelTools.CModel.ComputableModelEntity;
import cn.edu.njnu.geoproblemsolving.Entity.ModelTools.ModelItem.ModelItemEntity;
import cn.edu.njnu.geoproblemsolving.Entity.ModelTools.ModelItem.Support.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;


import java.util.*;


@Component
public class ComputableModelDaoImpl implements ComputableModelDao {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ComputableModelDaoImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Object readComputableModel(String oid) {
        Query query = Query.query(Criteria.where("oid").is(oid));
        if (mongoTemplate.find(query, ComputableModelEntity.class).isEmpty()) {
            return "None";
        } else {
            List<ComputableModelEntity> computableModelEntity = mongoTemplate.find(query, ComputableModelEntity.class);
            return computableModelEntity;
        }
    }

    @Override
    public ModelItemEntity addDataTemplate() {
        List<ComputableModelEntity> computableModelEntityList = mongoTemplate.findAll(ComputableModelEntity.class);
        ComputableModelEntity computableModelEntity = computableModelEntityList.get(0);
        Object mdlJson2 = computableModelEntity.getMdlJson();
        JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(mdlJson2));
        JSONObject jsonInfo = jsonObject.getJSONArray("ModelClass").getJSONObject(0);
        JSONArray jsonStates = jsonObject.getJSONArray("ModelClass").getJSONObject(0).getJSONArray("Behavior").getJSONObject(0).getJSONArray("StateGroup").getJSONObject(0).getJSONArray("States").getJSONObject(0).getJSONArray("State");
        JSONArray jsonEvents = ((JSONObject) (jsonStates.get(0))).getJSONArray("Event");

        ModelItemEntity modelItemEntity = new ModelItemEntity();
        List<State> stateList = new ArrayList<>();

        FileDataTemplate fileDataTemplate = new FileDataTemplate();
        fileDataTemplate.setValue("");
        fileDataTemplate.setType("file");
        fileDataTemplate.setTooltip("");

        for (int j = 0; j < jsonStates.size(); j++) {
            List<Event> eventList = new ArrayList<>();
            State state = new State();

            for (int i = 0; i < jsonEvents.size(); i++) {
                JSONObject obj = (JSONObject) jsonEvents.get(i);

                Event event = Event.builder()
                        .dataTemplate(fileDataTemplate)
                        .des(obj.getString("description"))
                        .name(obj.getString("name"))
                        .isOptional(obj.getBooleanValue("optional"))
                        .ioFlagEnum(obj.getString("type").equals("response") ? IOFlagEnum.INPUT : IOFlagEnum.OUTPUT)
                        .build();

                eventList.add(event);
            }
            JSONObject objState = (JSONObject) jsonStates.get(j);

            state.setDes(objState.get("description").toString());
            state.setName(objState.get("name").toString());
            state.setType(objState.get("type").toString());
            state.setId(objState.get("id").toString());
            state.setEventList(eventList);
            stateList.add(state);
        }


        modelItemEntity.setOid(computableModelEntity.getOid());
        modelItemEntity.setMd5(computableModelEntity.getMd5());//获得计算模型的oid&md5
        modelItemEntity.setStateList(stateList);
        modelItemEntity.setName(jsonInfo.get("name").toString());
        modelItemEntity.setDes(jsonInfo.get("type").toString());
        mongoTemplate.save(modelItemEntity);

//        ModelItemEntity modelItemEntity1=new ModelItemEntity();
//        ArrayList<State> objects = new ArrayList<>();
//        ArrayList<Event>  events= new ArrayList<>();
//
//        Event e = new Event();
//        ParameterInputDataTemplate parameterInputDataTemplate = new ParameterInputDataTemplate();
//        parameterInputDataTemplate.setDefaultValue("123");
//        parameterInputDataTemplate.setTooltip("123");
//        parameterInputDataTemplate.setValue("123");
//        parameterInputDataTemplate.setType("parameter_input");
//        e.setDataTemplate(parameterInputDataTemplate);
        return modelItemEntity;
    }
}
