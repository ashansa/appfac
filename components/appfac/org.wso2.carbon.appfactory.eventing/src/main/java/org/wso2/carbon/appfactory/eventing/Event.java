package org.wso2.carbon.appfactory.eventing;

public class Event {
    public static enum EventDispatchType {
        EMAIL, SOCIAL_ACTIVITY, GUARANTEED_DELIVERY
    }

    public static enum Category {
        INFO, ERROR, WARN, ACTION
    }

    private EventDispatchType[] eventDispatchTypes;
    private Category category;
    private long timestamp = System.currentTimeMillis();
    private String messageTitle;
    private String messageBody;
    private String sender;
    private String target;

    public EventDispatchType[] getEventDispatchTypes() {
        return eventDispatchTypes;
    }

    public void setEventDispatchTypes(EventDispatchType[] eventDispatchTypes) {
        this.eventDispatchTypes = eventDispatchTypes;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
