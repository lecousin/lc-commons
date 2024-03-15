package net.lecousin.commons.executors;

/** Simple marker interface, when the class LcExecutors is loaded, it will
 * use the ServiceLoader mechanism on this interface to allow initialization
 * and implementations to set default executors.
 */
public interface LcExecutorInitializer {

}
