package module.fileManagement.presentationTier.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.fileManagement.domain.FileManagementSystem;
import module.fileManagement.domain.FileRepository;
import module.organization.domain.OrganizationalModel;
import module.organization.domain.Party;
import module.organization.presentationTier.actions.PartyViewHook;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.bennu.core.presentationTier.actions.ContextBaseAction;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

@Mapping(path = "/fileManagementOrganizationModel")
public class OrganizationModelPluginAction extends ContextBaseAction {

    public static class FileRepositoryView extends PartyViewHook {

        @Override
        public String hook(final HttpServletRequest request, final OrganizationalModel organizationalModel, final Party party) {
            return "/fileManagement/organizationModelDocumentView.jsp";
        }

        @Override
        public String getViewName() {
            return "04_documentView";
        }

        @Override
        public String getPresentationName() {
            return FileManagementSystem.getMessage("add.node.file.management.interface");
        }

        @Override
        public boolean isAvailableFor(final Party party) {
            return party != null && party.isUnit();
        }
    }

    public ActionForward createFileRepository(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final String partyOid = getAttribute(request, "partyOid");
        final String organizationalModelOid = getAttribute(request, "organizationalModelOid");
        final Party party = AbstractDomainObject.fromExternalId(partyOid);
        FileRepository.getOrCreateFileRepository(party);
        request.setAttribute("party", party);
        final String redirect =
                String.format(
                        "/organizationModel.do?method=viewModel&organizationalModelOid=%s&partyOid=%s&viewName=04_documentView",
                        organizationalModelOid, partyOid);
        return new ActionForward(redirect, true);
    }

}
