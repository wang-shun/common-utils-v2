/**
 * Copyright ?2003, TopCoder, Inc. All rights reserved
 */
package com.youzan.sz.jutil.util;

/**
 * Wrapper class to override the equals method of an object so that we can use a HashSet to keep track of objects
 * with different references just like the IdentityHashMap in Java 1.4
 *
 * @author meteorchen
 * @version 1.0
 */
public final class ObjectWrapper {

    /** The wrapped object */
    Object obj;
    
    /**
     * Wraps the object
     *
     * @param o object to be wrapped
     */
    public ObjectWrapper(Object o) {
        obj = o;
    }
    
    /**
     * Overrides theequals method of the wrapped object
     *
     * @param o the other wrapper we are to compare this object to
     *
     * @return <code>true</code> if both wrapped objects point to the same thing, <code>false</code> otherwise
     */
    public boolean equals(Object o) {
        if (o instanceof ObjectWrapper) {
            return obj == ((ObjectWrapper) o).obj;
        }
        
        return false;
    }
    
    /**
     * Use the maintain the wrapped object's hashcode
     *
     * @return hashcode of the wrapped object
     */
    public int hashCode() {
        return obj.hashCode();
    }
}

