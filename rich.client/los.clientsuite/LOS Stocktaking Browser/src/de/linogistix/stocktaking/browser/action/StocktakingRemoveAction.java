/*
 * Copyright (c) 2006 - 2010 LinogistiX GmbH
 * 
 *  www.linogistix.com
 *  
 *  Project myWMS-LOS
 */
package de.linogistix.stocktaking.browser.action;

import de.linogistix.common.bobrowser.bo.BO;
import de.linogistix.common.bobrowser.bo.BOMasterNode;
import de.linogistix.common.services.J2EEServiceLocator;
import de.linogistix.common.userlogin.LoginService;
import de.linogistix.common.util.CursorControl;
import de.linogistix.common.util.ExceptionAnnotator;
import de.linogistix.los.stocktaking.facade.LOSStocktakingFacade;
import de.linogistix.stocktaking.res.StocktakingBundleResolver;
import java.util.logging.Logger;
import org.mywms.facade.FacadeException;
import org.mywms.globals.Role;
import org.mywms.model.BasicEntity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public final class StocktakingRemoveAction extends NodeAction {

    private static final Logger log = Logger.getLogger(StocktakingRemoveAction.class.getName());
   private static String[] allowedRoles = new String[]{
        Role.ADMIN.toString(),
        Role.FOREMAN.toString(),
        Role.INVENTORY_STR
    };

    public String getName() {
        return NbBundle.getMessage(StocktakingBundleResolver.class, "LABEL_REMOVE");
    }

    protected String iconResource() {
        return "de/linogistix/bobrowser/res/icon/Action.png";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        LoginService login = (LoginService) Lookup.getDefault().lookup(LoginService.class);
        if( login.checkRolesAllowed(allowedRoles) == false ) {
            return false;
        }
        
        return true;
    }

    protected void performAction(Node[] activatedNodes) {

        BasicEntity e;

        if (activatedNodes == null) {
            return;
        }

        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(StocktakingBundleResolver.class, "NotifyDescriptor.ReallyRemove"),
                NbBundle.getMessage(StocktakingBundleResolver.class, "LABEL_REMOVE"),
                NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        
        try {
            J2EEServiceLocator loc = (J2EEServiceLocator) Lookup.getDefault().lookup(J2EEServiceLocator.class);
            LOSStocktakingFacade m = loc.getStateless(LOSStocktakingFacade.class);

            for (Node n : activatedNodes) {
                if (n == null) {
                    continue;
                }
                if (!(n instanceof BOMasterNode)) {
                    log.warning("Not a BOMasterNodeType: " + n.toString());
                    continue;
                }
                m.removeOrder(((BOMasterNode)n).getEntity().getId());
            }
        } catch (FacadeException ex) {
            ExceptionAnnotator.annotate(ex);
        } finally {
            CursorControl.showNormalCursor();
        }
        
        if( activatedNodes.length>0 ) {
            Node n = activatedNodes[0];
            if (n instanceof BOMasterNode) {
                BOMasterNode ma = (BOMasterNode)n;
                BO bo = ma.getBo();
                bo.fireOutdatedEvent(ma);
            }
        }

    }
}

