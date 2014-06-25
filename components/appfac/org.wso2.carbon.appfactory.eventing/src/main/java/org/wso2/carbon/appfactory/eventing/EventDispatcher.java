package org.wso2.carbon.appfactory.eventing;


public interface EventDispatcher {

    public void dispatchEvent(Event event) throws AppFactoryEventException;
}
