package hex.tree.xgboost.task;

import ai.h2o.xgboost4j.java.Booster;
import hex.tree.xgboost.EvalMetric;
import org.apache.log4j.Logger;
import water.*;

public class XGBoostUpdateTask extends AbstractXGBoostTask<XGBoostUpdateTask> {

    private static final Logger LOG = Logger.getLogger(XGBoostUpdateTask.class);

    private final int _tid;

    public XGBoostUpdateTask(XGBoostSetupTask setupTask, int tid) {
        super(setupTask);
        _tid = tid;
    }

    @Override
    protected void execute() {
        Booster booster = XGBoostUpdater.getUpdater(_modelKey).doUpdate(_tid);
        if (booster == null)
            throw new IllegalStateException("Boosting iteration didn't produce a valid Booster.");
    }

    public byte[] getBoosterBytes() {
        final H2ONode boosterNode = getBoosterNode();
        final byte[] boosterBytes;
        if (H2O.SELF.equals(boosterNode)) {
            boosterBytes = XGBoostUpdater.getUpdater(_modelKey).getBoosterBytes();
        } else {
            LOG.debug("Booster will be retrieved from a remote node, node=" + boosterNode);
            FetchBoosterTask t = new FetchBoosterTask(_modelKey);
            boosterBytes = new RPC<>(boosterNode, t).call().get()._boosterBytes;
        }
        return boosterBytes;
    }

    public EvalMetric getEvalMetric() {
        final H2ONode boosterNode = getBoosterNode();
        final EvalMetric evalMetric;
        if (H2O.SELF.equals(boosterNode)) {
            evalMetric = XGBoostUpdater.getUpdater(_modelKey).getEvalMetric();
        } else {
            LOG.debug("CustomMetric will be retrieved from a remote node, node=" + boosterNode);
            FetchEvalMetricTask t = new FetchEvalMetricTask(_modelKey);
            evalMetric = new RPC<>(boosterNode, t).call().get()._evalMetric;
        }
        return evalMetric;
    }

    private static class FetchBoosterTask extends DTask<FetchBoosterTask> {
        private final Key _modelKey;

        // OUT
        private byte[] _boosterBytes;

        private FetchBoosterTask(Key modelKey) {
            _modelKey = modelKey;
        }

        @Override
        public void compute2() {
            _boosterBytes = XGBoostUpdater.getUpdater(_modelKey).getBoosterBytes();
            tryComplete();
        }
    }

    private static class FetchEvalMetricTask extends DTask<FetchEvalMetricTask> {
        private final Key _modelKey;

        // OUT
        private EvalMetric _evalMetric;

        private FetchEvalMetricTask(Key modelKey) {
            _modelKey = modelKey;
        }

        @Override
        public void compute2() {
            _evalMetric = XGBoostUpdater.getUpdater(_modelKey).getEvalMetric();
            tryComplete();
        }
    }

}
