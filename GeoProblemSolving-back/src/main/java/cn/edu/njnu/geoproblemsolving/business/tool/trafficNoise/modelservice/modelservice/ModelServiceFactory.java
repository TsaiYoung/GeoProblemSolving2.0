package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.modelservice.modelservice;

import cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.modelservice.enumcollection.ModelServiceLimitation;
import cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.modelservice.enumcollection.ModelServicePermission;
import cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.modelservice.enumcollection.ModelServiceStatus;
import cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.modelservice.enumcollection.Platform;
import com.alibaba.fastjson.JSONObject;

public class ModelServiceFactory {
	
	public static ModelService creatModelServiceByJSON(JSONObject jMs, String ip, int port){
		String oid = jMs.getString("_id");
		JSONObject modeljson = jMs.getJSONObject("ms_model");
		String name = modeljson.getString("m_name");
		String type = modeljson.getString("m_type");
		String url = modeljson.getString("m_url");
		String pid = modeljson.getString("p_id");
		String mid = modeljson.getString("m_id");
		boolean registered = modeljson.getBooleanValue("m_register");
		String des = jMs.getString("ms_des");
		String version = jMs.getString("mv_num");
		int msplatform = jMs.getInteger("ms_platform");
		Platform platform;
		if(msplatform == 1){
			platform = Platform.PLF_WINDOWS;
		}else if(msplatform == 2){
			platform = Platform.PLF_LINUX;
		}else if(msplatform == 3){
			platform = Platform.PLF_MACOS;
		}else{
			platform = Platform.PLF_UNKNOWN;
		}
		String deploymentTime = jMs.getString("ms_update");
		String img = jMs.getString("ms_img");
		String deployorname = jMs.getJSONObject("ms_user").getString("u_name");
		String deployoremail = jMs.getJSONObject("ms_user").getString("u_email");
		int msstatus = jMs.getInteger("ms_status");
		ModelServiceStatus status;
		if(msstatus == 0){
			status = ModelServiceStatus.STAT_OFFLINE;
		}else if(msstatus == 1){
			status = ModelServiceStatus.STAT_ONLINE;
		}else{
			status = ModelServiceStatus.STAT_DELETE;
		}
		
		int mslimit = jMs.getInteger("ms_limited");
		ModelServiceLimitation limit;
		if(mslimit == 0){
			limit = ModelServiceLimitation.LMT_PRIVATE;
		}else{
			limit = ModelServiceLimitation.LMT_PUBLIC;
		}
		int mspermission = jMs.getInteger("ms_permission");
		ModelServicePermission permission;
		if(mspermission == 0){
			permission = ModelServicePermission.PMS_OPEN;
		}else{
			permission = ModelServicePermission.PMS_PERMISSION;
		}
		
		ModelService pMs = new ModelService(oid,name,type,url,pid,mid,registered,des,version,platform,deploymentTime,
				img,deployorname,deployoremail,status,limit,permission,ip,port);
		return pMs;
	}

}
