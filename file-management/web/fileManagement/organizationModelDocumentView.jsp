<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<logic:present role="myorg.domain.RoleType.MANAGER">
	<bean:define id="partyName" name="party" property="presentationName" />
	<logic:present name="party" property="fileRepository">
		A unidade <%= partyName %> tem repositório definido
	</logic:present>
	<logic:notPresent name="party" property="fileRepository">
		A unidade <%= partyName %> não tem repositório definido.
		<bean:define id="url">/fileManagementOrganizationModel.do?method=createFileRepository&amp;organizationalModelOid=<bean:write name="organizationalModel" property="externalId"/></bean:define>
		<html:link action="<%= url %>" paramId="partyOid" paramName="party" paramProperty="externalId">
			Clique aqui para criar.
		</html:link>
	</logic:notPresent>
</logic:present>
<logic:notPresent role="myorg.domain.RoleType.MANAGER">
	Não tem permissões para visualizar esta página.
</logic:notPresent>