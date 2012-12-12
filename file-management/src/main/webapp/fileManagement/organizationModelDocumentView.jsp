<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<logic:present role="pt.ist.bennu.core.domain.RoleType.MANAGER">
	<bean:define id="partyName" name="party" property="presentationName" />
	<logic:present name="party" property="fileRepository">
		A unidade <%= partyName %> tem repositório definido
		<bean:define id="urlNavigateToPartyRepository">
		/vaadinContext.do?method=forwardToVaadin#DocumentBrowse?contextPath=<bean:write name="party" property="fileRepository.externalId"/>
		</bean:define>
		<html:link action="<%= urlNavigateToPartyRepository %>">Navegar para directoria</html:link>
	</logic:present>
	<logic:notPresent name="party" property="fileRepository">
		A unidade <%= partyName %> não tem repositório definido.
		<bean:define id="url">/fileManagementOrganizationModel.do?method=createFileRepository&amp;organizationalModelOid=<bean:write name="organizationalModel" property="externalId"/></bean:define>
		<html:link action="<%= url %>" paramId="partyOid" paramName="party" paramProperty="externalId">
			Clique aqui para criar.
		</html:link>
		</br>
	</logic:notPresent>
</logic:present>
<logic:notPresent role="pt.ist.bennu.core.domain.RoleType.MANAGER">
	Não tem permissões para visualizar esta página.
</logic:notPresent>
