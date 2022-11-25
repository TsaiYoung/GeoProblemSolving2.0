package cn.edu.njnu.geoproblemsolving.business.tool.landisII;

import cn.edu.njnu.geoproblemsolving.business.activity.docParse.DocParseServe;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.GeoAnalysisProcess;
import cn.edu.njnu.geoproblemsolving.business.activity.processDriven.service.NodeService;
import cn.edu.njnu.geoproblemsolving.business.resource.entity.ResourceEntity;
import cn.edu.njnu.geoproblemsolving.business.tool.FileUtil;
import cn.edu.njnu.geoproblemsolving.business.tool.landisII.biomass.LifeParameter;
import cn.edu.njnu.geoproblemsolving.business.tool.landisII.biomass.RequiredInput;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @ClassName LandisIIService
 * @Description 生成LandisII相关文件
 * @Author zhngzhng
 * @Date 2022/2/28
 **/
@Service
@Slf4j
public class LandisIIService {
    @Autowired
    DocParseServe docParseServe;

    @Autowired
    NodeService nodeService;

    @Autowired
    GeoAnalysisProcess geoAnalysisProcess;

    public File writeSpeciesFile(ArrayList<LandisSpecies> speciesInfo)  {
        if (speciesInfo == null || speciesInfo.isEmpty()) return null;
        String fileHeader = "LandisData  Species\n>>                      Sexual    Shade  Fire  Seed Disperal Dist  Vegetative   Sprout Age  Post-Fire\n>> Name      Longevity  Maturity  Tol.   Tol.  Effective  Maximum  Reprod Prob  Min   Max   Regen\n>> ----      ---------  --------  -----  ----  ---------  -------  -----------  ----------  --------\n";
        PrintStream printStream = null;
        try {
            File file = File.createTempFile("species_", ".txt");
            printStream = new PrintStream(new FileOutputStream(file));
            printStream.write(fileHeader.getBytes());
            for (Iterator<LandisSpecies> it = speciesInfo.iterator(); it.hasNext(); ) {
                LandisSpecies item = it.next();
                printStream.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", item.getName(), item.getLongevity(),
                        item.getMaturity(), item.getS_tolerance(),
                        item.getF_tolerance(), item.getE_seeding(), item.getM_seeding(), item.getReproduction(), item.getMin_resprouting(),
                        item.getMax_resprouting(), item.getPost_fire());
            }
            printStream.flush();
            printStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Failed to write Species file, error info: ", e.toString());
            return null;
        }
    }

    public File writeClimateConfig(LandisClimateConfig climateConfig) {
        if (climateConfig == null) return null;
        String fileHeader = "LandisData \"Climate Config\"\n\n";
        String fileFooter = ">> UsingClimateFire\tyes  << Optional parameter; default is no.";
        PrintStream printStream = null;
        try {
            File file = File.createTempFile("climateConfig_", ".txt");
            printStream = new PrintStream(new FileOutputStream(file));
            printStream.write(fileHeader.getBytes());
            String formatStr = "ClimateTimeSeries\t\t\t%s\nClimateFile\t\t\t\t%s\nClimateFileFormat\t\t\t%s\n\nSpinUpClimateTimeSeries\t\t\t%s\nSpinUpClimateFile\t\t\t%s\nSpinUpClimateFileFormat\t\t\t%s\n\n";
            printStream.printf(formatStr,
                    climateConfig.getClimateTimeSeries(), climateConfig.getClimateFile(), climateConfig.getClimateFileFormat(),
                    climateConfig.getSpinUpClimateTimeSeries(), climateConfig.getSpinUpClimateFile(), climateConfig.getSpinUpClimateFileFormat());
            printStream.write(fileFooter.getBytes());
            printStream.flush();
            printStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Failed to write climate config file, error info: ", e.toString());
            return null;
        }
    }

    public File writeDynamicData(ArrayList<LandisDynamicInput> dynamicInputList){
        if (dynamicInputList == null || dynamicInputList.isEmpty()) return null;
        String fileHeader = "LandisData  \"Dynamic Input Data\"\n\n" +
                ">> Year Ecoregion Species ProbEst MaxANPP MaxB\n" +
                ">> -------- -------------\n";
        PrintStream printStream = null;
        try {
            File file = File.createTempFile("dynamicData_", ".txt");
            printStream = new PrintStream(new FileOutputStream(file));
            printStream.write(fileHeader.getBytes());
            for (int i = 0; i < dynamicInputList.size(); i++) {
                LandisDynamicInput dynamicInput = dynamicInputList.get(i);
                printStream.printf("%s %s  %s       %s\t%s\t%s\n", dynamicInput.getYear(), dynamicInput.getEcoregion(), dynamicInput.getSpecies(),
                        dynamicInput.getProbEst(), dynamicInput.getMaxANPP(), dynamicInput.getMaxB());
            }
            printStream.flush();
            printStream.close();
            return file;
        }catch (Exception e){
            e.printStackTrace();
            log.warn("Failed to write dynamic input data file, error info: ", e.toString());
            return null;
        }
    }

    public File writeBiomassSuccession(LandisBiomassSuccession biomassSuccession) {
        if (biomassSuccession == null) return null;
        String fileHeader = "LandisData  \"Biomass Succession\"\n\n\n";
        PrintStream printStream = null;
        try {
            File file = File.createTempFile("biomassSuccession_", ".txt");
            printStream = new PrintStream(new FileOutputStream(file));
            printStream.write(fileHeader.getBytes());
            String partOne = ">>------------------\n>> REQUIRED INPUTS\n>>------------------\n\n";
            RequiredInput requiredInput = biomassSuccession.getRequiredInput();
            String requiredInputFormatStr = "Timestep  \t\t\t%s\n" +
                    "\n" +
                    "SeedingAlgorithm  \t\t%s\n" +
                    "\n" +
                    "InitialCommunities      \t%s\n" +
                    "InitialCommunitiesMap   \t%s\n" +
                    "\n" +
                    "ClimateConfigFile\t\t%s\n" +
                    "\n" +
                    ">> CalibrateMode \t\t%s\n" +
                    "\n" +
                    ">> SpinUpMortalityFraction \t%s\n\n\n\n";
            printStream.write(partOne.getBytes());
            printStream.printf(requiredInputFormatStr, requiredInput.getTimeStep(), requiredInput.getSeedingAlgorithm(), requiredInput.getInitialCommunities(),
                    requiredInput.getInitialCommunitiesMap(), requiredInput.getClimateConfigFile(),
                    requiredInput.getCalibrateMode(), requiredInput.getSpinUpMortalityFraction());

            String partTwo = ">>----------------------------\n>> LIFE HISTORY PARAMETERS\n>>----------------------------\n\n" +
                    "MinRelativeBiomass\n>> Shade\tPercent Max Biomass\n>> Class\tby Ecoregions\n>> ----------\t--------------------\n";
            //
            LifeParameter lifeParameter = biomassSuccession.getLifeParameter();
            ArrayList<JSONObject> minRelativeBiomassVal = lifeParameter.getMinRelativeBiomassVal();
            String nameStr = "\t";
            String class1Str = "\t1";
            String class2Str = "\t2";
            String class3Str = "\t3";
            String class4Str = "\t4";
            String class5Str = "\t5";
            for (int i = 0; i < minRelativeBiomassVal.size(); i++) {
                JSONObject item = minRelativeBiomassVal.get(i);
                nameStr += "\t" + item.getString("ecoregionName");
                class1Str += "\t" + item.getString("class1");
                class2Str += "\t" + item.getString("class2");
                class3Str += "\t" + item.getString("class3");
                class4Str += "\t" + item.getString("class4");
                class5Str += "\t" + item.getString("class5");
                if (i == minRelativeBiomassVal.size() - 1) {
                    nameStr += "\n";
                    class1Str += "\n";
                    class2Str += "\n";
                    class3Str += "\n";
                    class4Str += "\n";
                    class5Str += "\n\n\n";
                }
            }
            printStream.write(partTwo.getBytes());
            printStream.write(nameStr.getBytes());
            printStream.write(class1Str.getBytes());
            printStream.write(class2Str.getBytes());
            printStream.write(class3Str.getBytes());
            printStream.write(class4Str.getBytes());
            printStream.write(class5Str.getBytes());

            String slHeader = "SufficientLight\n>> Spp Shade\tProbability\n>> Class\tby Actual Shade\n>> ----------\t--------------------\t\n>>\t\t0\t1\t2\t3\t4\t5\n";
            ArrayList<JSONObject> sufficientLightVal = lifeParameter.getSufficientLightVal();
            printStream.write(slHeader.getBytes());
            for (int i = 0; i < sufficientLightVal.size(); i++) {
                JSONObject item = sufficientLightVal.get(i);
                printStream.printf("\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", item.getString("shadeclass"), item.getString("light0"), item.getString("light1"),
                        item.getString("light2"), item.getString("light3"), item.getString("light4"), item.getString("light5"));
            }

            String spHeader = "\n\nSpeciesParameters\n\n" +
                    ">> Species   Leaf\tWoodyBiomass\tMortality\tGrowth\tLeaf\t\n" +
                    ">>\t     Longevity\tDecayRate\tShapeParam\tShape\tLignin%\t\n" +
                    ">> --------  ---------\t---------\t----------\t------\t----    \n" +
                    ">>\n";
            printStream.write(spHeader.getBytes());
            ArrayList<JSONObject> speciesParametersVal = lifeParameter.getSpeciesParametersVal();
            //name leaf_longevity woody_decay_rate mortality growth leaf_lignin
            for (int i = 0; i < speciesParametersVal.size(); i++) {
                JSONObject item = speciesParametersVal.get(i);
                printStream.printf("%s\t%s\t%s\t\t%s\t\t%s\t%s\t\n", item.getString("name"), item.getString("leaf_longevity"),
                        item.getString("woody_decay_rate"), item.getString("mortality"),
                        item.getString("growth"), item.getString("leaf_lignin"));
            }

            String epHeader = "\n\n\nEcoregionParameters\n>>\tAET (mm)\n";
            printStream.write(epHeader.getBytes());
            ArrayList<JSONObject> ecoregionParametersVal = lifeParameter.getEcoregionParametersVal();
            for (int i = 0; i < ecoregionParametersVal.size(); i++) {
                JSONObject item = ecoregionParametersVal.get(i);
                //ecoregion aet
                printStream.printf("%s\t%s\n", item.getString("ecoregion"), item.getString("aet"));
                if (i == ecoregionParametersVal.size() - 1) {
                    printStream.printf("\n\nDynamicInputFile\t\t\t%s\n\n", item.getString("dynamicInputFile"));
                }
            }


            String frpHeader = "FireReductionParameters\n" +
                    ">>\tSeverity\tWoodLitter\tLitter\t\n" +
                    ">>\tFire\t\tReduct\t\tReduct\n";
            printStream.write(frpHeader.getBytes());
            ArrayList<JSONObject> fireReductionParametersVal = lifeParameter.getFireReductionParametersVal();
            for (int i = 0; i < fireReductionParametersVal.size(); i++) {
                JSONObject item = fireReductionParametersVal.get(i);
                //severity wood_reduction litter_reduction
                printStream.printf("\t%s\t\t%s\t\t%s\n", item.getString("severity"),
                        item.getString("wood_reduction"), item.getString("litter_reduction"));
            }

            String hrpHeader = "\nHarvestReductionParameters\n" +
                    ">>\tName\t\tWoodLitter\tLitter\tCohort\t\tCohort\n" +
                    ">>\t\t\tReduct\t\tReduct\tWoodRemoval\tLeafRemoval\n";
            printStream.write(hrpHeader.getBytes());
            ArrayList<JSONObject> harvestReductionParametersVal = lifeParameter.getHarvestReductionParametersVal();
            for (int i = 0; i < harvestReductionParametersVal.size(); i++) {
                JSONObject item = harvestReductionParametersVal.get(i);
                //name wood_reduction litter_reduction wood_removal leaf_removal
                printStream.printf("\t%s\t%s\t\t%s\t%s\t\t%s\n", item.getString("name"), item.getString("wood_reduction"),
                        item.getString("litter_reduction"), item.getString("wood_removal"), item.getString("leaf_removal"));
            }
            printStream.flush();
            printStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Failed to write succession file, error info: ", e.toString());
            return null;
        }
    }

    //写入文档、写入节点、自动更新
    public JSONObject docNodeAndFlow(String aid, String toolId, String graphId,ResourceEntity res, JSONArray participants){
        HashSet<String> userIds = new HashSet<>();
        for (Object item : participants) {
            JSONObject userInfo = JSONObject.parseObject(JSONObject.toJSONString(item));
            userIds.add(userInfo.getString("userId"));
        }
        try {
            String oid = docParseServe.geoAnalysis(aid, toolId, null, res, userIds);
            // 更新节点
            nodeService.addResToNode(aid, res.getUid());
            if (graphId != null && graphId != "") {
                geoAnalysisProcess.batchResFlowAutoUpdate(graphId, aid, res.getUid());
            }
            JSONObject respJson = new JSONObject();
            respJson.put("path", res.getAddress());
            respJson.put("operationId", oid);
            return respJson;
        }catch (Exception e){
            log.error("Failed to operation file, ", e.toString());
            return null;
        }

    }

}
