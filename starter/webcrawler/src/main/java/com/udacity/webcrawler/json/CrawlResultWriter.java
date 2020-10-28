package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
  private final CrawlResult result;
  private final static Logger logger = Logger.getLogger(CrawlResultWriter.class.getName());

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   */
  public void write(Path path) {
    logger.info("Initialized writing for Crawl Results to Path Provided: " + path.toString());
    ObjectMapper mapper = new ObjectMapper();
    try (FileWriter writer = new FileWriter(path.toString(), true)){
      mapper.writeValue(new BufferedWriter(writer), this.result);
    } catch (IOException ex) {
      logger.severe("Error while writing the results to path provided: " + ex.getMessage());
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   */
  public void write(Writer writer) {
    logger.info("Initialized writing for Crawl Results to writer provided");
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    try {
      mapper.writeValue(writer, this.result);
    } catch (IOException ex) {
      logger.severe("Error while writing the results to writer provided: " + ex.getMessage());
    }
  }
}
