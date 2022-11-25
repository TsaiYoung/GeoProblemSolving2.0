package cn.edu.njnu.geoproblemsolving.business.tool.trafficNoise.Data.Dao;

import com.alibaba.fastjson.JSONObject;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;
import org.gdal.osr.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.text.DecimalFormat;

public class prepareData {
    /*
    @function:为对应shp文件生成cgp文件，保证中文字段的正常显示
    @filePath:包含拓展名的文件绝对路径
     */
    public static void genCpgFile(String filePath) throws IOException {
        try {
            File file = new File(filePath);
            file.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(file);
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print("UTF-8");
            outputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
    @function:玄武盾会拦截对dbf的请求，将其复制为mdbf
    @fileName:不包含拓展名的文件绝对路径
    */
    public static void copyDbfFile(String fileName) throws IOException {
        try {
            Path sourcePath = Paths.get(fileName + ".dbf");
            Path destPath = Paths.get(fileName + ".mdbf");
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void copyMdbfFile(String fileName) throws IOException {
        try {
            Path sourcePath = Paths.get(fileName + ".mdbf");
            Path destPath = Paths.get(fileName + ".dbf");
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static JSONObject prepareRoadData(String roadFilePath) throws IOException {
        genCpgFile(roadFilePath + ".cpg");
        JSONObject resultJSON = new JSONObject();
        double[] resultArr = new double[]{0.0, 0.0};
        ogr.RegisterAll();
//        为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
//		为了是属性表字段支持中文，请添加下面这句代码
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");
        gdal.SetConfigOption("SHAPE_RESTORE_SHX", "YES");
        Driver driver = ogr.GetDriverByName("ESRI shapefile");
        if (driver == null)
            return resultJSON;
        DataSource dSource = driver.Open(roadFilePath + ".shp", 1);
        if (dSource == null)
            return resultJSON;

        Layer layer = dSource.GetLayerByIndex(0);
        if (layer == null)
            return resultJSON;
//        获得投影坐标
        SpatialReference projection = layer.GetSpatialRef();
//        获得地理坐标
        SpatialReference latlng = projection.CloneGeogCS();
//        新建投影坐标转地理坐标的坐标转换方法
        CoordinateTransformation XY2Latlng = new CoordinateTransformation(projection, latlng);

//        获得图层的范围，是投影坐标，需要转化为经纬度
        double[] prjExtent = layer.GetExtent();

        double[] latlngCenter = XY2Latlng.TransformPoint((prjExtent[0] + prjExtent[1]) / 2, (prjExtent[2] + prjExtent[3]) / 2);
        resultJSON.put("Lng", latlngCenter[0]);
        resultJSON.put("Lat", latlngCenter[1]);

//        如果之前文件没有相应字段则创建
//        has变量是用来标记字段是否为新创建的，若has为0，则说明是新创建字段，需要初始化值
        int hasWidth, hasM, hasP, hasCS, hasTS, hasRef, hasRH, hasRW, hasRA, hasSlope, hasSurface, hasID, hasLength;
        hasWidth = hasM = hasP = hasCS = hasTS = hasRef = hasRH = hasRW = hasRA = hasSlope = hasSurface = hasID = hasLength = 1;

//        OBJECTID
        if (layer.FindFieldIndex("OBJECTID", 1) == -1) {
            hasID = 0;
            FieldDefn OBJECTID = new FieldDefn("OBJECTID", ogr.OFTInteger);
            layer.CreateField(OBJECTID, 1);
        }
//        道路宽度Width
        if (layer.FindFieldIndex("Width", 1) == -1) {
            hasWidth = 0;
            FieldDefn width = new FieldDefn("Width", ogr.OFTReal);
            width.SetPrecision(10);
            layer.CreateField(width, 1);
        }
//        道路车流量M
        if (layer.FindFieldIndex("M", 1) == -1) {
            hasM = 0;
            FieldDefn M = new FieldDefn("M", ogr.OFTReal);
            M.SetPrecision(10);
            layer.CreateField(M, 1);
        }
//        道路重车所占比P
        if (layer.FindFieldIndex("P", 1) == -1) {
            hasP = 0;
            FieldDefn P = new FieldDefn("P", ogr.OFTReal);
            P.SetPrecision(10);
            layer.CreateField(P, 1);
        }
//        道路轻型车限速
        if (layer.FindFieldIndex("CarSpeed", 1) == -1) {
            hasCS = 0;
            FieldDefn CarSpeed = new FieldDefn("CarSpeed", ogr.OFTReal);
            CarSpeed.SetPrecision(10);
            layer.CreateField(CarSpeed, 1);
        }
//        道路重型车限速
        if (layer.FindFieldIndex("TruckSpeed", 1) == -1) {
            hasTS = 0;
            FieldDefn TruckSpeed = new FieldDefn("TruckSpeed", ogr.OFTReal);
            TruckSpeed.SetPrecision(10);
            layer.CreateField(TruckSpeed, 1);
        }
//        道路两侧是否有反射面
        if (layer.FindFieldIndex("HasReflect", 1) == -1) {
            hasRef = 0;
            FieldDefn HasReflect = new FieldDefn("HasReflect", ogr.OFTInteger);
            layer.CreateField(HasReflect, 1);
        }
//        道路两侧反射面的平均高度
        if (layer.FindFieldIndex("RefHeight", 1) == -1) {
            hasRH = 0;
            FieldDefn ReflectHeight = new FieldDefn("RefHeight", ogr.OFTReal);
            ReflectHeight.SetPrecision(10);
            layer.CreateField(ReflectHeight, 1);
        }
//        道路两侧反射面之间的平均宽度
        if (layer.FindFieldIndex("RefWidth", 1) == -1) {
            hasRW = 0;
            FieldDefn ReflectWidth = new FieldDefn("RefWidth", ogr.OFTReal);
            ReflectWidth.SetPrecision(10);
            layer.CreateField(ReflectWidth, 1);
        }
//        道路两侧反射面的材质是否是吸音材料
        if (layer.FindFieldIndex("RefAbsorb", 1) == -1) {
            hasRA = 0;
            FieldDefn ReflectAbsorb = new FieldDefn("RefAbsorb", ogr.OFTInteger);
            layer.CreateField(ReflectAbsorb, 1);
        }
//        道路坡度
        if (layer.FindFieldIndex("Slope", 1) == -1) {
            hasSlope = 0;
            FieldDefn Slope = new FieldDefn("Slope", ogr.OFTReal);
            Slope.SetPrecision(10);
            layer.CreateField(Slope, 1);
        }
//        道路表面材质
        if (layer.FindFieldIndex("Surface", 1) == -1) {
            hasSurface = 0;
            FieldDefn Surface = new FieldDefn("Surface", ogr.OFTInteger);
            layer.CreateField(Surface, 1);
        }
//        道路长度
        if (layer.FindFieldIndex("Length", 1) == -1) {
            hasLength = 0;
            FieldDefn Length = new FieldDefn("Length", ogr.OFTReal);
            Length.SetPrecision(10);
            layer.CreateField(Length, 1);
        }

        Feature feature = layer.GetNextFeature();
        int count = 0;
        int maxID = 0;
        while (feature != null) {
//            如果字段是刚添加或字段值还未设置，则初始化这个值
            if (hasWidth == 0 || !feature.IsFieldSet("Width")) {
                feature.SetField("Width", 10.0);
            }
            if (hasM == 0 || !feature.IsFieldSet("M")) {
                feature.SetField("M", 1000.0);
            }
            if (hasP == 0 || !feature.IsFieldSet("P")) {
                feature.SetField("P", 10.0);
            }
            if (hasCS == 0 || !feature.IsFieldSet("CarSpeed")) {
                feature.SetField("CarSpeed", 80.0);
            }
            if (hasTS == 0 || !feature.IsFieldSet("TruckSpeed")) {
                feature.SetField("TruckSpeed", 40.0);
            }
            if (hasRef == 0 || !feature.IsFieldSet("HasReflect")) {
                feature.SetField("HasReflect", 1);
            }
            if (hasRH == 0 || !feature.IsFieldSet("RefHeight")) {
                feature.SetField("RefHeight", 30.0);
            }
            if (hasRW == 0 || !feature.IsFieldSet("RefWidth")) {
                feature.SetField("RefWidth", 40.0);
            }
            if (hasRA == 0 || !feature.IsFieldSet("RefAbsorb")) {
                feature.SetField("RefAbsorb", 0);
            }
            if (hasSlope == 0 || !feature.IsFieldSet("Slope")) {
                feature.SetField("Slope", 0.0);
            }
            if (hasSurface == 0 || !feature.IsFieldSet("Surface")) {
                feature.SetField("Surface", 0);
            }
            if (hasLength == 0 || !feature.IsFieldSet("Length")) {
                DecimalFormat df = new DecimalFormat("#.000");
                double length = Double.parseDouble(df.format(feature.GetGeometryRef().Length()));
                feature.SetField("Length", length);
            }

            if (hasID == 0 || !feature.IsFieldSet("OBJECTID")) {
                feature.SetField("OBJECTID", count);
            } else {
                int featureID = feature.GetFieldAsInteger("OBJECTID");
                maxID = featureID > maxID ? featureID : maxID;
            }
            count++;

            layer.SetFeature(feature);
            feature = layer.GetNextFeature();
        }

        layer.SyncToDisk();
        layer.delete();
        dSource.SyncToDisk();
        dSource.delete();

        resultJSON.put("maxID", maxID > count ? maxID : count);
        return resultJSON;
    }

    public static JSONObject prepareBuildingData(String buildingFilePath) throws IOException {
        JSONObject resultJSON = new JSONObject();
        try {
            System.out.println("start read shp");
            ogr.RegisterAll();
//        为了支持中文路径，请添加下面这句代码
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
//		为了是属性表字段支持中文，请添加下面这句代码
            gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
            gdal.SetConfigOption("SHAPE_RESTORE_SHX", "YES");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        Driver driver = ogr.GetDriverByName("ESRI shapefile");
        if (driver == null)
            return resultJSON;
        DataSource dSource = driver.Open(buildingFilePath + ".shp", 1);
        if (dSource == null)
            return resultJSON;
        Layer layer = dSource.GetLayerByIndex(0);
        if (layer == null)
            return resultJSON;

//        获得投影坐标
        SpatialReference projection = layer.GetSpatialRef();
//        获得地理坐标
        SpatialReference latlng = projection.CloneGeogCS();
//        新建投影坐标转地理坐标的坐标转换方法
        CoordinateTransformation XY2Latlng = new CoordinateTransformation(projection, latlng);

//        获得图层的范围，是投影坐标，需要转化为经纬度
        double[] prjExtent = layer.GetExtent();

        double[] latlngCenter = XY2Latlng.TransformPoint((prjExtent[0] + prjExtent[1]) / 2, (prjExtent[2] + prjExtent[3]) / 2);
        resultJSON.put("Lng", latlngCenter[0]);
        resultJSON.put("Lat", latlngCenter[1]);

//        如果之前文件没有相应字段则创建
        int hasHeight, hasStorey, hasID;
        hasHeight = hasID = hasStorey = 1;
//        OBJECTID
        if (layer.FindFieldIndex("OBJECTID", 1) == -1) {
            hasID = 0;
            FieldDefn OBJECTID = new FieldDefn("OBJECTID", ogr.OFTInteger);
            layer.CreateField(OBJECTID, 1);
        }
//        建筑物高度
        if (layer.FindFieldIndex("Height", 1) == -1) {
            hasHeight = 0;
            FieldDefn height = new FieldDefn("Height", ogr.OFTReal);
            height.SetPrecision(10);
            layer.CreateField(height, 1);
        }

//        建筑物楼层数
        if (layer.FindFieldIndex("Storey", 1) == -1) {
            hasHeight = 0;
            FieldDefn storey = new FieldDefn("Storey", ogr.OFTInteger);
            layer.CreateField(storey, 1);
        }

        Feature feature = layer.GetNextFeature();
        int count = 0;
//        获取ID的最大值
        int maxID = 0;
        while (feature != null) {
//            如果字段是刚添加或字段值还未设置，则初始化这个值
            if (hasID == 0 || !feature.IsFieldSet("OBJECTID")) {
                feature.SetField("OBJECTID", count);
            } else {
                int featureID = feature.GetFieldAsInteger("OBJECTID");
                maxID = featureID > maxID ? featureID : maxID;
            }

            if (hasHeight == 0 || !feature.IsFieldSet("Height")) {
                feature.SetField("Height", 30.0);
            }

            if (hasStorey == 0 || !feature.IsFieldSet("Storey")) {
                feature.SetField("Storey", 10);
            }
            count++;
            layer.SetFeature(feature);
            feature = layer.GetNextFeature();
        }

        layer.SyncToDisk();
        layer.delete();
        dSource.SyncToDisk();
        dSource.delete();

        resultJSON.put("maxID", maxID > count ? maxID : count);
        return resultJSON;
    }

    public static JSONObject prepareBarrierData(String barrierFilePath) throws IOException {
        JSONObject resultJSON = new JSONObject();
        double[] resultArr = new double[]{0.0, 0.0};
        ogr.RegisterAll();
//        为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
//		为了是属性表字段支持中文，请添加下面这句代码
        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
        gdal.SetConfigOption("SHAPE_RESTORE_SHX", "YES");
        Driver driver = ogr.GetDriverByName("ESRI shapefile");
        if (driver == null)
            return resultJSON;
        DataSource dSource = driver.Open(barrierFilePath + ".shp", 1);
        if (dSource == null)
            return resultJSON;
        Layer layer = dSource.GetLayerByIndex(0);
        if (layer == null)
            return resultJSON;

//        获得投影坐标
        SpatialReference projection = layer.GetSpatialRef();
//        获得地理坐标
        SpatialReference latlng = projection.CloneGeogCS();
//        新建投影坐标转地理坐标的坐标转换方法
        CoordinateTransformation XY2Latlng = new CoordinateTransformation(projection, latlng);

//        获得图层的范围，是投影坐标，需要转化为经纬度
        double[] prjExtent = layer.GetExtent();

        double[] latlngCenter = XY2Latlng.TransformPoint((prjExtent[0] + prjExtent[1]) / 2, (prjExtent[2] + prjExtent[3]) / 2);
        resultJSON.put("Lng", latlngCenter[0]);
        resultJSON.put("Lat", latlngCenter[1]);

//        如果之前文件没有相应字段则创建
        int hasLength, hasHeight, hasID;
        hasHeight = hasLength = hasID = 1;

//        OBJECTID
        if (layer.FindFieldIndex("OBJECTID", 1) == -1) {
            hasID = 0;
            FieldDefn OBJECTID = new FieldDefn("OBJECTID", ogr.OFTInteger);
            layer.CreateField(OBJECTID, 1);
        }
//        屏障长度
        if (layer.FindFieldIndex("Length", 1) == -1) {
            hasLength = 0;
            FieldDefn length = new FieldDefn("Length", ogr.OFTReal);
            length.SetPrecision(10);
            layer.CreateField(length, 1);
        }
//        屏障高度
        if (layer.FindFieldIndex("Height", 1) == -1) {
            hasHeight = 0;
            FieldDefn height = new FieldDefn("Height", ogr.OFTReal);
            height.SetPrecision(10);
            layer.CreateField(height, 1);
        }

        Feature feature = layer.GetNextFeature();
        int count = 0;
//        获取ID的最大值
        int maxID = 0;
        while (feature != null) {
//            如果字段是刚添加或字段值还未设置，则初始化这个值
            if (hasHeight == 0 || !feature.IsFieldSet("Height")) {
                feature.SetField("Height", 2.5);
            }
            if (hasLength == 0 || !feature.IsFieldSet("Length")) {
                DecimalFormat df = new DecimalFormat("#.000");
                double length = Double.parseDouble(df.format(feature.GetGeometryRef().Length()));
                feature.SetField("Length", length);
            }
            if (hasID == 0 || !feature.IsFieldSet("OBJECTID")) {
                feature.SetField("OBJECTID", count);
            } else {
                int featureID = feature.GetFieldAsInteger("OBJECTID");
                maxID = featureID > maxID ? featureID : maxID;
            }
            count++;
            layer.SetFeature(feature);
            feature = layer.GetNextFeature();
        }


        layer.SyncToDisk();
        layer.delete();
        dSource.SyncToDisk();
        dSource.delete();

        resultJSON.put("maxID", maxID > count ? maxID : count);
        return resultJSON;
    }

    public static JSONObject prepareResultData(String resultFilePath) throws IOException {
        JSONObject resultJSON = new JSONObject();
        double[] resultArr = new double[]{0.0, 0.0};
        ogr.RegisterAll();
//        为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
//		为了是属性表字段支持中文，请添加下面这句代码
        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
        gdal.SetConfigOption("SHAPE_RESTORE_SHX", "YES");
        Driver driver = ogr.GetDriverByName("ESRI shapefile");
        if (driver == null)
            return resultJSON;
        DataSource dSource = driver.Open(resultFilePath + ".shp", 1);
        if (dSource == null)
            return resultJSON;
        Layer layer = dSource.GetLayerByIndex(0);
        if (layer == null)
            return resultJSON;

//        获得投影坐标
        SpatialReference projection = layer.GetSpatialRef();
//        获得地理坐标
        SpatialReference latlng = projection.CloneGeogCS();
//        新建投影坐标转地理坐标的坐标转换方法
        CoordinateTransformation XY2Latlng = new CoordinateTransformation(projection, latlng);

//        获得图层的范围，是投影坐标，需要转化为经纬度
        double[] prjExtent = layer.GetExtent();

        double[] latlngCenter = XY2Latlng.TransformPoint((prjExtent[0] + prjExtent[1]) / 2, (prjExtent[2] + prjExtent[3]) / 2);
        resultJSON.put("Lng", latlngCenter[0]);
        resultJSON.put("Lat", latlngCenter[1]);

//        如果之前文件没有相应字段则创建
        int hasLength, hasHeight, hasID;
        hasHeight = hasLength = hasID = 1;

//        OBJECTID
        if (layer.FindFieldIndex("OBJECTID", 1) == -1) {
            hasID = 0;
            FieldDefn OBJECTID = new FieldDefn("OBJECTID", ogr.OFTInteger);
            layer.CreateField(OBJECTID, 1);
        }
//        屏障长度
        if (layer.FindFieldIndex("Length", 1) == -1) {
            hasLength = 0;
            FieldDefn length = new FieldDefn("Length", ogr.OFTReal);
            length.SetPrecision(10);
            layer.CreateField(length, 1);
        }
//        屏障高度
        if (layer.FindFieldIndex("Height", 1) == -1) {
            hasHeight = 0;
            FieldDefn height = new FieldDefn("Height", ogr.OFTReal);
            height.SetPrecision(10);
            layer.CreateField(height, 1);
        }

        Feature feature = layer.GetNextFeature();
        int count = 0;
//        获取ID的最大值
        int maxID = 0;
        while (feature != null) {
//            如果字段是刚添加或字段值还未设置，则初始化这个值
            if (hasHeight == 0 || !feature.IsFieldSet("Height")) {
                feature.SetField("Height", 2.5);
            }
            if (hasLength == 0 || !feature.IsFieldSet("Length")) {
                DecimalFormat df = new DecimalFormat("#.000");
                double length = Double.parseDouble(df.format(feature.GetGeometryRef().Length()));
                feature.SetField("Length", length);
            }
            if (hasID == 0 || !feature.IsFieldSet("OBJECTID")) {
                feature.SetField("OBJECTID", count);
            } else {
                int featureID = feature.GetFieldAsInteger("OBJECTID");
                maxID = featureID > maxID ? featureID : maxID;
            }
            count++;
            layer.SetFeature(feature);
            feature = layer.GetNextFeature();
        }


        layer.SyncToDisk();
        layer.delete();
        dSource.SyncToDisk();
        dSource.delete();

        resultJSON.put("maxID", maxID > count ? maxID : count);
        return resultJSON;
    }
}
