// Define some base layers
// 底图

// var osm = L.tileLayer(
//     '//{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
//     {
//         attribution: '© OpenStreetMap contributors',
//     }
// );

var roadDataId, buildingDataId, barrierDataId, runningId;
var roadData, buildingData, barrierData;
var resultIdList = [];
var userId;
var resProxy = "https://geomodeling.njnu.edu.cn/dataTransferServer";

proj4.defs("EPSG:2437", "+proj=tmerc +lat_0=0 +lon_0=120 +k=1 +x_0=500000 +y_0=0 +ellps=krass +towgs84=15.8,-154.4,-82.3,0,0,0,0 +units=m +no_defs");

var mymap = L.map('mapid', {
    zoom: 4,
    // 禁止地图的双击缩放功能
    // doubleClickZoom: false,
    center: [31, 105],
    editable: true
});
var vectorMap = L.tileLayer("http://t0.tianditu.gov.cn/vec_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
    "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}", {
    maxZoom: 20,
    attribution:
        '&copy; <a href="http://map.tianditu.gov.cn/">tianditu</a> contributors',
});
var vectorAno = L.tileLayer("http://t0.tianditu.gov.cn/cva_w/wmts?tk=d6b0b78f412853967d91042483385d2c" +
    "&SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}", {maxZoom: 18});
var vector = L.layerGroup([vectorMap, vectorAno]);
vector.addTo(mymap);

var info = L.control();
info.onAdd = function () {//返回Dom element
    this._div = L.DomUtil.create('div', 'info');
    this.update();
    return this._div;
};

// 更新div的内容为建筑物图层的Storey或提示
info.update = function (props) {
    this._div.innerHTML = '<h4>Storey of Building</h4>' + (props ?
        '<b>This building has </b><br />' + props.Storey + ' floors'
        : 'Please hover your mouse over the building.');

}
info.addTo(mymap);


