package com.udacity.webcrawler.profiler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;
  private final static Logger logger = Logger.getLogger(ProfilerImpl.class.getName());

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    // TODO: Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
    //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.

    boolean isValidInterface = false;
    for (Method method : klass.getMethods()) {
      if (method.isAnnotationPresent(Profiled.class)) {
        isValidInterface = true;
        break;
      }
    }
    if (!isValidInterface) {
      throw new IllegalArgumentException(klass.getName() + " interface does not contain any profiled method");
    }
    InvocationHandler handler = new ProfilingMethodInterceptor<>(clock, state, delegate);
    Object proxy =
            Proxy.newProxyInstance(
                    ProfilerImpl.class.getClassLoader(),
                    new Class<?>[]{klass},
                    handler);
    return (T) proxy;
//    return delegate;
  }

  @Override
  public void writeData(Path path) {
    // TODO: Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.
    logger.info("Initialized writing Profiling data to Path Provided: " + path.toString());
    try (FileWriter writer = new FileWriter(path.toString(), true)){
      state.write(writer);
    } catch (IOException ex) {
      logger.severe("Error while writing the results to path provided: " + ex.getMessage());
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    logger.info("writeData ---------------");
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
