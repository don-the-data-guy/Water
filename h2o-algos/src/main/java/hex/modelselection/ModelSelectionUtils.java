package hex.modelselection;

import hex.DataInfo;
import hex.Model;
import hex.glm.GLM;
import hex.glm.GLMModel;
import water.DKV;
import water.Key;
import water.fvec.Frame;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static hex.genmodel.utils.MathUtils.combinatorial;

public class ModelSelectionUtils {
    public static Frame[] generateTrainingFrames(ModelSelectionModel.ModelSelectionParameters parms, int predNum, String[] predNames,
                                                 int numModels, String foldColumn) {
        int maxPredNum = predNames.length;
        Frame[] trainFrames = new Frame[numModels];
        int[] predIndices = IntStream.range(0, predNum).toArray();   // contains indices to predictor names
        int zeroBound = maxPredNum-predNum;
        int[] bounds = IntStream.range(zeroBound, maxPredNum).toArray();   // highest combo value
        for (int frameCount = 0; frameCount < numModels; frameCount++) {    // generate one combo
            trainFrames[frameCount] = generateOneFrame(predIndices, parms, predNames, foldColumn);
            DKV.put(trainFrames[frameCount]);
            updatePredIndices(predIndices, bounds);
        }
        return trainFrames;
    }

    /***
     * Given predictor indices stored in currentPredIndices, we need to find the next combination of predictor indices
     * to use to generate the next combination.  For example, if we have 4 predictors and we are looking to take two 
     * predictors, predictor indices can change in the following sequence [0,1]->[0,2]->[0,3]->[1,2]->[1,2]->[2,3]. 
     *
     * @param currentPredIndices
     * @param indicesBounds
     */
    public static void updatePredIndices(int[] currentPredIndices, int[] indicesBounds) {
        int lastPredInd = currentPredIndices.length-1;
        for (int index = lastPredInd; index >= 0; index--) {
            if (currentPredIndices[index] < indicesBounds[index]) { // increase LSB first
                currentPredIndices[index]++;
                updateLaterIndices(currentPredIndices, index, lastPredInd);
                break;
            } 
        }
    }

    /***
     * Give 5 predictors and say we want the combo of 3 predictors, this function will properly reset the prediction
     * combination indices say from [0, 1, 4] -> [0, 2, 3] or [0, 3, 4] -> [1, 2, 3].  Given an index that was just
     * updated, it will update the indices that come later in the list correctly.
     * 
     * @param currentPredIndices
     * @param indexUpdated
     * @param lastPredInd
     */
    public static void updateLaterIndices(int[] currentPredIndices, int indexUpdated, int lastPredInd) {
        for (int index = indexUpdated; index < lastPredInd; index++) {
            currentPredIndices[index+1] = currentPredIndices[index]+1;
        }
    }
    
    /***
     *     Given a predictor indices set, this function will generate a training frame containing the predictors with
     *     indices in predIndices.
     *     
     * @param predIndices
     * @param parms
     * @param predNames
     * @return
     */
    public static Frame generateOneFrame(int[] predIndices, ModelSelectionModel.ModelSelectionParameters parms, String[] predNames,
                                         String foldColumn) {
        final Frame predVecs = new Frame(Key.make());
        final Frame train = parms.train();
        int numPreds = predIndices.length;
        for (int index = 0; index < numPreds; index++) {
            int predVecNum = predIndices[index];
            predVecs.add(predNames[predVecNum], train.vec(predNames[predVecNum]));
        }
        if (parms._weights_column != null)
            predVecs.add(parms._weights_column, train.vec(parms._weights_column));
        if (parms._offset_column != null)
            predVecs.add(parms._offset_column, train.vec(parms._offset_column));
        if (foldColumn != null)
            predVecs.add(foldColumn, train.vec(foldColumn));
        predVecs.add(parms._response_column, train.vec(parms._response_column));
        return predVecs;
    }

    public static BitSet setBitSet(int[] currIndices, int totalPredSize) {
        BitSet predSet = new BitSet(totalPredSize);
        setBitSet(predSet, currIndices);
        return predSet;
    }
    
    public static void setBitSet(BitSet predBitSet, int[] currIndices) {
        for (int predIndex : currIndices)
            predBitSet.set(predIndex);
    }
    
