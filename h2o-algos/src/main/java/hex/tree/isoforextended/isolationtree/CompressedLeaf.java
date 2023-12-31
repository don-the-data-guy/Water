package hex.tree.isoforextended.isolationtree;

import water.AutoBuffer;

import static hex.genmodel.algos.isoforextended.ExtendedIsolationForestMojoModel.LEAF;

/**
 * IsolationTree Leaf Node with better memory performance. Store only the data that are needed for scoring.
 */
public class CompressedLeaf extends AbstractCompressedNode {
    private final int _numRows;

    public CompressedLeaf(IsolationTree.Node node) {
        this(node.getHeight(), node.getNumRows());
    }

    public CompressedLeaf(int currentHeight, int numRows) {
        super(currentHeight);
        _numRows = numRows;
    }

    public int getNumRows() {
        return _numRows;
    }

    /**
     * The structure of the bytes is:
     *
     * |identifierOfTheNodeType|numRows|
     */
    @Override
    public void toBytes(AutoBuffer ab) {
        ab.put1(LEAF); // identifier of this node type
        ab.put4(_numRows);
    }
}
