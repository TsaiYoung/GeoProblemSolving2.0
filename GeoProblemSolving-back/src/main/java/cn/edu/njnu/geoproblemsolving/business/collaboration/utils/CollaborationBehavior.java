package cn.edu.njnu.geoproblemsolving.business.collaboration.utils;

import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.ChatMsg;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.ComputeMsg;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.MsgRecords;
import cn.edu.njnu.geoproblemsolving.business.collaboration.repository.ChatMsgRepository;
import cn.edu.njnu.geoproblemsolving.business.collaboration.service.MsgRecordsService;
import cn.edu.njnu.geoproblemsolving.business.user.dao.UserDao;
import cn.edu.njnu.geoproblemsolving.business.user.dto.InquiryUserDto;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.CollaborationUser;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;


@Service
public class CollaborationBehavior {

    @Autowired
    UserDao iUserDao;

    @Autowired
    MsgRecordsService msgRecordsService;

    @Autowired
    ChatMsgRepository chatMsgRepository;

    /**
     * common
     **/
    public CollaborationUser getMemberInfo(String userId, Session session) {

        JsonResult userInfo = iUserDao.getUserInfo("userId", userId);


        CollaborationUser collaborationUser = new CollaborationUser();

        if (userInfo.getCode() != 0) {
            return collaborationUser;
        }
        collaborationUser.setUserId(userId);
        collaborationUser.setName(((InquiryUserDto) userInfo.getData()).getName());
        collaborationUser.setEmail(((InquiryUserDto) userInfo.getData()).getEmail());
        collaborationUser.setAvatar(((InquiryUserDto) userInfo.getData()).getAvatar());
        collaborationUser.setSession(session);

        return collaborationUser;
    }

