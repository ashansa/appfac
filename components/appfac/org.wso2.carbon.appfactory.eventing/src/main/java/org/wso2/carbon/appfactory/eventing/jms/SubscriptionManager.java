package org.wso2.carbon.appfactory.eventing.jms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.utils.Util;

public class SubscriptionManager {
    Log log = LogFactory.getLog(SubscriptionManager.class);
    private static SubscriptionManager subscriptionManager;

    private Map<String, org.wso2.carbon.appfactory.eventing.jms.Subscriber> subscriberMap;
    private Map<String, EventConsumer> eventConsumers =
                                                        new ConcurrentHashMap<String, EventConsumer>();

    private SubscriptionManager() {
        subscriberMap = new ConcurrentHashMap<String, org.wso2.carbon.appfactory.eventing.jms.Subscriber>();
    }

    public static SubscriptionManager getInstance() {
        if (subscriptionManager == null) {
            synchronized (SubscriptionManager.class) {
                if (subscriptionManager == null) {
                    subscriptionManager = new SubscriptionManager();
                }
            }
        }
        return subscriptionManager;
    }

    public void startSubscription(String topic, String uid) throws AppFactoryEventException {

        EventConsumer consumer = eventConsumers.get(uid);
        if (consumer == null) {
            consumer = new EventConsumer(uid);
            eventConsumers.put(uid, consumer);
            consumer.startSubscription(topic);

            if (log.isDebugEnabled()) {
                log.debug("New subscription is started for topic:" + topic +
                        " and for user:" + uid);
            }
        }

    }

    public boolean isSubscriptionStarted(String topic, String subscriberId) {
        return subscriberMap.get(Util.getUniqueSubscriptionId(topic, subscriberId)) != null;
    }

    public void stopAllSubscriptions(String uid) throws AppFactoryEventException {

        EventConsumer consumer = eventConsumers.get(uid);

        if (log.isDebugEnabled()) {
            log.debug("Stoping All subscriptions for  = " + uid);
        }

        if (uid != null) {

            consumer.stopAllSubscriptions();
        } else {
            if (log.isWarnEnabled()) {
                log.warn("unable to find event consumer by uid: [" + uid +
                         "] therefore, unable to stop all subscriptions.");
            }

        }
    }

    public void stopSubscription(String topic, String uid) throws AppFactoryEventException {

        EventConsumer consumer = eventConsumers.get(uid);

        if (consumer != null) {
            consumer.stopSubscription(topic);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("unable to find event consumer by uid: [" + uid +
                         "] therefore, unable to stop subscription for topic : " + topic);
            }
        }

    }


}
