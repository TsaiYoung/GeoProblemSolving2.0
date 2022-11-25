//package cn.edu.njnu.geoproblemsolving.Socket;
//
//import cn.edu.njnu.geoproblemsolving.Config.MyEndPointConfigure;
//
//import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.chatmessage.ChatMessageRecordsService;
//import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.AnsjSegService;
//import cn.edu.njnu.geoproblemsolving.business.user.dao.IUserDao;
//import cn.edu.njnu.geoproblemsolving.business.user.dto.InquiryUserDto;
//import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
//import com.alibaba.fastjson.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpSession;
//import javax.websocket.*;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @Author     ：Zhiyi
// * @Date       ：2020/12/15 12:21
// * @modified By：
// * @version:     1.0.0
// */
//@Component
//@ServerEndpoint(value = "/ChatServer/{roomId}", configurator = MyEndPointConfigure.class)
//public class ChatServerSocket {
//
//    private static final Map<String, Map<String, Session>> rooms = new ConcurrentHashMap<>();//在线用户
//    private static JSONObject messageJson = new JSONObject();
//    private static ArrayList<String> messagesArray = new ArrayList<>(); //信息缓存，用来发给新加入者
//    private static JSONObject members = new JSONObject();       // 在线成员
//
//    @Autowired
//    MongoTemplate mongoTemplate;
//
////    @Autowired
////    ChatMessageRecordsService chatMessageRecordsService;
//
//    @Autowired
//    AnsjSegService ansjSegService;
//
//    @Autowired
//    IUserDao iUserDao;
//
//    @OnOpen
//    public void onOpen(@PathParam("roomId") String roomId, Session session, EndpointConfig config) throws IOException {
//        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
//        String userId = httpSession.getAttribute("userId").toString();
//        String userName = ((HttpSession) config.getUserProperties().get(HttpSession.class.getName())).getAttribute("name").toString();
//
//
////        JsonResult userInfo =  iUserDao.getUserInfo("userId",userId);
////        String userAvatar = ((InquiryUserDto)userInfo.getData()).getAvatar();
//
//        //判断会话是否存在
//        if (rooms.containsKey(roomId)) {
//            rooms.get(roomId).put(userId, session);
//        } else {
//            Map<String, Session> room = new ConcurrentHashMap<>();
//            room.put(httpSession.getAttribute("userId").toString(), session);
//            rooms.put(roomId, room);
//        }
//
//        if (messageJson.containsKey(roomId)) {
//            try {
//                session.getBasicRemote().sendText(messageJson.get(roomId).toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        //用户信息
//        JSONObject user = new JSONObject();
//        user.put("userId", userId);
//        user.put("userName", userName);
////        user.put("userAvatar", userAvatar);
//        members.put(session.getId(), user);
//
//        broadcastMembersToRoom(roomId);
//        broadcastOnOffToRoom(roomId, userId, userName, "on");
//    }
//
//
//    @OnClose
//    public void onClose(@PathParam("roomId") String roomId, Session session) throws IOException {
//        for (Map.Entry<String, Session> server : rooms.get(roomId).entrySet()) {
//            if (server.getValue().equals(session)) {
//                rooms.get(roomId).remove(server.getKey());
//                break;
//            }
//        }
//        String userId = members.getJSONObject(session.getId()).getString("userId");
//        String userName = members.getJSONObject(session.getId()).getString("userName");
//        broadcastOnOffToRoom(roomId, userId, userName, "off");
//        members.remove(session.getId());
//
//        // 最后一个人退出后清理消息缓存
//        if (rooms.get(roomId).size() < 1) {
//            messagesArray.clear();
//            messageJson.put(roomId, messagesArray.toString());
//        }
//    }
//
//    @OnError
//    public void onError(@PathParam("roomId") Throwable error) {
//        System.out.println("发生错误");
//        error.printStackTrace();
//    }
//
//    //用户列表
//    private void broadcastMembersToRoom(String roomId) {
//        ArrayList<Object> memberList = new ArrayList<>();
//        for (Map.Entry<String, Session> server : rooms.get(roomId).entrySet()) {
//            JSONObject json = new JSONObject();
//            JsonResult userInfo =  iUserDao.getUserInfo("userId",server.getKey());
//            String userName = ((InquiryUserDto)userInfo.getData()).getName();
//            json.put("userId",server.getKey());
//            json.put("userName", userName);
//            memberList.add(json);
//        }
//        JSONObject messageObject = new JSONObject();
//        messageObject.put("type", "members");
//        messageObject.put("content", memberList);
//
//        String message = messageObject.toString();
//        for (Map.Entry<String, Session> server : rooms.get(roomId).entrySet()) {
//            try {
//                server.getValue().getBasicRemote().sendText(message);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void broadcastOnOffToRoom(String roomId, String userId, String userName, String behavior) throws IOException {
//        JSONObject messageObject = new JSONObject();
//        messageObject.put("type", "notice");
//        messageObject.put("behavior", behavior);
//        messageObject.put("srcUserId", userId);
//        if (behavior.equals("on")) {
//            messageObject.put("content", userName + " enter this room");
//        } else if (behavior.equals("off")) {
//            messageObject.put("content", userName + " leave this room");
//        }
//
//        String message = messageObject.toString();
//
//        for (Map.Entry<String, Session> server : rooms.get(roomId).entrySet()) {
//            if (!userId.equals(server.getKey())) {
//                server.getValue().getBasicRemote().sendText(message);
//            }
//        }
//    }
//
//    //接收消息后所调用的方法
//    @OnMessage
//    public void onMessage(@PathParam("roomId") String roomId, String message, Session session) throws IOException {
//        JSONObject messageObject = JSONObject.parseObject(message);
//        String messageType = messageObject.getString("type");
//        if (messageType.equals("message")) {
//            broadcastMessageToRoom(roomId, message);
//
//            // 添加消息至缓存
//            messagesArray.add(message);
//            messageJson.put(roomId, messagesArray.toString());
//        } else if (messageType.equals("message_pic") || messageType.equals("message_tool")) {
//            broadcastMessageToRoom(roomId, message);
//        }
//    }
//
//
//    private void broadcastMessageToRoom(String roomId, String message) throws IOException {
//        JSONObject messageObject = JSONObject.parseObject(message);
//        String srcUserId = messageObject.getString("srcUserId");
//        String targetUserId = messageObject.getString("targetUserId");
//        String content = messageObject.getString("content");
//        Boolean concepts = messageObject.getBooleanValue("geoConcepts");
//
//        if(concepts == null) concepts = false;
//        String relateConceptSet = "";
//        if(concepts) {
//            String result = ansjSegService.processInfo(content);
//            relateConceptSet = ansjSegService.elasticSearch(result);
//        }
//
//        //群聊
//        if (srcUserId.equals(targetUserId)) {
//            for (Map.Entry<String, Session> server : rooms.get(roomId).entrySet()) {
//                if (!srcUserId.equals(server.getKey())) {
//                    server.getValue().getBasicRemote().sendText(message);
//                }
//                if(relateConceptSet.equals("")) {
//                    server.getValue().getBasicRemote().sendText(relateConceptSet);
//                }
//            }
//        } else {
//            //私聊
//            for (Map.Entry<String, Session> server : rooms.get(roomId).entrySet()) {
//                if (targetUserId.equals((server.getKey()))) {
//                    server.getValue().getBasicRemote().sendText(message);
//                }
//                if(relateConceptSet.equals("")) {
//                    server.getValue().getBasicRemote().sendText(relateConceptSet);
//                }
//            }
//        }
//        saveMessage(roomId, message);
//    }
//
//
//    public void saveMessage(String roomId, String message) {
//        JSONObject messageObject = JSONObject.parseObject(message);
//        String type = messageObject.getString("type");
//        String targetUserId = messageObject.getString("targetUserId");
//        String targetUserName = messageObject.getString("targetUserName");
//        String srcUserId = messageObject.getString("srcUserId");
//        String srcUserName = messageObject.getString("srcUserName");
//        String content = messageObject.getString("content");
//
//        // 添加消息至缓存
//        messagesArray.add(message);
//        messageJson.put(roomId, messagesArray.toString());
//
////        AddChatMessageRecordsDTO addChatMessageRecordsDTO = new AddChatMessageRecordsDTO();
////        addChatMessageRecordsDTO.setSrcUserId(srcUserId);
////        addChatMessageRecordsDTO.setSrcUserName(srcUserName);
////        addChatMessageRecordsDTO.setTargetUserId(targetUserId);
////        addChatMessageRecordsDTO.setContent(content);
////        addChatMessageRecordsDTO.setType(type);
////        addChatMessageRecordsDTO.setMessageId(UUID.randomUUID().toString());
////        addChatMessageRecordsDTO.setRoomId(roomId);
////        chatMessageRecordsService.insert(addChatMessageRecordsDTO);
//    }
//}
