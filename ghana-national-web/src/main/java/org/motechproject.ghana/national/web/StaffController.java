package org.motechproject.ghana.national.web;

import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.StaffType;
import org.motechproject.ghana.national.helper.StaffHelper;
import org.motechproject.ghana.national.service.EmailTemplateService;
import org.motechproject.ghana.national.service.IdentifierGenerationService;
import org.motechproject.ghana.national.service.StaffService;
import org.motechproject.ghana.national.web.form.StaffForm;
import org.motechproject.mrs.exception.UserAlreadyExistsException;
import org.motechproject.mrs.model.Attribute;
import org.motechproject.mrs.model.User;
import org.motechproject.openmrs.advice.ApiSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping(value = "/admin/staffs")
public class StaffController {
    public static final String REQUESTED_STAFFS = "requestedStaffs";
    private StaffService staffService;
    private MessageSource messageSource;
    private IdentifierGenerationService identifierGenerationService;
    private StaffHelper staffHelper;

    public static final String STAFF_ID = "userId";
    public static final String STAFF_SEQUENTIAL_ID = "Id";
    public static final String STAFF_NAME = "userName";
    public static final String EMAIL = "email";
    public static final String CREATE_STAFF_FORM = "createStaffForm";
    static final String EDIT_STAFF_FORM = "editStaffForm";
    public static final String STAFF_ALREADY_EXISTS = "user_email_already_exists";
    public static final String NEW_STAFF_URL = "staffs/new";
    private EmailTemplateService emailTemplateService;
    public static final String SEARCH_STAFF_FORM = "searchStaffForm";
    public static final String SEARCH_STAFF = "staffs/search";
    static final String EDIT_STAFF_URL = "staffs/edit";
    public static final String SUCCESS = "staffs/success";

    public StaffController() {
    }

    @Autowired
    public StaffController(StaffService staffService, MessageSource messageSource, EmailTemplateService emailTemplateService,
                           IdentifierGenerationService identifierGenerationService, StaffHelper staffHelper) {
        this.staffService = staffService;
        this.messageSource = messageSource;
        this.emailTemplateService = emailTemplateService;
        this.identifierGenerationService = identifierGenerationService;
        this.staffHelper = staffHelper;
    }

    @ApiSession
    @RequestMapping(value = "new", method = RequestMethod.GET)
    public String newUser(ModelMap modelMap) {
        modelMap.addAttribute(CREATE_STAFF_FORM, new StaffForm());
        modelMap.addAttribute("roles", staffService.fetchAllRoles());
        return NEW_STAFF_URL;
    }

    @ApiSession
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String createUser(@Valid StaffForm staffForm, BindingResult bindingResult, ModelMap model) {
        Map userData;
        User user = new User();
        user.firstName(staffForm.getFirstName()).middleName(staffForm.getMiddleName()).lastName(staffForm.getLastName());
        user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_EMAIL, staffForm.getEmail()));
        user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER, staffForm.getPhoneNumber()));
        String roleOfStaff = staffForm.getRole();
        user.addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE, roleOfStaff));

        user.securityRole(StaffType.Role.securityRoleFor(roleOfStaff));
        if (StaffType.Role.isAdmin(roleOfStaff)) {
            user.userName(staffForm.getEmail());
        }
        user.id(identifierGenerationService.newStaffId());

        try {
            userData = staffService.saveUser(user);
            org.openmrs.User openMRSUser = (org.openmrs.User) userData.get("openMRSUser");
            model.put(STAFF_ID, openMRSUser.getSystemId());
            model.put(STAFF_NAME, user.getFullName());
            if (StaffType.Role.isAdmin(roleOfStaff)) {
                emailTemplateService.sendEmailUsingTemplates(openMRSUser.getUsername(), (String) userData.get("password"));
            }
            return getStaffForId(model,openMRSUser.getSystemId());
        } catch (UserAlreadyExistsException e) {
            bindingResult.addError(new FieldError(CREATE_STAFF_FORM, EMAIL, messageSource.getMessage(STAFF_ALREADY_EXISTS, null, Locale.getDefault())));
            model.addAttribute("roles", staffService.fetchAllRoles());
            model.mergeAttributes(bindingResult.getModel());
            return NEW_STAFF_URL;
        }
    }

    @ApiSession
    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String editFacilityForm(ModelMap modelMap, HttpServletRequest httpServletRequest) {
        String staffId = httpServletRequest.getParameter(STAFF_SEQUENTIAL_ID);
        return getStaffForId(modelMap, staffId);
    }

    @ApiSession
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String updateFacility(@Valid StaffForm updateStaffForm, BindingResult bindingResult, ModelMap modelMap) {
        try {
//            staffService.update();
            return SUCCESS;
        } catch (UsernameNotFoundException e) {
        }
        return NEW_STAFF_URL;
    }

    private String getStaffForId(ModelMap modelMap, String staffId) {
        User user = staffService.getUserById(staffId);
        modelMap.addAttribute(EDIT_STAFF_FORM, copyStaffValuesToForm(user));
        modelMap.addAttribute("roles", staffService.fetchAllRoles());
        return EDIT_STAFF_URL;
    }

    private StaffForm copyStaffValuesToForm(User user) {
        StaffForm staffForm = new StaffForm();
        staffForm.setFirstName(user.getFirstName());
        staffForm.setMiddleName(user.getMiddleName());
        staffForm.setLastName(user.getLastName());
//        staffForm.setEmail(user.getAttributes().get(0));
//        staffForm.setPhoneNumber(user.getAttributes().get(1).value());
//        staffForm.setRole(user.getAttributes().get(2).value());
        return staffForm;
    }

    @ApiSession
    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String searchStaffForm(ModelMap modelMap) {
        modelMap.put(SEARCH_STAFF_FORM, new StaffForm());
        modelMap.addAttribute("roles", staffService.fetchAllRoles());
        return SEARCH_STAFF;
    }

    @ApiSession
    @RequestMapping(value = "searchStaffs", method = RequestMethod.POST)
    public String searchStaff(@Valid final StaffForm staffForm, ModelMap modelMap) {
        final List<User> users = staffService.searchStaff(staffForm.getMotechId(), staffForm.getFirstName(),
                staffForm.getMiddleName(), staffForm.getLastName(), staffForm.getPhoneNumber(), staffForm.getRole());

        final ArrayList<StaffForm> requestedUsers = new ArrayList<StaffForm>();
        for (User user : users) {
            requestedUsers.add(new StaffForm(user.getId(), user.getFirstName(), user.getMiddleName(), user.getLastName(),
                    staffHelper.getEmail(user), staffHelper.getPhoneNumber(user), staffHelper.getRole(user)));
        }
        modelMap.put(SEARCH_STAFF_FORM, new StaffForm());
        modelMap.put(REQUESTED_STAFFS, requestedUsers);
        modelMap.addAttribute("roles", staffService.fetchAllRoles());
        return SEARCH_STAFF;
    }

}