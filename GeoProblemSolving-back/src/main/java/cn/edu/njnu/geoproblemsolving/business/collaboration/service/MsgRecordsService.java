package cn.edu.njnu.geoproblemsolving.business.collaboration.service;

import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.ChatMsg;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.collaboration.repository.ChatMsgRepository;
import cn.edu.njnu.geoproblemsolving.business.collaboration.repository.MsgRecordsRepository;
import cn.edu.njnu.geoproblemsolving.business.user.dao.UserDao;
import cn.edu.njnu.geoproblemsolving.business.user.entity.UserEntity;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import cn.edu.njnu.geoproblemsolving.common.utils.ResultUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MsgRecordsService {

    @Autowired
    MsgRecordsRepository msgRecordsRepository;

    @Autowired
    ChatMsgRepository chatMsgRepository;

    @Autowired
    UserDao userDao;

    private HashMap<String, String> avatarUrl;
    {
        avatarUrl = new HashMap<>();
        avatarUrl.put("time", System.currentTimeMillis()+"");
    }


    public JsonResult msgRecordsCreate(String aid, ArrayList<String> records, ArrayList<String> participants) {
        try {
            MsgRecords msgRecords = new MsgRecords();

            msgRecords.setRecordId(UUID.randomUUID().toString());
            msgRecords.setAid(aid);
            msgRecords.setRecords(records);
            msgRecords.setParticipants(participants);
            msgRecords.setCreatedTime(new Date());

            return ResultUtils.success(msgRecordsRepository.save(msgRecords));
        } catch (Exception ex){
            return ResultUtils.error(-2, ex.toString());
        }
    }

    public JsonResult findMsgRecords(String aid){
        try {
            String time = avatarUrl.get("time");
            Long startTime = Long.valueOf(time);
            if (System.currentTimeMillis() - startTime > 1000 * 60 * 60 *24){
                avatarUrl.clear();
                avatarUrl.put("time", System.currentTimeMillis()+"");
            }
            List<MsgRecords> result = msgRecordsRepository.findByAid(aid);
            if (result.isEmpty()) return ResultUtils.error(-1, "Fail: Message records do not exist.");
            ArrayList<JSONObject> records = Lists.newArrayList();
            for (MsgRecords item : result){
                JSONObject record = JSONObject.parseObject(JSONObject.toJSONString(item));
                ArrayList<String> userIds = item.getParticipants();
                ArrayList<HashMap<String, String>> participants = new ArrayList<>();
                for (String userId: userIds){
                    HashMap<String, String> idAndAvatar = new HashMap<>();
                    idAndAvatar.put("userId", userId);
                    String aUrl = avatarUrl.get(userId);
                    if (aUrl == null){
                        UserEntity user = userDao.findUserByIdOrEmail(userId);
                        if (user.getAvatar() != null && !user.getAvatar().equals("")){
                            aUrl = user.getAvatar();
                            avatarUrl.put(userId, aUrl);
                        }
                    }
                    if (aUrl != null && !aUrl.equals("")){
                        idAndAvatar.put("avatar", aUrl);
                    }
                    participants.add(idAndAvatar);
                    record.put("participants", participants);
                }
                records.add(record);
            }
            return ResultUtils.success(records);
        } catch (Exception ex){
            return ResultUtils.error(-2, ex.toString());
        }
    }

    public JsonResult findChatRecord(String recordId){
        try {
            Optional optional = msgRecordsRepository.findById(recordId);
            if(!optional.isPresent()) return ResultUtils.error(-1, "Fail: Message record do not exist.");

            MsgRecords msgRecords = (MsgRecords)optional.get();
            List<ChatMsg> result = new ArrayList<>();
            for(String messageId: msgRecords.getRecords()) {
                Optional message = chatMsgRepository.findById(messageId);
                if(message.isPresent()) result.add((ChatMsg) message.get());
            }

            return ResultUtils.success(result);
        } catch (Exception ex){
            return ResultUtils.error(-2, ex.toString());
        }
    }
}
