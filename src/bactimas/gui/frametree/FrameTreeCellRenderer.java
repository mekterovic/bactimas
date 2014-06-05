package bactimas.gui.frametree;


import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class FrameTreeCellRenderer  extends DefaultTreeCellRenderer  {	
  /**
	 * 
	 */
	private static final long serialVersionUID = 519960534254500195L;

@Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    final Component rc = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);		
    if (((DefaultMutableTreeNode)value).getUserObject() instanceof ITooltip ) {
    	this.setToolTipText(((ITooltip) ((DefaultMutableTreeNode)value).getUserObject()).getTooltip());	
    } 
    if (((DefaultMutableTreeNode)value).getUserObject() instanceof ICanRender) {
    	this.setIcon(((ICanRender) ((DefaultMutableTreeNode)value).getUserObject()).getIcon());	
    	this.setText(((ICanRender) ((DefaultMutableTreeNode)value).getUserObject()).getRenderString());
    }
    
    return rc;
  }



}