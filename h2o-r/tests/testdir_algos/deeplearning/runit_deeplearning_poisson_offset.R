setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source("../../../scripts/h2o-r-test-setup.R")
####### This tests offset in deeplearing for poisson by comparing results with expected behavior ######




test <- function() {
	ca = read.csv(file =locate("smalldata/glm_test/cancar_logIn.csv"),header = TRUE)
	cc = as.h2o(ca,destination_frame = "cc")
	cc$Merit = as.factor(cc$Merit)
	cc$Class = as.factor(cc$Class)
	myX =c("Merit","Class")
	myY = "Claims"
	
	#without offset
	#gg = gbm(formula = Claims~factor(Class)+factor(Merit) , distribution = "poisson",data = ca,
     #    n.trees = 9,interaction.depth = 1,n.minobsinnode = 1,shrinkage = 1,bag.fraction = 1,
      #   train.fraction = 1,verbose=T)
	#gg$train.error # -407652.4
	#pr = predict(gg, ca, type="response")
	#summary(pr)  #mean = 20200.0; min = 727.6 ; max = 202400.0
	hh = h2o.deeplearning(x = myX,y = "Claims",distribution ="poisson",hidden = c(6),epochs = 995,train_samples_per_iteration = -1,
                      reproducible = TRUE,activation = "Tanh",balance_classes = F,force_load_balance = FALSE,
                      seed = 5313,score_training_samples = 0,score_validation_samples = 0,
                      training_frame = cc, stopping_rounds=0)
	hh@model$training_metrics@metrics$mean_residual_deviance #-407674.1
	mean_deviance = hh@model$training_metrics@metrics$mean_residual_deviance
	ph = as.data.frame(h2o.predict(hh,newdata = cc))
	print(mean_deviance)
	print(mean(ph[,1]))
	print(min(ph[,1]))
	print(max(ph[,1]))
	expect_equal(10.30712, mean_deviance, tolerance=1e-5)
	expect_equal(20234.62, mean(ph[,1]), tolerance=1e-5)
	expect_equal(352.3061,    min(ph[,1]), tolerance=1e-5)
	expect_equal(217515.7, max(ph[,1]), tolerance=1e-5)

	#with offset
	#gg = gbm(formula = Claims~factor(Class)+factor(Merit)+offset(log(Insured))  , distribution = "poisson",data = ca,
     #    n.trees = 9,interaction.depth = 2,n.minobsinnode = 1,shrinkage = 1,bag.fraction = 1,
      #   train.fraction = 1,verbose=T)
	#gg$train.error  # -408153.4
	#link = predict(gg, ca, type="link")
	#link.offset = link + ca$logInsured
	#pred = exp(link.offset)
	#summary(pred)  #mean = 20200.0; min = 530.1; max = 215900.0
	hh = h2o.deeplearning(x = myX,y = "Claims",distribution ="poisson",hidden = c(6),epochs = 995,train_samples_per_iteration = -1,
                      reproducible = TRUE,activation = "Tanh",balance_classes = FALSE,force_load_balance = FALSE,
                      seed = 5313,score_training_samples = 0,score_validation_samples = 0,
                      offset_column = "logInsured",training_frame = cc, stopping_rounds=0)
	#hh@model$training_metrics@metrics$mean_residual_deviance 
	mean_deviance = hh@model$training_metrics@metrics$mean_residual_deviance
	ph = as.data.frame(h2o.predict(hh,newdata = cc))
	summary(ph)
	print(mean_deviance)
	print(mean(ph[,1]))
	print(min(ph[,1]))
	print(max(ph[,1]))
	expect_equal(4.049403, mean_deviance, tolerance=1e-3)
	expect_equal(20208.67, mean(ph[,1]), tolerance=1e-3)
	expect_equal(572.5512, min(ph[,1]), tolerance=1e-3)
	expect_equal(217348, max(ph[,1]), tolerance=1e-3)

	
}
doTest("Deeplearning offset Test: deeplearning w/ offset for poisson distribution", test)


