<style>
@import "../../../../static/css/Control.MiniMap.css";
@import "../../../../static/css/leaflet.pm.css";
#map {
  float: right;
  width: 100%;
  height: 100%;
}
#import-data {
  background-color: white;
  width: 30px;
  height: 30px;
  border-bottom: 1px solid lightgray;
  cursor: pointer;
}
#import-data:hover {
  background-color: #f3f3f3;
  width: 30px;
  height: 30px;
  border-bottom: 1px solid lightgray;
  cursor: pointer;
}
#export-data {
  background-color: white;
  width: 30px;
  height: 30px;
  cursor: pointer;
}
#export-data:hover {
  background-color: #f3f3f3;
  width: 30px;
  height: 30px;
  cursor: pointer;
}
#remove-all {
  background-color: white;
  width: 30px;
  height: 30px;
  border-bottom: 1px solid lightgray;
  cursor: pointer;
}
#remove-all:hover {
  background-color: #f3f3f3;
  width: 30px;
  height: 30px;
  border-bottom: 1px solid lightgray;
  cursor: pointer;
}
</style>
<template>
  <div>
    <div id="collab-tool-head"></div>
    <div id="collab-tool-sidebar"></div>
    <div id="collab-tool-content">
      <div id="edit-mask" title="The other participant is operating."></div>

      <div id="map" class="map">
        <Modal
          v-model="modalExport"
          title="Export GeoJSON to resource center"
          @on-ok="save2Resource('formValidate')"
        >
          <Form
            ref="formValidate"
            :model="formValidate"
            :rules="ruleValidate"
            :label-width="80"
            style="margin-left: 20px"
          >
            <FormItem label="Name" prop="fileName">
              <Input
                v-model="formValidate.fileName"
                placeholder="*.json"
                style="width: 350px"
              />
            </FormItem>
            <FormItem label="Description" prop="fileDescription">
              <Input
                v-model="formValidate.fileDescription"
                type="textarea"
                placeholder="Enter something..."
                style="width: 350px"
              />
            </FormItem>
          </Form>
          <p style="margin-left: 30px">
            Download this data directly.
            <Button
              style="margin-left: 50px"
              @click="downloadGeoJson('formValidate')"
              >Download</Button
            >
          </p>
        </Modal>
        <Modal
          v-model="modalImport"
          title="Import GeoJSON data to resource center and show the data"
          @on-ok="viewData"
          ok-text="OK"
          cancel-text="Cancel"
        >
          <Upload
            type="drag"
            :before-upload="handleUpload"
            action="-"
            accept=".json, .zip"
          >
            <div style="padding: 20px 0">
              <Icon
                type="ios-cloud-upload"
                size="52"
                style="color: #3399ff"
              ></Icon>
              <p>
                Click or drag files here to upload(The file size must control in
                <span style="color: red">1GB</span>)
              </p>
            </div>
          </Upload>
          <div v-show="showFile">
            <span id="show-file">
              <i class="ivu-icon ivu-icon-md-document"></i>
              {{ selectData.name }}
            </span>
          </div>
          <br />
        </Modal>
      </div>
    </div>
  </div>
