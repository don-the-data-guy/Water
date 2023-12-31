package water;

import org.junit.Ignore;
import water.fvec.Frame;
import water.fvec.TestFrameBuilder;
import water.fvec.Vec;

import java.util.UUID;

import static water.TestUtil.*;

@Ignore // prepackaged small H2O Frames
public class TestFrameCatalog {

  public static Frame oneChunkFewRows() {
    return new TestFrameBuilder()
            .withVecTypes(Vec.T_NUM, Vec.T_NUM, Vec.T_CAT, Vec.T_CAT)
            .withDataForCol(0, new double[]{1.2, 3.4, 5.6})
            .withDataForCol(1, new double[]{-1, 0, 1})
            .withDataForCol(2, new String[]{"a", "b", "a"})
            .withDataForCol(3, new String[]{"y", "y", "n"})
            .build();
  }

  public static Frame specialColumns() {
    return new TestFrameBuilder()
            .withColNames("Fold", "ColA", "Response", "ColB", "Weight", "Offset", "ColC")
            .withVecTypes(Vec.T_NUM, Vec.T_NUM, Vec.T_NUM, Vec.T_STR, Vec.T_NUM, Vec.T_NUM, Vec.T_CAT)
            .withDataForCol(0, ard(0, 1, 0, 1, 0, 1, 0))
            .withDataForCol(1, ard(Double.NaN, 1, 2, 3, 4, 5.6, 7))
            .withDataForCol(2, ard(1, 2, 3, 4, 1, 2, 3))
            .withDataForCol(3, ar("A", "B", "C", "E", "F", "I", "J"))
            .withDataForCol(4, ard(0.25, 0.25, 0.5, 0.5, 0.5, 0.75, 0.75))
            .withDataForCol(5, ard(0.1, 0.1, 0.1, 0.1, 0.2, 0.2, 0.2))
            .withDataForCol(6, ar("A", "B,", "A", "C", "A", "B", "A"))
            .build();
  }

  /**
   * Creates a frame with columns that are typically not used in model building (UUID, Bad)
   * @return a frame instance
   */
  public static Frame unusualTypes() {
    return new TestFrameBuilder()
            .withColNames("UUIDCol", "BadCol")
            .withVecTypes(Vec.T_UUID, Vec.T_BAD)
            .withDataForCol(0, ar(UUID.randomUUID().toString(), null))
            .withDataForCol(1, ard(Double.NaN, Double.NaN))
            .build();
  }

  public static Frame prostateCleaned() {
    final String response = "CAPSULE";
    final String testFile = "./smalldata/logreg/prostate.csv";
    Frame fr = parseAndTrackTestFile(testFile)
            .toCategoricalCol("RACE")
            .toCategoricalCol("GLEASON")
            .toCategoricalCol(response);
    fr.remove("ID").remove();
    DKV.put(fr);
    return fr;
  }

}
