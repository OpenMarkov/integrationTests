package org.openmarkov.safeCloning;

import org.apache.commons.lang3.SerializationUtils;

public class Test {
    
    
    public static void main(String[] args) {
        var original = new Child("a", "b", "c", 1, 2, 3);
        var cloned = SerializationUtils.clone(original);
        cloned = cloned;
    }
    
    
    static class Child extends Parent implements AutoCloneable {
        final String a;
        final String b;
        final String c;
        final int aInt;
        final int bInt;
        final int cInt;
        
        public Child(String a, String b, String c, int aInt, int bInt, int cInt) {
            super("parent " + a, "parent " + b, "parent " + c, aInt + 100, bInt + 100, cInt + 100);
            this.a = a;
            this.b = b;
            this.c = c;
            this.aInt = aInt;
            this.bInt = bInt;
            this.cInt = cInt;
        }
    }
    
    static class Parent implements AutoCloneable {
        final String pa;
        final String pb;
        final String pc;
        final int paInt;
        final int pbInt;
        final int pcInt;
        
        public Parent(String pa, String pb, String pc, int paInt, int pbInt, int pcInt) {
            this.pa = pa;
            this.pb = pb;
            this.pc = pc;
            this.paInt = paInt;
            this.pbInt = pbInt;
            this.pcInt = pcInt;
        }
    }
    
    static class NotCloneableClass {
        final double a;
        final double b;
        final double c;
        
        NotCloneableClass(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
    
}
