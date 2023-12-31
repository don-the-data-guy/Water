import sys, os
sys.path.insert(1, "../../../")
import h2o
from tests import pyunit_utils
from random import randint
import tempfile

def gam_binomial_mojo_MS():
    h2o.remove_all()
    NTESTROWS = 200    # number of test dataset rows
    PROBLEM="binomial"
    params = set_params()   # set deeplearning model parameters
    df = pyunit_utils.random_dataset(PROBLEM)   # generate random dataset
    dfnames = df.names
    # add GAM specific parameters
    params["gam_columns"] = []
    params["scale"] = []
    params["bs"] = []
    count = 0
    num_gam_cols = 3    # maximum number of gam columns
    for cname in dfnames:
        if not(cname == 'response') and (str(df.type(cname)) == "real"):
            params["gam_columns"].append(cname)
            params["scale"].append(0.001)
            params["bs"].append(3)
            count = count+1
            if (count >= num_gam_cols):
                break
    
    train = df[NTESTROWS:, :]
    test = df[:NTESTROWS, :]
    x = list(set(df.names) - {"response"})
    tmpdir = tempfile.mkdtemp()
    gamBinomialModel = pyunit_utils.build_save_model_generic(params, x, train, "response", "gam", tmpdir) # build and save mojo model
    MOJONAME = pyunit_utils.getMojoName(gamBinomialModel._id)

    h2o.download_csv(test[x], os.path.join(tmpdir, 'in.csv'))  # save test file, h2o predict/mojo use same file
    pred_h2o, pred_mojo = pyunit_utils.mojo_predict(gamBinomialModel, tmpdir, MOJONAME)  # load model and perform predict
    h2o.download_csv(pred_h2o, os.path.join(tmpdir, "h2oPred.csv"))
    print("Comparing mojo predict and h2o predict...")
    pyunit_utils.compare_frames_local(pred_h2o, pred_mojo, 0.1, tol=1e-10)    # make sure operation sequence is preserved from Tomk        h2o.save_model(glmOrdinalModel, path=TMPDIR, force=True)  # save model for debugging
    
def set_params():
    missingValues = ['MeanImputation']
    missing_values = missingValues[randint(0, len(missingValues)-1)]

    params = {'missing_values_handling': missing_values, 'family':"binomial"}
    print(params)
    return params


if __name__ == "__main__":
    pyunit_utils.standalone_test(gam_binomial_mojo_MS)
else:
    gam_binomial_mojo_MS()