    /**
     * Give a predictor subset with indices stored in currSubsetIndices, an array of training frames are generated by 
     * adding one predictor from predictorNames with predictors not already included in currSubsetIndices.  
     * 
     * @param parms
     * @param predictorNames
     * @param foldColumn
     * @param currSubsetIndices
     * @param validSubsets Lists containing only valid predictor indices to choose from
     * @return
     */
    public static Frame[] generateMaxRTrainingFrames(ModelSelectionModel.ModelSelectionParameters parms,
                                                     String[] predictorNames, String foldColumn,
                                                     List<Integer> currSubsetIndices, int newPredPos,
                                                     List<Integer> validSubsets, Set<BitSet> usedCombo) {
        List<Frame> trainFramesList = new ArrayList<>();
        List<Integer> changedSubset = new ArrayList<>(currSubsetIndices);
        changedSubset.add(newPredPos, -1);  // value irrelevant
        int[] predIndices = changedSubset.stream().mapToInt(Integer::intValue).toArray();
        int predNum = predictorNames.length;
        BitSet tempIndices =  new BitSet(predNum);
        for (int predIndex : validSubsets) {  // consider valid predictor indices only
            predIndices[newPredPos] = predIndex;
            if (usedCombo != null) {   // only need to check for replacement step 
                tempIndices.clear();
                setBitSet(tempIndices, predIndices);
                if (!usedCombo.contains(tempIndices)) {
                    usedCombo.add((BitSet) tempIndices.clone());
                    Frame trainFrame = generateOneFrame(predIndices, parms, predictorNames, foldColumn);
                    DKV.put(trainFrame);
                    trainFramesList.add(trainFrame);
                }
            } else {
                Frame trainFrame = generateOneFrame(predIndices, parms, predictorNames, foldColumn);
                DKV.put(trainFrame);
                trainFramesList.add(trainFrame);
            }
        }
        return trainFramesList.stream().toArray(Frame[]::new);
    }
    
    public static String[][] shrinkStringArray(String[][] array, int numModels) {
        int arrLen = array.length-1;
        int offset = numModels-1;
        String[][] newArray =new String[numModels][];
        for (int index=0; index < numModels; index++)
            newArray[offset-index] = array[arrLen-index].clone();
        return newArray;
    }
    
    public static double[][] shrinkDoubleArray(double[][] array, int numModels) {
        int arrLen = array.length-1;
        int offset = numModels-1;
        double[][] newArray =new double[numModels][];
        for (int index=0; index < numModels; index++)
            newArray[offset-index] = array[arrLen-index].clone();
        return newArray;
    }

    public static Key[] shrinkKeyArray(Key[] array, int numModels) {
        int arrLen = array.length;
        Key[] newArray = new Key[numModels];
        System.arraycopy(array, (arrLen-numModels), newArray, 0, numModels);
        return newArray;
    }
    
    public static String joinDouble(double[] val) {
        int arrLen = val.length; // skip the intercept terms
        String[] strVal = new String[arrLen];
        for (int index=0; index < arrLen; index++)
            strVal[index] = Double.toString(val[index]);
        return String.join(", ", strVal);
    }
    /**
     * Given an array GLMModel built, find the one with the highest R2 value that exceeds lastBestR2.  If found, return
     * the index where the best model is.  Else return -1
     * 
     * @param lastBestR2
     * @param bestR2Models
     * @return
     */
    public static int findBestR2Model(double lastBestR2, GLMModel[] bestR2Models) {
        int numModel = bestR2Models.length;
        int bestIndex = 0;
        double currBestR2 = lastBestR2;
        for (int index=0; index < numModel; index++) {
            if (bestR2Models[index] != null) {
                double bestR2 = bestR2Models[index].r2();
                if (bestR2 > currBestR2) {
                    bestR2Models[bestIndex].delete();
                    bestIndex = index;
                    currBestR2 = bestR2;
                } else {
                    bestR2Models[index].delete();
                }
            }
        }
        return currBestR2 > lastBestR2 ? bestIndex : -1;
    }
    
    public static GLMModel.GLMParameters[] generateGLMParameters(Frame[] trainingFrames,
                                                                 ModelSelectionModel.ModelSelectionParameters parms, 
                                                                 int nfolds, String foldColumn,
                                                                 Model.Parameters.FoldAssignmentScheme foldAssignment) {
        final int numModels = trainingFrames.length;
        GLMModel.GLMParameters[] params = new GLMModel.GLMParameters[numModels];
        final Field[] field1 = ModelSelectionModel.ModelSelectionParameters.class.getDeclaredFields();
        final Field[] field2 = Model.Parameters.class.getDeclaredFields();
        for (int index = 0; index < numModels; index++) {
            params[index] = new GLMModel.GLMParameters();
            setParamField(parms, params[index], false, field1, Collections.emptyList());
            setParamField(parms, params[index], true, field2, Collections.emptyList());
            params[index]._train = trainingFrames[index]._key;
            params[index]._nfolds = nfolds;
            params[index]._fold_column = foldColumn;
            params[index]._fold_assignment = foldAssignment;
            params[index]._just_r2 = true;
            params[index]._auto_rebalance = false;
        }
        return params;
    }
    