var legend = L.control({position: 'bottomright'});
legend.onAdd = function () {
    var div = L.DomUtil.create('div', 'info legend'),
        grades = [55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
        labels = [];
    // loop through our density intervals and generate a label with a colored square for each interval
    div.innerHTML += '<i style="background:' + getNoisePointColor(grades[0]) + '"></i> ' + '0' + '&ndash;' + grades[0] + '<br>';
    for (var i = 0; i < grades.length; i++) {
        div.innerHTML +=
            '<i style="background:' + getNoisePointColor(grades[i] + 1) + '"></i> ' +
            grades[i] + (grades[i + 1] ? '&ndash;' + grades[i + 1] + '<br>' : '+');
    }
    return div;
};
legend.addTo(mymap);


var operate = L.control({position: 'topleft'});
operate.onAdd = function () {
    var div = L.DomUtil.create('div', 'operate');
    return div;
};
operate.addTo(mymap);

L.control.scale().addTo(mymap);

// 监听事件
setListener();

function setListener() {
    var isZoomControl = false;
    var isDoubleClick = false;

    mymap.on("dblclick", (e) => {
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

    mymap.on("zoomend", (e) => {
        if (mymap.scrollWheelZoom || isZoomControl || isDoubleClick) {
            window.parent.sendCustomOperation({
                type: "operation",
                sender: userId,
                behavior: "zoom",
                content: JSON.stringify({
                    zoom: mymap.getZoom(),
                }),
            });
            isZoomControl = false;
            isDoubleClick = false;
        }
    });
}

//协同
window.parent.buildSocketChannel(
    getSocketOperation,
    getSocketData,
    getSocketComputation
);

function getSocketOperation(data) {
    var content;
    try {
        content = JSON.parse(data.content);
    } catch (e) {
        content = data.content;
    }
    if (data.type === "operation") {
        switch (data.behavior) {
            case "zoom": {
                mymap.setZoom(content.zoom);
                break;
            }
            case "mouse-click": {
                cancelAllState();
                $("#brforeRunModal").modal("hide");
                $("#mapid").css("cursor", "-webkit-grab");
                break;
            }
            case "data-import": {
                var result = content.result;
                var selectedDataType = content.featureType;

                mymap.setView([result.centerLat, result.centerLong], 15);

                switch (selectedDataType) {
                    case "Road":
                        loadShapefile(roadCenterLayer, result.url);
                        roadData = content.data;
                        roadDataId = content.data.uid;
                        createRoad.presentID = result.roadMaxID + 1;
                        break;
                    case "Building":
                        loadShapefile(buildingLayer, result.url);
                        buildingData = content.data;
                        buildingDataId = content.data.uid;
                        createBuilding.presentID = result.buildingMaxID + 1;
                        break;
                    case "Barrier":
                        loadShapefile(barrierLayer, result.url);
                        barrierData = content.data;
                        barrierDataId = content.data.uid;
                        break;
                }
                break;
            }
            case "data-visualization": {
                loadResultTiff(content.data);
                break
            }
            case "road-edit-start": {
                var prop = content.propInfo;
                roadCenterLayer.eachLayer((layer) => {
                    if (layer.feature.properties.OBJECTID == prop.OBJECTID) {
                        layer.setStyle({
                            weight: 5,
                            dashArray: '',
                            fillOpacity: 0.7
                        });
                    }
                });

                // 编辑框
                var result = content.resultInfo;
                $("#roadName").text(prop.NAME);
                $("#roadID").text(prop.OBJECTID);
                $("#roadLength").val(result.length.toFixed(2));
                $("#roadWidth").val(result.width);
                $("#roadSlope").val(result.slope);
                $("#roadM").val(result.M);
                $("#roadP").val(result.P);
                $("#carSpeed").val(result.carSpeed);
                $("#truckSpeed").val(result.truckSpeed);
                $("#roadSurface").val(result.roadSurface);

                if (result.hasReflect == 1) {
                    $("#hasReflect").prop("checked", true);
                    $("#reflectHeight").prop("disabled", false);
                    $("#reflectWidth").prop("disabled", false);
                    $("#isAbsorb").prop("disabled", false);
                    $("#reflectHeight").val(result.reflectHeight);
                    $("#reflectWidth").val(result.reflectWidth);
                    $("#isAbsorb").val(result.reflectAbsorb);
                } else {
                    $("#reflectHeight").val("");
                    $("#reflectWidth").val("");
                    $("#isAbsorb").val(-1);
                    $("#hasReflect").prop("checked", false);
                    $("#reflectHeight").prop("disabled", true);
                    $("#reflectWidth").prop("disabled", true);
                    $("#isAbsorb").prop("disabled", true);
                }
                $("#roadConfigModal").modal("show");
                $("#roadInfoSubmit").hide();

                break;
            }
            case "building-edit-start": {
                var prop = content.propInfo;
                buildingLayer.eachLayer((layer) => {
                    if (layer.feature.properties.OBJECTID == prop.OBJECTID) {
                        layer.setStyle({
                            weight: 5,
                            color: '#666',
                            dashArray: '',
                            fillOpacity: 0.7
                        });
                    }
                });

                var result = content.resultInfo;
                $("#buildingID").text(prop.OBJECTID);
                $("#buildingStorey").val(result.Storey);
                $("#buildingHeight").val(result.Height);
                $("#buildingConfigModal").modal("show");

                $("#buildingInfoSubmit").hide();
                break;
            }
            case "barrier-edit-start": {
                var prop = content.propInfo;
                barrierLayer.eachLayer((layer) => {
                    if (layer.feature.properties.OBJECTID == prop.OBJECTID) {
                        layer.setStyle({
                            weight: 5,
                            dashArray: '',
                            fillOpacity: 0.7
                        });
                    }
                });

                var result = content.resultInfo;
                $("#barrierID").text(prop.OBJECTID);
                $("#barrierLength").val(result.Length);
                $("#barrierHeight").val(result.Height);
                $("#barrierConfigModal").modal("show");

                $("#barrierInfoSubmit").hide();
                break;
            }
            case "edit-end": {
                if (content == 1) {
                    alert("Set the feature property successfully.");
                } else {
                    alert("Fail to set the property of this feature.");
                }
                break;
            }
            case "select-start": {
                cancelAllState();
                if (selectROI.isSelect) {
                    if (selectROI.ROI)
                        selectROI.rlayer.removeLayer(selectROI.ROI);
                    mymap.off("mousedown", selectROI.mousedown).off("mousemove", selectROI.mousemove).off("mouseup", selectROI.mouseup);
                }
                selectROI.isSelect = false;

                break;
            }
            case "select-end": {
                mymap.dragging.disable();
                selectROI.isSelect = true;

                var layar = L.geoJSON(content.layer, {});
                layar.addTo(selectROI.rlayer);
                break;
            }
            case "before-run": {
                cancelAllState();

                $("#sampleSize").val(10);
                $("#ROIHeight").val(10);
                $("#brforeRunModal").modal("show");
                $("#mapid").css("cursor", "-webkit-grab");

                $("#runModelBtn").hide();
                break;
            }
            case "run-start": {
                alert("RLS90 model start running.");
                $("#waitting").show();
                break;
            }
            case "run-success": {
                loadResultTiff(content.result);
                $("#waitting").hide();
                break;
            }
            case "clean": {
                cancelAllState();
                if (selectROI.ROI)
                    selectROI.rlayer.removeLayer(selectROI.ROI);

                noiseResultLayer.clearLayers();
                $(".legend").hide();
                break;
            }
            case "params-panel": {
                // 如果是第一个或者最后一个，则会有disabled class，直接返回
                // if (this.parent("span").hasClass("disabled")) {
                //     return;
                // }
                // 将当前页面收起来
                $(roadPage[currentPage]).slideUp();
                // 获取点击对象的id
                objId = content;
                if (objId == "previousInfo") {
                    currentPage--;
                } else {
                    currentPage++;
                }

                if (currentPage == 0) {
                    $("#previousInfo").parent("span").addClass("disabled");
                } else if (currentPage == 2) {
                    $("#nextInfo").parent("span").addClass("disabled");
                } else {
                    $("#previousInfo").parent("span").removeClass("disabled");
                    $("#nextInfo").parent("span").removeClass("disabled");
                }
                // 将目标页面滑下来
                $(roadPage[currentPage]).slideDown();

                break;
            }
            case "value-change": {
                if (content.type === "text") {
                    $("#" + content.id).val(content.value);
                    $("#" + content.id).css("border-color", "lightgreen");
                } else if (content.type === "checkbox") {
                    $("#" + content.id).prop(content.attributes);

                    if (content.attributes) {
                        $("#reflectHeight").prop("disabled", false);
                        $("#reflectHeight").val(30);
                        $("#reflectWidth").prop("disabled", false);
                        $("#reflectWidth").val(40);
                        $("#isAbsorb").prop("disabled", false);
                        $("#isAbsorb").val(0);
                    } else {
                        $("#reflectHeight").val("");
                        $("#reflectHeight").prop("disabled", true);
                        $("#reflectWidth").val("");
                        $("#reflectWidth").prop("disabled", true);
                        $("#isAbsorb").val(-1);
                        $("#isAbsorb").prop("disabled", true);
                    }
                }
                break;
            }
            case "": {
                reflectiveSurface
                break;
            }
        }
    }
}

function getSocketData() {

}

function getSocketComputation(data) {
    if (data != undefined && data != null) {
        clearInterval(state);
        state = null;

        $("#brforeRunModal").modal("hide");
        onSelectClick();
        $("#mapid").css("cursor", "-webkit-grab");

        data.computeOutputs.forEach((el) => {
            if (el.statename == "NoiseRegion" && el.event == "Output_RegionNoise") {

                var sampleSize = $("#sampleSize").val();
                var northWestPoint = selectROI.ROI.getBounds().getNorthWest();
                var southEastPoint = selectROI.ROI.getBounds().getSouthEast();
                $("#mapid").css("cursor", "wait");
                $.ajax({
                    // async:false,
                    type: "post",
                    url: "/GeoProblemSolving/resultInterpolation",
                    data: {
                        userId: userId,
                        resultData: el.url,
                        maxLat: northWestPoint.lat,
                        maxLon: southEastPoint.lng,
                        minLat: southEastPoint.lat,
                        minLon: northWestPoint.lng,
                        sampleSize: sampleSize
                    },
                    dataType: "text",
                    beforeSend: function () {
                        $("#waitting").show();
                    },
                    success: function (resp) {
                        var result = JSON.parse(resp);
                        if (result.respCode == 1) {
                            runningId = result.resultId;
                            resultIdList.push(runningId);
                            state = setInterval(getModelState, 2000);
                        } else {
                            console.log("error!");
                        }
                    },
                    error: function (e) {
                        $("#waitting").hide();
                        $("#mapid").css("cursor", "-webkit-grab");
                        console.log(e);
                    }
                });
            }
        });
        $("#waitting").hide();
    }
}

function paramChanged(id, value) {
    window.parent.sendElementChangeOperation(id, "value-change", "text", value, "", "");
}

var selectedData = {}
var selectedDataType = "";

function selectData(dataType) {
    $("#loadFile").empty();
    selectedDataType = dataType;

    $("#dataType").text("Import " + dataType + " Data");
    $("#loadDataModal").modal("show");

    if (dataType === "Result") {
        $("#dataNote").text("Please select a GeoTiff (*.tif).");
    } else {
        $("#dataNote").text("Please select a Shapefile (*.zip).");
    }

    var count = 0;
    for (var i = 0; i < resources.files.length; i++) {
        if (dataType === "Result" && resources.files[i].suffix === ".tif") {
            if (count == 0) {
                $("#loadFile").append("<option selected value='" + i + "'>" + resources.files[i].name + resources.files[i].suffix + "</option>");
                selectedData = resources.files[i];
            } else {
                $("#loadFile").append("<option value='" + i + "'>" + resources.files[i].name + resources.files[i].suffix + "</option>");
            }
            count++;
        } else if (dataType !== "Result" && resources.files[i].suffix === ".zip") {
            if (count == 0) {
                $("#loadFile").append("<option selected value='" + i + "'>" + resources.files[i].name + resources.files[i].suffix + "</option>");
                selectedData = resources.files[i];
            } else {
                $("#loadFile").append("<option value='" + i + "'>" + resources.files[i].name + resources.files[i].suffix + "</option>");
            }
            count++;
        }
    }
    $("#loadFile").on("change", function () {
        selectedData = resources.files[$("#loadFile").val()];
    });
}

function exportData(dataType) {
    let data = {};
    if (dataType === "Road") {
        if (roadData == undefined) {
            alert("There is no road data can be exported.");
            return;
        } else {
            data = {
                aid: window.parent.activityInfo.aid,
                uid: roadData.uid,
                input: roadData,
                name: roadData.name,
                user: userId,
                graphId: window.parent.activityInfo.parent,
            }
        }
    } else if (dataType === "Building") {
        if (buildingData == undefined) {
            alert("There is no building data can be exported.");
            return;
        } else {
            data = {
                aid: window.parent.activityInfo.aid,
                uid: buildingData.uid,
                name: buildingData.name,
                input: buildingData,
                user: userId,
                graphId: window.parent.activityInfo.parent,
            }
        }
    } else if (dataType === "Barrier") {
        if (barrierData == undefined) {
            alert("There is no barrier data can be exported.");
            return;
        } else {
            data = {
                aid: window.parent.activityInfo.aid,
                uid: barrierData.uid,
                name: barrierData.name,
                input: barrierData,
                user: userId,
                graphId: window.parent.activityInfo.parent,
            }
        }
    } else if (dataType === "Region") {
        if (selectROI.ROI == undefined) {
            alert("There is no simulation area selected.");
            return;
        } else {
            var northWestPoint = selectROI.ROI.getBounds().getNorthWest();
            var southEastPoint = selectROI.ROI.getBounds().getSouthEast();
            var upperLeftPoint = proj4("EPSG:4326", "EPSG:2437", [northWestPoint.lng, northWestPoint.lat]);
            var lowerRightPoint = proj4("EPSG:4326", "EPSG:2437", [southEastPoint.lng, southEastPoint.lat]);

            data = {
                aid: window.parent.activityInfo.aid,
                top: upperLeftPoint[1],
                right: lowerRightPoint[0],
                bottom: lowerRightPoint[1],
                left: upperLeftPoint[0],
                user: userId,
                graphId: window.parent.activityInfo.parent,
            }
        }
    }
    data["toolId"] = window.parent.toolId;
    data["participant"] = window.parent.onlineMembers;

    $.ajax({
        type: "post",
        url: "/GeoProblemSolving/export" + dataType + "Servlet",
        data: JSON.stringify(data),
        processData: false,
        contentType: 'application/json',
        success: function (resp) {
            var result = JSON.parse(resp);
            if (result.respCode === 1) {
                window.parent.loadingBackendOperation(result.operationId);
                alert("Export data successfully!");
                //
            } else {
                alert("Fail to export data!");
            }
        }
    });
}

function importData() {
    if (selectedData == {} || selectedDataType == "") return;

    if (selectedDataType === "Result") {
        $("#loadDataModal").modal("hide");
        loadResultTiff(selectedData.address);

        // collaboration
        window.parent.sendCustomOperation({
            type: "operation",
            sender: userId,
            behavior: "data-visualization",
            content: {
                data: selectedData.address,
            },
        });
    } else {
        if (selectedDataType === "Road") {
            roadData = selectedData;
        } else if (selectedDataType === "Building") {
            buildingData = selectedData;
        } else if (selectedDataType === "Barrier") {
            barrierData = selectedData;
        }

        $.ajax({
            type: "post",
            url: "/GeoProblemSolving/upload" + selectedDataType + "Servlet",
            data: JSON.stringify(selectedData),
            processData: false,
            contentType: 'application/json',
            beforeSend: function () {
                $("#waitting").show();
            },
            success: function (data) {
                $("#waitting").hide();
                // console.log(data);
                var result = JSON.parse(data);
                if (result.respCode == 1) {
                    $("#loadDataModal").modal("hide");

                    dataId = selectedData.uid;
                    mymap.setView([result.centerLat, result.centerLong], 15);

                    switch (selectedDataType) {
                        case "Road":
                            loadShapefile(roadCenterLayer, result.url);
                            roadDataId = dataId;
                            createRoad.presentID = result.roadMaxID + 1;
                            break;
                        case "Building":
                            loadShapefile(buildingLayer, result.url);
                            buildingDataId = dataId;
                            createBuilding.presentID = result.buildingMaxID + 1;
                            break;
                        case "Barrier":
                            loadShapefile(barrierLayer, result.url);
                            barrierDataId = dataId;
                            break;
                    }

                    // collaboration
                    window.parent.sendCustomOperation({
                        type: "operation",
                        sender: userId,
                        behavior: "data-import",
                        content: {
                            data: selectedData,
                            featureType: selectedDataType,
                            result: result
                        },
                    });
                }
            }
        });
    }
}

$("#loadDataModal").on("hidden.bs.modal", function () {
    $("#loadFile").val("");
    $("#uploadForm").off("submit");
})

var infoEditState = false;

//选择研究区域
var selectROI = {
    isSelect: false,
    ROI: null,
    startPoint: null,
    endPoint: null,
    rlayer: L.layerGroup(),
    color: "#0D82D7",
    mousedown: function (e) {
        selectROI.startPoint = e.latlng;
        // console.log(selectROI.startPoint)
        mymap.on("mousemove", selectROI.mousemove);

        if (selectROI.isSelect) {
            window.parent.sendCustomOperation({
                type: "operation",
                sender: userId,
                behavior: "select-start",
                content: {},
            });
        }
    },
    mousemove: function (e) {
        //移动时删掉上一次生成的矩形
        if (selectROI.ROI)
            selectROI.rlayer.removeLayer(selectROI.ROI);

        selectROI.endPoint = e.latlng;
        selectROI.ROI = L.rectangle([selectROI.startPoint, selectROI.endPoint], {color: selectROI.color, weight: 1});
        // ROI.addTo(mymap);
        selectROI.ROI.addTo(selectROI.rlayer);
        mymap.off("mousedown", selectROI.mousedown)
    },
    mouseup: function (e) {
        // mymap.off("mousemove", selectROI.mousemove).off("mousedown", selectROI.mousedown);
        mymap.off("mousedown", selectROI.mousedown).off("mousemove", selectROI.mousemove).off("mouseup", selectROI.mouseup);
        $("#mapid").css("cursor", "-webkit-grab");


        if (selectROI.isSelect) {
            window.parent.sendCustomOperation({
                type: "operation",
                sender: userId,
                behavior: "select-end",
                content: selectROI.rlayer.toGeoJSON(),
            });
        }
    }
}
selectROI.rlayer.addTo(mymap);

function onSelectClick() {
    cancelAllState();
    $("#brforeRunModal").modal("hide");
    if (selectROI.isSelect == false) {
        //点击按钮后禁用地图拖动
        mymap.dragging.disable();
        $("#mapid").css("cursor", "crosshair");
        mymap.on("mousedown", selectROI.mousedown).on("mouseup", selectROI.mouseup);

        selectROI.isSelect = true;

    } else {
        // 移除矩形
        if (selectROI.ROI)
            selectROI.rlayer.removeLayer(selectROI.ROI);

        mymap.dragging.enable();
        $("#mapid").css("cursor", "-webkit-grab");
        mymap.off("mousedown", selectROI.mousedown).off("mousemove", selectROI.mousemove).off("mouseup", selectROI.mouseup);
        selectROI.isSelect = false;
    }
    $(this).toggleClass('Selected');

}

var createRoad = {
    // 准备数据时后台返回的当前已有最大ID+1，为新的要素ID
    presentID: null,
    points: [],
    lines: new L.polyline([], {
        "color": "#ff0000"
    }),
    tempLines: new L.polyline([], {
        "color": "#ff6666"
    }),
    color: '#333333',
    circleGroup: new L.layerGroup().addTo(mymap),
    geoJson: null,
    mouseclick: function (e) {
        createRoad.points.push([e.latlng.lat, e.latlng.lng]);
        createRoad.lines.addLatLng(e.latlng);
        mymap.addLayer(createRoad.lines);
        createRoad.circleGroup.addLayer(L.circle(e.latlng, {
            radius: 0.5,
            color: createRoad.color,
            fillColor: createRoad.color,
            fillOpacity: 1
        }));
        mymap.on('mousemove', createRoad.mousemove).on('dblclick', createRoad.mousedblclick);
    },
    mousemove: function (e) {
        var pointsLength = createRoad.points.length
        if (pointsLength > 0) {
            // 将上一个点和当前点连接起来
            var ls = [createRoad.points[pointsLength - 1], [e.latlng.lat, e.latlng.lng]];
            // setLatLngs是将图层中所有的点替换掉
            createRoad.tempLines.setLatLngs(ls);
            mymap.addLayer(createRoad.tempLines);

            // 禁止地图双击缩放事件
            mymap.doubleClickZoom.disable();
        }
    },
    mousedblclick: function (e) {
        // L.polyline(createRoad.points).addTo(mymap);
        // 将polyline转换为geoJSON
        createRoad.geoJson = createRoad.lines.toGeoJSON();

        createRoad.points = [];
        createRoad.lines.remove();
        createRoad.tempLines.remove();
        // 由于双击后再次触发单击事件，所以line里面会有一个最后的那个点，需要重新new
        createRoad.lines = new L.polyline([], {
            "color": "#ff0000"
        });
        createRoad.circleGroup.clearLayers();
        mymap.off('click', createRoad.mouseclick).off('mousemove', createRoad.mousemove).off('dblclick', createRoad.mousedblclick);
        $("#mapid").css("cursor", "-webkit-grab");

        // 允许地图双击缩放事件
        mymap.doubleClickZoom.enable();
        createRoad.editRoadInfo();

    },
    editRoadInfo: function () {
        var roadID = createRoad.presentID++;
        var roadLength = 0;
        $("#roadID").text(roadID);
        $("#roadName").text("未知道路");
        $("#roadLength").val(roadLength.toFixed(2));
        $("#roadWidth").val(10);
        $("#roadSlope").val(0);
        $("#roadM").val(1000);
        $("#roadP").val(10);
        $("#carSpeed").val(80);
        $("#truckSpeed").val(40);
        $("#roadSurface").val(0);
        $("#reflectHeight").val("");
        $("#reflectWidth").val("");
        $("#hasReflect").prop("checked", true);
        $("#reflectHeight").prop("disabled", false);
        $("#reflectWidth").prop("disabled", false);
        $("#reflectHeight").val(30);
        $("#reflectWidth").val(40);
        $("#isAbsorb").prop("disabled", false);
        $("#isAbsorb").val(0);

        createRoad.geoJson.properties = {
            "OBJECTID": roadID,
            "NAME": "未知道路",
            "Width": 10,
            "M": 1000,
            "P": 10,
            "CarSpeed": 80,
            "TruckSpeed": 40,
            "HasReflect": 1,
            "RefHeight": 30,
            "RefWidth": 40,
            "RefAbsorb": 0,
            "Slope": 0,
            "Surface": 0
        }

        roadCenterLayer.addData(createRoad.geoJson);

        if (roadDataId != undefined || roadDataId != null) {
            $("#roadConfigModal").modal("show");
        } else {
            alert("Please create road on a road layer.");
        }

    }
}

function onCreateRoadClick(e) {
    cancelAllState();
    $("#mapid").css("cursor", "crosshair");

    // 停止对建筑物编辑的监听
    mymap.off('click', createBuilding.mouseclick).off('mousemove', createBuilding.mousemove).off('dblclick', createBuilding.mousedblclick);
    mymap.on('click', createRoad.mouseclick);

    // 阻止事件向上层冒泡
    e = e || window.event;
    if (e.stopPropagation) {
        // W3C的阻止方法
        e.stopPropagation();
    } else {
        // IE的阻止方法
        e.cancelBubble = true;
    }

}

var createBuilding = {
    // 准备数据时后台返回的当前已有最大ID+1，为新的要素ID
    presentID: null,
    points: [],
    color: '#777777',
    lines: new L.polyline([], {
        weight: 2,
        opacity: 1,
        color: 'black',
        dashArray: '3',
        fillOpacity: 0.7

    }),
    tempLines: new L.polyline([], {
        weight: 2,
        opacity: 1,
        color: '#777777',
        dashArray: '3',
        fillOpacity: 0.7

    }),
    circleGroup: new L.layerGroup().addTo(mymap),
    geoJson: null,
    mouseclick: function (e) {
        createBuilding.points.push([e.latlng.lat, e.latlng.lng]);
        createBuilding.lines.addLatLng(e.latlng);
        mymap.addLayer(createBuilding.lines);
        mymap.addLayer(createBuilding.tempLines);
        createBuilding.circleGroup.addLayer(L.circle(e.latlng, {
            radius: 0.5,
            color: createBuilding.color,
            fillColor: createBuilding.color,
            fillOpacity: 1
        }));
        mymap.on('mousemove', createBuilding.mousemove).on('dblclick', createBuilding.mousedblclick);
    },
    mousemove: function (e) {
        var pointsLength = createBuilding.points.length
        if (pointsLength > 0) {
            // 将上一个点、鼠标当前点和初始点连接起来
            var ls = [createBuilding.points[pointsLength - 1], [e.latlng.lat, e.latlng.lng], createBuilding.points[0]];
            // setLatLngs是将图层中所有的点替换掉
            createBuilding.tempLines.setLatLngs(ls);
            mymap.addLayer(createBuilding.tempLines);

            // 禁止地图双击缩放事件
            mymap.doubleClickZoom.disable();
        }
    },
    mousedblclick: function (e) {
        // var buildingPolygon = L.polygon(createBuilding.points);
        // buildingPolygon.addTo(mymap);
        createBuilding.geoJson = L.polygon(createBuilding.points).toGeoJSON();

        createBuilding.points = [];
        createBuilding.lines.remove();
        createBuilding.tempLines.remove();
        // 由于双击后再次触发单击事件，所以line里面会有一个最后的那个点，需要重新new
        createBuilding.lines = new L.polyline([], {
            weight: 2,
            opacity: 1,
            color: 'black',
            dashArray: '3',
            fillOpacity: 0.7

        });

        createBuilding.circleGroup.clearLayers();
        mymap.off('click', createBuilding.mouseclick).off('mousemove', createBuilding.mousemove).off('dblclick', createBuilding.mousedblclick);
        $("#mapid").css("cursor", "-webkit-grab");

        // 允许地图双击缩放事件
        mymap.doubleClickZoom.enable();
        createBuilding.editBuildingInfo();
    },
    editBuildingInfo: function () {
        var buildingID = createBuilding.presentID++;
        $("#buildingID").text(buildingID);
        $("#buildingStorey").val(10);
        $("#buildingHeight").val(30);

        createBuilding.geoJson.properties = {
            "OBJECTID": buildingID,
            "Storey": 10,
            "Height": 30
        }

        var latlngPoint = createBuilding.geoJson.geometry.coordinates[0];
        var prjPoint = [];
        for (var i = 0, l = latlngPoint.length; i < l; i++) {
            var tempPoint = proj4("EPSG:4326", "EPSG:2437", [latlngPoint[i][0], latlngPoint[i][1]]);
            prjPoint.push(tempPoint)
        }

        $.ajax({
            type: "post",
            url: "/GeoProblemSolving/createBuildingServlet",
            data: {
                "userId": userId,
                "buildingDataId": buildingDataId,
                "prjPoint": JSON.stringify(prjPoint),
                "prop": JSON.stringify(createBuilding.geoJson.properties)
            },
            dataType: "text",
            success: function (data) {
                // 将新要素添加到building图层
                buildingLayer.addData(createBuilding.geoJson);
            },
            error: function (e) {
                console.log(e);
            }
        });

        $("#buildingConfigModal").modal("show");
    }
}

function onCreateBuildingClick(e) {
    cancelAllState();
    $("#mapid").css("cursor", "crosshair");

    // 取消对编辑道路事件的绑定
    mymap.off('click', createRoad.mouseclick).off('mousemove', createRoad.mousemove).off('dblclick', createRoad.mousedblclick);
    mymap.on('click', createBuilding.mouseclick);

    // 阻止事件向上层冒泡
    e = e || window.event;
    if (e.stopPropagation) {
        // W3C的阻止方法
        e.stopPropagation();
    } else {
        // IE的阻止方法
        e.cancelBubble = true;
    }
}

function onMouseClick() {
    cancelAllState();
    $("#brforeRunModal").modal("hide");
    $("#mapid").css("cursor", "-webkit-grab");


    // send message
    window.parent.sendCustomOperation({
        type: "operation",
        sender: userId,
        behavior: "mouse-click",
        content: "mouse-click",
    });
}

function onEditInfoClick() {
    cancelAllState();
    infoEditState = true;
    $("#brforeRunModal").modal("hide");
    $("#mapid").css("cursor", "default");
}

function onCleanClick() {
    cancelAllState();
    if (selectROI.ROI)
        selectROI.rlayer.removeLayer(selectROI.ROI);

    noiseResultLayer.clearLayers();
    $(".legend").hide();

    window.parent.sendCustomOperation({
        type: "operation",
        sender: userId,
        behavior: "clean",
        content: "clean",
    });
}

function onBeforeRun() {
    cancelAllState();

    if (!selectROI.isSelect) {
        alert("Please select a computation area.");
        return;
    }

    $("#sampleSize").val(10);
    $("#ROIHeight").val(10);
    $("#brforeRunModal").modal("show");
    $("#mapid").css("cursor", "-webkit-grab");
    $("#runModelBtn").show();

    window.parent.sendCustomOperation({
        type: "operation",
        sender: userId,
        behavior: "before-run",
        content: "before-run",
    });
}

function onEditFeatureClick() {
    mymap.doubleClickZoom.disable();
    cancelAllState();
    editState = true;
}

// 在点击按钮之前取消当前的所有编辑状态
function cancelAllState() {
    $("#barrierConfigModal").modal("hide");
    $("#roadConfigModal").modal("hide");
    $("#buildingConfigModal").modal("hide");
    $("#brforeRunModal").modal("hide");
    infoEditState = false;
    editState = false;
    if (editingLayer != null) {
        editingLayer.disableEdit();
        editingLayer = null;
    }
    mymap.dragging.enable();
    mymap.doubleClickZoom.enable();
}


function closeConfig() {
    this.hide();
}

// var resultBounds;
// from manager server
function onRunModel() {
    var sampleSize = $("#sampleSize").val();
    var height = $("#ROIHeight").val();
    var northWestPoint = selectROI.ROI.getBounds().getNorthWest();
    var southEastPoint = selectROI.ROI.getBounds().getSouthEast();
    var upperLeftPoint = proj4("EPSG:4326", "EPSG:2437", [northWestPoint.lng, northWestPoint.lat]);
    var lowerRightPoint = proj4("EPSG:4326", "EPSG:2437", [southEastPoint.lng, southEastPoint.lat]);
    $("#mapid").css("cursor", "wait");
    $.ajax({
        // async:false,
        type: "post",
        url: "/GeoProblemSolving/prepareModelData",
        data: {
            userId: userId,
            roadData: roadData.uid,
            buildingData: buildingData.uid,
            barrierData: barrierData.uid,
            top: upperLeftPoint[1],
            right: lowerRightPoint[0],
            bottom: lowerRightPoint[1],
            left: upperLeftPoint[0],
            sampleSize: sampleSize,
            height: height
        },
        dataType: "text",
        beforeSend: function () {
            // console.log({
            //     maxLat: upperLeftPoint[1],
            //     maxLon: lowerRightPoint[0],
            //     minLat: lowerRightPoint[1],
            //     minLon: upperLeftPoint[0]
            // });
            $("#waitting").show();
        },
        success: function (resp) {
            var result = JSON.parse(resp);
            if (result.respCode == 1) {

                let inputs = [];
                inputs.push({
                    statename: "LoadVariables",
                    event: "InputRegionBBox",
                    tag: "InputRegionBBox",
                    url: result.regionBBox
                });
                inputs.push({
                    statename: "LoadVariables",
                    event: "InputRoadCenterLineData",
                    tag: "InputRoadCenterLineData",
                    url: result.road
                });
                inputs.push({
                    statename: "LoadVariables",
                    event: "InputBuildingData",
                    tag: "InputBuildingData",
                    url: result.building
                });
                inputs.push({
                    statename: "LoadVariables",
                    event: "InputBarrierData",
                    tag: "InputBarrierData",
                    url: result.barrier
                });
                inputs.push({
                    statename: "LoadVariables",
                    event: "InputHeightData",
                    tag: "InputHeightData",
                    url: result.height
                });
                inputs.push({
                    statename: "LoadVariables",
                    event: "InputSamplingSizeData",
                    tag: "InputSamplingSizeData",
                    url: result.samplingSize
                });
                let outputs = [{
                    statename: "NoiseRegion",
                    event: "Output_RegionNoise",
                    template: {
                        type: "none",
                        value: "",
                    }
                }]

                window.parent.sendCustomOperation({
                    type: "operation",
                    sender: userId,
                    behavior: "run-start",
                    content: "run-start",
                });

                window.parent.sendModelOperation(window.parent.activityInfo.aid, "da09d518ee0b8f7c50c2d4b695b98553", "172.21.213.105", "8061", inputs, outputs);

            } else {
                $("#waitting").hide();
                alert("Fail to get the simulation result!");
            }
        },
        error: function (e) {
            $("#waitting").hide();
            $("#mapid").css("cursor", "-webkit-grab");
            console.log(e);
        }
    });
}

// directly from model service container
// function onRunModel() {
//     var sampleSize = $("#sampleSize").val();
//     var height = $("#ROIHeight").val();
//     var northWestPoint = selectROI.ROI.getBounds().getNorthWest();
//     var southEastPoint = selectROI.ROI.getBounds().getSouthEast();
//     // console.log("1");
//     $("#mapid").css("cursor", "wait");
//     var upperLeftPoint = proj4("EPSG:4326", "EPSG:2437", [northWestPoint.lng, northWestPoint.lat]);
//     var lowerRightPoint = proj4("EPSG:4326", "EPSG:2437", [southEastPoint.lng, southEastPoint.lat]);
//     // resultBounds = [[lowerRightPoint[1],upperLeftPoint[0]],[upperLeftPoint[1],lowerRightPoint[0]]];
//     $.ajax({
//         // async:false,
//         type: "post",
//         url: "/GeoProblemSolving/runModelServlet",
//         data: {
//             userId: userId,
//             roadData: roadData.uid,
//             buildingData: buildingData.uid,
//             barrierData: barrierData.uid,
//             top: upperLeftPoint[1],
//             right: lowerRightPoint[0],
//             bottom: lowerRightPoint[1],
//             left: upperLeftPoint[0],
//             maxLat: northWestPoint.lat,
//             maxLon: southEastPoint.lng,
//             minLat: southEastPoint.lat,
//             minLon: northWestPoint.lng,
//             sampleSize: sampleSize,
//             height: height
//         },
//         dataType: "text",
//         beforeSend: function () {
//             console.log({
//                 maxLat: upperLeftPoint[1],
//                 maxLon: lowerRightPoint[0],
//                 minLat: lowerRightPoint[1],
//                 minLon: upperLeftPoint[0]
//             });
//             $("#waitting").show();
//         },
//         success: function (resp) {
//             var result = JSON.parse(resp);
//             if (result.respCode == 1) {
//                 runningId = result.resultId;
//                 resultIdList.push(runningId);
//                 state = setInterval(getModelState, 2000);
//             }
//             else {
//                 console.log("error!");
//             }
//         },
//         error: function (e) {
//             $("#waitting").hide();
//             $("#mapid").css("cursor", "-webkit-grab");
//             console.log(e);
//         }
//     });
// }

var state;

function getModelState() {
    $.ajax({
        type: "post",
        url: "/GeoProblemSolving/getModelState",
        data: {
            userId: userId,
            runningId: runningId
        },
        dataType: "text",
        success: async (resp) => {
            var result = JSON.parse(resp);
            if (result.finished == 1) {
                clearInterval(state);
                state = null;
                // 如果出错
                if (result.error == 1) {
                    $("#waitting").hide();
                    $("#mapid").css("cursor", "-webkit-grab");
                    alert("error!");
                } else {
                    $("#brforeRunModal").modal("hide");
                    onSelectClick();
                    $("#mapid").css("cursor", "-webkit-grab");

                    loadResultTiff(result.url);
                    $("#waitting").hide();

                    window.parent.sendCustomOperation({
                        type: "operation",
                        sender: userId,
                        behavior: "run-success",
                        content: {
                            userId: userId,
                            result: result.url
                        },
                    });
                }
            }
        },
        error: function (e) {
            $("#waitting").hide();
            $("#mapid").css("cursor", "-webkit-grab");
            console.log(e);
        }
    });
}

function onHasReflectChanged(id) {
    var isChecked = $("#hasReflect").prop("checked");
    if (isChecked == true) {
        $("#reflectHeight").prop("disabled", false);
        $("#reflectHeight").val(30);
        $("#reflectWidth").prop("disabled", false);
        $("#reflectWidth").val(40);
        $("#isAbsorb").prop("disabled", false);
        $("#isAbsorb").val(0);
    } else {
        $("#reflectHeight").val("");
        $("#reflectHeight").prop("disabled", true);
        $("#reflectWidth").val("");
        $("#reflectWidth").prop("disabled", true);
        $("#isAbsorb").val(-1);
        $("#isAbsorb").prop("disabled", true);
    }

    // send message
    window.parent.sendElementChangeOperation(id, "value-change", "checkbox", "", "", isChecked);
}


// 道路信息
function onRoadClick(prop) {
    if (infoEditState == true) {
        $.ajax({
            type: "post",
            url: "/GeoProblemSolving/getRoadInfoServlet",
            data: JSON.stringify({
                "roadData": roadData,
                "id": prop.OBJECTID,
            }),
            processData: false,
            contentType: 'application/json',
            success: function (data) {

                var result = JSON.parse(data);

                if (result.respCode === 1) {
                    $("#roadName").text(prop.NAME);
                    $("#roadID").text(prop.OBJECTID);
                    $("#roadLength").val(result.length.toFixed(2));
                    $("#roadWidth").val(result.width);
                    $("#roadSlope").val(result.slope);
                    $("#roadM").val(result.M);
                    $("#roadP").val(result.P);
                    $("#carSpeed").val(result.carSpeed);
                    $("#truckSpeed").val(result.truckSpeed);
                    $("#roadSurface").val(result.roadSurface);

                    if (result.hasReflect == 1) {
                        $("#hasReflect").prop("checked", true);
                        $("#reflectHeight").prop("disabled", false);
                        $("#reflectWidth").prop("disabled", false);
                        $("#isAbsorb").prop("disabled", false);
                        $("#reflectHeight").val(result.reflectHeight);
                        $("#reflectWidth").val(result.reflectWidth);
                        $("#isAbsorb").val(result.reflectAbsorb);
                    } else {
                        $("#reflectHeight").val("");
                        $("#reflectWidth").val("");
                        $("#isAbsorb").val(-1);
                        $("#hasReflect").prop("checked", false);
                        $("#reflectHeight").prop("disabled", true);
                        $("#reflectWidth").prop("disabled", true);
                        $("#isAbsorb").prop("disabled", true);
                    }

                    $("#roadConfigModal").modal("show");

                    // send message
                    window.parent.sendCustomOperation({
                        type: "operation",
                        sender: userId,
                        behavior: "road-edit-start",
                        content: {propInfo: prop, resultInfo: result},
                    });
                    $("#roadInfoSubmit").show();

                } else {
                    alert("Fail to Set the property of this road.");
                }

            },
            error: function (e) {
                console.log(e);
            }
        });
    }
}

var currentPage = 0;
var roadPage = ["#roadInfo", "#trafficInfo", "#reflectInfo"];

function showInfo() {
    // 如果是第一个或者最后一个，则会有disabled class，直接返回
    if (this.parent("span").hasClass("disabled")) {
        return;
    }
    // 将当前页面收起来
    $(roadPage[currentPage]).slideUp();
    // 获取点击对象的id
    objId = this[0].id;
    if (objId == "previousInfo") {
        currentPage--;
    } else {
        currentPage++;
    }

    if (currentPage == 0) {
        $("#previousInfo").parent("span").addClass("disabled");
    } else if (currentPage == 2) {
        $("#nextInfo").parent("span").addClass("disabled");
    } else {
        $("#previousInfo").parent("span").removeClass("disabled");
        $("#nextInfo").parent("span").removeClass("disabled");
    }
    // 将目标页面滑下来
    $(roadPage[currentPage]).slideDown();

    // collaboration
    window.parent.sendCustomOperation({
        type: "operation",
        sender: userId,
        behavior: "params-panel",
        content: objId,
    });
}

var roadConfig = {
    id: "-1",
    width: "10",
    slope: "5",
    M: "1000",
    P: "10",
    carSpeed: "80",
    truckSpeed: "40",
    roadSurface: "0",
    hasReflect: "1",
    reflectHeight: "30",
    reflectWidth: "40",
    reflectAbsorb: "0"
}

function onSetRoadInfoClick() {
    roadConfig.id = $("#roadID").text();
    roadConfig.width = $("#roadWidth").val();
    roadConfig.M = $("#roadM").val();
    roadConfig.P = $("#roadP").val();
    roadConfig.carSpeed = $("#carSpeed").val();
    roadConfig.truckSpeed = $("#truckSpeed").val();
    roadConfig.roadSurface = $("#roadSurface").val();
    roadConfig.slope = $("#roadSlope").val();

    roadConfig.hasReflect = $("#hasReflect").prop("checked") ? 1 : 0;
    roadConfig.reflectHeight = $("#reflectHeight").val();
    roadConfig.reflectWidth = $("#reflectWidth").val();
    roadConfig.reflectAbsorb = $("#isAbsorb").val();

    // console.log(roadConfig);

    $.ajax({
        type: "post",
        url: "/GeoProblemSolving/setRoadInfoServlet",
        data: JSON.stringify({
            "roadData": roadData,
            "roadConfig": JSON.stringify(roadConfig)
        }),
        processData: false,
        contentType: 'application/json',
        success: function (data) {
            var result = JSON.parse(data);
            if (result.respCode == 1) {
                let path = "/GeoProblemSolving" + result.newAddress.replace(/\\/g, '/');
                var xhr = new XMLHttpRequest();
                xhr.open("GET", path, true);
                xhr.responseType = "blob";
                xhr.onload = function (e) {
                    if (this.status == 200) {
                        var file = this.response;
                        var fileOfBlob = new File([file], roadData.name + roadData.suffix);
                        let resultData = window.parent.resaveResource(fileOfBlob, {uid: roadData.uid});
                        roadData.address = resultData.address;
                        alert("Set the property of building successfully.");
                    }
                };
                xhr.send();

            } else {
                alert("Fail to set the property of this road.");
            }

            // send message
            window.parent.sendCustomOperation({
                type: "operation",
                sender: userId,
                behavior: "edit-end",
                content: result.respCode,
            });
        },
        error: function (e) {
            console.log(e);
        }
    });

    // var filename = "Unnamed road" + roadID;
    // var description = "Road data";
    // var geojsonBlob = new Blob(
    //     [JSON.stringify(createRoad.geoJson, null, 2)],
    //     { type: "application/json" }
    // );
    // var fileOfBlob = new File([geojsonBlob], filename);
    // // upload
    // let file = saveResources([fileOfBlob], description, "data", "private", "");

    $("#roadConfigModal").modal("hide");
}

$("#roadConfigModal").on("hidden.bs.modal", function () {
    $("#roadCigContent").removeAttr("style");
    // 将道路页面切换到第一个
    $(roadPage[currentPage]).hide();
    currentPage = 0;
    $(roadPage[currentPage]).show();
    $("#previousInfo").parent("span").addClass("disabled");
    $("#nextInfo").parent("span").removeClass("disabled");
});


// 建筑物信息
function onBuildingClick(prop) {
    if (infoEditState == true) {
        $.ajax({
            type: "post",
            url: "/GeoProblemSolving/getBuildingInfoServlet",
            data: JSON.stringify({
                "buildingData": buildingData,
                "id": prop.OBJECTID
            }),
            processData: false,
            contentType: 'application/json',
            success: function (data) {
                // console.log(data);
                var result = JSON.parse(data);
                if (result.respCode === 1) {
                    $("#buildingID").text(prop.OBJECTID);
                    $("#buildingStorey").val(result.Storey);
                    $("#buildingHeight").val(result.Height);

                    $("#buildingConfigModal").modal("show");


                    // send message
                    window.parent.sendCustomOperation({
                        type: "operation",
                        sender: userId,
                        behavior: "building-edit-start",
                        content: {propInfo: prop, resultInfo: result}
                    });
                    $("#buildingInfoSubmit").show();
                } else {
                    alert("Fail to get the information of this building.");
                }
            },
            error: function (e) {
                console.log(e);
            }
        });
    }
}

var buildingConfig = {
    id: "-1",
    storey: "10",
    height: "30"
}

function onSetBuildingInfoClick() {
    buildingConfig.id = $("#buildingID").text();
    buildingConfig.storey = $("#buildingStorey").val();
    buildingConfig.height = $("#buildingHeight").val();
    // console.log(buildingConfig);

    $.ajax({
        type: "post",
        url: "/GeoProblemSolving/setBuildingInfoServlet",
        data: JSON.stringify({
            "buildingConfig": JSON.stringify(buildingConfig),
            "buildingData": buildingData
        }),
        processData: false,
        contentType: 'application/json',
        success: function (data) {
            var result = JSON.parse(data);
            if (result.respCode === 1) {
                let path = "/GeoProblemSolving" + result.newAddress.replace(/\\/g, '/');
                var xhr = new XMLHttpRequest();
                xhr.open("GET", path, true);
                xhr.responseType = "blob";
                xhr.onload = function (e) {
                    if (this.status == 200) {
                        var file = this.response;
                        var fileOfBlob = new File([file], buildingData.name + buildingData.suffix);
                        let resultData = window.parent.resaveResource(fileOfBlob, {uid: buildingData.uid});
                        buildingData.address = resultData.address;
                        alert("Set the property of building successfully.");
                    }
                };
                xhr.send();

            } else {
                alert("Fail to Set the property of this building.");
            }

            // send message
            window.parent.sendCustomOperation({
                type: "operation",
                sender: userId,
                behavior: "edit-end",
                content: result.respCode,
            });
        },
        error: function (e) {
            console.log(e);
        }
    });
    $("#buildingConfigModal").modal("hide");
}

$("#buildingConfigModal").on("hidden.bs.modal", function () {
    $("#buildingCigContent").removeAttr("style");

});


// 道路声屏障信息
function onBarrierClick(prop) {
    // console.log(prop)
    if (infoEditState == true) {
        $.ajax({
            type: "post",
            url: "/GeoProblemSolving/getBarrierInfoServlet",
            data: JSON.stringify({
                "barrierData": barrierData,
                "id": prop.OBJECTID
            }),
            processData: false,
            contentType: 'application/json',
            success: function (data) {
                // console.log(data);
                var result = JSON.parse(data);
                if (result.respCode == 1) {
                    $("#barrierID").text(prop.OBJECTID);
                    $("#barrierLength").val(result.Length);
                    $("#barrierHeight").val(result.Height);

                    $("#barrierConfigModal").modal("show");


                    // send message
                    window.parent.sendCustomOperation({
                        type: "operation",
                        sender: userId,
                        behavior: "barrier-edit-start",
                        content: {propInfo: prop, resultInfo: result},
                    });
                    $("#barrierInfoSubmit").show();
                } else {
                    alert("Fail to set the property of this barrier.");
                }
            },
            error: function (e) {
                console.log(e);
            }
        });
    }
}

var barrierConfig = {
    id: "-1",
    length: "10",
    height: "5"
}

function onSetBarrierInfoClick() {
    barrierConfig.id = $("#barrierID").text();
    barrierConfig.length = $("#barrierLength").val();
    barrierConfig.height = $("#barrierHeight").val();
    // console.log(barrierConfig);

    $.ajax({
        type: "post",
        url: "/GeoProblemSolving/setBarrierInfoServlet",
        data: JSON.stringify({
            "barrierConfig": JSON.stringify(barrierConfig),
            "barrierData": barrierData
        }),
        processData: false,
        contentType: 'application/json',
        success: function (data) {
            var result = JSON.parse(data);
            if (result.respCode == 1) {
                let path = "/GeoProblemSolving" + result.newAddress.replace(/\\/g, '/');
                var xhr = new XMLHttpRequest();
                xhr.open("GET", path, true);
                xhr.responseType = "blob";
                xhr.onload = function (e) {
                    if (this.status == 200) {
                        var file = this.response;
                        var fileOfBlob = new File([file], barrierData.name + barrierData.suffix);
                        let resultData = window.parent.resaveResource(fileOfBlob, {uid: barrierData.uid});
                        barrierData.address = resultData.address;
                        alert("Set the property of building successfully.");
                    }
                };
                xhr.send();
            } else {
                alert("Fail to Set the property of this building.");
            }

            // send message
            window.parent.sendCustomOperation({
                type: "operation",
                sender: userId,
                behavior: "edit-end",
                content: result.respCode,
            });
        },
        error: function (e) {
            console.log(e);
        }
    });
    $("#barrierConfigModal").modal("hide");
}

$("#barrierConfigModal").on("hidden.bs.modal", function () {
    $("#barrierCigContent").removeAttr("style");
});

var editState = false;
var editingLayer = null;

function editLayer(layer) {
    if (editState == 1) {
        // 先清除上一个图层的编辑状态
        if (editingLayer != null) {
            editingLayer.disableEdit();
        }
        if (editingLayer != layer) {
            layer.enableEdit();
            editingLayer = layer;
            $(document).on('keydown', deleteLayer);
            // editing会在修改的时候连续触发，不能在触发editing时保存修改
            layer.on("editable:editing", function (e) {
                // 监听事件不能绑定在layer上，修改后的layer不属于layer的范围,也不能使用匿名函数，那样会被绑定多次
                mymap.on("mouseup", editMouseUp);
            })
        }
        // 如果目标图层为上一个正在处于编辑状态的图层，则取消编辑状态
        else {
            editingLayer = null;
        }
    }
}

// 在编辑结束后鼠标抬起时触发
function editMouseUp(e) {
    if (editingLayer) {
        saveEdit("edit", editingLayer);
    }
    // 编辑结束后取消事件监听
    mymap.off("mouseup", editMouseUp);
}

function deleteLayer(e) {
    if (e.keyCode == 46) {
        editingLayer.remove();
        saveEdit("delete", editingLayer);
        $(document).off('keydown', deleteLayer);
    }
}

function saveEdit(editType, layer) {
    var layerName = layer.name;
    var latlngPoints;
    var prjPoints = [];
    var OBJECTID = layer.feature.properties.OBJECTID;
    var dataId;

    if (layerName == "Building") {
        latlngPoints = layer._latlngs[0];
        dataId = buildingDataId;
    } else if (layerName == "RoadCenterLine") {
        latlngPoints = layer._latlngs;
        dataId = roadDataId;
    } else {
        latlngPoints = layer._latlngs;
        dataId = barrierDataId;
    }

    if (editType != "delete") {
        // 将地理坐标转换为投影坐标
        latlngPoints.forEach(point => {
            var tempPoint = proj4("EPSG:4326", "EPSG:2437", [point.lng, point.lat]);
            prjPoints.push(tempPoint);
        });
        // if (layerName == "building") {
        //     // 最后一个点为第一个点，形成闭合环
        //     prjPoints.push(prjPoints[0]);
        // }
    }

    // console.log(prjPoints);

    $.ajax({
        type: "post",
        url: "/GeoProblemSolving/editFeatureServlet",
        data: {
            "userId": userId,
            "dataId": dataId,
            "editType": editType,
            "editLayer": layerName,
            "OBJECTID": OBJECTID,
            "prjPoints": JSON.stringify(prjPoints),
        },
        dataType: "text",
        success: function (data) {
            // 将新要素添加到building图层
            // roadCenterLayer.addData(createRoad.geoJson);
        },
        error: function (e) {
            console.log(e);
        }
    });

}

var buildingLayer = L.Proj.geoJson(null, {

    style(feature) {
        if (!feature || !feature.properties) {
            // console.log(feature);
            return {
                fillColor: "#AAAAAA",
                weight: 2,
                opacity: 1,
                color: 'black',
                dashArray: '3',
                fillOpacity: 0.7
            }
        }
        return {
            fillColor: getColor(feature.properties.Storey),
            weight: 2,
            opacity: 1,
            color: 'black',
            dashArray: '3',
            fillOpacity: 0.7
        };
    },
    // 鼠标在建筑物上的事件
    onEachFeature(feature, layer) {
        // 在添加图层时给图层名称
        layer = $.extend(layer, {
            name: "Building"
        });
        // console.log(layer);

        // layer.enableEdit(mymap);
        layer.on({
            mouseover(e) {
                var _layer = e.target;
                _layer.setStyle({
                    weight: 5,
                    color: '#666',
                    dashArray: '',
                    fillOpacity: 0.7
                });
                _layer.bringToFront();
                info.update(_layer.feature.properties);
            },
            mouseout(e) {
                buildingLayer.resetStyle(e.target);
                info.update();
            },
            click(e) {
                var prop = e.target.feature.properties;
                onBuildingClick(prop);
            },
            dblclick(e) {
                editLayer(e.target);
            }
        });
    },
}).addTo(mymap);
var roadCenterLayer = L.Proj.geoJson(null, {
    style(feature) {
        return {
            "color": "#ff0000"
        }
    },
    // 鼠标在道路上的事件
    onEachFeature(feature, layer) {
        // 在添加图层时给图层名称
        layer = $.extend(layer, {
            name: "RoadCenterLine"
        });
        layer.on({
            mouseover(e) {
                var _layer = e.target;
                _layer.setStyle({
                    weight: 5,
                    dashArray: '',
                    fillOpacity: 0.7
                });
                _layer.bringToFront();
                // console.log(_layer.feature.properties.OBJECTID);
            },
            mouseout(e) {
                roadCenterLayer.resetStyle(e.target);
            },
            click(e) {
                var prop = e.target.feature.properties;
                onRoadClick(prop);
            },
            dblclick(e) {
                editLayer(e.target);
            }
        });
    },
}).addTo(mymap);
var barrierLayer = L.Proj.geoJson(null, {
    style(feature) {
        return {
            "color": "#4444BB"
        }
    },
    // 鼠标在道路声屏障上的事件
    onEachFeature(feature, layer) {
        // 在添加图层时给图层名称
        layer = $.extend(layer, {
            name: "Barrier"
        });
        layer.on({
            mouseover(e) {
                var _layer = e.target;
                _layer.setStyle({
                    weight: 5,
                    dashArray: '',
                    fillOpacity: 0.7
                });
                _layer.bringToFront();
            },
            mouseout(e) {
                barrierLayer.resetStyle(e.target);
            },
            click(e) {
                var prop = e.target.feature.properties;
                onBarrierClick(prop);
            },
            dblclick(e) {
                editLayer(e.target);
            }
        });
    },
}).addTo(mymap);
var noiseResultLayer = L.layerGroup().addTo(mymap);

// 图层开关
L.control.layers({
    noiseResultLayer, roadCenterLayer, buildingLayer, barrierLayer
}).addTo(mymap);

function getNoisePointColor(value) {
    return value > 95 ? '#4d0017' :
        value > 90 ? '#BD0026' :
            value > 85 ? '#E31A1C' :
                value > 80 ? '#FC4E2A' :
                    value > 75 ? '#FD8D3C' :
                        value > 70 ? '#FEB24C' :
                            value > 65 ? '#FED976' :
                                value > 60 ? '#FFEDA0' :
                                    value > 55 ? '#cee77e' :
                                        // '#6DE494';
                                        '#add728';

    // return value > 95 ? '#FF0000' :
    //     value > 90 ? '#FF6F00' :
    //         value > 85 ? '#FFB700' :
    //             value > 80 ? '#FFFF00' :
    //                 value > 75 ? '#D2FF69' :
    //                     value > 70 ? '#91FFB4' :
    //                         value > 65 ? '#00FFFF' :
    //                             value > 60? '#38ACFF':
    //                                 value > 55? '#3661FF':
    //                                     // '#6DE494';
    //                                     '#0000FF';

}

function getColor(value) {
    return value > 35 ? '#000000' :
        value > 30 ? '#222222' :
            value > 25 ? '#444444' :
                value > 20 ? '#555555' :
                    value > 15 ? '#777777' :
                        value > 10 ? '#999999' :
                            value > 5 ? '#AAAAAA' :
                                '#DDDDDD';
}

function highlightFeature(e) {
    var layer = e.target;
    layer.setStyle({
        weight: 5,
        color: '#666',
        dashArray: '',
        fillOpacity: 0.7
    });

    if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {//ie edge opera 目前不支持
        layer.bringToFront();
    }
}

function resetHighlight(e, layer) {
    layer.resetStyle(e.target);
}

function zoomToFeature(e) {
    map.fitBounds(e.target.getBounds());
}

async function loadShapefile(layer, shapefilePath) {
    const source = await shapefile.open(shapefilePath + ".shp", null);
    // 使用utf-8的方法解码属性表文件
    // 玄武盾会拦截对dbf的请求，将其复制为mdbf
    const property = await shapefile.openDbf(shapefilePath + ".mdbf", {encoding: 'utf-8'});
    const response = await fetch(shapefilePath + ".prj");
    const firstProj = await response.text();
    while (true) {
        const feature = await source.read();
        const properties = await property.read();
        if (feature.done) {
            break;
        }

        let data = feature.value;
        data.properties = properties.value;

        data.crs = {
            "type": "name",
            "properties": {"name": "EPSG:2437"}
        };
        if (layer === noiseResultLayer) {
            L.geoJson(data, {
                pointToLayer(feature, latlng) {
                    // 跑出来的数据是投影坐标，将他转换为地理坐标并赋给latlng
                    // 在latlng处画点
                    var point = proj4("EPSG:2437", "EPSG:4326", [latlng.lng, latlng.lat]);
                    latlng.lng = point[0];
                    latlng.lat = point[1];
                    return L.circleMarker(latlng, {
                        radius: 8,
                        fillColor: getNoisePointColor(feature.properties.noise_10),
                        color: "#000",
                        weight: 0,
                        opacity: 1,
                        fillOpacity: 0.8
                    });
                }
            }).addTo(layer);
        }
        // 处理result以外的的其他图层
        else {
            layer.addData(data);
        }

    }
}

function loadResultTiff(resulUrl) {
    let uid;
    if (typeof(resulUrl) == "string"){
        uid = resulUrl.slice(-36);
    }
    let resUrl = resProxy + "/data/" + uid;
    L.leafletGeotiff(
        url = resUrl,
        options = {
            band: 0,
            displayMin: 0,
            displayMax: 100,
            name: "noiseResult",
            colorScale: "Noise",
            clampLow: false,
            clampHigh: true,
            arrowSize: 20,
        }
    ).addTo(mymap);
    buildingLayer.bringToFront();
    barrierLayer.bringToFront();
    roadCenterLayer.bringToFront();
    $(".legend").show();
}

// 页面即将关闭或刷新前保存现在地图的数据
window.onbeforeunload = function () {
    if (roadDataId || buildingDataId || barrierDataId || resultIdList.length > 0) {
        var currentData = {
            "roadDataId": roadDataId,
            "roadMaxID": createRoad.presentID,
            "buildingDataId": buildingDataId,
            "buildingMaxID": createBuilding.presentID,
            "barrierDataId": barrierDataId,
            "resultIdList": resultIdList
        };
        var currentZoom = mymap.getZoom();
        var centerLat = mymap.getCenter().lat;
        var centerLong = mymap.getCenter().lng;

        $.ajax({
            async: false,
            type: "post",
            url: "/GeoProblemSolving/saveMapStatusServlet",
            data: {
                "currentData": JSON.stringify(currentData),
                "currentZoom": currentZoom,
                "centerLat": centerLat,
                "centerLong": centerLong
            },
            dataType: "text",
            // success: function (data) {

            // },
            error: function (e) {
                console.log(e);
            }
        });
    }
}

// 页面加载时重新加载保存的地图状态
window.onpageshow = function (e) {
    $("#waitting").show();
    userId = parent.userId;
    resources = parent.currentResources;
    console.log("onload");
    $.ajax({
        type: "post",
        url: "/GeoProblemSolving/getMapStatusServlet",
        dataType: "text",
        success: async (resp) => {
            var result = JSON.parse(resp);
            if (result.respCode == 1) {
                $("#waitting").hide();
                $("#loadDataModal").modal("hide");
                var currentData = result.currentData;
                roadDataId = currentData.roadDataId;
                buildingDataId = currentData.buildingDataId;
                barrierDataId = currentData.barrierDataId;
                resultIdList = currentData.resultIdList;

                if (roadDataId) {
                    loadShapefile(roadCenterLayer, "./data/userData/" + userId + "/InputData/RoadCenterLine/" + roadDataId + "/RoadCenterLine");
                    createRoad.presentID = currentData.roadMaxID;
                }
                if (buildingDataId) {
                    loadShapefile(buildingLayer, "./data/userData/" + userId + "/InputData/Building/" + buildingDataId + "/Building");
                    createBuilding.presentID = currentData.buildingMaxID;
                }
                if (barrierDataId) {
                    loadShapefile(barrierLayer, "./data/userData/" + userId + "/InputData/Barrier/" + barrierDataId + "/Barrier");
                }
                if (resultIdList.length) {
                    // resultIdList.forEach(function (item) {
                    //     getModelState(userId, item)
                    // })
                }
                mymap.setView([result.centerLat, result.centerLong], result.currentZoom);
            } else {
                // $("#loadDataModal").modal("show");
            }
            $("#waitting").hide();
        },
        error: function (e) {
            console.log(e);
        }
    });
}


$(function () {
    userId = parent.userId;
    // 左上角的操作键
    $(".operate").append($("#operateTable"));


    // $("#loadDataModal").modal("show");

    $("#loadDataContent").draggable({
        // 限制模态框只能在某范围内拖动
        containment: "#loadDataModal",
        // 标题文字部分禁止拖动
        cancel: "input,label,button,select"
    });

    $("#roadCigContent").draggable({
        // 限制模态框只能在某范围内拖动
        containment: "#roadConfigModal",
        // 只允许在标题部分拖动
        handle: "#roadConfigHeader",
        // 标题文字部分禁止拖动
        cancel: "label"
    });

    $("#buildingCigContent").draggable({
        containment: "#buildingConfigModal",
        handle: "#buildingConfigHeader",
        cancel: "label"
    });

    $("#barrierCigContent").draggable({
        containment: "#barrierConfigModal",
        handle: "#barrierConfigHeader",
        cancel: "label"
    });

    $("#beforeRunContent").draggable({
        containment: "#brforeRunModal",
        cancel: "input,label,span,button"
    });
});
