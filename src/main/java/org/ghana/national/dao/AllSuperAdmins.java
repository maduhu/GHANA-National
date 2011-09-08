package org.ghana.national.dao;

import org.ektorp.CouchDbConnector;
import org.ghana.national.domain.SuperAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AllSuperAdmins extends AllUsers<SuperAdmin> {
    @Autowired
    protected AllSuperAdmins(CouchDbConnector db) {
        super(SuperAdmin.class, db);
    }
}
