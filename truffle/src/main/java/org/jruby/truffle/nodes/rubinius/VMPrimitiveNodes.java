/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.rubinius;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.core.KernelNodes;
import org.jruby.truffle.nodes.core.KernelNodesFactory;
import org.jruby.truffle.nodes.objects.ClassNode;
import org.jruby.truffle.nodes.objects.ClassNodeFactory;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.signal.ProcSignalHandler;
import org.jruby.truffle.runtime.signal.SignalOperations;

import sun.misc.Signal;

/**
 * Rubinius primitives associated with the VM.
 */
public abstract class VMPrimitiveNodes {

    @RubiniusPrimitive(name = "vm_gc_start", needsSelf = false)
    public static abstract class VMGCStartPrimitiveNode extends RubiniusPrimitiveNode {

        public VMGCStartPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public VMGCStartPrimitiveNode(VMGCStartPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public RubyNilClass vmGCStart() {
            final RubyThread runningThread = getContext().getThreadManager().leaveGlobalLock();

            try {
                System.gc();
            } finally {
                getContext().getThreadManager().enterGlobalLock(runningThread);
            }

            return getContext().getCoreLibrary().getNilObject();
        }

    }

    @RubiniusPrimitive(name = "vm_get_module_name", needsSelf = false)
    public static abstract class VMGetModuleNamePrimitiveNode extends RubiniusPrimitiveNode {

        public VMGetModuleNamePrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public VMGetModuleNamePrimitiveNode(VMGetModuleNamePrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public RubyString vmGetModuleName(RubyModule module) {
            notDesignedForCompilation();
            return getContext().makeString(module.getName());
        }

    }

    @RubiniusPrimitive(name = "vm_object_class", needsSelf = false)
    public static abstract class VMObjectClassPrimitiveNode extends RubiniusPrimitiveNode {

        @Child private ClassNode classNode;

        public VMObjectClassPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            classNode = ClassNodeFactory.create(context, sourceSection, null);
        }

        public VMObjectClassPrimitiveNode(VMObjectClassPrimitiveNode prev) {
            super(prev);
            classNode = prev.classNode;
        }

        @Specialization
        public RubyClass vmObjectClass(Object object) {
            return classNode.executeGetClass(object);
        }

    }

    @RubiniusPrimitive(name = "vm_object_equal", needsSelf = false)
    public static abstract class VMObjectEqualPrimitiveNode extends RubiniusPrimitiveNode {

        public VMObjectEqualPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public VMObjectEqualPrimitiveNode(VMObjectEqualPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public Object vmObjectEqual(boolean a, boolean b) {
            return a == b;
        }

        @Specialization
        public Object vmObjectEqual(int a, int b) {
            return a == b;
        }

        @Specialization
        public Object vmObjectEqual(long a, long b) {
            return a == b;
        }

        @Specialization
        public Object vmObjectEqual(double a, double b) {
            return a == b;
        }

        @Specialization
        public Object vmObjectEqual(RubyBasicObject a, RubyBasicObject b) {
            return a == b;
        }

    }

    @RubiniusPrimitive(name = "vm_object_kind_of", needsSelf = false)
    public static abstract class VMObjectKindOfPrimitiveNode extends RubiniusPrimitiveNode {

        @Child private KernelNodes.IsANode isANode;

        public VMObjectKindOfPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            isANode = KernelNodesFactory.IsANodeFactory.create(context, sourceSection, new RubyNode[] { null, null });
        }

        public VMObjectKindOfPrimitiveNode(VMObjectKindOfPrimitiveNode prev) {
            super(prev);
            isANode = prev.isANode;
        }

        @Specialization
        public boolean vmObjectKindOf(VirtualFrame frame, Object object, RubyModule rubyClass) {
            return isANode.executeIsA(frame, object, rubyClass);
        }

    }

    @RubiniusPrimitive(name = "vm_object_respond_to", needsSelf = false)
    public static abstract class VMObjectRespondToPrimitiveNode extends RubiniusPrimitiveNode {

        @Child private KernelNodes.RespondToNode respondToNode;

        public VMObjectRespondToPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            respondToNode = KernelNodesFactory.RespondToNodeFactory.create(context, sourceSection, new RubyNode[] { null, null, null });
        }

        public VMObjectRespondToPrimitiveNode(VMObjectRespondToPrimitiveNode prev) {
            super(prev);
            respondToNode = prev.respondToNode;
        }

        @Specialization
        public boolean vmObjectRespondTo(VirtualFrame frame, Object object, Object name, boolean includePrivate) {
            return respondToNode.executeDoesRespondTo(frame, object, name, includePrivate);
        }

    }

    @RubiniusPrimitive(name = "vm_object_singleton_class", needsSelf = false)
    public static abstract class VMObjectSingletonClassPrimitiveNode extends RubiniusPrimitiveNode {

        @Child private KernelNodes.SingletonClassMethodNode singletonClassNode;

        public VMObjectSingletonClassPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            singletonClassNode = KernelNodesFactory.SingletonClassMethodNodeFactory.create(context, sourceSection, new RubyNode[] { null });
        }

        public VMObjectSingletonClassPrimitiveNode(VMObjectSingletonClassPrimitiveNode prev) {
            super(prev);
            singletonClassNode = prev.singletonClassNode;
        }

        @Specialization
        public Object vmObjectClass(VirtualFrame frame, Object object) {
            return singletonClassNode.singletonClass(frame, object);
        }

    }

    @RubiniusPrimitive(name = "vm_set_module_name", needsSelf = false)
    public static abstract class VMSetModuleNamePrimitiveNode extends RubiniusPrimitiveNode {

        public VMSetModuleNamePrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public VMSetModuleNamePrimitiveNode(VMSetModuleNamePrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public Object vmSetModuleName(Object object) {
            throw new UnsupportedOperationException("vm_set_module_name");
        }

    }

    @RubiniusPrimitive(name = "vm_singleton_class_object", needsSelf = false)
    public static abstract class VMObjectSingletonClassObjectPrimitiveNode extends RubiniusPrimitiveNode {

        public VMObjectSingletonClassObjectPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public VMObjectSingletonClassObjectPrimitiveNode(VMObjectSingletonClassObjectPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public Object vmSingletonClassObject(Object object) {
            notDesignedForCompilation();
            return object instanceof RubyClass && ((RubyClass) object).isSingleton();
        }

    }

    @SuppressWarnings("restriction")
    @RubiniusPrimitive(name = "vm_watch_signal", needsSelf = false)
    public static abstract class VMWatchSignalPrimitiveNode extends RubiniusPrimitiveNode {

        public VMWatchSignalPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public VMWatchSignalPrimitiveNode(VMWatchSignalPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public boolean watchSignal(RubyString signalName, RubyString action) {
            if (!action.toString().equals("DEFAULT")) {
                throw new UnsupportedOperationException();
            }

            Signal signal = new Signal(signalName.toString());

            SignalOperations.watchDefaultForSignal(signal);
            return true;
        }

        @Specialization
        public boolean watchSignal(RubyString signalName, RubyNilClass ignore) {
            Signal signal = new Signal(signalName.toString());

            SignalOperations.watchSignal(signal, SignalOperations.IGNORE_HANDLER);
            return true;
        }

        @Specialization
        public boolean watchSignal(RubyString signalName, RubyProc proc) {
            Signal signal = new Signal(signalName.toString());

            SignalOperations.watchSignal(signal, new ProcSignalHandler(getContext(), proc));
            return true;
        }

    }

}
