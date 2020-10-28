package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final PageParserFactory parserFactory;
  private final int depth;
  private final List<Pattern> urlListToBeIgnored;
  private Instant endTime;

  private final ConcurrentSkipListSet<String> urlsVisited = new ConcurrentSkipListSet<>();
  private final ConcurrentHashMap<String, Integer> urlCountMap = new ConcurrentHashMap<>();

  @Inject
  ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          PageParserFactory pageParserFactory,
          @MaxDepth int maxDepth,
          @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.parserFactory = pageParserFactory;
    this.depth = maxDepth;
    this.urlListToBeIgnored = ignoredUrls;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    endTime = clock.instant().plus(timeout);
    for(String url : startingUrls) {
      if (isUrlVisited(url) || shouldIgnore(url))
        continue;
      pool.invoke(new CrawlTask(url, depth));
    }
    Map<String, Integer> sortedWordCount = urlCountMap.isEmpty() ? urlCountMap : WordCounts.sort(urlCountMap, popularWordCount);
    return new CrawlResult.Builder()
            .setWordCounts(sortedWordCount)
            .setUrlsVisited(urlsVisited.size())
            .build();
  }

  private boolean shouldIgnore(String url) {
    return urlListToBeIgnored.stream().anyMatch(e -> e.matcher(url).matches());
  }

  private boolean isUrlVisited(String url) {
    return urlsVisited.contains(url);
  }

//  synchronized
//  private void addToVisitedUrls(String url) {
//    urlsVisited.add(url);
//  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }

  private class CrawlTask extends RecursiveTask<Void> {

    private final String url;
    private final int crawlTaskDepth;

    public CrawlTask(String url, int depth) {
      this.url = url;
      this.crawlTaskDepth = depth;
    }

    @Override
    protected Void compute() {
      if(crawlTaskDepth == 0 || clock.instant().isAfter(endTime))
        return null;
      urlsVisited.add(url);
      PageParser.Result parseResult = parserFactory.get(url).parse();
      parseResult.getWordCounts().forEach((key, value) ->
              urlCountMap.put(key, urlCountMap.getOrDefault(key, 0) + value));
      for(String url : parseResult.getLinks()) {
        if (isUrlVisited(url) || shouldIgnore(url))
          continue;
        pool.invoke(new CrawlTask(url, crawlTaskDepth -1));
      }
      return null;
    }
  }
}
