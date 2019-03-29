package com.creditease.ns4.gear.watchdog.agent;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class AgentTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] transformed = null;
        System.out.println("Transforming " + className);
        ClassPool pool = null;
        CtClass cl = null;
        try {
            pool = ClassPool.getDefault();
            cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

//            CtMethod aop_method = pool.get("com.jdktest.instrument.AopMethods").
//                    getDeclaredMethod("aopMethod");
//            System.out.println(aop_method.getLongName());

            CodeConverter convert = new CodeConverter();
            if (cl.isInterface() == false) {
                CtMethod[] methods = cl.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].isEmpty() == false) {
                        insertMethod(methods[i]);
                    }
                }
                transformed = cl.toBytecode();
            }
        } catch (Exception e) {
            System.err.println("Could not instrument  " + className
                    + ",  exception : " + e.getMessage());
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }
        return transformed;
    }

    private void insertMethod(CtMethod method) throws NotFoundException, CannotCompileException {
        //situation 1:添加监控时间
        method.instrument(new ExprEditor() {
            public void edit(MethodCall m) throws CannotCompileException {
                m.replace("{ long stime = System.currentTimeMillis(); $_ = $proceed($$);System.out.println(\""
                        + m.getClassName() + "." + m.getMethodName()
                        + " cost:\" + (System.currentTimeMillis() - stime) + \" ms\");}");
            }
        });
        //situation 2:在方法体前后语句
        //method.insertBefore("System.out.println(\"enter method\");");
        //method.insertAfter("System.out.println(\"leave method\");");
    }
}
