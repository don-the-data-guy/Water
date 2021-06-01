#!/usr/bin/env python
# -*- encoding: utf-8 -*-
#
# This file is auto-generated by h2o-3/h2o-bindings/bin/gen_python.py
# Copyright 2016 H2O.ai;  Apache License Version 2.0 (see LICENSE for details)
#
from __future__ import absolute_import, division, print_function, unicode_literals

from h2o.estimators.estimator_base import H2OEstimator
from h2o.exceptions import H2OValueError
from h2o.frame import H2OFrame
from h2o.utils.typechecks import assert_is_type, Enum, numeric


class H2OGenericEstimator(H2OEstimator):
    """
    Import MOJO Model

    """

    algo = "generic"
    supervised_learning = False
    _options_ = {'requires_training_frame': False}

    def __init__(self,
                 model_id=None,  # type: Optional[Union[None, str, H2OEstimator]]
                 model_key=None,  # type: Optional[Union[None, str, H2OFrame]]
                 path=None,  # type: Optional[str]
                 ):
        """
        :param model_id: Destination id for this model; auto-generated if not specified.
               Defaults to ``None``.
        :type model_id: Union[None, str, H2OEstimator], optional
        :param model_key: Key to the self-contained model archive already uploaded to H2O.
               Defaults to ``None``.
        :type model_key: Union[None, str, H2OFrame], optional
        :param path: Path to file with self-contained model archive.
               Defaults to ``None``.
        :type path: str, optional
        """
        super(H2OGenericEstimator, self).__init__()
        self._parms = {}
        self._id = self._parms['model_id'] = model_id
        self.model_key = model_key
        self.path = path

    @property
    def model_key(self):
        """
        Key to the self-contained model archive already uploaded to H2O.

        Type: ``Union[None, str, H2OFrame]``.

        :examples:

        >>> from h2o.estimators import H2OGenericEstimator, H2OXGBoostEstimator
        >>> import tempfile
        >>> airlines= h2o.import_file("https://s3.amazonaws.com/h2o-public-test-data/smalldata/testng/airlines_train.csv")
        >>> y = "IsDepDelayed"
        >>> x = ["fYear","fMonth","Origin","Dest","Distance"]
        >>> xgb = H2OXGBoostEstimator(ntrees=1, nfolds=3)
        >>> xgb.train(x=x, y=y, training_frame=airlines)
        >>> original_model_filename = tempfile.mkdtemp()
        >>> original_model_filename = xgb.download_mojo(original_model_filename)
        >>> key = h2o.lazy_import(original_model_filename)
        >>> fr = h2o.get_frame(key[0])
        >>> model = H2OGenericEstimator(model_key=fr)
        >>> model.train()
        >>> model.auc()
        """
        return self._parms.get("model_key")

    @model_key.setter
    def model_key(self, model_key):
        self._parms["model_key"] = H2OFrame._validate(model_key, 'model_key')

    @property
    def path(self):
        """
        Path to file with self-contained model archive.

        Type: ``str``.

        :examples:

        >>> from h2o.estimators import H2OIsolationForestEstimator, H2OGenericEstimator
        >>> import tempfile
        >>> airlines= h2o.import_file("https://s3.amazonaws.com/h2o-public-test-data/smalldata/testng/airlines_train.csv")
        >>> ifr = H2OIsolationForestEstimator(ntrees=1)
        >>> ifr.train(x=["Origin","Dest"], y="Distance", training_frame=airlines)
        >>> generic_mojo_filename = tempfile.mkdtemp("zip","genericMojo")
        >>> generic_mojo_filename = model.download_mojo(path=generic_mojo_filename)
        >>> model = H2OGenericEstimator.from_file(generic_mojo_filename)
        >>> model.model_performance()
        """
        return self._parms.get("path")

    @path.setter
    def path(self, path):
        assert_is_type(path, None, str)
        self._parms["path"] = path


    @staticmethod
    def from_file(file=str):
        """
        Creates new Generic model by loading existing embedded model into library, e.g. from H2O MOJO.
        The imported model must be supported by H2O.

        :param file: A string containing path to the file to create the model from
        :return: H2OGenericEstimator instance representing the generic model

        :examples:

        >>> from h2o.estimators import H2OIsolationForestEstimator, H2OGenericEstimator
        >>> import tempfile
        >>> airlines= h2o.import_file("https://s3.amazonaws.com/h2o-public-test-data/smalldata/testng/airlines_train.csv")
        >>> ifr = H2OIsolationForestEstimator(ntrees=1)
        >>> ifr.train(x=["Origin","Dest"], y="Distance", training_frame=airlines)
        >>> original_model_filename = tempfile.mkdtemp()
        >>> original_model_filename = ifr.download_mojo(original_model_filename)
        >>> model = H2OGenericEstimator.from_file(original_model_filename)
        >>> model.model_performance()
        """
        model = H2OGenericEstimator(path = file)
        model.train()

        return model