</template>
<script>
import minimap from "@static/js/Control.MiniMap.min.js";
import pm from "@static/js/leaflet.pm.min.js";
import imIcon from "@static/Images/import.png";
import exIcon from "@static/Images/export.png";
import removeIcon from "@static/Images/trash.png";
//leaflet
import L from "leaflet";
import shp from "shpjs";
import "leaflet/dist/leaflet.css";
// this part resolve an issue where the markers would not appear
delete L.Icon.Default.prototype._getIconUrl;
//请求带上cookie以防session丢失
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"),
  iconUrl: require("leaflet/dist/images/marker-icon.png"),
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"),
});
export default {
  data() {
    return {
      // basic info
      activityInfo: {},
      userInfo: {},
      resources: [],
      //
      windowHeight: window.innerHeight,
      windowWidth: window.innerWidth - 60,
      modalExport: false,
      modalImport: false,
      map: null,
      baseLayers: null,
      traces: {},
      send_content: {},
      //geojson blob
      geojsonBlob: null,
      showFile: false,
      //存储绘制的图像layer
      drawingLayerGroup: null,
      participants: [],
      olParticipants: [],
      selectData: {},
      formValidate: {
        fileName: "",
        fileDescription: "",
      },
      ruleValidate: {
        fileName: [
          { required: true, message: "Please select type...", trigger: "blur" },
        ],
        fileDescription: [
          { required: false, message: "Drawing tool", trigger: "blur" },
        ],
      },
      resProxy: this.$store.getters.resProxy,
    };
  },
  mounted() {
    // 加载协同组件
    loadCollabComponent();
    this.getStepInfo();

    this.initMap();
    this.initLayer();
    this.initControl();
    this.listenDraw();
  },
  beforeDestroy() {},
  beforeRouteEnter: (to, from, next) => {
    next((vm) => {
      if (!vm.$store.getters.userState || vm.$store.getters.userId == "") {
        vm.$router.push({ name: "Login" });
      }
    });
  },
  methods: {
    getStepInfo() {
      if (componentStatus) {
        // 获取数据
        this.activityInfo = activityInfo;
        this.userInfo = userInfo;
        this.resources = resources;

        // 绑定函数
        buildSocketChannel(
          this.getSocketOperation,
          this.getSocketData,
          this.getSocketComputation
        );
        loadResChannel = this.loadResources;
      } else {
        let _this = this;
        setTimeout(function () {
          _this.getStepInfo();
        }, 1000);
      }
    },
    initMap() {
      this.tdtVectorMap =
        "https://t0.tianditu.gov.cn/vec_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
        "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";
      this.tdtVectorAno =
        // "https://t0.tianditu.gov.cn/cva_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
        // "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";
        "https://t0.tianditu.gov.cn/eva_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
        "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=eva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";
      this.tdtImgMap =
        "https://t0.tianditu.gov.cn/img_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
        "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";
      this.tdtImgAno =
        "https://t0.tianditu.gov.cn/cia_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
        "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cia&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";
      this.tdtTerrMap =
        "https://t0.tianditu.com/ter_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
        "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=ter&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";
      this.tdtTerrAno =
        "https://t0.tianditu.com/cta_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
        "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cta&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";

      this.map = L.map("map", {
        crs: L.CRS.EPSG3857,
        center: L.latLng(32.07, 118.78),
        zoom: 13,
      });
    },
    initLayer() {
      this.drawingLayerGroup = L.layerGroup([]);
      this.drawingLayerGroup.addTo(this.map);
    },
    initControl() {
      // 图层控件
      var vectorMap = L.tileLayer(this.tdtVectorMap, {
        maxZoom: 20,
        attribution:
          '&copy; <a href="https://map.tianditu.gov.cn/">tianditu</a> contributors',
      });
      var vectorAno = L.tileLayer(this.tdtVectorAno, { maxZoom: 18 });
      var vector = L.layerGroup([vectorMap, vectorAno]);

      var satelliteMap = L.tileLayer(this.tdtImgMap, {
        maxZoom: 18,
        attribution:
          '&copy; <a href="https://map.tianditu.gov.cn/">tianditu</a> contributors',
      });
      var satelliteAno = L.tileLayer(this.tdtImgAno, { maxZoom: 18 });
      var satellite = L.layerGroup([satelliteMap, satelliteAno]);

      var terrainMap = L.tileLayer(this.tdtTerrMap, {
        maxZoom: 18,
        attribution:
          '&copy; <a href="https://map.tianditu.gov.cn/">tianditu</a> contributors',
      });
      var terrainAno = L.tileLayer(this.tdtTerrAno, { maxZoom: 18 });
      var terrain = L.layerGroup([terrainMap, terrainAno]);

      this.baseLayers = {
        "Vector map": vector,
        "Satellite map": satellite,
        "Terrain map": terrain,
        // "Google satellite map": googleSatellite
      };
      var overlayLayers = {};
      L.control.layers(this.baseLayers, overlayLayers).addTo(this.map);
      this.baseLayers["Vector map"].addTo(this.map);

      // 比例尺
      L.control
        .scale({
          position: "bottomleft",
        })
        .addTo(this.map);

      // 鹰眼
      var normal = L.tileLayer(this.tdtVectorMap, { maxZoom: 18 });
      var miniMap = new L.Control.MiniMap(normal, {
        toggleDisplay: true,
        minimized: false,
        position: "bottomleft",
      }).addTo(this.map);

      // 绘图控件
      var options = {
        position: "topleft", // toolbar position, options are 'topleft', 'topright', 'bottomleft', 'bottomright'
        drawMarker: true, // adds button to draw markers
        drawPolyline: true, // adds button to draw a polyline
        drawRectangle: true, // adds button to draw a rectangle
        drawPolygon: true, // adds button to draw a polygon
        drawCircle: true, // adds button to draw a cricle
        cutPolygon: false, // adds button to cut a hole in a polygon
        editMode: true, // adds button to toggle edit mode for all layers
        dragMode: false,
        removalMode: true, // adds a button to remove layers
      };
      this.map.pm.addControls(options);

      this.diyDataControl();
      this.diyRemoveAllControl();
    },
    diyDataControl() {
      var that = this;
      L.Control.Data = L.Control.extend({
        //在此定义参数
        options: {
          position: "topright",
        },
        //在此初始化
        initialize: function (map) {},
        onAdd: function (map) {
          this._container = L.DomUtil.create("div", "leaflet-exportData");
          this._container.style =
            "border:2px solid rgba(128,128,128,0.5);border-radius:6px";

          let importData = document.createElement("div");
          importData.id = "import-data";
          importData.title = "Import data";
          importData.onclick = this._importData;
          let iconImport = document.createElement("img");
          iconImport.src = imIcon;
          iconImport.style = "margin-left: 3.5px;margin-top: 3px";
          importData.appendChild(iconImport);

          let exportData = document.createElement("div");
          exportData.id = "export-data";
          exportData.title = "Export GeoJSON";
          exportData.onclick = this._exportData;
          let iconExport = document.createElement("img");
          iconExport.src = exIcon;
          iconExport.style = "margin-left: 3.5px;margin-top: 3px";
          exportData.appendChild(iconExport);

          this._container.appendChild(importData);
          this._container.appendChild(exportData);
          return this._container;
        },
        _exportData() {
          var featuresSet = { type: "FeatureCollection", features: [] };
          that.map.eachLayer(function (layer) {
            try {
              var json = layer.toGeoJSON();
              if (json.type == "Feature") {
                featuresSet.features.push(json);
              }
            } catch (e) {}
          });
          if (featuresSet.features.length > 0) {
            that.geojsonBlob = new Blob(
              [JSON.stringify(featuresSet, null, 2)],
              { type: "application/json" }
            );
            that.modalExport = true;
          }
        },
        _importData() {
          that.modalImport = true;
        },
      });
      L.control.data = function () {
        return new L.Control.Data();
      };
      L.control.data().addTo(this.map);
    },
    diyRemoveAllControl() {
      var that = this;
      L.Control.Data = L.Control.extend({
        //在此定义参数
        options: {
          position: "topleft",
        },
        //在此初始化
        initialize: function (map) {},
        onAdd: function (map) {
          this._container = L.DomUtil.create("div", "leaflet-RemoveAll");
          this._container.style =
            "border:2px solid rgba(128,128,128,0.5);border-radius:6px";

          let removeAll = document.createElement("div");
          removeAll.id = "remove-all";
          removeAll.title = "Remove all";
          removeAll.onclick = this._removeAll;
          let iconImport = document.createElement("img");
          iconImport.src = removeIcon;
          iconImport.style = "margin-left: 3.5px;margin-top: 3px";
          removeAll.appendChild(iconImport);

          this._container.appendChild(removeAll);
          return this._container;
        },
        _removeAll() {
          that.drawingLayerGroup.clearLayers();

          that.send_content = {
            type: "operation",
            sender: that.userInfo.userId,
            behavior: "removeAll",
            content: JSON.stringify({}),
          };

          sendCustomOperation(that.send_content);
        },
      });
      L.control.data = function () {
        return new L.Control.Data();
      };
      L.control.data().addTo(this.map);
    },
    downloadGeoJson(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          var reader = new FileReader();
          if (this.geojsonBlob != null) {
            let filename = "";
            if (!/\.(json)$/.test(this.formValidate.fileName.toLowerCase())) {
              filename = this.formValidate.fileName + ".json";
            } else {
              filename = this.formValidate.fileName;
            }
            reader.readAsDataURL(this.geojsonBlob);
            reader.onload = function (e) {
              var a = document.createElement("a");
              a.download = filename;
              a.href = e.target.result;
              $("body").append(a);
              a.click();
              $(a).remove();
            };
          }
        } else {
          this.$Message.error("Please enter the necessary information!");
        }
      });
    },
    save2Resource(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          if (this.geojsonBlob != null) {
            // 完善文件信息
            let filename = "";
            if (!/\.(json)$/.test(this.formValidate.fileName.toLowerCase())) {
              filename = this.formValidate.fileName + ".json";
            } else {
              filename = this.formValidate.fileName;
            }
            let description = "";
            if (this.formValidate.fileDescription == "") {
              description = "Map tool data";
            } else {
              description = this.formValidate.fileDescription;
            }

            var fileOfBlob = new File([this.geojsonBlob], filename);
            // upload
            let file = saveResources(
              [fileOfBlob],
              description,
              "data",
              "private",
              ""
            );

            this.showFile = true;
            this.selectData = file[0];

            if (file.length > 0) {
              this.$Notice.open({
                title: "Save to resource center",
                desc: "Data saved successfully!",
              });
            }

            // 初始化formValidation
            this.formValidate = {
              fileName: "",
              fileDescription: "",
            };
          }
        } else {
          this.$Message.error("Please enter the necessary information!");
        }
      });
    },
    handleUpload(file) {
      if (!/\.(json|zip)$/.test(file.name.toLowerCase())) {
        this.$Message.error("Worry format");
        return false;
      }
      let uploadedList = uploadResources(
        file,
        "Map tool data",
        "data",
        "private"
      );
      if (uploadedList.length > 0) {
        this.showFile = true;
        this.selectData = uploadedList[0];
      }

      return false;
    },
    viewData() {
      let uid;
      if (typeof this.selectData.address == "string") {
        uid = this.selectData.address.slice(-36);
      }
      let fileUrl = this.resProxy + "/data/" + uid;
      if (/\.(json)$/.test(this.selectData.suffix.toLowerCase())) {
        //从url获取GeoJSON数据
        var that = this;
        var xhr = new XMLHttpRequest();
        xhr.open("GET", fileUrl, true);
        xhr.onload = function (e) {
          if (this.status == 200) {
            var file = JSON.parse(this.response);

            let geoJsonLayer = L.geoJSON(file, {
              style: function (feature) {
                return { color: "red" };
              },
            }).bindPopup(function (layer) {
              return layer.feature.properties.description;
            });
            that.loadFeatures(geoJsonLayer);
            //平移至数据位置
            that.map.fitBounds(geoJsonLayer.getBounds());
          } else {
            this.$Message.error("Loading failed");
          }
        };
        xhr.send();
      } else if (/\.(zip)$/.test(this.selectData.suffix.toLowerCase())) {
        try {
          var that = this;
          shp(fileUrl).then(function (file) {
            let geoJsonLayer = L.geoJSON(file, {
              style: function (feature) {
                return { color: "orange" };
              },
            }).bindPopup(function (layer) {
              return layer.feature.properties.description;
            });
            that.loadFeatures(geoJsonLayer);
            that.map.fitBounds(geoJsonLayer.getBounds());
          });
        } catch (res) {
          this.$Message.error("Worry data format!");
        }
      } else {
        this.$Message.error("Worry data format!");
      }
      this.showFile = false;
    },
    loadFeatures(featureCollection) {
      featureCollection.eachLayer((layer) => {
        this.drawingLayerGroup.addLayer(layer);
      });
    },
    setEditListen() {
      this.drawingLayerGroup.eachLayer((layer) => {
        let _this = this;
        layer.on("pm:edit", (e) => {
          _this.send_content = {
            type: "operation",
            sender: this.userInfo.userId,
            behavior: "edit",
            content: JSON.stringify({
              layer: _this.drawingLayerGroup.toGeoJSON(),
            }),
          };
          sendCustomOperation(_this.send_content);
        });
      });
    },
    listenDraw() {
      this.send_content = {};
      let isMouseDown = false;
      let isZoomControl = false;
      let isDoubleClick = false;
      let isLayerCtrlClick = false;

      this.map.on("mousedown", (e) => {
        isMouseDown = true;
      });

      this.map.on("mouseup", (e) => {
        isLayerCtrlClick = true;
      });

      this.map.on("dblclick", (e) => {
        isDoubleClick = true;
      });

      //缩放控件事件
      var element = document.querySelector("a.leaflet-control-zoom-in");
      L.DomEvent.addListener(element, "click", function (e) {
        isZoomControl = true;
      });
      element = document.querySelector("a.leaflet-control-zoom-out");
      L.DomEvent.addListener(element, "click", function (e) {
        isZoomControl = true;
      });

      // 图层控件
      this.map.on("baselayerchange", (e) => {
        if (isLayerCtrlClick) {
          this.send_content = {
            type: "operation",
            sender: this.userInfo.userId,
            behavior: "overlay",
            content: JSON.stringify({
              layer: e.name,
            }),
          };
          sendCustomOperation(this.send_content);
        }
        isLayerCtrlClick = false;
      });

      //缩放事件 与 鼠标事件同时发生
      this.map.on("zoomend", (e) => {
        if (this.map.scrollWheelZoom || isZoomControl || isDoubleClick) {
          this.send_content = {
            type: "operation",
            sender: this.userInfo.userId,
            behavior: "zoom",
            content: JSON.stringify({
              zoom: this.map.getZoom(),
            }),
          };
          sendCustomOperation(this.send_content);
          isZoomControl = false;
          isDoubleClick = false;
        }
      });

      //地图拖拽事件
      this.map.on("moveend", (e) => {
        if (isMouseDown) {
          this.send_content = {
            type: "operation",
            sender: this.userInfo.userId,
            behavior: "move",
            content: JSON.stringify({
              center: this.map.getCenter(),
            }),
          };
          sendCustomOperation(this.send_content);
        }
        isMouseDown = false;
      });

      // 裁剪事件
      this.map.on("pm:cut", (e) => {
        // this.drawingLayerGroup.removeLayer(e.originalLayer);
        // this.drawingLayerGroup.addLayer(e.layer);
        // this.send_content = {
        //   type: "operation",
        //   sender: this.userInfo.userId,
        //   behavior: "cut",
        //   content: JSON.stringify({
        //     layer: this.drawingLayerGroup.toGeoJSON(),
        //   }),
        // };
        // sendCustomOperation(this.send_content);
      });

      // 删除事件
      let _this = this;
      this.map.on("pm:remove", (e) => {
        _this.drawingLayerGroup.removeLayer(e.layer);
        this.send_content = {
          type: "operation",
          sender: this.userInfo.userId,
          behavior: "remove",
          content: JSON.stringify({
            layer: _this.drawingLayerGroup.toGeoJSON(),
          }),
        };
        sendCustomOperation(this.send_content);
      });

      this.map.on("pm:globaleditmodetoggled", (e) => {
        this.setEditListen();
      });

      // 画图事件
      this.map.on("pm:create", (e) => {
        this.map.removeLayer(e.layer);
        if (e.shape == "Circle") {
          this.traces = [];
          let points = e.layer._latlng;
          this.traces.push(points);
          let radius = e.layer._mRadius;
          this.traces.push(radius);

          let drawingLayer = L.circle(points, {
            radius: radius,
          });
          this.drawingLayerGroup.addLayer(drawingLayer);

          this.send_content = {
            type: "operation",
            sender: this.userInfo.userId,
            behavior: "add",
            content: JSON.stringify({
              shape: e.shape,
              layer: this.traces,
            }),
          };
        } else {
          this.drawingLayerGroup.addLayer(e.layer);

          this.send_content = {
            type: "operation",
            sender: this.userInfo.userId,
            behavior: "add",
            content: JSON.stringify({
              shape: "Others",
              layer: e.layer.toGeoJSON(),
            }),
          };
        }
        sendCustomOperation(this.send_content);
      });
    },
    getSocketData(data) {
      let socketMsg = data;
      if (socketMsg.type === "resource") {
        let dataItem = JSON.parse(socketMsg.content);
        switch (socketMsg.behavior) {
          case "update": {
            this.resources.push(dataItem);

            var that = this;
            var xhr = new XMLHttpRequest();
            let address = dataItem.address;
            let uid;
            if (typeof address == "string") {
              uid = address.slice(-36);
            }
            xhr.open("GET", this.resProxy + "/data/" + uid, true);
            xhr.onload = function (e) {
              if (this.status == 200) {
                var file = JSON.parse(this.response);

                let geoJsonLayer = L.geoJSON(file, {
                  style: function (feature) {
                    return { color: "green" };
                  },
                }).bindPopup(function (layer) {
                  return layer.feature.properties.description;
                });
                that.loadFeatures(geoJsonLayer);
              }
            };
            xhr.send();
            break;
          }
          case "select": {
            this.selectData = dataItem;
            this.viewData();
          }
        }
      }
    },
    getSocketComputation() {},
    getSocketOperation(data) {
      let content = JSON.parse(data.content);
      if (data.type === "operation") {
        switch (data.behavior) {
          case "zoom": {
            this.map.setZoom(content.zoom);
            break;
          }
          case "move": {
            this.map.panTo(content.center);
            break;
          }
          case "overlay": {
            try {
              this.map.removeLayer(this.baseLayers["Terrain map"]);
            } catch (e) {}
            try {
              this.map.removeLayer(this.baseLayers["Satellite map"]);
            } catch (e) {}
            try {
              this.map.removeLayer(this.baseLayers["Vector map"]);
            } catch (e) {}
            this.baseLayers[content.layer].addTo(this.map);
            break;
          }
          case "remove": {
            this.drawingLayerGroup.clearLayers();
            let geoJson = content.layer;
            let geoJsonLayer = L.geoJSON(geoJson, {
              style: function (feature) {},
            }).bindPopup(function (layer) {
              // return layer.feature.properties.description;
            });
            this.loadFeatures(geoJsonLayer);
            break;
          }
          case "edit": {
            this.drawingLayerGroup.clearLayers();
            let geoJson = content.layer;
            let geoJsonLayer = L.geoJSON(geoJson, {
              style: function (feature) {},
            }).bindPopup(function (layer) {
              // return layer.feature.properties.description;
            });
            this.loadFeatures(geoJsonLayer);
            break;
          }
          case "add": {
            let drawingLayer = null;
            if (content.shape == "Circle") {
              drawingLayer = L.circle(content.layer[0], {
                radius: content.layer[1],
              });
            } else {
              drawingLayer = L.geoJSON(content.layer, {
                style: function (feature) {},
              }).bindPopup(function (layer) {});
            }
            this.drawingLayerGroup.addLayer(drawingLayer);
            break;
          }
          case "cut": {
            // this.drawingLayerGroup.clearLayers();
            // let geoJson = content.layer;
            // let geoJsonLayer = L.geoJSON(geoJson, {
            //   style: function (feature) {
            //   }
            // }).bindPopup(function (layer) {
            //     return layer.feature.properties.description;
            // });
            // this.drawingLayerGroup.addLayer(geoJsonLayer);
            // break;
          }
          case "removeAll": {
            this.drawingLayerGroup.clearLayers();
          }
        }
      }
    },
    loadResources(resList) {
      for (let i = 0; i < resList.length; i++) {
        this.selectData = resList[i];
        this.viewData();

        let send_content = {
          type: "resource",
          sender: this.userInfo.userId,
          behavior: "select",
          content: this.selectData,
        };
        sendCustomOperation(send_content);
      }
    },
  },
};
</script>
