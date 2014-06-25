package org.wso2.carbon.appfactory.eventing;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.Event.Category;
import org.wso2.carbon.appfactory.eventing.utils.EventingConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

public class EventBuilderUtil {


    private static Log log = LogFactory.getLog(EventBuilderUtil.class);

    /**
     * =================================
     * Application Creation Events
     * =================================
     */

    //Application creation started event
    public static void invokeAppCreationStartedEvent(String appId, String createdBy, String title, String description, String status) {
        Event.Category eventStatus;
        if(status == "INFO"){
            eventStatus = Event.Category.INFO;
        } else {
            eventStatus = Event.Category.ERROR;
        }
        try {
            EventNotifier.getInstance().notify(EventBuilderUtil.buildAppCreationStartEvent(appId, createdBy, title, description, eventStatus));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify application creation event.", e);
        }
    }


    public static Event buildAppCreationStartEvent(String appId, String createdBy, String title, String description, Event.Category status){
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;

        if (status == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {

            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }

        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(createdBy);
        event.setCategory(status);
        event.setTarget(createdBy);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        return event;
    }



    // This creates an event with a target value as application_creation
    public static Event buildApplicationCreationEvent(String title, String description,
                                                      Event.Category category) {
        Event event = new Event();
        if (category.equals(Event.Category.INFO)) {
            Event.EventDispatchType[] eventDispatchTypes =
                    {Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setEventDispatchTypes(eventDispatchTypes);
        } else {
            Event.EventDispatchType[] eventDispatchTypes =
                    {
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.EMAIL};
            event.setEventDispatchTypes(eventDispatchTypes);
        }
        String sender = getSender();
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(sender);
        // event.setTarget("APPLICATION_CREATION");
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }

    // This creates an event with a target value as appId
    public static Event buildApplicationCreationEvent(String appId, String title,
                                                      String description, Event.Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes = {Event.EventDispatchType.SOCIAL_ACTIVITY};
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = getSender();
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }

    /**
     * =================================
     * Branch Creation Events
     * =================================
     */
    // When the branch is created successfully or branch creation fails
    public static Event buildBranchCreationEvent(String appId, String title, String description,
                                                 Event.Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        String sender = getSender();
        if (category.equals(Event.Category.INFO)) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setTarget(appId);
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
            event.setTarget(sender);
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(sender);
        event.setMessageBody(description);
        event.setCategory(category);
        event.setMessageTitle(title);
        return event;
    }

    /**
     * =================================
     * Continuous Integration
     * =================================
     */
    // When the build is started successfully or not
    public static Event buildTriggerBuildEvent(String appId, String repoForm, String buildTriggeredBy, String title, String description,
                                               Event.Category category) {
        Event event = new Event();
        String sender = getSender(buildTriggeredBy);
        event.setSender(sender);
        Event.EventDispatchType[] eventDispatchTypes;
        if (category.equals(Event.Category.INFO) & repoForm.equals(EventingConstants.ORIGINAL_REPO_FORM)) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setTarget(appId);
            event.setMessageBody(title);
        } else if(category.equals(Event.Category.INFO) & repoForm.equals(EventingConstants.FORKED_REPO_FORM)){
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
            event.setTarget(appId + ".fork.users." + sender);
            event.setMessageBody(description);
        } else if(category.equals(Category.ERROR) & repoForm.equals(EventingConstants.ORIGINAL_REPO_FORM)){
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.EMAIL};
            event.setTarget(appId);
            event.setMessageBody(description);
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.EMAIL};
            event.setTarget(appId + ".fork.users." + sender);
            event.setMessageBody(description);
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId);
        event.setMessageTitle(title);
        return event;
    }

    // When the build is successfully finished for failed
    public static Event buildContinuousIntegrationEvent(String appId, String repoForm, String title,
                                                        String description,
                                                        Event.Category category,
                                                        String buildTriggeredBy) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        if (category.equals(Event.Category.INFO)) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY,
                            Event.EventDispatchType.EMAIL};
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = getSender(buildTriggeredBy);
        event.setSender(sender);
        event.setCategory(category);
        if(repoForm.equals(EventingConstants.ORIGINAL_REPO_FORM)){
            event.setTarget(appId);
        } else {
            event.setTarget(appId + ".fork.users." + sender);
        }
        // event.setTarget(appId);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }

    /**
     * =================================
     * Forked Repo related events
     * =================================
     */
    // When an available branch is getting added to an already created forked
    // repo
    public static Event buildBranchForkingEvent(String appId, String title, String description,
                                                Event.Category category, String forkBranchUser) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        if (category.equals(Event.Category.INFO)) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = getSender(forkBranchUser);
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId + ".fork.users." + sender);
        event.setMessageTitle(title);
        event.setMessageBody(description);
        return event;
    }

    /**
     * =================================
     * User Addition to application
     * =================================
     */
    // When a user is added to the application
    public static Event buildUserAdditionToAppEvent(String appId, String title, String description,
                                                    Event.Category category) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        if (category.equals(Event.Category.INFO)) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }
        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = getSender();
        event.setSender(sender);
        event.setCategory(category);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        return event;
    }

    /**
     *  ======================================================
     * Getting deployed App/Service status from App Servers
     * ========================================================
     */
    //When a webapp/jaxrs/jaxws/jaggery app is deployed
    public static Event buildObtainWarDeploymentStatusEvent(String appId, String tenantDomain,
                                                            String title, String description,
                                                            Event.Category catagory) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;
        if (catagory == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }

        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = getSender();
        event.setSender(sender);
        event.setCategory(catagory);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        return event;
    }


    //When a dataservice is deployed
    public static Event buildObtainDbsDeploymentStatusEvent(String appId, String title,
                                                            String description,
                                                            Event.Category status) {
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;

        if (status == Category.INFO) {

            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {

            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }

        event.setEventDispatchTypes(eventDispatchTypes);
        String sender = getSender();
        event.setSender(sender);
        event.setCategory(status);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        return event;
    }


    /**
     *  ======================================================
     * Application promotion related events
     * ========================================================
     */
    public static void invokePromoteEvents(String appId, String promotedBy, String title, String description, String status) {
        Event.Category eventStatus;
        if(status == "INFO"){
            eventStatus = Event.Category.INFO;
        } else {
            eventStatus = Event.Category.ERROR;
        }
        try {
            EventNotifier.getInstance().notify(EventBuilderUtil.buildPromoteEvents(appId, promotedBy,title, description, eventStatus));
        } catch (AppFactoryEventException e) {
            log.error("Failed to notify application promote event.", e);
        }
    }


    public static Event buildPromoteEvents(String appId, String promotedBy, String title, String description, Event.Category status){
        Event event = new Event();
        Event.EventDispatchType[] eventDispatchTypes;

        if (status == Category.INFO) {
            eventDispatchTypes =
                    new Event.EventDispatchType[]{Event.EventDispatchType.SOCIAL_ACTIVITY};
        } else {

            eventDispatchTypes =
                    new Event.EventDispatchType[]{
                            Event.EventDispatchType.SOCIAL_ACTIVITY,
                            Event.EventDispatchType.GUARANTEED_DELIVERY};
        }

        event.setEventDispatchTypes(eventDispatchTypes);
        event.setSender(promotedBy);
        event.setCategory(status);
        event.setTarget(appId);
        event.setMessageBody(description);
        event.setMessageTitle(title);
        return event;
    }

    public static String getSender() {
        return getSender(null);
    }


    /**
     * @param recievedUserName
     * @return
     */
    public static String getSender(String recievedUserName) {
        String sender = null;
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isNotBlank(userName)) {
            sender = userName + "@" + tenantDomain;

        } else if (StringUtils.isNotBlank(recievedUserName)) {


            String[] splits = recievedUserName.split("@"); // we will extract
            // only the user
            // name from
            // received user
            // name.
            sender = splits[0] + "@" + tenantDomain;

        } else { // As the last resort we will use tenant admin to send the message


            UserRealm realm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (realm != null) {

                try {
                    RealmConfiguration configuration = realm.getRealmConfiguration();
                    sender = configuration.getAdminUserName() + "@" + tenantDomain;
                } catch (UserStoreException e) {
                    log.error("unable to retrieve the realm configuration", e);

                }

            }


        }

        return sender;

    }

}