    public void sendCollaborationStatus(CollaborationConfig config, Session session) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", "collaboration-init");
            messageObject.put("mode", config.getMode().toString());
            messageObject.put("operator", getMemberInfo(config.getOperator(), null));
            messageObject.put("waiting", config.getApplyQueue().size());
            session.getBasicRemote().sendText(messageObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendParticipantsInfo(HashMap<String, CollaborationUser> participants, CollaborationUser user, String behavior) {
        try {
            if (user == null) return;
            if (participants == null || participants.size() == 0) return;
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", "members");

            ArrayList members = new ArrayList();
            for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                members.add(participant.getValue().getUserInfo());
            }
            messageObject.put("participants", members);

            for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                CollaborationUser receiver = participant.getValue();
                messageObject.put("behavior", behavior);
                //发送者
                messageObject.put("activeUser", user.getUserInfo());
                receiver.getSession().getBasicRemote().sendText(messageObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Message
     **/
    public void sendMessageCache(ArrayList<ChatMsg> msgRecords, Session session) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", "message-cache");
            messageObject.put("content", msgRecords);
            session.getBasicRemote().sendText(messageObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void transferMessage(String type, HashMap<String, CollaborationUser> participants, CollaborationUser sender, List<String> receivers, String message, String time) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", type);
            messageObject.put("sender", sender.getUserInfo());
            messageObject.put("content", message);
            messageObject.put("time", time);

            if (receivers.size() == 0) {
                messageObject.put("receivers", "");
                // send message
                for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                    user.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                }
            } else {
                if(!receivers.contains(sender.getUserId())) {
                    receivers.add(sender.getUserId());
                }
                messageObject.put("receivers", receivers);
                // send message
                for (String receiver : receivers) {
                    for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                        if (user.getValue().getUserId().equals(receiver)) {
                            user.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void transferConceptMessage(HashMap<String, CollaborationUser> participants, CollaborationUser sender, List<String> receivers, String message, String time) {
        try {
            JSONObject messageObject = (JSONObject) JSONObject.parse(message);
            messageObject.put("sender", sender.getUserInfo());
            messageObject.put("time", time);

            if (receivers.size() == 0) {
                // send message
                for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                    user.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                }
            } else {
                // send message
                for (String receiver : receivers) {
                    for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                        if (user.getValue().getUserId().equals(receiver)) {
                            user.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStoredMsgRecords(HashMap<String, CollaborationUser> participants, MsgRecords msgRecords) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", "message-store");
            messageObject.put("time", msgRecords.getCreatedTime());
            messageObject.put("recordId", msgRecords.getRecordId());
            messageObject.put("participants", msgRecords.getParticipants());

            for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                CollaborationUser receiver = participant.getValue();
                receiver.getSession().getBasicRemote().sendText(messageObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStoredMsgRecords(HashMap<String, CollaborationUser> participants, String oid) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", "message-store");
            messageObject.put("oid", oid);

            for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                CollaborationUser receiver = participant.getValue();
                receiver.getSession().getBasicRemote().sendText(messageObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将聊天记录按时段存储
    public MsgRecords msgCacheStore(String aid, ArrayList<ChatMsg> records) {
        // save each message
        ArrayList<String> msgList = new ArrayList<>();
        ArrayList<String> memberIds = new ArrayList<>();

        for (ChatMsg message : records) {
            msgList.add(message.getMessageId());

            if (!memberIds.contains(message.getSender().getUserId())) {
                memberIds.add(message.getSender().getUserId());
            }

            chatMsgRepository.save(message);
        }
        // save message records
        MsgRecords msgRecords = (MsgRecords) msgRecordsService.msgRecordsCreate(aid, msgList, memberIds).getData();
        return msgRecords;
    }


    /**
     * operation
     **/
    public void transferOperation(HashMap<String, CollaborationUser> participants, String type, CollaborationUser sender, List<String> receivers, String behavior, String object) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", type);
            messageObject.put("sender", sender.getUserInfo());
            messageObject.put("behavior", behavior);
            messageObject.put("content", object);

            if (receivers.size() == 0) {
                messageObject.put("receivers", "");
                // send message
                for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                    user.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                }
            } else {
                messageObject.put("receivers", receivers);
                // send message
                for (String receiever : receivers) {
                    for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                        if (user.getValue().getUserId().equals(receiever)) {
                            user.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void operationRefuse(HashMap<String, CollaborationUser> participants, String type, String sender) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", type);
            messageObject.put("behavior", "Refuse");

            // send message
            for (Map.Entry<String, CollaborationUser> user : participants.entrySet()) {
                if (user.getValue().getUserId().equals(sender)) {
                    user.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送协同模式
     *
     * @param participants
     * @param mode
     */
    public void sendModeType(HashMap<String, CollaborationUser> participants, String mode) {
        try {
            for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                JSONObject messageObject = new JSONObject();
                messageObject.put("type", "mode");
                messageObject.put("content", mode);

                participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送控制信息
     *
     * @param config
     * @param queue
     * @param sender
     * @param type
     */
    public void sendControlInfo(CollaborationConfig config, List<String> queue, CollaborationUser sender, String type) {
        try {
            HashMap<String, CollaborationUser> participants = config.getParticipants();
            JSONObject messageObject = new JSONObject();
            messageObject.put("type", type);

            if (type.equals("control-apply")) {
                messageObject.put("sender", sender.getUserInfo());
                messageObject.put("waiting", queue.size());
                messageObject.put("operator", config.getOperator());
                // send message
                for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                    if(participant.getValue().getUserId().equals(config.getOperator())){
                        messageObject.put("waiting", -1);
                        participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                    } else if (queue.contains(participant.getValue().getUserId())) {
                        for (int i = 0; i < queue.size(); i++) {
                            if (queue.get(i).equals(participant.getValue().getUserId())) {
                                messageObject.put("waiting", i + 1);
                                participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                                break;
                            }
                        }
                    } else {
                        messageObject.put("waiting", 0);
                        participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                    }
                }
            } else if (type.equals("control-stop")) {
                messageObject.put("operator", config.getOperator());
                // send message
                for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                    if(participant.getValue().getUserId().equals(sender.getUserId())){
                        messageObject.put("waiting", 0);
                        participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                    } else if(participant.getValue().getUserId().equals(config.getOperator())){
                        messageObject.put("waiting", -1);
                        participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                    } else if (queue.contains(participant.getValue().getUserId())) {
                        for (int i = 0; i < queue.size(); i++) {
                            if (queue.get(i).equals(participant.getValue().getUserId())) {
                                messageObject.put("waiting", i + 1);
                                participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                                break;
                            }
                        }
                    } else {
                        messageObject.put("waiting", 0);
                        participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param participants  //当前的协同者
     * @param oldReceivers  //invoke时的协同者
     * @param computeResult //计算结果
     * @throws IOException
     */
    public void sendComputeResult(HashMap<String, CollaborationUser> participants, HashMap<String, CollaborationUser> oldReceivers, ComputeMsg computeResult) throws IOException {
        ArrayList<String> oldReceiverStr = Lists.newArrayList();
        for (Map.Entry<String, CollaborationUser> oldReceiver : oldReceivers.entrySet()) {
            oldReceiverStr.add(oldReceiver.getKey());
        }
        JSONObject messageObject = new JSONObject();
        messageObject.put("type", "computation");
        messageObject.put("computeSuc", computeResult.isComputeSuc());
        if (computeResult.isComputeSuc()){
            //模型运算输出
            messageObject.put("outputInfo", computeResult.getOutputs());
            //生成的资源
            messageObject.put("operationId", computeResult.getOid());
        }
        for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
            for (int i = 0; i < oldReceiverStr.size(); i++) {
                if (oldReceiverStr.get(i).equals(participant.getValue().getUserId())) {
                    participant.getValue().getSession().getBasicRemote().sendText(messageObject.toString());
                }
            }
        }
    }

    public void sendTasKAssignment(HashMap<String, CollaborationUser> participants, CollaborationUser sender, JSONObject msgJson, String receiverId) throws IOException {
        try {
            if (receiverId != null){
                CollaborationUser collaborationUser = participants.get(receiverId);
                collaborationUser.getSession().getBasicRemote().sendText(msgJson.toString());
                return;
            }
            for (Map.Entry<String, CollaborationUser> participant : participants.entrySet()) {
                // the message didn't need to receive the message.
                if (participant.getValue().getUserId().equals(sender.getUserId())) continue;
                participant.getValue().getSession().getBasicRemote().sendText(msgJson.toString());
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

}