    public static void setParamField(Model.Parameters params, GLMModel.GLMParameters glmParam, boolean superClassParams,
                                     Field[] paramFields, List<String> excludeList) {
        // assign relevant GAMParameter fields to GLMParameter fields
        Field glmField;
        boolean emptyExcludeList = excludeList.size() == 0;
        for (Field oneField : paramFields) {
            try {
                if (emptyExcludeList || !excludeList.contains(oneField.getName())) {
                    if (superClassParams)
                        glmField = glmParam.getClass().getSuperclass().getDeclaredField(oneField.getName());
                    else
                        glmField = glmParam.getClass().getDeclaredField(oneField.getName());
                    glmField.set(glmParam, oneField.get(params));
                }
            } catch (IllegalAccessException|NoSuchFieldException e) { // suppress error printing, only cares about fields that are accessible
                ;
            }
        }    
    }
    
    public static GLM[] buildGLMBuilders(GLMModel.GLMParameters[] trainingParams) {
        int numModels = trainingParams.length;
        GLM[] builders = new GLM[numModels];
        for (int index=0; index<numModels; index++)
            builders[index] = new GLM(trainingParams[index]);
        return builders;
    }
    
    public static void removeTrainingFrames(Frame[] trainingFrames) {
        for (Frame oneFrame : trainingFrames) 
            DKV.remove(oneFrame._key);
    }

    /**
     * Given GLM run results of a fixed number of predictors, find the model with the best R2 value.
     *
     * @param glmResults
     */
    public static GLMModel findBestModel(GLM[] glmResults) {
        double bestR2Val = 0;
        int numModels = glmResults.length;
        GLMModel bestModel = null;
        for (int index = 0; index < numModels; index++) {
            GLMModel oneModel = glmResults[index].get();
            double currR2 = oneModel.r2();
            if (oneModel._parms._nfolds > 0) {
                int r2Index = Arrays.asList(oneModel._output._cross_validation_metrics_summary.getRowHeaders()).indexOf("r2");
                Float tempR2 = (Float) oneModel._output._cross_validation_metrics_summary.get(r2Index, 0);
                currR2 = tempR2.doubleValue();
            }
            if (currR2 > bestR2Val) {
                bestR2Val = currR2;
                if (bestModel != null)
                    bestModel.delete();
                bestModel = oneModel;
            } else {
                oneModel.delete();
            }
        }
        return bestModel;
    }

    public static String[] extractPredictorNames(ModelSelectionModel.ModelSelectionParameters parms, DataInfo dinfo, 
                                            String foldColumn) {
        List<String> frameNames = Arrays.stream(dinfo._adaptedFrame.names()).collect(Collectors.toList());
        String[] nonResponseCols = parms.getNonPredictors();
        for (String col : nonResponseCols)
            frameNames.remove(col);
        if (foldColumn != null && frameNames.contains(foldColumn))
            frameNames.remove(foldColumn);
        return frameNames.stream().toArray(String[]::new);
        
    }
    
    public static List<String> extraModelColumnNames(List<String> coefNames, GLMModel bestModel) {
        List<String> coefUsed = new ArrayList<String>();
        List<String> modelColumns = new ArrayList<>(Arrays.asList(bestModel._output._names));
        for (String coefName : modelColumns) {
            if (coefNames.contains(coefName)) 
                coefUsed.add(coefName);
        }
        return coefUsed;
    }
    
    public static void updateValidSubset(List<Integer> validSubset, List<Integer> originalSubset, 
                                         List<Integer> currSubsetIndices) {
        List<Integer> onlyInOriginal = new ArrayList<>(originalSubset);
        onlyInOriginal.removeAll(currSubsetIndices);
        List<Integer> onlyInCurr = new ArrayList<>(currSubsetIndices);
        onlyInCurr.removeAll(originalSubset);
        validSubset.addAll(onlyInOriginal);
        validSubset.removeAll(onlyInCurr);
    }
}
