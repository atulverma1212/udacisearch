package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static java.time.temporal.ChronoField.MILLI_OF_SECOND;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor<T> implements InvocationHandler {

  private static final Logger logger = Logger.getLogger(ProfilingMethodInterceptor.class.getName());

  private final Clock clock;
  private final ProfilingState state;
  private final T target;
  private final Map<String, Method> methods = new HashMap<>();

  // TODO: You will need to add more instance fields and constructor arguments to this class.
//  ProfilingMethodInterceptor(Clock clock) {
//    this.clock = Objects.requireNonNull(clock);
//  }

  public ProfilingMethodInterceptor(Clock clock, ProfilingState state, T delegate) {
    this.clock = Objects.requireNonNull(clock);
    this.state = state;
    this.target = delegate;
    for(Method method: target.getClass().getDeclaredMethods()) {
      this.methods.put(method.getName(), method);
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.isAnnotationPresent(Profiled.class)) {
      Instant startTime = clock.instant();
      Object result = null;
      try {
        Method method1 = methods.get(method.getName());
        method1.setAccessible(true);
        result = method1.invoke(target, args);
        method1.setAccessible(false);
      } catch (UndeclaredThrowableException e) {
        logger.severe(e.getClass().getSimpleName() + " occurred while invoking method: " + method.getName()
                + "Error: " + e.getMessage());
      } catch (InvocationTargetException e) {
        logger.severe(e.getClass().getSimpleName() + " occurred while invoking method: " + method.getName()
                + "Error: " + e.getMessage());
        throw e.getCause();
      } finally {
        Duration elapsedDuration = Duration.between(startTime, clock.instant());
        state.record(target.getClass(), method, elapsedDuration);
      }

      return result;
      // TODO: This method interceptor should inspect the called method to see if it is a profiled
      //       method. For profiled methods, the interceptor should record the start time, then
      //       invoke the method using the object that is being profiled. Finally, for profiled
      //       methods, the interceptor should record how long the method call took, using the
      //       ProfilingState methods.

    }
    return method.invoke(target, args);
  }
}
