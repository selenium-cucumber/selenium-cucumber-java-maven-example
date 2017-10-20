package env;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ErrorHandler;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverUtil {
    public static long DEFAULT_WAIT = 20;
    protected static WebDriver driver=null;
    static String currentPath = System.getProperty("user.dir");
    static Properties prop = new Properties();
    static DesiredCapabilities capability=null;
    
    public static void uploadAppToSauceLab(String appPath){
    	
    }
    
    public static DesiredCapabilities getCapability(InputStream input) {
    	DesiredCapabilities capability = new DesiredCapabilities();
    	try {
    		prop.load(input);
    		
    		// set app path for app testing
    		if(prop.containsKey("app")) {
    			String appName = prop.getProperty("app");
    			if(appName.contains("sauce-stroage")){
    				appName=appName.split(":")[1];
    				String appPath = currentPath+"/src/main/java/appUnderTest/"+appName;
    				uploadAppToSauceLab(appPath);
    			}
    			String appPath = currentPath+"/src/main/java/appUnderTest/"+appName;
   				
    			File appFile = new File(appPath);
   				if(appFile.exists()) {
   					prop.setProperty("app", appPath);
   				}else {
   					System.out.println("Exception : No app with name '"+appName+"' found in appUnderTest directory");
   					System.exit(0);
   				}
    		}
    		
    		// set capabilities
    		Enumeration<Object> enuKeys = prop.keys();
    		while (enuKeys.hasMoreElements()) {
    			String key = (String) enuKeys.nextElement();
    			String value = prop.getProperty(key);
    			capability.setCapability(key, value);
    		}
    		input.close();
    		prop.clear();
    	}catch(Exception e) {
    		e.printStackTrace();
			System.exit(0);
    	}
    	return capability;
    }
    
    public static WebDriver getDefaultDriver() {
		if (driver != null) {
			return driver;
		}
		
		String enviroment = "desktop";
		String platform = "";
		String config = System.getProperty("config", "");
		
		if(!config.isEmpty())
		{
			try
			{
				enviroment = config.split("_")[0].toLowerCase();
				platform = config.split("_")[1].toLowerCase();
			}
			catch(Exception e)
			{
				System.out.println("Exception : Invalid config file name "+config+".properties");
				System.out.println("Config file format should be : enviroment_platform_device.properties");
				System.out.println("E.g : local_android_nexus5.properties");
				System.out.println("E.g : local_ios_iphone6.properties");
				System.out.println("E.g : browserstack_android_nexus5.properties");
				System.out.println("E.g : saucelab_windows7_chrome.properties");
			}
			
			try {
				InputStream input = new FileInputStream(currentPath+"/src/main/java/browserConfigs/"+config+".properties");
				capability = getCapability(input);
			}catch (FileNotFoundException e) {
				System.out.println("\nException : Config file not found with name '"+config+".properties'");
				System.exit(0);
			}
		}
		
		switch(enviroment)
		{
			case "local": if(platform.equals("android"))
							  driver = androidDriver(capability);
						  else if(platform.equals("ios"))
							  driver =  iosDriver(capability);
						  else{
							  System.out.println("Exception : unsupported platform");
							  System.exit(0);
						  }
						  break;
			
			case "browserstack": driver = browserStackDriver(capability);
								 break;
			
			case "saucelab": driver = sauceLabDriver(capability);
			 					 break;

			case "desktop": DesiredCapabilities capabilities = null;
							capabilities = DesiredCapabilities.firefox();
					        capabilities.setJavascriptEnabled(true);
					        capabilities.setCapability("takesScreenshot", true);
					        driver = chooseDriver(capabilities);
					        driver.manage().timeouts().setScriptTimeout(DEFAULT_WAIT, TimeUnit.SECONDS);
					        driver.manage().window().maximize();
					        break;
					        
			default : System.out.println("Exception : Unsupported Enviroment");
					  System.exit(0);
		}
        return driver;
    }

    private static WebDriver sauceLabDriver(DesiredCapabilities capabilities) {
    	URL remoteDriverURL = null;
    	try {
	    	InputStream input = new FileInputStream(currentPath+"/src/main/java/platformConfigs/saucelab.properties");
			prop.load(input);
			
			String url = prop.getProperty("protocol")+
	    				 "://"+
	    				 prop.getProperty("username")+
	    				 ":"+
	    				 prop.getProperty("access_key")+
	    				 prop.getProperty("url");
			
			input.close();
			prop.clear();
	    	remoteDriverURL = new URL(url);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return new RemoteWebDriver(remoteDriverURL, capability);
    }
    
    private static WebDriver browserStackDriver(DesiredCapabilities capabilities) {
    	URL remoteDriverURL = null;
    	try {
	    	InputStream input = new FileInputStream(currentPath+"/src/main/java/platformConfigs/browserstack.properties");
			prop.load(input);
			
			String url = prop.getProperty("protocol")+
	    				 "://"+
	    				 prop.getProperty("username")+
	    				 ":"+
	    				 prop.getProperty("access_key")+
	    				 prop.getProperty("url");
			
			input.close();
			prop.clear();
	    	remoteDriverURL = new URL(url);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return new RemoteWebDriver(remoteDriverURL, capability);
    }
    
    private static WebDriver androidDriver(DesiredCapabilities capabilities) {
		String port = "4723";	
		try {
			driver = new AndroidDriver(new URL("http://127.0.0.1:"+port+"/wd/hub"),capabilities);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return driver;
    }
    
    private static WebDriver iosDriver(DesiredCapabilities capabilities)
    {
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
