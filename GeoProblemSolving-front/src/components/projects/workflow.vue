<template>
	<div style="display: flex; background-color: #eee; height: calc(100vh - 130px)" id="workflow">
		<Menu active-name="projectInfo" style="width: 9%; position:ablusote; z-index: 0;" @on-select="changeIntro">
			<MenuGroup title="Project">
				<MenuItem name="projectInfo" >
					<Icon type="md-document" />
					Project Info
				</MenuItem>
				<MenuItem name="memberInfo" >
					<Icon type="md-chatbubbles" />
					Member Info
				</MenuItem>
			</MenuGroup>
			<MenuGroup title="Resource and Tool">
				<MenuItem name="resource">
					<Icon type="md-cloud" />
					Resource
				</MenuItem>
				<MenuItem name="tool" >
					<Icon type="ios-cog" />
					Tool
				</MenuItem>
			</MenuGroup>
			<MenuGroup title="Template">
				<MenuItem name="templateInfo">
					<Icon type="md-information-circle" />
					Template Info
				</MenuItem>
				<!-- <MenuItem name="2" >
					<Icon type="md-leaf" />
					Tool
				</MenuItem> -->
			</MenuGroup>
		</Menu>
		<Card
			dis-hover
			class="infoCard"
			id="infoCard"
			style="height: calc(100vh - 130px)"
		>
			<div v-if="introType == 'projectInfo'">
				<h2 style="margin-top:5px; text-align:center;">{{projectInfo.name}}</h2>
				<div style="margin-top:15px;" v-if="getProjectFinish">
					<img
						v-if="projectInfo.picture != null && projectInfo.picture != '' && projectInfo.picture != undefined"
						:src="projectInfo.picture"
						:title="projectInfo.name"
						style="margin-left: 35%;margin-top:10px; width: 100px; height: 100px; border-radius:50%; "
					/>
					<avatar
						:size="100"
						:username="projectInfo.name"
						style="margin-top:10px;margin-left: 35%;"
						v-else
					/>
				</div>
				<div style="heigth: 70%; width: 80%; float: left; margin-top:20px;">
					<div style="margin: 10px 0">
						<Label>Name:</Label>
						<span style="margin-left: 10px">{{ projectInfo.name }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Category:</Label>
						<span style="margin-left: 10px">{{ projectInfo.category }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Privacy:</Label>
						<span style="margin-left: 10px">{{ projectInfo.privacy }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Tag:</Label>
						<span style="margin-left: 10px">{{ projectInfo.tag }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Created Time:</Label>
						<span style="margin-left: 10px">{{ projectInfo.createdTime }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Description:</Label>
						<span style="margin-left: 10px">{{ projectInfo.description }}</span>
					</div>

				</div>
			</div>
			<div v-if="introType == 'memberInfo'">
				<h2 style="margin-top:5px; text-align:center; margin-bottom: 25px;">{{projectInfo.name}}</h2>
				<span style="font-size: 1.17em; font-weight: bold;">Participants</span>
					<Icon
						v-if="
							permissionIdentity(
								projectInfo.permission,
								userRole,
								'manage_member'
							)
						"
						type="md-people"
						size="16"
						title="Change user role"
						@click="userRoleBtn = !userRoleBtn"
						style="
							float: right;
							margin: 5px 10px 0 0;
							cursor: pointer;
							color: #8bc34a;
						"
					/>
				<vue-scroll
					:ops="scrollOps"
					style="margin-top: 10px; height: calc(100vh - 280px)"
				>
					<Card
						style="margin: 5px 0"
						:padding="5"
						v-for="member in participants"
						:key="member.userId"
					>
					<div style="display: flex; align-items: center">
						<div
							v-if="
								userRoleBtn &&
								member.userId != userInfo.userId &&
								roleCompare(userRole, member.role) != -1
							"
							title="Set user role"
							style="cursor: pointer; margin-right: 10px"
							@click="selectMember(member, 'role')"
						>
							<Icon type="md-people" size="20" color="#8bc34a" />
						</div>
						<div
							v-if="
								userRoleBtn &&
								(member.userId == userInfo.userId ||
								roleCompare(userRole, member.role) == -1)
							"
							:title="
								member.role.charAt(0).toUpperCase() + member.role.slice(1)
							"
							style="cursor: default; margin-right: 10px"
						>
							<Icon type="md-people" size="20" color="grey" />
						</div>
						<div
							@click="gotoPersonalSpace(member.userId)"
							style="display: flex; align-items: center; cursor: pointer"
						>
							<div class="memberImg" style="position: relative">
								<img
									v-if="member.avatar != '' && member.avatar != undefined"
									:src="avatarUrl(member.avatar)"
									style="width: 40px; height: 40px"
								/>
								<avatar
									v-else
									:username="member.name"
									:size="40"
									:rounded="true"
								/>
								<div class="onlinecircle"></div>
							</div>
							<div class="memberDetail">
								<div class="memberName">
									<span>{{ member.name }}</span>
								</div>
								<div class="memberRole">
									<span>{{
										member.role.charAt(0).toUpperCase() + member.role.slice(1)
									}}</span>
								</div>
							</div>
						</div>
					</div>
					</Card>
				</vue-scroll>
			</div>
			<div v-if="introType == 'resource'">
				<h2 style="margin-top:5px; text-align:center; margin-bottom: 25px;">{{projectInfo.name}}</h2>
				<res-list :activityInfo="projectInfo" ></res-list>
			</div>
			<div v-if="introType == 'tool'">
				<h2 style="margin-top:5px; text-align:center; margin-bottom: 25px;">{{projectInfo.name}}</h2>
				<tool-box :activityInfo="projectInfo" :projectInfo="projectInfo"></tool-box>
			</div>
		</Card>
		<Card
			dis-hover
			class="mxGraphCard"
			id="mxGraphCard"
			style="height: calc(100vh - 130px)"
		>
			<div style="margin-top: 40px;">
				<h3 style="position:absolute; left:3%; top:3%; ">Workflow</h3>
				<div style="position:absolute; right:3%; z-index:1;margin-top:10px;">
					<button @click="zoomActual()" style=" cursor: pointer;">ZoomActual</button>
					<button @click="getXml()"  style=" cursor: pointer;">XmlOutput</button>
				</div>
				<div style="position:absolute; right:26%; z-index:1;margin-top:10px;">
					<Button type="primary" @click="zoomIn()" shape="circle" icon="md-add" title="Zoom In"></Button>
					<Button type="primary" @click="zoomOut()" shape="circle" icon="md-remove" title="Zoom Out"></Button>
				</div>
				<div id="graphContainer" :style="{height:heightChange()}"></div>
			</div>

		</Card>
		<Card
			dis-hover
			class="modalCard"
			id="moadalCard"
			style="height: calc(100vh - 130px)"
		>
			<h2 v-show="tempToolInfo.value != undefined" style="margin-top:5px; text-align:center; margin-bottom: 25px;">{{tempToolInfo.value}}</h2>
			<Card dis-hover style="height:600px; width: 500px;margin: auto;"  v-show="tempToolInfo.vertex != undefined && tempToolInfo.vertex">
				<div style="heigth: 70%; width: 90%; float: left; margin-top:10px; margin-left:15px;">
					<Divider orientation="left">Operation Information</Divider>
					<div style="margin: 10px 0">
						<Label>Operation Name:</Label>
						<span style="margin-left: 10px">{{ tempToolInfo.value }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Id:</Label>
						<span style="margin-left: 10px">{{ tempToolInfo.id }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Style:</Label>
						<span style="margin-left: 10px">{{ tempToolInfo.style }}</span>
					</div>
					<Divider orientation="left" style="margin: 30px 0;">Tool Information</Divider>
					<div style="margin-top:15px;" v-if="getToolInfoFinish">
						<img
							v-if="selectToolInfo.toolImg != null && selectToolInfo.toolImg != '' && selectToolInfo.toolImg != undefined"
							:src="selectToolInfo.toolImg"
							:title="selectToolInfo.toolName"
							style="margin-left: 35%;margin-top:10px; width: 70px; height: 70px; border-radius:50%; "
						/>
						<avatar
							:size="70"
							:username="selectToolInfo.toolName"
							style="margin-top:10px;margin-left: 35%;"
							v-else
						/>
					</div>
					<div style="margin: 10px 0">
						<Label>Tool Name:</Label>
						<span style="margin-left: 10px">{{ selectToolInfo.toolName }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Privacy:</Label>
						<span style="margin-left: 10px">{{ selectToolInfo.privacy }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Tags:</Label>
						<span style="margin-left: 10px">{{ selectToolInfo.tags }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Created Time:</Label>
						<span style="margin-left: 10px">{{ selectToolInfo.createdTime }}</span>
					</div>
					<div style="margin: 10px 0">
						<Label>Description:</Label>
						<span style="margin-left: 10px">{{ selectToolInfo.description }}</span>
					</div>
				</div>
			</Card>
		</Card>
		<Modal v-model="vertexInfoShow" title="Vertex Infomation">
			<div>
				<span>Id:</span> {{selectVertex.id}} <br>
				<span>Style:</span> {{selectVertex.style}} <br>
				<span>Name:</span> {{selectVertex.value}}
			</div>
			<div slot="footer" style="display: inline-block">
				<Button type="primary" @click="vertexInfoShow = false" style="float: right; height: 30px; width:70px"
				>OK
				</Button>
				<Button @click="vertexInfoShow = false" style="float: right; margin-right: 15px; height: 30px; width:70px"
				>Cancel
				</Button>
			</div>
		</Modal>
		<Modal
			v-model="memberRoleModal"
			width="400px"
			title="Set the role of this member"
			@on-ok="updateUserRole()"
			ok-text="Ok"
			cancel-text="Cancel"
		>
			<h4>Select user role</h4>
			<Select v-model="roleSelected" text="Role" style="margin: 10px 0">
				<Option value="manager" v-show="roleCompare(userRole, 'manager') != -1"
				>Manager</Option
				>
				<Divider style="margin: 5px 0"></Divider>
				<Option value="core" disabled>Core team</Option>
				<Option
					value="researcher"
					v-show="roleCompare(userRole, 'researcher') != -1"
				>Researcher</Option
				>
				<Option value="expert" v-show="roleCompare(userRole, 'expert') != -1"
				>Expert</Option
				>
				<Option
					value="decision-maker"
					v-show="roleCompare(userRole, 'decision-maker') != -1"
				>Decision-maker</Option
				>
				<Option
					value="core-member"
					v-show="roleCompare(userRole, 'core-member') != -1"
				>Core-member</Option
				>
				<Divider style="margin: 5px 0"></Divider>
				<Option value="ordinary" disabled>Ordinary team</Option>
				<Option
					value="stakeholder"
					v-show="roleCompare(userRole, 'stakeholder') != -1"
				>Stakeholder</Option
				>
				<Option
					value="consultant"
					v-show="roleCompare(userRole, 'consultant') != -1"
				>Consultant</Option
				>
				<Option
					value="ordinary-member"
					v-show="roleCompare(userRole, 'ordinary-member') != -1"
				>Ordinary-member</Option
				>
			</Select>
		</Modal>
	</div>
</template>

<script>
	import mxgraph from './MxGraph/index';
	import Avatar from "vue-avatar";
	import resList from "../workingSpace/activity/utils/resList.vue";
	import toolBox from "../workingSpace/activity/utils/toolBox.vue";

	const {mxGraph, mxClient, mxEvent, mxCodec, mxUtils, mxConstants, mxPerimeter,mxHierarchicalLayout} = mxgraph;

	export default {
		name: "mxgraph",
			data(){
				return{
					scrollOps: {
						bar: {
						background: "lightgrey",
						},
					},
					userRole: 'visitor',
      				userRoleBtn: false,
					userInfo: {},
					projectInfo: {},
					getProjectFinish: false,
					getToolInfoFinish: false,
					creatorInfo: {},
					participants: [],
					introType: "projectInfo",
					graph: null,
					geoAnalysisList: [],
					inputList: [],
					outputList: [],
					vertexList: [],
					edgeList: [],
					operationList:[
						{
							id: "1001",
							name: "geo-analysis1",
							type: "geo-analysis",
							resList:[
								{
									type: "input",
									resId: "2001"
								},
								{
									type: "input",
									resId: "2002"
								},
								{
									type: "output",
									resId: "2003"
								},
								{
									type: "output",
									resId: "2004"
								},
							],
						},
						{
							id: "1002",
							name: "geo-analysis2",
							type: "geo-analysis",
							resList:[
								{
									type: "input",
									resId: "2004"
								},
								{
									type: "input",
									resId: "2005"
								},
								{
									type: "output",
									resId: "2006"
								},
								{
									type: "output",
									resId: "2007"
								},
							],
						},
						{
							id: "1003",
							name: "tool1",
							type: "tool",
						},
						{
							id: "1004",
							name: "geo-analysis3",
							type: "geo-analysis",
							resList:[
								{
									type: "input",
									resId: "2003"
								},
								{
									type: "input",
									resId: "2006"
								},
								{
									type: "output",
									resId: "2010"
								},
								{
									type: "output",
									resId: "2011"
								},
							],
						},
						{
							id: "1005",
							name: "geo-analysis4",
							type: "geo-analysis",
							resList:[
								{
									type: "input",
									resId: "2004"
								},
								{
									type: "input",
									resId: "2010"
								},
								{
									type: "input",
									resId: "2003"
								},
								{
									type: "output",
									resId: "2012"
								},
							],
						},
						{
							id: "1006",
							name: "geo-analysis5",
							type: "geo-analysis",
							resList:[
								{
									type: "input",
									resId: "2011"
								},
								{
									type: "input",
									resId: "2012"
								},
								{
									type: "input",
									resId: "2015"
								},
								{
									type: "output",
									resId: "2013"
								},
								{
									type: "output",
									resId: "2014"
								},
							],
						},
						{
							id: "1007",
							name: "geo-analysis6",
							type: "geo-analysis",
							resList:[
								{
									type: "input",
									resId: "2001"
								},
								{
									type: "input",
									resId: "2002"
								},
								{
									type: "output",
									resId: "2003"
								},
								{
									type: "output",
									resId: "2015"
								},
							],
						},

					],
					selectVertex: {},
					vertexInfoShow: false,
      				slctRoleMember: {},
					memberRoleModal: false,
					roleSelected: "",
					tempToolInfo: {},
					selectToolInfo: {},
				}
			},
		components:{
			Avatar,
			resList,
    		toolBox,
		},
		created() {
			this.getProjectInfo();
		},
		mounted () {

			this.initMxGraph();
		    this.listenGraphEvent();

        },
	    beforeDestroy() {
			window.removeEventListener("message", this.toolMsgHandle, false);
		},
	    methods:{
			getProjectInfo(){
				this.userInfo = this.$store.getters.userInfo;
				let urlInfo = this.getUrlInfo();
				this.$axios
					.get("/GeoProblemSolving/project/" + urlInfo)
					.then((res) => {
						if (res.data.code == 0) {
							// this.$set(this, "projectInfo", res.data.data[0])
							this.projectInfo = res.data.data;
							console.log(res.data.data);
							this.getProjectFinish = true;
							this.userRole = this.roleIdentity(this.projectInfo);
							this.operationApi.getActivityDoc(this.projectInfo.aid);
							this.getParticipants();
						} else {
							console.log(res.data.msg);
						}
					})
					.catch((err) => {
					this.$Message.error("Loading project failed.");
					});
			},
			getUrlInfo() {
				let url = window.location.href;
				let result = {};
				if (url.indexOf("/workflow/") != -1) {
					let urlStr = url.split("/workflow/");
					result = urlStr[1];
				}
				return result;
			},
			getParticipants() {
				let url = "";
				let activity = this.projectInfo;
				if (activity.level == 0) {
					url = "/GeoProblemSolving/project/" + activity.aid + "/user";
				} else if (activity.level == 1) {
					url = "/GeoProblemSolving/subproject/" + activity.aid + "/user";
				} else if (activity.level > 1) {
					url = "/GeoProblemSolving/activity/" + activity.aid + "/user";
				}
				console.log(this.projectInfo);
				//callback setTimeBack
				this.axios
					.get(url)
					.then((res) => {
						if (res.data.code == 0) {
							this.creatorInfo = res.data.data.creator;
							this.participants = res.data.data.members;
						} else {
							console.log(res.data.msg);
						}
						})
					.catch((err) => {
						throw err;
					});
			},
			roleIdentity(activity) {
				return this.userRoleApi.roleIdentify(
					activity.members,
					this.userInfo.userId
				);
			},
			permissionIdentity(permission, role, operation) {
				if (permission == undefined)
					permission = JSON.stringify(this.userRoleApi.getDefault());
				if (operation == "auto_join") {
					if (JSON.parse(permission).auto_join.visitor == "Yes") return true;
					else if (JSON.parse(permission).auto_join.visitor == "No") return false;
					else {
						return this.getParentPermission();
					}
				} else {
					return this.userRoleApi.permissionIdentity(
						JSON.parse(permission),
						role,
						operation
					);
				}
			},
			changeIntro(type){
				this.introType = type;
			},
			initMxGraph(){
				if (!mxClient.isBrowserSupported()) {
					// 判断是否支持mxgraph
					mxUtils.error('Browser is not supported!', 200, false);
				} else {
					// 在容器中创建图表
					let container = document.getElementById('graphContainer');
					let MxGraph = mxGraph;
					let MxCodec = mxCodec;
					var graph = new MxGraph(container);
					this.graph = graph;
					var parent = graph.getDefaultParent();

					//定义布局
					var layout = new mxHierarchicalLayout(graph);

					graph.getModel().beginUpdate();
					try {
						this.operationListToGraph();
						layout.execute(parent);
						graph.center(true,true,0.1,0.1);

						// graph.setEnabled(false);//graph只能预览
						graph.setCellsResizable(false);//节点不可改变大小
						mxGraphHandler.prototype.setMoveEnabled(false);//是否可以移动
						graph.setPanning(true);//拖动
						//禁用浏览器默认的右键菜单栏
						mxEvent.disableContextMenu(container);
						graph.setCellsMovable(false);//线是否可移动​
						// 是否可以移动连线，重新连接其他cell，主要用来展现中用
						graph.setCellsLocked(true);

					} finally {
						// Updates the display
						graph.getModel().endUpdate();
					}
					this.graph = graph;
					// this.zoomActual();
					// this.restoreModel();
				}
			},

			operationListToGraph(){
				let list = this.operationList;
				//读取input和output信息，并生成vertex
				list.forEach((item, index) =>{
					if(item.type == "geo-analysis"){
						if(item.resList.length > 0){
							for(let i = 0 ; i < item.resList.length ; i++){
								if(item.resList[i].type == "input"){
									this.inputList.push({
										resId: item.resList[i].resId,
										type: item.resList[i].type,
										vertexId: item.id,
									})
								} else {
									this.outputList.push({
										resId: item.resList[i].resId,
										type: item.resList[i].type,
										vertexId: item.id,
									})
								}
							}
						}
						let vertex = this.graph.insertVertex(
							this.graph.getDefaultParent(),
							item.id,
							item.name,
							200,
							200,
							150, //width
							30, //height
							"noedeStyle"
						);

						//绑定相关信息
						vertex.nodeAttribute = {};
						vertex.nodeAttribute.resList = item.resList;

						this.vertexList.push(vertex);
					}
				});

				//利用input和output信息 生成边edge
				for(let i = 0 ; i < this.inputList.length ; i++){
					for(let j = 0 ; j < this.outputList.length ; j++){
						if(this.inputList[i].resId == this.outputList[j].resId){
							let headVertex = this.getVertexById(this.outputList[j].vertexId);
							let tailVertex = this.getVertexById(this.inputList[i].vertexId);

							//判断是否为重复边
							let isNewEdge = true;
							for(let k = 0 ; k < this.edgeList.length ; k++){
								if(this.edgeList[k].headId == headVertex.id && this.edgeList[k].tailId == tailVertex.id){
									isNewEdge = false;
								}
							}
							if(isNewEdge){
								let edge = this.graph.insertEdge(
									this.graph.getDefaultParent(),
									null,
									'',
									headVertex,
									tailVertex
								);
								//绑定相关信息
								edge.edgeAttribute = {};
								edge.edgeAttribute.headId = headVertex.id;
								edge.edgeAttribute.tailId = tailVertex.id;
								// console.log(edge); //如果需要  可以将信息补充道edge中
								this.edgeList.push({
									headId: headVertex.id,
									tailId: tailVertex.id,
									resId: this.inputList[i].resId,
								})
							}
						}
					}
				}
			},
			getVertexById(vertexId){
				for(let i = 0 ; i < this.vertexList.length ; i++){
					if(this.vertexList[i].id == vertexId){
						return this.vertexList[i];
					}
				}
			},
			listenGraphEvent() {
				// 监听双击事件
				this.graph.addListener(mxEvent.DOUBLE_CLICK, async (graph, evt) => {
					// DOUBLE_CLICK
					if (evt.properties.cell != undefined) {
						let cell = evt.properties.cell;
						console.log(cell);
						this.selectVertex = cell;

						//临时实验
						this.selectVertex.backendType = "dataMethod";
						this.selectVertex.tid = "cc94e0c1-5958-4715-a4e0-4360d5fa6e06";
						this.selectVertex.toolUrl = null;

						// this.vertexInfoShow = true;
						this.openTool();
					}
				});
				// 监听单击事件
				this.graph.addListener(mxEvent.CLICK, async (graph, evt) => {
					// DOUBLE_CLICK
					if (evt.properties.cell != undefined) {
						let cell = evt.properties.cell;
						console.log(cell);
						this.tempToolInfo = cell;

						//临时实验
						this.tempToolInfo.tid = "0acca386-2f51-420b-972d-3ab69171c7cc";
						this.getToolInfo();

					}
				});
			},
			openTool(){
				let toolInfo = this.selectVertex;
				let routerUrl = toolInfo.toolUrl;
				if ( toolInfo.backendType == "webTool") {
					routerUrl = toolInfo.toolUrl;
				} else if ( toolInfo.backendType == "modelItem") {
					routerUrl = "/GeoProblemSolving/computeModel";
				} else if ( toolInfo.backendType == "dataMethod") {
					routerUrl = "/GeoProblemSolving/dataMethod";
				}
				var toolContent = `<iframe src="${routerUrl}" id="${toolInfo.tid}" style="width: 100%; height:100%;" frameborder="0"></iframe>`;

				jsPanel.create({
					id: 'panel',
					theme: 'primary',
					headerTitle: '测试',
					contentSize: {
						width: 1000,
						height: 600
					},
					contentOverflow: 'hidden',
					content: toolContent,
					container: "div#workflow",
					boxShadow: 5,
					dragit: {
						containment: [70, 30, -50, 20],
					},
					closeOnEscape: true,
					disableOnMaximized: true,
					callback: function () {
						this.content.style.padding = '0px';
					}
				});

					// 设置iframe 父子页面消息传输处理
				window.addEventListener("message", this.toolMsgHandle, false);
				let activity = this.projectInfo;
				let userInfo = this.userInfo;
				let taskList = this.operationApi.getTaskList();
				let iFrame = document.getElementById(toolInfo.tid);
				//iframe加载完毕后再发送消息，否则子页面接收不到message
				iFrame.onload = function () {
					//iframe加载完立即发送一条消息
					iFrame.contentWindow.postMessage(
					{
						user: userInfo,
						activity: activity,
						tasks: taskList,
						tid: toolInfo.tid,
						type: "activity",
					},
					"*"
					);
				};
			},
			toolMsgHandle(event) {
				if (event.data.type === "task") {
					let operations = event.data.operations;
					let behavior = event.data.behavior;
					for (let i = 0; i < operations.length; i++) {
						let operationType = operations[i].type;
						switch (operationType) {
							case "resource": {
							if (behavior === "record") {
								this.operationApi.resOperationRecord(
								this.activityInfo.aid,
								operations[i].id,
								event.data.task,
								behavior,
								this.userInfo.userId,
								operations[i].content
								);
								this.$store.commit("updateTempOperations", {
								behavior: "add",
								operation: operations[i],
								});
							} else if (behavior === "bind") {
								this.operationApi.bindTempOperation2Task(
								this.activityInfo.aid,
								operations[i].id,
								event.data.task
								);
								this.$store.commit("updateTempOperations", {
								behavior: "remove",
								operation: operations[i],
								});
							}
							break;
							}
							case "communication": {
							break;
							}
							case "geo-analysis": {
							if (behavior === "record") {
								this.operationApi.analysisRecord(
								this.activityInfo.aid,
								operations[i].id,
								event.data.task,
								this.userInfo.userId,
								operations[i].toolId,
								operations[i].purpose,
								operations[i].inputs,
								operations[i].outputs,
								operations[i].params,
								operations[i].participants
								);
							} else if (behavior === "bind") {
								this.operationApi.bindTempOperation2Task(
								this.activityInfo.aid,
								operations[i].id,
								event.data.task
								);
							}
							break;
							}
						}

						if (behavior === "record") {
							this.$store.commit("updateTempOperations", {
							behavior: "add",
							operation: operations[i],
							});
						} else if (behavior === "bind") {
							this.$store.commit("updateTempOperations", {
							behavior: "remove",
							operation: operations[i],
							});
						}
					}
				}
			},
			getToolInfo(){
				let tid = this.tempToolInfo.tid;
				this.getToolInfoFinish = false;
				this.axios
				.get("/GeoProblemSolving/tool/" + tid)
				.then((res) => {
					if (res.data.code == 0) {
						console.log(res.data.data);
						this.selectToolInfo = res.data.data;
						this.getToolInfoFinish = true;
					} else {
						console.log(res.data.msg);
					}
					})
				.catch((err) => {
					throw err;
				});
			},
			heightChange(){
				let count = this.vertexList.length;
				return (count * 100 + 100) + 'px'
			},
			zoomOut(){
				this.graph.zoomOut();
			},
			zoomIn(){
				this.graph.zoomIn();
			},
			zoomActual(){
				this.graph.zoomActual();
				this.graph.center(true,true,0.5,0.3);//将画布放到容器中间
			},

			getXml(){
				let encoder = new mxCodec();
				let graphXml = encoder.encode(this.graph.getModel());
				let xml = mxUtils.getPrettyXml(graphXml);
				console.log(xml);
			},
			selectMember(member, operation) {
				if (operation == "delete") {
					this.slctDletMember = member;
					this.removeMemberModal = true;
				} else if (operation == "role") {
					this.slctRoleMember = member;
					this.memberRoleModal = true;
					this.roleSelected = member.role;
				}
			},
			updateUserRole() {
				let member = this.slctRoleMember;
				let activity = this.activityInfo;
				let role = this.roleSelected;

				// get url
				let url = "";
				if (activity.level == 0) {
					url =
					"/GeoProblemSolving/project/" +
					activity.aid +
					"/user?userId=" +
					member.userId +
					"&role=" +
					role;
				} else if (activity.level == 1) {
					url =
					"/GeoProblemSolving/subproject/" +
					activity.aid +
					"/user?userId=" +
					member.userId +
					"&role=" +
					role;
				} else if (activity.level > 1) {
					url =
					"/GeoProblemSolving/activity/" +
					activity.aid +
					"/user?userId=" +
					member.userId +
					"&role=" +
					role;
				} else {
					return;
				}

				this.axios
				.put(url)
				.then((res) => {
				if (res.data.code == 0) {
					this.slctRoleMember.role = role;
					this.$Notice.info({ desc: "Change the member role successfully" });
					this.operationApi.participantUpdate(
					this.activityInfo.aid,
					"role",
					member.userId,
					member.name,
					role,
					member.domain
					);
					this.getParticipants();

					//notice
					let notice = {
					recipientId: member.userId,
					type: "notice",
					content: {
						title: "Role changed",
						description:
						"Your role in the activity: " +
						activity.name +
						", project: " +
						this.projectInfo.name +
						" has changed to " +
						role +
						".",
						approve: "unknow",
						projectId: this.projectInfo.aid,
						projectName: this.projectInfo.name,
						activityId: activity.aid,
						activityName: activity.name,
						activityLevel: activity.level,
						// removerName: this.userInfo.name,
						// removerId: this.userInfo.userId,
					},
					};
					this.sendNotice(notice);
				} else {
					console.log(res.data.msg);
				}
				})
				.catch((err) => {
				throw err;
				});
			},
			roleCompare(role1, role2) {
				return this.userRoleApi.roleCompare(role1, role2);
			},
			avatarUrl(url) {
				let avatarUrl = this.$store.state.UserServer + url;
				return avatarUrl;
			},
			gotoPersonalSpace(id) {
				if (id == this.$store.getters.userId) {
					window.location.href = "/GeoProblemSolving/newPersonalPage/overView";
				} else {
					window.location.href = "/GeoProblemSolving/memberPage/" + id;
				}
			},
		},
		filters:{
			getLength(list){
				return list.length;
			},
		},
	}
</script>

<style scoped>
	.infoCard {
		margin-right: 5px;
		width: 23%;
		overflow-y: auto;
		border: 0;
	}

	.mxGraphCard {
		margin-right: 5px;
		width: 38%;
		overflow-y: auto;
		border: 0;
	}

	.modalCard {
		margin-right: 5px;
		width: 30%;
		overflow-y: auto;
		border: 0;
	}
	.memberImg {
		width: 40px;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	.memberName {
		height: 20px;
		padding: 0px 10px;
		width: 100%;
	}
	.memberDetail {
		height: 100%;
		width: 100%;
		overflow: hidden;
	}
	.memberRole {
		height: 20px;
		padding: 0px 10px;
		width: 100%;
	}
	.memberName span {
		display: inline-block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	.memberRole span {
		display: inline-block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		max-width: 100%;
	}
	#graphContainer{
		width: 700px;
		height: 800px;
		max-width: 700px;
		min-height: 730px;
		border: 3px solid rgb(194, 185, 185);
		background-image: url('./MxGraph/images/grid.gif');
		margin: auto;
		overflow: hidden;

	}
	button{
		width: 80px;
		height: 30px;
		background: rgb(122, 122, 121);
		color: white;
		font-size: 12px;
		outline: none;
		border: none;
		border-radius: 15px;

	}
</style>
