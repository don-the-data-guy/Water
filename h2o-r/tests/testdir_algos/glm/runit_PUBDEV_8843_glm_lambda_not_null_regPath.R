setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source("../../../scripts/h2o-r-test-setup.R")
library(glmnet)

# Test that with regularization on, the p-values are computed
test.glm_reg_path <- function() {
    d <-  h2o.importFile(path = locate("smalldata/logreg/prostate.csv"))
    alphaArray <- c(0.1,0.5,0.9)
    m = h2o.glm(training_frame=d, x=3:9, y=2, family='binomial', alpha=alphaArray, compute_p_values = TRUE)
    regpath = h2o.getGLMFullRegularizationPath(m)
  
    expect_true(length(alphaArray)==length(regpath$alphas))
}

doTest("GLM p-values calculation with regularization on", test.glm_reg_path)
