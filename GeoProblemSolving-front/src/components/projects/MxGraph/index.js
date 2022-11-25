import mx from 'mxgraph';
const mxgraph = mx({
  mxImageBasePath: './src/images',
  mxBasePath: './src'
});
// decode bug https://github.com/jgraph/mxgraph/issues/49
window.mxGraph = mxgraph.mxGraph;
window.mxGraphModel = mxgraph.mxGraphModel;
window.mxEditor = mxgraph.mxEditor;
window.mxGeometry = mxgraph.mxGeometry;
window.mxDefaultKeyHandler = mxgraph.mxDefaultKeyHandler;
window.mxDefaultPopupMenu = mxgraph.mxDefaultPopupMenu;
window.mxStylesheet = mxgraph.mxStylesheet;
window.mxDefaultToolbar = mxgraph.mxDefaultToolbar;
window.mxConstants = mxgraph.mxConstants;
window.mxUndoManager = mxgraph.mxUndoManager;
window.mxCellOverlay = mxgraph.mxCellOverlay;
window.mxImageExport = mxgraph.mxImageExport;
window.mxXmlCanvas2D = mxgraph.mxXmlCanvas2D;
window.mxConnectionConstraint = mxgraph.mxConnectionConstraint;
window.mxObjectCodec = mxgraph.mxObjectCodec;
window.mxCodec = mxgraph.mxCodec;
window.mxShape = mxgraph.mxShape;
window.mxEdgeHandler = mxgraph.mxEdgeHandler;
window.mxVertexHandler = mxgraph.mxVertexHandler;
window.mxCellEditor = mxgraph.mxCellEditor;
window.mxGraphHandler = mxgraph.mxGraphHandler;
window.mxPerimeter = mxgraph.mxPerimeter;
window.mxCellState = mxgraph.mxCellState;

export default mxgraph;
