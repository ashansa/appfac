package org.wso2.carbon.appfactory.ext.authorization;

import java.security.BasicPermission;


public class AppFactorySecurityPermission extends BasicPermission {
    /**
     * Creates a new BasicPermission with the specified name.
     * Name is the symbolic name of the permission, such as
     * "setFactory",
     * "print.queueJob", or "topLevelWindow", etc.
     *
     * @param name the name of the BasicPermission.
     * @throws NullPointerException     if <code>name</code> is <code>null</code>.
     * @throws IllegalArgumentException if <code>name</code> is empty.
     */
    public AppFactorySecurityPermission(String name) {
        super(name);
    }
}
