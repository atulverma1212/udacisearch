package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final static Logger logger = Logger.getLogger(ConfigurationLoader.class.toString());

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toString()))){
      return read(bufferedReader);
    } catch (IOException e) {
      logger.severe("Error while loading file: " + e.getMessage());
    }

    return new CrawlerConfiguration.Builder().build();
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    logger.info("Reading config file");
    CrawlerConfiguration.Builder builder = new CrawlerConfiguration.Builder();
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
      builder = mapper.readValue(reader, CrawlerConfiguration.Builder.class);
    } catch (IOException e) {
      logger.severe("Error while reading config file: " + e.getMessage());
    }

    return builder.build();
  }
}
