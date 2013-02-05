package module.fileManagement.presentationTier.webdav;

import io.milton.http.AuthenticationHandler;
import io.milton.http.Filter;
import io.milton.http.fs.SimpleSecurityManager;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.ExpiredNonceRemover;
import io.milton.http.http11.auth.SecurityManagerDigestAuthenticationHandler;
import io.milton.http.http11.auth.SimpleMemoryNonceProvider;
import io.milton.servlet.DefaultMiltonConfigurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FMSMiltonConfigurator extends DefaultMiltonConfigurator {
    public static final String REALM = "WebDavDocs";

    @Override
    protected void build() {
        builder.setEnableBasicAuth(false);
        builder.setEnableCookieAuth(false);
        builder.setFsRealm(REALM);
        SimpleSecurityManager securityManager = new FMSSecurityManager(builder.getFsRealm(), new HashMap<String, String>());
        securityManager.setDigestGenerator(new DigestGenerator());

        builder.setSecurityManager(securityManager);

        final ExpiredNonceRemover expiredNonceRemover =
                new ExpiredNonceRemover(builder.getNonces(), builder.getNonceValiditySeconds());
        builder.setExpiredNonceRemover(expiredNonceRemover);

        final SimpleMemoryNonceProvider nonceProvider =
                new SimpleMemoryNonceProvider(builder.getNonceValiditySeconds(), expiredNonceRemover, builder.getNonces());
        builder.setNonceProvider(nonceProvider);

        List<AuthenticationHandler> authHandlers = new ArrayList<AuthenticationHandler>();
        authHandlers.add(new SecurityManagerDigestAuthenticationHandler(nonceProvider, securityManager));
        builder.setAuthenticationHandlers(authHandlers);

        builder.init();

        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new FMSPreAuthenticationFilter(builder.getHttp11ResponseHandler(), builder.getAuthenticationHandlers()));
        filters.addAll(builder.getFilters());
        builder.setFilters(filters);

        super.build();
    }
}
