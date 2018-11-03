package Worker;

import Enum.ButtonSelector;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Worker {
    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    private static final String COINS_AMOUNT_CSS = "span[id='CurrencyAmount']";
    private static final String TWEETS_CSS = "ol[id='stream-items-id'] li[class*='stream-item'] div[class*='tweet-text-container']";

    private static final String TWITTER_URL = "https://twitter.com/twitchfollows2";
    private static final String SITE_URL = "http://twitchfollows.com";
    private static final String VIEW_SITE = SITE_URL + "/view";
    private static final String FOLLOW_SITE = SITE_URL + "/follow";

    //TODO: config file for paths plugins/chromedriver/profile
    // files needed are under src/main/resources
    private static final String FILES_PATH = "src/main/resources";
    private static final String PLUGIN_UBLOCK_PATH = FILES_PATH + "/ublock.crx";
    private static final String CHROMEDRIVER_PATH = FILES_PATH + "/chromedriver.exe";

    private static final int WAIT_TIMEOUT = 10;

    private String viewHandler;
    private String followHandler;
    private String twitterHandler;
    private String tweetText = "";
    private WebDriver driver;
    private WebDriverWait wait;

    private void configureDriver() {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);

        String userDir = System.getProperty("user.dir");

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addExtensions(new File(PLUGIN_UBLOCK_PATH));
        chromeOptions.addArguments("--user-data-dir=" + userDir + "/profile");

        this.driver = new ChromeDriver(chromeOptions);
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    public void start() throws Exception {
        showWelcomeInfo();
        configureDriver();

        driver.get(TWITTER_URL);

        twitterHandler = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('" + SITE_URL + "');");

        showLoginInfo();

        while (driver.getWindowHandles().size() > 1) {
            driverWait(5);
        }

        LOG.info("Initializing sites");
        ((JavascriptExecutor) driver).executeScript("window.open('" + VIEW_SITE + "');");
        driverWait(5);
        ((JavascriptExecutor) driver).executeScript("window.open('" + FOLLOW_SITE + "');");

        configureHandlers();

        switchToTab(twitterHandler);

        completeOffers();
    }

    private void configureHandlers() {
        ArrayList<String> handlerList = new ArrayList<>(driver.getWindowHandles());
        String twHandler = handlerList.get(0);
        String folHandler = handlerList.get(1);
        String vHandler = handlerList.get(2);

        this.followHandler = folHandler;
        this.twitterHandler = twHandler;
        this.viewHandler = vHandler;
    }

    private void completeOffers() {
        WebElement element;
        while (true) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(TWEETS_CSS)));
                element = driver.findElements(By.cssSelector(TWEETS_CSS)).get(1);

                if (element != null) {
                    if (!tweetText.equals(element.getText())) {
                        tweetText = element.getText();
                        reactToNewOffer(tweetText);
                    }
                }
            } catch (Exception e) {
                LOG.error("Could not fetch tweet from Twitter.");
            }
            try {
                driverWait(4);
                driver.navigate().refresh();
            } catch (Exception e) {
                LOG.warn("Could not refresh Twitter.");
            }
        }
    }

    private void reactToNewOffer(String textToCheck) {
        if (textToCheck.contains("New offer") && textToCheck.contains("view")) {
            LOG.info("Found new view offer: " + textToCheck);
            switchToTab(viewHandler);
            driver.navigate().refresh();
            waitThenClick(ButtonSelector.VIEW, viewHandler);
            switchToTab(twitterHandler);
        } else if (textToCheck.contains("New offer") && textToCheck.contains("follow")) {
            LOG.info("Found new follow offer: " + textToCheck);
            switchToTab(followHandler);
            driver.navigate().refresh();
            waitThenClick(ButtonSelector.FOLLOW, followHandler);
            switchToTab(twitterHandler);
        } else {
            LOG.info("Tweet: " + textToCheck + " found, but it is not a follow/view offer.");
        }
    }

    private void switchToTab(String tabHandler) {
        Set<String> tabs = driver.getWindowHandles();
        for (String tab : tabs) {
            if (tab.contains(tabHandler)) {
                driver.switchTo().window(tab);
                return;
            }
        }
        LOG.warn("No proper tab found for " + tabHandler);
    }

    private void clickOffer(ButtonSelector selector, String handler) {
        clickOffer(selector, handler, false);
    }

    private void clickOffer(ButtonSelector selector, String handler, boolean retry) {
        List<WebElement> offers = driver.findElements(By.cssSelector(selector.getSelector()));
        if (offers.size() > 1) {
            LOG.info("More offers found.");
        }
        for (WebElement offer : offers) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(offer));
                offer.click();
                driverWait(1);
                switchToTab(handler);
                LOG.info("Clicked offer.");
            } catch (Exception e) {
                LOG.info("Could not click offer.");
            }
        }

        try {
            List<WebElement> offersNotClicked = driver.findElements(By.cssSelector(selector.getSelector()));
            if (offersNotClicked.size() > 0 && !retry) {
                LOG.info("Offers still not clicked, retrying.");
                clickOffer(selector, handler, true);
            }
            if (offersNotClicked.size() > 0 && retry) {
                LOG.warn("Offers still not clicked after retrying.");
            }
        } catch (Exception e) {

        }

        logCurrencyAmount();
    }

    private void logCurrencyAmount() {
        try {
            String currencyAmount = driver.findElement(By.cssSelector(COINS_AMOUNT_CSS)).getText();
            LOG.info("Coins amount: " + currencyAmount);
        } catch (Exception e) {
            LOG.warn("Could not fetch coins amount");
        }
    }

    private void driverWait(int seconds) throws Exception {
        synchronized (driver) {
            driver.wait(seconds * 1000);
        }
    }

    private void cleanup() throws Exception {
        cleanup(false);
    }

    private void cleanup(boolean refreshed) throws Exception {
        Set<String> tabs = driver.getWindowHandles();
        if (tabs.size() > 3) {
            driverWait(6);
            for (String tab : tabs) {
                switchToTab(tab);
                driverWait(1);
                if (!refreshed && driver.getCurrentUrl().contains("view?offer")) {
                    driver.navigate().refresh();
                    cleanup(true);
                } else if (driver.getCurrentUrl().contains("twitch.tv")
                        || (refreshed && driver.getCurrentUrl().contains("view?offer"))) {
                    driverWait(4);
                    LOG.info("closing " + driver.getCurrentUrl());
                    driver.close();
                    driverWait(1);
                }

            }
        }
    }

    private void waitThenClick(ButtonSelector selector, String handler) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector.getSelector())));
            clickOffer(selector, handler);
            cleanup();
        } catch (Exception e) {
            LOG.info("Offer not found on site.");
        }
    }

    private void showWelcomeInfo() {
        LOG.info("TWTICHFOLLOWS BOT BY PAWLOSEK");
        LOG.info("Contact: pawlosek1@gmail.com / wojaczek.paw@gmail.com");
    }

    private void showLoginInfo() {
        LOG.info("Log in to your account and close the twitchfollows tab.");
    }
}
