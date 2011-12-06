package org.motechproject.ghana.national.helper;

import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.mrs.model.Attribute;
import org.motechproject.mrs.model.User;
import org.springframework.stereotype.Component;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectUnique;
import static org.hamcrest.Matchers.is;

@Component
public class StaffHelper {

    public String getEmail(User user) {
        final Attribute attribute = selectUnique(user.getAttributes(),
                having(on(Attribute.class).name(), is(Constants.PERSON_ATTRIBUTE_TYPE_EMAIL)));
        return (attribute != null) ? attribute.value() : "";
    }

    public String getPhoneNumber(User user) {
        final Attribute attribute = selectUnique(user.getAttributes(),
                having(on(Attribute.class).name(), is(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER)));
        return (attribute != null) ? attribute.value() : "";
    }

    public String getRole(User user) {
        final Attribute attribute = selectUnique(user.getAttributes(),
                having(on(Attribute.class).name(), is(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE)));
        return (attribute != null) ? attribute.value() : "";
    }
}