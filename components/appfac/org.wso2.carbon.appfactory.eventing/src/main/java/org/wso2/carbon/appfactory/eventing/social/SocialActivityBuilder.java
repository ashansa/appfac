package org.wso2.carbon.appfactory.eventing.social;

import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.appfactory.eventing.Event;


public class SocialActivityBuilder {
    private Event event;

    public SocialActivityBuilder(Event event) {
        this.event = event;
    }
    /*
    {"verb":"post",
    "object":{"objectType":"review","content":"new comment.","rating":2},
    "target":{"id":"ebook:c5db8856-cd30-4d5f-8678-f9db793578c7"},
    "actor":{"id":"man@man.com","objectType":"person"},
    "id":"04fb5d8b-b4a1-48b9-8469-789b026baddb"}
    */
    public NativeObject buildActivity() {
        NativeObject nativeObject = new NativeObject();

        // set published element
        nativeObject.put("published", nativeObject, this.event.getTimestamp());

        // set actor object
        NativeObject actor = new NativeObject();
        actor.put("id", actor, event.getSender());
        actor.put("objectType", actor, "person");
        nativeObject.put("actor", nativeObject, actor);

        // set payload object
        NativeObject payload = new NativeObject();
        payload.put("title", payload, this.event.getMessageTitle());
        payload.put("content", payload, this.event.getMessageBody());
        payload.put("category", payload, this.event.getCategory());
        nativeObject.put("object", nativeObject, payload);

        // set verb
        nativeObject.put("verb", nativeObject, "post");

        //set target
        NativeObject target = new NativeObject();
        target.put("id", target, this.event.getTarget());
        nativeObject.put("target", nativeObject, target);

        return nativeObject;
    }


}
