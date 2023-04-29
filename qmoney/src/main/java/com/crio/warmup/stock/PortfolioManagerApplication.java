package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  private static final String TOKEN = "3fb87c54073502ec9ad7ece9f993af461f3e66ec";

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Task:
  // - Read the json file provided in the argument[0], The file is available in the classpath.
  // - Go through all of the trades in the given file,
  // - Prepare the list of all symbols a portfolio has.
  // - if "trades.json" has trades like
  // [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  // Then you should return ["MSFT", "AAPL", "GOOGL"]
  // Hints:
  // 1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  // Check if they are of any help to you.
  // 2. Return the list of all symbols in the same order as provided in json.

  // Note:
  // 1. There can be few unused imports, you will need to fix them to make the build pass.
  // 2. You can use "./gradlew build" to check if your code builds successfully.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File inputFile = resolveFileFromResources(args[0]);

    ObjectMapper om = getObjectMapper();

    PortfolioTrade[] pt = om.readValue(inputFile, PortfolioTrade[].class);

    // list of symbols
    List<String> symbols = new ArrayList<>();

    // add all symbols to list
    for (PortfolioTrade p : pt) {
      symbols.add(p.getSymbol());
    }

    return symbols;
  }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    File path = Paths
        .get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();

    return path;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Follow the instructions provided in the task documentation and fill up the correct values for
  // the variables provided. First value is provided for your reference.
  // A. Put a breakpoint on the first line inside mainReadFile() which says
  // return Collections.emptyList();
  // B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  // following the instructions to run the test.
  // Once you are able to run the test, perform following tasks and record the output as a
  // String in the function below.
  // Use this link to see how to evaluate expressions -
  // https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  // 1. evaluate the value of "args[0]" and set the value
  // to the variable named valueOfArgument0 (This is implemented for your reference.)
  // 2. In the same window, evaluate the value of expression below and set it
  // to resultOfResolveFilePathArgs0
  // expression ==> resolveFileFromResources(args[0])
  // 3. In the same window, evaluate the value of expression below and set it
  // to toStringOfObjectMapper.
  // You might see some garbage numbers in the output. Dont worry, its expected.
  // expression ==> getObjectMapper().toString()
  // 4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  // second place from top to variable functionNameFromTestFileInStackTrace
  // 5. In the same window, you will see the line number of the function in the stack trace window.
  // assign the same to lineNumberFromTestFileInStackTrace
  // Once you are done with above, just run the corresponding test and
  // make sure its working as expected. use below command to do the same.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 =
        "/home/crio-user/workspace/abkn9977-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
    String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "29:1";


    return Arrays.asList(
        new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.
  // TODO: CRIO_TASK_MODULE_REST_API
  // Find out the closing price of each stock on the end_date and return the list
  // of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    List<String> symbols = new ArrayList<>();

    if (args.length == 0 || args.equals(null))
      return symbols;

    // fetch the deserialized data usin readTradesFromJson method
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);

    if (trades.isEmpty())
      return symbols;

    // parse endDate to LocalDate object
    LocalDate date = LocalDate.parse(args[1]);

    List<TotalReturnsDto> tiingoResult = new ArrayList<>();

    RestTemplate rt = new RestTemplate();

    for (PortfolioTrade trade : trades) {
      // String url = prepareUrl(trade, date, "3fb87c54073502ec9ad7ece9f993af461f3e66ec");
      String url = prepareUrl(trade, date, getToken());

      TiingoCandle[] tc = rt.getForObject(url, TiingoCandle[].class);

      if (tc.length > 0) {
        TiingoCandle endDateQuote = tc[tc.length - 1];
        tiingoResult.add(new TotalReturnsDto(trade.getSymbol(), endDateQuote.getClose()));
      }

      // if tiingo retuns an empty array of objects
      else
        throw new RuntimeException();

    }

    // sort trades according to closing price
    Collections.sort(tiingoResult, (f, s) -> f.getClosingPrice().compareTo(s.getClosingPrice()));

    // get symbols
    symbols = tiingoResult.stream().map(q -> q.getSymbol()).collect(Collectors.toList());

    return symbols;
  }

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    // list of symbols
    List<PortfolioTrade> symbols = new ArrayList<>();

    if (filename.isEmpty())
      return symbols;

    File inputFile = resolveFileFromResources(filename);

    ObjectMapper om = getObjectMapper();

    PortfolioTrade[] pt = om.readValue(inputFile, PortfolioTrade[].class);

    // add all symbols to list
    for (PortfolioTrade p : pt) {
      symbols.add(p);
    }

    return symbols;
  }

  // TODO:
  // Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    // return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" +
    // endDate +"&endDate=" + endDate + "&token=" + token;

    return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
        + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  // for the stocks provided in the Json.
  // Use the function you just wrote #calculateAnnualizedReturns.
  // Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.



  // TODO:
  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Candle startDate = candles.get(0);
    return startDate.getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }

  public static String getToken() {
    return TOKEN;
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    List<Candle> candles = new ArrayList<>();

    if (trade == null)
      return candles;

    // initialize rest template to make api call
    RestTemplate rt = new RestTemplate();

    String url = prepareUrl(trade, endDate, token);

    TiingoCandle[] tCandles = rt.getForObject(url, TiingoCandle[].class);

    for (TiingoCandle tc : tCandles)
      candles.add(tc);

    return candles;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    List<AnnualizedReturn> annualReturn = new ArrayList<>();

    if (args == null || args.length == 0)
      return annualReturn;

    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    LocalDate endDate = LocalDate.parse(args[1]);

    if (trades.isEmpty())
      return annualReturn;

    for (PortfolioTrade trd : trades) {
      List<Candle> trdCandles = fetchCandles(trd, endDate, getToken());
      Double buyPrice = getOpeningPriceOnStartDate(trdCandles);
      Double sellPrice = getClosingPriceOnEndDate(trdCandles);

      AnnualizedReturn trdAnnualReturn =
          calculateAnnualizedReturns(endDate, trd, buyPrice, sellPrice);

      annualReturn.add(trdAnnualReturn);
    }

    Collections.sort(annualReturn, (a, b) -> b.getAnnualizedReturn().compareTo(a.getAnnualizedReturn()));

    return annualReturn;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Return the populated list of AnnualizedReturn for all stocks.
  // Annualized returns should be calculated in two steps:
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // 1.1 Store the same as totalReturns
  // 2. Calculate extrapolated annualized returns by scaling the same in years span.
  // The formula is:
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // 2.1 Store the same as annualized_returns
  // Test the same using below specified command. The build should be successful.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {

    Double totalReturn = (sellPrice - buyPrice) / buyPrice;

    // Double totalYears = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) /
    // (double) 365;
    Double totalYears = trade.getPurchaseDate().until(endDate, ChronoUnit.DAYS) / 365.24;

    Double annualizedReturn = Math.pow(1 + totalReturn, 1 / totalYears) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
      //list of annual returns
      List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

      if(args.length == 0 || args == null)
        return annualizedReturns;

      List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);

      LocalDate endDate = LocalDate.parse(args[1]);

      //get portfolio manager
      PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
    
      return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    List<String> l = new ArrayList(Arrays.asList("one", "two"));

    Stream<String> sl = l.stream();

    l.add("three");

    String s = sl.collect(Collectors.joining(" "));

    System.out.println(s);

    //printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}