``min_split_improvement``
-------------------------

- Available in: GBM, DRF, XGBoost, Uplift DRF
- Hyperparameter: yes

Description
~~~~~~~~~~~

This option specifies the minimum relative improvement in squared error reduction in order for a split to occur. When properly tuned, this option can help reduce overfitting because the algorithm will stop splitting when all the possible splits lead to worse error measures. In addition, a single tree will stop splitting when there are no more splits that satisfy the ``min_rows`` parameter, if it reaches ``max_depth``, or if there are no splits that satisfy this ``min_split_improvement`` parameter.

For GBM and DRF models, this value defaults to 0.00001; for XGBoost models, this value defaults to 0. Optimal values for this parameter are in the 1e-10...1e-3 range.

Related Parameters
~~~~~~~~~~~~~~~~~~

- `max_depth <max_depth.html>`__
- `min_rows <min_rows.html>`__


Example
~~~~~~~

.. tabs::
   .. code-tab:: r R

		library(h2o)
		h2o.init()

		# import the cars dataset: 
		# this dataset is used to classify whether or not a car is economical based on 
		# the car's displacement, power, weight, and acceleration, and the year it was made 
		cars <- h2o.importFile("https://s3.amazonaws.com/h2o-public-test-data/smalldata/junit/cars_20mpg.csv")

		# convert response column to a factor
		cars["economy_20mpg"] <- as.factor(cars["economy_20mpg"])

		# set the predictor names and the response column name
		predictors <- c("displacement", "power", "weight", "acceleration", "year")
		response <- "economy_20mpg"

		# split into train and validation sets
		cars_split <- h2o.splitFrame(data = cars, ratios = 0.8, seed = 1234)
		train <- cars_split[[1]]
		valid <- cars_split[[2]]

		# try using the `min_split_improvement` parameter:
		# train your model:
		cars_gbm <- h2o.gbm(x = predictors, y = response, training_frame = train,
		                    validation_frame = valid, min_split_improvement = 1e-3, seed = 1234)

		# print the auc for your model
		print(h2o.auc(cars_gbm, valid = TRUE))

		# Example of values to grid over for `min_split_improvement`:
		hyper_params <- list( min_split_improvement = c(1e-4, 1e-5)  )

		# this example uses cartesian grid search because the search space is small
		# and we want to see the performance of all models. For a larger search space use
		# random grid search instead: list(strategy = "RandomDiscrete")
		# this GBM uses early stopping once the validation AUC doesn't improve by at least 0.01% for
		# 5 consecutive scoring events
		grid <- h2o.grid(x = predictors, y = response, training_frame = train, validation_frame = valid,
		                 algorithm = "gbm", grid_id = "cars_grid", hyper_params = hyper_params,
		                 stopping_rounds = 5, stopping_tolerance = 1e-4, stopping_metric = "AUC",
		                 search_criteria = list(strategy = "Cartesian"), seed = 1234)

		## Sort the grid models by AUC
		sorted_grid <- h2o.getGrid("cars_grid", sort_by = "auc", decreasing = TRUE)
		sorted_grid


   .. code-tab:: python

		import h2o
		from h2o.estimators.gbm import H2OGradientBoostingEstimator
		h2o.init()

		# import the cars dataset:
		# this dataset is used to classify whether or not a car is economical based on
		# the car's displacement, power, weight, and acceleration, and the year it was made
		cars = h2o.import_file("https://s3.amazonaws.com/h2o-public-test-data/smalldata/junit/cars_20mpg.csv")

		# convert response column to a factor
		cars["economy_20mpg"] = cars["economy_20mpg"].asfactor()

		# set the predictor names and the response column name
		predictors = ["displacement","power","weight","acceleration","year"]
		response = "economy_20mpg"

		# split into train and validation sets
		train, valid = cars.split_frame(ratios = [.8], seed = 1234)

		# try turning on the `min_split_improvement` parameter:
		# initialize your estimator
		cars_gbm = H2OGradientBoostingEstimator(min_split_improvement = 1e-3, seed = 1234)

		# then train your model
		cars_gbm.train(x = predictors, y = response, training_frame = train, validation_frame = valid)

		# print the auc for the validation data
		print(cars_gbm.auc(valid=True))


		# Example of values to grid over for `min_split_improvement`
		# import Grid Search
		from h2o.grid.grid_search import H2OGridSearch

		# select the values for `min_split_improvement` to grid over
		hyper_params = {'min_split_improvement': [1e-4, 1e-5]}

		# this example uses cartesian grid search because the search space is small
		# and we want to see the performance of all models. For a larger search space use
		# random grid search instead: {'strategy': "RandomDiscrete"}
		# initialize the GBM estimator
		# use early stopping once the validation AUC doesn't improve by at least 0.01% for 
		# 5 consecutive scoring events
		cars_gbm_2 = H2OGradientBoostingEstimator(seed = 1234,
		                                          stopping_rounds = 5,
		                                          stopping_metric = "AUC", stopping_tolerance = 1e-4,)

		# build grid search with previously made GBM and hyper parameters
		grid = H2OGridSearch(model = cars_gbm_2, hyper_params = hyper_params,
		                     search_criteria = {'strategy': "Cartesian"})

		# train using the grid
		grid.train(x = predictors, y = response, training_frame = train, validation_frame = valid, seed = 1234)

		# sort the grid models by decreasing AUC
		sorted_grid = grid.get_grid(sort_by = 'auc', decreasing = True)
		print(sorted_grid)

