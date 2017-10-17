package env;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ErrorHandler;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverUtil {
    public static long DEFAULT_WAIT = 20;
    protected static WebDriver driver=null;

    public static WebDriver getDefaultDriver() {
		if (driver != null) {
			return driver;
		}
		
		String enviroment = "desktop";
		String platform = "";
		String config = System.getProperty("config", "");
		
		if(!config.isEmpty())
		{
			enviroment = config.split("_")[0].toLowerCase();
			platform = config.split("_")[1].toLowerCase();
		}
		
		switch(enviroment)
		{
			case "local": if(platform.equals("android"))
							  return androidDriver();
						  else if(platform.equals("ios"))
							  return iosDriver();
						  else{
							  System.out.println("unsupported platform");
							  System.exit(0);
						  }
			
			case "android": return androidDriver(); 
			case "ios": return iosDriver();
			case "desktop":break;
		}
        //System.setProperty("webdriver.chrome.driver", "webdrivers/chromedriver.exe");
        //System.setProperty("webdriver.gecko.driver", "./geckodriver");
        DesiredCapabilities capabilities = null;
		capabilities = DesiredCapabilities.firefox();
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability("takesScreenshot", true);
        driver = chooseDriver(capabilities);
        driver.manage().timeouts().setScriptTimeout(DEFAULT_WAIT, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        return driver;
    }

    private static WebDriver androidDriver()
    {
    	DesiredCapabilities capabilities = DesiredCapabilities.android();
    	capabilities.setCapability("deviceName", "");
		capabilities.setCapability("platformName", "android");
		capabilities.setCapability(CapabilityType.VERSION, "");
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "chrome");
		capabilities.setCapability("udid", "");
		String port = "4723";	
		try {
			driver = new AndroidDriver(new URL("http://127.0.0.1:"+port+"/wd/hub"),capabilities);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return driver;
    }
    
    private static WebDriver iosDriver()
    {
    	DesiredCapabilities capabilities = DesiredCapabilities.android();
    	capabilities.setCapability("deviceName", "");
		capabilities.setCapability("platformName", "iOS");
		capabilities.setCapability(CapabilityType.VERSION, "");
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "safari");
		capabilities.setCapability("udid", "");
		String port = "4723";	
		try {
			driver = new IOSDriver(new URL("http://127.0.0.1:"+port+"/wd/hub"),capabilities);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return driver;
    }
    
    /**
     * By default to web driver will be firefox
     *
     * Override it by passing -Dbrowser=Chrome to the command line arguments
     * @param capabilities
     * @return webdriver
     */
    private static WebDriver chooseDriver(DesiredCapabilities capabilities) {
		String preferredDriver = System.getProperty("browser", "Firefox");
		boolean headless = System.getProperty("headless", "false").equals("true");
		
		switch (preferredDriver.toLowerCase()) {
			case "safari":
				try {
					driver = new SafariDriver();
				}catch(Exception e) {
					System.out.println(e.getMessage());
					System.exit(0);
				}
				return driver;
			case "edge":
				try {
					driver = new EdgeDriver();
				}catch(Exception e) {
					System.out.println(e.getMessage());
					System.exit(0);
				}
				return driver;
			case "chrome":
				final ChromeOptions chromeOptions = new ChromeOptions();
				if (headless) {
					chromeOptions.addArguments("--headless");
				}
				capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
				try
				{
					driver = new ChromeDriver(capabilities);
					ErrorHandler handler = new ErrorHandler();
					handler.setIncludeServerErrors(false);
					//driver.setErrorHandler(handler);
				}catch(Exception e) {
					System.out.println(e.getMessage());
					System.exit(0);
				}
				return driver;
			case "PhantomJS":
				return new PhantomJSDriver(capabilities);
			default:
				FirefoxOptions options = new FirefoxOptions();
				if (headless) {
					options.addArguments("-headless", "-safe-mode");
				}
				capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
				try {
					driver = new FirefoxDriver(capabilities);
				}
				catch(Exception e) {
					System.out.println(e.getMessage());
					System.exit(0);
				}
				return driver;
		}
    }

    public static WebElement waitAndGetElementByCssSelector(WebDriver driver, String selector,
                                                            int seconds) {
        By selection = By.cssSelector(selector);
        return (new WebDriverWait(driver, seconds)).until( // ensure element is visible!
                ExpectedConditions.visibilityOfElementLocated(selection));
    }

	public static void closeDriver() {
		if (driver != null) {
			try {
				driver.close();
				driver.quit(); // fails in current geckodriver! TODO: Fixme
			} catch (NoSuchMethodError nsme) { // in case quit fails
			} catch (NoSuchSessionException nsse) { // in case close fails
			} catch (SessionNotCreatedException snce) {} // in case close fails
			driver = null;
		}
	}
}
