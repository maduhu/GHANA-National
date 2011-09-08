package org.ghana.national.domain;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;

@TypeDiscriminator("doc.type === 'SUPER_ADMIN'")
public class SuperAdmin extends User {
    @JsonProperty("type")
    final String type = "SUPER_ADMIN";

    private SuperAdmin() {
    }

    @Override
    protected String getAuthority() {
        return type;
    }

    public SuperAdmin(String username) {
        super(username);
    }
}
