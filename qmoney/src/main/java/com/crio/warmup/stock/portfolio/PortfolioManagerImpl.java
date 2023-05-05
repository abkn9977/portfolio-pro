
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.net.URI;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  private RestTemplate restTemplate;
  private static final String TOKEN = "289464e8faf5cf34aba42001442fb59b3c854b6c";

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  // TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    List<AnnualizedReturn> annualReturn = new ArrayList<>();

    if (portfolioTrades == null || portfolioTrades.isEmpty())
      return annualReturn;

    annualReturn = portfolioTrades.stream().map((trade) -> {
      List<Candle> candles;
      Double buyValue = 0.0;
      Double sellValue = 0.0;
      Double annualizedReturn = 0.0;
      Double totalReturns = 0.0;
      try {
        candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
        buyValue = candles.get(0).getOpen();
        sellValue = candles.get(candles.size() - 1).getClose();
        totalReturns = (sellValue - buyValue) / buyValue;
        Double totalYears = trade.getPurchaseDate().until(endDate, ChronoUnit.DAYS) / 365.24;
        annualizedReturn = Math.pow(1 + totalReturns, 1 / totalYears) - 1;
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);

    }).sorted(getComparator()).collect(Collectors.toList());

    return annualReturn;
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    String URI = buildUri(symbol, from, to);
    List<Candle> candles = new ArrayList<>();
    TiingoCandle[] tiingoCandles = restTemplate.getForObject(URI, TiingoCandle[].class);
    candles = Arrays.asList(tiingoCandles);
    return candles;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uri = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
        + startDate + "&endDate=" + endDate + "&token=" + TOKEN;

    return uri;
  }
}
