package cn.edu.njnu.geoproblemsolving.business.tool.landisII.biomass;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @ClassName LifeParameter
 * @Description Todo
 * @Author zhngzhng
 * @Date 2022/2/28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LifeParameter {
    // ecoregionName class1-5
    ArrayList<JSONObject> minRelativeBiomassVal;
    // shadeclass light0-5
    ArrayList<JSONObject> sufficientLightVal;

    //name leaf_longevity woody_decay_rate mortality growth leaf_lignin
    ArrayList<JSONObject> speciesParametersVal;
    //ecoregion aet dynamicInputFile
    ArrayList<JSONObject> ecoregionParametersVal;

    //severity wood_reduction litter_reduction
    ArrayList<JSONObject> fireReductionParametersVal;
    //name wood_reduction litter_reduction wood_removal leaf_removal
    ArrayList<JSONObject> harvestReductionParametersVal;

}
